/*!
 * Copyright 2002 - 2015 Webdetails, a Pentaho company. All rights reserved.
 *
 * This software was developed by Webdetails and is provided under the terms
 * of the Mozilla Public License, Version 2.0, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */

var PromptWizardManager = Base.extend({}, {
  WizardsArray: {},

  register: function(wizardType) {
    PromptWizardManager.WizardsArray[wizardType.getId()] = wizardType;
  },

  getWizard: function(wizardType) {
    return PromptWizardManager.WizardsArray[wizardType];
  }
});

var PromptWizard = WizardManager.extend({

  wizardId: "PROMPT_WIZARD",
  title: "Prompt Wizard",

  hasFunctions: true,
  hasParameters: true,

  constructor: function() {
    this.base();
    this.logger = new Logger("PromptWizard");
    this.functions = [];
  },

  getId: function() {
    return this.wizardId;
  },

  setInvoker: function(invoker) {
    this.invoker = invoker;
  },

  getPropertyValue: function(id) {
    return this.invoker.getPropertyValue(id)
  },

  getFunctionValue: function(_function) {
    this.logger.warn("PromptWizard: Method not implmemented");
    return "";
  },

  getParameterValue: function(parameterProperties) {
    this.logger.warn("PromptWizard: Method not implmemented");
    return "";
  },

  renderWizard: function () {

    $("#cdfdd-wizard-button-ok").removeAttr("disabled");

    var leftSectionContent = $('<div class="cdfdd-wizard-prompt-left-section span-5 last round">\n' +
    '</div>');

    if(this.hasFunctions)
      leftSectionContent.append('<table id="prompt-wizard-functions" class="prompt-wizard-functions">\n' +
      '	<tbody class="ui-widget-content">\n' +
      '	</tbody>\n' +
      '</table>');

    if(this.hasParameters)
      leftSectionContent.append('<table id="prompt-wizard-parameters" class="">\n' +
      '	<tbody class="ui-widget-content">\n' +
      '	</tbody>\n' +
      '</table>');

    var centerSectionContent = '<div class="cdfdd-wizard-prompt-center-section span-19 last round">\n' +
        '	<div class="span-18">\n' +
        '		<textarea  class="prompt-wizard-textarea" >' + this.invoker.getValue() + '</textarea>\n' +
        '	</div>\n' +
        '</div>';

    $("#" + WizardManager.WIZARD_LEFT_SECTION).append(leftSectionContent);
    $("#" + WizardManager.WIZARD_CENTER_SECTION).append(centerSectionContent);

    $(".round", "#" + WizardManager.WIZARD_BODY).corner();

    this.renderLeftPanel();
  },

  renderLeftPanel: function() {
    var myself = this;

    //1. RENDER FUNCTIONS
    if(this.hasFunctions) {

      var functionsTBody = $("#prompt-wizard-functions > tbody");
      functionsTBody.empty();
      functionsTBody.append("<tr id='functions' class='ui-state-hover expanded'><td>Functions</td></tr>");

      $.each(this.functions, function(i, _function) {
        var parentId = "function-" + _function.parent.replace(/ /g, "_").toLowerCase();
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
    if(this.hasParameters) {

      var parameters = Panel.getPanel(ComponentsPanel.MAIN_PANEL).getParameters();

      var parametersTBody = $("#prompt-wizard-parameters > tbody");
      parametersTBody.empty();
      parametersTBody.append("<tr id='parameters' class='ui-state-hover expanded'><td>Parameters</td></tr>");

      var parameterIdx = 0;
      $.each(parameters, function(i, param) {
        if(myself.getPropertyValue("name") != param.properties[0].value) {
          var parameterId = "parmeter-" + (++parameterIdx);
          var parentId = "parameter-" + param.properties[1].type.replace(/ /g, "_").toLowerCase();
          if($("#" + parentId).length == 0)
            parametersTBody.append("<tr id='" + parentId + "' class='child-of-parameters prompt-wizard-elements small' ><td>" + param.properties[1].type + "</td></tr>");

          var parameter = $("<td>" + param.properties[0].value + "</td>");
          parametersTBody.append($("<tr id='" + parameterId + "' class='child-of-" + parentId + " prompt-wizard-elements small'></tr>").append(parameter));
          parameter.click(function() {
            $(".prompt-wizard-textarea").insertAtCaret(myself.getParameterValue(param.properties));
            return false
          });
        }
      });

      parametersTBody.parent().treeTable();
    }
  },

  buttonOk: function() {
    this.apply();
    $('#' + WizardManager.MAIN_DIALOG).jqmHide();
  },

  buttonCancel: function() {
    $('#' + WizardManager.MAIN_DIALOG).jqmHide();
  },

  apply: function() {

    // set value. We need to add a space to prevent a string like function(){}
    // to be interpreted by json as a function instead of a string
    var value = $(".prompt-wizard-textarea").val();
    if(value.length != 0 && value.substr(value.length - 1, 1) != " ") {
      value = value + " ";
    }

    this.invoker.promptCallback(value);
  }

}, {
  WIZARD_RIGHT_SECTION: "cdfdd-wizard-prompt-right-section",
  WIZARD_LEFT_SECTION: "cdfdd-wizard-prompt-left-section"
});

var MdxWizard = PromptWizard.extend({

  wizardId: "MDX_WIZARD",
  title: "Mdx Wizard",

  constructor: function() {
    this.base();
    this.logger = new Logger("MdxWizard");
    this.functions = WizardFunctionsManager.getWizardFunctions("MdxWizard").getFunctions();
  },

  getParameterValue: function(parameterProperties) {
    return "${" + parameterProperties[0].value + "}";
  },

  getFunctionValue: function(_function) {
    return _function.value;
  },

  getMdxValue: function(level) {
    return level.defaultMemberQualifiedName != undefined ? level.defaultMemberQualifiedName : level.qualifiedName;
  },

  buttonPreview: function() {

    var myself = this;
    var queryDefinition = {
      queryType: 'mdx',
      topCount: 10,
      jndi: myself.getPropertyValue("jndi"),
      catalog: myself.getPropertyValue("catalog"),
      query: $(".prompt-wizard-textarea").val()
    };

    PivotLinkComponent.openPivotLink({pivotDefinition: queryDefinition});
  },

  renderWizard: function() {

    this.base();

    $("." + PromptWizard.WIZARD_LEFT_SECTION).append('<table id="prompt-wizard-olap" class="prompt-wizard-olap">\n' +
    '	<thead>\n' +
    '	</thead>\n' +
    '	<tbody class="ui-widget-content">\n' +
    '	</tbody>\n' +
    '</table>');

    $(".cdfdd-wizard-buttons form").prepend('<input id="cdfdd-wizard-button-preview" type="button" onclick="PromptWizardManager.getWizard(\'' + this.wizardId + '\').buttonPreview()" value="Preview"></input>');

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
      if(json.status == "true") {

        var dimensions = json.result.dimensions;
        var measures = json.result.measures;
        var olapIdx = 0;
        var dimensionTBody = $("#prompt-wizard-olap > tbody");
        dimensionTBody.empty();
        dimensionTBody.append("<tr id='olap' class='ui-state-hover expanded'><td>Olap:</td></tr>");

        $.each(dimensions, function(i, dimension) {
          var hierarchies = dimension.hierarchies;
          $.each(hierarchies, function(j, hierarchy) {
            var hierarchyId = "dimRow-" + (++olapIdx);
            var dim = $('<td>' + hierarchy.name + '</td>');
            dimensionTBody.append($("<tr id='" + hierarchyId + "' class='child-of-olap prompt-wizard-elements small'></tr>").append(dim));
            dim.bind("click", function(e) {
              if($(e.originalTarget).find("span").length > 0)$(".prompt-wizard-textarea").insertAtCaret(myself.getMdxValue(hierarchy));
              return false;
            });

            var levels = hierarchy.levels;
            $.each(levels, function(k, level) {
              var levelId = "dimRow-" + (++olapIdx);
              var dimLevel = $('<td>' + level.name + '</td>');
              dimensionTBody.append($("<tr id='" + levelId + "' class='child-of-" + hierarchyId + " prompt-wizard-elements small'></tr>").append(dimLevel));
              dimLevel.click(function() {
                $(".prompt-wizard-textarea").insertAtCaret(myself.getMdxValue(level));
                return false
              });
            });
          });
        });

        dimensionTBody.append("<tr id='olap-mesaures' class='child-of-olap prompt-wizard-elements small'><td>Measures</td></tr>");
        $.each(measures, function(i, m) {
          var measure = $('<td>' + m.name + '</td>');
          var measureId = "dimMeasure-" + (++olapIdx);
          dimensionTBody.append($("<tr id='" + measureId + "' class='child-of-olap-mesaures prompt-wizard-elements small'></tr>").append(measure));
          measure.click(function() {
            $(".prompt-wizard-textarea").insertAtCaret(myself.getMdxValue(m));
            return false
          });
        });

        dimensionTBody.parent().treeTable();
      }
    });


  }

}, {
});
PromptWizardManager.register(new MdxWizard());

var AcePromptWizard = PromptWizard.extend({

  mode: 'none',
  editor: null,
  EDITOR_ID: 'wizardEditor',

  renderWizard: function() {

    $("#cdfdd-wizard-button-ok").removeAttr("disabled");
    $('#wizardDialog').addClass('prompt-wizard-dialog');

    var leftSectionContent = $('<div class="cdfdd-wizard-prompt-left-section last">\n' +
    '</div>');

    if(this.hasFunctions) {
      leftSectionContent.append('' +
        '<div id="prompt-wizard-functions" class="prompt-accordion-container collapsed">' +
        '  <div class="prompt-wizard-caption">' +
        '    <span>Functions</span>' +
        '    <span id="prompt-functions-toggle" class="prompt-caption-more-less"></span>' +
        '  </div>' +
        '  <div id="prompt-functions-accordion" class="prompt-wizard-accordion"></div>' +
        '</div>');

    }

    if(this.hasParameters) {
      leftSectionContent.append('' +
        '<div id="prompt-wizard-parameters" class="prompt-accordion-container collapsed">' +
        '  <div class="prompt-wizard-caption">' +
        '    <span>Parameters</span>' +
        '    <span id="prompt-parameters-toggle" class="prompt-caption-more-less"></span>' +
        '  </div>' +
        '  <div id="prompt-parameters-accordion" class="prompt-wizard-accordion"></div>' +
        '</div>');
    }

    var rightSectionContent = '<div class="cdfdd-wizard-prompt-right-section last">\n' +
        '   <pre class="prompt-wizard-text-area" id="' + this.EDITOR_ID + '"></pre>\n' +
        '</div>';

    $("#" + WizardManager.WIZARD_LEFT_SECTION).append(leftSectionContent);
    $("#" + WizardManager.WIZARD_RIGHT_SECTION).append(rightSectionContent);

    this.renderLeftPanel();
  },

  renderLeftPanel: function() {
    if(this.hasFunctions) {
      this.renderFunctions();
    }

    if(this.hasParameters) {
      this.renderParameters();
    }
  },

  renderFunctions: function() {
    var functionsHolder = $("#prompt-functions-accordion");
    var myself = this;

    $.each(this.functions, function(i, _function) {
      if(_function != null) {
        var parentId = "function-" + _function.parent.replace(/ /g, "_").toLowerCase();
        if($("#" + parentId).length == 0) {
          functionsHolder.append('' +
            '<div>\n' +
            '  <h3 class="prompt-wizard-elements">' + _function.parent + '</h3>' +
            '  <div id="' + parentId + '"><ul></ul></div>\n' +
            '</div>');
        }

        var jsFunction = $('<li id="' + _function.type + '" class="prompt-wizard-elements">' + _function.name + '</li>');
        functionsHolder
            .find('#' + parentId + ' ul')
            .append(jsFunction);
        jsFunction.click(function() {
          myself.editor.insert(myself.getFunctionValue(_function));
          return false;
        });
      }
    });
    functionsHolder.accordion({
      header: 'h3',
      active: false,
      heightStyle: "content",
      collapsible: true
    });

    $('.prompt-wizard-caption', $('#prompt-wizard-functions'))
        .click(function() {
          $(this).parent().toggleClass('collapsed');
    });
  },

  renderParameters: function() {
    var parameters = Panel.getPanel(ComponentsPanel.MAIN_PANEL).getParameters();
    var parametersHolder = $("#prompt-parameters-accordion");
    var myself = this;

    $.each(parameters, function(i, param) {
      var name = param.properties[0].value;
      var type = param.properties[1].type;

      if(myself.getPropertyValue("name") != name) {
        var parameterId = "parameter-" + name;
        var parentId = "parameter-" + type.replace(/ /g, "_").toLowerCase();
        if($("#" + parentId).length == 0) {
          parametersHolder.append('' +
            '<div>\n' +
            '  <h3 class="prompt-wizard-elements">' + type + '</h3>' +
            '  <div id="' + parentId + '"><ul></ul></div>\n' +
            '</div>');
        }
        var parameter = $('<li id="' + parameterId + '" class="prompt-wizard-elements">' + name + '</li>');
        parametersHolder
            .find('#' + parentId + ' ul')
            .append(parameter);

        parameter.click(function() {
          myself.editor.insert(myself.getParameterValue(param.properties));
          return false
        });
      }
    });

    parametersHolder.accordion({
      header: 'h3',
      active: false,
      heightStyle: "content",
      collapsible: true
    });

    $('.prompt-wizard-caption', $('#prompt-wizard-parameters'))
        .click(function() {
          $(this).parent().toggleClass('collapsed');
    });
  },

  postRenderWizard: function() {
    this.editor = new CodeEditor();
    this.editor.initEditor(this.EDITOR_ID);
    this.editor.setMode(this.mode);
    var content = this.invoker.getValue();
    if (!content && this.queryTemplate) {
      content = this.queryTemplate;
    }
    this.editor.setContents(content);
  },

  apply: function() {
    var value = this.editor.getContents();
    this.invoker.promptCallback(value);
  }

});

var JavascriptWizard = AcePromptWizard.extend({

  wizardId: "JAVASCRIPT_WIZARD",
  title: "Javascript Wizard",
  hasOlap: false,
  mode: 'javascript',

  constructor: function() {
    this.base();
    this.logger = new Logger("JavascriptWizard");
    this.functions = WizardFunctionsManager.getWizardFunctions("JavascriptWizard").getFunctions();
  },

  getParameterValue: function(parameterProperties) {
    return parameterProperties[0].value;
  },

  getFunctionValue: function(_function) {
    return _function.value;
  },

  apply: function() {
    var value = this.editor.getContents();

    // set value. We need to add a space to prevent a string like function(){}
    // to be interpreted by json as a function instead of a string
    if(value && value.length != 0 && value.substr(value.length - 1, 1) != " ") {
      value = value + " ";
    }

    this.invoker.promptCallback(value);
  }

}, {
});
PromptWizardManager.register(new JavascriptWizard());

var CurrentMdxEditor = AcePromptWizard.extend({
  
    wizardId: "CURRENTMDX_EDITOR",
    title: "MDX Editor", 
    constructor: function(){
      this.base();
      this.logger = new Logger("CurrentMdxEditor" );
      this.hasFunctions = false;
      this.queryTemplate = 'select {} ON COLUMNS,\n' +
      '       {} ON ROWS\n' +
      '       from []';
    }

});
PromptWizardManager.register(new CurrentMdxEditor());

var SqlWizard = AcePromptWizard.extend({

  wizardId: "SQL_WIZARD",
  title: "Sql Editor",
  hasOlap: false,
  mode: 'sql',//TODO: unimplemented

  constructor: function() {
    this.base();
    this.logger = new Logger("SqlWizard");
    this.functions = WizardFunctionsManager.getWizardFunctions("SqlWizard").getFunctions();
  },

  getParameterValue: function(parameterProperties){
    return "${" + parameterProperties[0].value + "}";
  },

  getFunctionValue: function(_function) {
    return _function.value;
  }

}, {
});
PromptWizardManager.register(new SqlWizard());

//TODO: is this used anywhere?..
var CdaWizard = AcePromptWizard.extend({

  wizardId: "CDA_WIZARD",
  title: "CDA Wizard",
  hasOlap: false,

  mode: 'xml',

  constructor: function() {
    this.base();
    this.logger = new Logger("CdaWizard");
    this.functions = WizardFunctionsManager.getWizardFunctions("CdaWizard").getFunctions();
  },

  getParameterValue: function(parameterProperties) {
    return parameterProperties[0].value;
  },

  getFunctionValue: function(_function) {
    return _function.value;
  }

}, {
});
PromptWizardManager.register(new CdaWizard());

var DefaultEditor = AcePromptWizard.extend({ 

		wizardId: "DEFAULT_EDITOR",
		title: "Default Editor",
		
		mode: 'text',

		constructor: function(){
			this.base();
			this.logger = new Logger("DefaultEditor" );
			this.hasFunctions = false;
		},
		
		getParameterValue: function(parameterProperties){
			return "${" + parameterProperties[0].value + "}";
		},
		
		getFunctionValue: function(_function){
			return _function.value;
		}
		
	},{
});
PromptWizardManager.register(new DefaultEditor());

var MqlEditor = AcePromptWizard.extend({ 

		wizardId: "MQL_EDITOR",
		title: "MQL Editor",
		
		mode: 'text',

		constructor: function(){
			this.base();
			this.logger = new Logger("MqlEditor" );
			this.hasFunctions = false;
      this.queryTemplate ='<![CDATA[<?xml version="1.0" encoding="UTF-8"?>\n' +
        '<mql>\n' +
        ' <domain_type></domain_type>\n' +
        ' <domain_id></domain_id>\n' +
        ' <model_id></model_id>\n' +
        ' <model_name></model_name>\n' +
        ' <selections>\n' +
        '   <!-- Example selection\n' +
        '              <selection>\n' +
        '     <view>CAT_ORDERS</view>\n' +
        '     <column>BC_ORDERS_ORDERDATE</column>\n' +
        '   </selection>\n' +
        '     -->\n' +
        ' </selections>\n' +
        ' <constraints>\n' +
        '   <!-- Example constraint\n' +
        '   <constraint>\n' +
        '     <operator>AND</operator>\n' +
        '     <condition>[CAT_ORDERS.BC_ORDERDETAILS_QUANTITYORDERED] &gt;70</condition>\n' +
        '   </constraint>\n' +
        '   -->\n' +
        ' </constraints>\n' +
        ' <orders/>\n' +
        '</mql>]]>\n';
		},
		
		getParameterValue: function(parameterProperties){
			return "${" + parameterProperties[0].value + "}";
		},
		
		getFunctionValue: function(_function){
			return _function.value;
		}
		
	},{
});
PromptWizardManager.register(new MqlEditor());

var ScriptableEditor = AcePromptWizard.extend({ 

		wizardId: "SCRIPTABLE_EDITOR",
		title: "Scriptable Editor",
		
		mode: 'text',

		constructor: function(){
			this.base();
			this.logger = new Logger("ScriptableEditor" );
			this.hasFunctions = false;
      this.applyTemplate();
    },
		
		getParameterValue: function(parameterProperties){
			return "${" + parameterProperties[0].value + "}";
		},
		
		getFunctionValue: function(_function){
			return _function.value;
		},

    applyTemplate: function() {
      this.queryTemplate = 'import org.pentaho.reporting.engine.classic.core.util.TypedTableModel;\n' +
        '\n' +
        'String[] columnNames = new String[]{\n' +
        '"value","name2"\n' +
        '};\n' +
        '\n' +
        '\n' +
        'Class[] columnTypes = new Class[]{\n' +
        'Integer.class,\n' +
        'String.class\n' +
        '};\n' +
        '\n' +
        'TypedTableModel model = new TypedTableModel(columnNames, columnTypes);\n' +
        '\n' +
        'model.addRow(new Object[]{ new Integer("0"), new String("Name") });\n' +
        '\n' +
        'return model;';
    }
	},{
});
PromptWizardManager.register(new ScriptableEditor());

var JsonScriptableEditor = ScriptableEditor.extend({

  wizardId: "JSON_SCRIPTABLE_EDITOR",

  constructor: function(){
    this.base();
    this.logger = new Logger("JsonScriptableEditor" );
    this.hasFunctions = false;
    this.applyTemplate();
  },

  applyTemplate: function() {
    this.queryTemplate = '' +
    '{\n' +
    '   "resultset":[\n' +
    '      ["Name", 0]\n' +
    '   ],\n\n' +
    '   "metadata":[\n' +
    '      {"colIndex":0,"colType":"String","colName":"value"},\n' +
    '      {"colIndex":1,"colType":"Integer","colName":"name2"}\n' +
    '   ]\n' +
    '}';
  }
}, {
});
PromptWizardManager.register(new JsonScriptableEditor());

var XPathEditor = AcePromptWizard.extend({ 

		wizardId: "XPATH_EDITOR",
		title: "XPath Editor",
		
		mode: 'text',

		constructor: function(){
			this.base();
			this.logger = new Logger("XPathEditor" );
			this.hasFunctions = false;
	        this.queryTemplate = "/*/*[CUSTOMERS_CUSTOMERNUMBER=103]";
		},
		
		getParameterValue: function(parameterProperties){
			return "${" + parameterProperties[0].value + "}";
		},
		
		getFunctionValue: function(_function){
			return _function.value;
		}
		
	},{
});
PromptWizardManager.register(new XPathEditor());
