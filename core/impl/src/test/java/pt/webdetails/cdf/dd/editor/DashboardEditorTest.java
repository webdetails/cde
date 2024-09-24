/*!
 * Copyright 2002 - 2021 Webdetails, a Hitachi Vantara company. All rights reserved.
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
