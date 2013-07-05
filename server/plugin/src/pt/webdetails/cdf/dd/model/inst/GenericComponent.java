
package pt.webdetails.cdf.dd.model.inst;

import pt.webdetails.cdf.dd.model.core.validation.ValidationException;
import pt.webdetails.cdf.dd.model.meta.GenericComponentType;
import pt.webdetails.cdf.dd.model.meta.MetaModel;

/**
 * @author dcleao
 */
public class GenericComponent<TM extends GenericComponentType> extends VisualComponent<TM>
{
  protected GenericComponent(Builder builder, MetaModel metaModel) throws ValidationException
  {
    super(builder, metaModel);
  }

  @Override
  public TM getMeta()
  {
    return super.getMeta();
  }

  /**
   * Class to create and modify VisualComponent instances.
   */
  public static class Builder extends VisualComponent.Builder
  {
    @Override
    public GenericComponent build(MetaModel metaModel) throws ValidationException
    {
      if(metaModel == null) { throw new IllegalArgumentException("metaModel"); }
      
      return new GenericComponent(this, metaModel);
    }
  }
}
