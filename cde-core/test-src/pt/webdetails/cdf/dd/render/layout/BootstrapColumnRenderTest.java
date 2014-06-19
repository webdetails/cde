package pt.webdetails.cdf.dd.render.layout;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.commons.jxpath.JXPathContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Iterator;

public class BootstrapColumnRenderTest extends TestCase {

  private BootstrapColumnRenderForTest renderForTest;

  private class BootstrapColumnRenderForTest extends BootstrapColumnRender {

    private String bootstrapCss;

    public BootstrapColumnRenderForTest( JXPathContext context ) {
      super( context );
      this.bootstrapCss = "";
    }

    @Override
    protected boolean lastColumn() {
      return false;
    }

    @Override
    protected String getBootstrapClassString() {
      return this.bootstrapCss;
    }

    private void setBootstrapCss( String css ) {
      this.bootstrapCss = " class='" + css + "'";
    }

    private void setPropertyBagClass( String css ) {
      getPropertyBag().addClass( css );
    }
  }

  @Before
  public void setUp() throws Exception {
    JXPathContext context = Mockito.mock( JXPathContext.class );
    renderForTest = new BootstrapColumnRenderForTest( context );
  }

  @Test
  public void testRenderStartNoCss() {
    renderForTest.processProperties();
    String div = renderForTest.renderStart();

    Assert.assertEquals( "<div><div >", div );

  }

  @Test
  public void testRenderStartWithBootstrapCss() {

    renderForTest.setBootstrapCss( "col-xs-5" );
    renderForTest.processProperties();

    String div = renderForTest.renderStart();

    Assert.assertEquals( "<div class='col-xs-5'><div >", div );
  }

  @Test
  public void testRenderStartWithPropertyBag() {

    renderForTest.setPropertyBagClass( "span-6" );
    renderForTest.processProperties();

    String div = renderForTest.renderStart();

    Assert.assertEquals( "<div><div  class='span-6 ' >", div );
  }

  @Test
  public void testRenderStartWithAll() {

    renderForTest.setPropertyBagClass( "span-6" );
    renderForTest.setBootstrapCss( "col-xs-5" );
    renderForTest.processProperties();

    String div = renderForTest.renderStart();

    Assert.assertEquals( "<div class='col-xs-5'><div  class='span-6 ' >", div );
  }

  @Test
  public void testRenderClose() {
    String div = renderForTest.renderClose();
    Assert.assertEquals( "</div></div>", div );
  }

}
