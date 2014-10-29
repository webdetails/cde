var LayoutPanel = Panel.extend({

		id: "layout",
		name: "Layout Panel",
		treeTable: {},
		treeTableModel: {},
		propertiesTable: {},
		propertiesTableModel: {},

		constructor: function(id){
			
			this.base(id);
			this.logger = new Logger("Layout");
			Panel.register(this);

		},
		init: function() {
			operationSets = {
				blueprint: [
					new LayoutSaveAsTemplateOperation(),
					new LayoutApplyTemplateOperation(),
					new LayoutAddResourceOperation(),
					new LayoutAddFreeFormOperation(),
					new LayoutAddRowOperation()
				],
				bootstrap: [
					new LayoutSaveAsTemplateOperation(),
					new LayoutApplyTemplateOperation(),
					new LayoutAddResourceOperation(),
					new LayoutAddBootstrapPanelOperation(),
					new LayoutAddFreeFormOperation(),
					new LayoutAddRowOperation()
				],
				mobile: [
					new LayoutSaveAsTemplateOperation(),
					new LayoutApplyTemplateOperation(),
					new LayoutAddResourceOperation(),
					new LayoutAddCarouselOperation(),
					new LayoutAddFreeFormOperation(),
					new LayoutAddRowOperation(),
					new LayoutAddFilterBlockOperation()
				]
			};

			this.base();
			this.logger.debug("Specific init");

			// Tree

            this.treeTable = new TableManager(LayoutPanel.TREE);
            this.treeTable.setTitle("Layout Structure");

            var dashboardType = cdfdd.dashboardWcdf.rendererType || "bootstrap";
            this.treeTable.setInitialOperations(operationSets[dashboardType]);

            var treeTableModel = new TableModel('layoutTreeTableModel');
            treeTableModel.setColumnNames(['Type','Name']);
            treeTableModel.setColumnGetExpressions([
                function(row){return row.typeDesc},
                function(row){return row.properties[0].value}
                ]);
            treeTableModel.setColumnTypes(['String','String']);
            var rowId = function(row){return row.id};
            treeTableModel.setRowId(rowId);
            var rowType = function(row){return row.type};
            treeTableModel.setRowType(rowType);
            var parentId = function(row){return row.parent};
            treeTableModel.setParentId(parentId);
            var layoutRows = cdfdd.getDashboardData().layout.rows;
            treeTableModel.setData(layoutRows);
            this.treeTable.setTableModel(treeTableModel);
            this.treeTable.init();


			// Properties
			this.propertiesTable = new TableManager(LayoutPanel.PROPERTIES);
			this.propertiesTable.setTitle("Properties");
			var propertiesTableModel = new PropertiesTableModel('layoutPropertiesTableModel');

			// If we set the name, we need to change the name in the treeTable
			propertiesTableModel.setColumnSetExpressions([undefined,
				function(row,value){
					row.value = value
					if (row.name == 'name'){
						var _tableManager = TableManager.getTableManager("table-" + LayoutPanel.TREE);
						this.logger.debug("Changing the name - applying to previous row in " + _tableManager + " in row " + _tableManager.getSelectedCell()[0]);
						var _cell = _tableManager.getSelectedCell();
						$("#" + _tableManager.getTableId() + " > tbody > tr:eq("+ _cell[0] +") > td:eq(1)" ).text(value);
						
					}
				}
				]);

			this.propertiesTable.setTableModel(propertiesTableModel);
			this.propertiesTable.init();

			this.treeTable.setLinkedTableManager(this.propertiesTable);
			this.treeTable.setLinkedTableManagerOperation(function(row){
				var arr = []; 
				for (p in row.properties){
					if(row.properties.hasOwnProperty(p)){
						arr.push(row.properties[p]);
					}
				}
				return arr;
			});
		},

		getContent: function() {
		
			return ' \n' +
'			<div id="'+ LayoutPanel.TREE +'" class="span-12">Tree</div>\n' +
'			<div id="'+ LayoutPanel.PROPERTIES + '" class="span-12 last">Properties</div>\n' +
'			';			
		
		}, 

		// Get HtmlObjects
		getHtmlObjects: function(){
			
			var data = this.treeTable.getTableModel().getData();
			var output = [];
			var myself = this;
			$.each(data, function( i, row ) {
				if( row.type == LayoutRowModel.MODEL || row.type ==  LayoutColumnModel.MODEL ||
					row.type == LayoutBootstrapColumnModel.MODEL || row.type == LayoutFreeFormModel.MODEL ||
					row.type == LayoutBootstrapPanelHeaderModel.MODEL || row.type == LayoutBootstrapPanelBodyModel.MODEL ||
					row.type == LayoutBootstrapPanelFooterModel.MODEL ) {

					// Use the ones that don't have children
					if( myself.treeTable.getTableModel().getIndexManager().getIndex()[row.id].children.length == 0 ) {
						var rowProperties = row.properties;
						$.each(rowProperties, function( i, prop ) {
							if( prop.name == "name" && prop.value != "" ) {
								output.push(row);
								return false;
							}
						});
					}
				}
			});
			return output;
		}
	},{
	
		MAIN_PANEL: "layout_panel",
		TREE: "cdfdd-layout-tree",
		PROPERTIES: "cdfdd-layout-properties"
	});

var LayoutBootstrapPanelHeaderModel = BaseModel.extend({},{
  MODEL: 'BootstrapPanelHeader',
  getStub: function() {
    var _stub = {
      id: TableManager.generateGUID(),
      type: LayoutBootstrapPanelHeaderModel.MODEL,
      typeDesc: "Panel Header",
      parent: IndexManager.ROOTID,
      properties: []
    };

    _stub.properties.push(PropertiesManager.getProperty("name"));
    _stub.properties.push(PropertiesManager.getProperty("roundCorners"));
    _stub.properties.push(PropertiesManager.getProperty("cssClass"));
    _stub.properties.push(PropertiesManager.getProperty("textAlign"));
    return _stub;
  }

});
BaseModel.registerModel(LayoutBootstrapPanelHeaderModel);

var LayoutAddBootstrapPanelHeaderOperation = AddRowOperation.extend({

  id: "LAYOUT_ADD_BOOTSTRAP_PANEL_HEADER",
  types: [],
  name: "Add Bootstrap Panel Header",
  description: "Adds a bootstrap node",

  constructor: function () {
    this.logger = new Logger("LayoutAddBootstrapPanelHeaderOperation");
  },

  execute: function (tableManager) {
    var _stub = LayoutBootstrapPanelHeaderModel.getStub();
    var indexManager = tableManager.getTableModel().getIndexManager();
    var rowIdx = tableManager.getSelectedCell()[0];
    var rowId = tableManager.getTableModel().getEvaluatedId(rowIdx);
    var rowType = tableManager.getTableModel().getEvaluatedRowType(rowIdx);

    _stub.parent = rowId;
    var insertAtIdx = indexManager.getIndex()[rowId].index + 1;

    this.logger.debug("Inserting bootstrap node after at " + insertAtIdx);
    tableManager.insertAtIdx(_stub, insertAtIdx);

  }
});


var LayoutBootstrapPanelBodyModel = BaseModel.extend({},{
  MODEL: 'BootstrapPanelBody',
  getStub: function() {
    var _stub = {
      id: TableManager.generateGUID(),
      type: LayoutBootstrapPanelBodyModel.MODEL,
      typeDesc: "Panel Body",
      parent: IndexManager.ROOTID,
      properties: []
    };

    _stub.properties.push(PropertiesManager.getProperty("name"));
    _stub.properties.push(PropertiesManager.getProperty("roundCorners"));
    _stub.properties.push(PropertiesManager.getProperty("cssClass"));
    _stub.properties.push(PropertiesManager.getProperty("textAlign"));
    return _stub;
  }

});
BaseModel.registerModel(LayoutBootstrapPanelBodyModel);

var LayoutAddBootstrapPanelBodyOperation = AddRowOperation.extend({

  id: "LAYOUT_ADD_BOOTSTRAP_PANEL_BODY",
  types: [],
  name: "Add Bootstrap Panel Body",
  description: "Adds a bootstrap node",

  constructor: function () {
    this.logger = new Logger("LayoutAddBootstrapPanelBodyOperation");
  },

  execute: function (tableManager) {
    var _stub = LayoutBootstrapPanelBodyModel.getStub();
    var indexManager = tableManager.getTableModel().getIndexManager();
    var rowIdx = tableManager.getSelectedCell()[0];
    var rowId = tableManager.getTableModel().getEvaluatedId(rowIdx);
    var rowType = tableManager.getTableModel().getEvaluatedRowType(rowIdx);

    _stub.parent = indexManager.getIndex()[rowId].parent;
    var insertAtIdx = indexManager.getIndex()[_stub.parent].index + 2;

    this.logger.debug("Inserting bootstrap node after at " + insertAtIdx);
    tableManager.insertAtIdx(_stub, insertAtIdx);

  }
});


var LayoutBootstrapPanelFooterModel = BaseModel.extend({},{
  MODEL: 'BootstrapPanelFooter',
  getStub: function() {
    var _stub = {
      id: TableManager.generateGUID(),
      type: LayoutBootstrapPanelFooterModel.MODEL,
      typeDesc: "Panel Footer",
      parent: IndexManager.ROOTID,
      properties: []
    };
    _stub.properties.push(PropertiesManager.getProperty("name"));
    _stub.properties.push(PropertiesManager.getProperty("roundCorners"));
    _stub.properties.push(PropertiesManager.getProperty("cssClass"));
    _stub.properties.push(PropertiesManager.getProperty("textAlign"));
    return _stub;
  }

});
BaseModel.registerModel(LayoutBootstrapPanelFooterModel);

var LayoutAddBootstrapPanelFooterOperation = AddRowOperation.extend({

  id: "LAYOUT_ADD_BOOTSTRAP_PANEL_FOOTER",
  types: [],
  name: "Add Bootstrap Panel Footer",
  description: "Adds a bootstrap node",

  constructor: function () {
    this.logger = new Logger("LayoutAddBootstrapPanelFooterOperation");
  },

  execute: function (tableManager) {
    var _stub = LayoutBootstrapPanelFooterModel.getStub();
    var indexManager = tableManager.getTableModel().getIndexManager();
    var rowIdx = tableManager.getSelectedCell()[0];
    var rowId = tableManager.getTableModel().getEvaluatedId(rowIdx);
    var rowType = tableManager.getTableModel().getEvaluatedRowType(rowIdx);

    _stub.parent = indexManager.getIndex()[rowId].parent;
    var insertAtIdx = indexManager.getIndex()[_stub.parent].index + 3;

    this.logger.debug("Inserting bootstrap node after at " + insertAtIdx);
    tableManager.insertAtIdx(_stub, insertAtIdx);

  }
});


var LayoutRowModel = BaseModel.extend({
	},{
		MODEL: 'LayoutRow',

		getStub: function(){

			var _stub = {
				id: TableManager.generateGUID(),
				type: LayoutRowModel.MODEL,
				typeDesc: "Row",
				parent: IndexManager.ROOTID,
				properties: []
			};
			_stub.properties.push(PropertiesManager.getProperty("name"));
			_stub.properties.push(PropertiesManager.getProperty("height"));
			_stub.properties.push(PropertiesManager.getProperty("backgroundColor"));
			_stub.properties.push(PropertiesManager.getProperty("roundCorners"));
			_stub.properties.push(PropertiesManager.getProperty("cssClass"));
			_stub.properties.push(PropertiesManager.getProperty("textAlign"));

			return _stub;
		}
	});
BaseModel.registerModel(LayoutRowModel);


var LayoutAddRowOperation = AddRowOperation.extend({

	id: "LAYOUT_ADD_ROW",
	types: [],
	name: "Add Row",
	description: "Add a new row",

	constructor: function(){
		this.logger = new Logger("LayoutAddRowOperation");
	},

	execute: function(tableManager){

		// Add a row on the specified position;

		var _stub = LayoutRowModel.getStub();

		var rowIdx;
		var colIdx = 0;
		var rowId;
		var rowType;
		var insertAtIdx = -1;

		var indexManager = tableManager.getTableModel().getIndexManager();

		if (tableManager.isSelectedCell){
			rowIdx = tableManager.getSelectedCell()[0];
			colIdx = tableManager.getSelectedCell()[1];
			rowId = tableManager.getTableModel().getEvaluatedId(rowIdx);
			rowType = tableManager.getTableModel().getEvaluatedRowType(rowIdx);

			var nextSibling = indexManager.getNextSibling(rowId);
			if (typeof nextSibling == 'undefined'){
				insertAtIdx = indexManager.getLastChild(rowId).index + 1;
			}
			else{
				insertAtIdx = nextSibling.index;
			}
			// Logic: If this is a LayoutRowModel.MODEL, insert after, same parent as this layout row;
			// if it's a LayoutColumnModel.MODEL, insert after, parent on the column; Anything else, add
			// to the end

			if( rowType == LayoutRowModel.MODEL || rowType == LayoutSpaceModel.MODEL ){
				_stub.parent = indexManager.getIndex()[rowId].parent;
			}
			else if (rowType == LayoutColumnModel.MODEL || rowType == LayoutBootstrapColumnModel.MODEL ||
             		 rowType == LayoutBootstrapPanelHeaderModel.MODEL || rowType == LayoutBootstrapPanelBodyModel.MODEL ||
             		 rowType == LayoutBootstrapPanelFooterModel.MODEL || rowType == LayoutFreeFormModel.MODEL){
				_stub.parent = rowId;
			}
			else{
				// insert at the end
				insertAtIdx = tableManager.getTableModel().getData().length;
			}

		}
		else{
			insertAtIdx = tableManager.getTableModel().getData().length;
		}

		this.logger.debug("Inserting row after "+ rowType + " at " + insertAtIdx);

		tableManager.insertAtIdx(_stub,insertAtIdx);

		// edit the new entry - we know the name is on the first line
		if( typeof tableManager.getLinkedTableManager() != 'undefined' ){
			$("table#" + tableManager.getLinkedTableManager().getTableId() +" > tbody > tr:first > td:eq(1)").trigger('click');
		}
	}
});
CellOperations.registerOperation(new LayoutAddRowOperation());


var LayoutColumnModel = BaseModel.extend({
	},{
		MODEL: 'LayoutColumn',

		getStub: function(){

			var _stub = {
				id: TableManager.generateGUID(),
				type: LayoutColumnModel.MODEL,
				typeDesc: "Column",
				parent: IndexManager.ROOTID,
				properties: []
			};

			_stub.properties.push(PropertiesManager.getProperty("name"));
			_stub.properties.push(PropertiesManager.getProperty("columnSpan"));
			_stub.properties.push(PropertiesManager.getProperty("columnPrepend"));
			_stub.properties.push(PropertiesManager.getProperty("columnAppend"));
			_stub.properties.push(PropertiesManager.getProperty("columnPrependTop"));
			_stub.properties.push(PropertiesManager.getProperty("columnAppendBottom"));
			_stub.properties.push(PropertiesManager.getProperty("columnBorder"));
			_stub.properties.push(PropertiesManager.getProperty("columnBigBorder"));
			_stub.properties.push(PropertiesManager.getProperty("backgroundColor"));
			_stub.properties.push(PropertiesManager.getProperty("roundCorners"));
			_stub.properties.push(PropertiesManager.getProperty("height"));
			_stub.properties.push(PropertiesManager.getProperty("cssClass"));
			_stub.properties.push(PropertiesManager.getProperty("textAlign"));


			return _stub;
		}
	});
BaseModel.registerModel(LayoutColumnModel);

var LayoutBootstrapColumnModel = BaseModel.extend({
}, {
  MODEL: 'LayoutBootstrapColumn',

  getStub: function () {

    var _stub = {
      id: TableManager.generateGUID(),
      type: LayoutBootstrapColumnModel.MODEL,
      typeDesc: "Column",
      parent: IndexManager.ROOTID,
      properties: []
    };

    _stub.properties.push(PropertiesManager.getProperty("name"));
    _stub.properties.push(PropertiesManager.getProperty("bootstrapExtraSmall"));
    _stub.properties.push(PropertiesManager.getProperty("bootstrapSmall"));
    _stub.properties.push(PropertiesManager.getProperty("bootstrapMedium"));
    _stub.properties.push(PropertiesManager.getProperty("bootstrapLarge"));
    _stub.properties.push(PropertiesManager.getProperty("bootstrapCssClass"));
    _stub.properties.push(PropertiesManager.getProperty("backgroundColor"));
    _stub.properties.push(PropertiesManager.getProperty("roundCorners"));
    _stub.properties.push(PropertiesManager.getProperty("height"));
    _stub.properties.push(PropertiesManager.getProperty("cssClass"));
    _stub.properties.push(PropertiesManager.getProperty("textAlign"));


    return _stub;
  }
});
BaseModel.registerModel(LayoutBootstrapColumnModel);

var LayoutAddColumnsOperation = AddRowOperation.extend({

	id: "LAYOUT_ADD_COLUMN",
	types: [LayoutRowModel.MODEL, LayoutColumnModel.MODEL, LayoutBootstrapColumnModel.MODEL],
	name: "Add Columns",
	description: "Add a new column",

	constructor: function(){
		this.logger = new Logger("LayoutAddColumnsOperation");
	},

	execute: function(tableManager) {

		// Add a row on the specified position;
		var _stub = (cdfdd.dashboardWcdf.rendererType == "bootstrap") 
			? LayoutBootstrapColumnModel.getStub()
			: LayoutColumnModel.getStub();

		var rowIdx;
		var colIdx = 0;
		var rowId;
		var rowType;
		var insertAtIdx = -1;

		var indexManager = tableManager.getTableModel().getIndexManager();

		if (tableManager.isSelectedCell){
			rowIdx = tableManager.getSelectedCell()[0];
			colIdx = tableManager.getSelectedCell()[1];
			rowId = tableManager.getTableModel().getEvaluatedId(rowIdx);
			rowType = tableManager.getTableModel().getEvaluatedRowType(rowIdx);

			var nextSibling = indexManager.getNextSibling(rowId);
			if (typeof nextSibling == 'undefined'){
				insertAtIdx = indexManager.getLastChild(rowId).index + 1;
			}
			else{
				insertAtIdx = nextSibling.index;
			}
			// Logic: If this is a LayoutRowModel.MODEL, insert after, same parent as this layout row;
			// if it's a LayoutColumnModel.MODEL, insert after, parent on the column; Anything else, add
			// to the end

			if(rowType == LayoutColumnModel.MODEL || rowType == LayoutBootstrapColumnModel.MODEL){
				_stub.parent = indexManager.getIndex()[rowId].parent;
			}
			else if (rowType == LayoutRowModel.MODEL){
				_stub.parent = rowId;
			}
			else{
				// insert at the end
				insertAtIdx = -1;
			}

		}
		else{
			insertAtIdx = tableManager.getTableModel().getData().length;
		}

		this.logger.debug("Inserting column after "+ rowType + " at " + insertAtIdx);
		tableManager.insertAtIdx(_stub,insertAtIdx);

		
		// edit the new entry - we know the name is on the first line
		if( typeof tableManager.getLinkedTableManager() != 'undefined' ){
			$("table#" + tableManager.getLinkedTableManager().getTableId() +" > tbody > tr:first > td:eq(1)").trigger('click');
		}
	}
});
CellOperations.registerOperation(new LayoutAddColumnsOperation());


var LayoutFreeFormModel = BaseModel.extend({
},{

  MODEL: 'LayoutFreeForm',

  getStub: function(){
    var _stub = {
      id: TableManager.generateGUID(),
      type: LayoutFreeFormModel.MODEL,
      typeDesc: "FreeForm",
      parent: IndexManager.ROOTID,
      properties: []
    };

    _stub.properties.push(PropertiesManager.getProperty("name"));
    _stub.properties.push(PropertiesManager.getProperty("cssClass"));
    _stub.properties.push(PropertiesManager.getProperty("elementTag"));
    _stub.properties.push(PropertiesManager.getProperty("otherAttributes"));

    return _stub;
  }
});
BaseModel.registerModel(LayoutFreeFormModel);

var LayoutAddFreeFormOperation = AddRowOperation.extend({

  id: "LAYOUT_ADD_FREEFORM",
  types: [],
  name: "Add FreeForm",
  description: "Adds a freeForm element to the template",

  constructor: function(){
    this.logger = new Logger("LayoutAddFreeFormOperation");
  },

  execute: function(tableManager) {

    var _stub = LayoutFreeFormModel.getStub();

    var rowIdx;
    var colIdx = 0;
    var rowId;
    var rowType;
    var insertAtIdx = -1;

    var indexManager = tableManager.getTableModel().getIndexManager();

    if (tableManager.isSelectedCell){
      rowIdx = tableManager.getSelectedCell()[0];
      colIdx = tableManager.getSelectedCell()[1];
      rowId = tableManager.getTableModel().getEvaluatedId(rowIdx);
      rowType = tableManager.getTableModel().getEvaluatedRowType(rowIdx);

      var nextSibling = indexManager.getNextSibling(rowId);
      if (typeof nextSibling == 'undefined'){
        insertAtIdx = indexManager.getLastChild(rowId).index + 1;
      }
      else{
        insertAtIdx = nextSibling.index;
      }
      // Logic: If this is a LayoutRowModel.MODEL, insert after, same parent as this layout row; 
      // if it's a LayoutColumnModel.MODEL, insert after, parent on the column; Anything else, add
      // to the end

      if( rowType == LayoutSpaceModel.MODEL || rowType == LayoutBootstrapPanelModel.MODEL ) {
        _stub.parent = indexManager.getIndex()[rowId].parent;
      }
      else if ( rowType == LayoutRowModel.MODEL || rowType == LayoutColumnModel.MODEL || rowType == LayoutFreeFormModel.MODEL ||
                rowType == LayoutBootstrapColumnModel.MODEL || rowType == LayoutBootstrapPanelHeaderModel.MODEL ||
                rowType == LayoutBootstrapPanelBodyModel.MODEL || rowType == LayoutBootstrapPanelFooterModel.MODEL) {
        _stub.parent = rowId;
      }
      else {
        // insert at the end
        insertAtIdx = tableManager.getTableModel().getData().length;
      }

    }
    else{
      insertAtIdx = tableManager.getTableModel().getData().length;
    }

    this.logger.debug("Inserting row after "+ rowType + " at " + insertAtIdx);

    tableManager.insertAtIdx(_stub,insertAtIdx);

    // edit the new entry - we know the name is on the first line
    if( typeof tableManager.getLinkedTableManager() != 'undefined' ){
      $("table#" + tableManager.getLinkedTableManager().getTableId() +" > tbody > tr:first > td:eq(1)").trigger('click');
    }
  }
});
CellOperations.registerOperation(new LayoutAddFreeFormOperation());


var LayoutSpaceModel = BaseModel.extend({
	},{
		MODEL: 'LayoutSpace',

		getStub: function(){

			var _stub = {
				id: TableManager.generateGUID(),
				type: LayoutSpaceModel.MODEL,
				typeDesc: "Space",
				parent: IndexManager.ROOTID,
				properties: []
			};

			_stub.properties.push(PropertiesManager.getProperty("height"));
			_stub.properties.push(PropertiesManager.getProperty("backgroundColor"));
			_stub.properties.push(PropertiesManager.getProperty("cssClass"));

			return _stub;
		}
	});
BaseModel.registerModel(LayoutSpaceModel);

var LayoutAddSpaceOperation = AddRowOperation.extend({

	id: "LAYOUT_ADD_SPACE",
	types: [LayoutRowModel.MODEL],
	name: "Add Space",
	description: "Adds a horizontal rule",

	constructor: function(){
		this.logger = new Logger("LayoutAddSpaceOperation");
	},

	execute: function(tableManager){

		// Add a row on the specified position;

		var _stub = LayoutSpaceModel.getStub();

		var indexManager = tableManager.getTableModel().getIndexManager();

		var rowIdx = tableManager.getSelectedCell()[0];
		var colIdx = tableManager.getSelectedCell()[1];
		var rowId = tableManager.getTableModel().getEvaluatedId(rowIdx);
		var insertAtIdx;

		var nextSibling = indexManager.getNextSibling(rowId);
		if (typeof nextSibling == 'undefined'){
			insertAtIdx = indexManager.getLastChild(rowId).index + 1;
		}
		else{
			insertAtIdx = nextSibling.index;
		}

		_stub.parent = indexManager.getIndex()[rowId].parent;

		this.logger.debug("Inserting space after at " + insertAtIdx);
		tableManager.insertAtIdx(_stub,insertAtIdx);
	}
});
CellOperations.registerOperation(new LayoutAddSpaceOperation());


var LayoutImageModel = BaseModel.extend({
	},{
		MODEL: 'LayoutImage',

		getStub: function(){

			var _stub = {
				id: TableManager.generateGUID(),
				type: LayoutImageModel.MODEL,
				typeDesc: "Image",
				parent: IndexManager.ROOTID,
				properties: []
			};

			_stub.properties.push(PropertiesManager.getProperty("url"));
			_stub.properties.push(PropertiesManager.getProperty("cssClass"));

			return _stub;
		}
	});
BaseModel.registerModel(LayoutImageModel);

var LayoutAddImageOperation = AddRowOperation.extend({

	id: "LAYOUT_ADD_IMAGE",
	types: [LayoutRowModel.MODEL,LayoutColumnModel.MODEL, LayoutBootstrapColumnModel.MODEL,
          LayoutFreeFormModel.MODEL, LayoutBootstrapPanelHeaderModel.MODEL,
          LayoutBootstrapPanelBodyModel.MODEL, LayoutBootstrapPanelFooterModel.MODEL],
	name: "Add Image",
	description: "Adds an image",

	constructor: function(){
		this.logger = new Logger("LayoutAddImageOperation");
	},

	execute: function(tableManager){

		// Add a row on the specified position;

		var _stub = LayoutImageModel.getStub();

		var indexManager = tableManager.getTableModel().getIndexManager();

		var rowIdx = tableManager.getSelectedCell()[0];
		var colIdx = tableManager.getSelectedCell()[1];
		var rowId = tableManager.getTableModel().getEvaluatedId(rowIdx);
		var insertAtIdx;

		var nextSibling = indexManager.getNextSibling(rowId);
		if (typeof nextSibling == 'undefined'){
			insertAtIdx = indexManager.getLastChild(rowId).index + 1;
		}
		else{
			insertAtIdx = nextSibling.index;
		}

		_stub.parent = rowId;

		this.logger.debug("Inserting space after at " + insertAtIdx);
		tableManager.insertAtIdx(_stub,insertAtIdx);

		// edit the new entry - we know the name is on the first line
		if( typeof tableManager.getLinkedTableManager() != 'undefined' ){
			$("table#" + tableManager.getLinkedTableManager().getTableId() +" > tbody > tr:first > td:eq(1)").trigger('click');
		}
	}
});
CellOperations.registerOperation(new LayoutAddImageOperation());


var LayoutHtmlModel = BaseModel.extend({
	},{
		MODEL: 'LayoutHtml',

		getStub: function(){

			var _stub = {
				id: TableManager.generateGUID(),
				type: LayoutHtmlModel.MODEL,
				typeDesc: "Html",
				parent: IndexManager.ROOTID,
				properties: []
			};

			_stub.properties.push(PropertiesManager.getProperty("name"));
			_stub.properties.push(PropertiesManager.getProperty("html"));
			_stub.properties.push(PropertiesManager.getProperty("color"));
			_stub.properties.push(PropertiesManager.getProperty("fontSize"));
			_stub.properties.push(PropertiesManager.getProperty("cssClass"));

			return _stub;
		}
	});
BaseModel.registerModel(LayoutHtmlModel);

var LayoutAddHtmlOperation = AddRowOperation.extend({

	id: "LAYOUT_ADD_HTML",
	types: [LayoutRowModel.MODEL,LayoutColumnModel.MODEL, LayoutBootstrapColumnModel.MODEL,
        	LayoutBootstrapPanelHeaderModel.MODEL, LayoutBootstrapPanelBodyModel.MODEL,
        	LayoutBootstrapPanelFooterModel.MODEL, LayoutFreeFormModel.MODEL],
	name: "Add Html",
	description: "Adds plain Html code to the template",

	constructor: function(){
		this.logger = new Logger("LayoutAddHtmlOperation");
	},

	execute: function(tableManager){

		// Add a row on the specified position;

		var _stub = LayoutHtmlModel.getStub();

		var indexManager = tableManager.getTableModel().getIndexManager();

		var rowIdx = tableManager.getSelectedCell()[0];
		var colIdx = tableManager.getSelectedCell()[1];
		var rowId = tableManager.getTableModel().getEvaluatedId(rowIdx);
		var insertAtIdx;

		var nextSibling = indexManager.getNextSibling(rowId);
		if (typeof nextSibling == 'undefined'){
			insertAtIdx = indexManager.getLastChild(rowId).index + 1;
		}
		else{
			insertAtIdx = nextSibling.index;
		}

		_stub.parent = rowId;

		this.logger.debug("Inserting space after at " + insertAtIdx);

		tableManager.insertAtIdx(_stub,insertAtIdx);


		// edit the new entry - we know the name is on the first line
		if( typeof tableManager.getLinkedTableManager() != 'undefined' ){
			$("table#" + tableManager.getLinkedTableManager().getTableId() +" > tbody > tr:first > td:eq(1)").trigger('click');
		}
	}
});
CellOperations.registerOperation(new LayoutAddHtmlOperation());


var LayoutResourceModel = BaseModel.extend({
	},{
		MODEL: 'LayoutResource',

		getStub: function(){

			var _stub = {
				id: TableManager.generateGUID(),
				type: LayoutResourceModel.MODEL,
				typeDesc: "Resource",
				parent: IndexManager.ROOTID,
				properties: []
			};

			_stub.properties.push(PropertiesManager.getProperty("name"));
			_stub.properties.push(PropertiesManager.getProperty("resourceType"));
			_stub.properties.push(PropertiesManager.getProperty("resourceFile"));
			_stub.properties.push(PropertiesManager.getProperty("resourceCode"));

			return _stub;
		}
	});
BaseModel.registerModel(LayoutResourceModel);

var LayoutResourceCodeModel = LayoutResourceModel.extend({
},{	
	MODEL: 'LayoutResourceCode',
	
	getStub: function(){
		var _stub = LayoutResourceModel.getStub();
		_stub.type = this.MODEL;
		_stub.properties = [];
		_stub.properties.push(PropertiesManager.getProperty("name"));
		_stub.properties.push(PropertiesManager.getProperty("resourceType"));
		_stub.properties.push(PropertiesManager.getProperty("resourceCode"));
		return _stub;
	}
});
BaseModel.registerModel(LayoutResourceCodeModel);

var LayoutResourceFileModel = LayoutResourceModel.extend({
},{
	MODEL: 'LayoutResourceFile',
	
	getStub: function(){
		var _stub = LayoutResourceModel.getStub();
		_stub.type = this.MODEL;
		_stub.properties = [];
		_stub.properties.push(PropertiesManager.getProperty("name"));
		_stub.properties.push(PropertiesManager.getProperty("resourceType"));
		_stub.properties.push(PropertiesManager.getProperty("resourceFile"));
		return _stub;
	}
});
BaseModel.registerModel(LayoutResourceFileModel);

var LayoutAddResourceOperation = AddRowOperation.extend({

	id: "LAYOUT_ADD_RESOURCE",
	types: [],
	name: "Add Resource",
	description: "Adds a resource external file or code to the dashboard",

	constructor: function(){
		this.logger = new Logger("LayoutAddResourceOperation");
	},

	execute: function(tableManager){

		// Add a row. This special type goes always to the beginning;
		
		$.prompt('<h2>Add Resource</h2><hr>Resource Type:&nbsp;&nbsp;<select id="resourceType"><option value="Css">Css</option><option value="Javascript">Javascript</option></select>\n' +
'							 <select id="resourceSource"><option value="file">External File</option><option value="code">Code Snippet</option></select>',
		{buttons: { Ok: true, Cancel: false },
		prefix: "popup",
		submit: function(v){
			if(v){
				
				var resourceType = $("#resourceType").val();
				var resourceSource = $("#resourceSource").val();
				
				var _stub = (resourceSource == 'file')? LayoutResourceFileModel.getStub() : LayoutResourceCodeModel.getStub();
				
				//var _stub = LayoutResourceModel.getStub();
				
				_stub.properties[1].value = resourceType;
				
				var indexManager = tableManager.getTableModel().getIndexManager();

				var insertAtIdx = 0;

				tableManager.insertAtIdx(_stub,insertAtIdx);

				// edit the new entry - we know the name is on the first line
				if( typeof tableManager.getLinkedTableManager() != 'undefined' ){
					$("table#" + tableManager.getLinkedTableManager().getTableId() +" > tbody > tr:first > td:eq(1)").trigger('click');
				}
			}
		}});
	}
});
CellOperations.registerOperation(new LayoutAddResourceOperation());


var LayoutBootstrapPanelModel = BaseModel.extend({},{
  MODEL: 'BootstrapPanel',
  getStub: function() {
    var _stub = {
      id: TableManager.generateGUID(),
      type: LayoutBootstrapPanelModel.MODEL,
      typeDesc: "Bootstrap Panel",
      parent: IndexManager.ROOTID,
      properties: []
    };

    _stub.properties.push(PropertiesManager.getProperty("name"));
    _stub.properties.push(PropertiesManager.getProperty("height"));
    _stub.properties.push(PropertiesManager.getProperty("roundCorners"));
    _stub.properties.push(PropertiesManager.getProperty("cssClass"));
    _stub.properties.push(PropertiesManager.getProperty("bootstrapPanelStyle"));

    return _stub;
  }

});
BaseModel.registerModel(LayoutBootstrapPanelModel);

var LayoutAddBootstrapPanelOperation = AddRowOperation.extend({

  id: "LAYOUT_ADD_BOOTSTRAP_PANEL",
  types: [],
  name: "Add Bootstrap Panel",
  description: "Adds a bootstrap panel",

  constructor: function(){
    this.logger = new Logger("LayoutAddBootstrapPanelOperation");
  },

  execute: function(tableManager){

    var _stub = LayoutBootstrapPanelModel.getStub();
    var indexManager = tableManager.getTableModel().getIndexManager();
    var header = new LayoutAddBootstrapPanelHeaderOperation();
    var body = new LayoutAddBootstrapPanelBodyOperation();
    var footer = new LayoutAddBootstrapPanelFooterOperation();
    var insertAtIdx = -1;

    if (tableManager.isSelectedCell) {
      var rowIdx = tableManager.getSelectedCell()[0];
      var rowId = tableManager.getTableModel().getEvaluatedId(rowIdx);
      var rowType = tableManager.getTableModel().getEvaluatedRowType(rowIdx);

      var nextSibling = indexManager.getNextSibling(rowId);
      if (typeof nextSibling == 'undefined'){
        insertAtIdx = indexManager.getLastChild(rowId).index + 1;
      }
      else{
        insertAtIdx = nextSibling.index;
      }

      if( rowType == LayoutSpaceModel.MODEL ) {
        _stub.parent = indexManager.getIndex()[rowId].parent;
      } else if ( rowType == LayoutBootstrapPanelHeaderModel.MODEL || rowType == LayoutBootstrapPanelBodyModel.MODEL ||
                  rowType == LayoutBootstrapPanelFooterModel.MODEL || rowType == LayoutRowModel.MODEL ||
                  rowType == LayoutColumnModel.MODEL || rowType == LayoutBootstrapColumnModel.MODEL ||
                  rowType == LayoutFreeFormModel.MODEL) {
        _stub.parent = rowId;
      } else {
        insertAtIdx = tableManager.getTableModel().getData().length;
      }

    } else {
      insertAtIdx = tableManager.getTableModel().getData().length;
    }

    this.logger.debug("Inserting bootstrap node after at " + insertAtIdx);
    tableManager.insertAtIdx(_stub, insertAtIdx);

    header.execute(tableManager);
    body.execute(tableManager);
    footer.execute(tableManager);


  }
});
CellOperations.registerOperation(new LayoutAddBootstrapPanelOperation());


var LayoutDuplicateOperation = DuplicateOperation.extend({

  id: "LAYOUT_DUPLICATE",
  types: [LayoutRowModel.MODEL, LayoutColumnModel.MODEL, LayoutBootstrapColumnModel.MODEL, LayoutSpaceModel.MODEL,
      LayoutImageModel.MODEL, LayoutHtmlModel.MODEL, LayoutCarouselModel.MODEL, FilterBlockModel.MODEL,
      FilterRowModel.MODEL, FilterHeaderModel.MODEL, LayoutResourceModel.MODEL, LayoutBootstrapPanelModel.MODEL,
      LayoutFreeFormModel.MODEL],
  name: "Duplicate Layout Element",
  description: "Duplicate the selected Element and all its children",

  constructor: function () {
    this.logger = new Logger("LayoutDuplicateOperation");
  },

  canExecute: function(tableManager) {
    var rowIdx = tableManager.getSelectedCell()[0],
        rowType = tableManager.getTableModel().getEvaluatedRowType(rowIdx),
        isBootstrapPanelChild = rowType == LayoutBootstrapPanelBodyModel.MODEL
            || rowType == LayoutBootstrapPanelFooterModel.MODEL || rowType == LayoutBootstrapPanelHeaderModel.MODEL;

    return tableManager.isSelectedCell && !isBootstrapPanelChild;
  }
});
CellOperations.registerOperation(new LayoutDuplicateOperation);


var LayoutDeleteOperation = DeleteOperation.extend({

		id: "LAYOUT_DELETE",
		types: [LayoutRowModel.MODEL, LayoutColumnModel.MODEL, LayoutBootstrapColumnModel.MODEL,
                LayoutSpaceModel.MODEL, LayoutImageModel.MODEL, LayoutHtmlModel.MODEL,
                LayoutResourceModel.MODEL, LayoutCarouselModel.MODEL, FilterBlockModel.MODEL,
                FilterRowModel.MODEL, FilterHeaderModel.MODEL, LayoutBootstrapPanelModel.MODEL,
                LayoutFreeFormModel.MODEL],

		constructor: function(){
			this.logger = new Logger("LayoutDeleteOperation");
		}

});
CellOperations.registerOperation(new LayoutDeleteOperation);


var LayoutApplyTemplateOperation = ApplyTemplateOperation.extend({

		id: "LAYOUT_ADD_TEMPLATE",

		constructor: function(){
			this.logger = new Logger("LayoutApplyTemplateOperation");
		},

		execute: function(tableManager){
			
			this.logger.info("Loading templates...");

			var loadParams = {operation:"load"} 

			SynchronizeRequests.doGetJson(loadParams);
			
		}
});


var LayoutSaveAsTemplateOperation = SaveAsTemplateOperation.extend({

		id: "LAYOUT_SAVEAS_TEMPLATE",

		constructor: function(){
			this.logger = new Logger("LayoutSaveAsTemplateOperation");
		},

		execute: function(tableManager){
			var file = "";
			var title = "Custom Template";
			var includeComponents = true;
			var includeDataSources = true;
			var myself = this;
			var content = '\n' +
'				<span><h2>Save as Template</h2></span><br/><hr/>\n' +
'				<span id="fileLabel" >File Name:</span><br/><input class="cdf_settings_input" id="fileInput" type="text" value="" style="width:100%;"></input><br/>\n' +
'				<span>Title:</span><br/><input class="cdf_settings_input" id="titleInput" type="text" value=""style="width:100%;"></input><br>\n' +
'				<span>Include Components:</span><input type="checkbox" checked="yes" id="includeComponentsInput" value="true" />\n' +
'				&nbsp&nbsp<span>Include Datasources:</span><input type="checkbox" checked="yes" id="includeDataSourcesInput" value="true" />';

			$.prompt(content, {buttons: { Save: true, Cancel: false }, prefix: "popup",
				submit: function (v) {
					title = $("#titleInput").val();
					file = $("#fileInput").val();
					includeComponents = $("#includeComponentsInput").attr("checked");
					includeDataSources = $("#includeDataSourcesInput").attr("checked");

					var validate = true
					if (file.length == 0) {
						$("#fileLabel").css("color", "red");
						$("#fileLabel").text("* File Name: (required)");
						validate = false;
					}

					if (file.indexOf(".") != -1 && (file.length < 6 || file.lastIndexOf(".cdfde") != file.length - 6)) {
						$("#fileLabel").css("color", "red");
						$("#fileLabel").text("* File Name: (Invalid file extension. Must be .cdfde)");
						validate = false;
					}

					if (file.indexOf(".") == -1)
						file += ".cdfde";

					return !v || validate;
				},
				callback: function (v, m, f) {
					if (v) {
						myself.logger.info("Saving template...");

						var template = cdfdd.getDashboardData();
						template.layout.title = title;
						if (!includeComponents) {
							template.components.rows = [];

						}
						if (!includeDataSources) {
							template.datasources.rows = [];
						}

						//var templateParams = {operation:"save", file:file, cdfstructure: JSON.toJSONString(template,true)} ;
						var templateParams = {operation: "save", file: file, cdfstructure: JSON.stringify(template)};

						SynchronizeRequests.doPost(templateParams);
					}
				}
			});
		}
});
