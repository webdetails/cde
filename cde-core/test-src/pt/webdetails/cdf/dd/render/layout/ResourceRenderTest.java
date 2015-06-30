package pt.webdetails.cdf.dd.render.layout;

import org.apache.commons.jxpath.JXPathContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import pt.webdetails.cdf.dd.CdeConstants;

import static org.mockito.Mockito.when;

public class ResourceRenderTest {
  ResourceRender resourceRender;

  @Before
  public void setUp() throws Exception {
    JXPathContext context = Mockito.mock( JXPathContext.class );
    resourceRender = new ResourceRender( context );
  }

  @Test
  public void testRenderStart() {
    // CSS file
    when( resourceRender.getPropertyString( CdeConstants.RESOURCE_FILE ) ).thenReturn( "test.css" );
    when( resourceRender.getPropertyString( CdeConstants.RESOURCE_TYPE ) ).thenReturn( CdeConstants.CSS );
    Assert
      .assertEquals( resourceRender.renderStart(), "<link rel=\"stylesheet\" type=\"text/css\" href=\"test.css\" />" );

    // JS file
    when( resourceRender.getPropertyString( CdeConstants.RESOURCE_FILE ) ).thenReturn( "test.js" );
    when( resourceRender.getPropertyString( CdeConstants.RESOURCE_TYPE ) ).thenReturn( CdeConstants.JAVASCRIPT );
    Assert
      .assertEquals( resourceRender.renderStart(), "<script language=\"javascript\" type=\"text/javascript\" "
        + "src=\"test.js\"></script>" );

    // clear previous return values
    when( resourceRender.getPropertyString( CdeConstants.RESOURCE_FILE ) ).thenReturn( "" );

    // CSS code snippet
    when( resourceRender.getPropertyString( CdeConstants.RESOURCE_CODE ) ).thenReturn( "div {color: inherit;}" );
    when( resourceRender.getPropertyString( CdeConstants.RESOURCE_TYPE ) ).thenReturn( CdeConstants.CSS );
    Assert
      .assertEquals( resourceRender.renderStart(), "<style>\n<!--\ndiv {color: inherit;}\n-->\n</style>" );

    // JS code snippet
    when( resourceRender.getPropertyString( CdeConstants.RESOURCE_CODE ) ).thenReturn( "(function() { return; })();" );
    when( resourceRender.getPropertyString( CdeConstants.RESOURCE_TYPE ) ).thenReturn( CdeConstants.JAVASCRIPT );
    Assert
      .assertEquals( resourceRender.renderStart(), "<script language=\"javascript\" type=\"text/javascript\">\n"
        + "(function() { return; })();\n</script>" );
  }
}
