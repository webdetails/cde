(function() {

  var geonames = {
    name: "geonames",
    label: "GeoNames",
    defaults: {
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



      var opts =            {
        q: name,
        maxRows: 1,
        dataType: "json",
        featureClass: featureClass
      };



      var callBackName = 'GeoNameContinuation' + $.now() + st.position;
      window[callBackName] = function (result) {
        if (result.geonames && result.geonames.length > 0) {
          location = [parseFloat(result.geonames[0].lng),
                      parseFloat(result.geonames[0].lat)];
          st.continuationFunction(location);
        }
      };


      name = name.replace(/&/g,",");
      var request = 'http://ws.geonames.org/searchJSON?q=' +  encodeURIComponent(name)  + '&maxRows=1&callback=' + callBackName;
      request += ( featureClass ? '&featureClass=' + featureClass : '' ) ;


      var aObj = new JSONscriptRequest(request);
      // Build the script tag
      aObj.buildScriptTag();
      // Execute (add) the script tag
      aObj.addScriptTag();
    }
  };
  Dashboards.registerAddIn("NewMapComponent", "LocationResolver", new AddIn(geonames));

  // BEGIN HACK
  //if (wd.helpers.repository.getBaseSolutionPluginRoot() == '/public/' || true){
  if (true){
    // pentaho >= 5
    componentPath = '/pentaho/api/repos/pentaho-cdf-dd/resources/custom/components/NewMapComponent';
  } else {
    // pentaho < 5
    componentPath = 'getResource/system/pentaho-cdf-dd/resources/custom/components/NewMapComponent';
  }
  // END HACK

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
    defaults: {
    },
    implementation: function (tgt, st, opt) {
      var url = '../cgg/draw?script=' + st.cggGraphName;

      var width = st.width;
      var height = st.height;
      var cggParameters = {};
      if (st.width) cggParameters.width = st.width;
      if (st.height) cggParameters.height = st.height;

      for (parameter in st.parameters) {
        cggParameters[parameter] = st.parameters[parameter];
      }

      for(parameter in cggParameters){
        if( cggParameters[parameter] !== undefined ){
          url += "&param" + parameter + "=" + encodeURIComponent( cggParameters[parameter] ) ;
        }
      }

      return url;

    }
  };
  Dashboards.registerAddIn("NewMapComponent", "MarkerImage", new AddIn(cggMarker));

})();
