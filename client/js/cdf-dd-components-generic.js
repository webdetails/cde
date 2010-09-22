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

			constructor: function(){
				this.base();
				this.logger = new Logger("PromptRenderer");
				this.logger.debug("Creating new PromptRenderer");
				this.wizard = "PROMPT_WIZARD";
			},
			
			render: function(row,placeholder, getExpression,setExpression,editable, getRow){
			
				if(editable){
				
					this.row = row;
					this.getExpression = getExpression;
					this.setExpression = setExpression;
					this.placeholder = placeholder;
					this.getRow = getRow;
					
						var _editArea = $('<td><div style="float:left"><code></code></div><div class="edit" style="float:right"></div></td>');
						this.editArea = _editArea;
						
						_editArea.find("code").text(this.getFormattedExpression(row, getExpression));
						var myself= {row: row,getRow: getRow, getExpression:getExpression, setExpression:setExpression,editArea:_editArea,wizard:this.wizard};
						myself.callback = this.callback;
						myself.getValue = this.getValue;
						myself.getPropertyValue = this.getPropertyValue;
						var _prompt = $('<button class="cdfddInput">...</button>').bind("click",function(){
								var wizard = PromptWizardManager.getWizard(myself.wizard);
								wizard.setInvoker(myself);
								wizard.render();
							}).appendTo($("div.edit",_editArea));

						_editArea.appendTo(placeholder);
				}
				else
					$("<td>"+ this.getFormattedExpression(row, getExpression) +"</td>").appendTo(placeholder);
			},


			validate: function(settings, original){
				return true;
			},
			
			getFormattedExpression: function(row, getExpression){
				var _value = getExpression(row);
				if(_value.length > 30){
					_value = _value.substring(0,20) + " (...)";
				}
				return _value;
			},
			
			getValue: function(){
				return this.getExpression(this.row);
			},
			
			getPropertyValue: function(id){
				return this.getRow(id).value;
			},
			
			callback: function(value){
				this.setExpression(this.row,value);
				this.editArea.find("code").text(value.length > 30 ? value.substring(0,20) + " (...)" : value);
			}
});

var JavaScriptRenderer = PromptRenderer.extend({

		constructor: function(){
			this.base();
			this.logger = new Logger("JavascriptRenderer");
			this.logger.debug("Creating new JascriptRenderer");
			this.wizard = "JAVASCRIPT_WIZARD";
		}
});

var MdxQueryRenderer = PromptRenderer.extend({

		constructor: function(){
			this.base();
			this.logger = new Logger("MdxQueryRenderer");
			this.logger.debug("Creating new MdxQueryRenderer");
			this.wizard = "MDX_WIZARD";
		}
});

var SqlQueryRenderer = PromptRenderer.extend({

		constructor: function(){
			this.base();
			this.logger = new Logger("SqlQueryRenderer");
			this.logger.debug("Creating new SqlQueryRenderer");
			this.wizard = "SQL_WIZARD";
		}
});


var ValuesArrayRenderer = CellRenderer.extend({

			multiDimensionArray: true,
			
			cssPrefix: "StringList",

			constructor: function(){
				this.base();
				this.logger = new Logger("ValuesArrayRenderer");
				this.logger.debug("Creating new ValuesArrayRenderer");
			},
			
			render: function(row,placeholder, getExpression,setExpression,editable, getRow){
			
				if(editable){
					
					this.row = row;
					this.getExpression = getExpression;
					this.setExpression = setExpression;
					this.placeholder = placeholder;
					
					var _editArea = $("<td>"+ this.getExpression(row)  +"</td>");
					var myself = this;
					
					_editArea.click(function(){
					
						var arrayValue = myself.getExpression(row);
						var content = $('\
							<div id="' + myself.cssPrefix + '" class="' + myself.cssPrefix + 'Container">\
								<div class="' + myself.cssPrefix +'"></div>\
								<input class="' + myself.cssPrefix +'AddButton" type="button" value="Add"></input>\
							</div>');
					        
                                                vals = JSON.parse(arrayValue);	
                                                for(val in vals){
													if(myself.multiDimensionArray){
														myself.addParameters(val,vals[val][0],(vals[val][1] === undefined ? "" : vals[val][1] === null ? "null" : vals[val][1]),content);
													}
													else{
														myself.addParameters(val,vals[val],"null",content);
													}
                                                }
                                                var index = vals.length; 
//						if(arrayValue.length>0)
//							arrayValue = arrayValue.substring(1,arrayValue.length-1);
//														
//						var values = myself.multiDimensionArray ? arrayValue.split("],[") : arrayValue.split(",");
//						var index = 0;
//						for(v in values){
//							var value = values[v].replace(/"/g,"")
//								.replace(/]$/,"").replace(/\[?/,"").replace(/]$/,"")
//								.split(",");
//							myself.addParameters(index,value[0],(value[1] != undefined ? value[1] : ""),content);
//							index++;
//						}
						$.prompt('<div id="' + myself.cssPrefix + '" class="' + myself.cssPrefix +'Container">' + content.html() + '</div>',{ buttons: { Ok: true, Cancel: false} , prefix: 'jqi' + myself.cssPrefix,
							callback: function(v,m,f){
								if(v){
									// A bit of a hack to make null happen
									arrayValue = arrayValue.replace(/"null"/g,"null");
									myself.setExpression(row,arrayValue);
									_editArea.text(arrayValue);
								}
							},
							loaded: function(){
								$('.' + myself.cssPrefix + 'AddButton').bind('click',function(){
									myself.addParameters(index,"","",$("#" + myself.cssPrefix));
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
										if(myself.multiDimensionArray)
											array.push([$("#arg_" + i).val(),$("#val_" + i).val()]);
										else
											array.push($("#arg_" + i).val());
									}
								}
								arrayValue = array.length > 0 ? JSON.stringify(array) : "[]";
							}
							});
					 });
					 
					_editArea.appendTo(placeholder);
				}
				else{
					$("<td>"+ getExpression(row) +"</td>").appendTo(placeholder);
				}
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
					$.each(filters,function(i,filter){content += '<li><div onClick="ValuesArrayRenderer.setParameterValue(\'' + id + '\',\'' + filter.properties[0].value + '\')">' + filter.properties[0].value + '</div></li>';});
					content += '</ul>';
				}
				
				$.prompt(content + '</div>',{buttons: {Cancel: false}, prefix:'jqiStringListParameters',focus:1});
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

		constructor: function(){
			this.base();
			this.logger = new Logger("ArrayRenderer");
			this.logger.debug("Creating new ArrayRenderer");
		}
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


// Date Parameter - disabled for now
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
			_stub.properties.push(PropertiesManager.getProperty("propertyDateDim"));

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

		constructor: function(){
			this.base();
			this.logger = new Logger("CdaQueryRenderer");
			this.logger.debug("Creating new CdaQueryRenderer");
			this.wizard = "CDA_WIZARD";
		}
});

