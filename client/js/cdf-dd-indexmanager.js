
// IndexManager

var IndexManager = Base.extend({

		id: "",
		logger: {},
		index: {},
		
		

		constructor: function(tableModel){
			this.logger = new Logger("IndexManager");
			this.setTableModel(tableModel);
		},

		
		updateIndex: function(){

			var data = this.getTableModel().getData();
			var idx = {};
			var index = 0;

			// Add root entry
			var root = {};
			root.id = IndexManager.ROOTID;
			root.parent = null;
			root.children = [];
			root.type = _type;
			root.index = index;
			idx[IndexManager.ROOTID] = root; 

			for ( var rowIndex in data){
				var row = data[rowIndex];
				var _id = this.getTableModel().getEvaluatedId(rowIndex);
				var _parent;
				if (typeof this.getTableModel().getParentId() == 'undefined'){
					// Parent folder
					_parent = IndexManager.ROOTID;
				}
				else{
					// Child folder
					_parent = this.getTableModel().getParentId()(row);
					if (typeof _parent == 'undefined'){
						_parent = IndexManager.ROOTID;
					}
				}
				var _type = this.getTableModel().getRowType()(row) ;

				var entry = {};
				entry.id = _id;
				entry.parent = _parent;
				entry.children = [];
				entry.type = _type;
				entry.index = index;
				idx[_id] = entry; 
				idx[_parent].children.push(entry);

				index++;
			};

			this.setIndex(idx);

		},

		isFirstChild: function(rowId){
		
			return this.getIndex()[this.getIndex()[rowId].parent].children[0].id === rowId;
		},

		isLastChild: function(rowId){
			var _brothers = this.getBrothers(rowId);
			return _brothers[_brothers.length-1].id === rowId;

		},

		getBrothers: function(rowId){
			return this.getIndex()[this.getIndex()[rowId].parent].children;
		
		},

		getChildIndex: function(rowId){
			
			var brothers = this.getBrothers(rowId);
			var idx = -1;
			$.each(brothers,function(i,brother){
					if (brothers[i].id === rowId){
						idx = i;
						return false;
					}
				});
			return idx;
		
		},

		getLastChild: function(rowId){
			
			var _children = this.getIndex()[rowId].children;
			var _length = _children.length;
			if (_length == 0){
				return this.getIndex()[rowId];
			}
			else{
				return this.getLastChild(_children[_length-1].id);
			}
		
		},


		getPreviousSibling: function(rowId){
			return this.getBrothers(rowId)[this.getChildIndex(rowId) - 1];
		},

		getNextSibling: function(rowId){
			return this.getBrothers(rowId)[this.getChildIndex(rowId) + 1];
		},

		setIndex: function(index){this.index = index},
		getIndex: function(){return this.index},
		setTableModel: function(tableModel){this.tableModel = tableModel; this.updateIndex()},
		getTableModel: function(){return this.tableModel}

	},{
		ROOTID: "UnIqEiD"
	});


