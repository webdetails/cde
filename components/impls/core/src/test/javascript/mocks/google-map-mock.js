/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


define('google', [], function() {

  window.google = window.google || {
    maps: {
      Map: function () { },
      Point: function () { },
      event: {
        trigger: function () { },
        addListener: function () { }
      },
      LatLng: function () { },
      InfoWindow: function () {
        return {
          open: function () {}
        }
      },
      Size: function () { },
      OverlayView: function () { },
      Marker: function () { }
    }
  };

});
