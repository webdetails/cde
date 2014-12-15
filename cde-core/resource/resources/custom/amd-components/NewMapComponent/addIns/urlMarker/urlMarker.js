/*!
 * Copyright 2002 - 2014 Webdetails, a Pentaho company.  All rights reserved.
 *
 * This software was developed by Webdetails and is provided under the terms
 * of the Mozilla Public License, Version 2.0, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to  http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */

define([
  'cdf/AddIn',
  'cdf/Dashboard'],
  function(AddIn, Dashboard) {
  
  var urlMarker = new AddIn({
    name: "urlMarker",
    label: "Url Marker",
    defaults: {
      defaultUrl: '../pentaho-cdf-dd/resources/custom/amd-components/NewMapComponent/images/marker_grey.png'
    },
    implementation: function(tgt, st, opt) {
      if(st.url) {
        return st.url;
      }
      if(st.position) {
        switch(st.position % 5) {
          case 0:
            return '../pentaho-cdf-dd/resources/custom/amd-components/NewMapComponent/images/marker_grey.png';
          case 1:
            return '../pentaho-cdf-dd/resources/custom/amd-components/NewMapComponent/images/marker_blue.png';
          case 2:
            return '../pentaho-cdf-dd/resources/custom/amd-components/NewMapComponent/images/marker_grey02.png';
          case 3:
            return '../pentaho-cdf-dd/resources/custom/amd-components/NewMapComponent/images/marker_orange.png';
          case 4:
            return '../pentaho-cdf-dd/resources/custom/amd-components/NewMapComponent/images/marker_purple.png';
        }
      }

      return opt.defaultUrl;
    }
  });
  Dashboard.registerGlobalAddIn("NewMapComponent", "MarkerImage", urlMarker);

  return urlMarker;

});
