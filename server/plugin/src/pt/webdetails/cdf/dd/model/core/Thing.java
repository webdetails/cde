
package pt.webdetails.cdf.dd.model.core;

/**
 * The most abstract concept.
 * 
 * @author dcleao
 */
public abstract class Thing
{
  public abstract String getKind();
  public abstract String getId();
  
  public static abstract class Builder
  {
  }
}
