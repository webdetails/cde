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
  "amd!cdf/lib/underscore"
], function($, _) {
  "use strict";
  /**
   * TileServices (servers providing png images representing the map)
   OpenStreetMaps default tiles are ugly, I found many nicer tilesets that work in both map engines (google/openlayers)
   To compare the various tilesets, visit http://mc.bbbike.org/mc/?num=2

   Example of valid values for the CDE property "tilesets"
   "mapquest"
   ["mapquest"]
   ["mapquest", "stamen"]
   "custom/static/localMapService/${z}/${x}/${y}.png"
   "http://otile1.mqcdn.com/tiles/1.0.0/map/${z}/${x}/${y}.png"
   "http://otile{switch:1,2,3,4}.mqcdn.com/tiles/1.0.0/map/${z}/${x}/${y}.png"
   */

  function mapObj(obj, callback) {
    return _.object(_.map(obj, function(value, key) {
      return [key, callback(value, key)];
    }));
  }

  var tileServices = $.extend({
      // list of tileset services that were tested and are working at 2013-11-04, see http://mc.bbbike.org/mc/?num=2 for comparison
      "google": {}, //"http://mt{switch:0,1,2,3}.googleapis.com/vt?x=${x}&y=${y}&z=${z}"
      "google-roadmap": {},
      "google-terrain": {},
      "google-satellite": {},
      "google-hybrid": {}
    },

    /*
    mapObj({
      "thunderforest-landscape": "https://${switch:a,b,c}.tile.thunderforest.com/landscape/${z}/${x}/${y}.png",
      "thunderforest-outdoors": "https://${switch:a,b,c}.tile.thunderforest.com/outdoors/${z}/${x}/${y}.png"
    }, function(url, key) {
      return {
        id: key,
        url: url,
        attribution: 'Map tiles by &copy;<a href="http://www.thunderforest.com">Thunderforest</a>, Data by &copy;<a href="http://openstreetmap.org/copyright">OpenStreetMap</a> Contributors',
        legaInfo: ["http://www.thunderforest.com/terms/"]
      };
    }),
    */

    mapObj({
      "openstreetmaps": "http://{switch:a,b,c}.tile.openstreetmap.org/${z}/${x}/${y}.png",
      "openstreemaps-bw": "http://{switch:a,b}.tiles.wmflabs.org/bw-mapnik/${z}/${x}/${y}.png"
    }, function(url, key) {
      return {
        id: key,
        url: url,
        attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreeMap</a> contributors',
        legaInfo: ["http://www.openstreetmap.org/copyright"]
      };
    }),

    mapObj({
      "nokia-normal": "http://maptile.maps.svc.ovi.com/maptiler/maptile/newest/normal.day/${z}/${x}/${y}/256/png8",
      "nokia-normal-grey": "http://maptile.maps.svc.ovi.com/maptiler/maptile/newest/normal.day.grey/${z}/${x}/${y}/256/png8",
      "nokia-normal-transit": "http://maptile.maps.svc.ovi.com/maptiler/maptile/newest/normal.day.transit/${z}/${x}/${y}/256/png8",
      "nokia-satellite": "http://maptile.maps.svc.ovi.com/maptiler/maptile/newest/satellite.day/${z}/${x}/${y}/256/png8",
      "nokia-terrain": "http://maptile.maps.svc.ovi.com/maptiler/maptile/newest/terrain.day/${z}/${x}/${y}/256/png8"
    }, function(url, key) {
      return {
        id: key,
        url: url,
        attribution: 'Map tiles by <a href="https://www.here.com/">HERE</a>',
        legalInfo: ["https://legal.here.com/en/terms/serviceterms/us/"]
      };
    }),

    mapObj({
      "mapquest": "http://otile{switch:1,2,3,4}.mqcdn.com/tiles/1.0.0/map/${z}/${x}/${y}.png", //MapQuest tile server
      "mapquest-normal": "http://otile{switch:1,2,3,4}.mqcdn.com/tiles/1.0.0/map/${z}/${x}/${y}.png", //MapQuest tile server
      "mapquest-hybrid": "http://otile{switch:1,2,3,4}.mqcdn.com/tiles/1.0.0/hyb/${z}/${x}/${y}.png", //MapQuest tile server
      "mapquest-sat": "http://otile{switch:1,2,3,4}.mqcdn.com/tiles/1.0.0/sat/${z}/${x}/${y}.jpg", //MapQuest tile server
    }, function(url, key) {
      return {
        id: key,
        url: url,
        attribution: 'Map tiles by <a href="http://stamen.com">Stamen Design</a>, under <a href="http://creativecommons.org/licenses/by/3.0">CC BY 3.0</a>. Data by <a href="http://openstreetmap.org">OpenStreetMap</a>, under <a href="http://www.openstreetmap.org/copyright">ODbL</a>.',
        legalInfo: ["http://maps.stamen.com/"]
      };
    }),

    mapObj({
      "stamen": "http://{switch:a,b,c,d}.tile.stamen.com/terrain/${z}/${x}/${y}.jpg",
      "stamen-terrain": "http://{switch:a,b,c,d}.tile.stamen.com/terrain/${z}/${x}/${y}.jpg",
      "stamen-terrain-background": "http://{switch:a,b,c,d}.tile.stamen.com/terrain-background/${z}/${x}/${y}.jpg",
      "stamen-terrain-labels": "http://{switch:a,b,c,d}.tile.stamen.com/terrain-labels/${z}/${x}/${y}.jpg",
      "stamen-toner": "http://{switch:a,b,c,d}.tile.stamen.com/toner/${z}/${x}/${y}.png",
      "stamen-toner-lite": "http://{switch:a,b,c,d}.tile.stamen.com/toner-lite/${z}/${x}/${y}.png",
      "stamen-toner-background": "http://{switch:a,b,c,d}.tile.stamen.com/toner-background/${z}/${x}/${y}.png",
      "stamen-toner-hybrid": "http://{switch:a,b,c,d}.tile.stamen.com/toner-hybrid/${z}/${x}/${y}.png",
      "stamen-toner-labels": "http://{switch:a,b,c,d}.tile.stamen.com/toner-labels/${z}/${x}/${y}.png",
      "stamen-toner-lines": "http://{switch:a,b,c,d}.tile.stamen.com/toner-lines/${z}/${x}/${y}.png",
      "stamen-toner-2010": "http://{switch:a,b,c,d}.tile.stamen.com/toner-2010/${z}/${x}/${y}.png",
      "stamen-toner-2011": "http://{switch:a,b,c,d}.tile.stamen.com/toner-2011/${z}/${x}/${y}.png",
      "stamen-watercolor": "http://{switch:a,b,c,d}.tile.stamen.com/watercolor/${z}/${x}/${y}.jpg"
    }, function(url, key) {
      return {
        id: key,
        url: url,
        attribution: 'Map tiles by <a href="http://stamen.com">Stamen Design</a>, under <a href="http://creativecommons.org/licenses/by/3.0">CC BY 3.0</a>. Data by <a href="http://openstreetmap.org">OpenStreetMap</a>, under <a href="http://www.openstreetmap.org/copyright">ODbL</a>.',
        legalInfo: ["http://maps.stamen.com/"]
      };
    }),

    mapObj({
     // "mapbox-terrain": "https://{switch:a,b,c,d}.tiles.mapbox.com/v3/examples.map-9ijuk24y/${z}/${x}/${y}.jpg",
      "mapbox-satellite": "https://{switch:a,b,c,d}.tiles.mapbox.com/v3/examples.map-qfyrx5r8/${z}/${x}/${y}.png",

      "mapbox-control-room": "https://{switch:a,b}.tiles.mapbox.com/v3/mapbox.control-room/${z}/${x}/${y}.png",

      "mapbox-blue-marble-jan": "https://{switch:a,b}.tiles.mapbox.com/v3/mapbox.blue-marble-topo-jan/${z}/${x}/${y}.png",
      "mapbox-blue-marble-jul": "https://{switch:a,b}.tiles.mapbox.com/v3/mapbox.blue-marble-topo-jul/${z}/${x}/${y}.png",
      "mapbox-blue-marble-jul-bw": "https://{switch:a,b}.tiles.mapbox.com/v3/mapbox.blue-marble-topo-jul-bw/${z}/${x}/${y}.png",

      "mapbox-natural-earth-1": "https://{switch:a,b}.tiles.mapbox.com/v3/mapbox.natural-earth-1/${z}/${x}/${y}.png",
      "mapbox-natural-earth-2": "https://{switch:a,b}.tiles.mapbox.com/v3/mapbox.natural-earth-2/${z}/${x}/${y}.png",
      "mapbox-natural-earth-hypso": "https://{switch:a,b}.tiles.mapbox.com/v3/mapbox.natural-earth-hypso/${z}/${x}/${y}.png",
      "mapbox-natural-earth-hypso-bathy": "https://{switch:a,b}.tiles.mapbox.com/v3/mapbox.natural-earth-hypso-bathy/${z}/${x}/${y}.png",

      "mapbox-oceans-white": "https://{switch:a,b}.tiles.mapbox.com/v3/mapbox.oceans-white/${z}/${x}/${y}.png",
      "mapbox-world-black": "https://{switch:a,b}.tiles.mapbox.com/v3/mapbox.world-black/${z}/${x}/${y}.png",
      "mapbox-world-blank-bright": "https://{switch:a,b}.tiles.mapbox.com/v3/mapbox.world-blank-bright/${z}/${x}/${y}.png",
      "mapbox-world-blank-light": "https://{switch:a,b}.tiles.mapbox.com/v3/mapbox.world-blank-light/${z}/${x}/${y}.png",
      "mapbox-world-blue": "https://{switch:a,b}.tiles.mapbox.com/v3/mapbox.world-blue/${z}/${x}/${y}.png",
      "mapbox-world-bright": "https://{switch:a,b}.tiles.mapbox.com/v3/mapbox.world-bright/${z}/${x}/${y}.png",
      "mapbox-world-dark": "https://{switch:a,b}.tiles.mapbox.com/v3/mapbox.world-dark/${z}/${x}/${y}.png",
      "mapbox-world-glass": "https://{switch:a,b}.tiles.mapbox.com/v3/mapbox.world-glass/${z}/${x}/${y}.png",
      "mapbox-world-light": "https://{switch:a,b,c,d}.tiles.mapbox.com/v3/mapbox.world-light/${z}/${x}/${y}.png",
      "mapbox-world-print": "https://{switch:a,b}.tiles.mapbox.com/v3/mapbox.world-print/${z}/${x}/${y}.png"
      //"mapbox-world-light4": "http://api.mapbox.com/v4/mapbox.light/${z}/${x}/${y}.png",
      //"mapbox-example": "https://{switch:a,b,c,d}.tiles.mapbox.com/v3/examples.c7d2024a/${z}/${x}/${y}.png",
      //"mapbox-example2": "https://{switch:a,b,c,d}.tiles.mapbox.com/v3/examples.bc17bb2a/${z}/${x}/${y}.png"
    }, function(url, key) {
      return {
        id: key,
        url: url,
        attribution: 'Map tiles by <a href="http://mapbox.com/about/maps/">MapBox</a> &mdash; Data by &copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>',
        legalInfo: ["http://mapbox.com/about/maps/", "http://www.openstreetmap.org/copyright"]
      };
    }),

    mapObj({
      "openmapsurfer": "http://129.206.74.245:8001/tms_r.ashx?x=${x}&y=${y}&z=${z}",
      "openmapsurfer-roads": "http://129.206.74.245:8001/tms_r.ashx?x=${x}&y=${y}&z=${z}",
      "openmapsurfer-roads-grayscale": "http://129.206.74.245:8008/tms_rg.ashx?x=${x}&y=${y}&z=${z}",
      "openmapsurfer-semitransparent": "http://129.206.74.245:8003/tms_h.ashx?x=${x}&y=${y}&z=${z}",
      "openmapsurfer-hillshade": "http://129.206.74.245:8004/tms_hs.ashx?x=${x}&y=${y}&z=${z}",
      "openmapsurfer-contour": "http://129.206.74.245:8006/tms_il.ashx?x=${x}&y=${y}&z=${z}",
      "openmapsurfer-administrative": "http://129.206.74.245:8007/tms_b.ashx?x=${x}&y=${y}&z=${z}"
    }, function(url, key) {
      return {
        id: key,
        url: url,
        attribution: 'Map tiles by <a href="http://korona.geog.uni-heidelberg.de/contact.html">OpenMapSurfer</a>',
        legalInfo: ["http://korona.geog.uni-heidelberg.de/contact.html"]
      };
    }),

    mapObj({
      "cartodb-positron": "http://{switch:a,b,c,d}.basemaps.cartocdn.com/light_all/${z}/${x}/${y}.png",
      "cartodb-positron-nolabels": "http://{switch:a,b,c,d}.basemaps.cartocdn.com/light_nolabels/${z}/${x}/${y}.png",
      "cartodb-darkmatter": "http://{switch:a,b,c,d}.basemaps.cartocdn.com/dark_all/${z}/${x}/${y}.png",
      "cartodb-darkmatter-nolabels": "http://{switch:a,b,c,d}.basemaps.cartocdn.com/dark_nolabels/${z}/${x}/${y}.png"
    }, function(url, key) {
      return {
        id: key,
        url: url,
        attribution: 'Map tiles by <a href="http://cartodb.com/attributions#basemaps">CartoDB</a>, under <a href="https://creativecommons.org/licenses/by/3.0/" target="_blank">CC BY 3.0</a>. Data by <a href="http://www.openstreetmap.org/" target="_blank">OpenStreetMap</a>, under ODbL.',
        legalInfo: ["https://cartodb.com/basemaps/"]
      };
    }),

    mapObj({
        "esri-street": {
          url: "http://services.arcgisonline.com/ArcGIS/rest/services/World_street_Map/MapServer/tile/${z}/${y}/${x}",
          attribution: 'Map tiles by &copy; <a href="http://services.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer">Esri</a>'
        },
        "esri-ocean-basemap": {
          url: "http://services.arcgisonline.com/ArcGIS/rest/services/Ocean_Basemap/MapServer/tile/${z}/${y}/${x}",
          attribution: 'Map tiles by &copy; <a href="http://services.arcgisonline.com/ArcGIS/rest/services/Ocean_Basemap/MapServer">Esri</a>'
        },
        "esri-natgeo": {
          url: "http://services.arcgisonline.com/ArcGIS/rest/services/NatGeo_World_Map/MapServer/tile/${z}/${y}/${x}",
          attribution: 'Map tiles by &copy; <a href="http://services.arcgisonline.com/ArcGIS/rest/services/NatGeo_World_Map/MapServer">Esri</a>'
        },
        "esri-world": {
          url: "http://services.arcgisonline.com/ArcGIS/rest/services/World_Topo_Map/MapServer/tile/${z}/${y}/${x}",
          attribution: 'Map tiles by &copy; <a href="http://services.arcgisonline.com/ArcGIS/rest/services/World_Topo_Map/MapServer">Esri</a>'
        },
        "esri-lightgray": {
          url: "http://services.arcgisonline.com/ArcGIS/rest/services/Canvas/World_Light_Gray_Base/MapServer/tile/${z}/${y}/${x}",
          attribution: 'Map tiles by &copy; <a href="http://services.arcgisonline.com/ArcGIS/rest/services/Canvas/World_Light_Gray_Base/MapServer">Esri</a>'
        },
        "esri-delorme": {
          url: "http://services.arcgisonline.com/ArcGIS/rest/services/Specialty/DeLorme_World_Base_Map/MapServer/tile/${z}/${y}/${x}",
          attribution: 'Map tiles by &copy; <a href="http://services.arcgisonline.com/ArcGIS/rest/services/Specialty/DeLorme_World_Base_Map/MapServer">Esri</a>'
        }
      }, function(obj, key) {
        return $.extend(obj, {
          id: key,
          legalInfo: ["http://www.esri.com/legal/software-license", "http://downloads2.esri.com/ArcGISOnline/docs/tou_summary.pdf"]
        });
      }
    )
  );

  tileServices["default"] = tileServices["openstreetmaps"];
  return {
    tileServices: tileServices,
    otherTileServices: [
      // These are tilesets using special code
      //"google"
    ],
    tileServicesOptions: {
      // WIP: interface for overriding defaults
      "apple": {minZoom: 3, maxZoom: 14}
    }
  };

});
