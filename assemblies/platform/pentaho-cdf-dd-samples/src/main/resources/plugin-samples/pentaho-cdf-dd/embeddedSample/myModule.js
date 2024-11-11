/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/
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
