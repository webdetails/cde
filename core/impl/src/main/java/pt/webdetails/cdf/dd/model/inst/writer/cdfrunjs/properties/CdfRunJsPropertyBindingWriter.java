/*!
 * Copyright 2002 - 2017 Webdetails, a Hitachi Vantara company. All rights reserved.
 *
 * This software was developed by Webdetails and is provided under the terms
 * of the Mozilla Public License, Version 2.0, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */

package pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.properties;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pt.webdetails.cdf.dd.model.core.Thing;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriteContext;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriter;
import pt.webdetails.cdf.dd.model.core.writer.ThingWriteException;
import pt.webdetails.cdf.dd.model.core.writer.js.JsWriterAbstract;
import pt.webdetails.cdf.dd.model.inst.PropertyBinding;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteContext;
import pt.webdetails.cdf.dd.util.JsonUtils;

public abstract class CdfRunJsPropertyBindingWriter extends JsWriterAbstract implements IThingWriter {
  /*
   (?s) -> Activate single-line mode, which makes the regex «.» char match anything including \n and \r.
   
   Written without string escaping:
     «(?s)"(function\s*\(.*?})"»
   
   Essentially, this matches and captures (^) stuff like:
     «"function (.... }"»
       ^--------------^
   */
  protected static final Pattern _wrappedFunctionPattern = Pattern.compile( "(?s)\"(function\\s*\\(.*?})\"" );

  /*
   Avoid escaping ( and ), use:
     \u0028 -> (
     \u0029 -> )

   Avoid escaping { and }, use:
     \u007b -> {
     \u007d -> }
   
   Matches something like one of the following texts:
     «function (...) { ... }»
     «"function (...) { ... }"»
     «function abc (...) { ... }»
     «"function abc (...) { ... }"»
  
   REGEX: (\"|\s)*function\s*\u0028.*\u0029{1}\s*\u007b.*(\u007d(\"|\s)*)?|(\"|\s)
   *function\s*[a-zA-Z0-9\u002d\u005f]+\u0028.*\u0029{1}\s*\u007b.*(\u007d(\"|\s)*)?
   JAVA STRING REGEX:
  */
  protected static final Pattern _maybeWrappedFunctionValue = Pattern.compile(
      "(\\\"|\\s)*function\\s*\\u0028.*\\u0029{1}\\s*\\u007b.*(\\u007d(\\\"|\\s)*)?|(\\\"|\\s)"
      + "*function\\s*[a-zA-Z0-9\\u002d\\u005f]+\\u0028.*\\u0029{1}\\s*\\u007b.*(\\u007d(\\\"|\\s)*)?" );

  public void write( Object output, IThingWriteContext context, Thing t ) throws ThingWriteException {
    this.write( (StringBuilder) output, (CdfRunJsDashboardWriteContext) context, (PropertyBinding) t );
  }

  public abstract void write( StringBuilder out, CdfRunJsDashboardWriteContext context, PropertyBinding propBind )
    throws ThingWriteException;


  protected String writeString( String canonicalValue ) {
    /*
     Previous implementation was smart, IMHO, in this strange way.
        if (value.charAt(0) != '\"')
        {
          value = "\"" + value + "\"";
        }
        
        //escape newlines
        value = StringUtils.replace(value, "\n", "\\n");
        value = StringUtils.replace(value, "\r", "\\r");
        
     If this is to be taken into account, I think that:
     1) Only if it doesn't start with «"» should the \n and \r replacements be done.
     2) Should probably be done on the reader code, as canonical is... already canonical.
     */
    return fixScriptEndTags( JsonUtils.toJsString( canonicalValue ) );
  }

  protected String writeNumber( String canonicalValue ) {
    return canonicalValue;
  }

  protected String writeBoolean( String canonicalValue ) {
    return canonicalValue.equals( "true" ) ? "true" : "false";
  }

  protected String writeLiteral( String canonicalValue ) {
    return canonicalValue;
  }

  protected String writeArray( String canonicalValue ) {
    /*
     Arrays of what?
     
     The case of the extensionPoint property; in cdfde.js:
      
     {
      "name": "cccExtensionPoints",
      "value": "[[\"xAxisLabel_textAngle\",\"-0.3\"],[\"xAxisLabel_textAlign\",\"right\"]]",
      "type": "ValuesArray"
     }
     
     Note that property type "ValuesArray" has value type "Array".
     
     When the value property is read, 
     we a get a string with the characters between « and »:
      
       «[["xAxisLabel_textAngle","-0.3"],["xAxisLabel_textAlign","right"]]»
     
     Also, note that extension point values may be functions, 
     in which case they would literally come as:
       «"function(){}"»
        
     Our goal here is to unwrap functions from their string wrappers
     and output them as JS function literals.
     
     See the comments on _wrappedFunctionPattern.
     */
    Matcher m = _wrappedFunctionPattern.matcher( canonicalValue );
    if ( !m.find() ) {
      // an array of strings could contain script end tags, fix those cases
      return fixScriptEndTags( canonicalValue );
    }

    StringBuffer sb = new StringBuffer();
    do {
      // 1. The function without the wrapping quotes
      String rep = m.group( 1 );

      // 2. Unescape quotes inside the function: «\\"» -> «"»
      rep = rep.replaceAll( "\\\"", "\"" );

      // 3. «$» inside the function into «\$»: MATCH: «\$» -> REPLACE: «\\\$» -> «\$»
      // TODO: WHYYYYYY????????? 
      // SOMEONE EXPLAIN THIS SHITY CODE!
      rep = rep.replaceAll( "\\$", "\\\\\\$" );

      // 4. remove all newlines
      rep = rep.replaceAll( "\\\\n", " " ).replaceAll( "\\\\r", " " );

      // Replace the whole match (including the wrapping quotes)
      m.appendReplacement( sb, rep );
    } while ( m.find() );

    // Remaining text till the end
    m.appendTail( sb );

    return sb.toString();
  }

  protected String writeFunction( String canonicalValue ) {
    // See comments on _maybeWrappedFunctionValue.
    Matcher matcher = _maybeWrappedFunctionValue.matcher( canonicalValue );
    if ( matcher.find() ) {
      // TODO: It's a function already, but possibly wrapped... 
      // Output it wrapped???
      return canonicalValue;
    }

    // ASSUME it's a ~literal~ string expression.
    // Compile into a fucntion that builds/evaluates the string in runtime.

    // 1. remove all newlines
    canonicalValue = canonicalValue.replace( "\n", " " ).replace( "\r", " " );

    // 2. escape «"»
    canonicalValue = canonicalValue.replace( "\"", "\\\"" );

    // 3. change ${} with " + ${} + "  (assuming it's in the middle of a string)
    canonicalValue = canonicalValue.replaceAll( "(\\$\\{[^}]*\\})", "\"+ $1 + \"" );

    // 4 -> return a function with the expression «function() { return "..."; }»
    return "function() { return \"" + canonicalValue + "\"; }";
  }

  protected String fixScriptEndTags( String toFix ) {
    return toFix.replaceAll( "</script>", "</\" + \"script>" );
  }
}
