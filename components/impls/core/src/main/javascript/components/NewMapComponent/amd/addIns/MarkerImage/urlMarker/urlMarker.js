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


define([
  "cdf/AddIn",
  "cdf/Dashboard.Clean",
  "../../../Map.ext"
], function(AddIn, Dashboard, NewMapComponentExt) {
  "use strict";
  var urlMarker = {
    name: "urlMarker",
    label: "Url Marker",
    defaults: {
      defaultUrl: NewMapComponentExt.getMarkerImgPath() + "marker_grey.png",
      imagePath: NewMapComponentExt.getMarkerImgPath(),
      images: [
        "marker_grey.png",
        "marker_blue.png",
        "marker_grey02.png",
        "marker_orange.png",
        "marker_purple.png"
      ]
    },
    implementation: function(tgt, st, opt) {
      if (st.url) {
        return st.url;
      }
      if (st.position) {
        return opt.imagePath + opt.images[st.position % opt.images.length] || opt.defaultUrl;
      }

      return opt.defaultUrl;
    }
  };

  Dashboard.registerGlobalAddIn("NewMapComponent", "MarkerImage", new AddIn(urlMarker));

  return urlMarker;

});
