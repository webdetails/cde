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

describe("CDF-DD-TABLEMANAGER-TESTS", function() {
  var tableManager = getTestTableManager();
  var tableModel = getTestTableModel();
  tableModel.setData(_.extend([], exampleData_1));
  tableManager.setTableModel(tableModel);

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
    var tableManager = new TableManager("test-tableManager");
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

  describe("Testing Table Scroll #", function() {
    var table;
    beforeEach(function() {
      table = $('<div id="test-tableManager" style="height: 75px; width: 100px;">' +
      '  <div class="scrollContainer" style="max-height: 75px; overflow: scroll;">' +
      '    <table id="table-test-tableManager">' +
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

    afterEach(function() {
      $('#test-tableManager').remove();
    });

    it("scrollTo - going down", function() {
      var $scroll = $('#test-tableManager .scrollContainer');
      spyOn(tableModel, 'getEvaluatedId').and.callFake(function(idx) {
        return table.find('tr:eq(' + idx + ')').attr('id');
      });
      expect($scroll.scrollTop()).toEqual(0);
      tableManager.scrollTo(2);
      expect($scroll.scrollTop()).toEqual(30);
    });

    it("scrollTo - going up", function() {
      var $scroll = $('#test-tableManager .scrollContainer');
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
      })
    });

    describe("ChartExportTypeRenderer #", function() {
      var cetRenderer = new ChartExportTypeRenderer();
      it("getData", function() {
        expect(cetRenderer.getData()).toEqual({png: "png", svg: "svg"});
      })
    });

    describe("DataExportTypeRenderer #", function() {
      var detRenderer = new DataExportTypeRenderer();
      it("getData", function() {
        expect(detRenderer.getData()).toEqual({'xls': 'xls', 'csv': 'csv', 'xml': 'xml', 'json': 'json'});
      })
    });
  });
});
