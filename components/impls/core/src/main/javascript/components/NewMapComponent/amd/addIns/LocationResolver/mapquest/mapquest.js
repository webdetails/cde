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



define([
  "cdf/lib/jquery",
  "cdf/AddIn",
  "cdf/Dashboard.Clean",
  "../nominatim/nominatim"
], function($, AddIn, Dashboard, nominatim) {
  "use strict";
  var mapquest = $.extend(true, {}, nominatim, {
    name: "mapquest",
    label: "MapQuest",
    defaults: {
      url: "http://open.mapquestapi.com/nominatim/v1/search"
    }
  });

  Dashboard.registerGlobalAddIn("NewMapComponent", "LocationResolver", new AddIn(mapquest));

  return mapquest;

});
