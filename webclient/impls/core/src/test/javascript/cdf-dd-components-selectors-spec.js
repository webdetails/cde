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


describe("CDF-DD-COMPONENTS-SELECTORS-TESTS", function() {
  var tableManager = getTestTableManager();

  describe("HtmlObjectRenderer #", function() {

    var htmlObjectRenderer;
    var htmlObjectData = {
      "id": "id"
    };

    beforeEach(function() {
      htmlObjectRenderer = new HtmlObjectRenderer(tableManager);
      htmlObjectRenderer.getData = function() {
        return htmlObjectData;
      }
    });

    var verifyExpectation = function(value) {
      expect(!!value).toBe(true);
      expect(value.lastIndexOf("${h:")).toEqual(0);
      expect(value.lastIndexOf("}")).toEqual(value.length - 1);
    };

    it("Always saves values with ${h:} syntax", function() {
      // simulating editing the text input
      htmlObjectRenderer.getEditableCallback(verifyExpectation)("id");
      htmlObjectRenderer.getEditableCallback(verifyExpectation)("notInData");
    });

    it("Won't double encode", function() {
      // simulating editing the text input
      htmlObjectRenderer.getEditableCallback(verifyExpectation)("${h:id}");
      htmlObjectRenderer.getEditableCallback(verifyExpectation)("${h:notInData}");
    });

    it("Uses unencoded value as the label", function() {
      expect(htmlObjectRenderer.getLabel(htmlObjectData, "id")).toEqual("id");
      expect(htmlObjectRenderer.getLabel(htmlObjectData, "${h:id}")).toEqual("id");
      expect(htmlObjectRenderer.getLabel(htmlObjectData, "${h:notInData}")).toEqual("notInData");
    });
  });

  describe("ListenersRenderer #", function() {

      var renderer;

      beforeEach(function() {
        renderer = new ListenersRenderer(tableManager);
      });

      describe("getData()", function() {

        beforeEach(function() {
          var createProperty = function(val) {
            return {
              properties: [ {value: val} ]
            };
          };

          var data = [ createProperty('a'), createProperty('b') ];
          var producer = {
            getParameters: function() { return data; }
          };
          spyOn(Panel, 'getPanel').and.returnValue(producer);
        });

        var getDataReturnsWellFormedObjectTester = function(rendererValue) {
          renderer.value = rendererValue;

          var string = renderer.getData();
          var json = JSON.parse(string);
          expect(json.a).toEqual('a');
          expect(json.b).toEqual('b');
          expect(json.selected).toEqual(['plain', 'parameter']);
        }

        it("should return well-formed JSON object for double-quoted renderer values", function() {
          getDataReturnsWellFormedObjectTester('["plain", "${p:parameter}"]');
        });

        it("should return well-formed JSON object for single-quoted renderer values", function() {
          getDataReturnsWellFormedObjectTester("['plain', '${p:parameter}']");
        });

      });
    });
});