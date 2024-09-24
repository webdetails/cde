define(["cdf/lib/jquery", "cdf/Logger"], function($, Logger) {
  var myModule = {
    string: "TEST"
  };

  myModule.getString = function() {
    return this.string;
  };
  
  myModule.writeOnElement = function(selector, text) {
    var element = $(selector);
    if(element && element.length > 0) {
      element.text(text);
    } else {
      Logger.log("Selector " + selector + " wielded no results");
    }
  };

  return myModule;
});
