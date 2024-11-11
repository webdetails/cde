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
  "cdf/lib/jquery",
  "cdf/Dashboard.Clean",
  "cde/components/NewMapComponent/model/MapModel"
], function ($, Dashboard, MapModel) {

  describe("NewMapComponent/MapModel", function () {
    var model;
    beforeEach(function () {
      model = new MapModel({
        id: "root",
        nodes: [{
          id: "markers",
          nodes: [{
            id: "m1"
          }, {
            id: "m2"
          }]
        }]
      });
    });

    describe('correctly initializes', function () {
      it('in mode = "pan"', function () {
        expect(model.isPanningMode()).toBe(true);
      });
    });

    describe("correctly sets the mode to", function () {
      it('"pan"', function () {
        model.setPanningMode();
        expect(model.getMode()).toBe("pan");
        expect(model.isPanningMode()).toBe(true);
        expect(model.isZoomBoxMode()).toBe(false);
        expect(model.isSelectionMode()).toBe(false);
      });
      it('"zoombox"', function () {
        model.setZoomBoxMode();
        expect(model.getMode()).toBe("zoombox");
        expect(model.isPanningMode()).toBe(false);
        expect(model.isZoomBoxMode()).toBe(true);
        expect(model.isSelectionMode()).toBe(false);
      });
      it('"selection"', function () {
        model.setSelectionMode();
        expect(model.getMode()).toBe("selection");
        expect(model.isPanningMode()).toBe(false);
        expect(model.isZoomBoxMode()).toBe(false);
        expect(model.isSelectionMode()).toBe(true);
      });
    });
  });

  describe("NewMapComponent/MapModel.getStyle(): ", function () {
    var model, style;
    beforeEach(function () {
      var styleMaps = {
        global: {
          fill: "blue",
          normal: {
            stroke: "white",
            fill: "red"
          },
          selected: {
            fill: "yellow",
            "stroke-width": 1
          }
        },
        markers: {
          r: 10,
          unselected: {
            pan: {
              unselected: {
                normal: {
                  "stroke-width": 1
                },
                hover: {
                  "stroke-width": 2
                }
              }
            },
            "stroke-width": 10
          }
        },
        shapes: {
          "fill-opacity": 0.5,
          selected: {
            hover: {
              "fill-opacity": 0.9
            }
          },
          normal: {
            "z-index": 0
          },
          hover: {
            "z-index": 1,
            "fill-opacity": 0.7
          }
        }
      };
      model = new MapModel({
        id: "root",
        styleMap: styleMaps.global,
        nodes: [{
          id: "markers",
          styleMap: styleMaps.markers,
          nodes: [{
            id: "m1"
          }, {
            id: "m2",
            styleMap: {
              fill: "pink",
              zoombox: {
                fill: "brown"
              }
            }
          }]
        }]
      });
      model.setPanningMode();
      model.flatten().each(function (m) {
        m.setSelection(MapModel.SelectionStates.NONE);
        m.setHover(false);
      });
      style = function (id, mode, state, action) {
        var m = model.find(id);
        switch (mode) {
          case "pan":
            m.setPanningMode();
            break;
          case "zoombox":
            m.setZoomBoxMode();
            break;
          case "selection":
            m.setSelectionMode();
            break;
        }
        m.setSelection(state === "selected");
        m.setHover(action === "hover");
        return m.getStyle();
      };
    });

    function expectMatch(style, result) {
      for (var r in result) {
        expect(style[r]).toBe(result[r]);
      }
    }

    describe("(for a marker)", function () {
      it("computes the pan/unselected/normal style", function () {
        expectMatch(style("m1", "pan", "unselected", "normal"), {
          stroke: "white",
          fill: "red",
          r: 10,
          "stroke-width": 1
        });
        expectMatch(style("m2", "pan", "unselected", "normal"), {
          stroke: "white",
          fill: "pink",
          r: 10,
          "stroke-width": 1
        })
      });
      it("computes the pan/unselected/hover style", function () {
        expectMatch(style("m1", "pan", "unselected", "hover"), {
          stroke: undefined,
          fill: "blue",
          r: 10,
          "stroke-width": 2
        });
        expectMatch(style("m2", "pan", "unselected", "hover"), {
          stroke: undefined,
          fill: "pink",
          r: 10,
          "stroke-width": 2
        });
      });
      it("computes the pan/selected/normal style", function () {
        expectMatch(style("m1", "pan", "selected", "normal"), {
          stroke: "white",
          fill: "yellow",
          r: 10,
          "stroke-width": 1
        });
        expectMatch(style("m2", "pan", "selected", "normal"), {
          stroke: "white",
          fill: "pink",
          r: 10,
          "stroke-width": 1
        });
      });
      it("computes the pan/selected/hover style", function () {
        expectMatch(style("m1", "pan", "selected", "hover"), {
          stroke: undefined,
          fill: "yellow",
          r: 10,
          "stroke-width": 1
        });
        expectMatch(style("m2", "pan", "selected", "hover"), {
          stroke: undefined,
          fill: "pink",
          r: 10,
          "stroke-width": 1
        });
      });


      it("computes the zoombox/unselected/hover style", function () {
        expectMatch(style("m1", "zoombox", "unselected", "normal"), {
          stroke: "white",
          fill: "red",
          r: 10,
          "stroke-width": 10
        });
        expectMatch(style("m2", "zoombox", "unselected", "normal"), {
          stroke: "white",
          fill: "brown",
          r: 10,
          "stroke-width": 10
        });
      });
      it("computes the zoombox/unselected/hover style", function () {
        expectMatch(style("m1", "zoombox", "unselected", "hover"), {
          stroke: undefined,
          fill: "blue",
          r: 10,
          "stroke-width": 10
        });
        expectMatch(style("m2", "zoombox", "unselected", "hover"), {
          stroke: undefined,
          fill: "brown",
          r: 10,
          "stroke-width": 10
        });
      });
      it("computes the zoombox/selected/normal style", function () {
        expectMatch(style("m1", "zoombox", "selected", "normal"), {
          stroke: "white",
          fill: "yellow",
          r: 10,
          "stroke-width": 1
        });
        expectMatch(style("m2", "zoombox", "selected", "normal"), {
          stroke: "white",
          fill: "brown",
          r: 10,
          "stroke-width": 1
        });

      });
      it("computes the zoombox/selected/hover style", function () {
        expectMatch(style("m1", "zoombox", "selected", "hover"), {
          stroke: undefined,
          fill: "yellow",
          r: 10,
          "stroke-width": 1
        });
        expectMatch(style("m2", "zoombox", "selected", "hover"), {
          stroke: undefined,
          fill: "brown",
          r: 10,
          "stroke-width": 1
        });
      });
    });

  });
});