/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd;

import org.apache.commons.lang.StringUtils;

public final class DashboardCacheKey
{
  private final String cdfde, template, root;
  private final boolean debug, abs;
  private final String aliasPrefix;

  public DashboardCacheKey(
          String cdfde, 
          String template, 
          boolean debug,
          boolean abs,
          String schemedRoot,
          String aliasPrefix)
  {
    this.cdfde = StringUtils.defaultIfEmpty(cdfde, "");
    this.template = StringUtils.defaultIfEmpty(template, "");
    this.debug = debug;
    this.abs   = abs;
    this.root = StringUtils.defaultIfEmpty(schemedRoot, "");
    this.aliasPrefix = StringUtils.defaultIfEmpty(aliasPrefix, "");
  }

  public boolean isAbs()
  {
    return abs;
  }

  public boolean isDebug()
  {
    return debug;
  }

  public String getCdfde()
  {
    return cdfde;
  }

  public String getTemplate()
  {
    return template;
  }
  
  public String getRoot()
  {
    return root;
  }
  
  public String getAliasPrefix()
  {
    return this.aliasPrefix;
  }
  
  @Override
  public boolean equals(Object obj)
  {
    if (obj == null) { return false; }
    if (getClass() != obj.getClass()) { return false; }
    
    final DashboardCacheKey other = (DashboardCacheKey)obj;
    return this.debug == other.debug && 
           this.abs == other.abs &&
           this.cdfde.equals(other.cdfde) &&
           this.template.equals(other.template) &&
           this.root.equals(other.root) &&
           this.aliasPrefix.equals(other.aliasPrefix);
  }

  @Override
  public int hashCode()
  {
    int hash = 5;
    hash = 79 * hash + this.cdfde      .hashCode();
    hash = 79 * hash + this.template   .hashCode();
    hash = 79 * hash + this.root       .hashCode();
    hash = 79 * hash + this.aliasPrefix.hashCode();
    hash = 79 * hash + (this.debug ? 1 : 0);
    hash = 79 * hash + (this.abs   ? 1 : 0);
    return hash;
  }
}
