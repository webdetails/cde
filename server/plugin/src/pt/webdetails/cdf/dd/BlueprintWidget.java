/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.webdetails.cdf.dd;

import java.io.FileNotFoundException;
import pt.webdetails.cdf.dd.structure.WcdfDescriptor;


/**
 *
 * @author pdpi
 */
public class BlueprintWidget extends BlueprintDashboard implements Widget
{


  public BlueprintWidget(WcdfDescriptor wcdf, boolean absolute, String absRoot, boolean debug, String scheme, String alias) throws FileNotFoundException
  {
    super(wcdf, absolute, absRoot, debug, scheme, alias);
  }

  public String getType()
  {
    return "BlueprintWidget";
  }

}
