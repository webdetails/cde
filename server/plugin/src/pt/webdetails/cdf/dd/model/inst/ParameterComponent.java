
package pt.webdetails.cdf.dd.model.inst;

import pt.webdetails.cdf.dd.model.core.validation.ValidationException;
import pt.webdetails.cdf.dd.model.meta.ParameterComponentType;
import pt.webdetails.cdf.dd.model.meta.MetaModel;

/**
 * @author dcleao
 */
public class ParameterComponent<TM extends ParameterComponentType> extends NonVisualComponent<TM>
{
  protected ParameterComponent(Builder builder, MetaModel metaModel) throws ValidationException
  {
    super(builder, metaModel);
  }

  @Override
  public TM getMeta()
  {
    return super.getMeta();
  }

  /**
   * Class to create and modify ParameterComponent instances.
   */
  public static class Builder extends NonVisualComponent.Builder
  {
    @Override
    public ParameterComponent build(MetaModel metaModel) throws ValidationException
    {
      return new ParameterComponent(this, metaModel);
    }
  }
}
