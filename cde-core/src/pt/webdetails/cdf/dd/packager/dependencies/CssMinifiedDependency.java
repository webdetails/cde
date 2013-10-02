/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.packager.dependencies;

import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.Enumeration;
import java.util.Iterator;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pt.webdetails.cdf.dd.packager.PathOrigin;
import pt.webdetails.cdf.dd.packager.input.CssUrlReplacer;
import pt.webdetails.cpf.Util;
import pt.webdetails.cpf.repository.api.IRWAccess;


public class CssMinifiedDependency extends PackagedFileDependency {

  private static Log logger = LogFactory.getLog(CssMinifiedDependency.class);

  public CssMinifiedDependency( PathOrigin origin, String path, IRWAccess writer, Iterable<FileDependency> inputFiles ) {
    super( origin, path, writer, inputFiles );
  }

  @Override
  protected InputStream minifyPackage( Iterable<FileDependency> inputFiles ) {
    return new SequenceInputStream( new CssReplacementStreamEnumeration(inputFiles.iterator()) );
  }
  
  public static class CssReplacementStreamEnumeration implements Enumeration<InputStream> {

    private Iterator<FileDependency> deps;
    private CssUrlReplacer replacer;

    public CssReplacementStreamEnumeration(Iterator<FileDependency> deps) {
      this.deps = deps;
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
        String originalUrlPath = FilenameUtils.getFullPath(dep.getUrlFilePath());
        contents = replacer.processContents( contents, originalUrlPath );
        return Util.toInputStream( contents );
      } catch ( IOException e ) {
        logger.error("Error getting input stream for dependency " + dep +", skipping", e);
        return Util.toInputStream( "" );
      }
    }
  }
}
