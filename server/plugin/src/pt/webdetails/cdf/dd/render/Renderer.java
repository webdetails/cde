package pt.webdetails.cdf.dd.render;

import java.lang.reflect.Constructor;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@SuppressWarnings("unchecked")
public abstract class Renderer {


    protected JXPathContext doc;
    protected static Log logger;
    Class<JXPathContext>[] rendererConstructorArgs = new Class[]{JXPathContext.class};

    public Renderer() {
        logger = LogFactory.getLog(Renderer.class);
    }

    public abstract String render(JXPathContext doc) throws Exception;

    public abstract String getRenderClassName(String Type);

    public Object getRender(JXPathContext context) throws Exception {

        Object renderer = null;
        String renderType = null;
        try {

            renderType = (String) context.getValue("type");

            if (!renderType.equals("Label")) {
                Class<?> rendererClass = Class.forName(getRenderClassName(renderType));

                Constructor<?> constructor = rendererClass.getConstructor(rendererConstructorArgs);
                renderer = constructor.newInstance(new Object[]{context});
            }

        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            logger.error("Class not found: " + renderType);
            //throw new RenderException("Render not found for: " + renderType);
        }

        return renderer;
    }

    public String getIdent(int ident) {

        switch (ident) {
            case 0:
                return "";
            case 1:
                return " ";
            case 2:
                return "  ";
            case 3:
                return "   ";
            case 4:
                return "    ";
            case 8:
                return "        ";

        }

        StringBuilder identStr = new StringBuilder();
        for (int i = 0; i < ident; i++) {
            identStr.append(" ");
        }
        return identStr.toString();

    }

    public JXPathContext getDoc() {
        return doc;
    }

    public void setDoc(JXPathContext doc) {
        this.doc = doc;
    }
}
