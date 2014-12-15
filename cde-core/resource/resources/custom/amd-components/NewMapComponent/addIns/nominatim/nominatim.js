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
  'cdf/Dashboard',
  'cdf/lib/jquery'],
  function(AddIn, Dashboard, $) {
  
  var nominatim = new AddIn({
    name: "openstreetmap",
    label: "OpenStreetMap",
    defaults: {},
    implementation: function(tgt, st, opt) {
      var name = st.address;
      var keyword = 'q'; //Generic query
      if (!name) {
        //Check city
        if (st.city) {
          name = st.city;
          keyword = 'city';
        } else if (st.county) {
          name = st.county;
          keyword = 'county';
        } else if (st.region) {
          name = st.region;
          keyword = 'q';
        } else if (st.state) {
          name=st.state;
          keyword = 'state=';
        } else if (st.country) {
          name = st.country;
          keyword = 'country';
        }
      }
      var url = 'http://nominatim.openstreetmap.org/search';
      var data = {
        format: 'json',
        limit: '1'
      };
      data[keyword] = name;
      var success = function(result) {
        if(result && result.length > 0) {
          var location = [parseFloat(result[0].lon),
                          parseFloat(result[0].lat)];
          st.continuationFunction(location);
        }
      };
      $.getJSON(url, data, success);

    }
  });
  Dashboard.registerGlobalAddIn("NewMapComponent", "LocationResolver", nominatim);

  return nominatim;

});
