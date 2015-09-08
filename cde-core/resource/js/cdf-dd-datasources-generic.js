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

var MdxEntryBase = PalleteEntry.extend({
  execute : function(palleteManager) {
    var myself = this;
    this._execute = this.base;

    var content = '' +
      '<div class="popup-input-container">' +
        '<span class="popup-label">Catalog</span>' +
        '<select id="cdfddOlapCatalogSelect" class="popup-select" onchange="MdxEntryBase.getEntry(\'MDX_ENTRY\').catalogSelected()"></select>\n' +
      '</div>' +
      '<div class="popup-input-container bottom">' +
        '<span class="popup-label">Cube</span>' +
        '<select id="cdfddOlapCubeSelect" class="popup-select" onchange="MdxEntryBase.getEntry(\'MDX_ENTRY\').cubeSelected()" ></select>' +
      '</div>';
    var _inner = '' +
          '<div class="popup-header-container">\n' +
          '  <div class="popup-title-container">OLAP MDX Query</div>\n' +
          '</div>\n' +
          '<div class="popup-body-container layout-popup">\n' +
          content +
          '</div>';
    $.prompt(_inner,{
      buttons: {
        Ok: true,
        Skip: false
      },
      top: "40px",
      prefix:"popup",
      loaded: function(){
        var $popup = $(this);
        var cdfddOlapCatalogSelect = $('#cdfddOlapCatalogSelect', $popup);
        var cdfddOlapCubeSelect = $('#cdfddOlapCubeSelect', $popup);

        CDFDDUtils.movePopupButtons($popup);
        CDFDDUtils.buildPopupSelect(cdfddOlapCatalogSelect, {minimumResultsForSearch: -1});
        CDFDDUtils.buildPopupSelect(cdfddOlapCubeSelect, {minimumResultsForSearch: -1});
        $.getJSON(OlapUtils.getOlapCubesUrl(), {}, function(json) {
          if(json.status == "true"){
            var catalogs = json.result.catalogs;
            myself.catalogs = catalogs;
            $.each(catalogs,function(i,catalog){
              $("select#cdfddOlapCatalogSelect").append("<option>"+catalog.name+"</option>");
            });
            myself.catalogSelected();
          }
        });
      },
      callback: function(v,m,f){
        var datasourceStub = myself.getStub();
        if(v && myself.catalog != undefined){
          CDFDDUtils.getProperty(datasourceStub,"jndi").value =  myself.catalog.jndi;
          CDFDDUtils.getProperty(datasourceStub,"catalog").value = myself.catalog.schema;
          CDFDDUtils.getProperty(datasourceStub,"cube").value = myself.cube;
        }
        myself._execute(palleteManager,datasourceStub);
      }
    });
  },

  catalogSelected: function(){
    var myself = this;
    var selectedCatalog = $("#cdfddOlapCatalogSelect").val();
    var catalog = null;

    $.each(this.catalogs,function(i,cat){
      if(cat.name == selectedCatalog){
        catalog = cat;
        return;
      };
    });

    this.catalog = catalog;
    if(catalog != null){
      var cubes = catalog.cubes;
      $("select#cdfddOlapCubeSelect").empty();
      $.each(cubes,function(i,cube){
        if(i==0)myself.cube  = cube.name;
        $("select#cdfddOlapCubeSelect").append("<option>"+cube.name+"</option>");
      });
    }
  },

  cubeSelected: function(){
    this.cube = $("#cdfddOlapCatalogSelect").val();
  }
},{
  getEntry: function(id){
    var entry = undefined;
    $.each(CDFDDDatasourcesArray,function(i, ent) {
      if(ent.id == id) {
        entry = ent;
        return;
      }
    });
    return entry;
  }
});

var QueryRenderer = TextAreaRenderer.extend({});

var NoContextPathRenderer = ResourceFileRenderer.extend({

  giveContext: function(isSystem, path) {
    return path;
  },

  getFileName: function(filename) {
    if(filename.charAt(0) !== '/') {
      filename = this.getAbsoluteFileName(filename);
    }

    return filename;
  }
});

var CdaPathRenderer = NoContextPathRenderer.extend({
  getFileExtensions: function() {
    return ".cda";
  },

  getResourceType: function() {
    return 'cda';
  }
});

var CdaEditorRenderer = StringRenderer.extend({

  constructor: function(tableManager){
    this.base(tableManager);
    this.logger = new Logger("CdaFileRenderer");
    this.logger.debug("Creating new CdaFileRenderer");
  },

  render: function(placeholder, value, callback){

    var _editArea = $("<td></td>");
    var path = value;
    var _prompt = $('<button class="cdfddInput" style="width:auto">Edit in CDA</button>').bind("click",function(){
      CdaEditorRenderer.popup(path);
    });
    _editArea.append(_prompt);
    _editArea.appendTo(placeholder);

  },
  validate: function(settings, original){
    return true;
  }

},{
  popup: function(path)  {

    $("#wizardDialog").empty();
    $("#wizardDialog").append("<iframe src='../cda/editFile?path="+path+"&initialState=exEdit' width='100%' height='95%' ></iframe>");
    $("#wizardDialog").append("<button onclick='$(\"#wizardDialog\").jqmHide()'class='cdfddInput' style='float:right;width: auto'>Close</button>");
    $("#wizardDialog").jqmShow();
  }
});

var CggPathRenderer = ResourceFileRenderer.extend({

  getFileExtensions: function(){
    return ".js";
  },

  getResourceType: function(){
    return 'javascript';
  },

  formatSelection: function(file){
    return file;
  }
});

var KtrPathRenderer = NoContextPathRenderer.extend({

  //disallow selecting a folder for new file creation
  createNew: false,

  getFileExtensions: function(){
    return ".ktr";
  }
});

var SaikuPathRenderer = ResourceFileRenderer.extend({

  //disallow selecting a folder for new file creation
  createNew: false,

  //omit edit button
  renderEditorButton: function(){ return '';},

  getFileExtensions: function(){
    return ".saiku";
  },

  formatSelection: function(file){
    return file;
  }
});

var AnalyzerPathRenderer = ResourceFileRenderer.extend({

  //disallow selecting a folder for new file creation
  createNew: false,

  //omit edit button
  renderEditorButton: function(){ return '';},

  getFileExtensions: function(){
    return ".xanalyzer";
  },

  formatSelection: function(file){
    return file;
  }
});
