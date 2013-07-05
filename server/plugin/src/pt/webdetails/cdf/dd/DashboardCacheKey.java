
package pt.webdetails.cdf.dd;

public class DashboardCacheKey
{
  private String cdfde, template, root;
  private boolean debug, abs;

  public DashboardCacheKey(String cdfde, String template, boolean debug)
  {
    this.cdfde = cdfde;
    this.template = template;
    this.root = "";
    this.abs = false;
    this.debug = debug;
  }

  public boolean isAbs()
  {
    return abs;
  }

  public void setAbs(boolean abs)
  {
    this.abs = abs;
  }

  public boolean isDebug()
  {
    return debug;
  }

  public void setDebug(boolean debug)
  {
    this.debug = debug;
  }

  public String getCdfde()
  {
    return cdfde;
  }

  public String getTemplate()
  {
    return template;
  }
  
  /**
   * @return the root
   */
  public String getRoot()
  {
    return root;
  }

  /**
   * @param root the root to set
   */
  public void setRoot(String root)
  {
    this.root = root;
  }

  public void setRoot(String scheme, String root)
  {
    this.root = root.length() == 0 ? "" : scheme + "://" + root;
  }
  
  @Override
  public boolean equals(Object obj)
  {
    if (obj == null) { return false; }
    if (getClass() != obj.getClass()) { return false; }
    
    final DashboardCacheKey other = (DashboardCacheKey)obj;
    
    if ((this.cdfde == null) ? (other.cdfde != null) : !this.cdfde.equals(other.cdfde))
    {
      return false;
    }
    
    if ((this.template == null) ? (other.template != null) : !this.template.equals(other.template))
    {
      return false;
    }
    
    if (this.debug != other.debug || this.abs != other.abs) 
    { 
      return false; 
    }
    
    if ((this.root == null) ? (other.root != null) : !this.root.equals(other.root))
    {
      return false;
    }
    
    return true;
  }

  @Override
  public int hashCode()
  {
    int hash = 5;
    hash = 79 * hash + (this.cdfde    != null ? this.cdfde   .hashCode() : 0);
    hash = 79 * hash + (this.template != null ? this.template.hashCode() : 0);
    hash = 79 * hash + (this.root     != null ? this.root    .hashCode() : 0);
    hash = 79 * hash + (this.debug ? 1 : 0);
    hash = 79 * hash + (this.abs   ? 1 : 0);
    return hash;
  }
}
