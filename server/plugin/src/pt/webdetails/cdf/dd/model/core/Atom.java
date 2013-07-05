
package pt.webdetails.cdf.dd.model.core;

/**
 * Atoms have no explicit structure and no identity.
 * 
 * @author dcleao
 */
public abstract class Atom extends Thing
{
  public abstract String getKind();
  
  public final String getId()
  {
    return null;
  }
  
  public static abstract class Builder extends Thing.Builder
  {
  }
}
