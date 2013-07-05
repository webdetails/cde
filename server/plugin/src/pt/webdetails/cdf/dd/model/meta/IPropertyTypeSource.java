
package pt.webdetails.cdf.dd.model.meta;

/**
 * Interface that supports the building phase of component types.
 *
 * @author dcleao
 */
public interface IPropertyTypeSource
{
  PropertyType getProperty(String name);
}
