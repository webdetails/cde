package pt.webdetails.cdf.dd.render.layout;

import org.apache.commons.jxpath.JXPathContext;

public class BootstrapColumnRender extends DivRender {

  private String cssClass;

  public BootstrapColumnRender( JXPathContext context ) {
    super( context );
  }

  @Override
  public void processProperties() {

    super.processProperties();
    cssClass = getBootstrapClassString();

  }

  @Override
  public String renderStart() {

    String div = "<div ";

    div += cssClass + ">";

    div += "<div " + getPropertyBagString() + ">";

    return div;
  }


  private boolean lastColumn() {
    String parentId = (String) getNode().getValue( "parent" );
    return ( (Boolean) getNode().getValue(
      "not(following-sibling::*[parent='" + parentId + "'][type='LayoutBootstrapColumn'])" ) ).booleanValue();
  }

  @Override
  public String renderClose() {
    return "</div></div>";
  }

  private String getBootstrapClassString() {
    String css = "class='";

    if ( !getPropertyString( "bootstrapExtraSmall" ).equals( "" ) ) {
      css += "col-xs-" + getPropertyString( "bootstrapExtraSmall" );
    }
    if ( !getPropertyString( "bootstrapSmall" ).equals( "" ) ) {
      css += " col-sm-" + getPropertyString( "bootstrapSmall" );
    }
    if ( !getPropertyString( "bootstrapMedium" ).equals( "" ) ) {
      css += " col-md-" + getPropertyString( "bootstrapMedium" );
    }
    if ( !getPropertyString( "bootstrapLarge" ).equals( "" ) ) {
      css += " col-lg-" + getPropertyString( "bootstrapLarge" );
    }
    if ( !getPropertyString( "bootstrapCssClass" ).equals( "" ) ) {
      css += " " + getPropertyString( "bootstrapCssClass" );
    }
    if ( lastColumn() ) {
      css += " last";
    }

    css += "'";
    return css;
  }
}
