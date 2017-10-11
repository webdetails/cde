/*!
 * Copyright 2002 - 2017 Webdetails, a Hitachi Vantara company. All rights reserved.
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
