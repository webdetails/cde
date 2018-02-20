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
