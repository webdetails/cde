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

/*********************
 * FILTER OPERATIONS *
 *********************/

/*
 * MODELS
 */

/* Filter Block */

var FilterBlockModel = BaseModel.extend({}, {
  MODEL: 'FilterBlock',

  getStub: function() {

    var _stub = {
      id: TableManager.generateGUID(),
      type: FilterBlockModel.MODEL,
      typeDesc: "Filter Block",
      parent: IndexManager.ROOTID,
      properties: []
    };

    _stub.properties.push(PropertiesManager.getProperty("name"));
//    _stub.properties.push(PropertiesManager.getProperty("title"));

    return _stub;
  }
});
BaseModel.registerModel(FilterBlockModel);

/* Filter Row */

var FilterRowModel = BaseModel.extend({}, {
  MODEL: 'FilterRow',

  getStub: function () {

    var _stub = {
      id: TableManager.generateGUID(),
      type: FilterRowModel.MODEL,
      typeDesc: "Filter Row",
      parent: IndexManager.ROOTID,
      properties: []
    };

    _stub.properties.push(PropertiesManager.getProperty("name"));
    _stub.properties.push(PropertiesManager.getProperty("label"));

    return _stub;
  }
});
BaseModel.registerModel(FilterRowModel);

/* Filter Header */

var FilterHeaderModel = BaseModel.extend({}, {
  MODEL: 'FilterHeader',

  getStub: function() {

    var _stub = {
      id: TableManager.generateGUID(),
      type: FilterHeaderModel.MODEL,
      typeDesc: "Filter Header",
      parent: IndexManager.ROOTID,
      properties: []
    };

    _stub.properties.push(PropertiesManager.getProperty("name"));
    _stub.properties.push(PropertiesManager.getProperty("title"));

    return _stub;
  }
});
BaseModel.registerModel(FilterHeaderModel);

/* CAROUSEL */

var LayoutCarouselModel = BaseModel.extend({}, {
  MODEL: 'LayoutCarousel',

  getStub: function() {

    var _stub = {
      id: TableManager.generateGUID(),
      type: LayoutCarouselModel.MODEL,
      typeDesc: "Carousel",
      parent: IndexManager.ROOTID,
      properties: []
    };

    _stub.properties.push(PropertiesManager.getProperty("name"));
    _stub.properties.push(PropertiesManager.getProperty("showTitle"));

    return _stub;
  }
});
BaseModel.registerModel(LayoutCarouselModel);

/* CAROUSEL ITEM */

var LayoutCarouselItemModel = BaseModel.extend({}, {
  MODEL: 'LayoutCarouselItem',
  getStub: function () {
    var _stub = {
      id: TableManager.generateGUID(),
      type: LayoutCarouselItemModel.MODEL,
      typeDesc: "Carousel Item",
      parent: IndexManager.ROOTID,
      properties: []
    };
    _stub.properties.push(PropertiesManager.getProperty("name"));
    _stub.properties.push(PropertiesManager.getProperty("title"));
    return _stub;
  }
});
BaseModel.registerModel(LayoutCarouselItemModel);

/*
 * OPERATIONS
 */

/*Filter Block */

var LayoutAddFilterBlockOperation = AddRowOperation.extend({

  id: "LAYOUT_ADD_FILTER_BLOCK_MODEL",
  types: [],
  name: "Add a Filter Block",
  description: "Add a block in which to add your filters",

  models: [FilterBlockModel.MODEL],
  canMoveInto: [],
  canMoveTo: [],

  constructor: function() {
    this.logger = new Logger("LayoutAddFilterBlockOperation");
  },

  canExecute: function(tableManager) {
    return true;
  },

  execute: function(tableManager) {
    var _stub = FilterBlockModel.getStub();

    var indexManager = tableManager.getTableModel().getIndexManager();

    var insertAtIdx = 0;

    tableManager.insertAtIdx(_stub, insertAtIdx);

    this.selectFirstProperty(tableManager);
  }
});
CellOperations.registerOperation(new LayoutAddFilterBlockOperation());

/* Filter Row */

var LayoutAddFilterRowOperation = AddRowOperation.extend({

  id: "LAYOUT_ADD_PARAMETER_ROW",
  types: [
    FilterBlockModel.MODEL, FilterHeaderModel.MODEL, FilterRowModel.MODEL
  ],
  name: "Add Filter Row",
  description: "Add a new row for a parameter selector",

  models: [FilterRowModel.MODEL],
  canMoveInto: [],
  canMoveTo: [],

  constructor: function() {
    this.logger = new Logger("LayoutAddFilterRowOperation");
  },

  execute: function(tableManager) {
    var insertAtIdx;

    /* If a cell is selected, we want to insert the carousel immediately after that cell.
     * If not, we'll append it at the of the list.
     */
    if (tableManager.isSelectedCell) {
      var indexManager = tableManager.getTableModel().getIndexManager();
      var rowIdx, colIdx, rowId, rowType, _stub;
      rowIdx = tableManager.getSelectedCell()[0];
      colIdx = tableManager.getSelectedCell()[1];
      rowId = tableManager.getTableModel().getEvaluatedId(rowIdx);
      rowType = tableManager.getTableModel().getEvaluatedRowType(rowIdx);
      _stub = FilterRowModel.getStub();

      var nextSibling = indexManager.getNextSibling(rowId);
      if(typeof nextSibling == 'undefined') {
        insertAtIdx = indexManager.getLastChild(rowId).index + 1;
      } else {
        insertAtIdx = nextSibling.index;
      }

      if(rowType == FilterHeaderModel.MODEL || rowType == FilterRowModel.MODEL) {
        _stub.parent = indexManager.getIndex()[rowId].parent;
      } else if(rowType == FilterBlockModel.MODEL) {
        _stub.parent = rowId;
      } else {
        // insert at the end
        insertAtIdx = -1;
      }
    } else {
      insertAtIdx = tableManager.getTableModel().getData().length;

    }
    this.logger.debug("Inserting row at " + insertAtIdx);
    tableManager.insertAtIdx(_stub, insertAtIdx);

    this.selectFirstProperty(tableManager);
  }
});
CellOperations.registerOperation(new LayoutAddFilterRowOperation());

/* Filter Header */

var LayoutAddFilterHeaderOperation = AddRowOperation.extend({

  id: "LAYOUT_ADD_FILTER_HEADER",
  types: [
    FilterBlockModel.MODEL, FilterHeaderModel.MODEL, FilterRowModel.MODEL
  ],
  name: "Add Filter Header",
  description: "Add a new group header for the filters panel",

  models: [FilterHeaderModel.MODEL],
  canMoveInto: [],
  canMoveTo: [],

  constructor: function() {
    this.logger = new Logger("LayoutAddFilterHeaderOperation");
  },

  execute: function(tableManager) {
    var insertAtIdx;

    /* If a cell is selected, we want to insert the carousel immediately after that cell.
     * If not, we'll append it at the of the list.
     */
    if (tableManager.isSelectedCell) {
      var indexManager = tableManager.getTableModel().getIndexManager();
      var rowIdx, colIdx, rowId, rowType, _stub;
      rowIdx = tableManager.getSelectedCell()[0];
      colIdx = tableManager.getSelectedCell()[1];
      rowId = tableManager.getTableModel().getEvaluatedId(rowIdx);
      rowType = tableManager.getTableModel().getEvaluatedRowType(rowIdx);
      _stub = FilterHeaderModel.getStub();

      var nextSibling = indexManager.getNextSibling(rowId);
      if(typeof nextSibling == 'undefined') {
        insertAtIdx = indexManager.getLastChild(rowId).index + 1;
      } else {
        insertAtIdx = nextSibling.index;
      }

      if(rowType == FilterHeaderModel.MODEL || rowType == FilterRowModel.MODEL) {
        _stub.parent = indexManager.getIndex()[rowId].parent;
      } else if(rowType == FilterBlockModel.MODEL) {
        _stub.parent = rowId;
      } else {
        // insert at the end
        insertAtIdx = -1;
      }
    } else {
      insertAtIdx = tableManager.getTableModel().getData().length;

    }
    this.logger.debug("Inserting row at " + insertAtIdx);
    tableManager.insertAtIdx(_stub, insertAtIdx);

    this.selectFirstProperty(tableManager);
  }
});
CellOperations.registerOperation(new LayoutAddFilterHeaderOperation());

/***********************
 * CAROUSEL OPERATIONS *
 ***********************/

/* CAROUSEL */

var LayoutAddCarouselOperation = AddRowOperation.extend({

  id: "LAYOUT_ADD_CAROUSEL",
  types: [],
  name: "Add Carousel",
  description: "Add a new carousel that cycles between components",

  models: [LayoutCarouselModel.MODEL],
  canMoveInto: [],
  canMoveTo: [],

  constructor: function() {
    this.logger = new Logger("LayoutAddCarouselOperation");
  },

  canExecute: function(tableManager) {
    return true;
  },

  execute: function(tableManager) {
    var insertAtIdx;

    /* If a cell is selected, we want to insert the carousel immediately after that cell.
     * If not, we'll append it at the of the list.
     */
    if(tableManager.isSelectedCell) {
      var indexManager = tableManager.getTableModel().getIndexManager();
      var rowIdx, colIdx, rowId, rowType;
      rowIdx = tableManager.getSelectedCell()[0];
      colIdx = tableManager.getSelectedCell()[1];
      rowId = tableManager.getTableModel().getEvaluatedId(rowIdx);
      rowType = tableManager.getTableModel().getEvaluatedRowType(rowIdx);

      var nextSibling = indexManager.getNextSibling(rowId);
      if(typeof nextSibling == 'undefined') {
        insertAtIdx = indexManager.getLastChild(rowId).index + 1;
      } else {
        insertAtIdx = nextSibling.index;
      }
    } else {
      insertAtIdx = tableManager.getTableModel().getData().length;

    }
    var _stub = LayoutCarouselModel.getStub();
    this.logger.debug("Inserting row at " + insertAtIdx);
    tableManager.insertAtIdx(_stub, insertAtIdx);

    this.selectFirstProperty(tableManager);
  }
});
CellOperations.registerOperation(new LayoutAddCarouselOperation());

/* CAROUSEL ITEM */

var LayoutAddCarouselItemOperation = AddRowOperation.extend({

  id: "LAYOUT_ADD_CAROUSEL_ITEM",
  types: [
    LayoutCarouselModel.MODEL
  ],
  name: "Add Carousel Item",
  description: "Add a new item to a carousel",

  models: [FilterBlockModel.MODEL],
  canMoveInto: [],
  canMoveTo: [],

  constructor: function() {
    this.logger = new Logger("LayoutAddCarouselItemOperation");
  },

  execute: function(tableManager) {
    var insertAtIdx;

    /* If a cell is selected, we want to insert the carousel immediately after that cell.
     * If not, we'll append it at the of the list.
     */
    if(tableManager.isSelectedCell) {
      var indexManager = tableManager.getTableModel().getIndexManager();
      var rowIdx, colIdx, rowId, rowType;
      rowIdx = tableManager.getSelectedCell()[0];
      colIdx = tableManager.getSelectedCell()[1];
      rowId = tableManager.getTableModel().getEvaluatedId(rowIdx);
      rowType = tableManager.getTableModel().getEvaluatedRowType(rowIdx),
          _stub = LayoutCarouselItemModel.getStub();

      var nextSibling = indexManager.getNextSibling(rowId);
      if(typeof nextSibling == 'undefined') {
        insertAtIdx = indexManager.getLastChild(rowId).index + 1;
      } else {
        insertAtIdx = nextSibling.index;
      }

      if(rowType == LayoutCarouselItemModel.MODEL) {
        _stub.parent = indexManager.getIndex()[rowId].parent;
      } else if (rowType == LayoutCarouselModel.MODEL) {
        _stub.parent = rowId;
      } else {
        // insert at the end
        insertAtIdx = -1;
      }
    } else {
      insertAtIdx = tableManager.getTableModel().getData().length;
    }

    this.logger.debug("Inserting row at " + insertAtIdx);
    tableManager.insertAtIdx(_stub, insertAtIdx);

    this.selectFirstProperty(tableManager);
  }
});
CellOperations.registerOperation(new LayoutAddCarouselItemOperation());

