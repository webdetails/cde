package pt.webdetails.cdf.dd.render.renderer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Node;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;
import pt.webdetails.cdf.dd.render.properties.GenericProperty.RendererType;

/**
 *
 * @author pdpi
 */
public class GenericRenderer
{

  RendererType type;
  protected static final Log logger = LogFactory.getLog(GenericRenderer.class);
  private String name;
  private Map<String, String> values;

  public GenericRenderer(Node definition)
  {
    setDefinition(definition);
  }

  private void setDefinition(Node definition)
  {
    this.name = XmlDom4JHelper.getNodeText("Header/Name", definition) + "Custom";
    this.values = new HashMap<String, String>();
    String typeString = XmlDom4JHelper.getNodeText("Header/InputType/@type", definition);
    typeString = typeString == null ? "" : typeString.toUpperCase();
    this.type = typeString.equals("") ? RendererType.CUSTOM : RendererType.valueOf(typeString);
    List<Node> vals = definition.selectNodes("Values/Value");
    for (Node val : vals) {
      String value = XmlDom4JHelper.getNodeText("@display", val),
              key = XmlDom4JHelper.getNodeText(".", val);
      values.put(key,value);
    }
  }

  public String getName()
  {
    return name;
  }

  public String getDefinition()
  {
    StringBuilder str = new StringBuilder();
    str.append("var ");
    str.append(name);
    str.append("Renderer = SelectRenderer.extend({\n\n");
    str.append("selectData: {");
    switch (this.type)
    {
      case VALUELIST:
        for (String key : values.keySet())
        {
          str.append("'" + key + "': ");
          str.append("'" + values.get(key) + "',\n");
        }
        break;
      case DYNAMICLIST:
        break;
    }
    str.deleteCharAt(str.length() - 2); // Delete the extra comma
    str.append("}\n});");

    return str.toString();
    /*
    var GravityRenderer = SelectRenderer.extend({
    
    selectData: {
    'N': 'Top',
    'S': 'Bottom',
    'W': 'Left',
    'E': 'Right'
    }
    });
     */
  }
}
