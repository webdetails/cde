/*!
 * Copyright 2002 - 2015 Webdetails, a Pentaho company. All rights reserved.
 *
 * This software was developed by Webdetails and is provided under the terms
 * of the Mozilla Public License, Version 2.0, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */

package pt.webdetails.cdf.dd.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.text.DecimalFormat;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.dom4j.DocumentException;
import org.xml.sax.EntityResolver;

import pt.webdetails.cdf.dd.CdeEngine;
import pt.webdetails.cdf.dd.ICdeEnvironment;
import pt.webdetails.cpf.repository.api.FileAccess;
import pt.webdetails.cpf.repository.api.IBasicFile;
import pt.webdetails.cpf.repository.api.IContentAccessFactory;
import pt.webdetails.cpf.repository.api.IRWAccess;
import pt.webdetails.cpf.repository.api.IReadAccess;
import pt.webdetails.cpf.utils.CharsetHelper;

public class Utils {

  //  private static Log logger = LogFactory.getLog(Utils.class);

  private static final String NEWLINE = System.getProperty( "line.separator" );

  private static final DecimalFormat defaultNbrFormat = new DecimalFormat( "0.00" );

  public static String composeErrorMessage( String message, Exception cause ) {
    String msg = "";
    if ( StringUtils.isNotEmpty( message ) ) {
      msg += message;
    }

    if ( cause != null ) {
      if ( msg.length() > 0 ) {
        msg += NEWLINE;
      }
      msg += cause.getMessage();
    }

    return msg;
  }

  public static void main( String[] args ) {
    try {
      URI uri = new URI( "http://127.0.0.1:8080/pentaho/" );
      System.out.println( uri.getPath() );
      uri = new URI( "/pentaho/" );
      System.out.println( uri.getPath() );
      uri = new URI( "http://127.0.0.1:8080/pentaho" );
      System.out.println( uri.getPath() );
      uri = new URI( "/pentaho" );
      System.out.println( uri.getPath() );
    } catch ( URISyntaxException ex ) {
      Logger.getLogger( Utils.class.getName() ).log( Level.SEVERE, null, ex );
    }
  }

  public static String joinPath( String... paths ) {
    return StringUtils.defaultString( StringUtils.join( paths, "/" ) ).replaceAll( "/+", "/" );
  }

  public static String toFirstLowerCase( String text ) {
    if ( text == null ) {
      return null;
    }

    return text.length() > 1
      ? text.substring( 0, 1 ).toLowerCase() + text.substring( 1 )
      : text.toLowerCase();
  }

  public static String toFirstUpperCase( String text ) {
    if ( text == null ) {
      return null;
    }

    return text.length() > 1
      ? text.substring( 0, 1 ).toUpperCase() + text.substring( 1 )
      : text.toUpperCase();
  }

  public static String ellapsedSeconds( long start ) {
    return defaultNbrFormat.format( ( System.currentTimeMillis() - start ) / 1000.0 );
  }

  // Must start with a / and all \ are converted to / and has no duplicate /s.
  // When empty, returns empty
  public static String sanitizeSlashesInPath( String path ) {
    if ( StringUtils.isEmpty( path ) ) {
      return "";
    }
    return ( "/" + path )
      .replaceAll( "\\\\+", "/" )
      .replaceAll( "/+", "/" );
  }

  public static class PathResolutionException extends RuntimeException {

    private static final long serialVersionUID = 1838386876452885975L;

    PathResolutionException( String msg ) {
      super( msg );
    }
  }

  public static String getNodeText( final String xpath, final Node node ) {
    return getNodeText( xpath, node, null );
  }

  public static String getNodeText( final String xpath, final Node node, final String defaultValue ) {
    if ( node == null ) {
      return defaultValue;
    }
    Node n = node.selectSingleNode( xpath );
    if ( n == null ) {
      return defaultValue;
    }
    return n.getText();
  }

  /**
   * Create a <code>Document</code> from the contents of a file.
   *
   * @param file
   * @param resolver EntityResolver an instance of an EntityResolver that will resolve any external URIs. See the docs
   *                 on EntityResolver. null is an acceptable value.
   * @return <code>Document</code> initialized with the xml in <code>strXml</code>.
   * @throws DocumentException if the document isn't valid
   * @throws IOException       if the file doesn't exist
   */
  public static Document getDocFromFile( final IBasicFile file, final EntityResolver resolver )
    throws DocumentException, IOException {
    SAXReader reader = new SAXReader();
    if ( resolver != null ) { //TODO: this is always being called with null
      reader.setEntityResolver( resolver );
    }
    return reader.read( file.getContents() );
  }

  public static Document getDocument( InputStream input ) throws DocumentException {
    SAXReader reader = new SAXReader();
    return reader.read( input );
  }

  @SuppressWarnings( "unchecked" )
  public static List<Element> selectNodes( Element elem, String xpath ) {
    return elem.selectNodes( xpath );
  }

  @SuppressWarnings( "unchecked" )
  public static List<Element> selectNodes( Document doc, String xpath ) {
    return doc.selectNodes( xpath );
  }

  public static Document getDocFromFile( final IReadAccess access, final String filePath,
                                         final EntityResolver resolver ) throws DocumentException, IOException {
    return ( access != null && filePath != null ? getDocFromFile( access.fetchFile( filePath ), resolver ) : null );
  }

  public static IReadAccess getAppropriateReadAccess( String resource ) {
    return getAppropriateReadAccess( resource, null );
  }

  public static IReadAccess getAppropriateReadAccess( String resource, String basePath ) {
    return getAppropriateReadAccess( resource, basePath, getCdeEnvironment() );
  }

  public static IReadAccess getAppropriateReadAccess( String resource, String basePath, ICdeEnvironment environment ) {

    if ( StringUtils.isEmpty( resource ) ) {
      return null;
    }

    IContentAccessFactory factory = environment.getContentAccessFactory();

    String systemDir = environment.getSystemDir() + "/";
    String repoDir = environment.getPluginRepositoryDir() + "/";

    resource = StringUtils.strip( resource, "/" );

    if ( resource.regionMatches( true, 0, systemDir, 0, systemDir.length() ) ) {

      resource = resource.replaceFirst( systemDir, "" );

      String pluginId = environment.getPluginId() + "/";
      // system dir - this plugin
      if ( resource.regionMatches( true, 0, pluginId, 0, pluginId.length() ) ) {
        return factory.getPluginSystemReader( basePath );

      } else {
        // system dir - other plugin
        pluginId = resource.substring( 0, resource.indexOf( "/" ) );
        return factory.getOtherPluginSystemReader( pluginId, basePath );

      }

    } else if ( resource.regionMatches( true, 0, repoDir, 0, repoDir.length() ) ) {

      // plugin repository dir
      return factory.getPluginRepositoryReader( basePath );

    } else {

      // one of two: already trimmed system resource (ex: 'resources/templates/1-empty-structure.cdfde')
      // or a user solution resource (ex: 'plugin-samples/pentaho-cdf-dd/styles/my-style.css')

      if ( factory.getPluginSystemReader( basePath ).fileExists( resource ) ) {
        return factory.getPluginSystemReader( basePath );
      } else {
        // user solution dir
        return factory.getUserContentAccess( basePath );
      }
    }
  }

  public static IReadAccess getSystemReadAccess( String pluginId, String basePath ) {
    return getSystemReadAccess( pluginId, basePath, getCdeEnvironment() );
  }

  public static IReadAccess getSystemReadAccess( String pluginId, String basePath, ICdeEnvironment environment ) {
    IContentAccessFactory factory = environment.getContentAccessFactory();
    if ( StringUtils.isEmpty( pluginId ) ) {
      return factory.getPluginSystemReader( basePath );
    } else {
      return factory.getOtherPluginSystemReader( pluginId, basePath );
    }
  }

  public static IRWAccess getSystemRWAccess( String pluginId, String basePath ) {
    return getSystemRWAccess( pluginId, basePath, getCdeEnvironment() );
  }

  public static IRWAccess getSystemRWAccess( String pluginId, String basePath, ICdeEnvironment environment ) {
    IContentAccessFactory factory = environment.getContentAccessFactory();
    if ( StringUtils.isEmpty( pluginId ) ) {
      return factory.getPluginSystemWriter( basePath );
    } else {
      return factory.getOtherPluginSystemWriter( pluginId, basePath );
    }
  }

  public static IRWAccess getAppropriateWriteAccess( String resource ) {
    return getAppropriateWriteAccess( resource, null );
  }

  public static IRWAccess getAppropriateWriteAccess( String resource, String basePath ) {
    return getAppropriateWriteAccess( resource, basePath, getCdeEnvironment() );
  }

  public static IRWAccess getAppropriateWriteAccess( String resource, String basePath, ICdeEnvironment environment ) {

    if ( StringUtils.isEmpty( resource ) ) {
      return null;
    }

    IContentAccessFactory factory = environment.getContentAccessFactory();

    String systemDir = environment.getSystemDir() + "/";
    String repoDir = environment.getPluginRepositoryDir() + "/";

    resource = StringUtils.strip( resource, "/" );

    if ( resource.regionMatches( true, 0, systemDir, 0, systemDir.length() ) ) {

      resource = resource.replaceFirst( systemDir, "" );

      String pluginId = environment.getPluginId() + "/";
      // system dir - this plugin
      if ( resource.regionMatches( true, 0, pluginId, 0, pluginId.length() ) ) {
        return factory.getPluginSystemWriter( basePath );

      } else {
        // system dir - other plugin
        pluginId = resource.substring( 0, resource.indexOf( "/" ) );
        return factory.getOtherPluginSystemWriter( pluginId, basePath );

      }

    } else if ( resource.regionMatches( true, 0, repoDir, 0, repoDir.length() ) ) {

      // plugin repository dir
      return factory.getPluginRepositoryWriter( basePath );

    } else {

      // one of two: already trimmed system resource (ex: 'resources/templates/1-empty-structure.cdfde')
      // or a user solution resource (ex: 'plugin-samples/pentaho-cdf-dd/styles/my-style.css')

      if ( factory.getPluginSystemReader( basePath ).fileExists( resource ) ) {
        return factory.getPluginSystemWriter( basePath );
      } else {
        // user solution dir
        return factory.getUserContentAccess( basePath );
      }
    }
  }

  public static IBasicFile getFileViaAppropriateReadAccess( String resource ) {
    return getFileViaAppropriateReadAccess( resource, null );
  }

  public static IBasicFile getFileViaAppropriateReadAccess( String resource, String basePath ) {
    return getFileViaAppropriateReadAccess( resource, basePath, getCdeEnvironment() );
  }

  public static IBasicFile getFileViaAppropriateReadAccess( String resource, String basePath,
                                                            ICdeEnvironment environment ) {
    if ( StringUtils.isEmpty( resource ) ) {
      return null;
    }

    IContentAccessFactory factory = environment.getContentAccessFactory();

    String systemDir = environment.getSystemDir() + "/";
    String repoDir = environment.getPluginRepositoryDir() + "/";

    resource = StringUtils.strip( resource, "/" );

    if ( resource.regionMatches( true, 0, systemDir, 0, systemDir.length() ) ) {

      resource = resource.replaceFirst( systemDir, "" );

      String pluginId = environment.getPluginId() + "/";
      // system dir - this plugin
      if ( resource.regionMatches( true, 0, pluginId, 0, pluginId.length() ) ) {

        resource = resource.replaceFirst( pluginId, "" );

        return factory.getPluginSystemReader( basePath ).fetchFile( resource );

      } else {
        // system dir - other plugin
        pluginId = resource.substring( 0, resource.indexOf( "/" ) );
        resource = resource.replaceFirst( pluginId, "" );

        return factory.getOtherPluginSystemReader( pluginId, basePath ).fetchFile( resource );
      }

    } else if ( resource.regionMatches( true, 0, repoDir, 0, repoDir.length() ) ) {

      // plugin repository dir
      resource = resource.replaceFirst( repoDir, "" );
      return factory.getPluginRepositoryReader( basePath ).fetchFile( resource );

    } else {

      // one of two: already trimmed system resource (ex: 'resources/templates/1-empty-structure.cdfde')
      // or a user solution resource (ex: 'plugin-samples/pentaho-cdf-dd/styles/my-style.css')

      if ( factory.getPluginSystemReader( basePath ).fileExists( resource ) ) {
        return factory.getPluginSystemReader( basePath ).fetchFile( resource );

      } else if ( factory.getUserContentAccess( basePath ).fileExists( resource ) ) {
        // user solution dir
        return factory.getUserContentAccess( basePath ).fetchFile( resource );
      }
    }
    return null;
  }

  public static IReadAccess getSystemOrUserReadAccess( String filePath ) {
    IReadAccess readAccess = null;
    if ( filePath.startsWith( "/" + CdeEnvironment.getSystemDir() + "/" ) && ( filePath.endsWith( ".wcdf" ) || filePath
        .endsWith( ".cdfde" ) ) ) {
      readAccess = getSystemReadAccess( filePath.split( "/" )[2], null );
    } else if ( CdeEnvironment.getUserContentAccess().hasAccess( filePath, FileAccess.EXECUTE ) ) {
      readAccess = CdeEnvironment.getUserContentAccess();
    }
    return readAccess;
  }

  public static IRWAccess getSystemOrUserRWAccess( String filePath ) {
    IRWAccess rwAccess = null;
    if ( CdeEngine.getEnv().getUserSession().isAdministrator() && (
        filePath.startsWith( "/" + CdeEnvironment.getSystemDir() + "/" ) && ( filePath.endsWith( ".wcdf" ) || filePath
        .endsWith( ".cdfde" ) ) ) ) {
      rwAccess = getSystemRWAccess( filePath.split( "/" )[2], null );
    } else if ( CdeEnvironment.getUserContentAccess().fileExists( filePath ) ) {

      if ( CdeEnvironment.getUserContentAccess().hasAccess( filePath, FileAccess.WRITE ) ) {
        rwAccess = CdeEnvironment.getUserContentAccess();
      } else {
        return null;
      }
    } else if ( CdeEnvironment.getUserContentAccess()
        .hasAccess( "/" + FilenameUtils.getPath( filePath ), FileAccess.WRITE ) ) {
      // if file does not exist yet (ex: 'save as...'), then hasAccess method will not work on the file itself;
      // it should be checked against destination folder
      rwAccess = CdeEnvironment.getUserContentAccess();
    }
    return rwAccess;

  }

  public static boolean isValidJsonArray( String jsonString ) {
    try {
      JSONArray.fromObject( jsonString );
      return true;
    } catch ( JSONException e ) {
      return false;
    }
  }

  /**
   * Gets the name of the class of the component based on it's type.
   *
   * @param componentType
   * @return
   */
  public static String getComponentClassName( String componentType ) {

    if ( !StringUtils.isNotEmpty( componentType ) ) {
      return componentType;
    }

    StringBuilder sb = new StringBuilder();

    // starts with upper case character
    if ( !Character.isUpperCase( componentType.charAt( 0 ) ) ) {
      sb.append( Character.toUpperCase( componentType.charAt( 0 ) ) )
          .append( componentType.substring( 1 ) );
    } else {
      sb.append( componentType );
    }

    // ends with "Component"
    if ( !componentType.endsWith( "Component" ) ) {
      sb.append( "Component" );
    }

    return sb.toString();
  }

  public static ICdeEnvironment getCdeEnvironment() {
    return CdeEngine.getInstance().getEnvironment();
  }

  public static String getURLDecoded( String s ){
    return getURLDecoded( s , CharsetHelper.getEncoding() );
  }

  public static String getURLDecoded( String s, String enc ){
    if( s != null ){
      try {
        return URLDecoder.decode( s, ( enc != null ? enc : CharsetHelper.getEncoding() ) );
      } catch ( Exception e ){
        /* do nothing, assume this value as-is */
      }
    }
    return s;
  }

}
