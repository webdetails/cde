/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


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