
// Entry base model

var BaseModel = Base.extend({

		


	},{
		MODEL: 'BaseModel',

		getStub: function(){
			return {}
		},

		models: {},

		registerModel: function(_class){
			this.models[_class.MODEL]=_class;
		},

		getModel: function(_model){
			return this.models[_model];
		}

	});


var CellOperations = Base.extend({

		logger: {},

		constructor: function(){
			this.logger = new Logger("BaseType");
		}
},{

		operations: [],


		// After defining an operation. we need to register it
		registerOperation: function(operation){
			this.operations.push(operation);
		},

		getOperationsByType: function(type){
		
			var _operations = [];

			$.each(CellOperations.operations,function(i,value){
			
					for(var i in value.types){
						if(type.match("^"+value.types[i])){
							_operations.push(value);
						}
					}
				});
			return _operations;
		}

});

var BaseOperation = Base.extend({

		id: "BASE_OPERATION",
		types: ["TYPE"],
		name: "Base operation",
		description: "Base Operation description",
		icon: "/pentaho/content/pentaho-cdf-dd/getResource?resource=/images/toolbar-folder-add-48x48.png",
		order: 20,
		logger: {},
		hoverIcon: null, //icon to display on hover
		clickIcon: null, //icon while clicking
		showInactiveIcon: false, //show icon when !canExecute

		execute: function(tableManager){
			this.logger.error("Method not implemented; " + tableManager.getTableId() + "; " + tableManager.getSelectedCell());
		},

		canExecute: function(tableModel){
		
			return true;
		},

		constructor: function(){
			this.logger = new Logger("BaseOperation");
		},

		getHtml: function(tableManager,idx){

			var code ='';
			var tableManagerId = tableManager.getTableId();

			if (this.canExecute(tableManager)){
				
				var mouseHoverCmd = '';
				if(this.hoverIcon){
					mouseHoverCmd = 'onmouseover="$(this).attr(\'src\',\'' + this.hoverIcon + '\')" onmouseout="$(this).attr(\'src\',\'' + this.getIcon() + '\')"';
				}
				var mouseClickCmd ='';
				if(this.clickIcon){
					mouseClickCmd = 'onmousedown="$(this).attr(\'src\',\'' + this.clickIcon + '\')" onmouseup="$(this).attr(\'src\',\'' + this.getIcon() + '\')"';
				}
				
				code = '\n' +
'				<a class="tooltip" title="' + this.getName() + '"  href="javascript:TableManager.executeOperation(\'' + tableManagerId + '\','+ idx+');">\n' +
'				<img border="0" src="'+ this.getIcon() +'" class="cdfdd"' + mouseHoverCmd + ' ' + mouseClickCmd + ' ></img>\n' +
'				</a>\n' +
'				';
			}
			else if (this.showInactiveIcon) {
				var _icon = this.getIcon().replace(/(.*)\//,"$1/X");// "../iconName.png -> ../XiconName.png
					
				code = '\n' +
'				<img border="0" alt="' + this.getName() + '" src="'+ _icon +'" class="cdfdd"></img>\n' +
'				';
			}

			return code;
		
		},

		getId: function(){return this.id},
		setId: function(id){this.id = id},
		getName: function(){return this.name},
		setName: function(name){this.name = name},
		getDescription: function(){return this.description},
		setDescription: function(description){this.description = description},
		getIcon: function(){return this.icon},
		setIcon: function(icon){this.icon = icon}

});


var AddRowOperation = BaseOperation.extend({

		id: "ADD_ROW",
		types: ["GenericRow"],
		name: "New Row",
		description: "Adds a new row to the layout on the specific position",
		icon: "/pentaho/content/pentaho-cdf-dd/getResource?resource=/images/NAV/addrow.png",

		constructor: function(){
			this.logger = new Logger("AddRowOperation");
		}

});

CellOperations.registerOperation(new AddRowOperation());

var MoveUpOperation = BaseOperation.extend({

		id: "MOVE_UP",
		types: ["GenericMoveUp"],
		name: "Move Up",
		description: "Move up",
		icon: "/pentaho/content/pentaho-cdf-dd/getResource?resource=/images/NAV/up.png",
		hoverIcon: "/pentaho/content/pentaho-cdf-dd/getResource?resource=/images/NAV/up_mouseover.png",
		clickIcon: "/pentaho/content/pentaho-cdf-dd/getResource?resource=/images/NAV/up_onclick.png",

		constructor: function(){
			this.logger = new Logger("MoveUpOperation");
		},

		canExecute: function(tableManager){
		
			var rowIdx = tableManager.getSelectedCell()[0];
			var rowId = tableManager.getTableModel().getEvaluatedId(rowIdx);

			return !tableManager.getTableModel().getIndexManager().isFirstChild(rowId);

		},

		execute: function(tableManager){

			// Move up: move the selected node and all children
			// up to the previous item

			var rowIdx = tableManager.getSelectedCell()[0];
			var colIdx = tableManager.getSelectedCell()[1];
			var rowId = tableManager.getTableModel().getEvaluatedId(rowIdx);

			var fromIdx = rowIdx;
			var toIdx = -1;

			var nextSibling = tableManager.getTableModel().getIndexManager().getNextSibling(rowId);
			if (typeof nextSibling == 'undefined'){
				toIdx = tableManager.getTableModel().getIndexManager().getLastChild(rowId).index;
			}
			else{
				toIdx = nextSibling.index - 1;
			}
			var targetIdx = tableManager.getTableModel().getIndexManager().getPreviousSibling(rowId).index;

			this.logger.debug("Moving nodes from " + fromIdx + " to " + toIdx + " to the place of " + targetIdx);

			// Build a new data array
			var _data = tableManager.getTableModel().getData();
			var _tmp = _data.splice(fromIdx, toIdx-fromIdx + 1);

			_data.splice(targetIdx,0)
			Array().splice.apply(_data,[targetIdx,0].concat(_tmp));
			//_data = _data.slice(0,targetIdx).concat(_tmp).concat(_data.slice(targetIdx));
			tableManager.getTableModel().setData(_data);

			// Now do the same on the UI

			// move rows id: fromIdx -> toIdx to targetIdx
			for(var i = 0; i<= toIdx-fromIdx; i++){
				$('#'+tableManager.getTableId() + " > tbody > tr:eq("+ (targetIdx + i) +")").before(
					$('#'+tableManager.getTableId() + " > tbody > tr:eq("+ (fromIdx + i) +")")
				);
			}

			tableManager.setSelectedCell([targetIdx,colIdx]);
			tableManager.updateOperations();
			
			
			/*
			tableManager.cleanSelections();
			tableManager.init();
			tableManager.selectCell(targetIdx,colIdx);
			*/

			var a = [];
			$.each(_data,function(i,row){
				a.push(row.id);
			})
			this.logger.debug("Result: " + a.join(', '));
		
		}

});

CellOperations.registerOperation(new MoveUpOperation());

var MoveDownOperation = BaseOperation.extend({

		id: "MOVE_DOWN",
		types: ["GenericMoveDown"],
		name: "Move Down",
		description: "Move down",
		icon: "/pentaho/content/pentaho-cdf-dd/getResource?resource=/images/NAV/down.png",
		hoverIcon: "/pentaho/content/pentaho-cdf-dd/getResource?resource=/images/NAV/down_mouseover.png",
		clickIcon: "/pentaho/content/pentaho-cdf-dd/getResource?resource=/images/NAV/down_onclick.png",

		constructor: function(){
			this.logger = new Logger("MoveDownOperation");
		},

		canExecute: function(tableManager){
		
			var rowIdx = tableManager.getSelectedCell()[0];
			var rowId = tableManager.getTableModel().getEvaluatedId(rowIdx);

			return !tableManager.getTableModel().getIndexManager().isLastChild(rowId);

		},


		execute: function(tableManager){

			// Move up: move the selected node and all children
			// up to the previous item

			var rowIdx = tableManager.getSelectedCell()[0];
			var colIdx = tableManager.getSelectedCell()[1];
			var rowId = tableManager.getTableModel().getEvaluatedId(rowIdx);

			var fromIdx = rowIdx;
			var toIdx = -1;

			var indexManager = tableManager.getTableModel().getIndexManager();
			var nextSibling = indexManager.getNextSibling(rowId);
			if (typeof nextSibling == 'undefined'){
				toIdx = indexManager.getLastChild(rowId).index;
			}
			else{
				toIdx = nextSibling.index - 1;
			}
			var targetIdx = indexManager.getLastChild(indexManager.getNextSibling(rowId).id).index;

			this.logger.debug("Moving nodes from " + fromIdx + " to " + toIdx + " to the place of " + targetIdx);

			// Build a new data array
			var _data = tableManager.getTableModel().getData();
			var _tmp = _data.splice(fromIdx, toIdx-fromIdx + 1);

			Array().splice.apply(_data,[targetIdx-toIdx+fromIdx,0].concat(_tmp));
			//_data = _data.slice(0,targetIdx-toIdx+fromIdx).concat(_tmp).concat(_data.slice(targetIdx-toIdx+fromIdx));
			tableManager.getTableModel().setData(_data);

			// Now do the same on the UI

			// move rows id: fromIdx -> toIdx to targetIdx
			for(var i = 0; i<= toIdx-fromIdx; i++){
				$('#'+tableManager.getTableId() + " > tbody > tr:eq("+ (targetIdx) +")").after(
					$('#'+tableManager.getTableId() + " > tbody > tr:eq("+ (fromIdx) +")")
				);
			}

			tableManager.setSelectedCell([targetIdx-toIdx+fromIdx,colIdx]);
			tableManager.updateOperations();
			
			/*
			tableManager.cleanSelections();
			tableManager.init();
			tableManager.selectCell(targetIdx-toIdx+fromIdx,colIdx);
			*/

			var a = [];
			$.each(_data,function(i,row){
				a.push(row.id);
			})
			this.logger.debug("Result: " + a.join(', '));
		
		}

});

CellOperations.registerOperation(new MoveDownOperation());

var DeleteOperation = BaseOperation.extend({

		id: "Delete",
		types: ["GenericDelete"],
		name: "Delete",
		description: "Delete",
		icon: "/pentaho/content/pentaho-cdf-dd/getResource?resource=/images/NAV/remove.png",
		hoverIcon: "/pentaho/content/pentaho-cdf-dd/getResource?resource=/images/NAV/remove_mouseover.png",
		clickIcon: "/pentaho/content/pentaho-cdf-dd/getResource?resource=/images/NAV/remove_onclick.png",

		constructor: function(){
			this.logger = new Logger("DeleteOperation");
		},


		execute: function(tableManager){

			// Move up: move the selected node and all children
			// up to the previous item

			var rowIdx = tableManager.getSelectedCell()[0];
			var colIdx = tableManager.getSelectedCell()[1];
			var rowId = tableManager.getTableModel().getEvaluatedId(rowIdx);

			var fromIdx = rowIdx;
			var toIdx = -1;

			var indexManager = tableManager.getTableModel().getIndexManager();
			var nextSibling = indexManager.getNextSibling(rowId);
			if (typeof nextSibling == 'undefined'){
				toIdx = indexManager.getLastChild(rowId).index;
			}
			else{
				toIdx = nextSibling.index - 1;
			}
			
			// Store the parent to update the table
			var _parentId = indexManager.getIndex()[rowId].parent;
			
			//check if last in group, except in layout
			var deleteParent = tableManager.id != LayoutPanel.TREE &&
			                   _parentId != IndexManager.ROOTID && 
			                   indexManager.isFirstChild(rowId) && 
			                   indexManager.isLastChild(rowId);
			if(deleteParent){
				//start deleting in parent
				fromIdx = indexManager.getIndex()[_parentId].index;
				//update grandpa
				_parentId = indexManager.getIndex()[_parentId].parent;
			}
			
			this.logger.debug("Deleting nodes from " + fromIdx + " to " + toIdx );
			
			// Build a new data array
			tableManager.getTableModel().getData().splice(fromIdx, toIdx-fromIdx + 1);
			indexManager.updateIndex();


			// Now do the same on the UI

			// move rows id: fromIdx -> toIdx to targetIdx
			for(var i = 0; i<= toIdx-fromIdx; i++){
				$('#'+tableManager.getTableId() + " > tbody > tr:eq("+ (fromIdx) +")").remove();
			}

			tableManager.cellUnselected();
			
			/*
			tableManager.cleanSelections();
			tableManager.init();
			tableManager.selectCell(targetIdx-toIdx+fromIdx,colIdx);
			*/

			var a = [];
			$.each(tableManager.getTableModel().getData(),function(i,row){
				a.push(row.id);
			})
			this.logger.debug("Result: " + a.join(', '));

			// Update treeTable:
			tableManager.updateTreeTable(_parentId);
		
		}

});

CellOperations.registerOperation(new DeleteOperation());


var ApplyTemplateOperation = BaseOperation.extend({

		id: "APPLY_TEMPLATE",
		types: ["GenericApplyTemplate"],
		name: "Apply Template",
		description: "Applys a template.",
		icon: "/pentaho/content/pentaho-cdf-dd/getResource?resource=/images/NAV/loadtemp.png",
		hoverIcon: "/pentaho/content/pentaho-cdf-dd/getResource?resource=/images/NAV/loadtemp_mouseover.png",
		clickIcon: "/pentaho/content/pentaho-cdf-dd/getResource?resource=/images/NAV/loadtemp_onclick.png",

		constructor: function(){
			this.logger = new Logger("ApplyTemplateOperation");
		}

});

CellOperations.registerOperation(new ApplyTemplateOperation());

var SaveAsTemplateOperation = BaseOperation.extend({

		id: "SAVEAS_TEMPLATE",
		types: ["GenericSaveAsTemplate"],
		name: "Save as Template",
		description: "Save sa template.",
		icon: "/pentaho/content/pentaho-cdf-dd/getResource?resource=/images/NAV/savetemp.png",
		hoverIcon: "/pentaho/content/pentaho-cdf-dd/getResource?resource=/images/NAV/savetemp_mouseover.png",
		clickIcon: "/pentaho/content/pentaho-cdf-dd/getResource?resource=/images/NAV/savetemp_onclick.png",

		constructor: function(){
			this.logger = new Logger("SaveAsTemplateOperation");
		}

});

CellOperations.registerOperation(new SaveAsTemplateOperation());
