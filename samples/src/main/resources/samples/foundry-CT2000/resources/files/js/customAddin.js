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
