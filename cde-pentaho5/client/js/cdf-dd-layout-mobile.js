/*********************
 * FILTER OPERATIONS *
 *********************/

/*
 * MODELS
 */

/* Filter Block */

var FilterBlockModel = BaseModel.extend({}, {
  MODEL: 'FilterBlock',

  getStub: function () {

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

var FilterHeaderModel= BaseModel.extend({}, {
  MODEL: 'FilterHeader',

  getStub: function () {

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

/*
 * OPERATIONS
 */

/*Filter Block */

var LayoutAddFilterBlockOperation = AddRowOperation.extend({

  id: "LAYOUT_ADD_FILTER_BLOCK_MODEL",
  types: [],
  name: "Add a Filter Block",
  description: "Add a block in which to add your filters",
  icon: "getResource?resource=/images/NAV/FILTERPAGE.png",
  hoverIcon: "getResource?resource=/images/NAV/FILTERPAGE_mouseover.png",
  clickIcon: "getResource?resource=/images/NAV/FILTERPAGE_onclick.png",


  constructor: function () {
    this.logger = new Logger("LayoutAddFilterBlockOperation");
  },

  execute: function (tableManager) {
    var _stub = FilterBlockModel.getStub();
    					
    var indexManager = tableManager.getTableModel().getIndexManager();
    
    var insertAtIdx = 0;
    
    tableManager.insertAtIdx(_stub,insertAtIdx);
    
    // edit the new entry - we know the name is on the first line
    if( typeof tableManager.getLinkedTableManager() != 'undefined' ){
      $("table#" + tableManager.getLinkedTableManager().getTableId() +" > tbody > tr:first > td:eq(1)").trigger('click');
    }
  }
});

CellOperations.registerOperation(new LayoutAddFilterBlockOperation());

/* Filter Row */

var LayoutAddFilterRowOperation = AddRowOperation.extend({

  id: "LAYOUT_ADD_PARAMETER_ROW",
  types: [FilterBlockModel.MODEL,FilterHeaderModel.MODEL,FilterRowModel.MODEL],
  name: "Add Filter Row",
  description: "Add a new row for a parameter selector",
  icon: "getResource?resource=/images/NAV/FILTERLINE.png",
  hoverIcon: "getResource?resource=/images/NAV/FILTERLINE_mouseover.png",
  clickIcon: "getResource?resource=/images/NAV/FILTERLINE_onclick.png",


  constructor: function () {
    this.logger = new Logger("LayoutAddFilterRowOperation");
  },

  execute: function (tableManager) {
    var insertAtIdx;
    
    /* If a cell is selected, we want to insert the carousel immediately after that cell.
     * If not, we'll append it at the of the list.
     */
    if (tableManager.isSelectedCell) {
      var indexManager = tableManager.getTableModel().getIndexManager();
      var rowIdx, colIdx, rowId, rowType;
      rowIdx = tableManager.getSelectedCell()[0];
      colIdx = tableManager.getSelectedCell()[1];
      rowId = tableManager.getTableModel().getEvaluatedId(rowIdx);
      rowType = tableManager.getTableModel().getEvaluatedRowType(rowIdx),
      _stub = FilterRowModel.getStub();

      var nextSibling = indexManager.getNextSibling(rowId);
      if (typeof nextSibling == 'undefined') {
        insertAtIdx = indexManager.getLastChild(rowId).index + 1;
      } else {
        insertAtIdx = nextSibling.index;
      }

      if(rowType == FilterHeaderModel.MODEL || rowType == FilterRowModel.MODEL) {
        _stub.parent = indexManager.getIndex()[rowId].parent;
      }
      else if (rowType == FilterBlockModel.MODEL) {
	_stub.parent = rowId;
      }
      else{
        // insert at the end
	insertAtIdx = -1;
      }
    } else {
      insertAtIdx = tableManager.getTableModel().getData().length;

    }
    this.logger.debug("Inserting row at " + insertAtIdx);
    tableManager.insertAtIdx(_stub, insertAtIdx);
    // edit the new entry - we know the name is on the first line
    if (typeof tableManager.getLinkedTableManager() != 'undefined') {
      $("table#" + tableManager.getLinkedTableManager().getTableId() + " > tbody > tr:first > td:eq(1)").trigger('click');
    }
  }
});

CellOperations.registerOperation(new LayoutAddFilterRowOperation());

/* Filter Header */

var LayoutAddFilterHeaderOperation = AddRowOperation.extend({

  id: "LAYOUT_ADD_FILTER_HEADER",
  types: [FilterBlockModel.MODEL,FilterHeaderModel.MODEL,FilterRowModel.MODEL],
  name: "Add Filter Header",
  description: "Add a new group header for the filters panel",
  icon: "getResource?resource=/images/NAV/FILTERGROUPHEADER.png",
  hoverIcon: "getResource?resource=/images/NAV/FILTERGROUPHEADER_mouseover.png",
  clickIcon: "getResource?resource=/images/NAV/FILTERGROUPHEADER_onclick.png",


  constructor: function () {
    this.logger = new Logger("LayoutAddFilterHeaderOperation");
  },

  execute: function (tableManager) {
     var insertAtIdx;
    
    /* If a cell is selected, we want to insert the carousel immediately after that cell.
     * If not, we'll append it at the of the list.
     */
    if (tableManager.isSelectedCell) {
      var indexManager = tableManager.getTableModel().getIndexManager();
      var rowIdx, colIdx, rowId, rowType;
      rowIdx = tableManager.getSelectedCell()[0];
      colIdx = tableManager.getSelectedCell()[1];
      rowId = tableManager.getTableModel().getEvaluatedId(rowIdx);
      rowType = tableManager.getTableModel().getEvaluatedRowType(rowIdx),
      _stub = FilterHeaderModel.getStub();

      var nextSibling = indexManager.getNextSibling(rowId);
      if (typeof nextSibling == 'undefined') {
        insertAtIdx = indexManager.getLastChild(rowId).index + 1;
      } else {
        insertAtIdx = nextSibling.index;
      }

      if(rowType == FilterHeaderModel.MODEL || rowType == FilterRowModel.MODEL) {
        _stub.parent = indexManager.getIndex()[rowId].parent;
      }
      else if (rowType == FilterBlockModel.MODEL) {
	_stub.parent = rowId;
      }
      else{
        // insert at the end
	insertAtIdx = -1;
      }
    } else {
      insertAtIdx = tableManager.getTableModel().getData().length;

    }
    this.logger.debug("Inserting row at " + insertAtIdx);
    tableManager.insertAtIdx(_stub, insertAtIdx);
    // edit the new entry - we know the name is on the first line
    if (typeof tableManager.getLinkedTableManager() != 'undefined') {
      $("table#" + tableManager.getLinkedTableManager().getTableId() + " > tbody > tr:first > td:eq(1)").trigger('click');
    }
  }
});

CellOperations.registerOperation(new LayoutAddFilterHeaderOperation());

/***********************
 * CAROUSEL OPERATIONS *
 ***********************/

/*
 * CAROUSEL
 */

var LayoutCarouselModel = BaseModel.extend({}, {
  MODEL: 'LayoutCarousel',

  getStub: function () {

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


var LayoutAddCarouselOperation = AddRowOperation.extend({

  id: "LAYOUT_ADD_CAROUSEL",
  types: [],
  name: "Add Carousel",
  description: "Add a new carousel that cycles between components",
  icon: "getResource?resource=/images/NAV/ADDCAROUSEL.png",
  hoverIcon: "getResource?resource=/images/NAV/ADDCAROUSEL_mouseover.png",
  clickIcon: "getResource?resource=/images/NAV/ADDCAROUSEL_onclick.png",


  constructor: function () {
    this.logger = new Logger("LayoutAddCarouselOperation");
  },

  execute: function (tableManager) {
    var insertAtIdx;
    
    /* If a cell is selected, we want to insert the carousel immediately after that cell.
     * If not, we'll append it at the of the list.
     */
    if (tableManager.isSelectedCell) {
      var indexManager = tableManager.getTableModel().getIndexManager();
      var rowIdx, colIdx, rowId, rowType;
      rowIdx = tableManager.getSelectedCell()[0];
      colIdx = tableManager.getSelectedCell()[1];
      rowId = tableManager.getTableModel().getEvaluatedId(rowIdx);
      rowType = tableManager.getTableModel().getEvaluatedRowType(rowIdx);

      var nextSibling = indexManager.getNextSibling(rowId);
      if (typeof nextSibling == 'undefined') {
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
    // edit the new entry - we know the name is on the first line
    if (typeof tableManager.getLinkedTableManager() != 'undefined') {
      $("table#" + tableManager.getLinkedTableManager().getTableId() + " > tbody > tr:first > td:eq(1)").trigger('click');
    }
  }
});

CellOperations.registerOperation(new LayoutAddCarouselOperation());

/*
 * CAROUSEL ITEM
 */

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


var LayoutAddCarouselItemOperation = AddRowOperation.extend({

  id: "LAYOUT_ADD_CAROUSEL_ITEM",
  types: [LayoutCarouselModel.MODEL],
  name: "Add Carousel Item",
  description: "Add a new item to a carousel",
  icon: "getResource?resource=/images/NAV/ADDCAROUSEL_ITEM.png",
  hoverIcon: "getResource?resource=/images/NAV/ADDCAROUSEL_ITEM_mouseover.png",
  clickIcon: "getResource?resource=/images/NAV/ADDCAROUSEL_ITEM_onclick.png",


  constructor: function () {
    this.logger = new Logger("LayoutAddCarouselItemOperation");
  },

  execute: function (tableManager) {
    var insertAtIdx;
    
    /* If a cell is selected, we want to insert the carousel immediately after that cell.
     * If not, we'll append it at the of the list.
     */
    if (tableManager.isSelectedCell) {
      var indexManager = tableManager.getTableModel().getIndexManager();
      var rowIdx, colIdx, rowId, rowType;
      rowIdx = tableManager.getSelectedCell()[0];
      colIdx = tableManager.getSelectedCell()[1];
      rowId = tableManager.getTableModel().getEvaluatedId(rowIdx);
      rowType = tableManager.getTableModel().getEvaluatedRowType(rowIdx),
      _stub = LayoutCarouselItemModel.getStub();

      var nextSibling = indexManager.getNextSibling(rowId);
      if (typeof nextSibling == 'undefined') {
        insertAtIdx = indexManager.getLastChild(rowId).index + 1;
      } else {
        insertAtIdx = nextSibling.index;
      }

      if(rowType == LayoutCarouselItemModel.MODEL) {
        _stub.parent = indexManager.getIndex()[rowId].parent;
      }
      else if (rowType == LayoutCarouselModel.MODEL) {
	_stub.parent = rowId;
      }
      else{
        // insert at the end
	insertAtIdx = -1;
      }
    } else {
      insertAtIdx = tableManager.getTableModel().getData().length;

    }
    this.logger.debug("Inserting row at " + insertAtIdx);
    tableManager.insertAtIdx(_stub, insertAtIdx);
    // edit the new entry - we know the name is on the first line
    if (typeof tableManager.getLinkedTableManager() != 'undefined') {
      $("table#" + tableManager.getLinkedTableManager().getTableId() + " > tbody > tr:first > td:eq(1)").trigger('click');
    }
  }
});

CellOperations.registerOperation(new LayoutAddCarouselItemOperation());

