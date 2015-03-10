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

describe("Table Operations #", function() {
  var tableManager = undefined;
  var tableModel = undefined;
  var indexManager = undefined;

  /*Mock tableManager for testing*/
  var initTableManager = function(spyObjName) {
    var spyOnMethods = ['getSelectedCell', 'selectCell', 'getDroppedOnId', 'setDroppedOnId', 'getTableModel',
      'getTableId', 'updateTreeTable', /*'canMoveInto',*/ 'addRow', 'insertAtIdx'];
    tableManager = jasmine.createSpyObj(spyObjName, spyOnMethods);

    tableManager.setDroppedOnId.and.callFake(function(id){ tableManager.droppedOnId = id; });
    tableManager.getDroppedOnId.and.callFake(function() { return tableManager.droppedOnId; });
    tableManager.selectCell.and.callFake(function(row,col){ tableManager.selectedCell = [row,col] });
    tableManager.getSelectedCell.and.callFake(function() { return tableManager.selectedCell; });
    tableManager.getTableModel.and.callFake(function() { return tableModel; });
    tableManager.addRow.and.callFake(function() { return; });
    tableManager.insertAtIdx.and.callFake(function(row, pos) { tableModel.getData().splice(pos,0,row); indexManager.updateIndex() });
  };

  /*Mock tableModel for testing*/
  var initTableModel = function(spyObjName) {
    var spyOnMethods = ['getEvaluatedId', 'getParentId', 'getRowType', 'getIndexManager', 'getData', 'setData'];
    tableModel = jasmine.createSpyObj(spyObjName, spyOnMethods);

    var rowType = function(row){return row.type};
    var parentId = function(row){ return row.parent; };
    tableModel.getIndexManager.and.callFake(function() { return indexManager; });
    tableModel.getEvaluatedId.and.callFake(function( rowNumber ) { return tableModel.getData()[rowNumber].id; });
    tableModel.getParentId.and.callFake(function() { return parentId; });
    tableModel.getRowType.and.callFake(function() { return rowType; });
    tableModel.getData.and.callFake(function() { return tableModel.data; });
    tableModel.setData.and.callFake(function(data) { tableModel.data = data; indexManager.updateIndex() });
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
    populate(tableModel, exampleData_2);
    done();
  });

  afterEach(function(done) {
    tableManager = undefined;
    tableModel = undefined;
    indexManager = undefined;
    done();
  });

  describe("LayoutAddResourceOperation", function() {
    var addResource = new LayoutAddResourceOperation();
    var content = '' +
        '<h2>Add Resource</h2>\n' +
        '<hr>Resource Type:&nbsp;&nbsp;\n' +
        '<select id="resourceType">\n' +
        ' <option value="Css">Css</option>\n' +
        ' <option value="Javascript">Javascript</option>\n' +
        '</select>\n' +
        '<select id="resourceSource">\n' +
        ' <option value="file">External File</option>\n' +
        ' <option value="code">Code Snippet</option>\n' +
        '</select>\n';

    var buttons = {
      Ok: true,
      Cancel: false
    };
    var prefix = "popup";

    it("#execute", function() {
      spyOn($, "prompt");

      addResource.execute(tableManager);

      expect($.prompt.calls.mostRecent().args[0]).toEqual(content);
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
