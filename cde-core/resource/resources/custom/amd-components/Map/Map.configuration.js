define([
  'cdf/lib/jquery',
  'amd!cdf/lib/underscore',
  './Map.ext'
], function ($, _, MapExt) {

  return {
    getConfiguration: getConfiguration
  };

  function imgUrl(image, fallback) {
    var imgList = _.isString(image) ? [image] : image;

    return _.map(imgList, function(img){
      return "url(" + MapExt.getMarkerImgPath() + image + ")";
    }).join(', ') + fallback;
  }

  /**
   * Validates the configuration options and gathers them by context
   * @returns {{addIns: {MarkerImage: *, ShapeResolver: *, LocationResolver: *}}}
   */
  function getConfiguration() {
    var addIns = {
      MarkerImage: {
        name: this.markerCggGraph ? 'cggMarker' : this.markerImageGetter,
        options: {
          cggScript: this.markerCggGraph,
          parameters: this.cggGraphParameters,
          height: this.markerHeight,
          width: this.markerWidth,
          iconUrl: this.marker
        }
      },
      ShapeResolver: {
        name: this.shapeResolver,
        options: {
          url: this.shapeSource,
          parseShapeKey: this.parseShapeKey
        }
      },
      LocationResolver: {
        name: this.locationResolver || 'openstreetmap',
        options: {}
      },
      MapEngine: {
        name: this.mapEngineType,
        options: {
          rawOptions: {
            map: {}
          },
          style: {
            pan: {
              cursors: {
                dragging: undefined,
                draggable: undefined
              }
            },
            zoombox: {
              cursors: {
                dragging: 'crosshair',
                draggable: imgUrl(['zoom-cursor.svg', 'zoom-cursor.png','zoom-cursor.cur'], 'crosshair')
              }
            },
            selection: {
              cursors: {
                dragging: 'crosshair',
                draggable: 'crosshair'
              }
            }
          },
          tileServices: this.tileServices,
          tileServicesOptions: this.tileServicesOptions,
          tilesets: (_.isString(this.tilesets)) ? [this.tilesets] : this.tilesets,
          API_KEY: this.API_KEY || window.API_KEY
        }
      }
    };

    //
    var controls = {
      enableKeyboardNavigation: true,
      enableZoomOnMouseWheel: false
    };

    var viewport = {
      center: {
        latitude: parseFloat(this.centerLatitude),
        longitude: parseFloat(this.centerLongitude)
      },
      zoomLevel: {
        min: 0,
        max: Infinity,
        default: this.defaultZoomLevel
      }

    };

    return $.extend(true, {}, {
      isSelector: !_.isEmpty(this.parameter),
      addIns: addIns,
      controls: controls,
      styleMap: this.styleMap,
      viewport: viewport
    }, _.result(this, 'options'));
  }

});