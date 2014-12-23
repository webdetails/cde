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

var wd = (typeof wd !== 'undefined') ? wd : {};
wd.cde = wd.cde || {};
wd.cde.endpoints = {

  staticResUrl: "/api/repos",
  staticUrl: "/api/plugins/pentaho-cdf-dd/files/",
  pluginUrl: "/plugin/pentaho-cdf-dd/api/",
  cggPluginUrl: "/plugin/cgg/api/services/",
  cssResourceUrl: "resources/get?resource=",
  imageResourceUrl: "resources/get?resource=",
  jsResourceUrl: "resources/getJs?resource=",
  saikuUiPluginUrl: "/content/saiku-ui/index.html?biplugin5=true",

  //The webAppPath is defined at the start of Dashboards.js
  getWebappBasePath: function() {
    return webAppPath;
  },

  getStaticUrl: function() {
    return this.getWebappBasePath() + this.staticUrl;
  },

  getStaticResUrl: function() {
    return this.getWebappBasePath() + this.staticResUrl;
  },

  getPluginUrl: function() {
    return this.getWebappBasePath() + this.pluginUrl;
  },

  getUnbasedCggPluginUrl: function() {
    return this.cggPluginUrl;
  },

  getCggPluginUrl: function() {
    return this.getWebappBasePath() + this.cggPluginUrl;
  },

  getCssResourceUrl: function() {
    return this.getWebappBasePath() + this.pluginUrl + this.cssResourceUrl;
  },

  getImageResourceUrl: function() {
    return this.getWebappBasePath() + this.pluginUrl + this.imageResourceUrl;
  },

  getJsResourceUrl: function() {
    return this.getWebappBasePath() + this.pluginUrl + this.jsResourceUrl;
  },

  isEmptyFilePath: function(filePath) {
    return (!filePath || "/" == filePath);
  },

  getFilePathFromUrl: function() {
    // ex: /pentaho/api/repos/:public:plugin-samples:pentaho-cdf-dd:cde_sample1.wcdf/wcdf.edit

    var dash = ""; // empty file path that represents a new dash

    if(window.location.pathname.indexOf("/:") == -1) {
      return dash;
    } else {
      var regExp = window.location.pathname.match("(/:)(.*)(/)");

      if(regExp[2]) {
        dash = "/" + regExp[2].replace(new RegExp(":", "g"), "/");
      }

    }
    return dash;
  },

  getSaikuUiPluginUrl: function() {
    return this.getWebappBasePath() + this.saikuUiPluginUrl;
  }
};

var SynchronizeRequests = {

  selectTemplate: undefined,

  doPost: function(templateParams) {

    $.ajax({
      type: 'POST',
      data: templateParams,
      dataType: 'json',
      url: wd.cde.endpoints.getPluginUrl() + "syncronizer/syncronizeTemplates",
      success: function(result) {
        if(result && result.status == "true") {
          $.notifyBar({
            jqObject: NotifyBarUtils.getNotifyBarObject(),
            html: "Template saved successfully", delay: 100 });
        } else {
          $.notifyBar({
            jqObject: NotifyBarUtils.getNotifyBarObject(),
            html: "Errors saving template: " + result });
        }
      }
    });
  },

  doGetJson: function(loadParams) {

    $.ajax({
      type: 'POST',
      data: loadParams,
      dataType: 'json',
      url: wd.cde.endpoints.getPluginUrl() + "syncronizer/syncronizeTemplates",
      success: function(result) {

        if(result && result.status == "true") {
          templates = result.result;
          SynchronizeRequests.selectTemplate = undefined;
          var myTemplatesCount = 0;
          var _templates = '<h2 style="padding:10px; line-height: 20px;">Apply Template</h2><hr><div class="templates"><a class="prev disabled"></a><div class="scrollable"><div id="thumbs" class="thumbs">';
          var _myTemplates = '<h2 style="padding:10px; line-height: 20px;">Apply Custom Template</h2><hr><div class="templates"><a class="prev disabled"></a><div class="scrollable"><div id="thumbs" class="thumbs">';
          for(var v in templates) {
            if(templates.hasOwnProperty(v)) {
              if(templates[v].type == "default") {
                _templates += '<div><img id="' + v + '" src="' + templates[v].img + '"/><p>' + templates[v].structure.layout.title + '</p></div>';
              } else if(templates[v].type == "custom") {
                _myTemplates += '<div><img id="' + v + '" src="' + templates[v].img + '"/><p>' + templates[v].structure.layout.title + '</p></div>';
                myTemplatesCount++;
              }
            }
          }
          _templates += '</div></div><a class="next"></a></div>';
          _myTemplates += '</div></div><a class="next"></a></div>';
          var loaded = function() {
            SynchronizeRequests.selectTemplate = undefined;
            $("div.scrollable").scrollable({size: 3, items: '#thumbs', hoverClass: 'hover'});
            $(function() {
              $("div.scrollable:eq(0) div.thumbs div").bind('click', function() {
                SynchronizeRequests.selectTemplate = templates[$(this).find("img").attr("id")];
              });
            });
          };

          var callback = function(v, m, f) {
            var selectTemplate = SynchronizeRequests.selectTemplate;
            if(v == 1 && selectTemplate != undefined) {
              var overwriteComponents = selectTemplate.structure.components.rows.length != 0;
              var overwriteDatasources = selectTemplate.structure.datasources.rows.length != 0;
              var promptPrefix = 'popupTemplate';
              var message = Dashboards.i18nSupport.prop('SynchronizeRequests.CONFIRMATION_LOAD_TEMPLATE') + '<br><br>';

              if(overwriteComponents && overwriteDatasources) {
                message += Dashboards.i18nSupport.prop('SynchronizeRequests.OVERWRITE_LAYOUT_COMP_DS');
              } else if(overwriteComponents) {
                message += Dashboards.i18nSupport.prop('SynchronizeRequests.OVERWRITE_LAYOUT_COMP');
              } else if(overwriteDatasources) {
                message += Dashboards.i18nSupport.prop('SynchronizeRequests.OVERWRITE_LAYOUT_DS');
              } else {
                message += Dashboards.i18nSupport.prop('SynchronizeRequests.OVERWRITE_LAYOUT');
              }

              $.prompt(message, { buttons: { Ok: true, Cancel: false}, prefix: promptPrefix,
                callback: SynchronizeRequests.callbackLoadTemplate
              });

              $('#' + promptPrefix).css('height', '165px');
            }
          };

          var promptTemplates = {
            loaded: loaded,
            buttons: myTemplatesCount > 0 ? { MyTemplates: 2, Ok: 1, Cancel: 0 } : {Ok: 1, Cancel: 0},
            opacity: 0.2,
            prefix: 'popupTemplate',
            callback: callback,
            submit: function(v, m, f) {
              if(v != 2) return true;
              $.prompt.close();
              $.prompt(_myTemplates, promptMyTemplates, {prefix: "popupTemplate"});
            }
          };

          var promptMyTemplates = {
            loaded: loaded,
            buttons: { Back: 2, Ok: 1, Cancel: 0 },
            opacity: 0.2,
            prefix: 'popupTemplate',
            callback: callback,
            submit: function(v, m, f) {
              if(v != 2) return true;
              $.prompt.close();
              $.prompt(_templates, promptTemplates, {prefix: "popupTemplate"});
            }
          };

          $.prompt(_templates, promptTemplates, {prefix: "popupTemplate"});
        } else {
          $.notifyBar({
            jqObject: NotifyBarUtils.getNotifyBarObject(),
            html: "Error loading templates: " + json.result
          });
        }
      }
    });
  },

  //exposed callback function for ease of testing
  callbackLoadTemplate: function(v, m, f) {
    if(v) {
      if(!SynchronizeRequests.selectTemplate.structure.components.rows.length) {
        SynchronizeRequests.selectTemplate.structure.components.rows = cdfdd.dashboardData.components.rows;
      }

      if(!SynchronizeRequests.selectTemplate.structure.datasources.rows.length) {
        SynchronizeRequests.selectTemplate.structure.datasources.rows = cdfdd.dashboardData.datasources.rows;
      }

      cdfdd.dashboardData = SynchronizeRequests.selectTemplate.structure;
      cdfdd.layout.init();
      cdfdd.components.initTables();
      cdfdd.datasources.initTables();
      if(SynchronizeRequests.selectTemplate.structure.style) {
        cdfdd.dashboardWcdf.style = SynchronizeRequests.selectTemplate.structure.style;
      }
      if(SynchronizeRequests.selectTemplate.structure.rendererType) {
        cdfdd.dashboardWcdf.rendererType = SynchronizeRequests.selectTemplate.structure.rendererType;
      }
    }
  },

  createFile: function(params) {
    $.ajax({
      url: ExternalEditor.getWriteUrl(),
      type: 'PUT',
      data: params,
      success: function(result) {
        if(result.indexOf('saved ok') < 0) {
          alert(result);
        }
      }
    });
  }
};


var OlapWizardRequests = {
  olapObject: function(params, container, myself, direction) {

    $.getJSON(wd.cde.endpoints.getPluginUrl() + "olap/getLevelMembersStructure", params, function(json) {
      if(json.status == "true") {
        myself.membersArray = json.result.members;

        if(myself.membersArray.length == 0) {
          myself.membersArray = Util.clone(myself.initialMembersArray);
          myself.member = myself.initialMember;
        } else {
          myself.member = myself.membersArray[0].qualifiedName;
        }

        if(direction == 'down') {
          myself.memberDepth++;
        } else {
          myself.memberDepth--;
        }

        myself.render(container);
        myself.processChange();
      }
    });
  },

  olapManager: function(params, myself) {

    $.getJSON(wd.cde.endpoints.getPluginUrl() + "olap/getCubes", params, function(json) {
      if(json && json.status == "true") {

        var catalogs = json.result.catalogs;
        myself.setCatalogs(catalogs);

        myself.logger.info("Got correct response from getCubes: " + catalogs);

        var _selector = $("#cdfddOlapCubeSelector");
        _selector.append(
                '<select id="cdfddOlapCatalogSelect" class="small" onchange="WizardManager.getWizardManager(\'' + myself.wizardId + '\').catalogSelected()"><option value="-"> Select catalog </option></select><br/>');
        _selector.append(
                '<select id="cdfddOlapCubeSelect" class="small" onchange="WizardManager.getWizardManager(\'' + myself.wizardId + '\').cubeSelected()" ><option value="-"> Select cube </option></select>');

        $.each(catalogs, function(i, catalog) {
          $("select#cdfddOlapCatalogSelect", _selector).append("<option>" + catalog.name + "</option>");
        });

      } else {
        alert(json.result);
      }
    });
  },

  olapCubeSelected: function(params, selectedCube, selectedCatalog, myself) {

    $.getJSON(wd.cde.endpoints.getPluginUrl() + "olap/getCubeStructure", params, function(json) {
      if(json.status == "true") {

        myself.logger.info("Got correct response from GetCubeStructure");

        var dimensions = json.result.dimensions;

        var dimensionIdx = 0;
        var dimensionTBody = $("#cdfddOlapDimensionSelector > tbody");
        dimensionTBody.empty();
        $.each(dimensions, function(i, dimension) {
          var hierarchies = dimension.hierarchies;
          $.each(hierarchies, function(j, hierarchy) {
            var hierarchyId = "dimRow-" + (++dimensionIdx);
            var name = hierarchy.caption == undefined ? hierarchy.name : hierarchy.caption;
            dimensionTBody.append("<tr id='" + hierarchyId + "'><td>" + name + "</td></tr>");

            var levels = hierarchy.levels;
            $.each(levels, function(k, level) {
              var levelId = "dimRow-" + (++dimensionIdx);
              var name = level.caption == undefined ? level.name : level.caption;
              dimensionTBody.append("<tr id='" + levelId + "' class='olapObject child-of-" + hierarchyId + "'><td class='draggableDimension'>" + name + "</td></tr>");
              level.hierarchy = hierarchy;
              level.catalog = selectedCatalog;
              level.cube = selectedCube;
              myself.addOlapObject(WizardOlapObjectManager.DIMENSION, level);
            });
          });
        });
        dimensionTBody.parent().treeTable();
        $("td.draggableDimension", dimensionTBody).draggable({helper: 'clone'});

        // Measures
        var measures = json.result.measures;

        var measureIdx = 0;
        var measureTBody = $("#cdfddOlapMeasureSelector > tbody");
        measureTBody.empty();
        $.each(measures, function(i, measure) {
          var measureId = "levelRow-" + (++measureIdx);
          var name = measure.caption == undefined ? measure.name : measure.caption;
          measureTBody.append("<tr id='" + measureId + "' class='olapObject'><td class='draggableMeasure'>" + name + "</td></tr>");
          myself.addOlapObject(WizardOlapObjectManager.MEASURE, measure);

        });
        measureTBody.parent().treeTable();
        $("td.draggableMeasure", measureTBody).draggable({helper: 'clone', type: "Measure"});

        myself.getAvailableFilters();
      } else {
        alert(json.result);
      }
    });
  }
};


var StylesRequests = {

  syncStyles: function(myself) {

    $.getJSON(wd.cde.endpoints.getPluginUrl() + "syncronizer/syncronizeStyles", {
    }, function(json) {
      myself.styles = json.result;
    });
  },

  listStyleRenderers: function(myself) {

    $.getJSON(wd.cde.endpoints.getPluginUrl() + "renderer/listRenderers", {
    }, function(json) {
      myself.renderers = json.result;

    });
  },

  initStyles: function(saveSettingsParams, wcdf, myself, callback) {

    $.post(wd.cde.endpoints.getPluginUrl() + "syncronizer/syncronizeDashboard", saveSettingsParams, function(result) {
      if(result && result.status == "true") {
        myself.setDashboardWcdf(wcdf);
        callback();
      } else {
        $.notifyBar({
          jqObject: NotifyBarUtils.getNotifyBarObject(),
          html: "Errors initializing settings: " + result.result
        });
      }
    });
  }
};


var SaveRequests = {

  saveRequestParams: {
    selectedFolder: null,
    selectedFile: null,
    myself: null
  },


  saveSettings: function(saveSettingsParams, cdfdd, wcdf, myself) {

    $.post(wd.cde.endpoints.getPluginUrl() + "syncronizer/syncronizeDashboard", saveSettingsParams, function(result) {
      try {
        if(result && result.status == "true") {
          myself.setDashboardWcdf(wcdf);
          // We need to reload the layout engine in case the rendererType changed
          cdfdd.layout.init();
          $.notifyBar({
            jqObject: NotifyBarUtils.getNotifyBarObject(),
            html: "Dashboard Settings saved successfully",
            delay: 1000
          });
        } else {
          throw result.result;
        }
      } catch (e) {
        $.notifyBar({
          jqObject: NotifyBarUtils.getNotifyBarObject(),
          html: "Errors saving settings: " + e
        });
      }
    });
  },

  saveDashboard: function(saveParams, stripArgs) {

    if(wd.cde.endpoints.isEmptyFilePath(saveParams.file)) {
      saveParams.file = wd.cde.endpoints.getFilePathFromUrl();
    }
    var successFunction = function(result) {

      if(result && result.status == "true") {
        if(stripArgs.needsReload) {
          window.location.reload();
        } else {
          $.notifyBar({
            jqObject: NotifyBarUtils.getNotifyBarObject(),
            html: "Dashboard saved successfully",
            delay: 1000
          });
        }
      } else {
        $.notifyBar({
          jqObject: NotifyBarUtils.getNotifyBarObject(),
          html: "Errors saving file: " + result.result
        });
      }
    };

    // CDF-271 $.browser is depricated
    var rv;
    var re = new RegExp("MSIE ([0-9]{1,}[\.0-9]{0,})");
    if(re.exec(navigator.userAgent) != null) {
      rv = parseFloat(RegExp.$1);
    }

    if(rv && rv < 10) {
      console.log("Dashboard can't be saved using multipart/form-data, it will not save large Dashboards");
      $.post(wd.cde.endpoints.getPluginUrl() + "syncronizer/syncronizeDashboard", saveParams, successFunction);
    } else {
      var $uploadForm = $('<form action="' + wd.cde.endpoints.getPluginUrl() + 'syncronizer/saveDashboard" method="post" enctype="multipart/form-data">');
      $uploadForm.ajaxForm({
        data: saveParams,
        success: successFunction
      });
      $uploadForm.submit();
    }

  },

  saveAsDashboard: function(saveAsParams, selectedFolder, selectedFile, myself) {

    var successFunction = function(result) {
      if(result && result.status == "true") {
        if(selectedFolder[0] == "/") {
          selectedFolder = selectedFolder.substring(1, selectedFolder.length);
        }
        var solutionPath = selectedFolder.split("/");
        myself.initStyles(function() {
          window.location = window.location.protocol + "//" + window.location.host + wd.cde.endpoints.getWebappBasePath() + '/api/repos/:' + selectedFolder.replace(new RegExp("/", "g"), ":") + selectedFile + '/edit';
        });
      } else {
        $.notifyBar({
          jqObject: NotifyBarUtils.getNotifyBarObject(),
          html: "Errors saving file: " + result.result
        });
      }
    };

    // CDF-271 $.browser is depricated
    var rv;
    var re = new RegExp("MSIE ([0-9]{1,}[\.0-9]{0,})");
    if(re.exec(navigator.userAgent) != null) {
      rv = parseFloat(RegExp.$1);
    }

    if(rv && rv < 10) {
      console.log("Dashboard can't be saved using multipart/form-data, it will not save large Dashboards");
      $.post(wd.cde.endpoints.getPluginUrl() + "syncronizer/syncronizeDashboard", saveAsParams, successFunction);
    } else {
      var $uploadForm = $('<form action="' + wd.cde.endpoints.getPluginUrl() + 'syncronizer/saveDashboard" method="post" enctype="multipart/form-data">');
      $uploadForm.ajaxForm({
        data: saveAsParams,
        success: successFunction
      });
      $uploadForm.submit();
    }
  },

  saveAsWidget: function(saveAsParams, selectedFolder, selectedFile, myself) {

    $.extend(SaveRequests.saveRequestParams, {
      selectedFolder: selectedFolder,
      selectedFile: selectedFile,
      myself: myself
    });

    // CDF-271 $.browser is depricated
    var rv;
    var re = new RegExp("MSIE ([0-9]{1,}[\.0-9]{0,})");
    if(re.exec(navigator.userAgent) != null) {
      rv = parseFloat(RegExp.$1);
    }

    if(rv && rv < 10) {
      console.log("Dashboard can't be saved using multipart/form-data, it will not save large Dashboards");
      $.post(wd.cde.endpoints.getPluginUrl() + "syncronizer/syncronizeDashboard", saveAsParams, this.saveAsWidgetCallback);
    } else {
      var $uploadForm = $('<form action="' + wd.cde.endpoints.getPluginUrl() + 'syncronizer/saveDashboard" method="post" enctype="multipart/form-data">');
      $uploadForm.ajaxForm({
        data: saveAsParams,
        success: this.saveAsWidgetCallback
      });
      $uploadForm.submit();
    }

  },

  saveAsWidgetCallback: function(result) {
    if(result && result.status == "true") {

      var selectedFolder = SaveRequests.saveRequestParams.selectedFolder;
      var selectedFile = SaveRequests.saveRequestParams.selectedFile;
      var myself = SaveRequests.saveRequestParams.myself;

      if(selectedFolder[0] == "/") {
        selectedFolder = selectedFolder.substring(1, selectedFolder.length);
      }

      var updateParams = { widget: true };
      // TODO: dashboard is being saved twice. This also needs to be fixed..
      var wcdf = myself.getDashboardWcdf();
      var cleanStyle = myself.styles.indexOf('Clean');
      if(!wcdf.style) {
        updateParams.style = myself.styles[cleanStyle >= 0 ? cleanStyle : 0];
      }

      myself.saveSettingsRequest(updateParams);

      //redirect to new widget
      SaveRequests.redirect(selectedFolder, selectedFile);

    } else {
      $.notifyBar({
        jqObject: NotifyBarUtils.getNotifyBarObject(),
        html: "Errors saving file: " + result.result
      });
    }
  },

  redirect: function(selectedFolder, selectedFile) {
    window.location = window.location.protocol + "//" + window.location.host +
        wd.cde.endpoints.getWebappBasePath() + '/api/repos/:' + selectedFolder.replace(new RegExp("/", "g"), ":") + selectedFile + '/edit';
  }

};

var LoadRequests = {

  loadDashboard: function(loadParams, myself) {

    if(wd.cde.endpoints.isEmptyFilePath(loadParams.file)) {
      loadParams.file = wd.cde.endpoints.getFilePathFromUrl();
    }

    $.post(wd.cde.endpoints.getPluginUrl() + "syncronizer/syncronizeDashboard", loadParams, function(result) {
      if(result && result.status == "true") {
        myself.setDashboardData(myself.unstrip(result.result.data));
        myself.setDashboardWcdf(result.result.wcdf);
        myself.init();
      } else {
        alert(result && result.result);
      }
    });
  }
};

var PreviewRequests = {

  previewDashboard: function(saveParams, _href) {
    var successFunction = function(result) {
      if(result && result.status == "true") {
        $.fancybox({
          type: "iframe",
          closeBtn: true,
          autoSize: false,
          href: _href,
          width: $(window).width(),
          height: $(window).height()
        });
      } else {
        $.notifyBar({
          jqObject: NotifyBarUtils.getNotifyBarObject(),
          html: "Errors saving file: " + result.result
        });
      }
    };

    // CDF-271 $.browser is depricated
    var rv;
    var re = new RegExp("MSIE ([0-9]{1,}[\.0-9]{0,})");
    if(re.exec(navigator.userAgent) != null) {
      rv = parseFloat(RegExp.$1);
    }

    if(rv && rv < 10) {
      console.log("Dashboard can't be saved using multipart/form-data, it will not save large Dashboards");
      $.post(wd.cde.endpoints.getPluginUrl() + "syncronizer/syncronizeDashboard", saveParams, successFunction);
    } else {
      var $uploadForm = $('<form action="' + wd.cde.endpoints.getPluginUrl() + 'syncronizer/saveDashboard" method="post" enctype="multipart/form-data">');
      $uploadForm.ajaxForm({
        data: saveParams,
        success: successFunction
      });
      $uploadForm.submit();
    }
  },

  getPreviewUrl: function(solution, path, file) {
    var _href = wd.cde.endpoints.getPluginUrl() + "renderer/render?";
    _href = _href + "solution=" + solution + "&path=" + path + "&file=" + file + "&bypassCache=true&root=" + window.location.host;

    return _href;
  }
};

var SolutionTreeRequests = {

  getExplorerFolderEndpoint: function(url) {
    return wd.cde.endpoints.getPluginUrl() + "resources/explore";
  }
};


var PluginRequests = {

  getCDEPlugins: function(onSuccess) {
    $.ajax({
      url: wd.cde.endpoints.getPluginUrl() + "plugins/get",
      type: 'GET',
      dataType: 'json',
      data: [],
      success: onSuccess
    });
  }
};

var VersionRequests = {
  getGetVersion: function() {
    return wd.cde.endpoints.getPluginUrl() + "version/get";
  },

  getCheckVersion: function() {
    return wd.cde.endpoints.getPluginUrl() + "version/check";
  }
};

var ExternalEditor = {
  getEditorUrl: function() {
    return wd.cde.endpoints.getPluginUrl() + "editor/getExternalEditor";
  },

  getGetUrl: function() {
    return wd.cde.endpoints.getPluginUrl() + "editor/file/get";
  },

  getCanEditUrl: function() {
    return wd.cde.endpoints.getPluginUrl() + "editor/file/canEdit";
  },

  getWriteUrl: function() {
    return wd.cde.endpoints.getPluginUrl() + "editor/file/write";
  }
};


var OlapUtils = {
  getOlapCubesUrl: function() {
    return wd.cde.endpoints.getPluginUrl() + "olap/getCubes";
  }
};


var Cgg = {
  getCggDrawUrl: function() {
    return window.location.href.substring(0, window.location.href.indexOf("api") - 1) + wd.cde.endpoints.getUnbasedCggPluginUrl() + "draw";
  }
};

var NotifyBarUtils = {
  getNotifyBarObject: function() {
    return $("#notifyBar").length ? $("#notifyBar") : undefined;
  }
};
