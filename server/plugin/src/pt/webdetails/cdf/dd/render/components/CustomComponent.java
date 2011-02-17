package pt.webdetails.cdf.dd.render.components;

import java.util.List;
import org.apache.commons.jxpath.JXPathContext;
import org.dom4j.Node;

import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;
import pt.webdetails.cdf.dd.render.DependenciesEngine;
import pt.webdetails.cdf.dd.render.DependenciesManager;
import pt.webdetails.cdf.dd.render.properties.GenericProperty;
import pt.webdetails.cdf.dd.render.properties.PropertyManager;
import pt.webdetails.cdf.dd.util.Utils;

public class CustomComponent extends GenericComponent
{

  private String path;

  public CustomComponent(JXPathContext context)
  {
    super(context);
  }

  public CustomComponent()
  {
  }

  public CustomComponent(String path)
  {
    this.path = path;
  }

  @Override
  public void setDefinition(Node definition)
  {
    DependenciesEngine cdfDeps = DependenciesManager.getInstance().getEngine("CDF");
    DependenciesEngine rawDeps = DependenciesManager.getInstance().getEngine("CDF-RAW");
    DependenciesEngine styleDeps = DependenciesManager.getInstance().getEngine("CDF-CSS");
    DependenciesEngine ddDeps = DependenciesManager.getInstance().getEngine("CDFDD");
    this.componentName = XmlDom4JHelper.getNodeText("Header/IName", definition);
    String src;
    String ver = XmlDom4JHelper.getNodeText("Header/Version", definition);

    List<Node> deps = definition.selectNodes("Contents/Implementation/Dependencies/*");

    for (Node node : deps)
    {
      String name = XmlDom4JHelper.getNodeText(".", node);
      String depVer = XmlDom4JHelper.getNodeText("@version", node);
      src = XmlDom4JHelper.getNodeText("@src", node);
      
      String srcPath = Utils.joinPath(this.path, src);
      
      String app = XmlDom4JHelper.getNodeText("@app", node);
      // by default, dependencies are for CDF
      if (app == null || app.equals("CDF"))
      {
        try
        {
          cdfDeps.register(name, depVer, srcPath);
        }
        catch (Exception e)
        {
          logger.error("failed to register " + srcPath);
        }
      }
      else if (app.equals("CDFDD"))
      {
        try
        {
          ddDeps.register(name, depVer, srcPath);
        }
        catch (Exception e)
        {
          logger.error("failed to register " + srcPath);
        }
      }
    }
    List<Node> styles = definition.selectNodes("Contents/Implementation/Styles/*");

    for (Node node : styles)
    {
      String name = XmlDom4JHelper.getNodeText(".", node);
      String depVer = XmlDom4JHelper.getNodeText("@version", node);
      src = XmlDom4JHelper.getNodeText("@src", node);
      String srcPath = Utils.joinPath(this.path, src);  
      try
      {
        styleDeps.register(name, depVer, srcPath);
      }
      catch (Exception e)
      {
        logger.error("failed to register " + srcPath);
      }

    }

    List<Node> rawCode = definition.selectNodes("Contents/Implementation/Raw/*");

    for (Node node : rawCode)
    {
      String name = XmlDom4JHelper.getNodeText("@name", node);
      String depVer = XmlDom4JHelper.getNodeText("@version", node);
      src = XmlDom4JHelper.getNodeText(".", node);
      try
      {
        rawDeps.registerRaw(name, depVer, src);
      }
      catch (Exception e)
      {
        logger.error("failed to register code fragment: " + src);
      }

    }

    src = XmlDom4JHelper.getNodeText("Contents/Implementation/Code/@src", definition);
    String srcPath = Utils.joinPath(this.path, src);
    if (src != null)
    {
      try
      {
        cdfDeps.register(this.componentName, ver, srcPath);
      }
      catch (Exception e)
      {
        logger.error("failed to register " + srcPath);
      }
    }
    List<Node> props = definition.selectNodes("Contents/Implementation/CustomProperties/*");

    for (Node node : props)
    {
      PropertyManager.getInstance().registerPrivateProperty(this, new GenericProperty(this.path, node));
    }

    super.setDefinition(definition);
  }

  @Override
  public String getEntry()
  {
    String ignore = XmlDom4JHelper.getNodeText("Contents/Model/@ignore", getDefinition());
    if (ignore != null && ignore.toLowerCase().equals("true"))
    {
      return "";
    }
    else
    {
      return super.getEntry();
    }
  }

  @Override
  public String getModel()
  {
    String ignore = XmlDom4JHelper.getNodeText("Contents/Model/@ignore", getDefinition());
    if (ignore != null && ignore.toLowerCase().equals("true"))
    {
      return "";
    }
    else
    {
      return super.getModel();
    }
  }
}
