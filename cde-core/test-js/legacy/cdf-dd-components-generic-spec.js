/*!
 * Copyright 2002 - 2015 Webdetails, a Pentaho company. All rights reserved.
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

describe("CDF-DD-COMPONENTS-GENERIC-TESTS", function() {
  var tableManager = new TableManager('test-tableManager');

  describe("Testing ValuesArrayRenderer #", function() {
    var valueAr, arrayRenderer;

    beforeEach(function() {
      valueAr = new ValuesArrayRenderer(tableManager);
      arrayRenderer = new ArrayRenderer(tableManager);
    });

    it("# Object Initial Values", function() {
      expect(valueAr.multiDimensionArray).toBe(true);
      expect(valueAr.cssPrefix).toEqual('StringList');
      expect(valueAr.hasTypedValues).toBe(false);
      expect(valueAr.argTitle).toEqual('Arg');
      expect(valueAr.valTitle).toEqual('Value');
      expect(valueAr.typesArray).toEqual([]);
      expect(valueAr.selectData).toEqual({});
    });

    it("# addPopupRow", function() {
      spyOn(valueAr, 'buildTypedRow');
      spyOn(valueAr, 'buildSingleDimensionRow');
      spyOn(valueAr, 'buildMultiDimensionRow');

      var values1 = ["arg", ""];
      var values2 = ["arg", "value"];
      var values3 = ["arg", null];
      var values4 = ["arg", undefined];
      var values5 = ["key", "value", "default"];

      var index = 0;
      var container = $('<div>'); //undefined;
      valueAr.addPopupRow(index, values1, container);
      expect(valueAr.buildMultiDimensionRow).toHaveBeenCalledWith(index, values1[0], values1[1]);
      valueAr.addPopupRow(index, values2, container);
      expect(valueAr.buildMultiDimensionRow).toHaveBeenCalledWith(index, values2[0], values2[1]);
      valueAr.addPopupRow(index, values3, container);
      expect(valueAr.buildMultiDimensionRow).toHaveBeenCalledWith(index, values3[0], 'null');
      valueAr.addPopupRow(index, values4, container);
      expect(valueAr.buildMultiDimensionRow).toHaveBeenCalledWith(index, values4[0], '');
      valueAr.addPopupRow(index, values5, container);
      expect(valueAr.buildMultiDimensionRow).toHaveBeenCalledWith(index, values5[0], values5[1], values5[2]);

      expect(valueAr.buildTypedRow).not.toHaveBeenCalled();
      expect(valueAr.buildSingleDimensionRow).not.toHaveBeenCalled();
    });

    it("# getParameterValues", function() {
      //it is a multi dimensional array without typed values
      expect( valueAr.getRowValues(0).length ).toEqual(2)
    });

    it("# getInitialValue", function() {
      expect(valueAr.getInitialValue("[]")).toEqual([["", "", "", "", ""]]);
      expect(valueAr.getInitialValue("[\"arg1\", \"arg2\"]")).toEqual(["arg1", "arg2"]);
      expect(valueAr.getInitialValue("[[\"arg1\", \"value1\"]]")).toEqual([["arg1", "value1"]]);

      expect(arrayRenderer.getInitialValue("[]")).toEqual([""]);
      expect(arrayRenderer.getInitialValue("[\"arg1\"]")).toEqual(["arg1"]);

    });

    it("# isParameterEmpty", function() {
      expect(valueAr.isParameterEmpty(undefined)).toBe(true);
      expect(valueAr.isParameterEmpty(null)).toBe(true);
      expect(valueAr.isParameterEmpty([])).toBe(true);
      expect(valueAr.isParameterEmpty("")).toBe(true);
      expect(valueAr.isParameterEmpty([undefined, undefined])).toBe(true);
      expect(valueAr.isParameterEmpty(["", ""])).toBe(true);

      expect(valueAr.isParameterEmpty("arg1")).toBe(false);
      expect(valueAr.isParameterEmpty(["arg1", ""])).toBe(false);
      expect(valueAr.isParameterEmpty(["", "value1"])).toBe(false);
      expect(valueAr.isParameterEmpty(["arg1", "value1"])).toBe(false);
    });
  });

  describe("Testing ColTypesArrayRender #", function() {
    var ctar = new ColTypesArrayRender(tableManager);

    it("# Object Initial Values", function() {
      expect(ctar.multiDimensionArray).toBe(false);
      expect(ctar.cssPrefix).toEqual('StringArray');
      expect(ctar.hasTypedValues).toBe(false);
      expect(ctar.argTitle).toEqual('Arg');
      expect(ctar.valTitle).toEqual('Value');
      expect(ctar.typesArray).toEqual([]);
      expect(ctar.selectData).toEqual({});
    });

    it("# getData", function() {
      ctar.selectData = {};
      expect(ctar.getData()).toEqual({string: 'string', numeric: 'numeric', hidden: 'hidden'});

      ctar.selectData = { preData: 'value' };
      expect(ctar.getData()).toEqual({preData: 'value', string: 'string', numeric: 'numeric', hidden: 'hidden'});
    });
  });

  describe("Testing EditorValuesArrayRenderer #", function() {
    var evar = new EditorValuesArrayRenderer(tableManager);
    evar.cssClass = "css";
    evar.cssPrefix = "StringList";

    it("# getFormatedValue",function() {
      var shortValue = "Hello World!",
          longValue = "This      must      be      very      long!"; //must be more than 40 characters long
      expect( evar.getFormattedValue(shortValue) ).toBe(shortValue);
      expect( evar.getFormattedValue(longValue) ).toBe("This      must      be      ve (...)");
    });

    it("# getValueDiv", function() {
      var index = "0",
          value = "Hello World!",
          // using jquery to escape html
          tooltip = $("<a>").text("<pre>Hello World!</pre>").html();

      expect( evar.getValueSection(index, value) ).toBe(
          '<div class="popup-value-container">' +
          '  <div id="val_0" class="popup-text-div" title="' + tooltip + '" placeholder="Click to edit...">' + value + '</div>' +
          '</div>');
    });

    it("# escapeOutPutValue", function() {
      expect(evar.escapeOutputValue(undefined)).toBe(undefined);
      expect(evar.escapeOutputValue(null)).toBe(null);
      expect(evar.escapeOutputValue("Hello World!")).toBe("Hello World!");
      expect(evar.escapeOutputValue("'Hello' \"World\"!")).toBe("&#39;Hello&#39; &quot;World&quot;!");
    });
  });

  describe("Testing CdaParametersRenderer #", function() {
    var cdaPR;

    beforeEach(function() {
      $('<div id="test_container"></div>').appendTo('body');
      cdaPR = new CdaParametersRenderer(tableManager);
    });

    afterEach(function() {
      $('#test_container').remove();
      cdaPR = undefined;
    });

    it("# Object Initial Values", function() {
      expect(cdaPR.multiDimensionArray).toBe(true);
      expect(cdaPR.hasTypedValues).toBe(true);
      expect(cdaPR.cssPrefix).toEqual('ParameterList');

      expect(cdaPR.argTitle).toEqual('Name');
      expect(cdaPR.valTitle).toEqual('Value');
      expect(cdaPR.argPlaceholderText).toEqual('Insert Text...');
      expect(cdaPR.valPlaceHolderText).toEqual('Insert Text...');

      expect(cdaPR.typesArray).toEqual(['String', 'Integer', 'Numeric', 'Date', 'StringArray', 'IntegerArray', 'NumericArray', 'DateArray']);
      expect(cdaPR.patternUnlockTypes).toEqual(['Date', 'DateArray']);
      expect(cdaPR.selectData).toEqual({});
    });

    it("# Test getRowValues", function() {
      var dummyRow = $('<div id="dummy_row">')
          .append('<input id="arg_0" type="text" value="paramName"/>')
          .append('<input id="val_0" type="text" value="paramValue"/>')
          .append('<select id="type_0"><option selected>paramType</option>')
          .append('<input id="pattern_0" type="text" value="paramPattern"/>')
          .append('<input id="access_0" type="checkbox" checked/>');

      dummyRow.appendTo("#test_container");

      var expectedReturn = ["paramName", "paramValue", "paramType", "private", "paramPattern"];
      var actualReturn = cdaPR.getRowValues(0);

      expect(actualReturn.length).toEqual(5);
      expect(actualReturn).toEqual(expectedReturn);
    });
  });
});