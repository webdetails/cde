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

var LayoutPanel = Panel.extend({

  id: "layout",
  name: "Layout Panel",
  treeTable: {},
  treeTableModel: {},
  propertiesTable: {},
  propertiesTableModel: {},

  constructor: function(id) {
    this.base(id);
    this.logger = new Logger("Layout");
    Panel.register(this);
  },

  init: function() {
    var operationSets = {
      blueprint: [
        new LayoutSaveAsTemplateOperation(),
        new LayoutApplyTemplateOperation(),
        new LayoutAddResourceOperation(),
        new LayoutAddFreeFormOperation(),
        new LayoutAddRowOperation(),
        new LayoutAddColumnsOperation(),
        new LayoutAddSpaceOperation(),
        new LayoutAddImageOperation(),
        new LayoutAddHtmlOperation(),
        new LayoutDuplicateOperation(),
        new LayoutDeleteOperation()
      ],
      bootstrap: [
        new LayoutSaveAsTemplateOperation(),
        new LayoutApplyTemplateOperation(),
        new LayoutAddResourceOperation(),
        new LayoutAddBootstrapPanelOperation(),
        new LayoutAddFreeFormOperation(),
        new LayoutAddRowOperation(),
        new LayoutAddColumnsOperation(),
        new LayoutAddSpaceOperation(),
        new LayoutAddImageOperation(),
        new LayoutAddHtmlOperation(),
        new LayoutDuplicateOperation(),
        new LayoutDeleteOperation()
      ],
      mobile: [
        new LayoutSaveAsTemplateOperation(),
        new LayoutApplyTemplateOperation(),
        new LayoutAddResourceOperation(),
        new LayoutAddCarouselOperation(),
        new LayoutAddCarouselItemOperation(),
        new LayoutAddFilterBlockOperation(),
        new LayoutAddFilterRowOperation(),
        new LayoutAddFilterHeaderOperation(),
        new LayoutAddFreeFormOperation(),
        new LayoutAddRowOperation(),
        new LayoutAddColumnsOperation(),
        new LayoutAddSpaceOperation(),
        new LayoutAddImageOperation(),
        new LayoutAddHtmlOperation(),
        new LayoutDuplicateOperation(),
        new LayoutDeleteOperation()
      ]
    };

    this.base();
    this.logger.debug("Specific init");

    // Tree
    this.treeTable = new TableManager(LayoutPanel.TREE);
    this.treeTable.setTitle("Layout Structure");

    var dashboardType = cdfdd.dashboardWcdf.rendererType || "bootstrap";
    this.treeTable.setOperations(operationSets[dashboardType]);

    var treeTableModel = new TableModel('layoutTreeTableModel');
    treeTableModel.setColumnNames(['Type', 'Name']);
    treeTableModel.setColumnGetExpressions([
      function(row) { return row.typeDesc; },
      function(row) { return row.properties[0].value; }
    ]);
    treeTableModel.setColumnTypes(['String', 'String']);
    var rowId = function(row) { return row.id; };
    treeTableModel.setRowId(rowId);
    var rowType = function(row) { return row.type; };
    treeTableModel.setRowType(rowType);
    var parentId = function(row) { return row.parent; };
    treeTableModel.setParentId(parentId);
    var layoutRows = cdfdd.getDashboardData().layout.rows;
    treeTableModel.setData(layoutRows);
    this.treeTable.setTableModel(treeTableModel);
    this.treeTable.init();
    $('#' + LayoutPanel.TREE).addClass('selectedTable');

    // Properties
    this.propertiesTable = new TableManager(LayoutPanel.PROPERTIES);
    this.propertiesTable.setTitle("Properties");
    var propertiesTableModel = new PropertiesTableModel('layoutPropertiesTableModel');

    // If we set the name, we need to change the name in the treeTable
    propertiesTableModel.setColumnSetExpressions([undefined,
      function(row, value) {
        row.value = value;
        if(row.name == 'name') {
          var _tableManager = TableManager.getTableManager("table-" + LayoutPanel.TREE);
          this.logger.debug("Changing the name - applying to previous row in " + _tableManager + " in row " + _tableManager.getSelectedCell()[0]);
          var _cell = _tableManager.getSelectedCell();
          $("#" + _tableManager.getTableId() + " > tbody > tr:eq(" + _cell[0] + ") > td:eq(1)").text(value);
        }
      }
    ]);

    this.propertiesTable.setTableModel(propertiesTableModel);
    this.propertiesTable.init();

    this.treeTable.setLinkedTableManager(this.propertiesTable);
    this.treeTable.setLinkedTableManagerOperation(function(row) {
      var arr = [];
      for(p in row.properties) {
        if(row.properties.hasOwnProperty(p)) {
          arr.push(row.properties[p]);
        }
      }
      return arr;
    });

    $('#' + LayoutPanel.TREE).click(function(e) {
      $('#' + LayoutPanel.PROPERTIES).removeClass('selectedTable').addClass('unselectedTable');
      $('#' + LayoutPanel.TREE).addClass('selectedTable').removeClass('unselectedTable');
    });

    $('#' + LayoutPanel.PROPERTIES).click(function(e) {
      $('#' + LayoutPanel.TREE).addClass('unselectedTable').removeClass('selectedTable');
      $('#' + LayoutPanel.PROPERTIES).addClass('selectedTable').removeClass('unselectedTable');
    });
  },

  getContent: function() {
    return '' +
        '<div id="' + LayoutPanel.TREE + '" class="span-12 panel-scroll-element">Tree</div>\n' +
        '<div id="' + LayoutPanel.PROPERTIES + '" class="span-12 panel-scroll-element last">Properties</div>\n';
  },

  // Get HtmlObjects
  getHtmlObjects: function() {

    var data = this.treeTable.getTableModel().getData();
    var autoCompleteModels = [
      LayoutRowModel.MODEL, LayoutColumnModel.MODEL, LayoutBootstrapColumnModel.MODEL,
      LayoutFreeFormModel.MODEL, LayoutBootstrapPanelHeaderModel.MODEL, LayoutBootstrapPanelBodyModel.MODEL,
      LayoutBootstrapPanelBodyModel.MODEL
    ];
    var output = [];
    var myself = this;
    $.each(data, function(i, row) {
      if($.inArray(row.type, autoCompleteModels) > -1) {
        // Use the ones that don't have children and a name defined
        var rowProperties = row.properties;
        if(myself.treeTable.getTableModel().getIndexManager().getIndex()[row.id].children.length == 0
            && rowProperties[0].value != "") {
          output.push(row);
        }
      }
    });

    return output;
  },

  getDuplicateOperation: function() {
    return LayoutDuplicateOperation;
  },

  getSelectedTable: function() {
    var selectedTableId = $('#panel-' + this.id + ' .selectedTable').attr('id');

    return TableManager.getTableManager("table-" + selectedTableId);
  },

  selectNextTable: function() {
    var selectedTableId = $('#panel-' + this.id + ' .selectedTable').attr('id');

    if(selectedTableId == LayoutPanel.TREE && this.treeTable.isSelectedCell) {
      $('#' + LayoutPanel.PROPERTIES).click();
      return this.propertiesTable;
    } else if(selectedTableId == LayoutPanel.PROPERTIES) {
      $('#' + LayoutPanel.TREE).click();
      return this.treeTable;
    }
  }
}, {

  MAIN_PANEL: "layout_panel",
  TREE: "cdfdd-layout-tree",
  PROPERTIES: "cdfdd-layout-properties"
});

/*
 Layout Panel Models
 */
var LayoutResourceModel = BaseModel.extend({}, {

  MODEL: 'LayoutResource',

  getStub: function() {
    var _stub = {
      id: TableManager.generateGUID(),
      type: LayoutResourceModel.MODEL,
      typeDesc: "Resource",
      parent: IndexManager.ROOTID,
      properties: []
    };

    _stub.properties.push(PropertiesManager.getProperty("name"));
    _stub.properties.push(PropertiesManager.getProperty("resourceType"));
    _stub.properties.push(PropertiesManager.getProperty("resourceFile"));
    _stub.properties.push(PropertiesManager.getProperty("resourceCode"));

    return _stub;
  }
});
BaseModel.registerModel(LayoutResourceModel);

var LayoutResourceCodeModel = LayoutResourceModel.extend({}, {

  MODEL: 'LayoutResourceCode',

  getStub: function() {
    var _stub = LayoutResourceModel.getStub();

    _stub.type = this.MODEL;
    _stub.properties = [];
    _stub.properties.push(PropertiesManager.getProperty("name"));
    _stub.properties.push(PropertiesManager.getProperty("resourceType"));
    _stub.properties.push(PropertiesManager.getProperty("resourceCode"));

    return _stub;
  }
});
BaseModel.registerModel(LayoutResourceCodeModel);

var LayoutResourceFileModel = LayoutResourceModel.extend({}, {

  MODEL: 'LayoutResourceFile',

  getStub: function() {
    var _stub = LayoutResourceModel.getStub();

    _stub.type = this.MODEL;
    _stub.properties = [];
    _stub.properties.push(PropertiesManager.getProperty("name"));
    _stub.properties.push(PropertiesManager.getProperty("resourceType"));
    _stub.properties.push(PropertiesManager.getProperty("resourceFile"));

    return _stub;
  }
});
BaseModel.registerModel(LayoutResourceFileModel);

var LayoutBootstrapPanelModel = BaseModel.extend({}, {

  MODEL: 'BootstrapPanel',

  getStub: function() {
    var _stub = {
      id: TableManager.generateGUID(),
      type: LayoutBootstrapPanelModel.MODEL,
      typeDesc: "Bootstrap Panel",
      parent: IndexManager.ROOTID,
      properties: []
    };

    _stub.properties.push(PropertiesManager.getProperty("name"));
    _stub.properties.push(PropertiesManager.getProperty("height"));
    _stub.properties.push(PropertiesManager.getProperty("roundCorners"));
    _stub.properties.push(PropertiesManager.getProperty("cssClass"));
    _stub.properties.push(PropertiesManager.getProperty("bootstrapPanelStyle"));

    return _stub;
  }
});
BaseModel.registerModel(LayoutBootstrapPanelModel);

var LayoutBootstrapPanelHeaderModel = BaseModel.extend({}, {

  MODEL: 'BootstrapPanelHeader',

  getStub: function() {
    var _stub = {
      id: TableManager.generateGUID(),
      type: LayoutBootstrapPanelHeaderModel.MODEL,
      typeDesc: "Panel Header",
      parent: IndexManager.ROOTID,
      properties: []
    };

    _stub.properties.push(PropertiesManager.getProperty("name"));
    _stub.properties.push(PropertiesManager.getProperty("roundCorners"));
    _stub.properties.push(PropertiesManager.getProperty("cssClass"));
    _stub.properties.push(PropertiesManager.getProperty("textAlign"));
    return _stub;
  }
});
BaseModel.registerModel(LayoutBootstrapPanelHeaderModel);

var LayoutBootstrapPanelBodyModel = BaseModel.extend({}, {

  MODEL: 'BootstrapPanelBody',

  getStub: function() {
    var _stub = {
      id: TableManager.generateGUID(),
      type: LayoutBootstrapPanelBodyModel.MODEL,
      typeDesc: "Panel Body",
      parent: IndexManager.ROOTID,
      properties: []
    };

    _stub.properties.push(PropertiesManager.getProperty("name"));
    _stub.properties.push(PropertiesManager.getProperty("roundCorners"));
    _stub.properties.push(PropertiesManager.getProperty("cssClass"));
    _stub.properties.push(PropertiesManager.getProperty("textAlign"));
    return _stub;
  }
});
BaseModel.registerModel(LayoutBootstrapPanelBodyModel);

var LayoutBootstrapPanelFooterModel = BaseModel.extend({}, {

  MODEL: 'BootstrapPanelFooter',

  getStub: function() {
    var _stub = {
      id: TableManager.generateGUID(),
      type: LayoutBootstrapPanelFooterModel.MODEL,
      typeDesc: "Panel Footer",
      parent: IndexManager.ROOTID,
      properties: []
    };

    _stub.properties.push(PropertiesManager.getProperty("name"));
    _stub.properties.push(PropertiesManager.getProperty("roundCorners"));
    _stub.properties.push(PropertiesManager.getProperty("cssClass"));
    _stub.properties.push(PropertiesManager.getProperty("textAlign"));
    return _stub;
  }
});
BaseModel.registerModel(LayoutBootstrapPanelFooterModel);

var LayoutFreeFormModel = BaseModel.extend({}, {

  MODEL: 'LayoutFreeForm',

  getStub: function() {
    var _stub = {
      id: TableManager.generateGUID(),
      type: LayoutFreeFormModel.MODEL,
      typeDesc: "FreeForm",
      parent: IndexManager.ROOTID,
      properties: []
    };

    _stub.properties.push(PropertiesManager.getProperty("name"));
    _stub.properties.push(PropertiesManager.getProperty("cssClass"));
    _stub.properties.push(PropertiesManager.getProperty("elementTag"));
    _stub.properties.push(PropertiesManager.getProperty("otherAttributes"));

    return _stub;
  }
});
BaseModel.registerModel(LayoutFreeFormModel);

var LayoutRowModel = BaseModel.extend({}, {

  MODEL: 'LayoutRow',

  getStub: function() {
    var _stub = {
      id: TableManager.generateGUID(),
      type: LayoutRowModel.MODEL,
      typeDesc: "Row",
      parent: IndexManager.ROOTID,
      properties: []
    };

    _stub.properties.push(PropertiesManager.getProperty("name"));
    _stub.properties.push(PropertiesManager.getProperty("height"));
    _stub.properties.push(PropertiesManager.getProperty("backgroundColor"));
    _stub.properties.push(PropertiesManager.getProperty("roundCorners"));
    _stub.properties.push(PropertiesManager.getProperty("cssClass"));
    _stub.properties.push(PropertiesManager.getProperty("textAlign"));

    return _stub;
  }
});
BaseModel.registerModel(LayoutRowModel);

var LayoutColumnModel = BaseModel.extend({}, {

  MODEL: 'LayoutColumn',

  getStub: function() {
    var _stub = {
      id: TableManager.generateGUID(),
      type: LayoutColumnModel.MODEL,
      typeDesc: "Column",
      parent: IndexManager.ROOTID,
      properties: []
    };

    _stub.properties.push(PropertiesManager.getProperty("name"));
    _stub.properties.push(PropertiesManager.getProperty("columnSpan"));
    _stub.properties.push(PropertiesManager.getProperty("columnPrepend"));
    _stub.properties.push(PropertiesManager.getProperty("columnAppend"));
    _stub.properties.push(PropertiesManager.getProperty("columnPrependTop"));
    _stub.properties.push(PropertiesManager.getProperty("columnAppendBottom"));
    _stub.properties.push(PropertiesManager.getProperty("columnBorder"));
    _stub.properties.push(PropertiesManager.getProperty("columnBigBorder"));
    _stub.properties.push(PropertiesManager.getProperty("backgroundColor"));
    _stub.properties.push(PropertiesManager.getProperty("roundCorners"));
    _stub.properties.push(PropertiesManager.getProperty("height"));
    _stub.properties.push(PropertiesManager.getProperty("cssClass"));
    _stub.properties.push(PropertiesManager.getProperty("textAlign"));

    return _stub;
  }
});
BaseModel.registerModel(LayoutColumnModel);

var LayoutBootstrapColumnModel = BaseModel.extend({}, {

  MODEL: 'LayoutBootstrapColumn',

  getStub: function() {
    var _stub = {
      id: TableManager.generateGUID(),
      type: LayoutBootstrapColumnModel.MODEL,
      typeDesc: "Column",
      parent: IndexManager.ROOTID,
      properties: []
    };

    _stub.properties.push(PropertiesManager.getProperty("name"));
    _stub.properties.push(PropertiesManager.getProperty("bootstrapExtraSmall"));
    _stub.properties.push(PropertiesManager.getProperty("bootstrapSmall"));
    _stub.properties.push(PropertiesManager.getProperty("bootstrapMedium"));
    _stub.properties.push(PropertiesManager.getProperty("bootstrapLarge"));
    _stub.properties.push(PropertiesManager.getProperty("bootstrapCssClass"));
    _stub.properties.push(PropertiesManager.getProperty("backgroundColor"));
    _stub.properties.push(PropertiesManager.getProperty("roundCorners"));
    _stub.properties.push(PropertiesManager.getProperty("height"));
    _stub.properties.push(PropertiesManager.getProperty("cssClass"));
    _stub.properties.push(PropertiesManager.getProperty("textAlign"));

    return _stub;
  }
});
BaseModel.registerModel(LayoutBootstrapColumnModel);

var LayoutSpaceModel = BaseModel.extend({}, {

  MODEL: 'LayoutSpace',

  getStub: function() {
    var _stub = {
      id: TableManager.generateGUID(),
      type: LayoutSpaceModel.MODEL,
      typeDesc: "Space",
      parent: IndexManager.ROOTID,
      properties: []
    };

    _stub.properties.push(PropertiesManager.getProperty("height"));
    _stub.properties.push(PropertiesManager.getProperty("backgroundColor"));
    _stub.properties.push(PropertiesManager.getProperty("cssClass"));

    return _stub;
  }
});
BaseModel.registerModel(LayoutSpaceModel);

var LayoutImageModel = BaseModel.extend({}, {

  MODEL: 'LayoutImage',

  getStub: function() {
    var _stub = {
      id: TableManager.generateGUID(),
      type: LayoutImageModel.MODEL,
      typeDesc: "Image",
      parent: IndexManager.ROOTID,
      properties: []
    };

    _stub.properties.push(PropertiesManager.getProperty("url"));
    _stub.properties.push(PropertiesManager.getProperty("cssClass"));

    return _stub;
  }
});
BaseModel.registerModel(LayoutImageModel);

var LayoutHtmlModel = BaseModel.extend({}, {

  MODEL: 'LayoutHtml',

  getStub: function() {

    var _stub = {
      id: TableManager.generateGUID(),
      type: LayoutHtmlModel.MODEL,
      typeDesc: "Html",
      parent: IndexManager.ROOTID,
      properties: []
    };

    _stub.properties.push(PropertiesManager.getProperty("name"));
    _stub.properties.push(PropertiesManager.getProperty("html"));
    _stub.properties.push(PropertiesManager.getProperty("color"));
    _stub.properties.push(PropertiesManager.getProperty("fontSize"));
    _stub.properties.push(PropertiesManager.getProperty("cssClass"));

    return _stub;
  }
});
BaseModel.registerModel(LayoutHtmlModel);

/*
 Layout Panel Operations
 */
var LayoutSaveAsTemplateOperation = SaveAsTemplateOperation.extend({

  id: "LAYOUT_SAVEAS_TEMPLATE",

  constructor: function() {
    this.logger = new Logger("LayoutSaveAsTemplateOperation");
  },

  execute: function(tableManager) {
    var file = "";
    var title = "Custom Template";
    var includeComponents = true;
    var includeDataSources = true;
    var myself = this;
    var content = '' +
        '<div class="popup-input-container">\n' +
        '  <span class="popup-label" id="fileLabel" >File Name</span>\n' +
        '  <input class="popup-text-input" id="fileInput" placeholder="Insert Text..." type="text"/>\n' +
        '</div>\n' +

        '<div class="popup-input-container">\n' +
        '  <span class="popup-label">Title</span>\n' +
        '  <input class="popup-text-input" id="titleInput" placeholder="Insert Text..." type="text"/>\n' +
        '</div>\n' +

        '<div class="popup-input-container input-pair bottom clearfix">\n' +
        '  <div class="popup-checkbox-container">'+
        '    <input type="checkbox" checked="yes" id="includeComponentsInput" value="true"/>\n' +
        '    <label class="popup-input-label" for="includeComponentsInput">Include Components</label>\n' +
        '  </div>'+
        '  <div class="popup-checkbox-container">'+
        '    <input type="checkbox" checked="yes" id="includeDataSourcesInput" value="true"/>\n' +
        '    <label class="popup-input-label" for="includeDataSourcesInput">Include Datasources</label>\n' +
        '  </div>'+
        '</div>\n';

    var rv = "";
    var re = new RegExp("MSIE ([0-9]{1,}[\.0-9]{0,})");
    if(re.exec(navigator.userAgent) != null) {
      rv = parseFloat(RegExp.$1) < 10 ? "ie8" : "";
    }

    var contentWrapper = '' +
        '<div class="popup-header-container">\n' +
        '  <div class="popup-title-container">Save as Template</div>\n' +
        '</div>\n' +
        '<div class="popup-body-container layout-popup ' + rv + '">\n' + content + '</div>';


    $.prompt(contentWrapper, {buttons: { Save: true, Cancel: false }, top: "40px", prefix: "popup",
      loaded: function() {
        var $popup = $(this);

        $popup.addClass('settings-popup');
        CDFDDUtils.movePopupButtons($popup);
      },
      submit: function(v) {
        title = $("#titleInput").val();
        file = $("#fileInput").val();
        includeComponents = $("#includeComponentsInput").attr("checked");
        includeDataSources = $("#includeDataSourcesInput").attr("checked");

        var validate = true;
        if(file.length == 0) {
          $("#fileLabel").css("color", "red");
          $("#fileLabel").text("* File Name (required)");
          validate = false;
        }

        if(file.indexOf(".") != -1 && (file.length < 6 || file.lastIndexOf(".cdfde") != file.length - 6)) {
          $("#fileLabel").css("color", "red");
          $("#fileLabel").text("* File Name (Invalid file extension. Must be .cdfde)");
          validate = false;
        }

        if(file.indexOf(".") == -1) {
          file += ".cdfde";
        }

        return !v || validate;
      },

      callback: function(v, m, f) {
        if(v) {
          myself.logger.info("Saving template...");

          var template = cdfdd.getDashboardData();
          template.layout.title = title;
          template.style = cdfdd.dashboardWcdf.style;
          template.rendererType = cdfdd.dashboardWcdf.rendererType;
          if(!includeComponents) {
            template.components.rows = [];

          }
          if(!includeDataSources) {
            template.datasources.rows = [];
          }

          var templateParams = {operation: "save", file: file, cdfstructure: JSON.stringify(template), rendererType: cdfdd.dashboardWcdf.rendererType};
          SynchronizeRequests.doPost(templateParams);
        }
      }
    });
  }
});

var LayoutApplyTemplateOperation = ApplyTemplateOperation.extend({

  id: "LAYOUT_ADD_TEMPLATE",

  constructor: function() {
    this.logger = new Logger("LayoutApplyTemplateOperation");
  },

  canExecute: function(tableManager) {
    return true;
  },

  execute: function(tableManager) {
    this.logger.info("Loading templates...");

    var loadParams = { operation: "load", rendererType: cdfdd.dashboardWcdf.rendererType};
    SynchronizeRequests.doGetJson(loadParams);
  }
});

var LayoutAddResourceOperation = AddRowOperation.extend({

  id: "LAYOUT_ADD_RESOURCE",
  types: [],
  name: "Add Resource",
  description: "Adds a resource external file or code to the dashboard",

  models: [LayoutResourceFileModel.MODEL, LayoutResourceCodeModel.MODEL],
  canMoveInto: [],
  canMoveTo: [
    LayoutResourceCodeModel.MODEL, LayoutResourceFileModel.MODEL
  ],

  constructor: function() {
    this.logger = new Logger("LayoutAddResourceOperation");
  },

  canExecute: function(tableManager) {
    return true;
  },

  execute: function(tableManager) {

    // Add a row. This special type goes always to the beginning;
    var myself = this;
    var content = '' +
        '<div class="clearfix">\n' +

        '  <div class="popup-input-container bottom">\n' +
        '    <span class="popup-label">Resource Type</span>\n' +
        '    <select id="resourceType" class="popup-select">\n' +
        '      <option value=""></option>\n' +
        '      <option value="Css">Css</option>\n' +
        '      <option value="Javascript">Javascript</option>\n' +
        '    </select>\n' +
        '  </div>\n' +

        '  <div class="popup-input-container bottom last">\n' +
        '    <span class="popup-label">Resource Source</span>\n' +
        '    <select id="resourceSource" class="popup-select">\n' +
        '      <option value=""></option>\n' +
        '      <option value="file">External File</option>\n' +
        '      <option value="code">Code Snippet</option>\n' +
        '    </select>\n' +
        '  </div>\n' +

        '</div>\n';

    var contentWrapper = '' +
        '<div class="popup-header-container">\n' +
        '  <div class="popup-title-container">Add Resource</div>\n' +
        '</div>\n' +
        '<div class="popup-body-container layout-popup resource-popup">\n' + content + '</div>';

    $.prompt(contentWrapper, {
      buttons: {
        Ok: true,
        Cancel: false
      },

      top: "40px",
      prefix: "popup",

      loaded: function() {
        var $popup = $(this);
        var resourceType = $('#resourceType', $popup);
        var resourceSource = $('#resourceSource', $popup);

        CDFDDUtils.movePopupButtons($popup);
        CDFDDUtils.buildPopupSelect(resourceType, {});
        CDFDDUtils.buildPopupSelect(resourceSource, {});
      },

      submit: function(v) {
        myself.resourceSubmit(v, tableManager);
      }
    });
  },

  resourceSubmit: function(status, tableManager) {
    if(status) {
      var resourceType = $("#resourceType").val();
      var resourceSource = $("#resourceSource").val();

      var _stub = (resourceSource === 'file') ? LayoutResourceFileModel.getStub() : LayoutResourceCodeModel.getStub();
      _stub.properties[1].value = resourceType;

      var indexManager = tableManager.getTableModel().getIndexManager();
      var insertAtIdx = 0;
      tableManager.insertAtIdx(_stub, insertAtIdx);

      this.selectFirstProperty(tableManager);
    }
  }
});
CellOperations.registerOperation(new LayoutAddResourceOperation());

var LayoutAddBootstrapPanelOperation = AddRowOperation.extend({

  id: "LAYOUT_ADD_BOOTSTRAP_PANEL",
  types: [],
  name: "Add Bootstrap Panel",
  description: "Adds a bootstrap panel",

  models: [LayoutBootstrapPanelModel.MODEL],
  canMoveInto: [
    LayoutColumnModel.MODEL, LayoutBootstrapColumnModel.MODEL, LayoutFreeFormModel.MODEL,
    LayoutBootstrapPanelHeaderModel.MODEL, LayoutBootstrapPanelBodyModel.MODEL, LayoutBootstrapPanelFooterModel.MODEL
  ],
  canMoveTo: [
    LayoutRowModel.MODEL, LayoutSpaceModel.MODEL, LayoutHtmlModel.MODEL, LayoutImageModel.MODEL,
    LayoutBootstrapPanelModel.MODEL
  ],

  constructor: function() {
    this.logger = new Logger("LayoutAddBootstrapPanelOperation");
  },

  canExecute: function(tableManager) {
    return true;
  },

  execute: function(tableManager) {

    var _stub = LayoutBootstrapPanelModel.getStub();
    var indexManager = tableManager.getTableModel().getIndexManager();
    var header = new LayoutAddBootstrapPanelHeaderOperation();
    var body = new LayoutAddBootstrapPanelBodyOperation();
    var footer = new LayoutAddBootstrapPanelFooterOperation();
    var insertAtIdx = -1;

    if(tableManager.isSelectedCell) {
      var rowIdx = tableManager.getSelectedCell()[0];
      var colIdx = tableManager.getSelectedCell()[1];
      var rowId = tableManager.getTableModel().getEvaluatedId(rowIdx);
      var rowType = tableManager.getTableModel().getEvaluatedRowType(rowIdx);

      var nextSibling = indexManager.getNextSibling(rowId);
      if(typeof nextSibling == 'undefined') {
        insertAtIdx = indexManager.getLastChild(rowId).index + 1;
      } else {
        insertAtIdx = nextSibling.index;
      }

      if($.inArray(rowType, this.canMoveTo) > -1) {
        _stub.parent = indexManager.getIndex()[rowId].parent;
      } else if($.inArray(rowType, this.canMoveInto) > -1) {
        _stub.parent = rowId;
      } else {
        insertAtIdx = tableManager.getTableModel().getData().length;
      }
    } else {
      insertAtIdx = tableManager.getTableModel().getData().length;
    }

    this.logger.debug("Inserting bootstrap node after at " + insertAtIdx);
    tableManager.insertAtIdx(_stub, insertAtIdx);

    header.execute(tableManager);
    body.execute(tableManager);
    footer.execute(tableManager);

    tableManager.selectCell(insertAtIdx, colIdx);

    this.selectFirstProperty(tableManager);
  }
});
CellOperations.registerOperation(new LayoutAddBootstrapPanelOperation());

var LayoutAddBootstrapPanelHeaderOperation = AddRowOperation.extend({

  id: "LAYOUT_ADD_BOOTSTRAP_PANEL_HEADER",
  types: [],
  name: "Add Bootstrap Panel Header",
  description: "Adds a bootstrap node",

  constructor: function() {
    this.logger = new Logger("LayoutAddBootstrapPanelHeaderOperation");
  },

  execute: function(tableManager) {
    var _stub = LayoutBootstrapPanelHeaderModel.getStub();
    var indexManager = tableManager.getTableModel().getIndexManager();
    var rowIdx = tableManager.getSelectedCell()[0];
    var rowId = tableManager.getTableModel().getEvaluatedId(rowIdx);
    var rowType = tableManager.getTableModel().getEvaluatedRowType(rowIdx);

    _stub.parent = rowId;
    var insertAtIdx = indexManager.getIndex()[rowId].index + 1;

    this.logger.debug("Inserting bootstrap node after at " + insertAtIdx);
    tableManager.insertAtIdx(_stub, insertAtIdx);
  }
});

var LayoutAddBootstrapPanelBodyOperation = AddRowOperation.extend({

  id: "LAYOUT_ADD_BOOTSTRAP_PANEL_BODY",
  types: [],
  name: "Add Bootstrap Panel Body",
  description: "Adds a bootstrap node",

  constructor: function() {
    this.logger = new Logger("LayoutAddBootstrapPanelBodyOperation");
  },

  execute: function(tableManager) {
    var _stub = LayoutBootstrapPanelBodyModel.getStub();
    var indexManager = tableManager.getTableModel().getIndexManager();
    var rowIdx = tableManager.getSelectedCell()[0];
    var rowId = tableManager.getTableModel().getEvaluatedId(rowIdx);
    var rowType = tableManager.getTableModel().getEvaluatedRowType(rowIdx);

    _stub.parent = indexManager.getIndex()[rowId].parent;
    var insertAtIdx = indexManager.getIndex()[_stub.parent].index + 2;

    this.logger.debug("Inserting bootstrap node after at " + insertAtIdx);
    tableManager.insertAtIdx(_stub, insertAtIdx);
  }
});

var LayoutAddBootstrapPanelFooterOperation = AddRowOperation.extend({

  id: "LAYOUT_ADD_BOOTSTRAP_PANEL_FOOTER",
  types: [],
  name: "Add Bootstrap Panel Footer",
  description: "Adds a bootstrap node",

  constructor: function() {
    this.logger = new Logger("LayoutAddBootstrapPanelFooterOperation");
  },

  execute: function(tableManager) {
    var _stub = LayoutBootstrapPanelFooterModel.getStub();
    var indexManager = tableManager.getTableModel().getIndexManager();
    var rowIdx = tableManager.getSelectedCell()[0];
    var rowId = tableManager.getTableModel().getEvaluatedId(rowIdx);
    var rowType = tableManager.getTableModel().getEvaluatedRowType(rowIdx);

    _stub.parent = indexManager.getIndex()[rowId].parent;
    var insertAtIdx = indexManager.getIndex()[_stub.parent].index + 3;

    this.logger.debug("Inserting bootstrap node after at " + insertAtIdx);
    tableManager.insertAtIdx(_stub, insertAtIdx);
  }
});

var LayoutAddFreeFormOperation = AddRowOperation.extend({

  id: "LAYOUT_ADD_FREEFORM",
  types: [],
  name: "Add FreeForm",
  description: "Adds a freeForm element to the template",

  models: [LayoutFreeFormModel.MODEL],
  canMoveInto: [
    LayoutColumnModel.MODEL, LayoutBootstrapColumnModel.MODEL, LayoutFreeFormModel.MODEL,
    LayoutBootstrapPanelHeaderModel.MODEL, LayoutBootstrapPanelBodyModel.MODEL, LayoutBootstrapPanelFooterModel.MODEL
  ],
  canMoveTo: [
    LayoutRowModel.MODEL, LayoutSpaceModel.MODEL, LayoutHtmlModel.MODEL, LayoutImageModel.MODEL,
    LayoutBootstrapPanelModel.MODEL
  ],

  constructor: function() {
    this.logger = new Logger("LayoutAddFreeFormOperation");
  },

  canExecute: function(tableManager) {
    return true;
  },

  addRowOperationStub: function() {
    return LayoutFreeFormModel.getStub();
  }
});
CellOperations.registerOperation(new LayoutAddFreeFormOperation());

var LayoutAddRowOperation = AddRowOperation.extend({

  id: "LAYOUT_ADD_ROW",
  types: [],
  name: "Add Row",
  description: "Add a new row",

  models: [LayoutRowModel.MODEL],
  canMoveInto: [
    LayoutColumnModel.MODEL, LayoutBootstrapColumnModel.MODEL, LayoutBootstrapPanelHeaderModel.MODEL,
    LayoutBootstrapPanelBodyModel.MODEL, LayoutBootstrapPanelFooterModel.MODEL, LayoutFreeFormModel.MODEL
  ],
  canMoveTo: [
    LayoutRowModel.MODEL, LayoutSpaceModel.MODEL, LayoutHtmlModel.MODEL, LayoutImageModel.MODEL,
    LayoutBootstrapPanelModel.MODEL
  ],

  constructor: function() {
    this.logger = new Logger("LayoutAddRowOperation");
  },

  canExecute: function(tableManager) {
    var isLayoutTable = tableManager.getId() == LayoutPanel.TREE;
    return isLayoutTable;
  },

  addRowOperationStub: function() {
    return LayoutRowModel.getStub();
  }
});
CellOperations.registerOperation(new LayoutAddRowOperation());

var LayoutAddColumnsOperation = AddRowOperation.extend({

  id: "LAYOUT_ADD_COLUMN",
  types: [
    LayoutRowModel.MODEL, LayoutColumnModel.MODEL, LayoutBootstrapColumnModel.MODEL
  ],
  name: "Add Columns",
  description: "Add a new column",

  models: [LayoutBootstrapColumnModel.MODEL, LayoutColumnModel.MODEL],
  canMoveInto: [
    LayoutRowModel.MODEL
  ],
  canMoveTo: [
    LayoutColumnModel.MODEL, LayoutBootstrapColumnModel.MODEL, LayoutBootstrapPanelModel.MODEL,
    LayoutSpaceModel.MODEL, LayoutHtmlModel.MODEL, LayoutImageModel.MODEL, LayoutSpaceModel.MODEL
  ],

  constructor: function() {
    this.logger = new Logger("LayoutAddColumnsOperation");
  },

  addRowOperationStub: function() {
    return (cdfdd.dashboardWcdf.rendererType == "bootstrap")
        ? LayoutBootstrapColumnModel.getStub()
        : LayoutColumnModel.getStub();
  }
});
CellOperations.registerOperation(new LayoutAddColumnsOperation());

var LayoutAddSpaceOperation = AddRowOperation.extend({

  id: "LAYOUT_ADD_SPACE",
  types: [
    LayoutRowModel.MODEL, LayoutColumnModel.MODEL, LayoutBootstrapColumnModel.MODEL
  ],
  name: "Add Space",
  description: "Adds a horizontal rule",

  models: [LayoutSpaceModel.MODEL],
  canMoveInto: [
    LayoutColumnModel.MODEL, LayoutBootstrapColumnModel.MODEL, LayoutBootstrapPanelHeaderModel.MODEL,
    LayoutBootstrapPanelBodyModel.MODEL, LayoutBootstrapPanelFooterModel.MODEL, LayoutFreeFormModel.MODEL
  ],
  canMoveTo: [
    LayoutRowModel.MODEL, LayoutBootstrapPanelModel.MODEL,
    LayoutSpaceModel.MODEL, LayoutHtmlModel.MODEL, LayoutImageModel.MODEL
  ],

  constructor: function() {
    this.logger = new Logger("LayoutAddSpaceOperation");
  },

  addRowOperationStub: function() {
    return LayoutSpaceModel.getStub();
  }
});
CellOperations.registerOperation(new LayoutAddSpaceOperation());

var LayoutAddImageOperation = AddRowOperation.extend({

  id: "LAYOUT_ADD_IMAGE",
  types: [
    LayoutColumnModel.MODEL, LayoutBootstrapColumnModel.MODEL,
    LayoutFreeFormModel.MODEL, LayoutBootstrapPanelHeaderModel.MODEL,
    LayoutBootstrapPanelBodyModel.MODEL, LayoutBootstrapPanelFooterModel.MODEL
  ],
  name: "Add Image",
  description: "Adds an image",

  models: [LayoutImageModel.MODEL],
  canMoveInto: [
    LayoutColumnModel.MODEL, LayoutBootstrapColumnModel.MODEL, LayoutBootstrapPanelHeaderModel.MODEL,
    LayoutBootstrapPanelBodyModel.MODEL, LayoutBootstrapPanelFooterModel.MODEL, LayoutFreeFormModel.MODEL
  ],
  canMoveTo: [
    LayoutRowModel.MODEL, LayoutSpaceModel.MODEL, LayoutHtmlModel.MODEL, LayoutImageModel.MODEL, LayoutBootstrapPanelModel.MODEL
  ],

  constructor: function() {
    this.logger = new Logger("LayoutAddImageOperation");
  },

  addRowOperationStub: function() {
    return LayoutImageModel.getStub();
  }
});
CellOperations.registerOperation(new LayoutAddImageOperation());

var LayoutAddHtmlOperation = AddRowOperation.extend({

  id: "LAYOUT_ADD_HTML",
  types: [
    LayoutColumnModel.MODEL, LayoutBootstrapColumnModel.MODEL,
    LayoutBootstrapPanelHeaderModel.MODEL, LayoutBootstrapPanelBodyModel.MODEL,
    LayoutBootstrapPanelFooterModel.MODEL, LayoutFreeFormModel.MODEL
  ],
  name: "Add Html",
  description: "Adds plain Html code to the template",

  models: [LayoutHtmlModel.MODEL],
  canMoveInto: [
    LayoutColumnModel.MODEL, LayoutBootstrapColumnModel.MODEL, LayoutBootstrapPanelHeaderModel.MODEL,
    LayoutBootstrapPanelBodyModel.MODEL, LayoutBootstrapPanelFooterModel.MODEL, LayoutFreeFormModel.MODEL
  ],
  canMoveTo: [
    LayoutRowModel.MODEL, LayoutSpaceModel.MODEL, LayoutHtmlModel.MODEL, LayoutImageModel.MODEL, LayoutBootstrapPanelModel.MODEL
  ],

  constructor: function() {
    this.logger = new Logger("LayoutAddHtmlOperation");
  },

  addRowOperationStub: function() {
    return LayoutHtmlModel.getStub();
  }

});
CellOperations.registerOperation(new LayoutAddHtmlOperation());

var LayoutDuplicateOperation = DuplicateOperation.extend({

  id: "LAYOUT_DUPLICATE",
  types: [],
  name: "Duplicate Layout Element",
  description: "Duplicate the selected Element and all its children",

  constructor: function() {
    this.logger = new Logger("LayoutDuplicateOperation");
  },

  canExecute: function(tableManager) {
    var rowIdx = tableManager.getSelectedCell()[0],
        rowType = tableManager.getTableModel().getEvaluatedRowType(rowIdx),
        isBootstrapPanelChild = rowType == LayoutBootstrapPanelBodyModel.MODEL
            || rowType == LayoutBootstrapPanelFooterModel.MODEL || rowType == LayoutBootstrapPanelHeaderModel.MODEL;

    return tableManager.isSelectedCell && !isBootstrapPanelChild;
  },

  collapseDuplicated: function(duplicatedRowsData) {
    if(duplicatedRowsData.length > 1) {
      $.each(duplicatedRowsData.reverse(), function(i, row) {
        var node = $("#" + row.id);

        if(node.hasClass('parent')) {
          node.toggleBranch();
        } else {
          node.hide();
        }
      });
    }
  }
});
CellOperations.registerOperation(new LayoutDuplicateOperation);

var LayoutDeleteOperation = DeleteOperation.extend({

  id: "LAYOUT_DELETE",
  types: [],

  constructor: function() {
    this.logger = new Logger("LayoutDeleteOperation");
  }
});
CellOperations.registerOperation(new LayoutDeleteOperation);
