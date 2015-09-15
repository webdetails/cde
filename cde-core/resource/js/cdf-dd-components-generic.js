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
  hasTypedValues: false,//if true, args also have a type
  autocomplete: true, //disable autocomplete

  cssPrefix: "StringList",

  typesArray: [],//only used if hasTypedValues
  selectData: {},//to use with autocomplete

  //used for value input labels
  argTitle: 'Arg',
  valTitle: 'Value',

  popupTitle: 'Parameters',

  argPlaceholderText: 'Insert Text...',
  valPlaceHolderText: 'Parameters...',

  index: 0,

  constructor: function(tableManager) {
    this.base(tableManager);
    this.logger = new Logger("ValuesArrayRenderer");
    this.logger.debug("Creating new ValuesArrayRenderer");
  },

  render: function(placeholder, value, callback) {

    var _editArea = $("<td>" + (value.length > 30 ? (value.substring(0, 20) + " (...)") : value) + "</td>");
    var myself = this;

    _editArea.click(function() {
      var content = myself.getPopupContent();
      var vals = myself.getInitialValue(value);
      var index = myself.index = vals.length;

      cdfdd.arrayValue = vals;

      for(var i = 0; i < index; i++) {
        myself.addPopupRow(i, vals[i], $('.popup-list-body', content));
      }

      var htmlContent = $('<div>')
          .append(content)
          .html();

      $.prompt(htmlContent, {

        buttons: {
          Ok: true,
          Cancel: false
        },

        prefix: "popup",

        callback: function(v, m, f) {
          if(v) {
            var result = cdfdd.arrayValue;
            // A bit of a hack to make null happen
            result = result.replace(/"null"/g, "null");
            callback(result);
            _editArea.text(result);
          }
          delete cdfdd.arrayValue;

        },

        loaded: function() {
          myself.popupLoadedCallback($(this));
        },

        submit: function(v, m, f) {
          if(v) {
            myself.popupSubmitCallback();
          }
        }
      });
    });

    _editArea.appendTo(placeholder);
  },

  getPopupContent: function() {
    var cssPrefix = this.cssPrefix;

    var rv = "";
    var re = new RegExp("MSIE ([0-9]{1,}[\.0-9]{0,})");
    if(re.exec(navigator.userAgent) != null) {
      rv = parseFloat(RegExp.$1) < 10 ? "ie8" : "";
    }

    var popupHeader = '' +
        '<div class="popup-header-container">\n' +
        '  <div class="popup-title-container">' + this.popupTitle + '</div>\n' +
        '</div>\n';

    var addRowButton = '' +
        '<div class="popup-list-buttons add-button-container">\n' +
        '  <button class="popup-add-row-button">Add</button>\n' +
        '</div>\n';

    var removeRowButtons = '' +
        '<div class="popup-list-buttons remove-selection-button-container">\n' +
        '  <button class="popup-remove-selection">Remove</button>\n' +
        '  <button class="popup-cancel-selection">Cancel</button>\n' +
        '</div>';

    var rowListHeaders = this.getRowListHeaders();

    var popupBody = '' +
        '<div class="popup-body-container popup-list-body-container popup-add-mode ' + rv + '">\n' +
        '  <div class="popup-body-header clearfix">' + addRowButton + removeRowButtons + rowListHeaders + '  </div>' +
        '  <div class="popup-list-body"></div>\n' +
        '</div>';

    return $(popupHeader + popupBody);
  },

  getRowListHeaders: function() {
    var html = '';
    var cssClass = 'popup-list-row-label';

    if(this.multiDimensionArray) {
      html += '<div class="popup-label popup-arg-label">' + this.argTitle + '</div>' +
      '<div class="popup-label popup-value-label">' + this.valTitle + '</div>';

      if(this.hasTypedValues) {
        html += '<div class="popup-label">Type</div>';
        cssClass += ' typed-label';
      }
    }

    return html === "" ? html : '<div class="' + cssClass + '">' + html + '</div>';
  },

  popupLoadedCallback: function(popupObj) {
    var myself = this;
    CDFDDUtils.movePopupButtons(popupObj);


    popupObj.addClass(myself.cssPrefix + 'Popup array-list-popup');

    myself.onPopupLoad();

    /* Bind Events */
    myself.addRowButtonEvent();
    myself.removeSelectedRowsEvent();
    myself.cancelRowSelectionEvent();
    myself.dragAndDropHoverEvent('.popup-drag-icon');
    myself.selectRowToRemoveEvent('.popup-remove-row-select');
    myself.valueClickEvent('.popup-text-div');

    for(var i = 0; i < myself.index; i++) {
      myself.addAutoComplete(i);
    }

    myself.dragAndDrop();
    myself.handleOverflow();

    //Focus on the first available input field
    $('.popup-text-input:first').focus();
  },

  popupSubmitCallback: function() {
    var array = [];
    var myself = this;
    $('.popup-list-row').each(function() {
      var index = $(this).attr('id').replace('parameters_', '');
      var paramVal = myself.getRowValues(index);
      if(!myself.isParameterEmpty(paramVal)) {
        array.push(paramVal); //don't attempt to add deleted lines
      }
    });

    cdfdd.arrayValue = array.length > 0 ? JSON.stringify(array) : "[]";
  },

  /* Popup Events */
  addRowButtonEvent: function() {
    var myself = this;
    $('.popup-add-row-button').click(function() {
      var container = $('.popup-list-body');
      var index = myself.index++;

      if(myself.multiDimensionArray) {
        myself.addPopupRow(index, ["", "", "", ""], container);
      } else {
        myself.addPopupRow(index, "", container);
      }

      //events
      myself.handleOverflow();
      myself.selectRowToRemoveEvent("#remove_button_" + index);
      myself.dragAndDropHoverEvent("#drag_icon_" + index);
      myself.valueClickEvent('#val_' + index);
      myself.addAutoComplete(index);
    });
  },

  removeSelectedRowsEvent: function() {
    var myself= this;

    $('.popup-remove-selection').click(function() {
      var mainContainer = $('.popup-list-body-container');

      $('.popup-remove-selected').remove();
      mainContainer.addClass('popup-add-mode');
      mainContainer.removeClass('popup-remove-mode');

      myself.handleOverflow();
    });
  },

  cancelRowSelectionEvent: function() {
    $('.popup-cancel-selection').click(function() {
      var removeSelClass = 'popup-remove-selected';
      var mainContainer = $('.popup-list-body-container');

      var rows = $('.' + removeSelClass);

      rows.removeClass(removeSelClass);
      rows.find('.popup-text-input').prop('disabled', false);

      mainContainer.addClass('popup-add-mode');
      mainContainer.removeClass('popup-remove-mode');

    });
  },

  selectRowToRemoveEvent: function(selector) {
    var myself = this;
    $(selector).click(function() {
      var placeholder = $(this).parents('.popup-list-row');
      var removeSelClass = 'popup-remove-selected';
      var input = placeholder.find('.popup-text-input');
      var state = input.prop('disabled');

      placeholder.toggleClass(removeSelClass);
      input.prop('disabled', !state);

      var mainContainer = $('.popup-list-body-container');
      var isRemoving = !!mainContainer.has('.' + removeSelClass).length;

      mainContainer.toggleClass('popup-add-mode', !isRemoving);
      mainContainer.toggleClass('popup-remove-mode', isRemoving);

    }).hover(function() {
      $(this).parents('.popup-list-row').addClass('popup-remove-hover');

    }, function() {
      $(this).parents('.popup-list-row').removeClass('popup-remove-hover');

    });
  },

  valueClickEvent: function(selector) {
    //default does nothing
  },

  dragAndDropHoverEvent: function(selector) {
    $(selector).hover(function() {
      var container = $(this).parents('.popup-list-row');

      container.find('input').blur();
      container.addClass('popup-drag-hover');

    }, function() {
      var container = $(this).parents('.popup-list-row');
      container.removeClass('popup-drag-hover');

    });
  },

  onPopupLoad: function() {
    //custom renderers may want to do something on popup load, this is here just as a hook
  },

  postAddRow: function() {
    //default does nothing
  },

  validate: function(settings, original) {
    return true;
  },

  getInitialValue: function(value) {
    var initialValue = JSON.parse(value);

    if(!initialValue.length) {
      initialValue = this.multiDimensionArray ? [["", "", "", ""]] : [""];
    }

    return initialValue;
  },

  isParameterEmpty: function(param) {
    if(param == null || _.isEmpty(param)) {
      return true;
    }

    if(this.multiDimensionArray) {
      return  _.isEmpty(param[0]) && _.isEmpty(param[1]);
    }

    return false;
  },

  /**
   * @returns {Array} Values to be stored for each parameter_<i>
   **/
  getRowValues: function(i) {

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

  handleOverflow: function() {
    var container = $('.popup-list-body');

    if(!container.length) {
      return;
    }

    var element = container.get(0);
    if(element.offsetHeight < element.scrollHeight) {
      container.addClass('overflow-container');
    } else {
      container.removeClass('overflow-container');
    }
  },

  /***************
   * AutoComplete Functions
   *****/

  addAutoComplete: function(index) {
    if(!this.autocomplete) {
      return undefined;
    }

    var elemID = (this.multiDimensionArray ? '#val_' : '#arg_') + index;

    var myself = this;
    $(elemID).autocomplete({
      appendTo: '.popup-list-body-container',
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

  autoCompleteRequest: function(req, add) {
    var results = $.map(this.getData(), function(v, k) {
      return k;
    });

    add($.grep(results, function(elt, i) {
      return elt.toLowerCase().indexOf(req.term.toLowerCase()) >= 0;
    }));
  },

  getData: function() {
    var data = {};

    var filters = _.sortBy(Panel.getPanel(ComponentsPanel.MAIN_PANEL).getParameters(), function (filter) {
      return filter.properties[0].value;
    });

    var isWidget = cdfdd.getDashboardWcdf().widget;
    if(filters.length > 0) {
      $.each(filters, function(i, filter) {
        var value = filter.properties[0].value;
        if(isWidget && $.inArray(value, cdfdd.getDashboardWcdf().widgetParameters) > -1) {
          data[value] = '${p:' + value + '}';
        } else {
          data[value] = value;
        }
      });
    } else {
      data[''] = '';
    }

    return data;
  },

  /***************
   * Drag&Drop Functions
   *****/

  dragAndDrop: function() {
    $('div.popup .popup-list-body').sortable({
      axis: 'y',
      cursor: 'auto',
      placeholder: 'popup-sortable-holder',
      delay: 100,

      sort: function(event, ui) {
        var a = "";
      }
    });
  },


  /***************
   * UI Functions
   *****/

  addPopupRow: function(index, values, container) {

    var arg, value, type, access, row;
    if(this.multiDimensionArray) {
      arg = values[0];
      value = values[1] === undefined ? "" : values[1] === null ? "null" : values[1];
      values[1] = value;
      if(this.hasTypedValues) {
        type = values[2];
        access = values[3];

        row = this.buildTypedRow(index, arg, value, type, access);
      } else {
        row = this.buildMultiDimensionRow.apply(this, [index].concat(values));
      }
    } else {
      arg = values;
      row = this.buildSingleDimensionRow(index, arg);
    }

    container.append(row);
    container.find('#parameters_' + index + ' input:eq(0)').focus();

    this.postAddRow(container, index);
  },

  buildSingleDimensionRow: function(index, arg) {
    var argSection = this.getArgSection(index, arg);

    return this.wrapPopupRow(index, 'single-dimension-row', argSection);
  },

  buildMultiDimensionRow: function(index, arg, val) {
    var argSection = this.getArgSection(index, arg);
    var valSection = this.getValueSection(index, val);

    return this.wrapPopupRow(index, 'multi-dimension-row', argSection + valSection);
  },

  buildTypedRow: function(index, arg, val, type, access) {
    var argSection = this.getArgSection(index, arg);
    var valSection = this.getValueSection(index, val);

    var typeSection = this.getTypeSelector(index, type);
    var accessSection = this.getAccessCheckbox(index, access);

    return this.wrapPopupRow(index, 'typed-row', argSection + valSection + typeSection + accessSection);
  },

  wrapPopupRow: function(index, cssClass, html) {
    var removeButton = this.getRemoveRowButton(index);
    var dragIcon = this.getDragIcon(index);

    return '' +
        '<div id="parameters_' + index + '" class="popup-list-row ' + cssClass + '">' +
        removeButton + html + dragIcon + '</div>';
  },

  getRemoveRowButton: function(index) {
    return '<span type="button" id="remove_button_' + index + '" class="popup-remove-row-select"></span>';
  },

  getArgSection: function(index, value) {
    return '' +
        '<div class="popup-arg-container">' +
        '  <input id="arg_' + index + '" class="popup-text-input" type="text" value="' + value + '" placeholder="' + this.argPlaceholderText + '"></input>' +
        '</div>';
  },

  getValueSection: function(index, value) {
    return '' +
        '<div class="popup-value-container">' +
        '  <input id="val_' + index + '" class="popup-text-input" type="text" value="' + value + '" placeholder="' + this.valPlaceHolderText + '"></input>' +
        '</div>';
  },

  getDragIcon: function(index) {
    return '<div id="drag_icon_' + index + '" class="popup-drag-icon">' +
           '  <span class="drag-icon-holder"></span><div class="drag-icon-overlay"></div>' +
           '</div>';
  },

  getTypeSelector: function(index, type) {
    var typeOptions = "";
    var id = "type_" + index;

    for(var j = 0; j < this.typesArray.length; j++) {
      typeOptions += (this.typesArray[j] == type) ? '<option selected>' : '<option>';
      typeOptions += this.typesArray[j] + '</option>\n';
    }
    return '' +
        '<div class="popup-input-container popup-type-container">\n' +
        '  <select id="' + id + '" class="popup-select">' + typeOptions + '</select>' +
        '</div>\n';
  },

  getAccessCheckbox: function(index, access) {
    var checked = access === 'private' ? 'checked' : '';
    var id = "access_" + index;

    return '<div class="popup-checkbox-container">' +
    '  <input type="checkbox" id="' + id + '" name="' + id + '" ' + checked + '>' +
    '  <label class="popup-input-label" for="' + id + '">Private</label>' +
    '</div>';
  }
  //parameters field generation (end)

}, {
  setParameterValue: function(id, value) {
    $("#" + id.replace("parameter_button_", "val_")).val(value);
    cdfdd.impromptu.hide();
  }
});

var EditorValuesArrayRenderer = ValuesArrayRenderer.extend({

  autocomplete: false,
  popupTitle: 'Extension Points',

  valPlaceHolderText: 'Click to edit...',

  constructor: function(tableManager) {
    this.base(tableManager);
    this.logger = new Logger("EditorValuesArrayRenderer");
    this.logger.debug("Creating new EditorValuesArrayRenderer");
  },

  onPopupLoad: function() {
    $('.popup-text-div').tooltip();
  },

  valueClickEvent: function(selector) {
    var myself = this;
    $(selector).click(function() {
      var values = cdfdd.arrayValue;
      var param_i = this.id.replace("val_", "");
      var value = values[param_i] != undefined ? values[param_i][1] : "";

      var editor = new EditExtensionPointsRenderer(myself.getTableManager());
      editor.render($(this), value, myself.editorCallback);
    })
  },

  getValueSection: function(index, value) {
    var tooltip = value != "" ? $("<a>").text("<pre>" + value + "</pre>").html() : "Click to Edit...";
    return '' +
        '<div class="popup-value-container">' +
        '  <div id="val_' + index + '" class="popup-text-div" title="' + tooltip + '" placeholder="' + this.valPlaceHolderText + '">' + this.getFormattedValue(value) + '</div>' +
        '</div>';
  },

  editorCallback: function(value, index) {
    var divValue = value;
    if(divValue.length > 35) {
      divValue = divValue.substring(0, 25) + " (...)";
    }

    var $val = $("#val_" + index);
    $val.text(divValue);

    if(value != "") {
      // CDF-271 jQueryUI tooltip bug #8861 XSS Vulnerability, no HTML allowed in an element's title
      $val.tooltip({content: "<pre>" + value + "</pre>"});
    }

    cdfdd.arrayValue[index] = [ $("#arg_" + index).val(), value ];

  },

  escapeOutputValue: function(val) {
    if(val) {
      return val.replace(/["]/g, '&quot;').replace(/[']/g, '&#39;');

    } else {
      return val;
    }
  },

  getRowValues: function(i) {
    var arg = $('#arg_' + i).val();
    var value;

    if(arg != null) {
      value = cdfdd.arrayValue[i] != undefined ? cdfdd.arrayValue[i][1] : "";
    }

    return [arg, value];
  },

  getFormattedValue: function(_value) {
    if(_value.length > 40) {
      _value = _value.substring(0, 30) + " (...)";
    }
    return _value;
  }

});

/**
 * Single value renderer
 */
var ArrayRenderer = ValuesArrayRenderer.extend({

  popupTitle: 'String Array',
  multiDimensionArray: false,
  cssPrefix: "StringArray",

  constructor: function(tableManager) {
    this.base(tableManager);
    this.logger = new Logger("ArrayRenderer");
    this.logger.debug("Creating new ArrayRenderer");
  }

});

var ColHeadersArrayRenderer = ArrayRenderer.extend({
  popupTitle: 'Column Headers'
});

var ColFormatsArrayRenderer = ArrayRenderer.extend({
  popupTitle: 'Column Formats'
});

var ColWidthsArrayRenderer = ArrayRenderer.extend({
  popupTitle: 'Column Widths'
});

var ColSortableArrayRenderer = ArrayRenderer.extend({
  popupTitle: 'Sortable Columns'
});

var ColTypesArrayRender = ArrayRenderer.extend({
  popupTitle: 'Column Types',

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
  autocomplete: false,
  popupTitle: 'Output Options'
});

//arg, value, no param button, //TODO: own css
var ListArgValNoParamRenderer = ValuesArrayRenderer.extend({
  autocomplete: false
});

var SortByArrayRenderer = ListArgValNoParamRenderer.extend({
  popupTitle: 'Sort By',
  argTitle: 'Index',
  valTitle: 'Order'
});

//used by ExtraOptions
var OptionArrayRenderer = ListArgValNoParamRenderer.extend({
  popupTitle: 'Options',
  argTitle: 'Option'
});

var CacheKeysValuesRenderer = ListArgValNoParamRenderer.extend({
  popupTitle: 'Cache Keys',
  argTitle: 'Key',
  valTitle: 'Value',
  onPopupLoad: function(){
    var lh = $("#popupbox .popup-list-row-label");
    lh.find(".popup-label.popup-value-label").addClass('popup-value-label-small');
    lh.find(".popup-label.popup-arg-label").addClass('popup-arg-label-small');
    lh.append('<div class="popup-label popup-default-label">Default</div>');
  },
  getRowValues: function(i) {
      var result = [];
      result.push($('#arg_' + i).val());//key
      result.push($('#val_' + i).val());//value
      result.push($('#def_' + i).val());//default
      return result;
  },
  getArgSection: function(index, value) {
    return '' +
        '<div class="popup-arg-container-small">' +
        '  <input id="arg_' + index + '" class="popup-text-input" type="text" value="' + value + '" placeholder="Cache Key..."></input>' +
        '</div>';
  },
  getValueSection: function(index, value) {
    return '' +
        '<div class="popup-value-container-small">' +
        '  <input id="val_' + index + '" class="popup-text-input" type="text" value="' + value + '" placeholder="Value..."></input>' +
        '</div>';
  },
  getDefaultSection: function(index, value) {
    return '' +
        '<div class="popup-default-container">' +
        '  <input id="def_' + index + '" class="popup-text-input" type="text" value="' + value + '" placeholder="Default..."></input>' +
        '</div>';
  },
  buildMultiDimensionRow: function(index, arg, val, def) {
    var argSection = this.getArgSection(index, arg);
    var valSection = this.getValueSection(index, val);
    var defaultSection = this.getDefaultSection(index, def);

    return this.wrapPopupRow(index, 'multi-dimension-row', argSection + valSection + defaultSection);
  }
});

var CdaParametersRenderer = ValuesArrayRenderer.extend({
  cssPrefix: "ParameterList",

  argTitle: 'Name',
  valTitle: 'Value',

  valPlaceHolderText: 'Insert Text...',

  hasTypedValues: true,
  //TODO: this should be fetched from somewhere
  typesArray: ['String', 'Integer', 'Numeric', 'Date', 'StringArray', 'IntegerArray', 'NumericArray', 'DateArray'],

  /**
   * @returns {Array}
   **/
  getRowValues: function(i) {
    var name = $("#arg_" + i).val();
    var value = $("#val_" + i).val();
    var type = $("#type_" + i).val();
    var access = $("#access_" + i).attr('checked') ? 'private' : '';
    return [name, value, type, access];
  },

  postAddRow: function(container, index) {
    var selector = container.find("#type_" + index);
    CDFDDUtils.buildPopupSelect(selector, {});
  }
});

var CdaColumnsArrayRenderer = ValuesArrayRenderer.extend({
  autocomplete: false,
  popupTitle: 'Columns',
  argTitle: 'Index',
  valTitle: 'Name'
});

var CdaCalculatedColumnsArrayRenderer = ValuesArrayRenderer.extend({
  autocomplete: false,
  popupTitle: 'Calculated Columns',
  argTitle: 'Name',
  valTitle: 'Form.'
});

var abstractMapperRenderer = ValuesArrayRenderer.extend({

  constructor: function(tableManager) {
    this.base(tableManager);
    this.logger = new Logger(this.mapperType);
    this.logger.debug("Creating new " + this.mapperType);
  },

  onPopupLoad: function() {
    var requestUrl = this.getRequestUrl();
    var tableData = this.tableManager.getTableModel().data;
    for (var i = 0; i < tableData.length; i++) {
      if (tableData[i].name == "dashboardPath") {
        this.dashboardPath = tableData[i].value;
        break;
      }
    }
    var successFun = _.bind(this.requestSuccess, this);
    var errorFun = _.bind(this.requestError, this);

    $.ajax({
      async: true,
      method: "get",
      dataType: "json",
      url: requestUrl + this.dashboardPath,
      success: successFun,
      error: errorFun
    });
  },

  addAutoComplete: function(index) {
    if(!this.autocomplete) {
      return undefined;
    }

    var myself = this;
    $('#arg_' + index).autocomplete({
      appendTo: '.popup-list-body-container',

      source: function(req, add) {
        myself.autoCompleteRequest(req, add);
      },
      minLength: 0,
      delay: 300,

      select: function(event, ui) {
        $('#arg_' + index).find('input').val(ui.item.value);
      },

      focus: function(event, data) {
        if(data != undefined) {
          $('#arg_' + index).val(data.item.value);
        }
      },
      onsubmit: function(settings, original) {
        return myself.validate($('input', this).val());
      },
      height: 12
    });

    $('#val_' + index).autocomplete({
      appendTo: '.popup-list-body-container',

      source: function(req, add) {
        myself.otherAutoCompleteRequest(req, add);
      },
      minLength: 0,
      delay: 300,

      select: function(event, ui) {
        $('#val_' + index).find('input').val(ui.item.value);
      },

      focus: function(event, data) {
        if(data != undefined) {
          $('#val_' + index).val(data.item.value);
        }
      },
      onsubmit: function(settings, original) {
        return myself.validate($('input', this).val());
      },
      height: 12
    });
  },

  autoCompleteRequest: function(req, add) {
    var results = $.map(this.getData(), function(v, k) {
      return k;
    });

    add($.grep(results, function(elt, i) {
      return elt.toLowerCase().indexOf(req.term.toLowerCase()) >= 0;
    }));
  },

  otherAutoCompleteRequest: function(req, add) {
    var results = $.map(this.getOtherData(), function(v, k) {
      return k;
    });

    add($.grep(results, function(elt, i) {
      return elt.toLowerCase().indexOf(req.term.toLowerCase()) >= 0;
    }));
  },

  getOtherData: function() {
    var data = {};

    var filters = this[this.dataSaveProp];

    var isWidget = cdfdd.getDashboardWcdf().widget;
    if(filters.length > 0) {
      $.each(filters, function(i, filter) {
        var value = filter;
        if(isWidget && $.inArray(value, cdfdd.getDashboardWcdf().widgetParameters) > -1) {
          data[value] = '${p:' + value + '}';
        } else {
          data[value] = value;
        }
      });
    } else {
      data[''] = '';
    }

    return data;
  },
  getData: function() {
    var data = {};
    var filterCategory = this.datasourceFiltering ?
      Panel.getPanel( DatasourcesPanel.MAIN_PANEL).getDatasources() :
      Panel.getPanel(ComponentsPanel.MAIN_PANEL).getParameters();

    var filters = _.sortBy(filterCategory, function (filter) {
      return filter.properties[0].value;
    });

    var isWidget = cdfdd.getDashboardWcdf().widget;
    if(filters.length > 0) {
      $.each(filters, function(i, filter) {
        var value = filter.properties[0].value;
        if(isWidget && $.inArray(value, cdfdd.getDashboardWcdf().widgetParameters) > -1) {
          data[value] = '${p:' + value + '}';
        } else {
          data[value] = value;
        }
      });
    } else {
      data[''] = '';
    }
    return data;
  }

});

var ParameterMappingRenderer = abstractMapperRenderer.extend({
  mapperType: "ParameterMappingRenderer",
  popupTitle: 'Parameter Mapping',
  argTitle: 'Map from this dashboard',
  valTitle: 'Map to other dashboard',
  dataSaveProp: "otherDashboardParameters",
  getRequestUrl: function() {
    return wd.helpers.editor.getDashboardParametersUrl();
  },
  requestSuccess: function(data) {
    this.otherDashboardParameters = data ? data.parameters || [] : [];
  },
  requestError: function(){
    this.otherDashboardParameters = [];
  }
});

var DataSourceMappingRenderer = abstractMapperRenderer.extend({
  mapperType: "DataSourceMappingRenderer",
  popupTitle: 'Data Source Mapping',
  argTitle: 'Map from this dashboard',
  valTitle: 'Map to other dashboard',
  dataSaveProp: "otherDashboardDataSources",
  valPlaceHolderText: 'Datasources...',
  datasourceFiltering: true,
  getRequestUrl: function() {
    return wd.helpers.editor.getDashboardDataSourcesUrl();
  },
  requestSuccess: function(data) {
    this.otherDashboardDataSources = data ? data.dataSources || [] : [];
  },
  requestError: function(){
    this.otherDashboardDataSources = [];
  }
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
