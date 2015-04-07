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
