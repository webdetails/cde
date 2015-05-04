/*!
 * Copyright 2002 - 2015 Webdetails, a Pentaho company. All rights reserved.
 *
 * This software was developed by Webdetails and is provided under the terms
 * of the Mozilla Public License, Version 2.0, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */

// Global components array where all the new components are registered.
var CDFDDComponentsArray = [];

var ComponentsPanel = Panel.extend({

  name: "Components Panel",
  initPallete: true,
  componentsPallete: {},
  componentsTable: {},
  componentsTableModel: {},
  propertiesTable: {},
  propertiesTableModel: {},
  componentsArray: [],

  constructor: function(id) {
    this.base(id);
    this.logger = new Logger("Components");
    Panel.register(this);
  },

  initTables: function() {
    this.initPallete = false;
    this.init();
  },

  init: function() {
    var panelOperations = [
      new ComponentsMoveUpOperation(),
      new ComponentsMoveDownOperation(),
      new ComponentsDuplicateOperation(),
      new ComponentsDeleteOperation()
    ];
    this.base();
    this.logger.debug("Specific init");

    // Pallete
    if(this.initPallete) {
      this.componentsPallete = new PalleteManager(ComponentsPanel.PALLETE);
      this.addPalleteEntries();
    }
    this.componentsPallete.init();

    // Components
    this.componentsTable = new TableManager(ComponentsPanel.COMPONENTS);
    this.componentsTable.setTitle("Components");
    this.componentsPallete.setLinkedTableManager(this.componentsTable);
    this.componentsTable.setOperations(panelOperations);

    var componentsTableModel = new TableModel('componentsTreeTableModel');
    componentsTableModel.setColumnNames(['Type', 'Name']);
    componentsTableModel.setColumnGetExpressions([
      function(row) { return row.typeDesc; },
      function(row) {
        var props = row.properties;
        return props.length ? props[0].value : "";
      }
    ]);
    componentsTableModel.setColumnTypes(['String', 'String']);
    var rowId = function(row) { return row.id; };
    componentsTableModel.setRowId(rowId);
    var rowType = function(row) { return row.type; };
    componentsTableModel.setRowType(rowType);
    var parentId = function(row) { return row.parent };
    componentsTableModel.setParentId(parentId);
    var componentRows = cdfdd.getDashboardData().components.rows;
    componentsTableModel.setData(componentRows);
    this.componentsTable.setTableModel(componentsTableModel);
    this.componentsTable.init();
    $('#' + ComponentsPanel.COMPONENTS).addClass('selectedTable');

    // Properties
    this.propertiesTable = new TableManager(ComponentsPanel.PROPERTIES);
    this.propertiesTable.setTitle("Properties");
    var propertiesTableModel = new PropertiesTableModel('componentsPropertiesTableModel');

    // If we set the name, we need to change the name in the componentsTable
    propertiesTableModel.setColumnSetExpressions([undefined,
      function(row, value) {
        row.value = value;
        if(row.name == 'name') {
          var _tableManager = TableManager.getTableManager("table-" + ComponentsPanel.COMPONENTS);
          this.logger.debug("Changing the name - applying to previous row in " + _tableManager + " in row " + _tableManager.getSelectedCell()[0]);
          var _cell = _tableManager.getSelectedCell();
          $("#" + _tableManager.getTableId() + " > tbody > tr:eq(" + _cell[0] + ") > td:eq(1)").text(value);
        }
      }
    ]);

    this.propertiesTable.setTableModel(propertiesTableModel);
    this.propertiesTable.hasAdvancedProperties = true;
    this.propertiesTable.init();

    this.componentsTable.setLinkedTableManager(this.propertiesTable);
    this.componentsTable.setLinkedTableManagerOperation(function(row, classType) {
      var arr = [];
      var props = row.properties;
      for(var p in props) {
        var prop = props[p];
        if(!prop.classType || prop.classType == classType) {
          arr.push(prop);
        }
      }
      return arr;
    });

    $('#' + ComponentsPanel.COMPONENTS).click(function(e) {
      $('#' + ComponentsPanel.PROPERTIES).removeClass('selectedTable').addClass('unselectedTable');
      $('#' + ComponentsPanel.COMPONENTS).addClass('selectedTable').removeClass('unselectedTable');
    });

    $('#' + ComponentsPanel.PROPERTIES).click(function(e) {
      $('#' + ComponentsPanel.COMPONENTS).addClass('unselectedTable').removeClass('selectedTable');
      $('#' + ComponentsPanel.PROPERTIES).addClass('selectedTable').removeClass('unselectedTable');
    });
  },

  getContent: function() {
    return '\n' +
        '<div id="' + ComponentsPanel.PALLETE + '" class="span-6 accordion"></div>\n' +
        '<div id="' + ComponentsPanel.COMPONENTS + '" class="span-8 panel-scroll-element">Components</div>\n' +
        '<div id="' + ComponentsPanel.PROPERTIES + '" class="span-10 panel-scroll-element last">Properties</div>\n';
  },

  addPalleteEntries: function() {
    $.each(CDFDDComponentsArray, function(i, component) {
      Panel.getPanel(ComponentsPanel.MAIN_PANEL).getComponentsPallete().addEntry(component);
    });
  },

  // Get Parameters
  getParameters: function() {
    var data = this.componentsTable.getTableModel().getData();
    var output = [];
    $.each(data, function(i, row) {
      if(row.type.match("Parameter")) {
        output.push(row);
      }
    });
    return output;
  },

  getDuplicateOperation: function() {
    return ComponentsDuplicateOperation;
  },

  getSelectedTable: function() {
    var selectedTableId = $('#panel-' + this.id + ' .selectedTable').attr('id');

    return TableManager.getTableManager("table-" + selectedTableId);
  },

  selectNextTable: function() {
    var selectedTableId = $('#panel-' + this.id + '  .selectedTable').attr('id');

    if(selectedTableId == ComponentsPanel.COMPONENTS && this.componentsTable.isSelectedCell) {
      $('#' + ComponentsPanel.PROPERTIES).click();
      return this.propertiesTable;
    } else if(selectedTableId == ComponentsPanel.PROPERTIES) {
      $('#' + ComponentsPanel.COMPONENTS).click();
      return this.componentsTable;
    }
  },

  //Get components
  getComponents: function() {
    return this.componentsTable.getTableModel().getData();
  },

  setComponentsPallete: function(componentsPallete) {
    this.componentsPallete = componentsPallete;
  },
  getComponentsPallete: function() {
    return this.componentsPallete;
  },
  setComponentsArray: function(componentsArray) {
    this.componentsArray = componentsArray;
  },
  getComponentsArray: function() {
    return this.componentsArray;
  }
}, {

  MAIN_PANEL: "componentens_panel",
  PALLETE: "cdfdd-components-pallete",
  PALLETE_ID: "cdfdd-components-palletePallete",
  COMPONENTS: "cdfdd-components-components",
  PROPERTIES: "cdfdd-components-properties"
});

/*
 Components Panel Operations
 */
var ComponentsDuplicateOperation = DuplicateOperation.extend({

  id: "COMPONENTS_DUPLICATE",
  types: ["Components"],
  name: "Duplicate component",
  description: "Insert a clone of this component",

  constructor: function() {
    this.logger = new Logger("ComponentsDuplicateOperation");
  },

  canExecute: function( tableManager ) {
    return tableManager.isSelectedCell && !tableManager.isSelectedGroupCell;
  }
});
CellOperations.registerOperation(new ComponentsDuplicateOperation);

var ComponentsMoveUpOperation = MoveUpOperation.extend({

  id: "COMPONENTS_MOVE_UP",
  types: ["Components"],

  constructor: function() {
    this.logger = new Logger("ComponentsMoveUpOperation");
  }
});
CellOperations.registerOperation(new ComponentsMoveUpOperation);

var ComponentsMoveDownOperation = MoveDownOperation.extend({

  id: "COMPONENTS_MOVE_DOWN",
  types: ["Components"],

  constructor: function() {
    this.logger = new Logger("ComponentsMoveDownOperation");
  }
});
CellOperations.registerOperation(new ComponentsMoveDownOperation);

var ComponentsDeleteOperation = DeleteOperation.extend({

  id: "COMPONENTS_DELETE",
  types: ["Components"],

  constructor: function() {
    this.logger = new Logger("ComponentsDeleteOperation");
  }
});
CellOperations.registerOperation(new ComponentsDeleteOperation);


var ComponentValidations = {};
//name is always property 0
ComponentValidations.NAME_PROP_IDX = 0;
ComponentValidations.STATUS_ERROR = 'error';
ComponentValidations.STATUS_OK = 'ok';
ComponentValidations.STATUS_WARN = 'warn';

ComponentValidations.aggregateStatus = function(overallStatus, validationStatus) {
  if(validationStatus == null) return overallStatus;

  switch(overallStatus) {
    case ComponentValidations.STATUS_OK:
      overallStatus = validationStatus;
      break;
    case ComponentValidations.STATUS_WARN:
      if(validationStatus == ComponentValidations.STATUS_ERROR) {
        overallStatus = validationStatus;
      }
      break;
  }

  return overallStatus;
};

ComponentValidations.validateComponentNames = function(components) {

  if(components == null) components = Panel.getPanel(ComponentsPanel.MAIN_PANEL).getComponents();
  //TODO: also validate datasources
  var validations = [];
  var names = {};
  for(var i = 0; i < components.length; i++) {
    var comp = components[i];
    if(comp.type != 'Label' && comp.properties != undefined) {
      //param / comp
      var name = comp.properties[this.NAME_PROP_IDX].value;
      if(name == null || name == '') {
        var msg = 'A Component of type "' + comp.typeDesc + '" doesn\'t have a name.';
        validations.push([ComponentValidations.STATUS_ERROR, msg]);
      } else {
        if(names[name] != undefined) {
          var msg = 'A component of type "' + comp.typeDesc + '" has the same name "' + name + '" as a component of type "' + names[name] + '"';
          validations.push([ComponentValidations.STATUS_ERROR, msg]);
        }
        names[name] = comp.typeDesc;
      }
    }
  }

  if(validations.length > 0) {
    return {status: this.STATUS_ERROR, validations: validations };
  } else {
    return {status: this.STATUS_OK, validations: [] };
  }
};

ComponentValidations.validateHtmlObjects = function(components) {
  //validate:
  //        multiple references to same htmlObj - ERROR
  //        references to inexistent htmlObj - ERROR
  //        component with no reference to htmlObj - WARN
  var statusMultipleHtmlObjRef = this.STATUS_ERROR;
  var statusNoHtmlObjRef = this.STATUS_WARN;
  var statusNoSuchHtmlObj = this.STATUS_WARN;

  if(components == null) components = Panel.getPanel(ComponentsPanel.MAIN_PANEL).getComponents();

  //init count
  var htmlObjRefs = {};
  var htmlObjects = Panel.getPanel(LayoutPanel.MAIN_PANEL).getHtmlObjects();
  for(var i = 0; i < htmlObjects.length; i++) {
    var htmlObj = htmlObjects[i];
    var objName = htmlObj.properties[this.NAME_PROP_IDX].value;
    if(objName != null) {
      htmlObjRefs[ objName ] = [];
    }
  }

  var validations = [];
  var status = this.STATUS_OK;

  //validate
  for(var i = 0; i < components.length; i++) {
    var comp = components[i];
    var compName = comp.properties[this.NAME_PROP_IDX].value;

    if(compName != '') for(p in comp.properties) {
      if(comp.properties.hasOwnProperty(p)) {
        if(comp.properties[p].name == 'htmlObject') {
          var htmlObj = comp.properties[p].value;

          if(htmlObj == '' || htmlObj == null) {
            var msg = 'Component "' + compName + '" has no html object';
            validations.push([statusNoHtmlObjRef, msg]);
            status = this.aggregateStatus(status, statusNoHtmlObjRef);
          } else {
            var objRefs = htmlObjRefs[ htmlObj ];
            if(objRefs == null) {
              var msg = 'Component "' + compName + '" references inexistent html object "' + htmlObj + '"';
              validations.push([statusNoSuchHtmlObj, msg]);
              status = this.aggregateStatus(status, statusNoSuchHtmlObj);
            } else {
              if(objRefs.length > 0) {
                var msg = 'Components "' + compName + '" and "' + objRefs[0] + '" have the same html object "' + htmlObj + '"';
                validations.push([statusMultipleHtmlObjRef, msg]);
                status = this.aggregateStatus(status, statusMultipleHtmlObjRef);
              }
              objRefs.push(compName);
            }
          }
        }
      }
    }
  }

  return {status: status, validations: validations};
};

ComponentValidations.validateComponents = function() {
  var status = this.STATUS_OK;
  var validations = [];

  var nameValidations = this.validateComponentNames();
  status = this.aggregateStatus(status, nameValidations.status);
  validations = nameValidations.validations;

  var htmlObjValidations = this.validateHtmlObjects();
  status = this.aggregateStatus(status, htmlObjValidations.status);
  validations = validations.concat(htmlObjValidations.validations);

  var msg = 'Validations: <br/>';

  if(status == this.STATUS_OK) {
    msg += 'No obvious problems detected.';
  } else {
    for(var i = 0; i < validations.length; i++) {
      msg += '<br/>';
      msg += '[' + validations[i][0] + '] ' + validations[i][1];//ToDo: change to status, message
    }
  }

  $.prompt(msg, {prefix: "popup"});
};
