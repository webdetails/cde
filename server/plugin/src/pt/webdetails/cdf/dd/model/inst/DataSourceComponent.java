
package pt.webdetails.cdf.dd.model.inst;

import pt.webdetails.cdf.dd.model.core.validation.ValidationException;
import pt.webdetails.cdf.dd.model.meta.DataSourceComponentType;
import pt.webdetails.cdf.dd.model.meta.MetaModel;

/**
 * @author dcleao
 */
public class DataSourceComponent<TM extends DataSourceComponentType> extends NonVisualComponent<TM>
{
  protected DataSourceComponent(Builder builder, MetaModel metaModel) throws ValidationException
  {
    super(builder, metaModel);
  }

  @Override
  public TM getMeta()
  {
    return super.getMeta();
  }

  /**
   * Class to create and modify DataSourceComponent instances.
   */
  public static class Builder extends NonVisualComponent.Builder
  {
    @Override
    public DataSourceComponent build(MetaModel metaModel) throws ValidationException
    {
      return new DataSourceComponent(this, metaModel);
    }
  }
}
