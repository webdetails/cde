/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/

require([
  'cdf/AddIn',
  'cdf/Dashboard.Clean',
  'cdf/lib/jquery'
], function (AddIn, Dashboard, $) {
  var flagAddin = {
    name: "flagAddin",
    label: "flagAddin",

    implementation: function (tgt, st, opt) {
      var target = $(tgt);
      var country = st.series.replace(/ /g, "_");
      var myFlag = opt.imagesPath.replace('IMAGENAME', country);

      target.prepend("<div class='topFlag' style='background-image: url(" + myFlag + ");'></div>");

      target.attr('title', st.series);
    }
  };

  Dashboard.registerGlobalAddIn("Table", "colType", new AddIn(flagAddin));
});
