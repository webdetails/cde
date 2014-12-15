/*!
 * Copyright 2002 - 2014 Webdetails, a Pentaho company.  All rights reserved.
 *
 * This software was developed by Webdetails and is provided under the terms
 * of the Mozilla Public License, Version 2.0, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to  http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
 * the license for the specific language governing your rights and limitations.
*/

var WizardManager = Base.extend({

  wizardId: "BASEWIZARD",
  logger: {},
  title: "NOT SET",
  stub: {},

  constructor: function() {
    this.logger = new Logger("WizardManager");

    // Register this tablemanager in the global area
    WizardManager.register(this);
  },

  init: function() {

    this.logger.info("Initializing WizardManager");

    this.resetAll();
    this.initWizard();

    // Initialize jqModal
    $("#" + WizardManager.MAIN_DIALOG).jqm({modal: true });

  },

  render: function(_stub) {
    this.stub = _stub;
    this.init();
    this.resetWizard();
    this.renderWizard();
    $('#' + WizardManager.MAIN_DIALOG).jqmShow();
    if(typeof this.postRenderWizard == 'function') {
      this.postRenderWizard();
    }
  },

  resetAll: function() {
    // reset All
    $("#" + WizardManager.MAIN_DIALOG).empty();
  },

  resetWizard: function() {
    // reset body
    $("#" + WizardManager.WIZARD_LEFT_SECTION).empty();
    $("#" + WizardManager.WIZARD_CENTER_SECTION).empty();
  },

  renderWizard: function() {
    this.logger.warn("renderWizard: Method not implmemented");
  },

  postRenderWizard: function() {
    //default - do nothing
  },

  initWizard: function() {
    var wizard = '\n' +
        '<div class="container">  \n' +
        '	<div class="span-24 last round cdfdd-wizard-title"><h1>' + this.getTitle() + '</h1></div>\n' +
        '	<div id="wizardDialogBody">\n' +
        '   <form onsubmit="return false">\n' +
        '	    <div id="wizardDialogLeftSection" class="cdfdd-wizard-left span-5 round"></div>\n' +
        '			<div id="wizardDialogCenterSection" class="cdfdd-wizard-center span-19 last round"></div>\n' +
        '		</form>\n' +
        '	</div>\n' +
        '	<!-- buttons -->\n' +
        '	<div class="clear span-15" >&nbsp;</div>\n' +
        '	<div class="cdfdd-wizard-buttons span-9 last round">\n' +
        '	  <form onsubmit="return false;">\n' +
        '			<input id="cdfdd-wizard-button-ok" type="button" disabled onclick="WizardManager.getWizardManager(\'' + this.getWizardId() + '\').buttonOk()" value="Ok"></input> \n' +
        '			<input id="cdfdd-wizard-button-cancel" type="button" onclick="WizardManager.getWizardManager(\'' + this.getWizardId() + '\').buttonCancel()" value="Cancel"></input>\n' +
        '		</form>\n' +
        '</div>\n';

    $("#" + WizardManager.MAIN_DIALOG).append(wizard);
    $(".cdfdd-wizard-title").corner();
  },


  buttonOk: function() {
    this.logger.warn("OK Button pressed - not implemented yet");
    $('#' + WizardManager.MAIN_DIALOG).jqmHide();
    return false;
  },

  buttonCancel: function() {
    this.logger.warn("Cancel Button pressed - not implemented yet");
    $('#' + WizardManager.MAIN_DIALOG).jqmHide();
  },

  // Accessors
  setWizardId: function(wizardId) {
    this.wizardId = wizardId;
  },
  getWizardId: function() {
    return this.wizardId;
  },
  setTitle: function(title) {
    this.title = title;
  },
  getTitle: function() {
    return this.title;
  }

}, {
  MAIN_DIALOG: "wizardDialog",
  WIZARD_BODY: "wizardDialogBody",
  WIZARD_LEFT_SECTION: "wizardDialogLeftSection",
  WIZARD_CENTER_SECTION: "wizardDialogCenterSection",
  WIZARD_RIGHT_SECTION: "wizardDialogRightSection",

  wizardManagers: {},

  register: function(wizardManager) {
    WizardManager.wizardManagers[wizardManager.getWizardId()] = wizardManager;
  },

  getWizardManager: function(id) {
    return WizardManager.wizardManagers[id];
  },

  executeOperation: function(wizardManagerId, idx) {
    var wizardManager = WizardManager.getWizardManager(wizardManagerId);
    wizardManager.getOperations()[idx].execute(wizardManager);
  },

  globalInit: function() {
    // Enable the table selectors
    $(document).on("mouseover", "td.draggableDimension, td.draggableMeasure, td.draggableFilter", function() {
      var myself = $(this);

      myself.parent().parent().find("td").removeClass("ui-state-active");
      myself.addClass("ui-state-active");
    });
  }
});

var SaikuOlapWizard = WizardManager.extend({

  wizardId: "SAIKU_OLAP_WIZARD",
  title: "Saiku OLAP Wizard",

  constructor: function() {
    this.base();
    this.logger = new Logger("SaikuOlapWizard");
    WizardManager.register(this);
  },

  init: function() {
    this.base();
  },

  renderWizard: function() {
    var saikuContent = '\n' +
        '<div id="SaikuOlapWizardContent"> <br/>\n' +
        ' <table>\n' +
        '   <tr id="fileChooser">\n' +
        '    <td><input id="newSaikuQuery" type="button" value="New Saiku Query"/></td>\n' +
        '   </tr>\n' +
        ' </table><br/>\n' +
        ' <div id="SaikuWindow" /> <br/>\n' +
        '</div>\n';

    var saikuCallback = function(filename) {
      var splitFile = filename.split("/");
      var solution = splitFile[1];
      var action = splitFile[splitFile.length - 1];
      var path = "";
      if(splitFile.length > 3) {
        for(var i = 2; i < splitFile.length - 1; i++) {
          path += "/" + splitFile[i];
        }
      }
      var iframe = '<iframe id="saikuIframe" src="' + wd.cde.endpoints.getSaikuUiPluginUrl() +
          '&solution=' + solution + '&path=' + path + '&action=' + action +
          '&dimension_prefetch=false#query/open/' + action + '" width="100%" height="400px" />';
      $('#SaikuWindow').html(iframe);
      $("#cdfdd-wizard-button-ok").removeAttr("disabled");
    };

    $("#" + WizardManager.WIZARD_BODY).html(saikuContent);
    $("#newSaikuQuery").click(function() {
      var iframe = '<iframe id="saikuIframe" src="' + wd.cde.endpoints.getSaikuUiPluginUrl() + '" width="100%" height="400px" />';
      $('#SaikuWindow').html(iframe);
      $("#cdfdd-wizard-button-ok").removeAttr("disabled");
    });

    var spr = new SaikuPathRenderer();
    spr.render($('#fileChooser'), "Choose existing Saiku Query", saikuCallback);

    $(".round", "#" + WizardManager.WIZARD_BODY).corner();

  },

  buttonOk: function() {
    // callback method for getSaikuMdx()
    window.saveSaiku = function(saikuStub) {
      var saikuMondrianStub = BaseModel.getModel('Componentsmdx_mondrianJndi').getStub();
      CDFDDUtils.getProperty(saikuMondrianStub, "name").value = "olapQuery";

      CDFDDUtils.getProperty(saikuMondrianStub, "jndi").value = saikuStub.jndi;
      saikuStub.catalog = saikuStub.catalog.replace('solution:', '');
      CDFDDUtils.getProperty(saikuMondrianStub, "catalog").value = saikuStub.catalog;
      CDFDDUtils.getProperty(saikuMondrianStub, "query").value = saikuStub.mdx;

      var componentsPalleteManager = PalleteManager.getPalleteManager(DatasourcesPanel.PALLETE_ID);
      var entry = componentsPalleteManager.getEntries()['MDX_MONDRIANJNDI_ENTRY'];
      var componentsTableManager = componentsPalleteManager.getLinkedTableManager();

      insertAtIdx = componentsTableManager.createOrGetParent(entry.getCategory(), entry.getCategoryDesc());
      saikuMondrianStub.parent = entry.getCategory();
      componentsTableManager.insertAtIdx(saikuMondrianStub, insertAtIdx);
      $('#' + WizardManager.MAIN_DIALOG).jqmHide();
    };
    // call the get mdx method within saiku
    window.getSaikuMdx();
  }
});
var wizard = new SaikuOlapWizard();


var OlapWizard = WizardManager.extend({

  wizardId: "OLAP_WIZARD",
  title: "OLAP Wizard",
  catalogs: {},
  olapObjects: {DimensionTableData: [], MeasureTableData: [], FilterTableData: []},
  selectedWizardObjects: {rows: [], columns: [], filters: []},
  selectedOptions: {},
  levelType: "",
  levelDirection: "",

  constructor: function() {
    this.base();
    this.logger = new Logger("OlapWizard");
  },

  init: function() {
    this.base();
    this.resetOlapObjects();
    this.resetSelectedWizardObjects();
  },

  renderWizard: function() {


    var leftSectionContent = '\n' +
        '<div class="cdfdd-wizard-olap-cubes span-5 round last">\n' +
        '	<h3>Cube:</h3>\n' +
        ' <div id="cdfddOlapCubeSelector" class="centeredText"></div>\n' +
        '</div>\n' +
        '<div class="cdfdd-wizard-olap-dimensions span-5 round last">\n' +
        '	<div id="cdfddOlapDimensionDiv" class="centeredText">\n' +
        '		<table id="cdfddOlapDimensionSelector" class="cdfdd small ui-reset ui-clearfix ui-component ui-hover-state">\n' +
        '			<caption class="ui-state-hover">Dimensions</caption>\n' +
        '			<thead></thead>\n' +
        '			<tbody class="ui-widget-content"></tbody>\n' +
        '		</table>\n' +
        '		<table id="cdfddOlapMeasureSelector" class="cdfdd small ui-reset ui-clearfix ui-component ui-hover-state">\n' +
        '			<caption class="ui-state-hover">Measures</caption>\n' +
        '			<thead></thead>\n' +
        '			<tbody class="ui-widget-content"></tbody>\n' +
        '		</table>\n' +
        '		<table id="cdfddOlapFilterSelector" class="cdfdd small ui-reset ui-clearfix ui-component ui-hover-state">\n' +
        '		  <caption class="ui-state-hover">Filters</caption>\n' +
        '		  <thead></thead>\n' +
        '			<tbody class="ui-widget-content"></tbody>\n' +
        '		</table>\n' +
        '	</div>\n' +
        '</div>\n';

    var centerSectionContent = '\n' +
        '<div class="span-19 round last">\n' +
        '	<div class="span-13">\n' +
        '		<div id="cdfdd-olap-preview" >\n' +
        '		  <i>Preview Area:</i>\n' +
        '     <input id="cdfdd-olap-enable-preview" type="checkbox" onchange="WizardManager.getWizardManager(\'' + this.getWizardId() + '\').processChange()" value="true" checked="checked"></input>\n' +
        '			<div id="cdfdd-olap-preview-area"></div>\n' +
        '		</div>\n' +
        '		<div id="cdfdd-olap-rows" class="cdfdd-olap-droppable round"><i>Rows: </i><br/><span class="options"></span></div>\n' +
        '		<div id="cdfdd-olap-columns" class="cdfdd-olap-droppable round"><i>Columns: </i><br/><span class="options"></span></div>\n' +
        '		<div id="cdfdd-olap-filters" class="cdfdd-olap-droppable round"><i>Filters: </i><br/><span class="options"></span></div>\n' +
        '	</div>\n' +
        '	<div id="cdfdd-component-options" class="span-6 cdfdd-wizard-components-options last round"></div>\n' +
        '</div>';

    $("#" + WizardManager.WIZARD_LEFT_SECTION).append(leftSectionContent);
    $("#" + WizardManager.WIZARD_CENTER_SECTION).append(centerSectionContent);

    this.renderMainOlapArea();
    this.renderCubeSelector();

    $(".round", "#" + WizardManager.WIZARD_BODY).corner();

    // Accept droppable
    var myself = this;
    $("#cdfdd-olap-rows").droppable({
      accept: this.getDroppableRows(),
      hoverClass: 'ui-state-active',
      drop: function(ev, ui) {
        WizardManager.getWizardManager(myself.wizardId).processDroppable("rows", $(this), $(ui.draggable));
      }
    });
    $("#cdfdd-olap-columns").droppable({
      accept: this.getDroppableColumns(),
      hoverClass: 'ui-state-active',
      drop: function(ev, ui) {
        WizardManager.getWizardManager(myself.wizardId).processDroppable("columns", $(this), $(ui.draggable));
      }
    });

    $("#cdfdd-olap-filters").droppable({
      accept: this.getDroppableFilters(),
      hoverClass: 'ui-state-active',
      drop: function(ev, ui) {
        WizardManager.getWizardManager(myself.wizardId).processDroppable("filters", $(this), $(ui.draggable));
      }
    });

  },

  getDroppableRows: function() {
    this.logger.error("getDroppableRows not implemented");
  },

  getDroppableColumns: function() {
    this.logger.error("getDroppableColumns not implemented");
  },

  getDroppableFilters: function() {
    this.logger.error("getDroppableFilters not implemented");
  },

  getMainOlapArea: function() {

    this.logger.error("getMainOlapArea not implemented");
  },

  renderCubeSelector: function() {
    // Fetch list of cubes from server
    var myself = this;

    OlapWizardRequests.olapManager({}, myself);
  },

  renderDimensions: function() {
    this.logger.warn("renderDimensions - Not done yet");
  },

  catalogSelected: function() {
    var _selector = $("#cdfddOlapCubeSelector");
    var selectedCatalog = this.getCatalog();

    var cubes = selectedCatalog.cubes;
    $("select#cdfddOlapCubeSelect", _selector).empty();
    $.each(cubes, function(i, cube) {
      $("select#cdfddOlapCubeSelect", _selector).append("<option id=\"" + cube.id + "\">" + cube.name + "</option>");
    });
    this.cubeSelected();
  },

  cubeSelected: function() {
    var selectedCatalog = $("#cdfddOlapCatalogSelect").val();
    var selectedCube = $("#cdfddOlapCubeSelect").children(":selected").attr("id");
    this.logger.debug("Cube Selected: " + selectedCube);

    this.resetOlapObjects();
    this.resetSelectedWizardObjects();

    // Fetch dimension structure of selected cube from server
    var params = {
      catalog: selectedCatalog,
      cube: selectedCube
    };
    var myself = this;

    OlapWizardRequests.olapCubeSelected(params, selectedCube, selectedCatalog, myself);
  },

  getAvailableFilters: function() {
    var myself = this;
    var filters = Panel.getPanel(ComponentsPanel.MAIN_PANEL).getParameters();
    var filterTBody = $("#cdfddOlapFilterSelector > tbody");
    filterTBody.empty();
    $.each(filters, function(i, filter) {
      if(filter.dimension != undefined) {
        filterTBody.append("<tr class='olapObject'><td class='draggableFilter'\">" + filter.properties[0].value + "</td></tr>");
        myself.addOlapObject(WizardOlapObjectManager.FILTER, filter);
      }
    });
    filterTBody.parent().treeTable();
    $("td.draggableFilter", filterTBody).draggable({helper: 'clone'});
  },

  processDroppable: function(type, drop, drag) {
    var myself = this;
    var levelIdx = drag.parent().prevAll("tr.olapObject").length;
    var wizardObjectType = drag.attr("class").replace(/draggable/, "").split(" ")[0];

    //1. Add selected olap Object
    var wizardObject = this.addSelectedWizardObject(type, wizardObjectType, this.getOlapObject(wizardObjectType, levelIdx));

    //2. Render olap Object
    var container = drop.find(".options");
    var htmlWizardObject = wizardObject.render(container);

    //3. Add clear button.
    var clearButtonContainer = $('<div class="cdfdd-olap-clearButton"></div>');
    var clearButton = $('<a border="0"><img src="' + wd.cde.endpoints.getImageResourceUrl() + '/images/clear.gif">&nbsp;</a>');
    $(clearButton).bind('click', function() {
      myself.removeSelectdWizardObject(type, wizardObject);
      clearButtonContainer.remove();
      myself.processChange();
    });
    container.append(clearButtonContainer.append(clearButton));

    this.processChange();
  },

  removeSelectdWizardObject: function(type, object) {
    var index = this.selectedWizardObjects[type].indexOf(object);
    this.selectedWizardObjects[type][index].clear();
    this.selectedWizardObjects[type].splice(index, 1);
  },

  processChange: function() {
    this.logger.warn("processChange - Not done yet");
  },

  getCatalog: function() {
    var catalog = null;
    var selectedCatalog = $("#cdfddOlapCatalogSelect").val();
    $.each(this.getCatalogs(), function(i, cat) {

      if(cat.name == selectedCatalog) {
        catalog = cat;
        return false;
      }
    });
    return catalog;
  },

  getCube: function() {
    return $("#cdfddOlapCubeSelect").children(":selected").attr("id");
  },

  // Accessors
  setCatalogs: function(catalogs) {
    this.catalogs = catalogs;
  },
  getCatalogs: function() {
    return this.catalogs;
  },

  setSelectedOptions: function(selectedOptions) {
    this.selectedOptions = selectedOptions;
  },
  getSelectedOptions: function() {
    return this.selectedOptions;
  },

  addOlapObject: function(type, object) {
    this.olapObjects[type + "TableData"].push(object);
  },

  getOlapObject: function(type, index) {
    return this.olapObjects[type + "TableData"][index];
  },

  addSelectedWizardObject: function(type, olapType, object) {
    var selectedWizarObject = WizardOlapObjectManager.getOlapObject(olapType, object);
    var myself = this;
    selectedWizarObject.setProcessChange(function() {
      myself.processChange();
    });
    this.selectedWizardObjects[type].push(selectedWizarObject);
    return selectedWizarObject;
  },

  getSelectedWizardObject: function(type, index) {
    return this.selectedWizardObjects[type][index];
  },

  getSelectedRowsValue: function(preview) {
    var rows = [];
    for(o in this.selectedWizardObjects.rows) {
      if(this.selectedWizardObjects.rows.hasOwnProperty(o)) {
        rows.push(this.selectedWizardObjects.rows[o].getValue(preview));
      }
    }
    return rows;
  },

  getSelectedColumnsValue: function(preview) {
    var columns = [];
    for(o in this.selectedWizardObjects.columns) {
      if(this.selectedWizardObjects.columns.hasOwnProperty(o)) {
        columns.push(this.selectedWizardObjects.columns[o].getValue(preview));
      }
    }
    return columns;
  },

  getListenners: function() {
    var listeners = "[";
    for(objs in this.selectedWizardObjects) {
      if(this.selectedWizardObjects.hasOwnProperty(objs)) {
        for(o in this.selectedWizardObjects[objs]) {
          if(this.selectedWizardObjects[objs].hasOwnProperty(o)) {
            if(this.selectedWizardObjects[objs][o].getId != undefined) {
              listeners += "\"" + this.selectedWizardObjects[objs][o].getId() + "\",";
            }
          }
        }
      }
    }
    return listeners.length > 1 ? listeners.replace(/,$/, "]") : "";
  },

  getComponentParameters: function() {
    var params = '[';

    for(var i = 0; i < this.selectedWizardObjects.filters.length; i++) {
      var name = this.selectedWizardObjects.filters[i].olapObject.properties[0].value;
      if(i > 0) params += ',';
      params += '["' + name + '","' + name + '"]';
    }

    return params + ']';
  },

  // Resets

  resetOlapObjects: function() {
    this.olapObjects = {DimensionTableData: [], MeasureTableData: [], FilterTableData: []};
  },

  resetSelectedWizardObjects: function() {
    this.selectedWizardObjects = {rows: [], columns: [], filters: []};
  },

  addQueryToDatasources: function(series) {
    var datasourcesPalleteManager = PalleteManager.getPalleteManager(DatasourcesPanel.PALLETE_ID);
    var datasourcesTableManager = datasourcesPalleteManager.getLinkedTableManager();

    var queryModel = BaseModel.getModel('Componentsmdx_mondrianJndi');//TODO: any way to fetch this?
    var topCount = this.getSelectedOptions().topCount;
    topCount = (topCount.length > 0) ? topCount : undefined;
    var datasourceStub = queryModel.getStub();

    CDFDDUtils.getProperty(datasourceStub, "name").value = this.getSelectedOptions().name + "Query";
    CDFDDUtils.getProperty(datasourceStub, "jndi").value = this.getSelectedOptions().jndi;
    CDFDDUtils.getProperty(datasourceStub, "catalog").value = this.getSelectedOptions().schema.replace('solution:', '');
    CDFDDUtils.getProperty(datasourceStub, "query").value = this.buildQuery(false, topCount);
    if(this.includeUniqueName) {
      CDFDDUtils.getProperty(datasourceStub, "output").value = '["1","0"]';
    }

    if(this.selectedWizardObjects.filters) {
      var queryParamsStr = '[';
      for(var i = 0; i < this.selectedWizardObjects.filters.length; i++) {
        var filter = this.selectedWizardObjects.filters[i].olapObject;//ToDo: for now accepts only as string
        if(i > 0) {
          queryParamsStr += ',';
        }
        queryParamsStr += '[' + '"' + filter.properties[0].value + '",' +
            '"' + filter.dimension + '", "String",""]';//["<name>","<default>","String",""]
      }
      queryParamsStr += ']';
      CDFDDUtils.getProperty(datasourceStub, "parameters").value = queryParamsStr;
    }

    var mdxEntry = datasourcesPalleteManager.getEntries()['MDX_MONDRIANJNDI_ENTRY'];//TODO: any way to fetch this?
    var insertAtIdx = datasourcesTableManager.createOrGetParent(mdxEntry.getCategory(), mdxEntry.getCategoryDesc());
    datasourceStub.parent = mdxEntry.getCategory();
    datasourcesTableManager.insertAtIdx(datasourceStub, insertAtIdx);
  },

  getFilterValue: function(filter, preview) {
    if(preview) {
      return filter.olapObject.dimension;
    }
    else {
      return '${' + filter.olapObject.properties[0].value + '}';
    }
  },

  /**
   * Create the mdx query from elements
   */
  buildQuery: function(preview, topCount) {
    var isSelector = this.includeUniqueName == true;

    var rows = this.getSelectedRowsValue(preview).join(" * ");
    var columnsArr = this.getSelectedColumnsValue(preview);
    var columns = columnsArr.join(" * ");
    var cube = this.getSelectedOptions().cube;
    var sets = [];
    var members = [];
    var conditions = [];
    var isFirstColumnMeasure = columnsArr.length > 0 && this.getSelectedWizardObject("columns", 0).olapObject.type == 'measure';

    for(o in this.selectedWizardObjects.filters) {
      if(this.selectedWizardObjects.filters.hasOwnProperty(o)) {
        conditions.push(this.getFilterValue(this.selectedWizardObjects.filters[o], preview));
      }
    }
    //ini
    var rootDim = this.getSelectedWizardObject('rows', 0).member;
    if(rootDim == null) {
      rootDim = '';
    }
    rootDim = rootDim.substring(0, rootDim.indexOf(']') + 1);

    var memberDecl = "with member [Measures].[Name] as '" + rootDim + ".CurrentMember.UniqueName'";

    var filterBegin = " filter(";
    var filterEnd = ", not isempty((" + rootDim + ".CurrentMember" +
        (columnsArr.length > 0 ? (", " + columnsArr[0] ) : "") + ")) ) ";
    var preMeasure = "[Measures].[Name]";
    if(columns.length > 0) preMeasure += ',';

    //fim
    var preRows = topCount != undefined ?
        "TopCount(" :
        "";
    if(isSelector) {
      preRows += filterBegin;
    }

    var posRows = topCount != undefined ?
        ", " + topCount + ((isFirstColumnMeasure) ? ', ' + columnsArr[0] + ")" : ")") :
        "";
    if(isSelector) {
      posRows = filterEnd + posRows;
    }

    var nonEmptyPreStr = columns.length > 0 ? "NON EMPTY(" : "";
    var nonEmptyPosStr = columns.length > 0 ? ")" : "";

    var query = (isSelector ? memberDecl : '' ) + sets.join(" , \n") + members.join(" , \n") + " select " + nonEmptyPreStr +
        preRows + "{" + rows + "}" + posRows + nonEmptyPosStr + " on ROWS, \n " +
        nonEmptyPreStr + "{" + (isSelector ? preMeasure : '' ) + columns + "}" + nonEmptyPosStr + " on Columns \n from [" + cube + "]";

    if(conditions.length > 0) {
      query += "\n where (" + conditions.join(" , ") + ")";
    }

    return sets.length > 0 || members.length > 0 ? "with \n" + query : query;

  }

}, {
});


var OlapParameterWizard = OlapWizard.extend({

  wizardId: "OLAP_PARAMETER_WIZARD",
  includeUniqueName: true,

  constructor: function() {
    this.base();
    this.logger = new Logger("OlapParameterWizard");
  },

  renderMainOlapArea: function() {
    var content = $('\n' +
        '<div class="cdfdd-component-options-label">Name: <input id="cdfdd-olap-parameter-name" class="invalid" type="text" onchange="WizardManager.getWizardManager(\'' + this.getWizardId() + '\').processChange()" ></input></div>\n' +
        '<hr/>\n' +
        '<div class="cdfdd-component-options-label">Type:<select class="cdfdd-component-options-type" onchange="WizardManager.getWizardManager(\'' + this.getWizardId() + '\').processChange()" id="cdfdd-olap-parameter-type" name="cdfdd-olap-parameter-type"></select></div>\n' +
        '<div class="cdfdd-component-options-label">Html Object:<select class="cdfdd-component-options-htmlobject" onchange="WizardManager.getWizardManager(\'' + this.getWizardId() + '\').processChange()" id="cdfdd-olap-parameter-htmlobject" name="cdfdd-olap-parameter-htmlobject"></select></div>\n' +
        '<div class="cdfdd-component-options-label">Top Count:<select class="cdfdd-component-options-topCount" onchange="WizardManager.getWizardManager(\'' + this.getWizardId() + '\').processChange()" id="cdfdd-olap-parameter-topcount" name="cdfdd-olap-parameter-topcount"></select></div>\n');

    var olapMain = $("#cdfdd-component-options");
    var selector = $("#cdfdd-olap-parameter-type", content);
    selector.append('<option value="selectComponent">Select</option>');
    selector.append('<option value="radioComponent">Radio Box</option>');
    selector.append('<option value="multiButtonComponent">Multiple Buttons</option>');
    var topCountSelector = $("#cdfdd-olap-parameter-topcount", content);
    var topCounts = ["", 5, 10, 15, 20, 25, 50, 100];
    for(t in topCounts)
      if(topCounts.hasOwnProperty(t)) {
        topCountSelector.append('<option value="' + topCounts[t] + '">' + topCounts[t] + '</option>');
      }

    topCountSelector.val(50);

    var data = Panel.getPanel(LayoutPanel.MAIN_PANEL).getHtmlObjects();
    var htmlObject = $("#cdfdd-olap-parameter-htmlobject", content);
    $.each(data, function(i, val) {
      htmlObject.append('<option value="' + val.properties[0].value + '" : >' + val.properties[0].value + '</option>');
    });

    olapMain.html(content);
  },

  getDroppableRows: function() {
    return "td.draggableDimension, td.draggableMeasure, td.draggableFilter";
  },

  getDroppableColumns: function() {
    return "td.draggableDimension, td.draggableMeasure, td.draggableFilter";
  },

  getDroppableFilters: function() {
    return "td.draggableDimension, td.draggableMeasure, td.draggableFilter";
  },

  processChange: function() {
    // Clean preview
    $("#cdfdd-olap-preview-area").empty();
    var preview = $("#cdfdd-olap-enable-preview:checked").length > 0;
    var catalog = this.getCatalog();
    var valid = this.selectedWizardObjects.rows.length > 0 && catalog != undefined;
    var topCount = $("#cdfdd-olap-parameter-topcount").val();

    $("#cdfdd-olap-preview").removeClass('disabled');//enable preview area, will be disabled if unchecked
    if(!preview) {
      $("#cdfdd-olap-preview").addClass('disabled');
    }

    if(valid & preview) {
      this.getSelectedOptions().name = $("#cdfdd-olap-parameter-name").val();
      this.getSelectedOptions().type = $("#cdfdd-olap-parameter-type").val();
      this.getSelectedOptions().jndi = catalog.jndi;
      this.getSelectedOptions().schema = catalog.schema;
      this.getSelectedOptions().cube = this.getCube();
      this.getSelectedOptions().topCount = topCount;

      var topCount = this.getSelectedOptions().topCount;
      this.getSelectedOptions().query = this.buildQuery(true, topCount.length > 0 ? topCount : undefined);

      this.preview();
    }

    // Name
    var name = $("#cdfdd-olap-parameter-name").val();
    if(name.length > 0) {
      $("#cdfdd-olap-parameter-name").removeClass("invalid");
      valid = valid & true;
    } else {
      $("#cdfdd-olap-parameter-name").addClass("invalid");
      valid = false;
    }

    if(valid) {
      $("#cdfdd-wizard-button-ok").removeAttr("disabled");
    } else {
      $("#cdfdd-wizard-button-ok").attr("disabled", "disabled");
    }
  },

  preview: function() {

    this.logger.debug("Launching preview");

    // Build cdf component
    CDFDDPreviewComponent = {
      name: "CDFDDPreviewComponent",
      type: this.getSelectedOptions().type,
      valueAsId: true,
      size: this.getSelectedOptions().type == "selectMultiComponent" ? 5 : 1,
      selectMulti: true,
      queryDefinition: {
        queryType: 'mdx',
        jndi: this.getSelectedOptions().jndi,
        catalog: this.getSelectedOptions().schema,
        query: this.getSelectedOptions().query,
        cube: this.getSelectedOptions().cube
      },
      parameters: {},

      htmlObject: "cdfdd-olap-preview-area",
      executeAtStart: true
    };
    Dashboards.components = [];
    Dashboards.finishedInit = false;
    Dashboards.init([CDFDDPreviewComponent]);

  },


  buttonOk: function() {
    this.apply();
    $('#' + WizardManager.MAIN_DIALOG).jqmHide();
  },

  apply: function() {

    // Generate parameter
    $('#' + WizardManager.MAIN_DIALOG).jqmHide();

    // 1 - Add query to datasources
    this.addQueryToDatasources();

    // 2 - Add parameter to components
    var componentsPaletteManager = PalleteManager.getPalleteManager(ComponentsPanel.PALLETE_ID);
    var componentsTableManager = componentsPaletteManager.getLinkedTableManager();

    var parameterModel = BaseModel.getModel(ComponentsOlapParameterModel.MODEL);
    var parameterStub = parameterModel.getStub();
    var parameterId = this.getSelectedOptions().name + "Parameter";
    var dimension = this.selectedWizardObjects.rows[0].member;
    var type = this.getSelectedOptions().type;

    CDFDDUtils.getProperty(parameterStub, "name").value = parameterId;
    CDFDDUtils.getProperty(parameterStub, "propertyValue").value = dimension;

    parameterStub.dimension = dimension;

    var parameterEntry = new ParameterEntry();
    var insertAtIdx = componentsTableManager.createOrGetParent(parameterEntry.getCategory(), parameterEntry.getCategoryDesc());
    parameterStub.parent = parameterEntry.getCategory();
    componentsTableManager.insertAtIdx(parameterStub, insertAtIdx);

    // 3 - Add selector to components
    var type = this.getSelectedOptions().type;
    var model = "";
    var entry = null;

    switch (type) {
      case "selectComponent":
        model = ComponentsSelectModel.MODEL;
        entry = new SelectEntry();
        break;
      case "selectMultiComponent":
        model = ComponentsSelectMultiModel.MODEL;
        entry = new SelectMultiEntry();
        break;
      case "autocompleteBoxComponent":
        model = ComponentsAutocompleteBoxModel.MODEL;
        entry = new AutocompleteBoxEntry();
        break;
      case "radioComponent":
        model = ComponentsradioModel.MODEL;
        entry = new radioEntry();
        break;
      case "checkComponent":
        model = ComponentscheckModel.MODEL;
        entry = new checkEntry();
        break;
      case "multiButtonComponent":
        model = ComponentsmultiButtonModel.MODEL;
        entry = new multiButtonEntry();
        break;
      default:
        this.logger.error("Not done yet!");
        alert("Type" + type + " not implemented");
    }

    var selectorModel = BaseModel.getModel(model);
    var selectorStub = selectorModel.getStub();

    CDFDDUtils.getProperty(selectorStub, "name").value = this.getSelectedOptions().name + "Selector";
    CDFDDUtils.getProperty(selectorStub, "parameter").value = this.getSelectedOptions().name + "Parameter";
    CDFDDUtils.getProperty(selectorStub, "dataSource").value = this.getSelectedOptions().name + "Query";
    CDFDDUtils.getProperty(selectorStub, "htmlObject").value = $("#cdfdd-olap-parameter-htmlobject").val();
    var valueAsIdProp = CDFDDUtils.getProperty(selectorStub, "valueAsId")
    if(valueAsIdProp) {
      valueAsIdProp.value = false;
    }

    var listeners = this.getListenners();
    if(listeners.length > 0) {
      CDFDDUtils.getProperty(selectorStub, "listeners").value = listeners;
    }

    var paramsProp = CDFDDUtils.getProperty(selectorStub, "parameters") || // by alias
        CDFDDUtils.getProperty(selectorStub, "xActionArrayParameter"); // by name
    paramsProp.value = this.getComponentParameters();

    //insert entry
    insertAtIdx = componentsTableManager.createOrGetParent(entry.getCategory(), entry.getCategoryDesc());
    selectorStub.parent = entry.getCategory();
    componentsTableManager.insertAtIdx(selectorStub, insertAtIdx);

  }
}, {
}); //end OlapParameterWizard
var wizard = new OlapParameterWizard();


var OlapChartWizard = OlapWizard.extend({

  wizardId: "OLAP_CHART_WIZARD",

  constructor: function() {
    this.base();
    this.logger = new Logger("OlapChartWizard");
  },

  renderMainOlapArea: function() {

    var content = $('\n' +
        '<div class="cdfdd-component-options-label">Name: <input id="cdfdd-olap-parameter-name" class="invalid" type="text" onchange="WizardManager.getWizardManager(\'' + this.getWizardId() + '\').processChange()" ></input></div>\n' +
        '<hr/>\n' +
        '<div class="cdfdd-component-options-label">Title: <input id="cdfdd-olap-parameter-title" type="text" onchange="WizardManager.getWizardManager(\'' + this.getWizardId() + '\').processChange()" id="cdfdd-olap-parameter-title" name="cdfdd-olap-parameter-title"></input></div>\n' +
        '<div class="cdfdd-component-options-label">Type:<select class="cdfdd-component-options-type" onchange="WizardManager.getWizardManager(\'' + this.getWizardId() + '\').processChange()" id="cdfdd-olap-parameter-type" name="cdfdd-olap-parameter-type"></select></div>\n' +
        '<div class="cdfdd-component-options-label">Html Object:<select class="cdfdd-component-options-htmlobject" onchange="WizardManager.getWizardManager(\'' + this.getWizardId() + '\').processChange()" id="cdfdd-olap-parameter-htmlobject" name="cdfdd-olap-parameter-htmlobject"></select></div>\n' +
        '<div class="cdfdd-component-options-label">Orientation:<select class="cdfdd-component-options-orientation" onchange="WizardManager.getWizardManager(\'' + this.getWizardId() + '\').processChange()" id="cdfdd-olap-parameter-orientation" name="cdfdd-olap-parameter-orientation"></select></div>\n' +
        '<div class="cdfdd-component-options-label">Top Count:<select class="cdfdd-component-options-topCount" onchange="WizardManager.getWizardManager(\'' + this.getWizardId() + '\').processChange()" id="cdfdd-olap-parameter-topcount" name="cdfdd-olap-parameter-topcount"></select></div>\n');

    var appendOption = function(obj, options) {
      for(v in options) {
        if(options.hasOwnProperty(v) && v != undefined && v != null) {
          obj.append('<option value="' + options[v][0] + '">' + options[v][1] + '</option>');
        }
      }
    };

    var componentOptions = $("#cdfdd-component-options");
    var typeSelector = $("#cdfdd-olap-parameter-type", content);
    appendOption(typeSelector, [
      ["BarChart", "Bar Chart"],
      ["PieChart", "Pie Chart"],
      ["LineChart", "Line Chart"],
      ["DotChart", "Dot Chart"]
    ]);
    var topCountSelector = $("#cdfdd-olap-parameter-topcount", content);
    appendOption(topCountSelector, [
      ["", ""],
      ["5", "5"],
      ["10", "10"],
      ["15", "15"]
    ]);
    var orientationSelector = $("#cdfdd-olap-parameter-orientation", content);
    appendOption(orientationSelector, [
      ["horizontal", "Horizontal"],
      ["vertical", "Vertical"]
    ]);
    componentOptions.html(content);

    var data = Panel.getPanel(LayoutPanel.MAIN_PANEL).getHtmlObjects();
    var htmlObject = $("#cdfdd-olap-parameter-htmlobject", content);
    $.each(data, function(i, val) {
      htmlObject.append('<option value="' + val.properties[0].value + '" : >' + val.properties[0].value + '</option>');
    });
  },

  getDroppableRows: function() {
    return "td.draggableDimension, td.draggableMeasure, td.draggableFilter";
  },

  getDroppableColumns: function() {
    return "td.draggableDimension, td.draggableMeasure, td.draggableFilter";
  },

  getDroppableFilters: function() {
    return "td.draggableDimension, td.draggableMeasure, td.draggableFilter";
  },

  processChange: function() {
    // Clean preview
    $("#cdfdd-olap-preview-area").empty();
    var preview = $("#cdfdd-olap-enable-preview:checked").length > 0;
    var catalog = this.getCatalog();
    var valid = this.selectedWizardObjects.rows.length > 0 && this.selectedWizardObjects.columns.length > 0 && catalog != undefined;
    ;

    $("#cdfdd-olap-preview").removeClass('disabled');//enable preview area, will be disabled if unchecked
    if(!preview) {
      $("#cdfdd-olap-preview").addClass('disabled');
    }

    if(valid & preview) {
      this.getSelectedOptions().name = $("#cdfdd-olap-parameter-name").val();
      this.getSelectedOptions().title = $("#cdfdd-olap-parameter-title").val();
      this.getSelectedOptions().type = $("#cdfdd-olap-parameter-type").val();
      this.getSelectedOptions().topCount = $("#cdfdd-olap-parameter-topcount").val();
      this.getSelectedOptions().orientation = $("#cdfdd-olap-parameter-orientation").val();
      this.getSelectedOptions().jndi = catalog.jndi;
      this.getSelectedOptions().schema = catalog.schema;
      this.getSelectedOptions().cube = this.getCube();
      var topCount = this.getSelectedOptions().topCount;
      topCount = (topCount.length > 0) ? topCount : undefined;

      this.getSelectedOptions().query = this.buildQuery(true, topCount);
      this.preview();
    }


    // Name
    var name = $("#cdfdd-olap-parameter-name").val();
    if(name.length > 0) {
      $("#cdfdd-olap-parameter-name").removeClass("invalid");
      valid = valid & true;
    } else {
      $("#cdfdd-olap-parameter-name").addClass("invalid");
      valid = false;
    }

    if(valid) {
      $("#cdfdd-wizard-button-ok").removeAttr("disabled");
    } else {

      $("#cdfdd-wizard-button-ok").attr("disabled", "disabled");
    }

  },

  getSeriesName: function() {
    var row = this.selectedWizardObjects.rows[0];
    if(row) {
      for(var i = 0; i < row.membersArray.length; i++) {
        if(row.member == row.membersArray[i].qualifiedName) {
          return row.membersArray[i].name;
        }
      }
    }
    return null;
  },

  preview: function() {
    this.logger.debug("Launching preview");

    // Build cdf component
    CDFDDPreviewComponentDefinition = {
      width: 440,
      height: 200,
      title: "Preview",
      titlePosition: "top",
      titleSize: 40,
      showDots: this.getSelectedOptions().type == "cccDotChart",
      showLines: this.getSelectedOptions().type == "cccLineChart",

      legend: false,
      maxBarSize: 100,

      innerGap: 0.9,
      explodedSliceIndex: 0,
      explodedSliceRadius: 0,
      orientation: this.getSelectedOptions().orientation,
      queryType: 'mdx',
      jndi: this.getSelectedOptions().jndi,
      catalog: this.getSelectedOptions().schema,
      title: this.getSelectedOptions().title,
      query: this.getSelectedOptions().query,
      crosstabMode: true,
      seriesInRows: false,
      animate: false,
      clickable: false,
      timeSeries: false,
      timeSeriesFormat: "%Y-%m-%d",
      stacked: false,
      panelSizeRatio: 0.8,
      barSizeRatio: 0.9,
      colors: [],
      showValues: false,
      valuesAnchor: "right",
      titlePosition: "top",
      titleSize: 25,
      legend: true,
      legendPosition: "bottom",
      legendAlign: "center",
      showXScale: true,
      xAxisPosition: "bottom",
      xAxisSize: 30,
      showYScale: true,
      yAxisPosition: "left",
      yAxisSize: 50,
      xAxisFullGrid: false,
      yAxisFullGrid: false,
      axisOffset: 0,
      originIsZero: true,
      secondAxis: false,
      secondAxisIndependentScale: true,
      secondAxisIdx: -1,
      secondAxisOriginIsZero: true,
      secondAxisColor: "blue",
      extensionPoints: []
    };

    CDFDDPreviewComponent = {
      name: "CDFDDPreviewComponent",
      type: this.getComponentType(),
      chartDefinition: CDFDDPreviewComponentDefinition,
      htmlObject: "cdfdd-olap-preview-area",
      executeAtStart: true,
      postFetch: function(values) {
        values.resultset.map(function(row) {//parse numeric values, otherwise sum will concatenate them
          row.splice(1, 1, parseFloat(row[1]));
        });

        return {
          resultset: values.resultset,
          metadata: [
            {colIndex: 0, colName: 'Name', colType: 'String'},
            {colIndex: 1, colName: 'Value', colType: 'Numeric'}
          ]
        };
      }
    };

    Dashboards.components = [];
    Dashboards.finishedInit = false;
    Dashboards.init([CDFDDPreviewComponent]);
  },


  buttonOk: function() {
    this.apply();
    $('#' + WizardManager.MAIN_DIALOG).jqmHide();
  },

  getComponentType: function() {
    return 'ccc' + this.getSelectedOptions().type;  //BarChart | LineChart | PieChart
  },

  apply: function() {

    // Generate parameter

    //var olapParameter = BaseModel.getModel('ComponentsParameter');

    // Populate main stub
    $('#' + WizardManager.MAIN_DIALOG).jqmHide();

    // 1. Add query to datasources
    this.addQueryToDatasources(this.getSeriesName());

    // 2. Add chart to components
    var type = this.getComponentType();

    var chartModel = BaseModel.getModel('Components' + type);
    var chartStub = chartModel.getStub();

    if(CDFDDUtils.getProperty(chartStub, "name")) {
      CDFDDUtils.getProperty(chartStub, "name").value = this.getSelectedOptions().name + "Chart";
    }
    if(CDFDDUtils.getProperty(chartStub, "title")) {
      CDFDDUtils.getProperty(chartStub, "title").value = this.getSelectedOptions().name; //ToDo: new form field
    }
    if(CDFDDUtils.getProperty(chartStub, "htmlObject")) {
      CDFDDUtils.getProperty(chartStub, "htmlObject").value = $("#cdfdd-olap-parameter-htmlobject").val();
    }
    if(CDFDDUtils.getProperty(chartStub, "dataSource")) {
      CDFDDUtils.getProperty(chartStub, "dataSource").value = this.getSelectedOptions().name + "Query";
    }
    if(CDFDDUtils.getProperty(chartStub, "height")) {
      CDFDDUtils.getProperty(chartStub, "height").value = "300";
    }
    if(CDFDDUtils.getProperty(chartStub, "width")) {
      CDFDDUtils.getProperty(chartStub, "width").value = "400";
    }
    if(CDFDDUtils.getProperty(chartStub, "crosstabMode")) {
      CDFDDUtils.getProperty(chartStub, "crosstabMode").value = true;
    }
    if(CDFDDUtils.getProperty(chartStub, "orientation")) {
      CDFDDUtils.getProperty(chartStub, "orientation").value = this.getSelectedOptions().orientation;
    }

    var listeners = this.getListenners();
    if(listeners.length > 0) {
      CDFDDUtils.getProperty(chartStub, "listeners").value = listeners;
    }

    var paramsProp = CDFDDUtils.getProperty(chartStub, "parameters") || // by alias
        CDFDDUtils.getProperty(chartStub, "xActionArrayParameter"); // by name
    paramsProp.value = this.getComponentParameters();

    var entryName = type.toUpperCase() + '_ENTRY';

    var componentsPalleteManager = PalleteManager.getPalleteManager(ComponentsPanel.PALLETE_ID);
    var chartEntry = componentsPalleteManager.getEntries()[entryName];

    var componentsTableManager = componentsPalleteManager.getLinkedTableManager();
    var insertAtIdx = componentsTableManager.createOrGetParent(chartEntry.getCategory(), chartEntry.getCategoryDesc());
    chartStub.parent = chartEntry.getCategory();
    componentsTableManager.insertAtIdx(chartStub, insertAtIdx);

  }

}, {
});
var wizard = new OlapChartWizard();


