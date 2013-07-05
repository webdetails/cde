
package pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard;

import java.util.Date;
import org.apache.commons.lang.StringUtils;
import org.pentaho.platform.api.engine.IPentahoSession;
import pt.webdetails.cdf.dd.DashboardDesignerContentGenerator;
import pt.webdetails.cdf.dd.model.core.writer.DefaultThingWriteContext;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriterFactory;
import pt.webdetails.cdf.dd.model.inst.Component;
import pt.webdetails.cdf.dd.model.inst.Dashboard;

/**
 * @author dcleao
 */
public class CdfRunJsDashboardWriteContext extends DefaultThingWriteContext
{
  private static final String DASHBOARD_PATH_TAG = "\\$\\{dashboardPath\\}";
  
  private static final String ABS_DIR_RES_TAG    = "\\$\\{res:(/.+/)\\}";
  private static final String REL_DIR_RES_TAG    = "\\$\\{res:(.+/)\\}";
  
  private static final String ABS_RES_TAG        = "\\$\\{res:(/.+)\\}";
  private static final String REL_RES_TAG        = "\\$\\{res:(.+)\\}";
  
  private static final String ABS_IMG_TAG        = "\\$\\{img:(/.+)\\}";
  private static final String REL_IMG_TAG        = "\\$\\{img:(.+)\\}";
  
  // ------------
  
  private static final String COMPONENT_PREFIX = "render_";
  
  private static final String SHORT_H_TAG = "\\$\\{h:(.+?)\\}";
  private static final String SHORT_C_TAG = "\\$\\{c:(.+?)\\}";
  private static final String SHORT_P_TAG = "\\$\\{p:(.+?)\\}";
  private static final String LONG_H_TAG  = "\\$\\{htmlObject:(.+?)\\}";
  private static final String LONG_C_TAG  = "\\$\\{component:(.+?)\\}";
  private static final String LONG_P_TAG  = "\\$\\{parameter:(.+?)\\}";
  
  // ------------
  
  private boolean _isFirstInList = true;
  private final Date      _writeDate;
  private final String    _indent;
  private final Dashboard _dash;
  private final boolean   _bypassCacheRead;
  private final IPentahoSession _userSession;
  
  protected final CdfRunJsDashboardWriteOptions _options;

  public CdfRunJsDashboardWriteContext(
          IThingWriterFactory factory, 
          String indent, 
          boolean bypassCacheRead, 
          Dashboard dash, // Current Dashboard/Widget
          IPentahoSession userSession,
          CdfRunJsDashboardWriteOptions options)
  {
    super(factory, true);
    
    if(dash == null) { throw new IllegalArgumentException("dash"); }
    if(options  == null) { throw new IllegalArgumentException("options"); }
    if(userSession == null) { throw new IllegalArgumentException("userSession"); }
    
    this._indent = StringUtils.defaultIfEmpty(indent, "");
    this._bypassCacheRead = bypassCacheRead;
    this._dash  = dash;
    this._options   = options;
    this._userSession = userSession;
    this._writeDate = new Date();
  }
  
  private CdfRunJsDashboardWriteContext(
          CdfRunJsDashboardWriteContext other,
          String indent)
  {
    super(other.getFactory(), other.getBreakOnError());
    
    this._indent    = StringUtils.defaultIfEmpty(indent, "");
    this._bypassCacheRead = other._bypassCacheRead;
    this._dash      = other._dash;
    this._options   = other._options;
    this._userSession = other._userSession;
    this._writeDate = other._writeDate;
  }
  
  public CdfRunJsDashboardWriteContext withIndent(String indent)
  {
    return new CdfRunJsDashboardWriteContext(this, indent);
  }
  
  public String getIndent()
  {
    return this._indent;
  }
  
  public boolean isBypassCacheRead()
  {
    return this._bypassCacheRead;
  }
  
  public Date getWriteDate()
  {
    return this._writeDate;
  }
  
  public Dashboard getDashboard()
  {
    return this._dash;
  }
  
  public boolean isFirstInList()
  {
    return this._isFirstInList;
  }
  
  public void setIsFirstInList(boolean isFirst)
  {
    this._isFirstInList = isFirst;
  }
  
  public CdfRunJsDashboardWriteOptions getOptions()
  {
    return this._options;
  }
  
  public IPentahoSession getUserSession()
  {
    return this._userSession;
  }
  
  // --------------
  
  public String getId(Component comp)
  {
    return comp.buildId(this._options.getAliasPrefix());
  }
  
  // --------------
  
  public String replaceTokensAndAlias(String content)
  {
    return this.replaceAlias(this.replaceTokens(content));
  }
          
  public String replaceTokens(String content)
  {
    final long timestamp = this._writeDate.getTime();
    
    final String root = this._options.isAbsolute() ?
                  (this._options.getSchemedRoot() + DashboardDesignerContentGenerator.SERVER_URL_VALUE) :
                  "";
    
    final String path = this._dash.getWcdf().getStructurePath().replaceAll("(.+/).*", "$1");
    
    return content
      .replaceAll(DASHBOARD_PATH_TAG, path.replaceAll("(^/.*/$)", "$1")) // replace the dashboard path token
      .replaceAll(ABS_IMG_TAG,        root + "res$1" + "?v=" + timestamp)// build the image links, with a timestamp for caching purposes
      .replaceAll(REL_IMG_TAG,        root + "res" + path + "$1" + "?v=" + timestamp)// build the image links, with a timestamp for caching purposes
      .replaceAll(ABS_DIR_RES_TAG,    root + "res$1")// Directories don't need the caching timestamp
      .replaceAll(REL_DIR_RES_TAG,    root + "res" + path + "$1")// Directories don't need the caching timestamp
      .replaceAll(ABS_RES_TAG,        root + "res$1" + "?v=" + timestamp)// build the image links, with a timestamp for caching purposes
      .replaceAll(REL_RES_TAG,        root + "res" + path + "$1" + "?v=" + timestamp);// build the image links, with a timestamp for caching purposes
  }
  
  public String replaceAlias(String content)
  {
    if (content == null) { return ""; }
    
    String alias = this._options.getAliasPrefix();
            
    String aliasAndName = (StringUtils.isNotEmpty(alias) ? (alias + "_") : "") + "$1";
    
    return content
      .replaceAll(SHORT_C_TAG, COMPONENT_PREFIX + aliasAndName)
      .replaceAll(LONG_C_TAG,  COMPONENT_PREFIX + aliasAndName)
      .replaceAll(SHORT_H_TAG, aliasAndName)
      .replaceAll(SHORT_P_TAG, aliasAndName)
      .replaceAll(LONG_H_TAG,  aliasAndName)
      .replaceAll(LONG_P_TAG,  aliasAndName);
  }
}
