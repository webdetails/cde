/*!
 * Copyright 2002 - 2018 Webdetails, a Hitachi Vantara company. All rights reserved.
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
import java.text.MessageFormat;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.dom4j.DocumentException;
import org.json.JSONArray;
import org.json.JSONException;
import org.xml.sax.EntityResolver;

import pt.webdetails.cdf.dd.CdeConstants;
import pt.webdetails.cdf.dd.CdeEngine;
import pt.webdetails.cdf.dd.ICdeEnvironment;
import pt.webdetails.cdf.dd.structure.DashboardWcdfDescriptor;
import pt.webdetails.cpf.Util;
import pt.webdetails.cpf.repository.api.FileAccess;
import pt.webdetails.cpf.repository.api.IBasicFile;
import pt.webdetails.cpf.repository.api.IContentAccessFactory;
import pt.webdetails.cpf.repository.api.IRWAccess;
import pt.webdetails.cpf.repository.api.IReadAccess;
import pt.webdetails.cpf.utils.CharsetHelper;
import pt.webdetails.cpf.utils.XmlParserFactoryProducer;

public class Utils {
  private static final Log logger = LogFactory.getLog( Utils.class );

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
      logger.error( ex.getMessage(), ex );
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
   * @param file the file with the <code>Document</code> content
   * @param resolver EntityResolver an instance of an EntityResolver that will resolve any external URIs. See the docs
   *                 on EntityResolver. null is an acceptable value.
   * @return <code>Document</code> initialized with the xml in <code>strXml</code>.
   * @throws DocumentException if the document isn't valid
   * @throws IOException       if the file doesn't exist
   */
  public static Document getDocFromFile( final IBasicFile file, final EntityResolver resolver )
    throws DocumentException, IOException {
    SAXReader reader = XmlParserFactoryProducer.getSAXReader( resolver );
    return reader.read( file.getContents() );
  }

  public static Document getDocument( InputStream input ) throws DocumentException {
    SAXReader reader = XmlParserFactoryProducer.getSAXReader( null );
    return reader.read( input );
  }

  @Deprecated
  public static List<Node> selectNodes( Element elem, String xpath ) {
    return elem.selectNodes( xpath );
  }

  @Deprecated
  public static List<Node> selectNodes( Document doc, String xpath ) {
    return doc.selectNodes( xpath );
  }

  @SuppressWarnings( "unchecked" )
  public static List<Element> selectElements( Element elem, String xpath ) {
    return (List<Element>) (List<?>) selectNodes( elem, xpath );
  }

  @SuppressWarnings( "unchecked" )
  public static List<Element> selectElements( Document doc, String xpath ) {
    return (List<Element>) (List<?>) selectNodes( doc, xpath );
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
    if ( isSystemDashboard( filePath ) ) {
      readAccess = getSystemReadAccess( filePath.split( "/" )[ 2 ], null );
    } else if ( CdeEnvironment.getUserContentAccess().hasAccess( filePath, FileAccess.EXECUTE ) ) {
      readAccess = CdeEnvironment.getUserContentAccess();
    }
    return readAccess;
  }

  public static IRWAccess getSystemOrUserRWAccess( String filePath ) {
    if ( CdeEnvironment.isAdministrator() && isSystemDashboard( filePath ) ) {
      return getSystemRWAccess( filePath.split( "/" )[ 2 ], null );
    }
    return getUserRWAccess( filePath );
  }

  public static IRWAccess getUserRWAccess( String filePath ) {
    if ( CdeEnvironment.getUserContentAccess().fileExists( filePath ) ) {
      if ( CdeEnvironment.canCreateContent()
          && CdeEnvironment.getUserContentAccess().hasAccess( filePath, FileAccess.WRITE ) ) {
        return CdeEnvironment.getUserContentAccess();
      } else {
        return null;
      }
    } else if ( CdeEnvironment.canCreateContent() && CdeEnvironment.getUserContentAccess()
        .hasAccess( "/" + FilenameUtils.getPath( filePath ), FileAccess.WRITE ) ) {
      // if file does not exist yet (ex: 'save as...'), then hasAccess method will not work on the file itself;
      // it should be checked against destination folder
      return CdeEnvironment.getUserContentAccess();
    }
    return null;
  }

  public static boolean isValidJsonArray( String jsonString ) {
    try {
      new JSONArray( jsonString );
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

  public static String getURLDecoded( String s ) {
    return getURLDecoded( s, CharsetHelper.getEncoding() );
  }

  public static String getURLDecoded( String s, String enc ) {
    if ( s != null ) {
      try {
        return URLDecoder.decode( s, ( enc != null ? enc : CharsetHelper.getEncoding() ) );
      } catch ( Exception e ) {
        /* do nothing, assume this value as-is */
      }
    }
    return s;
  }

  public static String readTemplate( DashboardWcdfDescriptor wcdf ) throws IOException {
    return readStyleTemplateOrDefault( wcdf.getStyle() );
  }

  public static String readStyleTemplateOrDefault( String styleName ) throws IOException {
    if ( StringUtils.isNotEmpty( styleName ) ) {
      try {
        return readStyleTemplate( styleName );
      } catch ( IOException ex ) {
        logger.debug( ex.getMessage() );
      }
    }

    // Couldn't open template file, attempt to use default
    return readStyleTemplate( CdeConstants.DEFAULT_STYLE );
  }

  public static String readStyleTemplate( String styleName ) throws IOException {
    String location = CdeEnvironment.getPluginResourceLocationManager().getStyleResourceLocation( styleName );
    if ( StringUtils.isEmpty( location ) ) {
      logger.error( MessageFormat.format( "Couldn''t find style template file ''{0}'', will fallback to ''{1}''",
          styleName, CdeConstants.DEFAULT_STYLE ) );
      location =
        CdeEnvironment.getPluginResourceLocationManager().getStyleResourceLocation( CdeConstants.DEFAULT_STYLE );
    }
    return readTemplateFile( location );
  }

  public static String readTemplateFile( String templateFile ) throws IOException {
    try {
      if ( CdeEnvironment.getPluginRepositoryReader().fileExists( templateFile ) ) {
        // template is in solution repository
        return Util.toString( CdeEnvironment.getPluginRepositoryReader().getFileInputStream( templateFile ) );

      } else if ( CdeEnvironment.getPluginSystemReader().fileExists( templateFile ) ) {
        // template is in system
        return Util.toString( CdeEnvironment.getPluginSystemReader().getFileInputStream( templateFile ) );
      } else if ( Utils.getAppropriateReadAccess( templateFile ).fileExists( templateFile ) ) {
        return Util.toString( Utils.getAppropriateReadAccess( templateFile ).getFileInputStream( templateFile ) );
      } else {
        // last chance : template is in user-defined folder
        return Util.toString( CdeEnvironment.getUserContentAccess().getFileInputStream( templateFile ) );
      }
    } catch ( IOException ex ) {
      logger.error( MessageFormat.format( "Couldn't open template file '{0}'.", templateFile ), ex );
      throw ex;
    }
  }

  public static boolean isSystemDashboard( String path ) {
    return ( path.startsWith( "/" + CdeEnvironment.getSystemDir() + "/" ) && ( path.endsWith( ".wcdf" )
        || path.endsWith( ".cdfde" ) ) );
  }

  public static String getWcdfReposPath( String path ) {
    return CdeEngine.getEnv().getApplicationReposUrl() + toRepositoryPath( path );
  }

  public static  String toRepositoryPath( String path ) {
    if ( !path.startsWith( "/" ) ) {
      path = "/" + path;
    }
    return path.replaceAll( "/", ":" );
  }

}
