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

var PromptRenderer = CellRenderer.extend({

  callback: null,
  editArea: null,
  value: null,

  constructor: function(tableManager) {
    this.base(tableManager);
    this.logger = new Logger("PromptRenderer");
    this.logger.debug("Creating new PromptRenderer");
    this.wizard = "PROMPT_WIZARD";
  },

  render: function(placeholder, value, callback) {

    var _editArea = $('<td><div style="float:left"><code></code></div><div class="edit" style="float:right"></div></td>');

    var wizard = PromptWizardManager.getWizard(this.wizard);
    value = this.getQueryTemplateValue(wizard, value);

    _editArea.find("code").text(this.getFormattedValue(value));
    var myself = this;

    var _prompt = $('<button class="cdfddInput">...</button>').bind("click", function() {

      // Storing the var for later use when render() is not called again
      wizard.setInvoker(myself);
      myself.callback = callback;
      myself.editArea = _editArea;
      myself.value = value;
      wizard.render();
    }).appendTo($("div.edit", _editArea));

    _editArea.appendTo(placeholder);
  },

  getQueryTemplateValue: function(wizard, value) {

    if(!value && wizard.queryTemplate) {
      value = wizard.queryTemplate;

      var tableModel = this.tableManager.getTableModel();
      var _setExpression = tableModel.getColumnSetExpressions()[1];
      var colIdx = tableModel.getRowIndexByName('query');
      var property = tableModel.getData()[colIdx];

      _setExpression.apply(this.tableManager, [property, value]);
    }

    return value;
  },

  validate: function(settings, original) {
    return true;
  },

  getFormattedValue: function(_value) {

    if(_value.length > 30) {
      _value = _value.substring(0, 20) + " (...)";
    }
    return _value;
  },

  getValue: function() {
    return this.value;
  },

  getPropertyValue: function(id) {
    return this.getTableManager().getTableModel().getRowByName(id).value;
  },

  promptCallback: function(value) {
    this.callback(value);
    this.value = value;
    this.editArea.find("code").text(value.length > 30 ? value.substring(0, 20) + " (...)" : value);
  }
});

var JavaScriptRenderer = PromptRenderer.extend({

  constructor: function(tableManager) {
    this.base(tableManager);
    this.logger = new Logger("JavascriptRenderer");
    this.logger.debug("Creating new JascriptRenderer");
    this.wizard = "JAVASCRIPT_WIZARD";
  }
});

var MdxQueryRenderer = PromptRenderer.extend({

  constructor: function(tableManager) {
    this.base(tableManager);
    this.logger = new Logger("MdxQueryRenderer");
    this.logger.debug("Creating new MdxQueryRenderer");
    this.wizard = "MDX_WIZARD";
  }
});

var CurrentMdxQueryRenderer = PromptRenderer.extend({ 

  constructor: function(tableManager){
    this.base(tableManager);
    this.logger = new Logger("CurrentMdxQueryRenderer");
    this.logger.debug("Creating new CurrentMdxQueryRenderer");
    this.wizard = "CURRENTMDX_EDITOR";
  }
});

var SqlQueryRenderer = PromptRenderer.extend({

  constructor: function(tableManager) {
    this.base(tableManager);
    this.logger = new Logger("SqlQueryRenderer");
    this.logger.debug("Creating new SqlQueryRenderer");
    this.wizard = "SQL_WIZARD";
  }
});

var MqlQueryRenderer = PromptRenderer.extend({

  constructor: function(tableManager){
    this.base(tableManager);
    this.logger = new Logger("MqlQueryRenderer");
    this.logger.debug("Creating new MqlQueryRenderer");
    this.wizard = "MQL_EDITOR";
  }
});

var XPathQueryRenderer = PromptRenderer.extend({

  constructor: function(tableManager){
    this.base(tableManager);
    this.logger = new Logger("XPathQueryRenderer");
    this.logger.debug("Creating new XPathQueryRenderer");
    this.wizard = "XPATH_EDITOR";
  }
});

var ScriptableQueryRenderer = PromptRenderer.extend({

  constructor: function(tableManager){
    this.base(tableManager);
    this.logger = new Logger("ScriptableQueryRenderer");
    this.logger.debug("Creating new ScriptableQueryRenderer");
    this.wizard = "SCRIPTABLE_EDITOR";
  }
});

var JsonScriptableQueryRenderer = PromptRenderer.extend({

  constructor: function(tableManager){
    this.base(tableManager);
    this.logger = new Logger("JsonScriptableQueryRenderer");
    this.logger.debug("Creating new JsonScriptableQueryRenderer");
    this.wizard = "JSON_SCRIPTABLE_EDITOR";
  }
});

var DefaultQueryRenderer = PromptRenderer.extend({

  constructor: function(tableManager){
    this.base(tableManager);
    this.logger = new Logger("DefaultQueryRenderer");
    this.logger.debug("Creating new DefaultQueryRenderer");
    this.wizard = "DEFAULT_EDITOR";
  }
});

var ValuesArrayRenderer = CellRenderer.extend({

  multiDimensionArray: true,

  cssPrefix: "StringList",

  hasTypedValues: false,//if true, args also have a type

  typesArray: [],//only used if hasTypedValues
  selectData: {},//to use with autocomplete

  //used for value input labels
  argTitle: 'Arg',
  valTitle: 'Value',

  constructor: function(tableManager) {
    this.base(tableManager);
    this.logger = new Logger("ValuesArrayRenderer");
    this.logger.debug("Creating new ValuesArrayRenderer");
  },

  render: function(placeholder, value, callback) {

    var _editArea = $("<td>" + (value.length > 30 ? (value.substring(0, 20) + " (...)") : value) + "</td>");
    var myself = this;

    _editArea.click(function() {

      var arrayValue = value;
      var content = $(
          '<div id="' + myself.cssPrefix + '" class="' + myself.cssPrefix + 'Container">\n' +
          ' <div class="' + myself.cssPrefix + '"></div>\n' +
          ' <input class="' + myself.cssPrefix + 'AddButton" type="button" value="Add"></input>\n' +
          '</div>');

      var vals = JSON.parse(arrayValue);

      for(var i = 0; i < vals.length; i++) {
        myself.addParameter(i, vals[i], content);
      }

      var index = vals.length;
      var wrapper = $("<div>", {id: myself.cssPrefix, 'class': myself.cssPrefix + 'Container'});
      wrapper.append(content);
      $.prompt(wrapper.html(), {

        buttons: {
          Ok: true,
          Cancel: false
        },

        prefix: "popup",

        callback: function(v, m, f) {
          if(v) {
            // A bit of a hack to make null happen
            arrayValue = arrayValue.replace(/"null"/g, "null");
            callback(arrayValue);
            _editArea.text(arrayValue);
          }
        },

        loaded: function() {
            //button bindings
            myself.onPopupLoad();
            if(myself.cssPrefix == "StringList") {
              $('.popup').css("width", "630px");//Since some objects extend from this one this is just to confirm we're resizing the right popup.
            }
            var addParamVal = _.bind(myself.addParameterValue, myself);
            $('.' + myself.cssPrefix + 'AddButton').bind('click', function() {
              if(myself.multiDimensionArray) {
                myself.addParameter(index, ["", "", ""], $("#" + myself.cssPrefix));
              }
              else {
                myself.addParameter(index, "", $("#" + myself.cssPrefix));
              }
              $("#remove_button_" + index).bind('click', myself.removeParameter);
              $("#parameter_button_" + index).bind('click', function(){addParamVal(this.id);});
              myself.addAutoComplete(index);
              index++;
            });
            $('.' + myself.cssPrefix + 'Remove').bind('click', myself.removeParameter);
            $('.' + myself.cssPrefix + 'Parameter').bind('click', function(){addParamVal(this.id);});
        },

        submit: function(v, m, f) {
          var array = [];
          for(var i = 0; i < index; i++) {
            var paramVal = myself.getParameterValues(i);
            if(paramVal != null && paramVal.length > 0 && paramVal[0] != null) array.push(paramVal); //don't attempt to add deleted lines
          }
          arrayValue = array.length > 0 ? JSON.stringify(array) : "[]";
        }
      });

      for(i = 0; i < vals.length; i++) {
        myself.addAutoComplete(i);
        myself.addFocusEvent(i);
      }

      $('.' + myself.cssPrefix + 'Container #arg_0').focus();

      if(!myself.multiDimensionArray) {
        myself.dragAndDrop();
      }
    });

    _editArea.appendTo(placeholder);

  },

  onPopupLoad: function(){
    //custom renderers may want to do something on popup load, this is here just as a hook 
  },

  validate: function(settings, original) {
    return true;
  },

  /**
   * @param i line number
   * @param values {Array}
   **/
  addParameter: function(i, values, container) {//TODO: still not done
    if(this.multiDimensionArray) {
      if(this.hasTypedValues) {
        var val = values[1] === undefined ? "" : values[1] === null ? "null" : values[1];
        var type = values.length >= 3 ? values[2] : null;
        this.addTypedParameters(i, values[0], val, type, container);
      } else {
        this.addParameters(i, values[0], (values[1] === undefined ? "" : values[1] === null ? "null" : values[1]), container);
      }
    } else {
      this.addParameters(i, values, "null", container);
      this.addFocusEvent(i);
    }
  },

  addParameters: function(i, arg, val, container) {

    if(val) {
      val = val.replace(/["]/g, '&quot;');
    }//for output only, will come back ok

    var parameterButton = this.getParameterButton(i);
    var removeButton = this.getRemoveButton(i);
    var argInput = this.getTextInput(this.argTitle, arg, this.cssPrefix + 'Args', 'arg_' + i);
    var valInput = this.getTextInput(this.valTitle, val, this.cssPrefix + 'Val', 'val_' + i);

    var row = this.getParameterRow(i, argInput, valInput, parameterButton, removeButton);

    var subContainer = container.find('.' + this.cssPrefix);
    subContainer.append(row);
    subContainer.find('#parameters_' + i + ' input:eq(0)').focus();
  },

  addFocusEvent: function(index) {
    //default does nothing
  },

  getParameterRow: function(i, argInput, valInput, parameterButton, removeButton){
    return '<div id="parameters_' + i + '" >\n' +
              argInput +
              (this.multiDimensionArray ?
                  ('<div class="' + this.cssPrefix + 'Values">' + valInput + parameterButton + removeButton + '</div><br />') :
                  removeButton) +
              '</div>\n';
  },

  addTypedParameters: function(i, arg, val, type, container) {//ToDo: should be refactored with addParameters, currently not used
    //used when hasTypedValues=true, assumes multiDimensionalArray

    if(val) {
      val = val.replace(/["]/g, '&quot;');
    }//for output only, will come back ok

    var parameterButton = this.getParameterButton(i);
    var removeButton = this.getRemoveButton(i);
    var argInput = this.getTextInput(this.argTitle, arg, this.cssPrefix + 'Args', 'arg_' + i);
    var valInput = this.getTextInput(this.valTitle, val, this.cssPrefix + 'Val', 'val_' + i);

    var typeSelect = this.getTypeSelector('Type', type, 'type_' + i);
    var row =
        '<div id="parameters_' + i + '" >\n' +
        argInput +
        '<div class="' + this.cssPrefix + 'Values">' + valInput + '</div>' + '<div class="' + this.cssPrefix + 'Types">' + typeSelect + parameterButton + removeButton + '<br /></div>\n';
    container.find('.' + this.cssPrefix).append(row);
  },

  /**
   * @returns {Array} Values to be stored for each parameter_<i>
   **/
  getParameterValues: function(i) {

    if(!this.multiDimensionArray) {
      return   $('#arg_' + i).val();
    } else {

      var result = [];

      result.push($('#arg_' + i).val());//name
      result.push($('#val_' + i).val());//value
      if(this.hasTypedValues) {
        result.push($('#type_' + i).val());//type
      }

      return result;
    }
  },

  //TODO: redo, move to another file

  //parameters field generation (begin)

  getParameterButton: function(i) {
    return '<input id="parameter_button_' + i + '" class="' + this.cssPrefix + 'Parameter" type="button" value="..."></input>\n';
  },

  getRemoveButton: function(i) {
    return '<input id="remove_button_' + i + '" class="' + this.cssPrefix + 'Remove" type="button" value="-" ></input>\n';
  },

  getTextInput: function(title, value, cssClass, id) {
    return '<div class="' + cssClass + '">' +
        (title != null ? ('<span class="' + this.cssPrefix + 'TextLabel">' + title + '</span>') : '' ) +
        '<input  id="' + id + '" class="' + this.cssPrefix + 'Text" type="text" value="' + value + '"></input></div>\n';
  },

  getTypeSelector: function(title, type, id) {
    var typeOptions = "";
    for(var j = 0; j < this.typesArray.length; j++) {
      typeOptions += (this.typesArray[j] == type) ? '<option selected>' : '<option>';
      typeOptions += this.typesArray[j] + '</option>';
    }
    return '<div class="' + this.cssPrefix + 'Type">' +
        (title != null ? ('<span class="' + this.cssPrefix + 'TextLabel">' + title + ':</span>' ) : '' ) +
        '<select  id="' + id + '" class="' + this.cssPrefix + 'Text">' + typeOptions + '</select></div>\n';
  },

  getAccessCheckbox: function(access, cssClass, id) {
    var checked = (access == 'private');
    return '<div class="' + cssClass + '"> <input id="' + id + '" type="checkbox" value="private" ' + (checked ? 'checked="checked"' : '' ) + ' /></div>';
  },
  //parameters field generation (end)

  removeParameter: function() {
    $("#" + this.id.replace("remove_button_", "parameters_")).remove();
  },

  addParameterValue: function(id) {
    var content = '<div id="parameterList" class="StringListParameterContainer">';
    var filters = _.sortBy(Panel.getPanel(ComponentsPanel.MAIN_PANEL).getParameters(), function (filter) {
      return filter.properties[0].value;
    });
    var isWidget = cdfdd.getDashboardWcdf().widget;

    if(filters.length == 0) {
      content += "<p>No Parameters!</p>";
    } else {
      content += '<p>Choose Parameter:</p><ul class="StringListParameter">';
      $.each(filters, function(i, filter) {
        var value = filter.properties[0].value;

        if(isWidget && $.inArray(value, cdfdd.getDashboardWcdf().widgetParameters) > -1) {
          content += '<li><div onClick="ValuesArrayRenderer.setParameterValue(\'' + id + '\',\'${p:' + value + '}\')">' + value + '</div></li>';
        } else {
          content += '<li><div onClick="ValuesArrayRenderer.setParameterValue(\'' + id + '\',\'' + value + '\')">' + value + '</div></li>';
        }
      });
      content += '</ul>';
    }

    cdfdd.impromptu = $.prompt(content + '</div>', {
      buttons: {
        Cancel: false
      },
      prefix: 'popup',
      focus: 1
    });
  },

  addAutoComplete: function(index) {
    var elemID = (this.multiDimensionArray ? '#val_' : '#arg_') + index;
    var placeHolder = '#' + this.cssPrefix;
    var myself = this;
    $(elemID).autocomplete({
      appendTo: placeHolder,
      source: function(req, add) {
        myself.autoCompleteRequest(req, add);
      },
      minLength: 0,
      delay: 300,
      select: function(event, ui) {
        $(elemID).find('input').val(ui.item.value);
      },
      focus: function(event, data) {
        if(data != undefined) {
          $(elemID).val(data.item.value);
        }
      },
      onsubmit: function(settings, original) {
        return myself.validate($('input', this).val());
      },
      height: 12
    });
  },

  getData: function() {
    return this.selectData || {};
  },

  autoCompleteRequest: function(req, add) {
    var results = $.map(this.getData(), function(v, k) {
      return k;
    });

    add($.grep(results, function(elt, i) {
      return elt.toLowerCase().indexOf(req.term.toLowerCase()) >= 0;
    }));
  },

  dragAndDrop: function(index) {
    var myself = this;
    $('div.popupcontainer .' + this.cssPrefix).sortable({
      axis: 'y',
      cursor: 'auto',
      placeholder: 'popupSortableHolder'
    });
  }
}, {
  setParameterValue: function(id, value) {
    $("#" + id.replace("parameter_button_", "val_")).val(value);
    cdfdd.impromptu.hide();
  }
});


var EditorValuesArrayRenderer = ValuesArrayRenderer.extend({

  arrayValue: null,

  constructor: function(tableManager) {
    this.base(tableManager);
    this.logger = new Logger("EditorValuesArrayRenderer");
    this.logger.debug("Creating new EditorValuesArrayRenderer");
  },

  render: function(placeholder, value, callback) {

    var _editArea = $("<td>" + ( value.length > 30 ? ( value.substring(0, 20) + " (...)" ) : value ) + "</td>");
    var myself = this;

    _editArea.click(function() {

      var arrayValue = value;
      var content = $(
          "<div id='" + myself.cssPrefix + "' class='" + myself.cssPrefix + "Container'>" +
          "  <div class='" + myself.cssPrefix + "'></div>\n" +
          "    <input class='" + myself.cssPrefix + "AddButton' type='button' value='Add'></input>");

      var vals = JSON.parse(value);
      cdfdd.arrayValue = vals;
      var index = vals.length;

      for(var i = 0; i < index; i++) {
        myself.addParameters(i, vals[i][0], vals[i][1], content);
      }

      var htmlContent = content.wrap("<div>").parent().html();

      $.prompt(htmlContent, {

        buttons: {
          Ok: true,
          Cancel: false
        },

        prefix: "popup",

        callback: function(v, m, f) {
          if(v) {
            // A bit of a hack to make null happen
            arrayValue = arrayValue.replace(/"null"/g, "null");
            callback(arrayValue);
            _editArea.text(arrayValue);
          }
          delete cdfdd.arrayValue;
        },

        loaded: function() { //button bindings
          $('.popup').css("width", "630px");
          $('.' + myself.cssPrefix + 'AddButton').click(function() {
            myself.addParameters(index, "", "", $("#" + myself.cssPrefix));

            $("#remove_button_" + index).click(myself.removeParameter);
            $("#parameter_button_" + index).click(function() {
              myself.editorInit(this, cdfdd.arrayValue);
            });

            index++;
          });

          $('.' + myself.cssPrefix + 'Remove').click(myself.removeParameter);
          $('.' + myself.cssPrefix + 'Parameter').click(function() {
            myself.editorInit(this, cdfdd.arrayValue);
          });
          $('.' + myself.cssPrefix + 'ValueDiv').tooltip();

        },

        submit: function(v, m, f) {
          var array = [];
          for(var i = 0; i < index; i++) {
            var paramVal = myself.getParameterValues(i);
            if(paramVal != null && paramVal.length > 0 && paramVal[0] != null) array.push(paramVal); //don't attempt to add deleted lines
          }
          arrayValue = array.length > 0 ? JSON.stringify(array) : "[]";
        }
      });
      //end of $.prompt
    });
    //end of _editArea.click
    _editArea.appendTo(placeholder);
  },

  editorCallback: function(value, index) {
    var divValue = value;
    if(divValue.length > 40) {
      divValue = divValue.substring(0, 30) + " (...)";
    }

    $("#val_" + index).text(divValue);

    if(value != "") {
      // CDF-271 jQueryUI tooltip bug #8861 XSS Vulnerability, no HTML allowed in an element's title
      $("#val_" + index).tooltip({content: "<pre>" + value + "</pre>"});
    }

    cdfdd.arrayValue[index] = [ $("#arg_" + index).val(), value ];

  },

  editorInit: function(placeholder, values) {
    var param_i = placeholder.id.replace("parameter_button_", "");
    var value = values[param_i] != undefined ? values[param_i][1] : "";

    var editor = new EditExtensionPointsRenderer(this.getTableManager());
    editor.render($(placeholder), value, this.editorCallback);
  },

  addParameters: function(i, arg, val, container) {

    val = this.escapeOutputValue(val); //for output only, will come back ok

    var parameterButton = this.getParameterButton(i);
    var removeButton = this.getRemoveButton(i);

    var argInput = this.getTextInput(this.argTitle, arg, this.cssPrefix + 'Args', 'arg_' + i);
    var valDiv = this.getValueDiv(this.valTitle, val, this.cssPrefix + 'Val', 'val_' + i);
    var row = "<div id='parameters_" + i + "' class='" + this.cssPrefix + "ParameterHolder'>\n" + argInput +
        "<div class='" + this.cssPrefix + "Values'>" + valDiv + parameterButton + removeButton + "</div><br />" +
        "</div>\n";

    var subContainer = container.find('.' + this.cssPrefix);
    subContainer.append(row);
    subContainer.find('#parameters_' + i + ' input:eq(0)').focus();
  },

  escapeOutputValue: function(val) {
    if(val) {
      return val.replace(/["]/g, '&quot;').replace(/[']/g, '&#39;');

    } else {
      return val;
    }
  },

  getParameterValues: function(i) {
    var arg = $('#arg_' + i).val();
    var value = cdfdd.arrayValue[i] != undefined ? cdfdd.arrayValue[i][1] : "";

    return [arg, value];
  },

  getFormattedValue: function(_value) {

    if(_value.length > 40) {
      _value = _value.substring(0, 30) + " (...)";
    }
    return _value;
  },

  getValueDiv: function(title, value, cssClass, id) {
    var tooltip = value != "" ? "<pre>" + value + "</pre>" : "";
    return "<div class='" + cssClass + "'>" +
        (title != null ? "<span class='" + this.cssPrefix + "TextLabel'>" + title + "</span>" : "") +
        "<div id='" + id + "' class='" + this.cssPrefix + "ValueDiv' title='" + tooltip + "'>" + this.getFormattedValue(value) +
        "</div></div>\n";
  }

});

/**
 * Single value renderer
 */
var ArrayRenderer = ValuesArrayRenderer.extend({

  multiDimensionArray: false,

  cssPrefix: "StringArray",

  constructor: function(tableManager) {
    this.base(tableManager);
    this.logger = new Logger("ArrayRenderer");
    this.logger.debug("Creating new ArrayRenderer");
  },

  getTextInput: function(value, i) {
    return '<input  id="arg_' + i + '" class="' + this.cssPrefix + 'Text" type="text" value="' + value + '"></input>';
  },

  addParameters: function(i, arg, val, container) {

    if(val) {
      val = val.replace(/["]/g, '&quot;');
    }//for output only, will come back ok

    var myself = this;
    var removeButton = this.getRemoveButton(i);
    var argInput = this.getTextInput(arg, i);

    var row = '<div id="parameters_' + i + '" class="' + this.cssPrefix + 'ParameterHolder">' +
        removeButton + argInput + '<div class="' + this.cssPrefix + 'DragIcon"><span/></div></div>\n';

    var subContainer = container.find('.' + this.cssPrefix);
    subContainer.append(row);
  },

  addFocusEvent: function(index) {
    var parameterInput = $('.' + this.cssPrefix + 'Container #arg_' + index);
    var myself = this;
    parameterInput
        .focus(function(e) {
          $(this).parent().addClass(myself.cssPrefix + 'Focus');
        })
        .blur(function(e) {
          $(this).parent().removeClass(myself.cssPrefix + 'Focus');
        });

    parameterInput.focus();
  }
});

var ColTypesArrayRender = ArrayRenderer.extend({
  getData: function() {
    var data = this.selectData || {};
    _.extend(data, {
      string: 'string',
      numeric: 'numeric',
      hidden: 'hidden'
    });
    return data;
  }
});

var IndexArrayRenderer = ArrayRenderer.extend({
  argTitle: 'Index'
});

//arg, value, no param button, //TODO: own css
var ListArgValNoParamRenderer = ValuesArrayRenderer.extend({
  //disable parameter button
  getParameterButton: function(i) {
    return '';
  }
});

var SortByArrayRenderer = ListArgValNoParamRenderer.extend({
  argTitle: 'Index',
  valTitle: 'Order'
});

//used by ExtraOptions
var OptionArrayRenderer = ListArgValNoParamRenderer.extend({
  argTitle: 'Option'
});

var CacheKeysValuesRenderer = ListArgValNoParamRenderer.extend({
  argTitle: 'Key',
  valTitle: 'Value'
});

var CdaParametersRenderer = ValuesArrayRenderer.extend({
  cssPrefix: "ParameterList",
  argTitle: 'Name',
  valTitle: 'Value',
  hasTypedValues: true,
  //TODO: this should be fetched from somewhere
  typesArray: ['String', 'Integer', 'Numeric', 'Date', 'StringArray', 'IntegerArray', 'NumericArray', 'DateArray'],

  /**
   * @returns {Array}
   **/
  getParameterValues: function(i) {
    var name = $("#arg_" + i).val();
    var value = $("#val_" + i).val();
    var type = $("#type_" + i).val();
    var access = $("#access_" + i).attr('checked') ? 'private' : '';
    return [name, value, type, access];
  },

  addParameter: function(i, values, container) {

    var arg = values[0];
    var val = values[1];
    var type = values[2];
    var access = values[3];

    if(val === undefined) {
      val = "";
    } else if(val === null) {
      val = "null";
    } else if(val) {
      val = val.replace(/["]/g, '&quot;');
    }//for output only, will come back ok

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

    if(i == 0) { //add table and header
      container.find('.' + this.cssPrefix).append('<table> </table>');
      var hdr = '<tr>';
      hdr += '<th><span class="' + this.cssPrefix + 'TextLabel">' + this.argTitle + '</span></th>';
      hdr += '<th><span class="' + this.cssPrefix + 'TextLabel">' + this.valTitle + '</span></th>';
      hdr += '<th><span class="' + this.cssPrefix + 'TextLabel">Type</span></th>';
      hdr += '<th><span class="' + this.cssPrefix + 'TextLabel">Private?</span></th>';
      hdr += '<th><span class="' + this.cssPrefix + 'TextLabel"></span></th>';
      hdr += '</tr>';
      row = hdr + row;
    }

    var subContainer = container.find('.' + this.cssPrefix + ' table');
    subContainer.append(row);
    subContainer.find('#parameters_' + i + ' input:eq(0)').focus();
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

var CdaQueryRenderer = PromptRenderer.extend({

  constructor: function(tableManager) {
    this.base(tableManager);
    this.logger = new Logger("CdaQueryRenderer");
    this.logger.debug("Creating new CdaQueryRenderer");
    this.wizard = "CDA_WIZARD";
  }
});

var MondrianCatalogRenderer = SelectRenderer.extend({

  logger: null,
  selectData: {},
  catalogs: [],

  getDataInit: function() {

    var myself = this;
    $.getJSON(OlapUtils.getOlapCubesUrl(), {}, function(json) {
      if(json.status == "true") {
        var catalogs = json.result.catalogs;
        myself.catalogs = catalogs;
        $.each(catalogs, function(i, catalog) {
          myself.selectData[catalog.schema.replace("solution:", "")] = catalog.name;
        });
      }
    });
  },

  postChange: function(value) {
    // Searching for value
    var jndi, cube, seen = false;
    $.each(this.catalogs, function(i, c) {
      if(c.schema == "solution:" + value) {
        seen = true;
        cube = c.cubes[0].name;
        jndi = c.jndi;
        return false;
      }
    });

    if(seen) {
      Dashboards.log("Found: " + jndi);
      // Update other fields
      var jndiRow = this.getTableManager().getTableModel().getRowByName("jndi");
      if(jndiRow != undefined) {
        jndiRow.value = jndi;
        this.getTableManager().renderColumnByRow(jndiRow);
      }

      var queryRow = this.getTableManager().getTableModel().getRowByName("query");
      if(queryRow != undefined && queryRow.value == "") {
        queryRow.value = "select {} on ROWS, {} on COLUMNS from [" + cube + "]";
        this.getTableManager().renderColumnByRow(queryRow);
      }

    }
  }
});

var JndiRenderer = SelectRenderer.extend({

  logger: null,
  selectData: [],
  catalogs: [],

  getDataInit: function() {

    var myself = this;
    $.getJSON(OlapUtils.getOlapCubesUrl(), {}, function(json) {
      if(json.status == "true") {
        var catalogs = json.result.catalogs;
        var hash = {};
        $.each(catalogs, function(i, catalog) {
          hash[catalog.jndi] = 1;
        });
        for(jndi in hash) {
          myself.selectData.push(jndi);
        }
      }
    });
  }
});

var ParameterMappingRenderer = ValuesArrayRenderer.extend({
  argTitle: '',
  valTitle: '',
  onPopupLoad: function() {
    var myself = this;
    var getParametersUrl = wd.helpers.editor.getDashboardParametersUrl();
    var tableData = this.tableManager.getTableModel().data;
    for (var i = 0; i < tableData.length; i++) {
      if (tableData[i].name == "dashboardPath") {
        this.dashboardPath = tableData[i].value;
        break;
      }
    }
    $.ajax({
      async: true,
      method: "get",
      dataType: "json",
      url: getParametersUrl + this.dashboardPath,
      success: function(data) {
        myself.otherDashboardParameters = data ? data.parameters || [] : [];
      },
      error: function(){
        myself.otherDashboardParameters = [];
      }
    });
  }, 

  constructor: function(tableManager) {
    this.base(tableManager);
    this.logger = new Logger("ParameterMappingRenderer");
    this.logger.debug("Creating new ParameterMappingRenderer");
  },

  addParameterValue: function(id) {
    var leftContent = '<div id="parameterListLeft" class="StringListParameterContainer" style="float:left;">';
    var rightContent = '<div id="parameterListRight" class="StringListParameterContainer" style="float:left;">';

    var filters = _.sortBy(Panel.getPanel(ComponentsPanel.MAIN_PANEL).getParameters(), function (filter) {
      return filter.properties[0].value;
    });

    rightContent += '<p>Map to other dashboard:</p><ul class="StringListParameter">';
    if (this.otherDashboardParameters.length == 0) {
      rightContent += '<li><div><p>No Parameters!<p></div></li>';
    } else {
      for (var i = 0; i < this.otherDashboardParameters.length; i++) {
        rightContent += '<li><div onClick="ParameterMappingRenderer.preRegisterValue(this, false, \'' + this.otherDashboardParameters[i] + '\')">' + this.otherDashboardParameters[i] + '</div></li>';
      }
    }
    rightContent += '</ul></div>';

    leftContent += '<p>Map from this dashboard:</p><ul class="StringListParameter">';
    if(filters.length == 0) {
      leftContent += '<li><div><p>No Parameters!<p></div></li>';
    } else {
      $.each(filters, function(i, filter) {
        var value = filter.properties[0].value;
          leftContent += '<li><div onClick="ParameterMappingRenderer.preRegisterValue( this, true, \'' + value + '\')">' + value + '</div></li>';
        });
    }
    leftContent += '</ul></div>';

    var content = leftContent + rightContent;

    cdfdd.impromptu = $.prompt(content, {
      buttons: {
        Ok: true,
        Cancel: false
      },
      prefix: 'popup',
      focus: 1,
      submit: _.bind(function(e,v,m,f){
        if (e) {
          $("#" + id.replace("parameter_button_", "arg_")).val(ParameterMappingRenderer.thisParam || "");
          $("#" + id.replace("parameter_button_", "val_")).val(ParameterMappingRenderer.otherParam || "");
        }
        ParameterMappingRenderer.thisParam = undefined;
        ParameterMappingRenderer.otherParam = undefined;
      }, this)
    });
    
    
  },

  getParameterRow: function(i, argInput, valInput, parameterButton, removeButton) {
    var parameterRow = '<div id="parameters_' + i + '" >\n' + argInput +
              (this.multiDimensionArray ?
                ('<div class="' + this.cssPrefix + 'Values">' + valInput + parameterButton + removeButton + '</div><br />') :
                removeButton) + '</div>\n';

    if ( ($("#StringList").length > 0 && $("#paramMappHeader").length === 0) || i === 0 ) {
      return '<div id="paramMappHeader">' +
              '<div class="StringListArgs">Map from this dashboard</div>' +
              '<div class="StringListValues">Map to other dashboard</div>' +
            '</div>' + parameterRow;
    }
    return parameterRow;
  },

  removeParameter: function() {
    var paramMappTitle = $("#paramMappHeader");
    $("#" + this.id.replace("remove_button_", "parameters_")).remove();
    if(paramMappTitle && paramMappTitle.siblings().length === 0) {
      paramMappTitle.remove();
    }
  }
},{
    preRegisterValue: function(elem, left, val) {
      if (left) {
        ParameterMappingRenderer.thisParam = val;
      } else {
        ParameterMappingRenderer.otherParam = val;
      }
      $(elem)
        .parentsUntil(".StringListParameter")
          .parent()
            .find("li div")
              .removeClass('selected');
      $(elem).addClass('selected');
    }
});
