describe("CDF-DD-COMPONENTS-GENERIC-TESTS", function() {

  describe("Testing EditorValuesArrayRenderer #", function() {
    var tableManager = new TableManager("test-tableManager");
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