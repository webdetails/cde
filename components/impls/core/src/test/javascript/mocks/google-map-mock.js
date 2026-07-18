/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
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
