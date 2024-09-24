/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import pt.webdetails.cdf.dd.model.core.validation.ValidationException;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriterFactory;
import pt.webdetails.cdf.dd.model.inst.Dashboard;
import pt.webdetails.cdf.dd.model.meta.DashboardType;
import pt.webdetails.cdf.dd.model.meta.MetaModel;
import pt.webdetails.cdf.dd.structure.DashboardWcdfDescriptor;
import pt.webdetails.cdf.dd.util.Utils;
import pt.webdetails.cpf.repository.util.RepositoryHelper;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class CdfRunJsDashboardWriteContextForTesting extends CdfRunJsDashboardWriteContext {
  private static final String ROOT = "src/test/resources";
  private static final String TEST_FOLDER = "test";
  private static final String DASHBOARD = "testDashboard.wcdf";

  public CdfRunJsDashboardWriteContextForTesting( IThingWriterFactory factory, String indent,
                                                  boolean bypassCacheRead, CdfRunJsDashboardWriteOptions options ) {
    super( factory, indent, bypassCacheRead,
      getDashboard( RepositoryHelper.joinPaths( ROOT, TEST_FOLDER, DASHBOARD ).substring( 1 ), false ),
      options );
  }

  @Override
  public String replaceTokens( String content ) {
    return "tokens replaced";
  }

  @Override
  protected String getSystemDir() {
    return "system";
  }

  @Override
  protected String getSystemPluginId() {
    return "test-plugin";
  }

  public static Dashboard getDashboard( String path, boolean isSystem ) {
    Document wcdfDoc = null;
    try {
      wcdfDoc = Utils.getDocument( new FileInputStream( path ) );
    } catch ( DocumentException | FileNotFoundException e ) {
      e.printStackTrace();
    }

    DashboardWcdfDescriptor wcdf = DashboardWcdfDescriptor.fromXml( wcdfDoc );
    Dashboard.Builder builder = new Dashboard.Builder();

    DashboardType dashboardType = null;
    try {
      dashboardType = new DashboardType.Builder().build();
    } catch ( ValidationException e ) {
      e.printStackTrace();
    }

    if ( !isSystem ) {
      builder.setSourcePath( path );
    } else {
      // this is needed because we need to remove the src/test/resources path used to get the file
      // this is secure because there are no folder before the system while getting files from the server
      builder.setSourcePath( path.replace( ROOT + "/", "" ) );
    }

    builder.setWcdf( wcdf );
    builder.setMeta( dashboardType );
    MetaModel.Builder metaBuilder = new MetaModel.Builder();

    MetaModel model;
    Dashboard dashboard = null;
    try {
      model = metaBuilder.build();
      dashboard = builder.build( model );

    } catch ( ValidationException e ) {
      e.printStackTrace();
    }

    return dashboard;
  }
}
