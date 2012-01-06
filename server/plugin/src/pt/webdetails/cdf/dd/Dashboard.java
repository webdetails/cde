/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.webdetails.cdf.dd;

import java.util.Date;
import org.pentaho.platform.api.engine.IParameterProvider;

/**
 *
 * @author pdpi
 */
public interface Dashboard
{

  public String render();
  public String render(IParameterProvider params);
  public String getHeader();

  public String getContent();

  public Date getLoaded();
  
  public String getType();
}
