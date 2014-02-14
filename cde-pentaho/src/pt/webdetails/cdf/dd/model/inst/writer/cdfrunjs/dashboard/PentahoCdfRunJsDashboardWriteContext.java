package pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard;

import pt.webdetails.cdf.dd.CdeEngine;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriterFactory;
import pt.webdetails.cdf.dd.model.inst.Dashboard;

public class PentahoCdfRunJsDashboardWriteContext extends CdfRunJsDashboardWriteContext {

  public PentahoCdfRunJsDashboardWriteContext( IThingWriterFactory factory,
      String indent, boolean bypassCacheRead, Dashboard dash,
      CdfRunJsDashboardWriteOptions options ) {
    super( factory, indent, bypassCacheRead, dash, options );
  }
  public PentahoCdfRunJsDashboardWriteContext( CdfRunJsDashboardWriteContext factory,
      String indent) {
    super(factory,indent);
  }


  @Override
  public String replaceTokens(String content) {
    final long timestamp = this._writeDate.getTime();

    final String root = this._options.isAbsolute() ?
        (this._options.getSchemedRoot() + CdeEngine.getInstance().getEnvironment().getApplicationBaseContentUrl()) : "";


    final String path = this._dash.getSourcePath().replaceAll("(.+/).*", "$1");

    return content
        .replaceAll( DASHBOARD_PATH_TAG, path.replaceAll( "(^/.*/$)", "$1" ) ) // replace the dashboard path token
        .replaceAll( ABS_IMG_TAG, root + "res$1" + "?v=" + timestamp )// build the image links, with a timestamp for caching purposes
        .replaceAll( REL_IMG_TAG, root + "res" + path + "$1" + "?v=" + timestamp )// build the image links, with a timestamp for caching purposes
        .replaceAll( ABS_DIR_RES_TAG, root + "res$1" )// Directories don't need the caching timestamp
        .replaceAll( REL_DIR_RES_TAG, root + "res" + path + "$1" )// Directories don't need the caching timestamp
        .replaceAll( ABS_RES_TAG, root + "res$1" + "?v=" + timestamp )// build the image links, with a timestamp for caching purposes
        .replaceAll( REL_RES_TAG, root + "res" + path + "$1" + "?v=" + timestamp );// build the image links, with a timestamp for caching purposes
  }

}
