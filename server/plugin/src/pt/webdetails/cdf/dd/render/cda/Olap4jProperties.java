/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.webdetails.cdf.dd.render.cda;

import java.util.HashMap;
import java.util.Iterator;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.jxpath.JXPathContext;
import org.dom4j.Attribute;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author pdpi
 */
public class Olap4jProperties implements CdaElementRenderer
{

  private JSONObject definition;
  private String paramName = "";

  private HashMap<String,String> names;
  public Olap4jProperties(String paramName)
  {
    this.paramName = paramName;
    names = new HashMap<String,String>();
    names.put("olap4juser","JdbcUser");
    names.put("olap4jpass","JdbcPassword");
    names.put("olap4jurl","Jdbc");
    names.put("olap4jcatalog","Catalog");
    names.put("olap4jdriver","JdbcDrivers");
  }

  public void renderInto(Element dataAccess)
  {
    Document doc = dataAccess.getOwnerDocument();

    Element prop = doc.createElement("Property");
    prop.setAttribute("name",names.get(paramName));
    prop.appendChild(doc.createTextNode((String) definition.get("value")));
    dataAccess.appendChild(prop);
  }

  public void setDefinition(JSONObject definition)
  {
    this.definition = definition;

  }

}
