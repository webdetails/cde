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

// The jquery.multiselect lib used by the editor uses $.fn.attr instead of $.fn.prop
// to get/set the property "checked".
// Making sure to redirect to the appropriate function when we detect this.
(function() {
  var originalAttr = $.fn.attr;
  $.fn.attr = function() {
    if(arguments[0] === "checked") {
      // the editor code is not relying on string values on an attribute "checked"
      // so its safe to just redirect attr to prop
      return $.fn.prop.apply(this, arguments);
    }
    return originalAttr.apply(this, arguments);
  }
})();
