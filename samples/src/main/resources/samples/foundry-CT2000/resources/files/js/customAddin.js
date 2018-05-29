require(['cdf/AddIn','cdf/Dashboard.Clean','cdf/lib/jquery'], 

function(AddIn,Dashboard,$) {
    var flagAddin = {
        name: "flagAddin",
        label: "flagAddin",
        implementation: function (tgt, st, opt) {
            var t = $(tgt),
                country = st.series.replace(/ /g,"_"),
                myFlag = opt.imagesPath.replace('IMAGENAME', country);            
            t.prepend("<div class='topFlag' style='background-image: url("+myFlag+");'></div>");
            t.attr('title', st.series);
        }
    };
    Dashboard.registerGlobalAddIn("Table", "colType", new AddIn(flagAddin));
});