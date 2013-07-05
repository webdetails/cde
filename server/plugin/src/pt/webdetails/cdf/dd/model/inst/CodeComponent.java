
package pt.webdetails.cdf.dd.model.inst;

import pt.webdetails.cdf.dd.model.core.validation.ValidationException;
import pt.webdetails.cdf.dd.model.meta.CodeComponentType;
import pt.webdetails.cdf.dd.model.meta.MetaModel;

/**
 * @author dcleao
 */
public class CodeComponent<TM extends CodeComponentType> extends NonVisualComponent<TM>
{
  protected CodeComponent(Builder builder, MetaModel metaModel) throws ValidationException
  {
    super(builder, metaModel);
  }

  @Override
  public TM getMeta()
  {
    return super.getMeta();
  }

  /**
   * Class to create and modify CodeComponent instances.
   */
  public static class Builder extends NonVisualComponent.Builder
  {
    @Override
    public CodeComponent build(MetaModel metaModel) throws ValidationException
    {
      return new CodeComponent(this, metaModel);
    }
  }
}
