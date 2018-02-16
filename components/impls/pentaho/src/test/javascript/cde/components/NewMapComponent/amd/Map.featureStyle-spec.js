/*!
 * Copyright 2002 - 2018 Webdetails, a Hitachi Vantara company. All rights reserved.
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