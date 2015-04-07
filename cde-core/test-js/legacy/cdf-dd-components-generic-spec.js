/*!
 * Copyright 2002 - 2015 Webdetails, a Pentaho company.  All rights reserved.
 *
 * This software was developed by Webdetails and is provided under the terms
 * of the Mozilla Public License, Version 2.0, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to  http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */

describe("CDF-DD-COMPONENTS-GENERIC-TESTS", function() {
  var tableManager = new TableManager('test-tableManager');

  describe("Testing ValuesArrayRenderer #", function() {
    var valueAr = new ValuesArrayRenderer(tableManager);

    it("# Object Initial Values", function() {
      expect(valueAr.multiDimensionArray).toBe(true);
      expect(valueAr.cssPrefix).toEqual('StringList');
      expect(valueAr.hasTypedValues).toBe(false);
      expect(valueAr.argTitle).toEqual('Arg');
      expect(valueAr.valTitle).toEqual('Value');
      expect(valueAr.typesArray).toEqual([]);
      expect(valueAr.selectData).toEqual({});
    });

    it("# addParameter", function() {
      spyOn(valueAr, 'addTypedParameters');
      spyOn(valueAr, 'addParameters');

      var values1 = ["arg","",""];
      var values2 = ["arg", "value"];
      var values3 = ["arg", null];
      var values4 = ["arg", undefined];

      var index = 0;
      var container = undefined;
      valueAr.addParameter(index, values1, container);
      expect(valueAr.addParameters).toHaveBeenCalledWith(index, values1[0], values1[1], container);
      valueAr.addParameter(index, values2, container);
      expect(valueAr.addParameters).toHaveBeenCalledWith(index, values2[0], values2[1], container);
      valueAr.addParameter(index, values3, container);
      expect(valueAr.addParameters).toHaveBeenCalledWith(index, values3[0], 'null', container);
      valueAr.addParameter(index, values4, container);
      expect(valueAr.addParameters).toHaveBeenCalledWith(index, values4[0], '', container);

      expect(valueAr.addTypedParameters).not.toHaveBeenCalled();
    });

    it("# getParameterValues", function() {
      //it is a multi dimensional array without typed values
      expect( valueAr.getParameterValues(0).length ).toEqual(2)
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
      expect(ctar.getData()).toEqual({string: 'string', numeric: 'numeric'});

      ctar.selectData = { preData: 'value' };
      expect(ctar.getData()).toEqual({ preData: 'value', string: 'string', numeric: 'numeric' });
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
      var title = "myTitle",
          value = "Hello World!",
          cssClass = "myCssClass",
          id = "myId";

      expect( evar.getValueDiv(title, value, cssClass, id) ).toBe(
          "<div class='myCssClass'><span class='StringListTextLabel'>myTitle</span>"
          + "<div id='myId' class='StringListValueDiv' title='<pre>Hello World!</pre>'>Hello World!</div></div>\n");
      expect( evar.getValueDiv(null, value, cssClass, id) ).toBe(
          "<div class='myCssClass'><div id='myId' class='StringListValueDiv' title='<pre>Hello World!</pre>'>"
          + "Hello World!</div></div>\n");
      expect( evar.getValueDiv(title, "", cssClass, id) ).toBe(
          "<div class='myCssClass'><span class='StringListTextLabel'>myTitle</span>"
          + "<div id='myId' class='StringListValueDiv' title=''></div></div>\n");
    });

    it("# escapeOutPutValue", function() {
      expect(evar.escapeOutputValue(undefined)).toBe(undefined);
      expect(evar.escapeOutputValue(null)).toBe(null);
      expect(evar.escapeOutputValue("Hello World!")).toBe("Hello World!");
      expect(evar.escapeOutputValue("'Hello' \"World\"!")).toBe("&#39;Hello&#39; &quot;World&quot;!");
    });
  });
});