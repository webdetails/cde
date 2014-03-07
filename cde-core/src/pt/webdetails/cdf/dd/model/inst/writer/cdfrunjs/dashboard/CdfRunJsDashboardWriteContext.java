/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard;

import java.util.Date;
import org.apache.commons.lang.StringUtils;

import pt.webdetails.cdf.dd.CdeEngine;
import pt.webdetails.cdf.dd.model.core.writer.DefaultThingWriteContext;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriterFactory;
import pt.webdetails.cdf.dd.model.inst.Component;
import pt.webdetails.cdf.dd.model.inst.Dashboard;

/**
 * @author dcleao
 */
public abstract class CdfRunJsDashboardWriteContext extends DefaultThingWriteContext
{
  protected static final String DASHBOARD_PATH_TAG = "\\$\\{dashboardPath\\}";

  protected static final String ABS_DIR_RES_TAG    = "\\$\\{res:(/.+/)\\}";
  protected static final String REL_DIR_RES_TAG    = "\\$\\{res:(.+/)\\}";

  protected static final String ABS_RES_TAG        = "\\$\\{res:(/.+)\\}";
  protected static final String REL_RES_TAG        = "\\$\\{res:(.+)\\}";

  protected static final String ABS_IMG_TAG        = "\\$\\{img:(/.+)\\}";
  protected static final String REL_IMG_TAG        = "\\$\\{img:(.+)\\}";
  
  // ------------

  protected static final String COMPONENT_PREFIX = "render_";

  protected static final String SHORT_H_TAG = "\\$\\{h:(.+?)\\}";
  protected static final String SHORT_C_TAG = "\\$\\{c:(.+?)\\}";
  protected static final String SHORT_P_TAG = "\\$\\{p:(.+?)\\}";
  protected static final String LONG_H_TAG  = "\\$\\{htmlObject:(.+?)\\}";
  protected static final String LONG_C_TAG  = "\\$\\{component:(.+?)\\}";
  protected static final String LONG_P_TAG  = "\\$\\{parameter:(.+?)\\}";
  
  // ------------

  protected boolean _isFirstInList = true;
  protected final Date      _writeDate;
  protected final String    _indent;
  protected final Dashboard _dash;
  protected final boolean   _bypassCacheRead;
  
  protected final CdfRunJsDashboardWriteOptions _options;

  public CdfRunJsDashboardWriteContext(
          IThingWriterFactory factory, 
          String indent, 
          boolean bypassCacheRead, 
          Dashboard dash, // Current Dashboard/Widget
          CdfRunJsDashboardWriteOptions options)
  {
    super(factory, true);
    
    if(dash == null) { throw new IllegalArgumentException("dash"); }
    if(options  == null) { throw new IllegalArgumentException("options"); }
    
    this._indent = StringUtils.defaultIfEmpty(indent, "");
    this._bypassCacheRead = bypassCacheRead;
    this._dash  = dash;
    this._options   = options;
    this._writeDate = new Date();
  }
  
  protected CdfRunJsDashboardWriteContext(
          CdfRunJsDashboardWriteContext other,
          String indent)
  {
    super(other.getFactory(), other.getBreakOnError());
    
    this._indent    = StringUtils.defaultIfEmpty(indent, "");
    this._bypassCacheRead = other._bypassCacheRead;
    this._dash      = other._dash;
    this._options   = other._options;
    this._writeDate = other._writeDate;
  }
  
  public CdfRunJsDashboardWriteContext withIndent(String indent)
  {
    return CdeEngine.getInstance().getEnvironment()
        .getCdfRunJsDashboardWriteContext( this, indent );
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
  
  // --------------
  
  public String getId(Component<?> comp)
  {
    return comp.buildId(this._options.getAliasPrefix());
  }
  
  // --------------
  
  public String replaceTokensAndAlias(String content)
  {
    return this.replaceAlias(this.replaceTokens(content));
  }
          
  public abstract String replaceTokens(String content);
  
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
