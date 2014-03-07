package pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard;

import pt.webdetails.cdf.dd.CdeEngine;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriterFactory;
import pt.webdetails.cdf.dd.model.inst.Dashboard;

public class PentahoCdfRunJsDashboardWriteContext extends CdfRunJsDashboardWriteContext {
  private static final String RESOURCE_API_GET = "api/resources";

  public PentahoCdfRunJsDashboardWriteContext( IThingWriterFactory factory,
      String indent, boolean bypassCacheRead, Dashboard dash,
      CdfRunJsDashboardWriteOptions options ) {
    super( factory, indent, bypassCacheRead, dash, options );
  }
  public PentahoCdfRunJsDashboardWriteContext( CdfRunJsDashboardWriteContext factory,
      String indent) {
    super(factory, indent);
  }

  @Override
  public String replaceTokens(String content) {
    final long timestamp = this._writeDate.getTime();

    final String root = getRoot();

    final String path = this._dash.getSourcePath().replaceAll("(.+/).*", "$1");

    return content
        .replaceAll(DASHBOARD_PATH_TAG, path.replaceAll("(^/.*/$)", "$1")) // replace the dashboard path token
        .replaceAll(ABS_IMG_TAG,        root + RESOURCE_API_GET + "$1" + "?v=" + timestamp)// build the image links, with a timestamp for caching purposes
        .replaceAll(REL_IMG_TAG,        root + RESOURCE_API_GET + path + "$1" + "?v=" + timestamp)// build the image links, with a timestamp for caching purposes
        .replaceAll(ABS_DIR_RES_TAG,    root + RESOURCE_API_GET + "$1")// Directories don't need the caching timestamp
        .replaceAll(REL_DIR_RES_TAG,    root + RESOURCE_API_GET + path + "$1")// Directories don't need the caching timestamp
        .replaceAll(ABS_RES_TAG,        root + RESOURCE_API_GET + "$1" + "?v=" + timestamp)// build the image links, with a timestamp for caching purposes
        .replaceAll(REL_RES_TAG,        root + RESOURCE_API_GET + path + "$1" + "?v=" + timestamp);// build the image links, with a timestamp for caching purposes
  }

  protected String getRoot(){
    return this._options.isAbsolute() ?
        (this._options.getSchemedRoot() + CdeEngine.getInstance().getEnvironment().getApplicationBaseContentUrl()) :
        CdeEngine.getInstance().getEnvironment().getApplicationBaseContentUrl();

  }

}
