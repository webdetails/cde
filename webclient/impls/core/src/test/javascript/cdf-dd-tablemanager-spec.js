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


describe("CDF-DD-TABLEMANAGER-TESTS", function() {
  var tableManager, tableModel,
      tableManagerId = "test-tableManager";

  beforeEach(function(done) {
    $('<div id="' + tableManagerId + '">').appendTo('body');

    tableManager = getTestTableManager();
    tableModel = getTestTableModel();
    tableModel.setData(_.extend([], exampleData_1));
    tableManager.setTableModel(tableModel);
    done();
  });

  afterEach(function(done) {
    $('#' + tableManagerId).remove();
    done();
  });

  it("correctly formats paths upon file picking",function() {
    var resourceFileRenderer = new ResourceFileRenderer(tableManager);

    var file1 = "myResource.css",
        file2 = "/resources/myResource.css",
        file3 = "/mySolution/resources/myResource.css";

    //solution dashboards
    // - new dashboard
    CDFDDFileName = "";
    expect(resourceFileRenderer.formatSelection(file1)).toBe("${solution:/myResource.css}");
    expect(resourceFileRenderer.formatSelection(file2)).toBe("${solution:/resources/myResource.css}");
    expect(resourceFileRenderer.formatSelection(file3)).toBe("${solution:/mySolution/resources/myResource.css}");

    // - editing a dashboard
    CDFDDFileName = "/mySolution/myDashboard.wcdf";
    expect(resourceFileRenderer.formatSelection(file1)).toBe("${solution:../myResource.css}");
    expect(resourceFileRenderer.formatSelection(file2)).toBe("${solution:../resources/myResource.css}");
    expect(resourceFileRenderer.formatSelection(file3)).toBe("${solution:resources/myResource.css}");

    //system dashboards
    // - new dashboard
    // this case is not applicable because cde new dashboard is only used in solution
    // system dashboards are created and always edited from a previously known file
    CDFDDFileName = "";
    expect(resourceFileRenderer.formatSelection(file1)).toBe("${solution:/myResource.css}");
    expect(resourceFileRenderer.formatSelection(file2)).toBe("${solution:/resources/myResource.css}");
    expect(resourceFileRenderer.formatSelection(file3)).toBe("${solution:/mySolution/resources/myResource.css}");

    // - editing a dashboard
    // system resources always have the plugin folder as root
    CDFDDFileName = "/system/myPlugin/mySolution/myDashboard.wcdf";
    expect(resourceFileRenderer.formatSelection(file1)).toBe("${system:../myResource.css}");
    expect(resourceFileRenderer.formatSelection(file2)).toBe("${system:../resources/myResource.css}");
    expect(resourceFileRenderer.formatSelection(file3)).toBe("${system:resources/myResource.css}");
  });

  it("correctly gets fileName before opening External Editor", function() {
    var resourceFileRenderer = new ResourceFileRenderer(tableManager);

    var file1 = "/myResource.css",
        file2 = "/resources/myResource.css",
        file3 = "/mySolution/resources/myResource.css";

    //solution dashboards
    // - new dashboard
    CDFDDFileName = "";
    expect(resourceFileRenderer.getFileName("${solution:/myResource.css}"))
        .toBe(file1);
    expect(resourceFileRenderer.getFileName("${solution:/resources/myResource.css}"))
        .toBe(file2);
    expect(resourceFileRenderer.getFileName("${solution:/mySolution/resources/myResource.css}"))
        .toBe(file3);

    // - editing a dashboard
    CDFDDFileName = "/mySolution/myDashboard.wcdf";
    expect(resourceFileRenderer.getFileName("${solution:/myResource.css}"))
        .toBe(file1);
    expect(resourceFileRenderer.getFileName("${solution:../myResource.css}"))
        .toBe(file1);
    expect(resourceFileRenderer.getFileName("${solution:../resources/myResource.css}"))
        .toBe(file2);
    expect(resourceFileRenderer.getFileName("${solution:resources/myResource.css}"))
        .toBe(file3);

    //system dashboards
    // - new dashboard
    // this case is not applicable because cde new dashboard is only used in solution
    // system dashboards are created and always edited from a previously known file
    CDFDDFileName = "";
    expect(resourceFileRenderer.getFileName("${solution:/myResource.css}"))
        .toBe(file1);
    expect(resourceFileRenderer.getFileName("${solution:/resources/myResource.css}"))
        .toBe(file2);
    expect(resourceFileRenderer.getFileName("${solution:/mySolution/resources/myResource.css}"))
        .toBe(file3);

    // - editing a dashboard
    // system resources always have the plugin folder as root
    CDFDDFileName = "/system/myPlugin/mySolution/myDashboard.wcdf";
    var systemCtx = "/system/myPlugin";
    expect(resourceFileRenderer.getFileName("${system:/myResource.css}"))
        .toBe(systemCtx + file1);
    expect(resourceFileRenderer.getFileName("${system:../myResource.css}"))
        .toBe(systemCtx + file1);
    expect(resourceFileRenderer.getFileName("${system:../resources/myResource.css}"))
        .toBe(systemCtx + file2);
    expect(resourceFileRenderer.getFileName("${system:resources/myResource.css}"))
        .toBe(systemCtx + file3);
  });

  it("test TableManager complete init", function() {
    var $table = $("#" + tableManagerId);
    $table.append('<div class="scrollContainer completeClass">');
    spyOn(tableManager, 'render');

    tableManager.init(false);
    var result = $table.html().replace(/>\s+/g, '>');
    var expected = '<div class="tableContainer">' +
        '<a id="anchor-test-tableManager" class="tableAnchor" href=""></a>' +
        '<div class="tableCaption ui-state-default">' +
        '<div class="simpleProperties propertiesSelected">Title</div>' +
        '</div>' +
        '<div class="scrollContainer">' +
        '<table id="table-test-tableManager" class="table-test-tableManager myTreeTable cdfdd ui-reset ui-clearfix ui-component ui-hover-state">'  +
        '<thead></thead><tbody class="ui-widget-content"></tbody></table>' +
        '</div></div>';

    expect(result).toEqual(expected);
    expect($('.scrollContainer').hasClass('completeClass')).toBe(false);
  });

  it("test TableManager partial init", function() {
    var $table = $("#" + tableManagerId);
    $table.append('<div class="scrollContainer partialClass">');
    spyOn(tableManager, 'render');

    tableManager.init(true);
    var result = $table.html().replace(/[>]\s+/g, '>');
    var expected = '<div class="scrollContainer partialClass">' +
        '<table id="table-test-tableManager" class="table-test-tableManager myTreeTable cdfdd ui-reset ui-clearfix ui-component ui-hover-state">'  +
        '<thead></thead><tbody class="ui-widget-content"></tbody></table>' +
        '</div>';

    expect(result).toEqual(expected);
    expect($('.scrollContainer').hasClass('partialClass')).toBe(true);
  });

  /*
   * Scroll Tests
   */
  describe("Testing Table Scroll #", function() {
    var table;
    beforeEach(function() {
      table = $('<div id="' + tableManagerId + '" style="height: 75px; width: 100px;">' +
      '  <div class="scrollContainer" style="max-height: 75px; overflow: scroll;">' +
      '    <table id="table-' + tableManagerId + '">' +
      '      <tbody>' +
      '        <tr id="freeForm" style="height: 30px;"><td>a</td></tr>' +
      '        <tr id="col" style="height: 30px;"><td>b</td></tr>' +
      '        <tr id="col-2" style="height: 30px;"><td>c</td></tr>' +
      '      </tobdy>' +
      '    </table>' +
      '  </div>' +
      '</div>');
      table.appendTo('body');
    });

    it("scrollTo - going down", function() {
      var $scroll = $('#' + tableManagerId + ' .scrollContainer');
      spyOn(tableModel, 'getEvaluatedId').and.callFake(function(idx) {
        return table.find('tr:eq(' + idx + ')').attr('id');
      });
      expect($scroll.scrollTop()).toEqual(0);
      tableManager.scrollTo(2);
      expect($scroll.scrollTop()).toEqual(30);
    });

    it("scrollTo - going up", function() {
      var $scroll = $('#' + tableManagerId + ' .scrollContainer');
      spyOn(tableModel, 'getEvaluatedId').and.callFake(function(idx) {
        return table.find('tr:eq(' + idx + ')').attr('id');
      });
      $scroll.scrollTop(30);
      expect($scroll.scrollTop()).toEqual(30);
      tableManager.scrollTo(0);
      expect($scroll.scrollTop()).toEqual(0);
    });
  });
  
  /*
   * Drag And Drop Tests
   */
  describe("Testing Drag And Drop #", function() {
    afterEach(function(done) {
      $('tr').remove();
      done();
    });

    it("addExtraHoverStyles", function() {
      var dragRowUp = $('<tr id="freeForm"></tr>');
      var dragRowDown = $('<tr id="col"></tr>');
      var hoverRow = $('<tr id="col-2" class="parent expanded"></tr>');
      var hoverChildRow = $('<tr id="html"></tr>').appendTo('body');

      spyOn(dragRowDown, 'position').and.returnValue({top: 5 });
      spyOn(hoverRow, 'position').and.returnValue({top: 10 });
      spyOn(dragRowUp, 'position').and.returnValue({top: 15 });

      tableManager.addExtraHoverStyles(dragRowDown, hoverRow);
      expect(hoverRow.hasClass('layout_hover_moveTo_down')).toBe(false);
      expect(hoverChildRow.hasClass('layout_hover_moveTo_down')).toBe(true);

      tableManager.addExtraHoverStyles(dragRowDown, hoverChildRow);
      expect(hoverChildRow.hasClass('layout_hover_moveTo_down')).toBe(true);

      tableManager.addExtraHoverStyles(dragRowUp, hoverRow);
      expect(hoverRow.hasClass('layout_hover_moveTo_up')).toBe(true);

      hoverRow.addClass('layout_hover_dropInto');
      expect(tableManager.addExtraHoverStyles(dragRowUp, hoverRow)).toBe(undefined);
    });

    it("removeExtraHoverStyles", function() {
      var hoverRow = $('<tr id="col-2" class="parent expanded layout_hover_moveTo_down"></tr>');
      var hoverChildRow = $('<tr id="html" class="layout_hover_moveTo_down"></tr>').appendTo('body');;

      tableManager.removeExtraHoverStyles(hoverRow);
      expect(hoverRow.hasClass('layout_hover_moveTo_down')).toBe(false);
      expect(hoverChildRow.hasClass('layout_hover_moveTo_down')).toBe(false);

      hoverChildRow.addClass('layout_hover_moveTo_down');
      tableManager.removeExtraHoverStyles(hoverChildRow);
      expect(hoverChildRow.hasClass('layout_hover_moveTo_down')).toBe(false);
    });

    it("disableDragObj", function() {
      var testDisableDragObj = function(testSet, expectedResult) {
        var L = testSet.length;
        for(var i = 0; i < L; i++) {
          expect(tableManager.disableDragObj({ type: testSet[i] })).toBe(expectedResult);
        }
      };

      testDisableDragObj(['LayoutResourceCode', 'LayoutResourceFile', 'BootstrapPanel', 'LayoutFreeForm',
        'LayoutRow', 'LayoutColumn', 'LayoutBootstrapColumn', 'LayoutSpace', 'LayoutImage', 'LayoutHtml'], false);

      testDisableDragObj(['BootstrapPanelHeader', 'BootstrapPanelBody', 'BootstrapPanelFooter',
        undefined, 'Label'], true);
    });

    it("isChildrenOfObj", function() {
      expect(tableManager.isChildrenOfObj('row','freeForm')).toBe(false);
      expect(tableManager.isChildrenOfObj('row','col')).toBe(true);
    });

    it("canMoveInto", function() {
      var testCanMoveInto = function(dragIds, dropId, expectedResult) {
        for (var i = 0, L = dragIds.length; i < L; i++) {
          expect(tableManager.canMoveInto(dragIds[i], dropId)).toBe(expectedResult);
        }
      }

      testCanMoveInto(['row', 'html', 'image', 'space', 'freeForm', 'bootstrapPanel'], 'freeForm', true);
      testCanMoveInto(['resource', 'col', 'col-2'], 'freeForm', false);

      testCanMoveInto(['col', 'col-2'], 'row', true);
      testCanMoveInto(['html', 'image', 'freeForm', 'bootstrapPanel', 'resource', 'row', 'space'], 'row', false);

      testCanMoveInto(['row', 'html', 'image', 'space', 'freeForm', 'bootstrapPanel'], 'col', true);
      testCanMoveInto(['resource', 'col', 'col-2'], 'col', false);

      testCanMoveInto(['resource', 'row', 'col', 'col-2', 'html', 'image', 'space', 'freeForm', 'bootstrapPanel'], 'bootstrapPanel', false);
      testCanMoveInto(['resource', 'row', 'col', 'col-2', 'html', 'image', 'space', 'freeForm', 'bootstrapPanel'], 'html', false);
      testCanMoveInto(['resource', 'row', 'col', 'col-2', 'html', 'image', 'space', 'freeForm', 'bootstrapPanel'], 'image', false);
      testCanMoveInto(['resource', 'row', 'col', 'col-2', 'html', 'image', 'space', 'freeForm', 'bootstrapPanel'], 'space', false);
      testCanMoveInto(['resource', 'row', 'col', 'col-2', 'html', 'image', 'space', 'freeForm', 'bootstrapPanel'], 'resource', false);

    });

    it("canMoveTo", function() {
      var testCanMoveTo = function(dragIds, dropId, expectedResult) {
        var L = dragIds.length;
        for (var i = 0; i < L; i++) {
          expect(tableManager.canMoveTo(dragIds[i], dropId)).toBe(expectedResult);
        }
      };

      testCanMoveTo(['resource', 'row', 'col', 'col-2', 'html', 'image', 'space', 'freeForm', 'bootstrapPanel'], 'freeForm', false);

      testCanMoveTo(['resource', 'col', 'col-2'], 'row', false);
      testCanMoveTo(['row', 'space', 'html', 'image', 'freeForm', 'bootstrapPanel'], 'row', true);

      testCanMoveTo(['resource', 'row', 'html', 'image', 'space', 'freeForm', 'bootstrapPanel'], 'col', false);
      testCanMoveTo(['col', 'col-2'], 'col', true);

      testCanMoveTo(['row', 'col', 'col-2', 'html', 'image', 'space', 'freeForm', 'bootstrapPanel'], 'bootstrapPanel', true);
      testCanMoveTo(['resource'], 'bootstrapPanel', false);
      testCanMoveTo(['row', 'col', 'col-2', 'html', 'image', 'space', 'freeForm', 'bootstrapPanel'], 'html', true);
      testCanMoveTo(['resource'], 'html', false);
      testCanMoveTo(['row', 'col', 'col-2', 'html', 'image', 'space', 'freeForm', 'bootstrapPanel'], 'image', true);
      testCanMoveTo(['resource'], 'image', false);
      testCanMoveTo(['row', 'col', 'col-2', 'space', 'freeForm', 'bootstrapPanel'], 'space', true);
      testCanMoveTo(['resource'], 'resource', true);
    });

    it("disableDrop", function() {
      expect(tableManager.disableDrop('row', 'col')).toBe(true);
      expect(tableManager.disableDrop('col', 'freeForm')).toBe(true);
      expect(tableManager.disableDrop('resource', 'col')).toBe(true);
      expect(tableManager.disableDrop('html', 'space')).toBe(true);
      expect(tableManager.disableDrop('row', 'freeForm')).toBe(false);
    })
  });

  /*
   * CellRenderer Tests
   */
  describe("Testing CellRenderers #", function() {

    /*
     * SelectRenderer
     */
    describe("Testing SelectRenderer #", function() {
      var selectRenderer;

      beforeEach(function() {
        selectRenderer = new SelectRenderer();
        selectRenderer.selectData = {
          'hello': 'World',
          'true': 'True',
          'false': 'False'
        };
        selectRenderer.processData();
      });

      it("# getActualValue", function() {
        expect(selectRenderer.getActualValue(null)).toEqual(null);
        expect(selectRenderer.getActualValue("")).toEqual("");
        expect(selectRenderer.getActualValue("World")).toEqual("hello");
        expect(selectRenderer.getActualValue("True")).toEqual("true");
        expect(selectRenderer.getActualValue("False")).toEqual("false");
      });
    });

    /*
     * ComponentToExportRenderer
     */
    describe("ComponentToExportRenderer #", function() {
      var mockComponent_1, mockComponent_2, mockComponent_3, cteRenderer;

      beforeEach(function() {
        mockComponent_1 = {
          meta_cdwSupport: "true",
          meta_cdwRender: "false",
          properties: [{value: "mockComponent_1"}]
        };

        mockComponent_2 = {
          meta_cdwSupport: "true",
          meta_cdwRender: "false",
          properties: [{value: ""}]
        };

        mockComponent_3 = {
          type: "ComponentsTable",
          properties: [{value: "mockComponent_3"}]
        };

        cdfdd.dashboardData.components = {
          rows: [mockComponent_1, mockComponent_2, mockComponent_3]
        };

        cteRenderer = new ComponentToExportRenderer();
      });

      afterEach(function() {
        mockComponent_1 = undefined;
        mockComponent_2 = undefined;
        cteRenderer = undefined;
        cdfdd.dashboardData.components = {};
      });


      it("getData", function() {
        expect(cteRenderer.getData()).toEqual({mockComponent_1: "mockComponent_1", mockComponent_3: "mockComponent_3"});
      });
    });

    /*
     * ChartComponentToExportRenderer
     */
    describe("ChartComponentToExportRenderer #", function() {
      var mockComponent_1, mockComponent_2, cctoRenderer;

      beforeEach(function() {
        mockComponent_1 = {
          meta_cdwSupport: "true",
          meta_cdwRender: "false",
          properties: [{value: "mockComponent_1"}]
        };

        mockComponent_2 = {
          meta_cdwSupport: "true",
          meta_cdwRender: "false",
          properties: [{value: "mockComponent_2"}]
        };

        cdfdd.dashboardData.components = {
          rows: [mockComponent_1, mockComponent_2]
        };

       cctoRenderer = new ChartComponentToExportRenderer();
      });

      afterEach(function() {
        mockComponent_1 = undefined;
        mockComponent_2 = undefined;
        cctoRenderer = undefined;
        cdfdd.dashboardData.components = {};
      });

      it("getData", function() {
        expect(cctoRenderer.getData()).toEqual({mockComponent_1: "mockComponent_1", mockComponent_2: "mockComponent_2"});
      });

      it("postChange", function() {
        expect(cctoRenderer.prevSelectedValue).toEqual("");
        cctoRenderer.postChange("mockComponent_1");
        expect(mockComponent_1.meta_cdwRender).toEqual("true");

        expect(cctoRenderer.prevSelectedValue).toEqual("mockComponent_1");
        cctoRenderer.postChange("");
        expect(mockComponent_1.meta_cdwRender).toEqual("false");
      });
    });

    describe("ChartExportTypeRenderer #", function() {
      var cetRenderer = new ChartExportTypeRenderer();
      it("getData", function() {
        expect(cetRenderer.getData()).toEqual({png: "png", svg: "svg"});
      });
    });

    describe("DataExportTypeRenderer #", function() {
      var detRenderer = new DataExportTypeRenderer();
      it("getData", function() {
        expect(detRenderer.getData()).toEqual({'xls': 'xls', 'csv': 'csv', 'xml': 'xml', 'json': 'json'});
      });
    });

    describe("Input validation #", function(){
      var ph;
      var phId = "test_validations_placeholder";

      beforeEach(function(){
        $("body").append('<div id="' + phId + '"></div>');
        ph = $("#" + phId);
      });

      afterEach(function() {
        ph.remove();
      });

      describe("StringRenderer #", function() {
        it("accepts all input", function() {
          var stringRenderer = new StringRenderer();
          var testValue = "testValue_.!#$%&/()=<tag></script>";
          var testValueEscaped = "testValue_.!#$%&amp;/()=&lt;tag&gt;&lt;/script&gt;";
          stringRenderer.render(ph, testValue, function() {});
          expect(stringRenderer.validate(testValue)).toEqual(true);
          expect(ph.find("td").html()).toEqual(testValueEscaped);
          expect(ph.find("td").text()).toEqual(testValue);
        });
      });

      describe("IdRenderer #", function() {
        it("validates input", function() {
          var idRenderer = new IdRenderer();
          expect(idRenderer.validate("validId1")).toEqual(true);
          expect(idRenderer.validate("valid_Id1")).toEqual(true);
          expect(idRenderer.validate("invalid.Id1")).toEqual(false);
        });
      });

      describe("IntegerRenderer #", function() {
        it("validates input", function() {
          var integerRenderer = new IntegerRenderer();
          expect(integerRenderer.validate("1")).toEqual(true);
          expect(integerRenderer.validate("100")).toEqual(true);
          expect(integerRenderer.validate("1.2")).toEqual(false);
          expect(integerRenderer.validate("NaN")).toEqual(false);
        });
      });

      describe("FloatRenderer #", function() {
        it("validates input", function() {
          var floatRenderer = new FloatRenderer();
          expect(floatRenderer.validate("1")).toEqual(true);
          expect(floatRenderer.validate("100")).toEqual(true);
          expect(floatRenderer.validate("1.2")).toEqual(true);
          expect(floatRenderer.validate("NaN")).toEqual(false);
        });
      });

      describe("SelectRenderer #", function() {
        it("validates input", function() {
          var selectRenderer = new SelectRenderer();
          selectRenderer.revertedSelectData = {
            'prop': 'value'
          }
          expect(selectRenderer.validate("prop")).toEqual(true);
          expect(selectRenderer.validate("PROP")).toEqual(true);
          selectRenderer.caseInsensitiveMatch = false;
          expect(selectRenderer.validate("prop")).toEqual(true);
          expect(selectRenderer.validate("PROP")).toEqual(false);
        });
      });

      describe("ResourceFileRenderer #", function() {
        it("escapes user input", function() {
          var tm = getTestTableManager();
          tm.setTableModel(getTestTableModel());
          var resourceFileRenderer = new ResourceFileRenderer(tm);
          var testValue = "testValue_.!#$%&/()=<tag></script>";
          var testValueEscaped = "testValue_.!#$%&amp;/()=&lt;tag&gt;&lt;/script&gt;";
          resourceFileRenderer.render(ph, testValue, function() {});
          expect(ph.find("td .cdfdd-resourceFileNameRender").html()).toEqual(testValueEscaped);
          expect(ph.find("td .cdfdd-resourceFileNameRender").text()).toEqual(testValue);
        });
      });
    });
  });
});
