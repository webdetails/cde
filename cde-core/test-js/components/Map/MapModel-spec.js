define([
  'cdf/lib/jquery',
  'cdf/Dashboard.Clean',
  'cde/components/Map/model/MapModel'
], function ($, Dashboard, MapModel) {

  describe('NewMapComponent/MapModel', function () {
    var model;
    beforeEach(function () {
      model = new MapModel({
        id: 'root',
        nodes: [{
         id: 'markers',
          nodes: [{
            id: 'm1'
          },{
            id: 'm2'
          }]
        }]
      });
    });

    describe('correctly initializes', function(){
      it('in mode = "pan"', function(){
        expect(model.isPanningMode()).toBe(true);
      });

      it('with the ', function(){

      });

    });

    describe('correctly sets the mode to', function () {
      it('"pan"', function () {
        model.setPanningMode();
        expect(model.getMode()).toBe('pan');
        expect(model.isPanningMode()).toBe(true);
        expect(model.isZoomBoxMode()).toBe(false);
        expect(model.isSelectionMode()).toBe(false);
      });
      it('"zoombox"', function () {
        model.setZoomBoxMode();
        expect(model.getMode()).toBe('zoombox');
        expect(model.isPanningMode()).toBe(false);
        expect(model.isZoomBoxMode()).toBe(true);
        expect(model.isSelectionMode()).toBe(false);
      });
      it('"selection"', function () {
        model.setSelectionMode();
        expect(model.getMode()).toBe('selection');
        expect(model.isPanningMode()).toBe(false);
        expect(model.isZoomBoxMode()).toBe(false);
        expect(model.isSelectionMode()).toBe(true);
      });
    });


  });

});