package pt.webdetails.cdf.dd.render.renderer;

import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Node;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;
import pt.webdetails.cdf.dd.render.DependenciesEngine;
import pt.webdetails.cdf.dd.render.DependenciesManager;

/**
 *
 * @author pdpi
 */
public class GenericRenderer
{

  protected static final Log logger = LogFactory.getLog(GenericRenderer.class);
  private String path;
  private String name;
  private String parent;

  public GenericRenderer(String path, Document definition)
  {
    this.path = path;
    setDefinition(definition);
  }

  private void setDefinition(Document definition)
  {
    DependenciesEngine ddDeps = DependenciesManager.getInstance().getEngine("CDFDD");
    this.name = XmlDom4JHelper.getNodeText("/DesignerRenderer/Header/Name", definition);
    this.parent = XmlDom4JHelper.getNodeText("/DesignerRenderer/Implementation/Parent", definition);
    String src = XmlDom4JHelper.getNodeText("/DesignerRenderer/Contents/Implementation/Code/@src", definition);
    String ver = XmlDom4JHelper.getNodeText("/DesignerRenderer/Header/Version", definition);
    if (src != null)
    {
      try
      {
        ddDeps.register("RENDER_" + this.name, ver, path + src);
      }
      catch (Exception e)
      {
        logger.error("failed to register " + path);
      }
    }


    List<Node> deps = definition.selectNodes("/DesignerRenderer/Content/Implementation/Dependencies/*");

    for (Node node : deps)
    {
      String name = XmlDom4JHelper.getNodeText(".", node);
      String depVer = XmlDom4JHelper.getNodeText("@version", node);
      src = XmlDom4JHelper.getNodeText("@src", node);
      try
      {
        ddDeps.register(name, depVer, path + src);
      }
      catch (Exception e)
      {
        logger.error("failed to register " + path);
      }
    }
  }

  public String getName()
  {
    return name;
  }
}
