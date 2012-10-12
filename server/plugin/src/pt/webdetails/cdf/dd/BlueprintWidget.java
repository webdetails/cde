/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.webdetails.cdf.dd;

import java.io.FileNotFoundException;
import org.apache.commons.jxpath.JXPathContext;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import pt.webdetails.cdf.dd.render.RenderComponents;
import pt.webdetails.cdf.dd.render.RenderLayout;
import pt.webdetails.cdf.dd.structure.WcdfDescriptor;
import pt.webdetails.cpf.repository.RepositoryAccess;

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
