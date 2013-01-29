/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.webdetails.cdf.dd.util;

import net.sf.json.JSON;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import java.io.*;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

/**
 * @author pedro
 */
public class JsonUtils {

  public static JSON readJsonFromInputStream(final InputStream input) throws IOException {
    
    String contents = StringUtils.trim(IOUtils.toString(input, "UTF-8"));
    return (JSON) JSONSerializer.toJSON(contents);
  }

  public static void buildJsonResult(final OutputStream out, final Boolean sucess, final Object result) {

    final JSONObject jsonResult = new JSONObject();

    jsonResult.put("status", sucess.toString());
    if (result != null) {
      jsonResult.put("result", result);
    }

    PrintWriter pw = null;
    try {
      pw = new PrintWriter(out);
      pw.print(jsonResult.toString(2));
      pw.flush();
    } finally{
      IOUtils.closeQuietly(pw);
    }
  }

  public static String toJsString(String text)
  {
	  String content;
	  if(text == null)
	  {
		  content = "";
	  }
	  else
	  {
		  content = text.replaceAll("\"", "\\\"")
                    .replaceAll("\n", "\\n" )
                    .replaceAll("\r", "\\r" );
	  }

	  return "\"" + content + "\"";
  }
}
