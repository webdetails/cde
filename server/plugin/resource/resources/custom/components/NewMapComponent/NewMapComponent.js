/** 
*
* NewMapComponent 
*
* <DESCRIPTION>
*
*/


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
		}
	
	
		name = name.replace(/&/g,",");
		var request = 'http://ws.geonames.org/searchJSON?q=' +  encodeURIComponent(name)  + '&maxRows=1&featureClass=' + featureClass + '&callback=' + callBackName;
		
		
			var aObj = new JSONscriptRequest(request);
			// Build the script tag
			aObj.buildScriptTag();
			// Execute (add) the script tag
			aObj.addScriptTag();        
    }
  };
  Dashboards.registerAddIn("NewMapComponent", "LocationResolver", new AddIn(geonames));
  
  
  var urlMarker = {
  	name: "urlMarker",
  	label: "Url Marker",
  	defaults: {
  		defaultUrl:  'getResource/system/pentaho-cdf-dd/resources/custom/components/NewMapComponent/images/marker_grey.png'
  	},
  	implementation: function (tgt, st, opt) {
  		if (st.url) 
	  		return st.url;  	  	
	  	
	  	if (st.position) {
	  		switch (st.position % 5) {
	  			case 0:
	  				return 'getResource/system/pentaho-cdf-dd/resources/custom/components/NewMapComponent/images/marker_grey.png';
	  			case 1:
	  				return 'getResource/system/pentaho-cdf-dd/resources/custom/components/NewMapComponent/images/marker_blue.png'
	  			case 2:
	  				return 'getResource/system/pentaho-cdf-dd/resources/custom/components/NewMapComponent/images/marker_grey02.png';
	  			case 3:
	  				return 'getResource/system/pentaho-cdf-dd/resources/custom/components/NewMapComponent/images/marker_orange.png';
	  			case 4:
	  				return 'getResource/system/pentaho-cdf-dd/resources/custom/components/NewMapComponent/images/marker_purple.png';
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




var NewMapComponent = BaseComponent.extend({
    ph: undefined,
    mapEngine: undefined,
    values: undefined,
    update : function() {
       var myself=this;

       	if (this.testData) {                     
        	this.render(this.testData); 
           	return;
       	}

		if (!myself.queryDefinition)  {  
			myself.render({});
			return
		}
       var query = new Query(myself.queryDefinition);
        query.fetchData(myself.parameters, function(values) {
            var changedValues = undefined;
            if((typeof(myself.postFetch)=='function')){
                changedValues = myself.postFetch(values);                
            }
            if (changedValues !== undefined) {
                values = changedValues;
            }
            myself.render(values);
        });
        
    },
    
    render: function(values) {
        
          
        if (this.mapEngineType == 'google') 
            this.mapEngine = new GoogleMapEngine();
        else
            this.mapEngine = new OpenStreetMapEngine();
            
        this.values = values;    
        this.mapEngine.init(this);                
    },
    
    initCallBack: function() {

        this.ph = $("#" + this.htmlObject);
        this.ph.empty();                
        this.mapEngine.renderMap(this.ph[0], this.centerLongitude, this.centerLatitude, this.defaultZoomLevel);                 
        this.setupMarkers(this.values);                
    },
        
    setupMarkers: function(values) {
        //Build an hashmap from metadata
        var mapping = this.getMapping(values);
        
        if (!values || !values.resultset)
            return;
        

        var myself = this;
        $(values.resultset).each(function (i, elt) {
            var location;
            if (mapping.addressType != 'coordinates') {
                location = myself.getAddressLocation((mapping.address != undefined ? elt[mapping.address]:undefined), mapping.addressType, elt, mapping, i);
            } else {
                location = [elt[mapping.longitude], elt[mapping.latitude]];
                myself.renderMarker(location, elt, mapping, i);
            }                        
        });
        
    },
    
    renderMarker: function(location, elt, mapping, position) {
        var myself = this;  
            if (location === undefined) {
                Dashboards.log('Unable to get location for address ' + elt[mapping.address] + '. Ignoring element.');
                return true;
            }
                    
            
            var marker;            
            var description;
            
		    var markerWidth = myself.markerWidth;
            if (mapping.markerWidth) 
                markerWidth = elt[mapping.markerWidth];
            var markerHeight = myself.markerHeight;
            if (mapping.markerHeight)
                markerHeight = elt[mapping.markerHeight];            
            
            var defaultMarkers = false;
            
            if (mapping.marker) marker = elt[mapping.marker];         
            if (marker == undefined) {
            	var st = {data: elt, position: position};
				var addinName = this.markerImageGetter;				
				
				//Check for cgg graph marker  
				if (this.markerCggGraph) {
					st.cggGraphName = this.markerCggGraph;
					st.width = markerWidth;
					st.height = markerHeight;
					st.parameters = {};
					$(this.cggGraphParameters).each (function (i, parameter) {
						st.parameters[parameter[0]] =  elt[mapping[parameter[1]]];
					});
					addinName = 'cggMarker';			
				} else {	
					//Else resolve to urlMarker addin
					st.url = myself.marker;
					defaultMarkers = myself.marker == undefined;
					addinName = 'urlMarker';				
				}
				
        		if (!addinName) addinName = 'urlMarker';
		        var addIn = this.getAddIn("MarkerImage",addinName);        		        
		        marker = addIn.call(this.ph, st, this.getAddInOptions("MarkerImage", addIn.name));				
            }

            if (mapping.description) description = elt[mapping.description];
                        
            var clickFunction = function(data, mapElement) {

                $(myself.popupParameters).each(function (i, eltA) {
                    Dashboards.fireChange(eltA[1], data[ mapping[ eltA[0].toLowerCase() ] ]);                             
                });
                
                if (myself.markerClickFunction)
                    myself.markerClickFunction(data);
                    
                if (myself.popupContentsDiv || mapping.popupContents) {
                	var contents;
                	if (mapping.popupContents) contents = elt[mapping.popupContents];
                    var height = mapping.popupContentsHeight ? elt[mapping.popupContentsHeight] : undefined;
                    if (!height) height = myself.popupHeight;
                    var width = mapping.popupContentsWidth? elt[mapping.popupContentsWidth] : undefined;
                    if (!width) width = myself.popupWidth;
//                    if (!contents) contents = $("#" + myself.popupContentsDiv).html();                    

					var borderColor = undefined;
					if (defaultMarkers) {
						switch (position % 5) {
							case 0:
								borderColor = "#394246"; 
								break;
							case 1:
								borderColor = "#11b4eb"; 
								break;							
							case 2:
								borderColor = "#7a879a";
								break;
							case 3:
								borderColor = "#e35c15";
								break;
							case 4:
								borderColor = "#674f73";
								break;
						}
					}
                    myself.mapEngine.showPopup(data,  mapElement, height, width, contents, myself.popupContentsDiv, borderColor);
                }
            };

      
            Dashboards.log('About to render ' + location[0] + ' / ' + location[1] + ' with marker ' + marker + ' sized ' + markerHeight + ' / ' + markerWidth + 'and description ' + description);
            myself.mapEngine.setMarker(location[0], location[1], marker, description, elt, clickFunction, markerWidth, markerHeight);
        
    },
    
    getAddressLocation: function (address, addressType, data, mapping, position) {

        var addinName = this.locationResolver;
        if (!addinName) addinName = 'geonames';
        var addIn = this.getAddIn("LocationResolver",addinName);        
        
        
        target = this.ph;
        var state = {address: address, addressType: addressType, position: position};
        if (mapping.country != undefined) state.country = data[mapping.country];
        if (mapping.city != undefined) state.city = data[mapping.city];
        if (mapping.county != undefined) state.county = data[mapping.county];
        if (mapping.region != undefined) state.region = data[mapping.region];        
        if (mapping.state != undefined) state.state = data[mapping.state];        
        var myself = this;
        state.continuationFunction = function (location) {
            myself.renderMarker(location, data, mapping, position);
        }
        addIn.call(target,state,this.getAddInOptions("LocationResolver",addIn.name));
    },
    
    getMapping: function(values) {
        var map = {};
        
  
        
        
        if(!values.metadata || values.metadata.length == 0)
            return map;

		//Iterate through the metadata. We are looking for the following columns:
		// * address or one or more of 'Country', 'State', 'Region', 'County', 'City'
		// * latitude and longitude - if found, we no longer care for address 
		// * description - Description to show on mouseover
		// * marker - Marker image to use - usually this will be an url
		// * markerWidth - Width of the marker
		// * markerHeight - Height of the marker
		// * popupContents - Contents to show on popup window
		// * popupWidth - Width of the popup window
		// * popupHeight - Height of the popup window
		
		$(values.metadata).each(function (i, elt) {
			
			switch (elt.colName.toLowerCase()) {
				case 'latitude':
					map.addressType = 'coordinates';
					map.latitude = i;
					break;
				case 'longitude':
					map.addressType = 'coordinates';
					map.longitude = i;
					break;
				case 'description':
					map.description = i;
					break;
				case 'marker':
					map.marker = i;
					break;
				case 'markerWidth':
					map.markerWidth = i;
					break;
				case 'markerHeight':
					map.markerHeight = i;
					break;
				case 'popupContents':
					map.popupContents = i;
					break;
				case 'popupWidth':
					map.popupWidth = i;
					break;
				case 'popupHeight':
					map.popupHeight = i;
					break;
				case 'address':
					if (!map.addressType) {
						map.address = i;
						map.addressType = 'address';
					}
					break;
				default:
					map[elt.colName.toLowerCase()] = i;
					break;
				// if ($.inArray(values.metadata[0].colName, ['Country', 'State', 'Region', 'County', 'City'])) {
			}
				
		});

        return map;
    }
    
    
});


var GoogleMapEngine = Base.extend({
    map: undefined,
    centered: false,
    overlays: [],
    init: function(mapComponent) {         
        
        
        $.when( loadGoogleMaps(3, API_KEY) ).then (
            function (status) {
        OurMapOverlay.prototype = new google.maps.OverlayView();
        OurMapOverlay.prototype.onAdd = function() {
            // Note: an overlay's receipt of onAdd() indicates that
            // the map's panes are now available for attaching
            // the overlay to the map via the DOM.

            // Create the DIV and set some basic attributes.
            var div = document.createElement('DIV');
            div.id = 'MapOverlay';
            div.style.position = "absolute";
            
            if (this.borderColor_) {
            	div.style.border = '3px solid ' + this.borderColor_;
            } else {
				div.style.border = "none";
            }

			
/*			var myself = this;
			var closeDiv = $("<div id=\"MapOverlay_close\" class=\"olPopupCloseBox\" style=\"position: absolute;\"></div>");
			closeDiv.click(function () {
				myself.setMap(null);
			});

			$(div).append(closeDiv);
*/
			if (this.popupContentDiv_ && this.popupContentDiv_.length > 0) {
				$(div).append($('#' + this.popupContentDiv_));			
			} else
	            div.innerHTML = this.htmlContent_;


            // Set the overlay's div_ property to this DIV
            this.div_ = div;

            // We add an overlay to a map via one of the map's panes.
            // We'll add this overlay to the overlayImage pane.
            var panes = this.getPanes();
            panes.overlayLayer.appendChild(div);
        };

        OurMapOverlay.prototype.draw = function() {
            // Size and position the overlay. We use a southwest and northeast
            // position of the overlay to peg it to the correct position and size.
            // We need to retrieve the projection from this overlay to do this.
            var overlayProjection = this.getProjection();

            // Retrieve the southwest and northeast coordinates of this overlay
            // in latlngs and convert them to pixels coordinates.
            // We'll use these coordinates to resize the DIV.
            var sp = overlayProjection.fromLatLngToDivPixel(this.startPoint_);
  
            // Resize the DIV to fit the indicated dimensions.
            var div = this.div_;
            div.style.left = sp.x + 'px';
            div.style.top = (sp.y + 30) + 'px';
            div.style.width = this.width_ + 'px';
            div.style.height = this.height_ + 'px';
        };


        OurMapOverlay.prototype.onRemove = function() {
        	if (this.popupContentDiv_) {
//        	 	$('#' + this.popupContentDiv_).append($(this.div_));
//        	 	$(this.div_).detach();
        	}
        	this.div_.style.display = 'none';
//            this.div_.parentNode.removeChild(this.div_);
 //           this.div_ = null;
        };
               
        mapComponent.initCallBack(); 
        
            });
    },
    
    setMarker: function(lon, lat, icon, description, data, clickFunction, markerWidth, markerHeight) {
        var myLatLng = new google.maps.LatLng(lat,lon);
        
        var image = new google.maps.MarkerImage(icon,
            // This marker is 20 pixels wide by 32 pixels tall.
            new google.maps.Size(markerWidth, markerHeight),
            // The origin for this image is 0,0.
            new google.maps.Point(0,0),
            // The anchor for this image is the base of the flagpole at 0,32.
            new google.maps.Point(0, 0));
        
        
        
        var marker = new google.maps.Marker({
            position: myLatLng,
            map: this.map,
            icon: image,
            title: description
        });
        
        if (clickFunction !== undefined) {
            google.maps.event.addListener(marker, 'click', function () {clickFunction(data, marker);});            
        }
        
        if (!this.centered)
	        this.map.setCenter(myLatLng);        	               
    },
    
    renderMap: function(target, centerLongitude, centerLatitude, zoomLevel) {
        var latlng;
        
        if (centerLatitude && centerLatitude != '' && centerLongitude && centerLongitude != '') {
	        latlng = new google.maps.LatLng(centerLatitude, centerLongitude);        
	        this.centered = true;
	    } else
	    	latlng = new google.maps.LatLng(38.471, -9.15);        

		if (!zoomLevel) zoomLevel = 2;
		
        var myOptions = {
            zoom: zoomLevel,
            center: latlng,
            mapTypeId: google.maps.MapTypeId.ROADMAP
        };
        this.map = new google.maps.Map(target, myOptions);
    },
    
    showPopup: function(data, mapElement, popupHeight, popupWidth, contents, popupContentDiv, borderColor) {



        var overlay = new OurMapOverlay(mapElement.getPosition(), popupWidth, popupHeight, contents, popupContentDiv, this.map, borderColor);
        
       $(this.overlays).each(function (i, elt) {elt.setMap(null);});        
        this.overlays.push(overlay);        
    }

});


function OurMapOverlay(startPoint, width, height, htmlContent, popupContentDiv, map, borderColor) {

  // Now initialize all properties.
  this.startPoint_ = startPoint;
  this.width_ = width;
  this.height_ = height;
  this.map_ = map;
  this.htmlContent_ = htmlContent;
  this.popupContentDiv_ = popupContentDiv;
  this.borderColor_ = borderColor;
    
  this.div_ = null;

  // Explicitly call setMap() on this overlay
  this.setMap(map);
}



var OpenStreetMapEngine = Base.extend({
    map: undefined,
    markers: undefined,
    centered: false,
//    featureLayer: undefined,
    useMercator: true,
    init: function(mapComponent) {
       mapComponent.initCallBack();        
    },
    
    showPopup: function(data,  mapElement, popupHeight, popupWidth, contents, popupContentDiv, borderColor) {

        var feature = mapElement;                
        
        if (popupContentDiv && popupContentDiv.length > 0) {
        	var div = $('<div>');
        	div.append($('#' + popupContentDiv));
        	contents = div.html();
        }
        
        var name = "featurePopup";
        if (borderColor != undefined) {
        	name = name + borderColor.substring(1);
        }
        
        popup = new OpenLayers.Popup.Anchored(name,
                             feature.lonlat,
                             new OpenLayers.Size(popupHeight,popupWidth),
                             contents,
                             null, true, null);
        
        feature.popup = popup;
        popup.feature = feature;
        
        $(this.map.popups).each(function (i, elt) {elt.hide();});
        
        this.map.addPopup(popup);                
    },
    
    setMarker: function(lon, lat, icon, description, data, clickFunction, markerWidth, markerHeight) {    
		var size = new OpenLayers.Size(markerWidth,markerHeight);
    	var offset = new OpenLayers.Pixel(-(size.w/2), -size.h);
		var iconObj = new OpenLayers.Icon(icon,size,offset);
        var marker, feature;


    	if(this.useMercator){
	        marker = new OpenLayers.Marker(lonLatToMercator(new OpenLayers.LonLat(lon,lat)),iconObj);		
			//create a feature to bind marker and record array together
			feature = new OpenLayers.Feature(this.markers,lonLatToMercator(new OpenLayers.LonLat(lon,lat)),data);
		} else {
			marker = new OpenLayers.Marker(new OpenLayers.LonLat(lon,lat),iconObj);
			feature = new OpenLayers.Feature(this.markers,new OpenLayers.LonLat(lon,lat),data);
		}

        feature.marker = marker;

        //create mouse down event for marker, set function to marker_click 
        if (clickFunction != undefined)
		    marker.events.register('mousedown', feature, function (evt) {clickFunction(data, feature);});

/*
        if (description) {
            var ttips = new OpenLayers.Control.ToolTips({bgColor:"black",textColor:"white", bold : true, opacity : 0.50});            
    		this.map.addControl(ttips);
            var funcOnMouseOver = function (evt) {
                ttips.show({html:description});            
            };
            
            var funcOnMouseOut = function (evt) {
                ttips.hide();    
            };
            
    	    marker.events.register('mouseover', feature, funcOnMouseOver);
			marker.events.register('mouseout', feature, funcOnMouseOut);
        }
  */                      
        this.markers.addMarker(marker);
        
        if (!this.centered)
        	this.map.setCenter(marker.lonlat);
    },
    
    renderMap: function(target, centerLongitude, centerLatitude, zoomLevel) {
        Dashboards.log('Entered renderMap');
        var useLayerControl = false;
        var customMap = false;
        var centerPoint;
        
	if (centerLongitude && centerLongitude != '' && centerLatitude && centerLatitude != '') {
	    if(this.useMercator) {
			centerPoint = lonLatToMercator(new OpenLayers.LonLat(centerLongitude,centerLatitude));
		}else{
			centerPoint = new OpenLayers.LonLat(centerLongitude,centerLatitude);
		}
	}

    	this.map = new OpenLayers.Map(target, {maxExtent: new OpenLayers.Bounds(-20037508,-20037508,20037508,20037508),
                      numZoomLevels: 18,
                      maxResolution: 156543,
                      units: 'm',
                      projection: "EPSG:41001" 
                      
        });



        var layer = new OpenLayers.Layer.TMS(
                "OpenStreetMap","http://tile.openstreetmap.org/",
	           {
				 type: 'png', getURL: osm_getTileURL, 
                 transparent: 'true',
	             displayOutsideMaxExtent: true}
	            );
    	// add the OpenStreetMap layer to the map          
		this.map.addLayer(layer);
    
    
         // add a layer for the markers                                             
    this.markers = new OpenLayers.Layer.Markers( "Markers" );
	this.map.addLayer(this.markers);
	    
	//set center and zoomlevel of the map
	if (centerPoint) {
		this.map.setCenter(centerPoint);
		this.centered = true;
	} else {
		this.map.setCenter(lonLatToMercator(new OpenLayers.LonLat(-9.15,38.46)));
	}

	if (zoomLevel != '')
		this.map.zoomTo(zoomLevel);

    Dashboards.log('Exited renderMap');

    }
});

