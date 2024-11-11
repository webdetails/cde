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


var Commands = Base.extend({

  logger: {},

  constructor: function () {
    this.logger = new Logger("BaseType");
  }

}, {

  executedCommands: [],

  executeCommand: function(command) {
    try {
      command.execute();

      this.executedCommands.push(command);
      CDFDDUtils.markAsDirty();
    } catch(e) {
      //empty
    }
  },

  undoCommand: function() {
    var command = this.executedCommands.pop();
    if(command != undefined) {
      command.undo();
    }
  },

  cleanExecutedCommands: function() {
    this.executedCommands = [];
  }
});

var BaseCommand = Base.extend({

  id: "BaseCommand",
  logger: {},

  constructor: function() {
    this.logger = new Logger("BaseCommand");
  },

  execute: function() {
    //default does nothing
  },

  undo: function() {
    //default does nothing
  }
});

var RowOperationCommand = BaseCommand.extend({

  id: "RowOperationCommand",
  operation: undefined,
  tableManager: undefined,

  constructor: function(operation, tableManager) {
    this.tableManager = tableManager;
    this.operation = operation;
    this.logger = new Logger("RowOperationCommand");
  },

  execute: function() {
    if(!this.operation.checkAndExecute(this.tableManager)) {
      throw "Command couldn't be executed";
    }
  },

  undo: function() {
    //not implemented
  }
});

var EntryCommand = BaseCommand.extend({

  id: "EntryCommand",
  entry: undefined,
  palleteManager: undefined,

  constructor: function(entry, palleteManager) {
    this.palleteManager = palleteManager;
    this.entry = entry;
    this.logger = new Logger("EntryCommand");
  },

  execute: function() {
    this.entry.execute(this.palleteManager);
  },

  undo: function() {
    //not implemented
  }
});

var ChangePropertyCommand = BaseCommand.extend({

  id: "ChangePropertyCommand",
  tableManager: undefined,
  property: undefined,
  newValue: undefined,

  constructor: function(tableManager, property, value) {
    this.tableManager = tableManager;
    this.property = property;
    this.newValue = value;

    this.logger = new Logger("ChangePropertyCommand");
  },

  execute: function() {
    var tableModel = this.tableManager.getTableModel();
    var _setExpression = tableModel.getColumnSetExpressions()[1];

    _setExpression.apply(this.tableManager, [this.property, this.newValue]);
  },

  undo: function() {
    //not implemented
  }
});
