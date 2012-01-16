var PalleteManager = Base.extend({

		id: "",
		palleteId: "",
		logger: {},
		linkedTableManager: undefined,
		categories: {},
		entries: {},

		constructor: function(id){
			this.logger = new Logger("PalleteManager - " + id);
			this.id = id;
			this.setPalleteId(id+"Pallete");

			$("#"+this.id).append(this.newPallete());

			// Register this tablemanager in the global area
			PalleteManager.register(this);
		},

		init : function(){

			this.render();

		},

		reset: function(){
			$("#"+this.id).empty();
		},

		newPallete: function(args){

//TODO: temp changed rem <div style = "height: 24px;" />
			var _container = '<div id="' + this.getPalleteId() + '"></div>';
			return _container;
		},


		render: function(){

			this.logger.debug("Rendering pallete " + this.getPalleteId());
			var _placeholder = $("#"+this.getPalleteId());

			// Accordion
			_placeholder.accordion({
					header: "h3",
					active: false,
					autoHeight: false,
					//event: "mouseover",
					icons: {
						header: "ui-icon-triangle-1-e",
						headerSelected: "ui-icon-triangle-1-s"
					}
				});

		},

		addEntry: function(palleteEntry){

			this.getEntries()[palleteEntry.getId()] = palleteEntry;

			var _cat = palleteEntry.getCategory();
			if(typeof this.getCategories()[_cat] == 'undefined' ){
				this.createCategory(palleteEntry);
			}
			this.getCategories()[_cat].push(palleteEntry);

			var _placeholder = $(".pallete-" + _cat +" > ul ", $("#"+this.getPalleteId()) );
//TODO: this hover ain't pretty, change this...
//			<li onmouseover="$(this).addClass(\'ui-state-hover\')" onmouseout="$(this).removeClass(\'ui-state-hover\')" ><a class="tooltip" title="' + palleteEntry.getDescription() + '"  href="javascript:PalleteManager.executeEntry(\'' + this.getPalleteId() + '\',\''+ palleteEntry.getId() +'\');">
			var code = '\n' +
'							<li><a class="tooltip" title="' + palleteEntry.getDescription() + '"  href="javascript:PalleteManager.executeEntry(\'' + this.getPalleteId() + '\',\''+ palleteEntry.getId() +'\');">\n' +
'			'+ palleteEntry.getName() +'\n' +
'			</a>\n' +
'			';

			_placeholder.append(code)
		
		
		},

		createCategory: function(palleteEntry){
			
			$("#"+this.getPalleteId()).append('<div><h3><a href="#">' + palleteEntry.getCategoryDesc() + '</a></h3><div class="pallete-'+palleteEntry.getCategory()+'"><ul></ul></div></div>');

			this.getCategories()[palleteEntry.getCategory()] = [];
					
		},


		// Accessors
		setId: function(id){this.id = id},
		getId: function(){return this.id},
		setPalleteId: function(palleteId){this.palleteId = palleteId},
		getPalleteId: function(){return this.palleteId},
		getLinkedTableManager: function(){return this.linkedTableManager},
		setLinkedTableManager: function(linkedTableManager){this.linkedTableManager = linkedTableManager},
		setCategories: function(categories){this.categories = categories},
		getCategories: function(){return this.categories},
		setEntries: function(entries){this.entries = entries},
		getEntries: function(){return this.entries}

	},{
		palleteManagers: {},
		
		register: function(palleteManager){
			PalleteManager.palleteManagers[palleteManager.getPalleteId()] = palleteManager;
		},

		getPalleteManager: function(id){
			return PalleteManager.palleteManagers[id];
		},

		executeEntry: function(palleteManagerId,id){
			
			var palleteManager = PalleteManager.getPalleteManager(palleteManagerId);
			palleteManager.getEntries()[id].execute(palleteManager);
		}
	});



var PalleteEntry = Base.extend({

		id: "PALLETE_ENTRY",
		name: "Base operation",
		description: "Base Operation description",
		icon: "/pentaho/content/pentaho-cdf-dd/getResource?resource=/images/toolbar-folder-add-48x48.png",
		category: undefined,
		categoryDesc: undefined,
		logger: {},

		constructor: function(){
			this.logger = new Logger("BaseOperation");
		},

		execute: function(palleteManager,stub){
			// Add a new entry

			var tableManager = palleteManager.getLinkedTableManager();

			var _stub = stub != undefined ? stub : this.getStub();

			var rowIdx;
			var colIdx = 0;
			var rowId;
			var rowType;
			var insertAtIdx = -1;

			var indexManager = tableManager.getTableModel().getIndexManager();

			// insertAtIdx = tableManager.getTableModel().getData().length;
			insertAtIdx = tableManager.createOrGetParent(this.category, this.categoryDesc);
			_stub.parent=this.category;
			

			this.logger.debug("Inserting row after "+ rowType + " at " + insertAtIdx);
			
			tableManager.insertAtIdx(_stub,insertAtIdx);

			// focus the newly created line
			//atableManager.selectCell(insertAtIdx,colIdx);

			// edit the new entry - we know the name is on the first line
			if( typeof tableManager.getLinkedTableManager() != 'undefined' ){
				$("table#" + tableManager.getLinkedTableManager().getTableId() +" > tbody > tr:first > td:eq(1)").trigger('click');
			}

			return _stub;
		},

		getStub: function(){
			this.logger.warn("NOT IMPLEMENTED YET");
		},

		getId: function(){return this.id},
		setId: function(id){this.id = id},
		getName: function(){return this.name},
		setName: function(name){this.name = name},
		getCategory: function(){return this.category},
		setCategory: function(category){this.category = category},
		getCategoryDesc: function(){return this.categoryDesc},
		setCategoryDesc: function(categoryDesc){this.categoryDesc = categoryDesc},
		getDescription: function(){return this.description},
		setDescription: function(description){this.description = description},
		getIcon: function(){return this.icon},
		setIcon: function(icon){this.icon = icon}

	});


var PalleteWizardEntry = PalleteEntry.extend({

		// override this
		execute: function(palleteManager){

			this.renderWizard();
		},

		// and this
		apply: function(palleteManager){
		
			this.logger.fatal("Not done yet");
		
		}

	},{
	});

