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


describe("Table Operations #", function() {
  var tableManager, tableModel, indexManager;

  /*Mock tableManager for testing*/
  var initTableManager = function(spyObjName) {
    var spyOnMethods = ['getSelectedCell', 'selectCell', 'getDroppedOnId', 'setDroppedOnId', 'getTableModel',
      'getTableId', 'updateTreeTable', 'addRow', 'insertAtIdx'];
    tableManager = jasmine.createSpyObj(spyObjName, spyOnMethods);

    tableManager.setDroppedOnId.and.callFake(function(id) {
      tableManager.droppedOnId = id;
    });
    tableManager.getDroppedOnId.and.callFake(function() {
      return tableManager.droppedOnId;
    });
    tableManager.selectCell.and.callFake(function(row,col) {
      tableManager.selectedCell = [row,col];
    });
    tableManager.getSelectedCell.and.callFake(function() {
      return tableManager.selectedCell;
    });
    tableManager.getTableModel.and.callFake(function() {
      return tableModel;
    });
    tableManager.addRow.and.callFake(function() {});
    tableManager.insertAtIdx.and.callFake(function(row, pos) {
      tableModel.getData().splice(pos,0,row);
      indexManager.updateIndex();
    });
  };

  /*Mock tableModel for testing*/
  var initTableModel = function(spyObjName) {
    var spyOnMethods = ['getEvaluatedId', 'getParentId', 'getRowType', 'getIndexManager', 'getData', 'setData'];
    tableModel = jasmine.createSpyObj(spyObjName, spyOnMethods);

    var rowType = function(row) {
      return row.type;
    };
    var parentId = function(row) {
      return row.parent;
    };

    tableModel.getIndexManager.and.callFake(function() {
      return indexManager;
    });
    tableModel.getEvaluatedId.and.callFake(function(rowNumber) {
      return tableModel.getData()[rowNumber].id;
    });
    tableModel.getParentId.and.callFake(function() {
      return parentId;
    });
    tableModel.getRowType.and.callFake(function() {
      return rowType;
    });
    tableModel.getData.and.callFake(function() {
      return tableModel.data;
    });
    tableModel.setData.and.callFake(function(data) {
      tableModel.data = data;
      indexManager.updateIndex();
    });
  };

  var initIndexManager = function(tableModel) {
    indexManager = new IndexManager(tableModel);
  };

  var populate = function(tableModel, dataSet) {
    //Populate data for tableModel
    dataSet = _.extend([], dataSet);
    tableModel.setData(dataSet);
  };

  beforeEach(function(done) {
    initTableManager('TableManager');
    initTableModel('TableModel');
    initIndexManager(tableModel);
    done();
  });

  describe("Layout Panel #", function() {
    var layoutPanel;

    beforeEach(function(done) {
      layoutPanel = new LayoutPanel("test-layout-panel");
      layoutPanel.treeTable = tableManager;
      populate(tableManager.getTableModel(), exampleData_1);
      done();
    });

    it("# getHtmlTargets", function() {
      var expected = [
        { 'id': 'row', 'parent': "UnIqEiD", 'type': "LayoutRow",
            properties: [{ 'name': "name", 'value': "row" }] },
        { 'id': 'col', 'parent': "row", 'type': "LayoutColumn",
            properties: [{ 'name': "name", 'value': "col" }] },
        { 'id': 'col-2', 'parent': "row", 'type': "LayoutBootstrapColumn",
            properties: [{ 'name': "name", 'value': "col-2" }] },
        { 'id': 'freeForm', 'parent': "UnIqEiD", 'type': "LayoutFreeForm",
            properties: [{ 'name': "name", 'value': "freeForm" }] },
        { 'id': 'bootstrapPanelHeader', 'parent': "bootstrapPanel", 'type': "BootstrapPanelHeader",
            properties: [{ 'name': "name", 'value': "bootstrapPanelHeader" }] },
        { 'id': 'bootstrapPanelBody', 'parent': "bootstrapPanel", 'type': "BootstrapPanelBody",
            properties: [{ 'name': "name", 'value': "bootstrapPanelBody" }] },
        { 'id': 'bootstrapPanelFooter', 'parent': "bootstrapPanel", 'type': "BootstrapPanelFooter",
            properties: [{ 'name': "name", 'value': "bootstrapPanelFooter" }] }
      ];

      expect(layoutPanel.getHtmlTargets()).toEqual(expected);
    });

    it("# getHtmlObjects", function() {
      var expected = [
        { 'id': 'freeForm', 'parent': "UnIqEiD", 'type': "LayoutFreeForm",
          properties: [{ 'name': "name", 'value': "freeForm" }] },
        { 'id': 'bootstrapPanelHeader', 'parent': "bootstrapPanel", 'type': "BootstrapPanelHeader",
          properties: [{ 'name': "name", 'value': "bootstrapPanelHeader" }] },
        { 'id': 'bootstrapPanelBody', 'parent': "bootstrapPanel", 'type': "BootstrapPanelBody",
          properties: [{ 'name': "name", 'value': "bootstrapPanelBody" }] },
        { 'id': 'bootstrapPanelFooter', 'parent': "bootstrapPanel", 'type': "BootstrapPanelFooter",
          properties: [{ 'name': "name", 'value': "bootstrapPanelFooter" }] }
      ];

      expect(layoutPanel.getHtmlObjects()).toEqual(expected);
    });
  });

  describe("LayoutAddResourceOperation", function() {

    beforeEach(function() {
      populate(tableModel, exampleData_2);
    });

    var addResource = new LayoutAddResourceOperation();

    var content = '' +
        '<div class="clearfix">\n' +

        '  <div class="popup-input-container bottom">\n' +
        '    <span class="popup-label">Resource Type</span>\n' +
        '    <select id="resourceType" class="popup-select">\n' +
        '      <option value=""></option>\n' +
        '      <option value="Css">Css</option>\n' +
        '      <option value="Javascript">Javascript</option>\n' +
        '    </select>\n' +
        '  </div>\n' +

        '  <div class="popup-input-container bottom last">\n' +
        '    <span class="popup-label">Resource Source</span>\n' +
        '    <select id="resourceSource" class="popup-select">\n' +
        '      <option value=""></option>\n' +
        '      <option value="file">External File</option>\n' +
        '      <option value="code">Code Snippet</option>\n' +
        '    </select>\n' +
        '  </div>\n' +

        '</div>\n';

    var contentWrapper = '' +
        '<div class="popup-header-container">\n' +
        '  <div class="popup-title-container">Add Resource</div>\n' +
        '</div>\n' +
        '<div class="popup-body-container layout-popup resource-popup">\n' + content + '</div>';

    var buttons = {
      Ok: true,
      Cancel: false
    };
    var prefix = "popup";

    it("#execute", function() {
      spyOn($, "prompt");

      addResource.execute(tableManager);

      expect($.prompt.calls.mostRecent().args[0]).toEqual(contentWrapper);
      expect($.prompt.calls.mostRecent().args[1].buttons).toEqual(buttons);
      expect($.prompt.calls.mostRecent().args[1].prefix).toEqual(prefix);
      expect(typeof $.prompt.calls.mostRecent().args[1].submit).toEqual('function');
    });

    describe("# resourceSubmit", function() {
      var stubFile = { properties:  [
        {name: 'name', value: ''},
        {name: 'resourceType', value: 'Css'},
        {name: 'resourceFile', value: 'file'}
      ]};

      var stubCode = { properties:  [
        {name: 'name', value: ''},
        {name: 'resourceType', value: 'Css'},
        {name: 'resourceFile', value: 'code'}
      ]};

      $(content).appendTo('body');

      beforeEach(function(done) {
        spyOn(addResource, 'selectFirstProperty');
        spyOn(LayoutResourceFileModel, 'getStub').and.returnValue(stubFile);
        spyOn(LayoutResourceCodeModel, 'getStub').and.returnValue(stubCode);
        done();
      });

      afterEach(function(done) {
        done();
      });

      it("# 'OK' pressed, with 'External File' selected", function() {
        $('#resourceSource option[value="file"]').prop('selected', true);
        addResource.resourceSubmit(true, tableManager);

        expect(tableManager.insertAtIdx.calls.mostRecent().args[0]).toEqual(stubFile);
        expect(addResource.selectFirstProperty).toHaveBeenCalled();
      });


      it("# 'OK' pressed, with 'Code Snippet' selected", function() {
        $('#resourceSource option[value="code"]').prop('selected', true);
        addResource.resourceSubmit(true, tableManager);

        expect(tableManager.insertAtIdx.calls.mostRecent().args[0]).toEqual(stubCode);
        expect(addResource.selectFirstProperty).toHaveBeenCalled();
      });


      it("# 'Cancel' pressed", function() {
        addResource.resourceSubmit(false, tableManager);

        expect(tableManager.insertAtIdx).not.toHaveBeenCalled();
        expect(addResource.selectFirstProperty).not.toHaveBeenCalled();
      });
    });
  });
});
