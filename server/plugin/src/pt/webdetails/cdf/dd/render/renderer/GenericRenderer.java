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
  private String source;
  private String baseType;
  private String validation = "";
  
  public GenericRenderer(Node definition)
  {
    setDefinition(definition);
  }
  
  private void setDefinition(Node definition)
  {
    this.name = XmlDom4JHelper.getNodeText("Header/Name", definition) + "Custom";
    this.values = new HashMap<String, String>();
    String typeString = XmlDom4JHelper.getNodeText("Header/InputType/@type", definition);
    baseType = XmlDom4JHelper.getNodeText("Header/InputType/@base", definition, "SelectRenderer");
    
    typeString = typeString == null ? "" : typeString.toUpperCase();
    this.type = typeString.equals("") ? RendererType.CUSTOM : RendererType.valueOf(typeString);
    source = XmlDom4JHelper.getNodeText("Values/@source", definition);
    type = source != null ? RendererType.DYNAMICLIST : type;
    
    validation = XmlDom4JHelper.getNodeText("Header/Validation", definition);
    
    if (validation == "") {
		List<Node> vals = definition.selectNodes("Values/Value");
		for (Node val : vals)
		{
		  String value = XmlDom4JHelper.getNodeText("@display", val),
		          key = XmlDom4JHelper.getNodeText(".", val);
		  values.put(key, value);
		}
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
    str.append("Renderer = " + baseType + ".extend({\n\n");
    switch (this.type)
    {
      case VALUELIST:
    	  
  		if (validation == "") {
  			str.append("selectData: ");
			str.append("{");
		    for (String key : values.keySet()) {
		      str.append("'" + key + "': ");
		      str.append("'" + values.get(key) + "',\n");
		    }
		    str.deleteCharAt(str.length() - 2); // Delete the extra comma
		    str.append("}");
		} else {
		  	str.append("validation: " + validation);
		}    
        break;
      case DYNAMICLIST:
    	str.append("selectData: ");
        str.append(source);
        str.append("\n");
        break;
    }
    str.append("\n});");
    
    return str.toString();
  }
}
