/*!
 * Copyright 2002 - 2015 Webdetails, a Pentaho company. All rights reserved.
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
;
(function () {
  var componentPath = Dashboards.getWebAppPath() + '/content/pentaho-cdf-dd/resources/custom/components/NewMapComponent';
  var urlMarker = {
    name: "urlMarker",
    label: "Url Marker",
    defaults: {
      defaultUrl: componentPath + '/images/marker_grey.png',
      imagePath: componentPath + '/images/',
      images: [
        'marker_grey.png',
        'marker_blue.png',
        'marker_grey02.png',
        'marker_orange.png',
        'marker_purple.png'
      ]
    },
    implementation: function (tgt, st, opt) {
      if (st.url)
        return st.url;
      if (st.position) {
        return opt.imagePath + opt.images[st.position % opt.images.length] || opt.defaultUrl;
      }
      return opt.defaultUrl;
    }
  };
  Dashboards.registerAddIn("NewMapComponent", "MarkerImage", new AddIn(urlMarker));

})();