// Global components array where all the new components are registered.
var CDFDDComponentsArray = [];

var ComponentsPanel = Panel.extend({

  name: "Components Panel",
  componentsPallete: {},
  componentsTable: {},
  componentsTableModel: {},
  propertiesTable: {},
  propertiesTableModel: {},
  componentsArray: [],


  constructor: function(id){

    this.base(id);
    this.logger = new Logger("Components");
    Panel.register(this);
		
  },

  init: function(){

    this.base();
    this.logger.debug("Specific init");


    // Pallete
    this.componentsPallete = new PalleteManager(ComponentsPanel.PALLETE);
    this.addPalleteEntries();
    this.componentsPallete.init();



    // Components

    this.componentsTable = new TableManager(ComponentsPanel.COMPONENTS);
    this.componentsTable.setTitle("Components");
    this.componentsPallete.setLinkedTableManager(this.componentsTable);

    // this.componentsTable.setInitialOperations([new ComponentsAddRowOperation()]);

    var componentsTableModel = new TableModel('componentsTreeTableModel');
    componentsTableModel.setColumnNames(['Type','Name']);
    componentsTableModel.setColumnGetExpressions([
      function(row){
        return row.typeDesc
        },
      function(row){
        return row.properties[0].value
        }
      ]);
    componentsTableModel.setColumnTypes(['String','String']);
    componentsTableModel.setRowId(function(row){
      return row.id
      });
    componentsTableModel.setRowType(function(row){
      return row.type
      });
    componentsTableModel.setParentId(function(row){
      return row.parent
      });
    componentsTableModel.setData(cdfdd.getDashboardData().components.rows);
    this.componentsTable.setTableModel(componentsTableModel);
    this.componentsTable.init();


    // Properties
    this.propertiesTable = new TableManager(ComponentsPanel.PROPERTIES);
    this.propertiesTable.setTitle("Properties");
    var propertiesTableModel = new PropertiesTableModel('componentsPropertiesTableModel');

    // If we set the name, we need to change the name in the componentsTable
    propertiesTableModel.setColumnSetExpressions([undefined,
      function(row,value){
        row.value = value
        if (row.name == 'name'){
          var _tableManager = TableManager.getTableManager("table-" + ComponentsPanel.COMPONENTS);
          this.logger.debug("Changing the name - applying to previous row in " + _tableManager + " in row " + _tableManager.getSelectedCell()[0]);
          var _cell = _tableManager.getSelectedCell();
          $("#" + _tableManager.getTableId() + " > tbody > tr:eq("+ _cell[0] +") > td:eq(1)" ).text(value);
						
        }
      }
      ]);

    this.propertiesTable.setTableModel(propertiesTableModel);
    this.propertiesTable.hasAdvancedProperties = true;
    this.propertiesTable.init();

    this.componentsTable.setLinkedTableManager(this.propertiesTable);
    this.componentsTable.setLinkedTableManagerOperation(function(row, classType){
      var arr = [];
      for (p in row.properties){
        if(row.properties[p].classType == undefined || row.properties[p].classType == classType)
          arr.push(row.properties[p]);
      }
      return arr;
    });


  },

  getContent: function(){
		
    return ' \
			<div id="'+ ComponentsPanel.PALLETE +'" class="span-6 accordion"></div>\
			<div id="'+ ComponentsPanel.COMPONENTS +'" class="span-8">Components</div>\
			<div id="'+ ComponentsPanel.PROPERTIES + '" class="span-10 last">Properties</div>\
			';			
		
  },

  addPalleteEntries: function(){

    $.each(CDFDDComponentsArray,function(i,component){
      Panel.getPanel(ComponentsPanel.MAIN_PANEL).getComponentsPallete().addEntry(component);
    });
  },


  // Get Parameters
  getParameters: function(){
			
    var data = this.componentsTable.getTableModel().getData();
    var output = [];
    $.each(data,function(i,row){
      if(row.type.match("Parameter")){
        output.push(row);
      }
    });
    return output;
		
  },
		
  setComponentsPallete: function(componentsPallete){
    this.componentsPallete = componentsPallete
    },
  getComponentsPallete: function(){
    return this.componentsPallete
    },
  setComponentsArray: function(componentsArray){
    this.componentsArray = componentsArray
    },
  getComponentsArray: function(){
    return this.componentsArray
    }

},{
	
  MAIN_PANEL: "componentens_panel",
  PALLETE: "cdfdd-components-pallete",
  PALLETE_ID: "cdfdd-components-palletePallete",
  COMPONENTS: "cdfdd-components-components",
  PROPERTIES: "cdfdd-components-properties"

});


var ComponentsMoveUpOperation = MoveUpOperation.extend({

  id: "COMPONENTS_MOVE_UP",
  types: ["Components"],

  constructor: function(){
    this.logger = new Logger("ComponentsMoveUpOperation");
  }

});

CellOperations.registerOperation(new ComponentsMoveUpOperation);


var ComponentsMoveDownOperation = MoveDownOperation.extend({

  id: "COMPONENTS_MOVE_DOWN",
  types: ["Components"],

  constructor: function(){
    this.logger = new Logger("ComponentsMoveDownOperation");
  }

});

CellOperations.registerOperation(new ComponentsMoveDownOperation);


var ComponentsDeleteOperation = DeleteOperation.extend({

  id: "COMPONENTS_DELETE",
  types: ["Components"],

  constructor: function(){
    this.logger = new Logger("ComponentsDeleteOperation");
  }

});

CellOperations.registerOperation(new ComponentsDeleteOperation);



