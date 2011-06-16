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
  icon: "/pentaho/content/pentaho-cdf-dd/getResource?resource=/images/NAV/addcolumn.png",
  hoverIcon: "/pentaho/content/pentaho-cdf-dd/getResource?resource=/images/NAV/addcolumn_mouseover.png",
  clickIcon: "/pentaho/content/pentaho-cdf-dd/getResource?resource=/images/NAV/addcolumn_onclick.png",


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
  icon: "/pentaho/content/pentaho-cdf-dd/getResource?resource=/images/NAV/addcomponent.png",
  hoverIcon: "/pentaho/content/pentaho-cdf-dd/getResource?resource=/images/NAV/addcomponent_mouseover.png",
  clickIcon: "/pentaho/content/pentaho-cdf-dd/getResource?resource=/images/NAV/addcomponent_onclick.png",


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

