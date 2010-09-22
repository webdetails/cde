/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.webdetails.cdf.dd.util;

import net.sf.json.JSON;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import java.io.*;

/**
 * @author pedro
 */
public class JsonUtils {

  public static JSON readJsonFromInputStream(final InputStream input) throws IOException {

    final InputStreamReader in = new InputStreamReader(input, "UTF-8");
    final BufferedReader bin = new BufferedReader(in);

    final StringBuffer str = new StringBuffer();
    String c;
    while ((c = bin.readLine()) != null) {
      str.append(c);
    }

    return (JSON) JSONSerializer.toJSON(str.toString());

  }

  public static void buildJsonResult(final OutputStream out, final Boolean sucess, final Object result) {

    final JSONObject jsonResult = new JSONObject();

    jsonResult.put("status", sucess.toString());
    if (result != null) {
      jsonResult.put("result", result);
    }

    final PrintWriter pw = new PrintWriter(out);
    pw.print(jsonResult.toString(2));
    pw.flush();
    pw.close();
  }
}
