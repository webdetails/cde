package pt.webdetails.cdf.dd.packager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.text.ParseException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pt.webdetails.cdf.dd.packager.input.CssUrlReplacer;
import pt.webdetails.cdf.dd.packager.input.StaticSystemOrigin;
import pt.webdetails.cdf.dd.render.StringFilter;
import pt.webdetails.cpf.Util;
import pt.webdetails.cpf.repository.api.IContentAccessFactory;
import pt.webdetails.cpf.repository.api.IRWAccess;
import pt.webdetails.cpf.repository.util.RepositoryHelper;

/**
 * A set of css|js files that can be packaged into a single file.
 */
public class DependenciesPackage {

  static Log logger = LogFactory.getLog(DependenciesPackage.class);

  public enum PackagingMode
  {
    MINIFY, CONCATENATE
  };

  //FIXME TODO raw dependencies, minified file output, use in DependenciesManager, check streams
  private String name;
  private Map<String, FileDependency> fileDependencies;
  private PackagedFileDependency packagedDependency;
  private Packager.Filetype type;

  private Map<String, SnippetDependency> rawDependencies;
  private IContentAccessFactory factory;

  //TODO: type
  public DependenciesPackage(String name, Packager.Filetype type, IContentAccessFactory factory) {
    this.name = name;
    fileDependencies = new HashMap<String, DependenciesPackage.FileDependency>();
    rawDependencies = new HashMap<String, DependenciesPackage.SnippetDependency>();
    this.type = type;
    this.factory = factory;

  }

  /**
   * Registers a dependency in this package
   * @param name
   * @param version
   * @param origin
   * @param path
   * @return
   */
  public boolean registerFileDependency(String name, String version, PathOrigin origin, String path) {
    FileDependency newDep = new FileDependency( version, origin, path);
    FileDependency dep = fileDependencies.get( name );
    
    if (dep == null || dep.isOlderVersionThan( newDep )) {
      fileDependencies.put( name, newDep );
      //invalidate packaged if there
      packagedDependency = null;
    }
    return false;
  }

  //TODO: entryPoint
  public String getDependencies(StringFilter filter, boolean isPackaged) {
    return null;
  }

  public String getName() {
    return name;
  }

  protected String getUnpackagedDependencies(StringFilter format) {
    StringBuilder output = new StringBuilder();
    for (Dependency dep : fileDependencies.values()) {
      output.append((format == null ? format : format).filter(dep.getDependencyInclude()) + format);
    }
    return output.toString();
  }

  protected String getPackagedDependency() {
    if (packagedDependency == null) {
      String packagedPath = name + "." + type.toString().toLowerCase();
      IRWAccess writer = factory.getPluginSystemWriter( packagedPath );
      PathOrigin origin = new StaticSystemOrigin( type.toString().toLowerCase() );
      switch ( type ) {
        case CSS:
          packagedDependency = new CssMinifiedDependency( origin, packagedPath, writer, fileDependencies.values() );
          break;
        case JS:
          packagedDependency = new JsMinifiedDependency( origin, packagedPath, writer, fileDependencies.values() );
          break;
        default:
          break;//TODO:
      }
    }
    return packagedDependency.getDependencyInclude();
  }

  //------------------------------------------------------------------------------
  //TODO: move to own files
  private abstract class Dependency
  {
    protected String version;
//    protected String hash;

    protected Dependency(String version)
    {
      this.version = version;
    }

    public boolean isOlderVersionThan(Dependency other) {
      // assuming version numberings always increase lexicographically
      return this.version == null || this.version.compareTo( other.version ) < 0;
    }

    //TODO: does it make sense to have the same for both?
    abstract public String getDependencyInclude();

    abstract public String getContents() throws IOException;
  }
  
  private class FileDependency extends Dependency {
    
    protected String filePath;
    protected PathOrigin origin;
    private String hash;

    public FileDependency(String version, PathOrigin origin, String path) {
      super(version);
      this.filePath = path;
      this.hash = null;
      this.origin = origin;
    }


    public String getCheckSum() {
      if (hash == null) {
        InputStream in = null;
        try {
          in = getFileInputStream();
          hash = Util.getMd5Digest( in );
        }
        catch (IOException e) {
          logger.error( "Could not compute md5 checksum.", e);
        }
        finally {
          IOUtils.closeQuietly( in );
        }
      }
      return hash;
    }

    public InputStream getFileInputStream() throws IOException {
      return origin.getReader( factory ).getFileInputStream( filePath );
    }

    /**
     * @return path for including this file
     */
    public String getDependencyInclude()
    {
      // the ?v=<hash> is used to bypass browser cache when needed
      String md5 = getCheckSum();
      String file = filePath + ((md5 == null) ? "" : "?v=" + md5);
      // translate local path to a url path
      return origin.getUrlPrepend(file);
    }

    @Override
    public String getContents() throws IOException {
      return Util.toString( origin.getReader( factory ).getFileInputStream( filePath ) );
    }

    public String getUrlFilePath() {
      return origin.getUrlPrepend(filePath);
    }

//    public PathOrigin getOrigin() {
//      return origin;
//    }

  }

  /**
   * A code snippet
   */
  private class SnippetDependency extends Dependency
  {

    private String content;

    public SnippetDependency(String version, String contents)
    {
      super(version);
      this.content = contents;
    }

    /**
     * @return raw snippet
     */
    public String getDependencyInclude()
    {
      return content;
    }

    @Override
    public String getContents() {
      return content;
    }
  }

  protected abstract class PackagedFileDependency extends FileDependency {

    private Iterable<FileDependency> inputFiles;
    private IRWAccess writer;
//    protected byte[] contents;
    private boolean isSaved;

    public PackagedFileDependency(PathOrigin origin, String path, IRWAccess writer, Iterable<FileDependency> inputFiles) {
      super( "", origin, path );
      this.inputFiles = inputFiles;
      this.writer = writer;
    }

    @Override
    public synchronized InputStream getFileInputStream() throws IOException {
      if ( !isSaved ) {
        writer.saveFile( filePath, minifyPackage(inputFiles));//TODO: check save
        inputFiles = null;
      }
      return super.getFileInputStream();
    }

    protected abstract InputStream minifyPackage(Iterable<FileDependency> inputFiles);

    public boolean isOlderVersionThan(Dependency other) {
      return true;//TODO:
    }
  }

  
  public static class JsMinificationEnumeration implements Enumeration<InputStream> {

    private Iterator<FileDependency> deps;

    public JsMinificationEnumeration(Iterator<FileDependency> deps) {
      this.deps = deps;
    }

    @Override
    public boolean hasMoreElements() {
      return deps.hasNext();
    }

    @Override
    public InputStream nextElement() {
      FileDependency dep = deps.next();
      try {
        InputStream input = dep.getFileInputStream();
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        JSMin jsMin = new JSMin( input, bytesOut );
        jsMin.jsmin();
        return new ByteArrayInputStream( bytesOut.toByteArray() );
      } catch ( ParseException e ) {
        logger.error( "Error parsing javascript dependency " + dep + " at offset " + e.getErrorOffset() + ", skipping..", e );
      } catch ( IOException e ) {
        logger.error( "Error getting input stream for dependency " + dep + ", skipping..", e );
      }
      return Util.toInputStream( "" );
    }

  }

  public class JsMinifiedDependency extends PackagedFileDependency {

    public JsMinifiedDependency( PathOrigin origin, String path, IRWAccess writer, Iterable<FileDependency> inputFiles ) {
      super( origin, path, writer, inputFiles );
    }

    @Override
    protected InputStream minifyPackage(Iterable<FileDependency> inputFiles) {
      return new SequenceInputStream( new JsMinificationEnumeration( inputFiles.iterator()) );
    }
  }

  public static interface FileUrlPathPair {
    InputStream getInputStream();
    String getUrlDisplacement();
  }

  public static class CssReplacementStreamEnumeration implements Enumeration<InputStream> {

    private Iterator<FileDependency> deps;
    private String outputFolderUrlPath;
    private CssUrlReplacer replacer;

    public CssReplacementStreamEnumeration(String outputFolderUrlPath, Iterator<FileDependency> deps) {
      this.deps = deps;
      this.outputFolderUrlPath = outputFolderUrlPath;
      this.replacer = new CssUrlReplacer();
    }

    @Override
    public boolean hasMoreElements() {
      return deps.hasNext();
    }

    @Override
    public InputStream nextElement() {
      FileDependency dep = deps.next();
      try {

        String contents = Util.toString( dep.getFileInputStream() );
        //strip filename from url
        String originalUrlPath = FilenameUtils.getPath(dep.getUrlFilePath());
        String pathChange = RepositoryHelper.relativizePath( outputFolderUrlPath, originalUrlPath, true );
        contents = replacer.processContents( contents, pathChange );
        return Util.toInputStream( contents );
      } catch ( IOException e ) {
        // TODO Auto-generated catch block
        logger.error("Error getting input stream for dependency " + dep +", skipping", e);
        return Util.toInputStream( "" );
      }
    }
  }



  public class CssMinifiedDependency extends PackagedFileDependency {

    public CssMinifiedDependency( PathOrigin origin, String path, IRWAccess writer, Iterable<FileDependency> inputFiles ) {
      super( origin, path, writer, inputFiles );
    }

    @Override
    protected InputStream minifyPackage( Iterable<FileDependency> inputFiles ) {
      return new SequenceInputStream( new CssReplacementStreamEnumeration(filePath, inputFiles.iterator()) );
    }
  }

  
}
