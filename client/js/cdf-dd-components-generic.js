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

  constructor: function(tableManager){
    this.base(tableManager);
    this.logger = new Logger("ValuesArrayRenderer");
    this.logger.debug("Creating new ValuesArrayRenderer");
  },
			
  render: function(placeholder, value, callback){
			
					
    var _editArea = $("<td>"+ value  +"</td>");
    var myself = this;
					
    _editArea.click(function(){
					
      var arrayValue = value;
      var content = $('\
							<div id="' + myself.cssPrefix + '" class="' + myself.cssPrefix + 'Container">\
								<div class="' + myself.cssPrefix +'"></div>\
								<input class="' + myself.cssPrefix +'AddButton" type="button" value="Add"></input>\
							</div>');
					        
      vals = JSON.parse(arrayValue);
      for(val in vals){
        if(myself.multiDimensionArray){
          if(myself.hasTypedValues){
            myself.addTypedParameters(val,vals[val][0],(vals[val][1] === undefined ? "" : vals[val][1] === null ? "null" : vals[val][1]), vals[val][2], content);
          }
          else {
            myself.addParameters(val,vals[val][0],(vals[val][1] === undefined ? "" : vals[val][1] === null ? "null" : vals[val][1]), content);
          }
        }
        else{
          if(myself.hasTypedValues){
            myself.addTypedParameters(val,vals[val],"null",null,content);
          }
          else {
            myself.addParameters(val,vals[val],"null",content);
          }
        }
      }
      var index = vals.length;

      $.prompt('<div id="' + myself.cssPrefix + '" class="' + myself.cssPrefix +'Container">' + content.html() + '</div>',{
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
        loaded: function(){
          $('.' + myself.cssPrefix + 'AddButton').bind('click',function(){
            if(myself.hasTypedValues) myself.addTypedParameters(index,"","","",$("#" + myself.cssPrefix));
            else myself.addParameters(index,"","",$("#" + myself.cssPrefix));
									
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
            if($("#arg_" + i).length > 0 && $("#arg_" + i).val().length > 0){
              if(myself.multiDimensionArray){
                if(myself.hasTypedValues) array.push([$("#arg_" + i).val(),$("#val_" + i).val(), $("#type_" + i).val()]);
                else array.push([$("#arg_" + i).val(),$("#val_" + i).val()]);//TODO:ok?
              }
              else{
                array.push($("#arg_" + i).val());
              }
            }
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
			
  addParameters : function(i,arg,val,container){

    var parameterButton = 	'<input id="parameter_button_' + i + '" class="' + this.cssPrefix +'Parameter" type="button" value="..."></input>\n';
    var removeButton = 	'<input id="remove_button_' + i + '" class="' + this.cssPrefix +'Remove" type="button" value="-" ></input>\n';
    var argInput = 		'<div class="'+this.cssPrefix+'Args">' +
    '<span class="'+this.cssPrefix+'TextLabel">Arg'+i+':</span>' +
    '<input  id="arg_' + i + '" class="' + this.cssPrefix +'Text" type="text" value="' + arg + '"></input></div>\n';
    var valInput = 		'<div class="'+this.cssPrefix+'Val">' +
    '<span class="'+this.cssPrefix+'TextLabel">Val'+i+':</span>' +
    '<input  id="val_' + i + '" class="' + this.cssPrefix +'Text" type="text" value="' + val + '"></input></div>\n';
    var row =
    '<did id="parameters_' + i +'" >\n' +
    argInput +
    (this.multiDimensionArray ? ('<div class="' + this.cssPrefix +'Values">' + valInput + parameterButton + removeButton + '</div><br />') :  removeButton) +
    '</div>\n';
    container.find('.' + this.cssPrefix).append(row);
  },
			
  addTypedParameters : function(i,arg,val,type,container){
    //used when hasTypedValues=true, assumes multiDimensionalArray
    var parameterButton = 	'<input id="parameter_button_' + i + '" class="' + this.cssPrefix +'Parameter" type="button" value="..."></input>\n';
    var removeButton = 	'<input id="remove_button_' + i + '" class="' + this.cssPrefix +'Remove" type="button" value="-" ></input>\n';
    var argInput = 		'<div class="'+this.cssPrefix+'Args">' +
    '<span class="'+this.cssPrefix+'TextLabel">Arg'+i+':</span>' +
    '<input  id="arg_' + i + '" class="' + this.cssPrefix +'Text" type="text" value="' + arg + '"></input></div>\n';
    var valInput = 		'<div class="'+this.cssPrefix+'Val">' +
    '<span class="'+this.cssPrefix+'TextLabel">Val'+i+':</span>' +
    '<input  id="val_' + i + '" class="' + this.cssPrefix +'Text" type="text" value="' + val + '"></input></div>\n';
					
    var typeOptions = "";
    for(var j = 0; j < this.typesArray.length; j++){
      typeOptions += (this.typesArray[j] == type) ? '<option selected>' : '<option>';
      typeOptions += this.typesArray[j] + '</option>';
    }
    var typeSelect = '<div class="'+this.cssPrefix+'Type">' +
    '<span class="'+this.cssPrefix+'TextLabel">Type'+i+':</span>' +
    '<select  id="type_' + i + '" class="' + this.cssPrefix +'Text">' + typeOptions + '</select></div>\n';
    var row =
    '<did id="parameters_' + i +'" >\n' +
    argInput +
    '<div class="' + this.cssPrefix +'Values">' + valInput + '</div>' + '<div class="' + this.cssPrefix +'Types">' +  typeSelect + parameterButton + removeButton  +'<br /></div>\n';
    container.find('.' + this.cssPrefix).append(row);
  },
						
  removeParameter : function(){
    $("#" + this.id.replace("remove_button_","parameters_")).remove();
  },
						
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

var ArrayRenderer = ValuesArrayRenderer.extend({
		
  multiDimensionArray: false,
		
  cssPrefix: "StringArray",

  constructor: function(tableManager){
    this.base(tableManager);
    this.logger = new Logger("ArrayRenderer");
    this.logger.debug("Creating new ArrayRenderer");
  }
});

var CdaParametersRenderer = ValuesArrayRenderer.extend({
  cssPrefix: "ParameterList",
			
  hasTypedValues: true,
  //TODO: this should be fetched from somewhere
  typesArray: ['String','Integer','Numeric','Date','StringArray','IntegerArray','NumericArray','DateArray']
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

