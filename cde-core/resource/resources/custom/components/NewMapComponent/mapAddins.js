(function() {

  var geonames = {
    name: "geonames",
    label: "GeoNames",
    defaults: {
      username: ''
    },
    implementation: function (tgt, st, opt) {
      var location;
      var name = st.address;
      var featureClass;
      if (!name) {
        //Check city
        if (st.city) {
          name = st.city;
          featureClass = 'P';
        } else if (st.county) {
          name = st.county;
          featureClass = 'A';
        } else if (st.region) {
          name = st.region;
          featureClass = 'A';
        } else if (st.state) {
          name=st.state;
          featureClass = 'A';
        } else if (st.country) {
          name = st.country;
          featureClass = 'A';
        }
      }

      var url = 'http://ws.geonames.org/searchJSON';
      var data = {
        q: name.replace(/&/g,","),
        maxRows: 1,
        dataType: "json",
        username: opt.username,
        featureClass: featureClass
      };
      if (featureClass) {
        data.featureClass = featureClass;
      }
      var success = function (result) {
        if (result.geonames && result.geonames.length > 0) {
          location = [parseFloat(result.geonames[0].lng),
                      parseFloat(result.geonames[0].lat)];
          st.continuationFunction(location);
        }
      };
      $.getJSON(url, data, success);
    }
  };
  Dashboards.registerAddIn("NewMapComponent", "LocationResolver", new AddIn(geonames));

  var nominatim = {
    name: "openstreetmap",
    label: "OpenStreetMap",
    defaults: {
    },
    implementation: function (tgt, st, opt) {
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
      var success = function (result) {
        if (result && result.length > 0) {
          var location = [parseFloat(result[0].lon),
                          parseFloat(result[0].lat)];
          st.continuationFunction(location);
        }
      };
      $.getJSON(url, data, success);

    }
  };
  Dashboards.registerAddIn("NewMapComponent", "LocationResolver", new AddIn(nominatim));


  var componentPath = Dashboards.getWebAppPath() + '/content/pentaho-cdf-dd/resources/custom/components/NewMapComponent';
  var urlMarker = {
    name: "urlMarker",
    label: "Url Marker",
    defaults: {
      defaultUrl:  componentPath + '/images/marker_grey.png'
    },
    implementation: function (tgt, st, opt) {
      if (st.url)
        return st.url;

      if (st.position) {
        switch (st.position % 5) {
        case 0:
          return componentPath + '/images/marker_grey.png';
        case 1:
          return componentPath + '/images/marker_blue.png';
        case 2:
          return componentPath + '/images/marker_grey02.png';
        case 3:
          return componentPath + '/images/marker_orange.png';
        case 4:
          return componentPath + '/images/marker_purple.png';
        }
      }

      return opt.defaultUrl;
    }
  };
  Dashboards.registerAddIn("NewMapComponent", "MarkerImage", new AddIn(urlMarker));

  var cggMarker = {
    name: "cggMarker",
    label: "CGG Marker",
    defaults: {},
    implementation: function(tgt, st, opt) {
      var url = wd.helpers.cggHelper.getCggDrawUrl() + '?script=' + st.cggGraphName;

      var width = st.width;
      var height = st.height;
      var cggParameters = {};
      if (st.width) cggParameters.width = st.width;
      if (st.height) cggParameters.height = st.height;

      cggParameters.noChartBg = true;

      for (parameter in st.parameters) {
        cggParameters[parameter] = st.parameters[parameter];
      }

      // Check debug level and pass as parameter
      var level = Dashboards.debug;
      if (level > 1) {
        cggParameters.debug = true;
        cggParameters.debugLevel = level;
      }

      for (parameter in cggParameters) {
        if (cggParameters[parameter] !== undefined) {
          url += "&param" + parameter + "=" + encodeURIComponent(cggParameters[parameter]);
        }
      }

      return url;

    }
  };
  Dashboards.registerAddIn("NewMapComponent", "MarkerImage", new AddIn(cggMarker));

})();
