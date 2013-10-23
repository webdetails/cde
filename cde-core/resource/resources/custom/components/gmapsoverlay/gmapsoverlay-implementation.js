/*
This Source Code Form is subject to the 
terms of the Mozilla Public License, v. 2.0. 
If a copy of the MPL was not distributed 
with this file, You can obtain one at 
http://mozilla.org/MPL/2.0/.
*/

function submitGeocode(input) {
  return function(e) {
    var keyCode;
  
    if (window.event) {
      keyCode = window.event.keyCode;
    } /*else if (variable) {
      keyCode = e.which;
    }*/
  
    if (keyCode == 13) {
      geocoder.geocode( { address: input.value }, function(results, status) {
        if (status == google.maps.GeocoderStatus.OK) {
          map.fitBounds(results[0].geometry.viewport);
        } else {
          alert("The location entered could not be found");
        }
      });
    }
  }
}

function getColor (value, legendRanges) {
	var qtd = Object.keys(legendRanges.ranges).length;
	for (var j = 0; j < qtd; j++) {
		if ((isNaN(legendRanges.ranges[j].min) && value <= legendRanges.ranges[j].max) ||
			(isNaN(legendRanges.ranges[j].max) && value >= legendRanges.ranges[j].min) ||
			(value >= legendRanges.ranges[j].min && value <= legendRanges.ranges[j].max)) {
				return legendRanges.ranges[j].color;
		}
	}
}

var gMapsOverlayComponent = BaseComponent.extend({
		update : function() {
		
			myself = this;
		
			if (!myself.queryDefinition)  {  
				return
			}
			
			if (!myself.mapName)  {  
				return
			}
			
			if (!myself.mapHeight || !myself.mapWidth) {
				return
			}
			
			myself.legendRanges = new Object;
			if (myself.legend) {
					myself.legendRanges.ranges = new Object;
					myself.legendRanges.text = ((!myself.legendText) ? "" : myself.legendText);
					
				for (var i = 0; i < myself.legend.length; i++) {
					var opts = myself.legend[i][1].split(";");
					myself.legendRanges.ranges[i] = new Object;
					myself.legendRanges.ranges[i].min = parseFloat(opts[0]);
					myself.legendRanges.ranges[i].max = parseFloat(opts[1]);
					myself.legendRanges.ranges[i].color = opts[2];
					myself.legendRanges.ranges[i].desc = myself.legend[i][0];
				}
				
				myself.legendRanges.getColor = function (value) {
					for (var j = 0; j < Object.keys(myself.legendRanges.ranges).length; j++) {
						if ((isNaN(myself.legendRanges.ranges[j].min) && value <= myself.legendRanges.ranges[j].max) ||
							(isNaN(myself.legendRanges.ranges[j].max) && value >= myself.legendRanges.ranges[j].min) ||
							(value >= myself.legendRanges.ranges[j].min && value <= myself.legendRanges.ranges[j].max)) {
								return myself.legendRanges.ranges[j].color;
							}
					}

				}
			}

			var query = Dashboards.getQuery(myself.queryDefinition);
			query.fetchData(myself.parameters, function(values) {
				var changedValues = undefined;
				if((typeof(myself.postFetch)=='function')){
					changedValues = myself.postFetch(values);                
				}
				if (changedValues !== undefined) {
					values = changedValues;
				}
				
				myself.queryResult = new Object;
				for (i in values.resultset) {
					var item = values.resultset[i];
					myself.queryResult[item[0]] = new Object;
					myself.queryResult[item[0]].Qtd = item[1];
				}
				
			});
			
			$.getJSON(wd.helpers.repository.getRsourceUrl()+wd.helpers.repository.getBaseSolutionPluginRoot()+
                "cde/components/GMapsOverlay/map-def/" + myself.mapName + ".js", function(json) {
			
				if (!json)  {  
					return
				}

				myself.mapDefinition = json;
			});
			
			myself.initialize();
		},
		
		initialize: function() {
		
			this.mapEngine = new GMapEngine();
	        this.mapEngine.init(this);                
		},
		
	    initCallBack: function() {
		
			var myself = this;
			
			this.ph = $("#" + this.htmlObject);
			this.ph.empty();                
			this.mapEngine.createMap(this.ph[0], this.centerLongitude, this.centerLatitude, this.defaultZoomLevel, this.mapHeight, this.mapWidth);
			this.mapEngine.renderMap(this.mapDefinition, this.queryResult, ((!this.defaultColor) ? "#EAEAEA" : this.defaultColor), myself.legendRanges);
			this.mapEngine.resetButton(this.ph[0].id, this.defaultZoomLevel, this.centerLongitude, this.centerLatitude);
			
			if (this.search == true) {
				this.mapEngine.searchBox(this.ph[0].id);
			}
			
			if (Object.keys(myself.legendRanges.ranges).length > 0) {
				this.mapEngine.createLegend(this.ph[0].id, this.legendRanges);
			}
			
		}	

});

var GMapEngine = Base.extend({
    map: undefined,
	opened_info: undefined,
    centered: false,
    overlays: [],
    init: function(mapComponent) {         
        
        $.when( loadGoogleMapsOverlay() ).then (
            function (status) {
			
				mapComponent.initCallBack(); 

            });
    },
    
    createMap: function(target, centerLongitude, centerLatitude, defaultZoomLevel, mapHeight, mapWidth) {

		var mapOptions = {
			styles:[ { "featureType": "administrative", "stylers": [ { "visibility": "off" } ] } ],
			zoom: parseInt(defaultZoomLevel),
			center: new google.maps.LatLng(centerLatitude, centerLongitude),
			disableDefaultUI: false,
			mapTypeControl: false,
			streetViewControl: false,
			mapTypeId: google.maps.MapTypeId.TERRAIN
		};
				
        this.map = new google.maps.Map(target, mapOptions);
		this.opened_info = new google.maps.InfoWindow();
		
		$(target).css("height", mapHeight + "px");
		$(target).css("width", mapWidth + "px");
    },
	
    renderMap: function(mapDefinition, queryResult, defaultColor, legend) {
	
		var myself = this; 
		
		var color = defaultColor;
		
		for (var c in mapDefinition) {

			var coods = mapDefinition[c];
			var polyPath = [];

			for(j = 1; j < coods.length; j++){
				polyPath.push(new google.maps.LatLng(coods[j][0], coods[j][1]));
			}
			
			if (queryResult[c] && (Object.keys(legend.ranges).length > 0)) {
				color = getColor(queryResult[c].Qtd, legend);
			} else {
				color = defaultColor;
			}

			var shape = new google.maps.Polygon({
				paths:polyPath,
				fillColor:color,
				fillOpacity:1,
				strokeWeight:0.5
			});
			
			
			var message = "<b>" + c + "</b>" + "</br></br>";
			if (queryResult[c] != null) {
				message = message + Math.round(queryResult[c].Qtd*100)/100 + "</br>";
			}

			shape.infowindow = new google.maps.InfoWindow({
				content: message
			});
			
			
			shape.infowindow.name = c;
			shape.setMap(myself.map);
			
			google.maps.event.addListener(shape, 'click', function (event) {myself.showInfo(event, myself, this.infowindow);});
		}

		
	
    },
	
	showInfo: function (event, mapEngine, infowindow) {
			mapEngine.opened_info.close();
			//if (mapEngine.opened_info.name != infowindow.name) {
				infowindow.setPosition(event.latLng);
				infowindow.open(mapEngine.map);
				mapEngine.opened_info = infowindow;
			//}
	},
	
	resetButton: function (id, zoom, centerLongitude, centerLatitude) {

		var myself = this;
		
		var controlReset = document.createElement('div');
		var linkReset = document.createElement('a');
		controlReset.appendChild(linkReset);
		controlReset.setAttribute('id', 'controlReset_' + id);
		linkReset.setAttribute('id', 'linkReset_' + id);
		linkReset.href = "javascript:void(0)";
		linkReset.className = 'button';
		linkReset.onclick = (function () {	myself.map.setZoom(zoom);
											myself.map.setCenter(new google.maps.LatLng(centerLatitude, centerLongitude));
										});
		//linkReset.textContent = 'Reset';
		linkReset.innerHTML = 'Reset';
		
		myself.map.controls[google.maps.ControlPosition.TOP_LEFT].push(controlReset);
	},
	
	searchBox: function (id) {
	
			var myself = this;
			
			var control = document.createElement('div');
			var input = document.createElement('input');
			control.appendChild(input);
			control.setAttribute('id', 'locationField_' + id);
			input.style.width = '250px';
			input.style.height = '100%';
			input.style.margin = '0px';
			input.style.border = '1px solid #A9BBDF';
			input.style.borderRadius = '2px';
			input.setAttribute('id', 'locationInput_' + id);
			myself.map.controls[google.maps.ControlPosition.TOP_RIGHT].push(control);

			var ac = new google.maps.places.Autocomplete(input, { types: ['geocode'] });
			google.maps.event.addListener(ac, 'place_changed', function() {
				var place = ac.getPlace();
				if (place.geometry.viewport) {
					myself.map.fitBounds(place.geometry.viewport);
				} else {
					myself.map.setCenter(place.geometry.location);
					myself.map.setZoom(17);
				}
			});

			google.maps.event.addListener(myself.map, 'bounds_changed', function() {
				input.blur();
				input.value = '';
			});

			input.onkeyup = submitGeocode(input);
	},
	
	createLegend: function (id, legend) {
	
		var myself = this;
		
		// Set CSS styles for the DIV containing the control
		// Setting padding to 5 px will offset the control
		// from the edge of the map
		var controlDiv = document.createElement('DIV');
		controlDiv.style.padding = '5px';
		controlDiv.setAttribute('id', 'legendDiv_' + id);

		// Set CSS for the control border
		var controlUI = document.createElement('DIV');
		controlUI.setAttribute('id', 'legendUI_' + id);
		controlUI.style.backgroundColor = 'white';
		controlUI.style.borderStyle = 'solid';
		controlUI.style.borderWidth = '1px';
		controlUI.title = 'Legend';
		controlDiv.appendChild(controlUI);

		// Set CSS for the control text
		var controlText = document.createElement('DIV');
		controlText.setAttribute('id', 'legendText_' + id);
		controlText.style.fontFamily = 'Arial,sans-serif';
		controlText.style.fontSize = '12px';
		controlText.style.paddingLeft = '4px';
		controlText.style.paddingRight = '4px';

		var legendTable = "";
		var qtd = Object.keys(legend.ranges).length;
		for (var j = 0; j < qtd; j++) {
		
			if (isNaN(legend.ranges[j].min)) {
				legendTable += "<li><span style='background:" + legend.ranges[j].color + ";'><= " + legend.ranges[j].max + "</span>" + legend.ranges[j].desc + "</li>";
			} else if (isNaN(legend.ranges[j].max)) {
				legendTable += "<li><span style='background:" + legend.ranges[j].color + ";'>>= " + legend.ranges[j].min + "</span>" + legend.ranges[j].desc + "</li>";
			} else {
				//legendTable += "<li><span style='background:" + legend.ranges[j].color + ";'>" + legend.ranges[j].min + "-" + legend.ranges[j].max + "</span>" + legend.ranges[j].desc + "</li>";
				legendTable += "<li><span style='background:" + legend.ranges[j].color + ";'>" + legend.ranges[j].max + "</span>" + legend.ranges[j].desc + "</li>";
			}
		
        }
		  
		// Add the text
		controlText.innerHTML = "" +
		  	"<div class='my-legend'>" +
			"<div class='legend-title'>Legend</div>" +
			"<div class='legend-scale'>" +
			"  <ul class='legend-labels'>" +
			     legendTable +
			"  </ul>" +
			"</div>" +
			"<div class='legend-source'>" + legend.text + "</div>" +
			"</div>";

		controlUI.appendChild(controlText);
		
		myself.map.controls[google.maps.ControlPosition.RIGHT_BOTTOM].push(controlDiv);
	},
	
    showPopup: function(data, mapElement, popupHeight, popupWidth, contents, popupContentDiv, borderColor) {
        var overlay = new OurMapOverlay(mapElement.getPosition(), popupWidth, popupHeight, contents, popupContentDiv, this.map, borderColor);
        
		$(this.overlays).each(function (i, elt) {elt.setMap(null);});        
        this.overlays.push(overlay);        
    }

});
