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

package pt.webdetails.cdf.dd.structure;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;

import org.json.JSONObject;
import pt.webdetails.cdf.dd.Messages;
import pt.webdetails.cdf.dd.util.CdeEnvironment;
import pt.webdetails.cdf.dd.util.JsonUtils;
import pt.webdetails.cdf.dd.DashboardManager;
import pt.webdetails.cdf.dd.model.meta.MetaModel;
import pt.webdetails.cdf.dd.MetaModelManager;
import pt.webdetails.cdf.dd.model.meta.PropertyType;
import pt.webdetails.cdf.dd.model.core.UnsupportedThingException;
import pt.webdetails.cdf.dd.model.core.reader.ThingReadException;
import pt.webdetails.cdf.dd.model.core.validation.ValidationException;
import pt.webdetails.cdf.dd.model.core.writer.DefaultThingWriteContext;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriteContext;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriter;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriterFactory;
import pt.webdetails.cdf.dd.model.core.writer.ThingWriteException;
import pt.webdetails.cdf.dd.model.inst.Dashboard;
import pt.webdetails.cdf.dd.model.inst.writer.cggrunjs.CggRunJsDashboardWriteContext;
import pt.webdetails.cdf.dd.model.inst.writer.cggrunjs.CggRunJsThingWriterFactory;
import pt.webdetails.cdf.dd.model.meta.IPropertyTypeSource;
import pt.webdetails.cdf.dd.model.meta.WidgetComponentType;
import pt.webdetails.cdf.dd.model.meta.writer.cdexml.XmlThingWriterFactory;
import pt.webdetails.cdf.dd.render.CdaRenderer;
import pt.webdetails.cdf.dd.util.Utils;
import pt.webdetails.cpf.repository.api.IRWAccess;
import pt.webdetails.cpf.repository.api.IReadAccess;
import pt.webdetails.cpf.utils.CharsetHelper;

public class DashboardStructure implements IDashboardStructure {
  private static Log logger = LogFactory.getLog( DashboardStructure.class );

  public static String SYSTEM_PLUGIN_EMPTY_STRUCTURE_FILE_PATH = "resources/empty-structure.json";
  public static String SYSTEM_PLUGIN_EMPTY_WCDF_FILE_PATH = "/system/pentaho-cdf-dd/resources/empty.wcdf";

  public DashboardStructure() {
  }

  public void delete( HashMap<String, Object> parameters ) throws DashboardStructureException {
    // 1. Delete File
    String filePath = (String) parameters.get( "file" );

    logger.info( "Deleting File:" + filePath );

    if ( !Utils.getSystemOrUserRWAccess( filePath ).deleteFile( filePath ) ) {
      throw new DashboardStructureException(
        Messages.getString( "DashboardStructure.ERROR_007_DELETE_FILE_EXCEPTION" ) );
    } else {
      logger.info( "Deleted file " + filePath );
    }
  }

  public void deletePreviewFiles( String cdeFilePath ) throws DashboardStructureException {
    IRWAccess access = Utils.getSystemOrUserRWAccess( cdeFilePath );

    if ( access == null ) {
      throw new DashboardStructureException(
        Messages.getString( "XmlStructure.ERROR_011_READ_WRITE_ACCESS_EXCEPTION" ) );
    }

    access.deleteFile( cdeFilePath.replace( ".cdfde", "_tmp.cdfde" ) );
    access.deleteFile( cdeFilePath.replace( ".cdfde", "_tmp.wcdf" ) );
    access.deleteFile( cdeFilePath.replace( ".cdfde", "_tmp.cda" ) );
  }

  /**
   * @returns a standard json result obj (?)
   */
  public String load( String cdeFilePath ) throws Exception {
    InputStream cdeFileInput = null;

    try {

      logger.info( "Loading File:" + cdeFilePath );

      // 1. Read .CDFDE file

      String wcdfFilePath = null;
      if ( StringUtils.isEmpty( cdeFilePath ) ) {
        cdeFileInput =
          CdeEnvironment.getPluginSystemReader().getFileInputStream( SYSTEM_PLUGIN_EMPTY_STRUCTURE_FILE_PATH );
        wcdfFilePath = SYSTEM_PLUGIN_EMPTY_WCDF_FILE_PATH;
      } else {
        IReadAccess access = Utils.getSystemOrUserReadAccess( cdeFilePath );
        if ( access.fileExists( cdeFilePath ) ) {
          cdeFileInput = access.getFileInputStream( cdeFilePath );
          wcdfFilePath = cdeFilePath.replace( ".cdfde", ".wcdf" );
        } else {
          String msg = "File not found " + cdeFilePath + " in " + access;
          logger.error( msg );
          return JsonUtils.getJsonResult( false, msg );
        }
      }

      JSONObject cdeData = JsonUtils.readJsonFromInputStream( cdeFileInput );
      JSONObject wcdfData = loadWcdfDescriptor( wcdfFilePath ).toJSON();
      // 3. Read .WCDF
      JSONObject result = new JSONObject();
      result.put( "wcdf", wcdfData );
      result.put( "data", cdeData );
      return JsonUtils.getJsonResult( true, result );
    } catch ( Exception t ) {
      throw new DashboardStructureException(
        Messages.getString( "DashboardStructure.ERROR_003_LOAD_READING_FILE_EXCEPTION" ) );
    } finally {
      IOUtils.closeQuietly( cdeFileInput );
    }

  }

  public DashboardWcdfDescriptor loadWcdfDescriptor( String wcdfFilePath ) throws IOException {
    DashboardWcdfDescriptor wcdf = DashboardWcdfDescriptor.load( wcdfFilePath );

    return wcdf != null ? wcdf : new DashboardWcdfDescriptor();
  }

  public DashboardWcdfDescriptor loadWcdfDescriptor( Document wcdfDoc ) {
    return DashboardWcdfDescriptor.fromXml( wcdfDoc );
  }

  /**
   * @deprecated
   */
  public HashMap<String, String> save( HashMap<String, Object> parameters ) throws Exception {
    String cdeFilePath = (String) parameters.get( "file" );
    String cdfdeJsText = (String) parameters.get( "cdfstructure" );

    return save( cdeFilePath, cdfdeJsText );
  }

  public HashMap<String, String> save( String cdeFilePath, String cdfdeJsText ) throws Exception {
    final HashMap<String, String> result = new HashMap<>();

    logger.info( "Saving File:" + cdeFilePath );

    IRWAccess access = Utils.getSystemOrUserRWAccess( cdeFilePath );
    boolean isPreview = cdeFilePath.contains( "_tmp.cdfde" );

    // 1. CDE
    if ( !access.saveFile( cdeFilePath, new ByteArrayInputStream( safeGetEncodedBytes( cdfdeJsText ) ) ) ) {
      throw new DashboardStructureException(
        Messages.getString( "DashboardStructure.ERROR_006_SAVE_FILE_ADD_FAIL_EXCEPTION" ) );
    }

    // 2. CDA
    CdaRenderer cdaRenderer = new CdaRenderer( cdfdeJsText );

    String cdaFileName = cdeFilePath.replace( ".cdfde", ".cda" );

    // Any data sources?
    if ( cdaRenderer.isEmpty() ) {
      access.deleteFile( cdaFileName );

    } else {
      // throws Exception ????
      String cdaText = cdaRenderer.render();
      if ( !access.saveFile( cdaFileName, new ByteArrayInputStream( safeGetEncodedBytes( cdaText ) ) ) ) {
        throw new DashboardStructureException(
          Messages.getString( "DashboardStructure.ERROR_006_SAVE_FILE_ADD_FAIL_EXCEPTION" ) );
      }
    }

    if ( !isPreview ) {
      String wcdfFilePath = cdeFilePath.replace( ".cdfde", ".wcdf" );

      // 3. When the component is a widget,
      //    and its internal "structure" has changed,
      //    Then any dashboard where it is used and 
      //    whose render result is cached 
      //    must be invalidated.
      DashboardManager.getInstance().invalidateDashboard( wcdfFilePath );

      // 4. CGG (requires an updated Dashboard instance)
      this.saveCgg( access, wcdfFilePath );
    }

    // TODO: Is this used?
    result.put( "cdfde", "true" );
    result.put( "cda", "true" );
    result.put( "cgg", "true" );

    return result;
  }

  private void saveCgg( IRWAccess access, String cdeRelFilePath )
    throws ThingReadException, UnsupportedThingException, ThingWriteException {
    String wcdfFilePath = cdeRelFilePath.replace( ".cdfde", ".wcdf" );

    // Obtain an UPDATED dashboard object
    DashboardManager dashMgr = DashboardManager.getInstance();
    Dashboard dash = dashMgr.getDashboard( wcdfFilePath, /*bypassCacheRead*/false );

    CggRunJsThingWriterFactory cggWriteFactory = new CggRunJsThingWriterFactory();
    IThingWriter cggDashWriter = cggWriteFactory.getWriter( dash );
    CggRunJsDashboardWriteContext cggDashContext = new CggRunJsDashboardWriteContext( cggWriteFactory, dash );
    cggDashWriter.write( access, cggDashContext, dash );
  }


  public void saveas( HashMap<String, Object> parameters ) throws Exception {
    String filePath = (String) parameters.get( "file" );
    String title = StringUtils.defaultIfEmpty( (String) parameters.get( "title" ), "Dashboard" );
    String description = StringUtils.defaultIfEmpty( (String) parameters.get( "description" ), "" );
    String cdfdeJsText = (String) parameters.get( "cdfstructure" );

    saveAs( filePath, title, description, cdfdeJsText, false );
  }

  public HashMap<String, String> saveAs( String filePath, String title, String description, String cdfdeJsText ) throws Exception {
    return saveAs( filePath, title, description, cdfdeJsText, false );
  }

  public HashMap<String, String> saveAs( String filePath, String title, String description, String cdfdeJsText,
                                         boolean isPreview ) throws Exception {
    // TODO: This method does not maintain the Widget status and parameters of a dashboard
    // Is this intended?

    if ( !CdeEnvironment.getFileHandler().saveDashboardAs( filePath, title, description, cdfdeJsText, isPreview ) ) {
      throw new DashboardStructureException(
        Messages.getString( "DashboardStructure.ERROR_005_SAVE_PUBLISH_FILE_EXCEPTION" ) );
    }

    return save( filePath.replace( ".wcdf", ".cdfde" ), cdfdeJsText );
  }

  public void newfile( HashMap<String, Object> parameters ) throws Exception {
    // 1. Read Empty Structure
    InputStream cdfstructure = null;
    try {
      cdfstructure =
        CdeEnvironment.getPluginSystemReader().getFileInputStream( SYSTEM_PLUGIN_EMPTY_STRUCTURE_FILE_PATH );

      // 2. Save file
      parameters.put( "cdfstructure", JsonUtils.readJsonFromInputStream( cdfstructure ).toString() );

      saveas( parameters );

    } finally {
      IOUtils.closeQuietly( cdfstructure );
    }
  }

  /**
   * Deligates parameters setting to {@link #savesettings(HashMap)} adds updated wcdf to JSON object
   * and returns it.
   *
   * @param parameters map of parameters of wcdf to updating
   * @return JSON object with wcdf that conatins updated parameters
   * @throws Exception
   */
  public JSONObject saveSettingsToWcdf( HashMap<String, Object> parameters ) throws Exception {
    savesettings( parameters );

    final JSONObject wcdfData = this.loadWcdfDescriptor( (String) parameters.get( "file" ) ).toJSON();
    final JSONObject result = new JSONObject();
    result.put( "wcdf", wcdfData );

    return result;
  }

  // .WCDF file
  public void savesettings( HashMap<String, Object> parameters ) throws DashboardStructureException {
    String wcdfFilePath = (String) parameters.get( "file" );
    logger.info( "Saving settings file:" + wcdfFilePath );

    DashboardWcdfDescriptor wcdf = null;
    try {
      wcdf = DashboardWcdfDescriptor.load( wcdfFilePath );

    } catch ( IOException ex ) {
      // Access?
      throw new DashboardStructureException(
        Messages.getString( "DashboardStructure.ERROR_009_SAVE_SETTINGS_FILENOTFOUND_EXCEPTION" ) );
    }

    if ( wcdf == null ) {
      throw new DashboardStructureException(
        Messages.getString( "DashboardStructure.ERROR_009_SAVE_SETTINGS_FILENOTFOUND_EXCEPTION" ) );
    }

    // Update with client info
    wcdf.update( parameters );

    // Save to repository
    String wcdfText = wcdf.toXml().asXML();
    if ( !Utils.getSystemOrUserRWAccess( wcdfFilePath )
        .saveFile( wcdfFilePath, new ByteArrayInputStream( safeGetEncodedBytes( wcdfText ) ) ) ) {
      throw new DashboardStructureException(
        Messages.getString( "DashboardStructure.ERROR_010_SAVE_SETTINGS_FAIL_EXCEPTION" ) );
    } else {
      // Since we changed some settings, we need to invalidate the cached dashboard
      DashboardManager.getInstance().invalidateDashboard( wcdfFilePath );
    }

    // Save widget component.xml file?
    if ( wcdf.isWidget() ) {
      publishWidgetComponentXml( wcdf );
    }
  }

  private void publishWidgetComponentXml( DashboardWcdfDescriptor wcdf ) {
    String widgetPath = wcdf.getPath().replaceAll( ".wcdf$", ".component.xml" );

    logger.info( "Saving widget component file:" + widgetPath );

    Document doc = createAndWriteWidgetComponentTypeXml( wcdf );
    if ( doc == null ) {
      // Failed
      return;
    }

    Utils.getSystemOrUserRWAccess( widgetPath )
      .saveFile( widgetPath, new ByteArrayInputStream( safeGetEncodedBytes( doc.asXML() ) ) );

    // This will allow the metadata model to receive the
    // new/updated widget-component definition (name and parameters).
    // The CDE Editor will show new/updated widgets.
    // No need to refresh data source definitions.
    try {
      DashboardManager.getInstance().refreshAll( /*refreshDatasources*/false );
    } catch ( Exception ex ) {
      logger.error( "Error while refreshing the meta data cache", ex );
    }
  }

  private static Document createAndWriteWidgetComponentTypeXml( DashboardWcdfDescriptor wcdf ) {
    WidgetComponentType widget = createWidgetComponentType( wcdf );
    if ( widget == null ) {
      return null;
    }

    IThingWriterFactory factory = new XmlThingWriterFactory();
    IThingWriteContext context = new DefaultThingWriteContext( factory, true );

    IThingWriter writer;
    try {
      writer = factory.getWriter( widget );
    } catch ( UnsupportedThingException ex ) {
      logger.error( "No writer to write widget component type to XML", ex );
      return null;
    }

    Document doc = DocumentHelper.createDocument();
    try {
      writer.write( doc, context, widget );
    } catch ( ThingWriteException ex ) {
      logger.error( "Failed writing widget component type to XML", ex );
      return null;
    }

    return doc;
  }

  private static WidgetComponentType createWidgetComponentType( DashboardWcdfDescriptor wcdf ) {
    WidgetComponentType.Builder builder = new WidgetComponentType.Builder();
    String name = wcdf.getWidgetName();
    builder
      .setName( "widget" + name )
      .setLabel( name )
        // TODO: Consider using wcdf.getDescription() directly?
      .setTooltip( name + " Widget" )
      .setCategory( "WIDGETS" )
      .setCategoryLabel( "Widgets" )
      .addAttribute( "widget", "true" )
      .addAttribute( "wcdf", wcdf.getPath() );

    builder.useProperty( null, "htmlObject" );

    for ( String paramName : wcdf.getWidgetParameters() ) {
      // Create an *own* property
      PropertyType.Builder prop = new PropertyType.Builder();

      // valueType is String
      prop
        .setName( paramName )
        .setLabel( "Parameter " + paramName )
        .setTooltip( "What dashboard parameter should map to widget parameter '" + paramName + "'?" );

      prop.setInputType( "Parameter" );

      builder.addProperty( prop );

      // And use it
      builder.useProperty( null, paramName );
    }

    // Use the current global meta-model to build the component in.
    MetaModel model = MetaModelManager.getInstance().getModel();
    IPropertyTypeSource propSource = model.getPropertyTypeSource();
    try {
      return (WidgetComponentType) builder.build( propSource );
    } catch ( ValidationException ex ) {
      logger.error( ex );
      return null;
    }
  }

  private static byte[] safeGetEncodedBytes( String text ) {
    try {
      return text.getBytes( CharsetHelper.getEncoding() );
    } catch ( UnsupportedEncodingException ex ) {
      // Never happens
      return null;
    }
  }
}
