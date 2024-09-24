/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

define([
  'cde/components/NewMapComponent/Map.featureStyles'
], function (Map) {

  xdescribe('Map.getStyle', function () {
    var style;
    beforeEach(function () {
      style = {
        fill: 'white',
        selected: {
          fill: 'red',
          normal: {
            'fill-opacity': 0.5
          },
          hover: {
            stroke: 'white'
          }
        },
        pan: {
          'stroke-width': 1,
          selected: {
            fill: 'red-pan',
            hover: {
              stroke: 'pan'
            }
          }
        },
        zoombox: {
          'stroke-width': 2
        },
        selected: {
          'stroke-width': 3
        }
      };
    });

    it('computes a basic style', function(){
      var computedStyle = Map.getStyle(style, 'pan', 'unselected', 'normal')
      expect(computedStyle.fill).toBe('white');
      expect(computedStyle.stroke).toBeUndefined();

    })

  });

});