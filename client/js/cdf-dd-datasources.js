// Global datasources array where all the new datasources are registered.
var CDFDDDatasourcesArray = [];


var DatasourcesPanel = Panel.extend({

		name: "Datasources Panel",
		datasourcesPallete: {},
		datasourcesTable: {},
		datasourcesTableModel: {},
		propertiesTable: {},
		propertiesTableModel: {},
		datasourcesArray: [],


		constructor: function(id){

			this.base(id);
			this.logger = new Logger("Datasources");
			Panel.register(this);
		
		}, 

		init: function(){

			this.base();
			this.logger.debug("Specific init");


			// Pallete
			this.datasourcesPallete = new PalleteManager(DatasourcesPanel.PALLETE);
			this.addPalleteEntries();
			this.datasourcesPallete.init();



			// Datasources

			this.datasourcesTable = new TableManager(DatasourcesPanel.DATASOURCES);
			this.datasourcesTable.setTitle("Datasources");
			this.datasourcesPallete.setLinkedTableManager(this.datasourcesTable);

			// this.datasourcesTable.setInitialOperations([new DatasourcesAddRowOperation()]);

			var datasourcesTableModel = new TableModel('datasourcesTreeTableModel');
			datasourcesTableModel.setColumnNames(['Type','Name']);
			datasourcesTableModel.setColumnGetExpressions([
				function(row){return row.typeDesc},
				function(row){return CDFDDUtils.ev(row.properties[0].value)}
				]);
			datasourcesTableModel.setColumnTypes(['String','String']);
			datasourcesTableModel.setRowId(function(row){return row.id});
			datasourcesTableModel.setRowType(function(row){return row.type});
			datasourcesTableModel.setParentId(function(row){return row.parent});
			datasourcesTableModel.setData(cdfdd.getDashboardData().datasources.rows);
			this.datasourcesTable.setTableModel(datasourcesTableModel);
			this.datasourcesTable.init();


			// Properties
			this.propertiesTable = new TableManager(DatasourcesPanel.PROPERTIES);
			this.propertiesTable.setTitle("Properties");
			var propertiesTableModel = new PropertiesTableModel('datasourcesPropertiesTableModel');
			propertiesTableModel.setColumnGetExpressions([
				function(row){return row.description},
				function(row){return CDFDDUtils.ev(row.value)}
				]);

			// If we set the name, we need to change the name in the datasourcesTable
			propertiesTableModel.setColumnSetExpressions([undefined,
				function(row,value){
					row.value = value
					if (row.name == 'name'){
						var _tableManager = TableManager.getTableManager("table-" + DatasourcesPanel.DATASOURCES);
						this.logger.debug("Changing the name - applying to previous row in " + _tableManager + " in row " + _tableManager.getSelectedCell()[0]);
						var _cell = _tableManager.getSelectedCell();
						$("#" + _tableManager.getTableId() + " > tbody > tr:eq("+ _cell[0] +") > td:eq(1)" ).text(value);
						
					}
				}
				]);

			this.propertiesTable.setTableModel(propertiesTableModel);
			this.propertiesTable.init();

			this.datasourcesTable.setLinkedTableManager(this.propertiesTable);
			this.datasourcesTable.setLinkedTableManagerOperation(function(row){
					var arr = []; 
					for (p in row.properties){
						arr.push(row.properties[p]);
					}
					return arr;
				});


		},

		getContent: function(){
		
			return ' \n' +
'			<div id="'+ DatasourcesPanel.PALLETE +'" class="span-6 accordion"></div>\n' +
'			<div id="'+ DatasourcesPanel.DATASOURCES +'" class="span-8">Datasources</div>\n' +
'			<div id="'+ DatasourcesPanel.PROPERTIES + '" class="span-10 last">Properties</div>\n' +
'			';			
		
		},

		addPalleteEntries: function(){

			$.each(CDFDDDatasourcesArray,function(i,datasource){
					Panel.getPanel(DatasourcesPanel.MAIN_PANEL).getDatasourcesPallete().addEntry(datasource);
				});
		},


		// Get Datasources
		getDatasources: function(){
			
			var data = this.datasourcesTable.getTableModel().getData();
			var output = [];
			var myself = this;
			$.each(data,function(i,row){
        if(row.type != "Label")
					output.push(row);
				});
			return output;
		
		},

		// Get Previous jndi - iterate through all but the last one, the newly added
		getPreviousProperty: function(prop){
			
			var data = this.datasourcesTable.getTableModel().getData();
			var output = "";
			var myself = this;
			$.each(data,function(i,row){
					if (i == data.length - 1)
						return false;
					if(row.type == DatasourcesMdxModel.MODEL || row.type == DatasourcesSqlModel ){
						output = Panel.getRowPropertyValue(row, prop);
						return false;
					}
				});
			return output;
		
		},
		
		getPreviousJndi: function(){return this.getPreviousProperty('jndi')},
		getPreviousCatalog: function(){return this.getPreviousProperty('catalog')},
		getPreviousCube: function(){return this.getPreviousProperty('cube')},
			
		
		setDatasourcesPallete: function(datasourcesPallete){this.datasourcesPallete = datasourcesPallete},
		getDatasourcesPallete: function(){return this.datasourcesPallete},
		setDatasourcesArray: function(datasourcesArray){this.datasourcesArray = datasourcesArray},
		getDatasourcesArray: function(){return this.datasourcesArray}

	},{
	
		MAIN_PANEL: "datasourceens_panel",
		PALLETE: "cdfdd-datasources-pallete",
		PALLETE_ID: "cdfdd-datasources-palletePallete",
		DATASOURCES: "cdfdd-datasources-datasources",
		PROPERTIES: "cdfdd-datasources-properties"

	});


var DatasourcesMoveUpOperation = MoveUpOperation.extend({

		id: "DATASOURCES_MOVE_UP",
		types: ["Datasources"],

		constructor: function(){
			this.logger = new Logger("DatasourcesMoveUpOperation");
		}

});

CellOperations.registerOperation(new DatasourcesMoveUpOperation);


var DatasourcesMoveDownOperation = MoveDownOperation.extend({

		id: "DATASOURCES_MOVE_DOWN",
		types: ["Datasources"],

		constructor: function(){
			this.logger = new Logger("DatasourcesMoveDownOperation");
		}

});

CellOperations.registerOperation(new DatasourcesMoveDownOperation);


var DatasourcesDeleteOperation = DeleteOperation.extend({

		id: "DATASOURCES_DELETE",
		types: ["Datasources"],

		constructor: function(){
			this.logger = new Logger("DatasourcesDeleteOperation");
		}

});

CellOperations.registerOperation(new DatasourcesDeleteOperation);




