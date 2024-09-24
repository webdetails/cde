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
  'cdf/lib/jquery',
  'cdf/Dashboard.Clean',
  'cde/components/NewMapComponent/ControlPanel/ControlPanel',
  'cde/components/NewMapComponent/model/MapModel'
], function ($, Dashboard, ControlPanel, MapModel) {

  describe('NewMapComponent/ControlPanel', function () {
    var cp, $div, model;
    beforeEach(function () {
      $div = $('<div />').appendTo($('body'));
      model = new MapModel({
        id: 'root',
        nodes: [{
          id: 'markers',
          nodes: [{
            id: 'm1'
          }, {
            id: 'm2'
          }]
        }]
      });
      cp = new ControlPanel($div, model, {
        isSelector: true
      });
      cp.render();
    });

    afterEach(function () {
      $div.remove();
    });

    describe('correctly switches the mode to', function () {
      it('"pan"', function () {
        model.setZoomBoxMode();
        $div.find('.map-control-pan').click();
        expect(model.isPanningMode()).toBe(true);
        expect(model.isZoomBoxMode()).toBe(false);
        expect(model.isSelectionMode()).toBe(false);
      });
      it('"zoombox"', function () {
        model.setPanningMode();
        $div.find('.map-control-zoombox').click();
        expect(model.isPanningMode()).toBe(false);
        expect(model.isZoomBoxMode()).toBe(true);
        expect(model.isSelectionMode()).toBe(false);
      });
      it('"selection"', function () {
        model.setPanningMode();
        $div.find('.map-control-select').click();
        expect(model.isPanningMode()).toBe(false);
        expect(model.isZoomBoxMode()).toBe(false);
        expect(model.isSelectionMode()).toBe(true);
      });
    });

  });

});