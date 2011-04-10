/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.webdetails.cdf.dd;

import java.util.Date;

/**
 *
 * @author pdpi
 */
public interface Dashboard
{

  public String render();

  public String getHeader();

  public String getContent();

  public Date getLoaded();
}
