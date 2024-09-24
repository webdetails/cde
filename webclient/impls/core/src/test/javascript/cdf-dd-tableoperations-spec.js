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

describe("Table Operations #", function() {

  var tableManager = undefined;
  var tableModel = undefined;
  var indexManager = undefined;
  var canMoveInto = false;

  /*Mock tableManager for testing*/
  var initTableManager = function(spyObjName) {
    var spyOnMethods = ['getSelectedCell', 'selectCell', 'getDroppedOnId', 'setDroppedOnId', 'getTableModel',
                        'getTableId', 'updateTreeTable', 'canMoveInto', 'addRow', 'insertAtIdx'];
    tableManager = jasmine.createSpyObj(spyObjName, spyOnMethods);

    tableManager.setDroppedOnId.and.callFake(function(id){ tableManager.droppedOnId = id; });
    tableManager.getDroppedOnId.and.callFake(function() { return tableManager.droppedOnId; });
    tableManager.selectCell.and.callFake(function(row,col){ tableManager.selectedCell = [row,col] });
    tableManager.getSelectedCell.and.callFake(function() { return tableManager.selectedCell; });
    tableManager.getTableModel.and.callFake(function() { return tableModel; });
    tableManager.canMoveInto.and.callFake(function(drag, drop){ return canMoveInto; });
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
    //indexManager.setIndex(rowIndex);
  };

  var populate = function(tableModel, dataSet) {
    //Populate data for tableModel
    dataSet = _.extend([], dataSet);
    tableModel.setData(dataSet);
  };

  //Helpers
  var getRowProperties = function( rowIndex ) { return tableModel.getData()[rowIndex].properties; };
  var setCanMoveInto = function( value ) { canMoveInto = value; };

  beforeEach(function(done){
    initTableManager('TableManager');
    initTableModel('TableModel');
    initIndexManager(tableModel);
    populate(tableModel, exampleData_2);
    done();
  });

  afterEach(function(done){
    tableManager = undefined;
    tableModel = undefined;
    indexManager = undefined;
    setCanMoveInto( false );
    done();
  });

  /**
   * ## Table Operations # Duplicate Operation
   */
  it("Duplicate Operation - Row without children", function() {
    var duplicateOp = new DuplicateOperation();
    tableManager.selectCell(4,0);
    duplicateOp.execute(tableManager);

    expect(tableManager.getSelectedCell()[0] + "," + tableManager.getSelectedCell()[1]).toBe("4,0");
    expect(getRowProperties(0)[0].value).toBe('first');
    expect(getRowProperties(1)[0].value).toBe('first-child');
    expect(getRowProperties(2)[0].value).toBe('first-child-2');
    expect(getRowProperties(3)[0].value).toBe('first-child-2-child');
    expect(getRowProperties(4)[0].value).toBe('second_new');
    expect(getRowProperties(5)[0].value).toBe('second');
  });

  it("Duplicate Operation - Row with children", function() {
    var duplicateOp = new DuplicateOperation();
    tableManager.selectCell(0,0);
    duplicateOp.execute(tableManager);

    expect(tableManager.getSelectedCell()[0] + "," + tableManager.getSelectedCell()[1]).toBe("0,0");
    expect(getRowProperties(0)[0].value).toBe('first_new');
    expect(getRowProperties(1)[0].value).toBe('first-child_new');
    expect(getRowProperties(2)[0].value).toBe('first-child-2_new');
    expect(getRowProperties(3)[0].value).toBe('first-child-2-child_new');
    expect(getRowProperties(4)[0].value).toBe('first');
    expect(getRowProperties(5)[0].value).toBe('first-child');
    expect(getRowProperties(6)[0].value).toBe('first-child-2');
    expect(getRowProperties(7)[0].value).toBe('first-child-2-child');
    expect(getRowProperties(8)[0].value).toBe('second');
  });

  it("Duplicate Operation - Row with children and child of other row", function() {
    var duplicateOp = new DuplicateOperation();
    tableManager.selectCell(2,0);
    duplicateOp.execute(tableManager);

    expect(tableManager.getSelectedCell()[0] + "," + tableManager.getSelectedCell()[1]).toBe("2,0");
    expect(getRowProperties(0)[0].value).toBe('first');
    expect(getRowProperties(1)[0].value).toBe('first-child');
    expect(getRowProperties(2)[0].value).toBe('first-child-2_new');
    expect(getRowProperties(3)[0].value).toBe('first-child-2-child_new');
    expect(getRowProperties(4)[0].value).toBe('first-child-2');
    expect(getRowProperties(5)[0].value).toBe('first-child-2-child');
    expect(getRowProperties(6)[0].value).toBe('second');
  });

  /**
   * ## Table Operations # MoveTo Operation
   */
  it("MoveTo Operation - Move Down", function() {
    var moveToOp = new MoveToOperation();
    tableManager.selectCell(0,0); //select 'first' row
    tableManager.setDroppedOnId('second');

    moveToOp.execute(tableManager);

    expect(getRowProperties(0)[0].value).toBe('second');
    expect(getRowProperties(1)[0].value).toBe('first');
    expect(getRowProperties(2)[0].value).toBe('first-child');
    expect(getRowProperties(3)[0].value).toBe('first-child-2');
    expect(getRowProperties(4)[0].value).toBe('first-child-2-child');
  });

  it("MoveTo Operation - Move Up", function() {
    var moveToOp = new MoveToOperation();
    tableManager.selectCell(2,0); //select 'first-child-2' row
    tableManager.setDroppedOnId('first-child');

    moveToOp.execute(tableManager);

    expect(getRowProperties(0)[0].value).toBe('first');
    expect(getRowProperties(1)[0].value).toBe('first-child-2');
    expect(getRowProperties(2)[0].value).toBe('first-child-2-child');
    expect(getRowProperties(3)[0].value).toBe('first-child');
    expect(getRowProperties(4)[0].value).toBe('second');
  });

  it("MoveTo Operation - Move Into", function() {
    var moveToOp = new MoveToOperation();
    tableManager.selectCell(4,0); //select 'second' row
    tableManager.setDroppedOnId('first-child');
    setCanMoveInto( true );

    moveToOp.execute(tableManager);

    expect(getRowProperties(0)[0].value).toBe('first');
    expect(getRowProperties(1)[0].value).toBe('first-child');
    expect(getRowProperties(2)[0].value).toBe('second');
    expect(getRowProperties(3)[0].value).toBe('first-child-2');
    expect(getRowProperties(4)[0].value).toBe('first-child-2-child');


  });

  it("MoveTo Operation - Move Out", function() {
    var moveToOp = new MoveToOperation();
    tableManager.selectCell(3,0); //select 'first-child-2-child' row
    tableManager.setDroppedOnId('first');

    moveToOp.execute(tableManager);

    expect(getRowProperties(0)[0].value).toBe('first-child-2-child');
    expect(getRowProperties(1)[0].value).toBe('first');
    expect(getRowProperties(2)[0].value).toBe('first-child');
    expect(getRowProperties(3)[0].value).toBe('first-child-2');
    expect(getRowProperties(4)[0].value).toBe('second');
  });


});
