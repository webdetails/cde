package pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard;

import pt.webdetails.cdf.dd.model.core.writer.IThingWriterFactory;
import pt.webdetails.cdf.dd.model.inst.Dashboard;

public class PentahoCdfRunJsDashboardWriteContextForTesting extends PentahoCdfRunJsDashboardWriteContext {
  public PentahoCdfRunJsDashboardWriteContextForTesting(
      IThingWriterFactory factory,
      String indent, boolean bypassCacheRead, Dashboard dash,
      CdfRunJsDashboardWriteOptions options ) {
    super( factory, indent, bypassCacheRead, dash, options );
  }

  @Override protected String getRoot() {
    return "";
  }
}
