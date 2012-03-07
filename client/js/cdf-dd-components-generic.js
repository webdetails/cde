// Basic Parameter

var ParameterEntry = PalleteEntry.extend({

  id: "PARAMETER_ENTRY",
  name: "Simple parameter",
  description: "Simple parameter",
  category: "GENERIC",
  categoryDesc: "Generic",

  getStub: function(){
    return ComponentsParameterModel.getStub();
  }

});

var ComponentsParameterModel = BaseModel.extend({
  },{
    MODEL: 'ComponentsParameter',

    getStub: function(){

      var _stub = {
        id: TableManager.generateGUID(),
        type: ComponentsParameterModel.MODEL,
        typeDesc: "Parameter",
        parent: IndexManager.ROOTID,
        properties: []
      };

      _stub.properties.push(PropertiesManager.getProperty("name"));
      _stub.properties.push(PropertiesManager.getProperty("propertyValue"));
      _stub.properties.push(PropertiesManager.getProperty("bookmarkable"));

      return _stub;
    }
  });
BaseModel.registerModel(ComponentsParameterModel);
CDFDDComponentsArray.push(new ParameterEntry());

var ComponentsOlapParameterModel = ComponentsParameterModel.extend({
  },{
    MODEL: 'ComponentsOlapParameter',

    getStub: function(){
      var _stub = ComponentsParameterModel.getStub();
      _stub.dimension = "";
      return _stub;
    }
  });
BaseModel.registerModel(ComponentsOlapParameterModel);


// Javascript Paramenter
var JavascriptParameterEntry = PalleteEntry.extend({

  id: "JAVASCRIPT_PARAMETER_ENTRY",
  name: "Custom parameter",
  description: "Custom parameter with javascript code",
  category: "GENERIC",
  categoryDesc: "Generic",
  getStub: function(){
    return ComponentsJavascriptParameterModel.getStub();
  }

});
	
	
var PromptRenderer = CellRenderer.extend({

  callback: null,
  editArea: null,
  value: null,

  constructor: function(tableManager){
    this.base(tableManager);
    this.logger = new Logger("PromptRenderer");
    this.logger.debug("Creating new PromptRenderer");
    this.wizard = "PROMPT_WIZARD";
  },
			
  render: function(placeholder, value, callback){

    var _editArea = $('<td><div style="float:left"><code></code></div><div class="edit" style="float:right"></div></td>');

    _editArea.find("code").text(this.getFormattedValue(value));
    var myself= this;

    var _prompt = $('<button class="cdfddInput">...</button>').bind("click",function(){

      // Storing the var for later use when render() is not called again
      var wizard = PromptWizardManager.getWizard(myself.wizard);
      wizard.setInvoker(myself);
      myself.callback = callback;
      myself.editArea = _editArea;
      myself.value = value;
      wizard.render();
    }).appendTo($("div.edit",_editArea));

    _editArea.appendTo(placeholder);

  },


  validate: function(settings, original){
    return true;
  },
			
  getFormattedValue: function(_value){
    
    if(_value.length > 30){
      _value = _value.substring(0,20) + " (...)";
    }
    return _value;
  },

  getValue: function(){
    return this.value;
  },

  getPropertyValue: function(id){
    return this.getTableManager().getTableModel().getRowByName(id).value;
  },

  promptCallback: function(value){
    this.callback(value)
    this.value = value;
    this.editArea.find("code").text(value.length > 30 ? value.substring(0,20) + " (...)" : value);
  }
});

var JavaScriptRenderer = PromptRenderer.extend({

  constructor: function(tableManager){
    this.base(tableManager);
    this.logger = new Logger("JavascriptRenderer");
    this.logger.debug("Creating new JascriptRenderer");
    this.wizard = "JAVASCRIPT_WIZARD";
  }
});

var MdxQueryRenderer = PromptRenderer.extend({

  constructor: function(tableManager){
    this.base(tableManager);
    this.logger = new Logger("MdxQueryRenderer");
    this.logger.debug("Creating new MdxQueryRenderer");
    this.wizard = "MDX_WIZARD";
  }
});

var SqlQueryRenderer = PromptRenderer.extend({

  constructor: function(tableManager){
    this.base(tableManager);
    this.logger = new Logger("SqlQueryRenderer");
    this.logger.debug("Creating new SqlQueryRenderer");
    this.wizard = "SQL_WIZARD";
  }
});


var ValuesArrayRenderer = CellRenderer.extend({

  multiDimensionArray: true,
			
  cssPrefix: "StringList",
			
  hasTypedValues: false,//if true, args also have a type
			
  typesArray: [],//only used if hasTypedValues
  
	//used for value input labels
	argTitle: 'Arg',
	valTitle: 'Value',

  constructor: function(tableManager){
    this.base(tableManager);
    this.logger = new Logger("ValuesArrayRenderer");
    this.logger.debug("Creating new ValuesArrayRenderer");
  },
			
  render: function(placeholder, value, callback){
			
    var _editArea = $("<td>"+ (value.length>30?(value.substring(0,20) + " (...)"):value)  +"</td>");
    var myself = this;
					
    _editArea.click(function(){
					
      var arrayValue = value;
      var content = $('\n' +
'            <div id="' + myself.cssPrefix + '" class="' + myself.cssPrefix + 'Container">\n' +
'              <div class="' + myself.cssPrefix +'"></div>\n' +
'              <input class="' + myself.cssPrefix +'AddButton" type="button" value="Add"></input>\n' +
'            </div>');
      
      vals = JSON.parse(arrayValue);
      
      for(var i=0; i < vals.length; i++){
        myself.addParameter(i, vals[i], content);
      }
      
      var index = vals.length;
      var wrapper = $("<div>",{id: myself.cssPrefix, 'class': myself.cssPrefix+'Container'});
      wrapper.append(content);
      $.prompt(wrapper.html(),{
        
        buttons: {
          Ok: true,
          Cancel: false
        } ,
        
        prefix: 'jqi' + myself.cssPrefix,
        
        callback: function(v,m,f){
          if(v){
            // A bit of a hack to make null happen
            arrayValue = arrayValue.replace(/"null"/g,"null");
            callback(arrayValue);
            _editArea.text(arrayValue);
          }
        },
        
        loaded: function(){ //button bindings
          $('.' + myself.cssPrefix + 'AddButton').bind('click',function(){
            if(myself.multiDimensionArray){
              myself.addParameter(index, ["","",""], $("#" + myself.cssPrefix));
            }
            else {
              myself.addParameter(index, "", $("#" + myself.cssPrefix));
            }
            $("#remove_button_"+index).bind('click',myself.removeParameter);
            $("#parameter_button_"+index).bind('click',myself.addParamterValue);
            index++;
          });
          $('.' + myself.cssPrefix + 'Remove').bind('click',myself.removeParameter);
          $('.' + myself.cssPrefix + 'Parameter').bind('click',myself.addParamterValue);
        },
        
        submit: function(v,m,f){
          var array = [];
          for(var i = 0; i < index; i++){
            var paramVal = myself.getParameterValues(i);
            if(paramVal != null && paramVal.length > 0 && paramVal[0] != null) array.push( paramVal ); //don't attempt to add deleted lines
          }
          arrayValue = array.length > 0 ? JSON.stringify(array) : "[]";
        }
      });
    });
					 
    _editArea.appendTo(placeholder);

  },

  validate: function(settings, original){
    return true;
  },
  
  /**
   * @param i line number 
   * @param values {Array} 
   **/
  addParameter : function(i, values, container){//TODO: still not done
    if(this.multiDimensionArray){
      if(this.hasTypedValues){
        var val = values[1] === undefined ? "" : values[1] === null ? "null" : values[1];
        var type = values.length >= 3 ? values[2] : null;
        this.addTypedParameters(i,values[0], val, type, container);
      }
      else {
        this.addParameters(i,values[0],(values[1] === undefined ? "" : values[1] === null ? "null" : values[1]), container);
      }
    }
    else{
      this.addParameters(i,values,"null",container);
    }
  },

  addParameters : function(i,arg,val,container){

    if(val) { val = val.replace(/["]/g,'&quot;');}//for output only, will come back ok
    
    var parameterButton = this.getParameterButton(i);
    var removeButton = this.getRemoveButton(i);
    var argInput = this.getTextInput(this.argTitle, arg, this.cssPrefix + 'Args', 'arg_' + i );
    var valInput = this.getTextInput(this.valTitle, val, this.cssPrefix + 'Val', 'val_' + i);

    var row =
      '<did id="parameters_' + i +'" >\n' +
      argInput +
      (this.multiDimensionArray ? 
        ('<div class="' + this.cssPrefix +'Values">' + valInput + parameterButton + removeButton + '</div><br />') :  
        removeButton) +
      '</div>\n';
    
    container.find('.' + this.cssPrefix).append(row);
  },
    
  addTypedParameters : function(i,arg,val,type,container){//ToDo: should be refactored with addParameters, currently not used
    //used when hasTypedValues=true, assumes multiDimensionalArray
    
    if(val) { val = val.replace(/["]/g,'&quot;');}//for output only, will come back ok
    
    var parameterButton = this.getParameterButton(i);
    var removeButton = this.getRemoveButton(i);
    var argInput = this.getTextInput(this.argTitle, arg, this.cssPrefix + 'Args', 'arg_' + i);
    var valInput = this.getTextInput(this.valTitle, val, this.cssPrefix + 'Val', 'val_' + i);
					
    var typeSelect = this.getTypeSelector('Type', type, 'type_' + i);
    var row =
    '<did id="parameters_' + i +'" >\n' +
    argInput +
    '<div class="' + this.cssPrefix +'Values">' + valInput + '</div>' + '<div class="' + this.cssPrefix +'Types">' +  typeSelect + parameterButton + removeButton  +'<br /></div>\n';
    container.find('.' + this.cssPrefix).append(row);
  },
  
  /**
   * @returns {Array} Values to be stored for each parameter_<i>
   **/ 
  getParameterValues : function(i) {
    
    if(!this.multiDimensionArray){
      return   $('#arg_' + i).val() ;
    } else {  
    
      var result = []
      
      result.push( $('#arg_' + i).val() );//name
      result.push( $('#val_' + i).val() );//value
      if(this.hasTypedValues){
        result.push( $('#type_' + i).val() );//type
      }
      
      return result;
    }
  },
	
	//TODO: redo, move to another file
	
	//parameters field generation (begin)
  
  getParameterButton : function (i){
    return '<input id="parameter_button_' + i + '" class="' + this.cssPrefix +'Parameter" type="button" value="..."></input>\n';
  },
  getRemoveButton : function (i){
    return '<input id="remove_button_' + i + '" class="' + this.cssPrefix +'Remove" type="button" value="-" ></input>\n';
  },
  
  getTextInput : function (title, value, cssClass, id){
    return '<div class="'+ cssClass + '">' +
    (title != null ?   ('<span class="'+this.cssPrefix+'TextLabel">' + title +'</span>') : '' )+
    '<input  id="' + id + '" class="' + this.cssPrefix +'Text" type="text" value="' + value + '"></input></div>\n';
  },
  
  getTypeSelector : function (title, type, id){
    var typeOptions = "";
    for(var j = 0; j < this.typesArray.length; j++){
      typeOptions += (this.typesArray[j] == type) ? '<option selected>' : '<option>';
      typeOptions += this.typesArray[j] + '</option>';
    }
    return '<div class="'+this.cssPrefix+'Type">' +
    (title != null ? ('<span class="'+this.cssPrefix+'TextLabel">'+ title + ':</span>' ) : '' )+
    '<select  id="' + id + '" class="' + this.cssPrefix +'Text">' + typeOptions + '</select></div>\n';
  },  
	
  getAccessCheckbox : function(access, cssClass, id){
    var checked = (access == 'private');
    return '<div class="' + cssClass + '"> <input id="' + id + '" type="checkbox" value="private" ' + (checked ? 'checked="checked"' : '' ) + ' /></div>';
  },
  //parameters field generation (end)
  
  
  removeParameter : function(){
    $("#" + this.id.replace("remove_button_","parameters_")).remove();
  },
  
  //TODO: change name (2 refs)
  addParamterValue : function(){
    var id = this.id;
    var content = '<div id="parameterList" class="StringListParameterContainer">';
    var filters = Panel.getPanel(ComponentsPanel.MAIN_PANEL).getParameters();
    if(filters.length == 0)
      content += "<p>No Parameters!</p>";
    else{
      content += '<p>Choose Parameter:</p><ul class="StringListParameter">';
      $.each(filters,function(i,filter){
        content += '<li><div onClick="ValuesArrayRenderer.setParameterValue(\'' + id + '\',\'' + filter.properties[0].value + '\')">' + filter.properties[0].value + '</div></li>';
      });
      content += '</ul>';
    }
				
    $.prompt(content + '</div>',{
      buttons: {
        Cancel: false
      },
      prefix:'jqiStringListParameters',
      focus:1
    });
  }
},{
  setParameterValue: function(id,value){
    $("#" + id.replace("parameter_button_","val_")).val(value);
    $.prompt.close();
  }
});

/**
 * Single value renderer
 */
var ArrayRenderer = ValuesArrayRenderer.extend({
		
  multiDimensionArray: false,
		
  cssPrefix: "StringArray",

  constructor: function(tableManager){
    this.base(tableManager);
    this.logger = new Logger("ArrayRenderer");
    this.logger.debug("Creating new ArrayRenderer");
  }
});

var IndexArrayRenderer = ArrayRenderer.extend({
    argTitle: 'Index'
});


//arg, value, no param button, //TODO: own css
var ListArgValNoParamRenderer = ValuesArrayRenderer.extend({
    //disable parameter button
    getParameterButton : function(i) { return ''; }
});

var SortByArrayRenderer = ListArgValNoParamRenderer.extend({
    argTitle: 'Index',
    valTitle : 'Order'
});

//used by ExtraOptions
var OptionArrayRenderer = ListArgValNoParamRenderer.extend({
  argTitle : 'Option'
});

var CdaParametersRenderer = ValuesArrayRenderer.extend({
  cssPrefix: "ParameterList",
	argTitle: 'Name',
	valTitle: 'Value',
  hasTypedValues: true,
  //TODO: this should be fetched from somewhere
  typesArray: ['String','Integer','Numeric','Date','StringArray','IntegerArray','NumericArray','DateArray'],
  
  /**
   * @returns {Array} 
   **/ 
  getParameterValues : function(i) {
    var name = $("#arg_" + i).val();
    var value = $("#val_" + i).val();
    var type = $("#type_" + i).val();
    var access = $("#access_" + i).attr('checked') ? 'private' : '';
    return [name, value, type, access];
  },
  
  addParameter : function(i, values, container){
    
    var arg = values[0];
    var val = values[1];
    var type = values[2];
    var access = values[3];
    
    
    if(val ===  undefined) { val = "" ; }
    else if(val ===  null) { val = "null" ; }
    else if(val) { val = val.replace(/["]/g,'&quot;');}//for output only, will come back ok
    
    var parameterButton = this.getParameterButton(i);
    var removeButton = this.getRemoveButton(i);
    
    var argInput = this.getTextInput(null, arg, this.cssPrefix + 'Args', 'arg_' + i);
    var valInput = this.getTextInput(null, val, this.cssPrefix + 'Val', 'val_' + i);
    var typeSelect = this.getTypeSelector(null, type, 'type_' + i);
    var accessCb = this.getAccessCheckbox(access, this.cssPrefix + 'Access', 'access_' + i);
    

    
    var row = '<tr id="parameters_' + i + '" >';
    row += '<td>' + argInput + '</td>';
    row += '<td>' + valInput + '</td>';
    row += '<td>' + typeSelect + '</td>';
    row += '<td>' + accessCb + '</td>';
    row += '<td>' + removeButton + '</td>';
    row += '</tr>';
    
    if(i==0){//add table and header
      container.find('.' + this.cssPrefix).append('<table> </table>');
      var hdr = '<tr>';
      hdr += '<th><span class="'+this.cssPrefix+'TextLabel">' + this.argTitle + '</span></th>'
      hdr += '<th><span class="'+this.cssPrefix+'TextLabel">' + this.valTitle + '</span></th>'
      hdr += '<th><span class="'+this.cssPrefix+'TextLabel">Type</span></th>'
      hdr += '<th><span class="'+this.cssPrefix+'TextLabel">Private?</span></th>'
      hdr += '<th><span class="'+this.cssPrefix+'TextLabel"></span></th>'
      hdr += '</tr>';
      row = hdr + row;
    }
    container.find('.' + this.cssPrefix + ' table').append(row);
	}
  
});

var CdaColumnsArrayRenderer = ValuesArrayRenderer.extend({
	argTitle: 'Index',
	valTitle: 'Name'
});

var CdaCalculatedColumnsArrayRenderer = ValuesArrayRenderer.extend({
	argTitle: 'Name',
	valTitle: 'Form.' 
});

var ComponentsJavascriptParameterModel = BaseModel.extend({
  },{
    MODEL: 'ComponentsJavascriptParameter',

    getStub: function(){

      var _stub = {
        id: TableManager.generateGUID(),
        type: ComponentsJavascriptParameterModel.MODEL,
        typeDesc: "Custom Parameter",
        parent: IndexManager.ROOTID,
        properties: []
      };

      _stub.properties.push(PropertiesManager.getProperty("name"));
      _stub.properties.push(PropertiesManager.getProperty("javaScript"));
      _stub.properties.push(PropertiesManager.getProperty("bookmarkable"));

      return _stub;
    }
  });
BaseModel.registerModel(ComponentsJavascriptParameterModel);
CDFDDComponentsArray.push(new JavascriptParameterEntry());



var DateParameterEntry = PalleteEntry.extend({

  id: "DATE_PARAMETER_ENTRY",
  name: "Date parameter",
  description: "Date parameter",
  category: "GENERIC",
  categoryDesc: "Generic",
  getStub: function(){
    return ComponentsDateParameterModel.getStub();
  }

});

var ComponentsDateParameterModel = BaseModel.extend({
  },{
    MODEL: 'ComponentsDateParameter',

    getStub: function(){

      var _stub = {
        id: TableManager.generateGUID(),
        type: ComponentsDateParameterModel.MODEL,
        typeDesc: "DateParameter",
        parent: IndexManager.ROOTID,
        properties: []
      };

      _stub.properties.push(PropertiesManager.getProperty("name"));
      _stub.properties.push(PropertiesManager.getProperty("propertyDateValue"));
      _stub.properties.push(PropertiesManager.getProperty("bookmarkable"));
      //_stub.properties.push(PropertiesManager.getProperty("propertyDateDim")); <- what's this for?'

      return _stub;
    }
  });
BaseModel.registerModel(ComponentsDateParameterModel);
CDFDDComponentsArray.push(new DateParameterEntry());


// Function Entry
var FunctionEntry = PalleteEntry.extend({
  id: "FUNCTION_ENTRY",
  name: "JavaScript Function",
  description: "JavaScript function",
  category: "SCRIPT",
  categoryDesc: "Scripts",
  getStub: function(){
    return ComponentsFunctionModel.getStub();
  }
});

var ComponentsFunctionModel = BaseModel.extend({
  },{
    MODEL: 'ComponentsFunction',

    getStub: function(){

      var _stub = {
        id: TableManager.generateGUID(),
        type: ComponentsFunctionModel.MODEL,
        typeDesc: "Function",
        parent: IndexManager.ROOTID,
        properties: []
      };

      _stub.properties.push(PropertiesManager.getProperty("name"));
      _stub.properties.push(PropertiesManager.getProperty("javaScript"));

      return _stub;
    }
  });
BaseModel.registerModel(ComponentsFunctionModel);
CDFDDComponentsArray.push(new FunctionEntry());

var CdaQueryRenderer = PromptRenderer.extend({

  constructor: function(tableManager){
    this.base(tableManager);
    this.logger = new Logger("CdaQueryRenderer");
    this.logger.debug("Creating new CdaQueryRenderer");
    this.wizard = "CDA_WIZARD";
  }
});

var MondrianCatalogRenderer = SelectRenderer.extend({

  logger: null,
  selectData: {
    '':''
  },
  catalogs:[],


  getDataInit: function(){
    
    var myself = this;
    $.getJSON(CDFDDServerUrl + "OlapUtils", {
      operation: "GetOlapCubes"
    }, function(json) {
      if(json.status == "true"){
        var catalogs = json.result.catalogs;
        myself.catalogs = catalogs;
        $.each(catalogs,function(i,catalog){
          myself.selectData[catalog.schema.replace("solution:","")] = catalog.name;
        });
      }
    });
  },

  postChange: function(value){
    // Searching for value
    var jndi,cube,seen=false;
    $.each(this.catalogs,function(i,c){
      if(c.schema == "solution:"+value){
        seen = true;
        cube = c.cubes[0].name;
        jndi = c.jndi;
        return false;
      }
    });

    if(seen){
      Dashboards.log("Found: " + jndi);
      // Update other fields
      var jndiRow = this.getTableManager().getTableModel().getRowByName("jndi");
      if(jndiRow != undefined){
        jndiRow.value=jndi;
        this.getTableManager().renderColumnByRow(jndiRow);
      }
      
      var queryRow = this.getTableManager().getTableModel().getRowByName("query");
      if(queryRow != undefined && queryRow.value==""){
        queryRow.value="select {} on ROWS, {} on COLUMNS from ["+cube+"]";
        this.getTableManager().renderColumnByRow(queryRow);
      }

    }
  }
});


var JndiRenderer = SelectRenderer.extend({

  logger: null,
  selectData: [''],
  catalogs:[],


  getDataInit: function(){

    var myself = this;
    $.getJSON(CDFDDServerUrl + "OlapUtils", {
      operation: "GetOlapCubes"
    }, function(json) {
      if(json.status == "true"){
        var catalogs = json.result.catalogs;
        var hash = {};
        $.each(catalogs,function(i,catalog){
          hash[catalog.jndi]=1;
        });
        for(jndi in hash){
          myself.selectData.push(jndi);
        }
      }
    });
  }
});

var ArraySelectRenderer = ValuesArrayRenderer.extend({
  cssPrefix: "GenericSelect",
  getParameterValues: function(i) {
    var value = $("#val_" + i).val();
    return value;
  },
  addParameter: function(i, val, container) {
    var table;
    if(i==0){//add table and header
      table = $('<table> </table>').appendTo(container.find('.' + this.cssPrefix));
      var hdr = '<tr>';
      hdr += '<th><span class="'+this.cssPrefix+'TextLabel">' + this.argTitle + '</span></th>'
      hdr += '<th><span class="'+this.cssPrefix+'TextLabel">' + this.valTitle + '</span></th>'
      hdr += '<th><span class="'+this.cssPrefix+'TextLabel"></span></th>'
      hdr += '</tr>';
      $(hdr).appendTo(table);
    } else {
      table = container.find('.' + this.cssPrefix + ' table') 
    }

    if(val ===  undefined) { val = "" ; }
    else if(val ===  null) { val = "null" ; }
    else if(val instanceof Array) { val = val[0];}
    val.replace(/["]/g,'&quot;');//for output only, will come back ok

    var valInput = "<input id='val_" + i + "' type='text' value='"+val+"' />";
    var removeButton = this.getRemoveButton(i);
    var row = '<tr id="parameters_' + i + '" >';
    row += '<td>' + this.argType + " " + i + "</td>";
    row += '<td>' + valInput + '</td>';
    row += '<td>' + removeButton + '</td>';
    row += '</tr>';

    table.append(row);
    var myself = this;
    setTimeout(function(){
      $("#val_" + i).autocomplete({
        source : function(req, add){
          myself.autoCompleteRequest(req,add);
        },
        minLength: 0,
        delay:myself.getDelay(),
        select: function(evt,ui) {
          $("#val_" + i).find("input").val(ui.item.value);
        },
        focus:  function (evt, data) {
          if (data != undefined) $('#val_' + i).val(data.item.value);
        },
        onsubmit: function(settings,original){
          return myself.validate($('input',this).val());
        },
        height: 12
      });
    },10);
  },
  autoCompleteRequest: function(req,add) {
    var results = this.selectData.map(function(e){return {label: e[1], value: e[0]};}) 
    add(jQuery.grep(results, function(elt, i){
      return elt.value.indexOf(req.term) >= 0;
    }));
  },
  validate: function(settings, original){
    return true;
  },
  getData: function(){
    // Default implementation
    return this.selectData;
  },
  getDelay: function() {return 300;}
});
