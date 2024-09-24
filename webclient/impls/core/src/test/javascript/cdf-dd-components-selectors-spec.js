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