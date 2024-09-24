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

describe("Commands Test #", function() {

  var myCommands = _.extend({}, Commands);

  afterEach(function() {
    myCommands.executedCommands = []
  });

  it("# ExecuteCommand", function() {
    expect(myCommands.executedCommands).toEqual([]);

    var command = new BaseCommand();
    spyOn(command, 'execute');
    myCommands.executeCommand(command);

    expect(myCommands.executedCommands.length).toEqual(1);
    expect(command.execute).toHaveBeenCalled();
    expect(myCommands.executedCommands[0].id).toEqual("BaseCommand");
  });

  it("# UndoCommand", function() {
    var command = new BaseCommand();
    spyOn(command, 'undo');
    myCommands.executeCommand(command);

    expect(myCommands.executedCommands.length).toEqual(1);
    expect(myCommands.executedCommands[0].id).toEqual("BaseCommand");

    myCommands.undoCommand();

    expect(command.undo).toHaveBeenCalled();
    expect(myCommands.executedCommands).toEqual([]);
  });

  it("# RowOperationCommand initialization", function() {
    var tableManager = { id: "mockTableManager" };
    var operation = { id: "mockOperation", execute: function() {} };
    var command = new RowOperationCommand(operation, tableManager);

    expect(command.id).toEqual("RowOperationCommand");
    expect(command.operation.id).toEqual(operation.id);
    expect(command.tableManager.id).toEqual(tableManager.id);
  });

  it("# EntryCommand initialization", function() {
    var palleteManager = { id: "mockPalleteManager" };
    var entry = { id: "mockEntry", execute: function() {} };
    var command = new EntryCommand(entry, palleteManager);

    expect(command.id).toEqual("EntryCommand");
    expect(command.entry.id).toEqual(entry.id);
    expect(command.palleteManager.id).toEqual(palleteManager.id);
  });

  it("# ChangePropertyCommand initialization", function() {
    var tableManager = { id: "mockTableManager" };
    var propertyRow = { id: "mockPropertyRow" };
    var value = "newValue";
    var command = new ChangePropertyCommand(tableManager, propertyRow, value);

    expect(command.id).toEqual("ChangePropertyCommand");
    expect(command.newValue).toEqual(value);
    expect(command.property.id).toEqual(propertyRow.id);
    expect(command.tableManager.id).toEqual(tableManager.id);
  });
});
