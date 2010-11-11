
var TableManager = Base.extend({

  id: "",
  tableId: "",
  logger: {},
  title: "Title",
  tableModel: {},
  initialOperations:[],
  isSelectedCell: false,
  hasAdvancedProperties: false,
  selectedCell: [],
  operations: [],
  linkedTableManager: undefined,
  linkedTableManagerOperation: undefined,
  cellRendererPool: {},

  constructor: function(id){
    this.logger = new Logger("TableManager - " + id);
    this.id = id;
    this.tableId = "table-" + id;

    // set a Default Table Model
    this.setTableModel(new TableModel());

    // Register this tablemanager in the global area
    TableManager.register(this);
  },

  init : function(){
			
    this.reset();
    $("#"+this.id).append(this.newTable());
    this.render();

  },

  reset: function(){
    $("#"+this.id).empty();
  },


  render: function(){

    this.logger.debug("Rendering table " + this.getTableId());

    // Create headers;
    var headerRows = $("<tr></tr>");
    var myself = this;
    $.each(myself.getTableModel().getColumnNames(),function(i,val){
      var _header = $("<th class=\"ui-state-default\">"+ val +"</th>");
      if ( typeof myself.getTableModel().getColumnSizes() != 'undefined'){
        _header.attr('width',myself.getTableModel().getColumnSizes()[i]);
      }
      _header.appendTo(headerRows);
    })
    headerRows.appendTo("#"+this.getTableId() + " > thead");

    // Create rows
    var myself=this;
    var data = this.getTableModel().getData() || [];
    $.each(data,function(i,row){
      myself.addRow(row);
    });

    $("#"+this.getTableId()).treeTable();
    this.updateOperations();

		
  },

  newTable: function(args){
    var isLayoutTable = this.tableId == 'table-cdfdd-layout-tree';
    //	var operationsDiv = ;

    isLayoutTable=false;
    var table = ''+
    //	(isLayoutTable ? ('<div id="'+ this.tableId +'Operations" style="height: 32px" class="cdfdd-operations"></div>') : '') +
    '<table id="'+ this.tableId +'" class="myTreeTable cdfdd ui-reset ui-clearfix ui-component ui-hover-state">\
			<caption class="ui-state-default"><div class="simpleProperties propertiesSelected">'+this.title+'</div>' +
    (!isLayoutTable ? ('<div id="'+ this.tableId +'Operations" style="float: right" class="cdfdd-operations"></div>') : '') +
    (this.hasAdvancedProperties == true ? '<span style="float:left">&nbsp;&nbsp;/&nbsp;&nbsp;</span><div class="advancedProperties propertiesUnSelected">Advanced Properties</div>' : '') +
    '</caption>\
			<thead>\
			</thead>\
			<tbody class="ui-widget-content">\
			</tbody>\
			</table>\
			';

    return table;
  },


  addRow: function(row,pos){
    // Adds row. -1 to add to the end of the list

    // Merge default options here
    // this.logger.debug("Adding row type "+ row.type  +" to table " + this.getTableId());
    var _model = BaseModel.getModel(row.type);
    if(typeof _model != 'undefined')
      this.extendProperties(row,_model.getStub());

    var rowObj = $('<tr></tr>');

    // Get id
    var _id;
    try{
      _id =  this.getTableModel().getRowId()(row);
      rowObj.attr("id",_id);
    }
    catch(e){
      this.logger.error("Error evaluating id expression " + this.getTableModel().getRowId() + ": " + e);
    }


    var _parent;
    var _parentExpression = this.getTableModel().getParentId();
    // parentId?
    try{
      if (typeof _parentExpression != 'undefined'){
        _parent = _parentExpression(row);
        if (typeof _parent != 'undefined' && _parent != IndexManager.ROOTID){
          //this.logger.debug("Row parent: " + _parent );
          rowObj.addClass("child-of-" + _parent);
        }
      }
    }
    catch(e){
      this.logger.error("Error evaluating parent expression " + _parentExpression + ": " + e);
    }


    // Add columns

    for (var i in this.getTableModel().getColumnGetExpressions()){
      this.renderColumn(rowObj,row,i);
    }


    var selector = "table.#" + this.getTableId() + " tbody";
    if(pos < 0){
      rowObj.appendTo($(selector));
      $(selector).append(html);
    }
    else{
      var _selector = $(selector + " > tr:eq(" + pos + ")");
      _selector.length == 1?_selector.before(rowObj):rowObj.appendTo($(selector));
    }

    return _id;

  },

  renderColumn: function(tr,row,colIdx){

    var renderer;

    var tm = this.getTableModel();
    var ct = tm.getColumnTypes()[colIdx];
    var _type = typeof ct == 'function'?ct(row):ct;
    
    if( !(typeof tm.getEditable() == 'undefined'? false:tm.getEditable()[colIdx]) ){
      _type = "Label";
    }

    var _setExpression = tm.getColumnSetExpressions()[colIdx];

    if(typeof this.cellRendererPool[_type] == 'undefined'){
      try {
        eval('renderer = new ' + _type+"Renderer(this)");
        this.cellRendererPool[_type] = renderer;
      } catch (e) {
        this.logger.warn("Error creating renderer: " + e);
        renderer = new CellRenderer(this);
      }

    }
    else{
      renderer = this.cellRendererPool[_type];
    }

    var myself=this;
    return renderer.render(tr, tm.getColumnGetExpressions()[colIdx](row),function(value){
      _setExpression.apply(myself,[row, value]);

      // Rerender this column
      tr.find("td:eq("+colIdx+")").remove();
      myself.renderColumn(tr,row,colIdx);
    });

  },

  updateTreeTable: function(rowId){

    if ( rowId != IndexManager.ROOTID){

      var _parentQ = $('#'+this.getTableId() + " > tbody > tr#"+ rowId);
      _parentQ.removeClass("initialized");
      _parentQ.removeClass("parent");
      $("> td > span.expander",_parentQ).remove();
      _parentQ.initializeTreeTableNode();
      _parentQ.expand();
    }

  },

  insertAtIdx: function(_stub,insertAtIdx){
		
    // Insert it on the dataModel
    this.getTableModel().getData().splice(insertAtIdx,0,_stub);
    this.getTableModel().getIndexManager().updateIndex();
    var newId = this.addRow(_stub,insertAtIdx);

    // Update treeTable:
    this.updateTreeTable(_stub.parent);

    // focus the newly created line
    this.selectCell(insertAtIdx,1,'simple');

  },

  createOrGetParent: function(category,categoryDesc){
    // Does this exist? If yes, return the last position
    var indexManager = this.getTableModel().getIndexManager();
    var cat = indexManager.getIndex()[category];
    if(typeof cat == 'undefined'){
      // Create it and return the last idx
      var _stub = {
        id: category,
        name: categoryDesc,
        type: "Label",
        typeDesc: "<i>Group</i>",
        parent: IndexManager.ROOTID,
        properties: [{
          name: "Group",
          description: "Group",
          value: categoryDesc,
          type: "Label"
        }]
      };
      insertAtIdx = this.getTableModel().getData().length;
      this.insertAtIdx(_stub,insertAtIdx);
      return insertAtIdx + 1;

    }
    else{
      // Append at the end
      return cat.index + cat.children.length + 1;

    }

		
  },

  updateOperations: function(){

    // Add all initial operation plus row/cell specific operations
    this.setOperations(this.getInitialOperations());

    if(this.isSelectedCell)
      var _ops = CellOperations.getOperationsByType(
        this.getTableModel().getEvaluatedRowType(this.selectedCell[0])
        );
    this.setOperations(this.getOperations().concat(_ops));

    this.logger.debug("Found " + this.getOperations().length + " operations for this cell");
    var _opsNode = $("#"+this.getTableId()+"Operations");
    _opsNode.empty();

    var myself = this;
    $.each(this.getOperations(),function(i,_operation){
      if (typeof _operation != 'undefined')
        _opsNode.append(_operation.getHtml(myself, i));
    });

  },


  cellClick: function(row,col,classType){
    // Update operations

    if(typeof this.getLinkedTableManager() != 'undefined')
      this.getLinkedTableManager().cellUnselected();

    this.isSelectedCell =  true;
    this.selectedCell = [row,col];
    this.updateOperations();
    this.fireDependencies(row,col,classType);

  },

  cellUnselected: function(){
    this.isSelectedCell = false;
    this.cleanSelections();
    this.updateOperations();
    this.cleanDependencies();
    if(typeof this.getLinkedTableManager() != 'undefined')
      this.getLinkedTableManager().cellUnselected();
  },

  selectCell: function(row,col,classType){

    // Unselect

    this.cleanSelections();
    $('#'+this.getTableId() + " > tbody > tr:eq("+ row +")").addClass("ui-state-active");

    // Uncomment following cells to enable td highlight
    //$('#'+this.getTableId() + " > tbody > tr:eq("+ row +") > td:eq("+ col + ")").addClass("ui-state-active");

    // Fire cellClicked; get id
    this.cellClick(row,col,classType);

  },

  cleanSelections: function(){


    $('#'+this.getTableId()).find("tr.ui-state-active").removeClass("ui-state-active"); // Deselect currently ui-state-active rows

  // Uncomment following cells to enable td highlight
  //$('#'+this.getTableId()).find("tr td.ui-state-active").removeClass("ui-state-active"); // Deselect currently ui-state-active rows

  },

  fireDependencies: function(row,col,classType){
    if( typeof this.getLinkedTableManager() != 'undefined' ){

      var data = this.getLinkedTableManagerOperation()(this.getTableModel().getData()[row],classType);

      var tableManager = this.getLinkedTableManager();

      tableManager.getTableModel().setData(data);
      tableManager.cleanSelections();
      tableManager.init();
    //tableManager.selectCell(targetIdx,colIdx);

    }
  },

  cleanDependencies: function(){
    if( typeof this.getLinkedTableManager() != 'undefined' ){
      var tableManager = this.getLinkedTableManager();

      tableManager.getTableModel().setData([]);
      tableManager.cleanSelections();
      tableManager.init();
    }
  },

  extendProperties: function(row,stub){
    // 1 - get names on original
    // 2 - get names on stub
    // 3 - add to the original the ones not on the second
    var pRow = {};

    $.each(row.properties,function(i,p){
      pRow[p.name]=p;
    });
    $.each(stub.properties,function(i,s){
      if(typeof pRow[s.name] == 'undefined')
        row.properties.push(s);
    });
		
  },

  // Accessors
  setId: function(id){
    this.id = id
  },
  getId: function(){
    return this.id
  },
  setTitle: function(title){
    this.title = title
  },
  getTitle: function(){
    return this.title
  },
  setTableId: function(tableId){
    this.tableId = tableId
  },
  getTableId: function(){
    return this.tableId
  },
  setTableModel: function(tableModel){
    this.tableModel = tableModel
  },
  getTableModel: function(){
    return this.tableModel
  },
  setInitialOperations: function(initialOperations){
    this.initialOperations = initialOperations
  },
  getInitialOperations: function(){
    return this.initialOperations
  },
  setOperations: function(operations){
    this.operations = operations
  },
  getOperations: function(){
    return this.operations
  },
  setSelectedCell: function(selectedCell){
    this.selectedCell = selectedCell
  },
  getSelectedCell: function(){
    return this.selectedCell
  },
  setLinkedTableManager: function(linkedTableManager){
    this.linkedTableManager = linkedTableManager
  },
  getLinkedTableManager: function(){
    return this.linkedTableManager
  },
  setLinkedTableManagerOperation: function(linkedTableManagerOperation){
    this.linkedTableManagerOperation = linkedTableManagerOperation
  },
  getLinkedTableManagerOperation: function(){
    return this.linkedTableManagerOperation
  }

},{
  tableManagers: {},

  register: function(tableManager){
    TableManager.tableManagers[tableManager.getTableId()] = tableManager;
  },

  getTableManager: function(id){
    return TableManager.tableManagers[id];
  },

  executeOperation: function(tableManagerId,idx){

    var tableManager = TableManager.getTableManager(tableManagerId);
    tableManager.getOperations()[idx].execute(tableManager);
  },

  globalInit: function(){

    // Enable the table selectors
    $("table.myTreeTable tbody tr td").live("mousedown",function() {
      var myself = $(this);

      // get Current Id:
      var row = myself.parent().prevAll().length;
      var col = myself.prevAll().length;

      var wasSelected = myself.hasClass("selected")
      var _tableManager = TableManager.getTableManager(myself.closest("table").attr("id"));

      if (!wasSelected){
        _tableManager.selectCell(row,col,'simple');
      }
      else{
        _tableManager.cellUnselected();
      }
							
    });


    $(".advancedProperties").live('click',function() {

      var tbody =  $("#table-" + ComponentsPanel.PROPERTIES + " tbody");
      tbody.fadeOut(300);
      setTimeout(function(){
        var myself = $("#table-" + ComponentsPanel.COMPONENTS + " .ui-state-active td");
        if(myself.length > 0){
          var row = myself.parent().prevAll().length;
          var col = myself.prevAll().length;
          var _tableManager = TableManager.getTableManager(myself.closest("table").attr("id"));
          _tableManager.selectCell(row,col,'advanced');
          $(".advancedProperties").attr("class","advancedProperties propertiesSelected");
          $(".advancedProperties").parent().find(".simpleProperties").attr("class","simpleProperties propertiesUnSelected");

        }
      },500);
    });
		
    $(".simpleProperties").live('click',function() {
      var tbody =  $("#table-" + ComponentsPanel.PROPERTIES + " tbody");
      tbody.fadeOut(300);
      setTimeout(function(){
        var myself = $("#table-" + ComponentsPanel.COMPONENTS + " .ui-state-active td")
        if(myself.length > 0){
          var row = myself.parent().prevAll().length;
          var col = myself.prevAll().length;
          var _tableManager = TableManager.getTableManager(myself.closest("table").attr("id"));
          _tableManager.selectCell(row,col,'simple');
          $(".advancedProperties").attr("class","advancedProperties propertiesUnSelected");
          $(".advancedProperties").parent().find(".simpleProperties").attr("class","simpleProperties propertiesSelected");
        }

      },500);

    });
			

  },
  S4: function() {
    return (((1+Math.random())*0x10000)|0).toString(16).substring(1);
  },
  generateGUID: function() {
    return (TableManager.S4()+TableManager.S4()+"-"+TableManager.S4()+
      "-"+TableManager.S4()+"-"+TableManager.S4()+
      "-"+TableManager.S4()+TableManager.S4()+TableManager.S4());
  }


});


var TableModel = Base.extend({

  id: "row.id",
  logger: {},
  data: [],
  indexManager: {},
  columnId: undefined,
  columnNames: [],
  columnGetExpressions: [],
  columnTypes: [],
  columnSizes: undefined ,
  editable: undefined,
  columnSetExpressions: [],
  rowId: "row.id",
  parentId: undefined,
  rowType: "row.type",

  constructor: function(id){
    this.logger = new Logger("TableModel" + id);
    this.id = id;
    this.setIndexManager(new IndexManager(this));

    this.init();
  },

  getEvaluatedId: function(rowNumber){

    try{
      var row = this.data[rowNumber];
      return this.getRowId()(row);
    }
    catch(e){
      this.logger.error("Error getting id " + e);
    }

  },

  getRowByName:function(name){
    var row;
    $.each(this.data,function(i,r){
      if(r.name == name){
        row = r;
        return;
      }
    });
    return row;
  },


  getEvaluatedRowType: function(rowNumber){

    try{
      var row = this.data[rowNumber];
      return this.getRowType()(row);
    }
    catch(e){
      this.logger.error("Error getting row type: " + e);
    }

  },


  init: function(){
  // Do nothing
  },

  setId: function(id){
    this.id = id
  },
  getId: function(){
    return this.id
  },
  setData: function(data){
    this.data = data;
    this.getIndexManager().updateIndex()
  },
  getData: function(){
    return this.data
  },
  setIndexManager: function(indexManager){
    this.indexManager = indexManager
  },
  getIndexManager: function(){
    return this.indexManager
  },
  setColumnNames: function(columnNames){
    this.columnNames = columnNames
  },
  getColumnNames: function(){
    return this.columnNames
  },
  setColumnGetExpressions: function(columnGetExpressions){
    this.columnGetExpressions = columnGetExpressions
  },
  getColumnGetExpressions: function(){
    return this.columnGetExpressions
  },
  setColumnSetExpressions: function(columnSetExpressions){
    this.columnSetExpressions = columnSetExpressions
  },
  getColumnSetExpressions: function(){
    return this.columnSetExpressions
  },
  setColumnTypes: function(columnTypes){
    this.columnTypes = columnTypes
  },
  getColumnTypes: function(){
    return this.columnTypes
  },
  setColumnSizes: function(columnSizes){
    this.columnSizes = columnSizes
  },
  getColumnSizes: function(){
    return this.columnSizes
  },
  setEditable: function(editable){
    this.editable = editable
  },
  getEditable: function(){
    return this.editable
  },
  setRowId: function(rowId){
    this.rowId = rowId
  },
  getRowId: function(){
    return this.rowId
  },
  setRowType: function(rowType){
    this.rowType = rowType
  },
  getRowType: function(){
    return this.rowType
  },
  setParentId: function(parentId){
    this.parentId = parentId
  },
  getParentId: function(){
    return this.parentId
  }

});


// Properties Table Model

var PropertiesTableModel = TableModel.extend({

  constructor: function(id){
    this.logger = new Logger("TableModel" + id);
    this.id = id;
    this.setIndexManager(new IndexManager(this));

    this.setColumnNames(['Property','Value']);
    this.setColumnGetExpressions([function(row){
      return row.description
    },function(row){
      return row.value
    }]);
    this.setColumnSetExpressions([undefined,function(row,value){
      row.value = value
    }]);
    this.setColumnTypes(['String', function(row){
      return row.type
    }]);
    this.setColumnSizes(['40%','60%']);
    this.setEditable([false, true]);
    this.setRowId(function(row){
      return TableManager.generateGUID()
    });
    this.setRowType(function(row){
      return row.type
    });

    this.init();
  }


});


var CellRenderer = Base.extend({


  logger: {},
  tableManager: null,

  constructor: function(tableManager){
    this.logger = new Logger("CellRenderer");
    this.logger.debug("Creating new CellRenderer");
    this.tableManager = tableManager;
  },

  // Defaults to a common string type
  //  render: function(row,placeholder, getExpression,setExpression,editable){

  render: function(placeholder, value, callback){
    $("<td>"+ value +"</td>").appendTo(placeholder);
  },

  getTableManager: function(){
    return this.tableManager;
  }

});


var LabelRenderer = CellRenderer.extend({

  constructor: function(tableManager){
    this.base(tableManager);
    this.logger = new Logger("LabelRenderer");
    this.logger.debug("Creating new LabelRenderer");
  }


});


var StringRenderer = CellRenderer.extend({

  constructor: function(tableManager){
    this.base(tableManager);
    this.logger = new Logger("StringRenderer");
    this.logger.debug("Creating new StringRenderer");
  },

  render: function(placeholder, value, callback){

    var _editArea = $("<td>"+ value +"</td>");
    var myself = this;
    _editArea.editable(function(value,settings){
      myself.logger.debug("Saving new value: " + value );
      callback(value);

      return value;
    } , {
      cssclass: "cdfddInput",
      select: true,
      onblur: "submit",
      onsubmit: function(settings,original){
        return myself.validate($('input',this).val());
      }
    });
    _editArea.appendTo(placeholder);

  },

  validate: function(settings, original){
    return true;
  }

});

var IdRenderer = StringRenderer.extend({

  constructor: function(tableManager){
    this.base(tableManager);
    this.logger = new Logger("IdRenderer");
    this.logger.debug("Creating new IdRenderer");
  },

  validate: function(value){

    if(!value.match(/^[a-zA-Z0-9_.]*$/)){
      $.prompt('Argument '+ value + ' invalid. Can only contain alphanumeric characters and the special _ and . characters');
      return false;
    }
    return true;
  }

});


var IntegerRenderer = StringRenderer.extend({

  constructor: function(tableManager){
    this.base(tableManager);
    this.logger = new Logger("IntegerRenderer");
    this.logger.debug("Creating new IntegerRenderer");
  },

  validate: function(value){

    if(!value.match(/^\d*$/)){
      $.prompt('Argument '+ value + ' must be numeric');
      return false;
    }
    return true;
  }

});
		
var FloatRenderer = StringRenderer.extend({

  constructor: function(tableManager){
    this.base(tableManager);
    this.logger = new Logger("FloatRenderer");
    this.logger.debug("Creating new FloatRenderer");
  },

  validate: function(value){

    if(!value.match(/^\d*\.?\d*$/)){
      $.prompt('Argument '+ value + ' must be numeric');
      return false;
    }
    return true;
  }
});

var SelectRenderer = CellRenderer.extend({


  selectData: {
  },

  revertedSelectData: {},
  autocompleteArray: [],

  constructor: function(tableManager){
    this.base(tableManager);
    this.logger = new Logger("SelectRenderer");
    this.logger.debug("Creating new SelectRenderer");

  },

  render: function(placeholder, value, callback){

    this.processData();
    var _editArea = $("<td>"+ ((!$.isArray(this.getData()) && typeof this.getData()[value] != undefined)? this.getData()[value]: value) + "</td>");
    var myself = this;

    
    _editArea.editable(function(value,settings){
      
      if (!$.isArray(myself.getData()) && typeof myself.revertedSelectData[value]!= "undefined"){
        myself.logger.debug("Saving new value: " + myself.revertedSelectData[value] );
        callback(myself.revertedSelectData[value]);
      }
      else{
        myself.logger.debug("Saving new value: " + value );
        callback(value);
      }

      return value;
    }, {
      type      : "autocomplete",
      tooltip   : "Click to edit...",
      onblur    : "submit",
      autocomplete : {
        source : function(req, add){
          myself.autoCompleteRequest(req,add);
        },
        minLength: 0,
        delay:this.getDelay()
      },
      onsubmit: function(settings,original){
        return myself.validate($('input',this).val());
      },
      height: 12
    });
     
    _editArea.appendTo(placeholder);


  },

  autoCompleteRequest: function(req,add){
    
    add(this.autocompleteArray)
    
  },

  getDelay: function(){
    return 300;
  },

  processData: function(){
    if ($.isArray(this.getData())){
      this.isArray= true;
      this.autocompleteArray = this.getData();
    }
    else{
      this.isArray = false;
      // Get the correct values and inverting the selectData
      this.autocompleteArray = [];
      for (i in this.getData()){
        this.autocompleteArray.push(this.getData()[i]);
        this.revertedSelectData[this.getData()[i]] = i;
      }

    }
  },


  validate: function(settings, original){
    return true;
  },

  getData: function(){
    // Default implementation
    return this.selectData;
  }



});


var BooleanRenderer = SelectRenderer.extend({

  selectData: {
    'true':'True',
    'false':'False'
  }
  
});

var SelectMultiRenderer = CellRenderer.extend({

  value: null,

  constructor: function(tableManager){
    this.base(tableManager);
    this.logger = new Logger("SelectMultiRenderer");
    this.logger.debug("Creating new SelectMultiRenderer");
  },

  render: function(placeholder, value, callback){

    this.value = value;
    var _editArea = $("<td>"+ value + "</td>");
    var myself = this;
    _editArea.editable(function(value,settings){

      var selector = $(this);
      var value = "['"+selector.find("input").val().replace(/, /g,"','") + "']";
      if (value=="['Select options']"){
        value = "[]";
      }
      myself.logger.debug("Saving new value: " + value );
      callback(value);

      return myself.getFormattedValue(value);
    } , {
      cssclass: "cdfddInput",
      data   : this.getData(),
      type   : 'selectMulti',
      submit: 'OK',
      height: 12,
      onsubmit: function(settings,original){
        return myself.validate($('input',this).val());
      }
    });
    _editArea.appendTo(placeholder);

  },

  validate: function(settings, original){
    return true;
  },

  getData: function(){
    return '{"A": "Alpha","B":"Beta"}';
  },

  getFormattedValue: function(value){
    return value;
  }


});


var RoundCornersRenderer = SelectRenderer.extend({

  selectData: {
    '':'Simple',
    'cdfdd-round':'Round',
    'cdfdd-bevel':'Bevel',
    'cdfdd-notch':'Notch',
    'cdfdd-bite':'Bite',
    'cdfdd-bevel_top':'Top Bevel',
    'cdfdd-dog_tr':'Dog TR'
  }
  
});


var TextAlignRenderer = SelectRenderer.extend({

  selectData: {
    '':'',
    'left':'Left',
    'center':'Center',
    'right':'Right'
  }
});


	

var ColorRenderer = CellRenderer.extend({

  constructor: function(tableManager){
    this.base(tableManager);
    this.logger = new Logger("ColorRenderer");
    this.logger.debug("Creating new ColorRenderer");
    this.id = 0;
  },
			
  getId: function(){
    return this.id++;
  },

  render: function(placeholder, value, callback){
    
     
    this.placeholder = placeholder;
					
    var id = this.getId();
    var inputId = "#colorpicker_input_" + id;
    var checkId = "#colorpicker_check_" + id;
    var _editArea = $('<td><form onsubmit="return false" class="cdfddInput"><input id="colorpicker_check_' + id + '" class="colorcheck" type="checkbox"></input><input id="colorpicker_input_' + id+ '" class="colorinput" readonly="readonly" type="text" size="7"></input></form></td>');
    var myself=this;
    $(checkId ,_editArea).bind("click",function(){

      if($(this).is(":checked")){
        $(inputId,_editArea).attr("disabled",true);
        $(inputId,_editArea).attr("readonly","readonly");
        $(inputId).trigger("click");
      }
      else{
        $(inputId,_editArea).val("");
        $(inputId,_editArea).attr("disabled",true);
        callback("");
      }
    });
    this.updateValueState(value,_editArea, inputId, checkId);
    $(inputId,_editArea).ColorPicker({
      onSubmit: function(hsb, hex, rgb, el) {
        $(el).val("#"+hex);
        $(el).ColorPickerHide();
        callback("#"+hex);
      },
      onBeforeShow: function () {
        $(this).ColorPickerSetColor(this.value.substring(1));
      }
    });
    // $("input",_editArea).ColorPicker();
    _editArea.appendTo(placeholder);

    

  },

  updateValueState: function(value,placeholder,inputId,checkId){
    // set checkbox and textarea state
    if (value == ''){
      $(checkId,placeholder).removeAttr("checked");
      $(checkId,placeholder).css("background-color","#ffffff");
      $(inputId,placeholder).attr("disabled",true);
    }
    else{
      $(checkId,placeholder).attr("checked","true");
      $(inputId,placeholder).removeAttr("disabled");
      $(inputId,placeholder).attr("readonly","readonly");
      $(inputId,placeholder).val(value);
    }

  }
});

var TextAreaRenderer = CellRenderer.extend({

  // Locally set the value. Please not that this will only be used
  value: null,

  constructor: function(tableManager){
    this.base(tableManager);
    this.logger = new Logger("TextAreaRenderer");
    this.logger.debug("Creating new TextAreaRenderer");
  },

  render: function(placeholder, value, callback){

    // Storing the var for later use when render() is not called again
    this.value = value;


    var _editArea = $('<td><div style="float:left"><code></code></div><div class="edit" style="float:right"></div></td>');
    _editArea.find("code").text(this.getFormattedValue(value));
    var myself=this;
    var _prompt = $('<button class="cdfddInput">...</button>').bind("click",function(){

      var _inner = 'Edit<br /><textarea wrap="off" cols="80" class="cdfddEdit" name="textarea">' + myself.value + '</textarea>';
      // Store what we need in a global var
      cdfdd.textarea = [myself,placeholder, myself.value, callback];
      $.prompt(_inner,{
        buttons: {
          Ok: true,
          Cancel: false
        },
        callback: myself.callback,
        opacity: 0.2,
        prefix:'brownJqi'
      });
    }).appendTo($("div.edit",_editArea));

    _editArea.appendTo(placeholder);

    
  },

  callback: function(v,m,f){
    if (v){
      // set value. We need to add a space to prevent a string like function(){}
      // to be interpreted by json as a function instead of a string
      var value = f.textarea;
      this.value = value;
      if(value.length != 0 && value.substr(value.length-1,1)!=" "){
        value = value+" ";
      }
      cdfdd.textarea[3](value);
      $("code",cdfdd.textarea[1]).text(cdfdd.textarea[0].getFormattedValue(value));
    }
    delete cdfdd.textarea;
  },


  getFormattedValue: function(_value){
    
    if(_value.length > 30){
      _value = _value.substring(0,20) + " (...)";
    }
    return _value;
  }

});

var HtmlRenderer = TextAreaRenderer.extend({});

var ResourceRenderer = TextAreaRenderer.extend({});
	
var DateRenderer = CellRenderer.extend({

  callback: undefined,

  constructor: function(tableManager){
    this.base(tableManager);
    this.logger = new Logger("DateRenderer");
    this.logger.debug("Creating new DateRenderer");
  },
			
  render: function(placeholder, value, callback){

    this.callback = callback;

    var _editArea = $("<td>"+this.getFormattedValue(value)+ "</td>");
    var myself = this;

    _editArea.editable(function(value,settings){
      myself.logger.debug("Saving new value: " + value );
      if (value != 'pickDate')
        callback(value);

      return myself.getFormattedValue(value);
    } , {
      cssclass: "cdfddInput",
      data   : this.getData(value),
      type   : 'select',
      submit: 'OK',
      height: 12,
      onsubmit: function(settings,original){
        var selectedValue = $(this.children()[0]).val();
        if(selectedValue == 'pickDate'){
          myself.pickDate($(this.children()[0]));
          return false;
        }
        return myself.validate();
      }
    });
    _editArea.appendTo(placeholder);

    

  },
			
  pickDate: function(input){
    var myself=this;
    this.datePicker = $("<input/>").css("width","80px");
    $(input).replaceWith(this.datePicker);
    this.datePicker.datepicker({
      dateFormat: 'yy-mm-dd',
      changeMonth: true,
      changeYear: true,
      onSelect:function(date, input) {
        myself.callback(date);
      }
    });
    this.datePicker.datepicker('show');
  },
			
  validate: function(settings, original){
    return true;
  },
			
  getData: function(value){
    var data = Panel.getPanel(ComponentsPanel.MAIN_PANEL).getParameters();
    var _str = "{'today':'Today','yesterday':'Yesterday','lastWeek':'One week ago','lastMonth':'One month ago','monthStart':'First day of month','yearStart':'First day of year','pickDate':'Pick Date', 'selected':'" + value + "'}";

    return _str;
  },

  getFormattedValue: function(selectedValue){
    
    var date;

    if (selectedValue.match(/\d{4}-\d{2}-\d{2}/)){
      var dateArray = selectedValue.split('-');
      date = new Date(dateArray[0],dateArray[1],dateArray[2]);
      return  this.toDateString(date);
    }
    else{

      if(selectedValue == 'pickDate')
        return this.toDateString(this.datePicker.datepicker('getDate'));
				
      return selectedValue;
    }


  },
			
  toDateString: function(d){
    var currentMonth = "0" + (d.getMonth() + 1);
    var currentDay = "0" + (d.getDate());
    return d.getFullYear() + "-" + (currentMonth.substring(currentMonth.length-2, currentMonth.length)) + "-" + (currentDay.substring(currentDay.length-2, currentDay.length));
  }


});
		
var DateRangeRenderer = DateRenderer.extend({
		
  pickDate: function(input){
    this.datePicker = $("<input/>").css("width","80px");
    $(input).replaceWith(this.datePicker);
				
    var offset = this.datePicker.offset();
    var myself = this;
				
    var a = this.datePicker.daterangepicker({
      posX: offset.left-400,
      posY: offset.top-100,
      dateFormat: 'yy-mm-dd',
      onDateSelect: function(rangeA, rangeB) {
        myself.rangeA = rangeA;
        myself.rangeB = rangeB;
      }
    });
				
    this.datePicker.click();
  },
			
  getData: function(value){
    var data = Panel.getPanel(ComponentsPanel.MAIN_PANEL).getParameters();
    var _str = "{'monthToDay':'Month to day','yearToDay':'Year to day','pickDate':'Pick Dates', 'selected':'" + (value) + "'}";
				
    return _str;
  },
			
  getFormattedValue: function(value){
    var selectedValue = value;
    if(selectedValue == 'pickDate'){
      return  this.rangeA + " - " + this.rangeB;
    }
				
    var date = new Date()
    if(selectedValue == "monthToDay" )
      date.setDate(1);
    else if(selectedValue == "yearToDay" ){
      date.setMonth(0);
      date.setDate(1);
    }
				
    return  this.toDateString(date) + " " + this.toDateString(new Date());
  }
	
});
	
var ResourceFileRenderer = CellRenderer.extend({

  callback: null,

  constructor: function(tableManager){
    this.base(tableManager);
    this.logger = new Logger("ResourceFileRenderer");
    this.logger.debug("Creating new ResourceFileRenderer");
  },

  render: function(placeholder, value, callback){
    
    this.callback = callback;

    var content = $('<td></td>');
    var _editArea = $('<div class="cdfdd-resourceFileNameRender" >'+ value +'</div>');
    var _fileExplorer = $('<button class="cdfdd-resourceFileExplorerRender">...</button>');
    content.append(_editArea);
    content.append(_fileExplorer);
					
    var myself = this;
    _editArea.editable(function(value,settings){
      myself.logger.debug("Saving new value: " + value );
      callback(value)
      return value;
    } , {
      cssclass: "cdfddInput",
      select: true,
      onsubmit: function(settings,original){
        return myself.validate($('input',this).val());
      }
    });
					
    var fileExtensions = this.getFileExtensions();
    _fileExplorer.bind('click',function(){
						
      var fileExplorercontent = 'Choose Resouce:<div id="container_id" class="urltargetfolderexplorer"></div>';
      var selectedFile = "";

      $.prompt(fileExplorercontent,{
        loaded: function(){
          selectedFile = "";
          $('#container_id').fileTree(
          {
            root: '/',
            script: CDFDDDataUrl.replace("Syncronize","ExploreFolder?fileExtensions="+fileExtensions),
            expandSpeed: 1000,
            collapseSpeed: 1000,
            multiFolder: false,
            folderClick:
            function(obj,folder){
              if($(".selectedFolder").length > 0)$(".selectedFolder").attr("class","");
              $(obj).attr("class","selectedFolder");
            }
          },
          function(file) {
            selectedFile = file;
            $(".selectedFile").attr("class","");
            $("a[rel='" + file + "']").attr("class","selectedFile");
          });
        },
        buttons: {
          Ok: true,
          Cancel: false
        },
        opacity: 0.2,
        callback: function(v,m,f){
          if(v && selectedFile.length > 0){
            var file = myself.formatSelection(selectedFile);
            _editArea.text(file);
            myself.callback(file);
          }
        }
      });
    });
					
    content.appendTo(placeholder);

  },

  getFileExtensions: function(){
    return this.getTableManager().getTableModel().getRowByName("resourceType").value == "Css" ? ".css" : ".js";
  },

  formatSelection: function(file){
    return "../pentaho-cdf/GetCDFResource?resource=" + file;
  },

  validate: function(settings, original){
    return true;
  }

});
