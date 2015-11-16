define([
  'cde/components/Map/Map.featureStyles'
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