/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.webdetails.cdf.dd.util;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author pedro
 */
public class PropertyBag {

    private Hashtable properties;
    protected static final Log logger = LogFactory.getLog(PropertyBag.class);

    public PropertyBag() {

        properties = new Hashtable();

    }

    public void addId(String id) {
        if (id.length() > 0) {
            properties.put("id", id.replace(' ', '_'));
        }
    }

    public void addClass(String _class) {
        if (_class.length() > 0) {
            if (!properties.containsKey("classes")) {
                properties.put("classes", new Vector());
            }
            ((Vector) properties.get("classes")).add(_class);
        }
    }

    public void addStyle(String style, String value) {
        if (value.length() > 0) {
            if (!properties.containsKey("styles")) {
                properties.put("styles", new Vector());
            }
            ((Vector) properties.get("styles")).add(style + ":" + value + ";");
        }
    }

    public void addColClass(String _class, Object v) {

        if (v instanceof String) {
            String value = (String) v;
            if (value.length() > 0) {
                addClass(_class + value);
            }
        } else if (v instanceof Boolean) {
            Boolean value = (Boolean) v;
            if (value.booleanValue()) {
                addClass(_class);
            }
        }
    }

    public String getPropertiesString() {
        String str = properties.containsKey("id") ? "id='" + (String) properties.get("id") + "' " : "";
        if (properties.containsKey("classes")) {
            str += " class='";
            Iterator nodeIterator = ((Vector<String>) properties.get("classes")).iterator();
            while (nodeIterator.hasNext()) {
                str += nodeIterator.next() + " ";
            }
            str += "' ";
        }
        if (properties.containsKey("styles")) {
            str += " style='";
            Iterator nodeIterator = ((Vector<String>) properties.get("styles")).iterator();
            while (nodeIterator.hasNext()) {
                str += nodeIterator.next();
            }
            str += "' ";
        }
        return str;
    }
}
