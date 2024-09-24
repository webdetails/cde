/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package pt.webdetails.cdf.dd.editor;


import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DashboardEditorTest {

  @Test
  public void testProcessDashboardSupportTag() {

    final String editorPage = "<script type=\"text/javascript\" "
      + "src=\"@CDE_RENDERER_API@/getComponentDefinitions?supports=@SUPPORT_TYPE@\"></script>";

    assertEquals(
      "<script type=\"text/javascript\" src=\"@CDE_RENDERER_API@/getComponentDefinitions?supports=legacy\"></script>",
      DashboardEditor.processDashboardSupportTag( editorPage, false ) );

    assertEquals(
      "<script type=\"text/javascript\" src=\"@CDE_RENDERER_API@/getComponentDefinitions?supports=amd\"></script>",
      DashboardEditor.processDashboardSupportTag( editorPage, true ) );
  }
}
