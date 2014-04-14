/*
 This Source Code Form is subject to the
 terms of the Mozilla Public License, v. 2.0.
 If a copy of the MPL was not distributed
 with this file, You can obtain one at
 http://mozilla.org/MPL/2.0/.
 */

/*

Contributions by Carlos Russo from Webdetails.pt

* TODO Consider using .kml files directly, see https://code.google.com/p/geoxml3/
* TODO Attempt merging with NewMapComponent
* TODO Attempt using API of https://github.com/mapstraction/mxn/


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

var gMapsOverlayComponent = UnmanagedComponent.extend({

    mapEngineOpts: undefined, //override this in preExec
    colormap: [[0, 102, 0, 255], [255, 255 ,0,255], [255, 0,0, 255]], //RGBA
	
	// Get color based on a Range
	getColorLegend: function (value, legendRanges) {
		var qtd = Object.keys(legendRanges.ranges).length;
		for (var j = 0; j < qtd; j++) {
			if ((isNaN(legendRanges.ranges[j].min) && value <= legendRanges.ranges[j].max) ||
				(isNaN(legendRanges.ranges[j].max) && value >= legendRanges.ranges[j].min) ||
				(value >= legendRanges.ranges[j].min && value <= legendRanges.ranges[j].max)) {
				return legendRanges.ranges[j].color;
			}
		}
	},

	// Get color based on Continuous Color Map
    getColorMap: function() {

        var interpolate = function(a, b, n){
            var c = [], d=[];
            var k, kk, step;
            for (k=0; k<a.length; k++){
                c[k] = [];
                for (kk=0, step = (b[k]-a[k])/n; kk<n; kk++){
                    c[k][kk] = a[k] + kk*step;
                }
            }
            for (k=0; k<c[0].length; k++){
                d[k] = [];
                for (kk=0; kk<c.length; kk++){
                    d[k][kk] =  Math.round(c[kk][k]);
                }
            }
            return d;
        };
		
        var cmap = [];
		
        for (k=1; k<this.colormap.length; k++)
        {
            cmap = cmap.concat(interpolate(this.colormap[k-1], this.colormap[k], 512));
        }
		
        return _.map( cmap, function (v) {
            return 'rgba('+ v.join(',') +')';
        });
    },
	
    _getMapDefinition : function(myself, callback){

		if (!!myself.mapName & !myself.mapDefinition){
			$.getJSON(wd.helpers.repository.getRsourceUrl()+wd.helpers.repository.getBaseSolutionPluginRoot()+
					"cde/components/gmapsoverlay/map-def/" + myself.mapName + ".js", function(json, callback) {
				if (json)  {
					myself.mapDefinition = json;
				}
			});
        }
        //Dashboards.log('mapDefinition :' + _.keys(this.mapDefinition));
		callback(myself);
    },
	
    postProcessData: function (values, myself){
	
        /*
		* do a postProcessing, something like a postPostFetch
        */

		var defaultFillOpacity = 0.6;
		var defaultStrokeWeight = 0.5;
		
        myself.queryResult = {};
		myself.isContinuousMapColor = $.isEmptyObject(myself.legend);
		var nrCols = values.metadata.length;
		
		for (i in values.resultset) {
			var item = values.resultset[i];
			
			var value;
			if (nrCols < 3) // DataSet onde a legenda é baseada no valor
			{
				value = parseFloat(item[1]);
				color = "";
				myself.isColorDefinedInDS = false;
			}
			else // DataSet com a 3a coluna informando qual cor utilizar, nao utilizar Legenda nem Mapa de Cor continuo para definir cor da regiao.
			{
				value = item[1];
				color = item[2];
				myself.isColorDefinedInDS = true;
			}
			
			myself.queryResult[item[0]]  = {
					'value': value,
					'color': color
			};
			
			if (item.length > 2){
				myself.queryResult[item[0]].payload = item.slice(2);
			}
		}

		myself._parseLegend(myself.isContinuousMapColor);
		
        // patch queryResult with color information
		if (myself.isContinuousMapColor) 
		{
			var colormap = myself.getColorMap();
			var qvalues = _.map(myself.queryResult, function (q) { return q.value; });
			var minValue = _.min(qvalues), maxValue = _.max(qvalues);
			var n = colormap.length;
			_.each(myself.queryResult, function (v, key) {
				var level =  (v.value-minValue) / (maxValue - minValue);
				myself.queryResult[key] = _.extend({
					level: level,
					fillColor: colormap[Math.floor( level * (n-1)) ],
					fillOpacity: defaultFillOpacity,
					strokeWeight: defaultStrokeWeight
				},  myself.queryResult[key]);
			});
		} 
		else 
		{
			_.each(myself.queryResult, function (v, key) {
			
				var color;
				if (myself.isColorDefinedInDS) {
					color = v.color;
				}
				else
				{
					color = myself.getColorLegend(v.value, myself.legendRanges);
				}

				myself.queryResult[key] = _.extend({
					fillColor: color,
					fillOpacity: defaultFillOpacity,
					strokeWeight: defaultStrokeWeight
				},  myself.queryResult[key]);
			});
		}

    },
	
    _parseLegend : function (isContinuousMapColor){
	
        this.legendRanges = new Object;
		
		this.legendRanges.ranges = new Object;
		this.legendRanges.text = ((!this.legendText) ? "" : this.legendText);
		this.legendRanges.source = ((!this.sourceText) ? " " : this.sourceText);

		if (!isContinuousMapColor) {
			for (var i = 0; i < this.legend.length; i++) {
				var opts = this.legend[i][1].split(";");
				this.legendRanges.ranges[i] = new Object;
				this.legendRanges.ranges[i].min = parseFloat(opts[0]);
				this.legendRanges.ranges[i].max = parseFloat(opts[1]);
				this.legendRanges.ranges[i].color = opts[2];
				this.legendRanges.ranges[i].desc = this.legend[i][0];
			}
		}
    },
	
    update : function() {

		myself = this;

		if ($.isEmptyObject(myself.queryDefinition))  {  
			Dashboards.error("GMaps - Datasource not defined.");
			return
		}
		
		if (!myself.mapName)  {  
			Dashboards.error("GMaps - Map Name not defined.");
			return
		}
		
		if (!myself.mapHeight || !myself.mapWidth) {
			Dashboards.error("GMaps - Map Height and/or Width not defined.");
			return
		}
		
        // first get the map definition (asynchronously), and then launch triggerQuery (asynchronously)
        myself._getMapDefinition(myself, function (myself) {
			myself.triggerQuery(myself.queryDefinition, function(values) {

				myself.postProcessData(values, myself);
				
				// Start Google Map stuff
				myself._initialize();

			});
        });
    },

    _initialize: function() {
		this.mapEngine = new GMapEngine();
        this.mapEngine.opts = $.extend(true, this.mapEngine.opts, this.mapEngineOpts);
        if (this.clickCallback){
            this.mapEngine.clickCallback = this.clickCallback;
        }
		this.mapEngine.init(this);
    },

    draw: function() {
		var myself = this;
		this.ph = $("#" + this.htmlObject);
		this.ph.empty();

		this.mapEngine.createMap(this.ph[0], this.centerLongitude, this.centerLatitude, this.defaultZoomLevel, this.mapHeight, this.mapWidth);
		this.mapEngine.renderMap(this.mapDefinition, this.queryResult, ((!this.defaultColor) ? "#EAEAEA" : this.defaultColor), myself.legendRanges);
		this.mapEngine.resetButton(this.ph[0].id, this.defaultZoomLevel, this.centerLongitude, this.centerLatitude);

		if (this.search == true) {
			this.mapEngine.searchBox(this.ph[0].id);
		}
        this.mapEngine.renderLegend(this.ph[0].id, this.mapDefinition, this.queryResult, this.getColorMap(), [0, 0.5, 1], myself.legendRanges, myself.isContinuousMapColor, myself.isColorDefinedInDS);

    }

});

var GMapEngine = Base.extend({
    map: undefined,
    opts: {
        mapOptions: {
            styles:[
                {
                    featureType: "administrative",
                    stylers: [ { "visibility": "off" } ]
                }
            ],
			disableDefaultUI: false,
			mapTypeControl: false,
			streetViewControl: false
        }
    },
    opened_info: undefined,
    centered: false,
    overlays: [],
    init: function(mapComponent) {

        $.when( loadGoogleMapsOverlay() ).then (
            function (status) {
				mapComponent.draw();
			}
		);
    },

    createMap: function(target, centerLongitude, centerLatitude, defaultZoomLevel, mapHeight, mapWidth) {
        // see possible features on https://developers.google.com/maps/documentation/javascript/reference#MapTypeStyleFeatureType
		var mapOptions = $.extend(true, {
			zoom: parseInt(defaultZoomLevel),
			center: new google.maps.LatLng(centerLatitude, centerLongitude), 
			mapTypeId: google.maps.MapTypeId.TERRAIN
		}, this.opts.mapOptions);

        this.map = new google.maps.Map(target, mapOptions);
		this.opened_info = new google.maps.InfoWindow();
		
		$(target).css("height", mapHeight + "px");
		$(target).css("width", mapWidth + "px");

    },

    renderMap: function(mapDefinition, queryResult, defaultColor, legend) {
	
        if (!mapDefinition){
            return;
        }
		var myself = this;

		for (var c in mapDefinition) {
			var coods = mapDefinition[c],
				polyPath = [];
				
			for (var k = 0; k < coods.length; k++) {
				polyPath.push(new google.maps.LatLng( coods[k][0], coods[k][1]) );
			}

			var shapeinfo = {
				fillColor:  !!queryResult[c] ? queryResult[c].fillColor: defaultColor,
				fillOpacity: !!queryResult[c] ? queryResult[c].fillOpacity : 0,
				strokeWeight: !!queryResult[c] ? queryResult[c].strokeWeight : 0,
				strokeColor: '#8c8c8c'
			};

			var shape = new google.maps.Polygon(_.extend({
				paths : polyPath
			}, shapeinfo));


			var shapeValue = queryResult[c] ? queryResult[c].value : null;

			shape.infowindow = new google.maps.InfoWindow({
				content: myself.tooltipMessage(c, shapeValue),
				pixelOffset: { width: 0, height:-3 }
			});
			
			shape.infowindow.dataPayload = _.extend({
				name: c,
				value: shapeValue,
				level: queryResult[c] ? queryResult[c].level : 0
			}, shapeinfo);

			if (!!queryResult[c] ) {
				queryResult[c].shape = shape;
			}

			shape.setMap(myself.map);
			google.maps.event.addListener(shape, 'click', function (event) {
				myself.clickCallback(this.infowindow, event);
				myself.displayCoordinates(event.latLng);
			});
			
			google.maps.event.addListener(shape, 'click',function (event) {
				this.fillOpacity=1;
				this.strokeColor= "#000000";
				this.setVisible(false);
				this.setVisible(true);
				this.infowindow.setOptions({ maxWidth: 500});
				this.infowindow.setPosition(event.latLng);
				if (!this.infowindow.getMap())
					this.infowindow.open(myself.map);
				myself.opened_info = this.infowindow;

			});

			/*
			google.maps.event.addListener(shape, 'mousemove',function (event) {
				this.strokeColor= "#000000";
				this.setVisible(false);
				this.setVisible(true);
			});
			*/
			
			google.maps.event.addListener(shape, 'mouseout', function (event) {
				//this.strokeWeight=0.5;
				myself.opened_info.close();
				this.fillOpacity=0.6;
				this.strokeColor= "#8c8c8c";
				this.setVisible(false);
				this.setVisible(true);
			});
			
		}
    },
	
    tooltipMessage : function (shapeName, shapeValue) {
		var message = shapeName + "</br>" + (shapeValue ? shapeValue : '-');
        return '<div class="gmapsoverlay-tooltip">' + message + '</div>';
    },
	
	// Apenas para debug de codigo
    clickCallback : function (shape, event){
        //Override this
        Dashboards.log(shape.dataPayload.name + ':' + shape.dataPayload.value + ':' + shape.dataPayload.level*100 + '%');
    },
	
	// Apenas para debug, mostra Lat/Lng no console
	displayCoordinates: function(pnt) {
		var lat = pnt.lat();
		lat = lat.toFixed(4);
		var lng = pnt.lng();
		lng = lng.toFixed(4);
		Dashboards.log("Lat: " + lat + "  Lng: " + lng);
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
		linkReset.className = 'gmapsoverlay-button';
		linkReset.onclick = (function () {	myself.map.setZoom(zoom);
							myself.map.setCenter(new google.maps.LatLng(centerLatitude, centerLongitude));
						 });
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
	
    renderLegend: function (id, mapDefinition, queryResult, colormap, ticks, legendRanges, isContinuousMapColor, isColorDefinedInDS) {
		/*
        var engNotation = function(d) {
            var Log1000 = Math.log(1000);
            var engLabels = ['', ' k' , ' M', ' G' , ' T', ' P'];
            var exponent3 = ( d == 0 ? 0 :
                              Math.floor(Math.round( 100 * Math.log(d)/Log1000 )/100) );
            var mantissa = Math.round( 100* d / Math.pow(1000, exponent3))/100;
            return mantissa + engLabels[exponent3];
        };
		*/

		if (isContinuousMapColor)
		{
			var sigFigs = function (num, sig) {
				if (num == 0)
					return 0;
				if (Math.round(num) == num)
					return num;
				var digits = Math.round((-Math.log(Math.abs(num)) / Math.LN10) + (sig || 2));
				//round to significant digits (sig)
				if (digits < 0)
					digits = 0;
				return num.toFixed(digits);
			};


			if (queryResult && mapDefinition ) {
				var values = _.map(queryResult, function (q) { return q.value; });
				var minValue = _.min(values),
					maxValue = _.max(values);
				var n = colormap.length;
				var rounding=1;
				if (maxValue < -5){
					rounding = ((maxValue -minValue)/5).toString().split('.');
					rounding = rounding.length > 1 ? Math.pow(10, Math.max(rounding[1].length, 3)): 1;
					}
				var legend = _.map(ticks, function (level) {
					var value = (minValue + level * (maxValue - minValue)*rounding)/rounding;
					return {
						value: sigFigs(value,1),
						level: level,
						fillColor: colormap[Math.floor( level* n -1)]
					};
				});
			}

			this.legend = legend;
		}

		// Set CSS styles for the DIV containing the control
		// Setting padding to 5 px will offset the control
		// from the edge of the map
		var controlDiv = document.createElement('DIV');
		controlDiv.style.padding = '5px';
		controlDiv.setAttribute('id', 'legendDiv_' + id);

		// Set CSS for the control border
		var controlUI = document.createElement('DIV');
		controlUI.setAttribute('id', 'legendUI_' + id);
		//controlUI.style.backgroundColor = 'white';
		//controlUI.style.borderStyle = 'solid';
		//controlUI.style.borderWidth = '1px';
		controlUI.title = 'Legend';
		controlDiv.appendChild(controlUI);

		// Set CSS for the control text
		var controlText = document.createElement('DIV');
		controlText.setAttribute('id', 'legendText_' + id);
		controlText.style.fontFamily = 'Arial,sans-serif';
		controlText.style.fontSize = '12px';
		controlText.style.paddingLeft = '4px';
		controlText.style.paddingRight = '4px';

		if (isContinuousMapColor)
		{
			var legendTable = '';
			_.each(legend, function(el){
				var left = (el.level != 0) ? el.level*100 + '%' : '-1px';
				legendTable += "<div class='gmapsoverlay-legend-label' style='left:"+ left+ ";position:absolute;'><div>"+ el.value + "</div></div>";
			});
			
			// Add the text
			controlText.innerHTML = "" +
			"<div class='gmapsoverlay-legend'>" +
			"  <div class='gmapsoverlay-legend-title'>" + legendRanges.text + "</div>" +
			"  <div class='gmapsoverlay-legend-scale'>" +
			"    <div class='gmapsoverlay-legend-labels'>" +
					legendTable +
			"    </div>" +
			"  </div>" +
			"<div class='gmapsoverlay-legend-source'>" + legendRanges.source + "</div>" +
			"</div>";

		}
		else
		{
			var legendTable = "";
			var qtd = Object.keys(legendRanges.ranges).length;
			for (var j = 0; j < qtd; j++) {
			
				if (isColorDefinedInDS) 
				{
					legendTable += "<li><span style='background:" + legendRanges.ranges[j].color + ";'></span>" + legendRanges.ranges[j].desc + "</li>";
				} 
				else if (isNaN(legendRanges.ranges[j].min)) 
				{
					legendTable += "<li><span style='background:" + legendRanges.ranges[j].color + ";'><= " + legendRanges.ranges[j].max + "</span>" + legendRanges.ranges[j].desc + "</li>";
				} 
				else if (isNaN(legendRanges.ranges[j].max)) 
				{
					legendTable += "<li><span style='background:" + legendRanges.ranges[j].color + ";'>>= " + legendRanges.ranges[j].min + "</span>" + legendRanges.ranges[j].desc + "</li>";
				} 
				else 
				{
					//legendTable += "<li><span style='background:" + legendRanges.ranges[j].color + ";'>" + legendRanges.ranges[j].min + "-" + legendRanges.ranges[j].max + "</span>" + legendRanges.ranges[j].desc + "</li>";
					legendTable += "<li><span style='background:" + legendRanges.ranges[j].color + ";'>" + legendRanges.ranges[j].max + "</span>" + legendRanges.ranges[j].desc + "</li>";
				}
			}
		
			// Add the text
			controlText.innerHTML = "" +
		  	"<div class='gmapsoverlay-legend' style='width: auto'>" +
			"<div class='gmapsoverlay-legend-title'>" + legendRanges.text + "</div>" +
			"<div class='gmapsoverlay-legend-scale-range'>" +
			"  <ul class='gmapsoverlay-legend-labels-range'>" +
			     legendTable +
			"  </ul>" +
			"</div>" +
			"<div class='gmapsoverlay-legend-source'>" + legendRanges.source + "</div>" +
			"</div>";

		}

		controlUI.appendChild(controlText);
		this.map.controls[google.maps.ControlPosition.BOTTOM_CENTER].push(controlDiv);
		
    },

    showPopup: function(data, mapElement, popupHeight, popupWidth, contents, popupContentDiv, borderColor) {
        var overlay = new OurMapOverlay(mapElement.getPosition(), popupWidth, popupHeight, contents, popupContentDiv, this.map, borderColor);

		$(this.overlays).each(function (i, elt) {elt.setMap(null);});
        this.overlays.push(overlay);
    }

});
