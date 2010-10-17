var PromptWizardManager = Base.extend({
	},{
		WizardsArray: {},
		
		register: function(wizardType){
			PromptWizardManager.WizardsArray[wizardType.getId()] = wizardType;
		},
		
		getWizard: function(wizardType){
			return PromptWizardManager.WizardsArray[wizardType];
		}
	}
);

var PromptWizard = WizardManager.extend({

		wizardId: "PROMPT_WIZARD",
		title: "Prompt Wizard",
		
		hasFunctions: true,
		hasParameters: true,

		constructor: function(){
			this.base();
			this.logger = new Logger("PromptWizard" );
			this.functions = [];
		},
		
		getId: function(){
			return this.wizardId;
		},
		
		setInvoker: function(invoker){
			this.invoker = invoker;
		},
		
		getPropertyValue: function(id){
			return this.invoker.getPropertyValue(id)
		},
		
		getFunctionValue: function(_function){
			this.logger.warn("PromptWizard: Method not implmemented");
			return "";
		},
		
		getParameterValue: function(parameterProperties){
			this.logger.warn("PromptWizard: Method not implmemented");
			return "";
		},
		
		renderWizard: function() {
		
			$("#cdfdd-wizard-button-ok").removeAttr("disabled");
		
			var leftSectionContent = $('\
				<div class="cdfdd-wizard-prompt-left-section span-5 last round">\
				</div>');
				
			if(this.hasFunctions)
				leftSectionContent.append('\
					<table id="prompt-wizard-functions" class="prompt-wizard-functions">\
							<tbody class="ui-widget-content">\
							</tbody>\
							</table>');
							
			if(this.hasParameters)
				leftSectionContent.append('\
					<table id="prompt-wizard-parameters" class="">\
							<tbody class="ui-widget-content">\
							</tbody>\
							</table>');
				
			var centerSectionContent = '\
				<div class="cdfdd-wizard-prompt-center-section span-19 last round">\
					<div class="span-18">\
						<textarea  class="prompt-wizard-textarea" >' + this.invoker.getValue() + '</textarea>\
					</div>\
				</div>';
				
			/*var rightSectionContent = $('\
				<div class="cdfdd-wizard-prompt-right-section span-6 last round">\
				</div>');*/

			$("#" + WizardManager.WIZARD_LEFT_SECTION).append( leftSectionContent );
			$("#" + WizardManager.WIZARD_CENTER_SECTION).append( centerSectionContent );
			//$("#" + WizardManager.WIZARD_RIGHT_SECTION).append( rightSectionContent );
			
			$(".round","#" + WizardManager.WIZARD_BODY).corner();
			
			this.renderLeftPanel();
		},
		
		renderLeftPanel: function(){
			
			//1. RENDER FUNCTIONS
			if(this.hasFunctions){

				var functionsTBody = $("#prompt-wizard-functions > tbody");
				functionsTBody.empty();
				functionsTBody.append("<tr id='functions' class='ui-state-hover expanded'><td>Functions</td></tr>");
				
				var myself = this;
				$.each(this.functions,function(i,_function){
					var parentId = "function-" +  _function.parent.replace(/ /g,"_").toLowerCase();
					if($("#" + parentId).length == 0)
						functionsTBody.append("<tr id='" + parentId + "' class='child-of-functions prompt-wizard-elements small' ><td>" + _function.parent + "</td></tr>");
					
					var jsFunction = $("'<td class='draggableLevel'>" + _function.name + "</td>'");
					functionsTBody.append($("<tr id='" + _function.type + "' class='child-of-" + parentId + " prompt-wizard-elements small'></tr>").append(jsFunction));
					jsFunction.click(function() { 
						$(".prompt-wizard-textarea").insertAtCaret(myself.getFunctionValue(_function));
						return false
					});

				});
				
				functionsTBody.parent().treeTable();
			}
			
			//1. RENDER PARAMETERS
			if(this.hasParameters){
			
				var parameters = Panel.getPanel(ComponentsPanel.MAIN_PANEL).getParameters(); 
				
				var parametersTBody = $("#prompt-wizard-parameters > tbody");
				parametersTBody.empty();
				parametersTBody.append("<tr id='parameters' class='ui-state-hover expanded'><td>Parameters</td></tr>");
				var myself = this;
				var parameterIdx = 0;
				$.each(parameters,function(i,param){
					if(myself.getPropertyValue("name") != param.properties[0].value){
						var parameterId = "parmeter-"+(++parameterIdx);
						var parentId = "parameter-" + param.properties[1].type.replace(/ /g,"_").toLowerCase();
						if($("#" + parentId).length == 0)
							parametersTBody.append("<tr id='" + parentId + "' class='child-of-parameters prompt-wizard-elements small' ><td>" + param.properties[1].type + "</td></tr>");
						
						var parameter = $("<td>"+param.properties[0].value+"</td>");
						parametersTBody.append($("<tr id='"+ parameterId +"' class='child-of-" + parentId + " prompt-wizard-elements small'></tr>").append(parameter));
						parameter.click(function() { 
							$(".prompt-wizard-textarea").insertAtCaret(myself.getParameterValue(param.properties));
							return false
						});
					}
				});
				
				parametersTBody.parent().treeTable();
			}
		},

		buttonOk: function(){
			this.apply();
			$('#'+ WizardManager.MAIN_DIALOG).jqmHide();
		},
		
		buttonCancel: function(){
			$('#'+ WizardManager.MAIN_DIALOG).jqmHide();
		},

		apply: function(){

			// set value. We need to add a space to prevent a string like function(){}
			// to be interpreted by json as a function instead of a string
			var value = $(".prompt-wizard-textarea").val();
			if(value.length != 0 && value.substr(value.length-1,1)!=" "){
				value = value+" ";
			}

			this.invoker.promptCallback(value);
		}

	},{
	
		WIZARD_RIGHT_SECTION: "cdfdd-wizard-prompt-right-section",
		
		WIZARD_LEFT_SECTION: "cdfdd-wizard-prompt-left-section"

});


var JavascriptWizard = PromptWizard.extend({

		wizardId: "JAVASCRIPT_WIZARD",
		title: "Javascript Wizard",
		hasOlap: false,

		constructor: function(){
			this.base();
			this.logger = new Logger("JavascriptWizard" );
			this.functions = WizardFunctionsManager.getWizardFunctions("JavascriptWizard").getFunctions();
		},
		
		getParameterValue: function(parameterProperties){
			return parameterProperties[0].value;
		},
		
		getFunctionValue: function(_function){
			return _function.value;
		}
		
	},{

});

PromptWizardManager.register(new JavascriptWizard());

var MdxWizard = PromptWizard.extend({
	
		wizardId: "MDX_WIZARD",
		title: "Mdx Wizard",

		constructor: function(){
			this.base();
			this.logger = new Logger("MdxWizard" );
			this.functions = WizardFunctionsManager.getWizardFunctions("MdxWizard").getFunctions();
		},
		
		getParameterValue: function(parameterProperties){
			return "${" + parameterProperties[0].value + "}";
		},
		
		getFunctionValue: function(_function){
			return _function.value;
		},
		
		getMdxValue: function(level){
			return level.defaultMemberQualifiedName != undefined ? level.defaultMemberQualifiedName : level.qualifiedName;
		},
		
		buttonPreview: function(){
			
			var myself = this;
			var queryDefinition = {
				queryType: 'mdx',
				topCount:10,
				jndi: myself.getPropertyValue("jndi"),
				catalog: myself.getPropertyValue("catalog"),
				query: $(".prompt-wizard-textarea").val()
			};
			
			PivotLinkComponent.openPivotLink({pivotDefinition:queryDefinition});
		},
		
		renderWizard: function(){
			
			this.base();
			
			$("." + PromptWizard.WIZARD_LEFT_SECTION).append('\
				<table id="prompt-wizard-olap" class="prompt-wizard-olap">\
					<thead>\
					</thead>\
					<tbody class="ui-widget-content">\
					</tbody>\
					</table>');
			
			$(".cdfdd-wizard-buttons form").prepend('<input id="cdfdd-wizard-button-preview" type="button" onclick="PromptWizardManager.getWizard(\''+ this.wizardId +'\').buttonPreview()" value="Preview"></input>');
			
			// Fetch dimension structure of selected cube from server
			var params = {
				operation: "GetCubeStructure",
				catalog: this.getPropertyValue("catalog"),
				cube: this.getPropertyValue("cube"),
				jndi: this.getPropertyValue("jndi")
			};
			var myself = this;
			
			this.logger.debug("Getting olap:");
			this.logger.debug("Catalog:" + params.catalog);
			this.logger.debug("Cube:" + params.cube);
			this.logger.debug("Jndi:" + params.jndi);

			$.getJSON(CDFDDServerUrl + "OlapUtils", params, function(json) {
					if(json.status == "true"){
						
						var dimensions = json.result.dimensions;
						var measures = json.result.measures;
						var olapIdx = 0;
						var dimensionTBody = $("#prompt-wizard-olap > tbody");
						dimensionTBody.empty();
						dimensionTBody.append("<tr id='olap' class='ui-state-hover expanded'><td>Olap:</td></tr>");
						
						$.each(dimensions,function(i,dimension){
								var hierarchies = dimension.hierarchies;
								$.each(hierarchies,function(j,hierarchy){
										var hierarchyId = "dimRow-"+(++olapIdx);
										var dim = $('<td>'+hierarchy.name+'</td>');
										dimensionTBody.append($("<tr id='"+ hierarchyId +"' class='child-of-olap prompt-wizard-elements small'></tr>").append(dim));
										dim.bind("click", function(e){
											if($(e.originalTarget).find("span").length > 0 )$(".prompt-wizard-textarea").insertAtCaret(myself.getMdxValue(hierarchy));return false;
										});
										
										var levels = hierarchy.levels;
										$.each(levels,function(k,level){
												var levelId = "dimRow-"+(++olapIdx);
												var dimLevel = $('<td>'+level.name+'</td>');
												dimensionTBody.append($("<tr id='"+ levelId +"' class='child-of-"+hierarchyId+" prompt-wizard-elements small'></tr>").append(dimLevel));
												dimLevel.click(function() {$(".prompt-wizard-textarea").insertAtCaret(myself.getMdxValue(level));return false});
										});
								});
						});
						
						dimensionTBody.append("<tr id='olap-mesaures' class='child-of-olap prompt-wizard-elements small'><td>Measures</td></tr>");
						$.each(measures,function(i,m){
							var measure = $('<td>'+m.name+'</td>');
							var measureId = "dimMeasure-"+(++olapIdx);
							dimensionTBody.append($("<tr id='"+ measureId +"' class='child-of-olap-mesaures prompt-wizard-elements small'></tr>").append(measure));
							measure.click(function() {$(".prompt-wizard-textarea").insertAtCaret(myself.getMdxValue(m));return false});
						})
						
						dimensionTBody.parent().treeTable();
					}
			});
			
			
		}
		
	},{

});

PromptWizardManager.register(new MdxWizard());


var SqlWizard = PromptWizard.extend({

		wizardId: "SQL_WIZARD",
		title: "Sql Wizard",
		hasOlap: false,

		constructor: function(){
			this.base();
			this.logger = new Logger("SqlWizard" );
			this.functions = WizardFunctionsManager.getWizardFunctions("SqlWizard").getFunctions();
		},
		
		getParameterValue: function(parameterProperties){
			return parameterProperties[0].value;
		},
		
		getFunctionValue: function(_function){
			return _function.value;
		}
		
	},{

});

PromptWizardManager.register(new SqlWizard());

var CdaWizard = PromptWizard.extend({

		wizardId: "CDA_WIZARD",
		title: "CDA Wizard",
		hasOlap: false,

		constructor: function(){
			this.base();
			this.logger = new Logger("CdaWizard" );
			this.functions = WizardFunctionsManager.getWizardFunctions("CdaWizard").getFunctions();
		},
		
		getParameterValue: function(parameterProperties){
			return parameterProperties[0].value;
		},
		
		getFunctionValue: function(_function){
			return _function.value;
		}
		
	},{

});

PromptWizardManager.register(new CdaWizard());
