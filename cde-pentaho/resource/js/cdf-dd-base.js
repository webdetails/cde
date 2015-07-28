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

var wd = (typeof wd !== 'undefined') ? wd : {};
wd.cde = wd.cde || {};
wd.cde.endpoints = {

  staticUrl: "/content/pentaho-cdf-dd/",
  pluginUrl: "/content/pentaho-cdf-dd/",
  cggPluginUrl: "/content/cgg/",
  cssResourceUrl: "getCssResource?resource=",
  imageResourceUrl: "getResource?resource=",
  jsResourceUrl: "getJsResource?resource=",
  saikuUiPluginUrl: "/content/saiku-ui/index.html?biplugin=true",
  newDashboardUrl: "NewDashboard",

  //The webAppPath is defined at the start of Dashboards.js
  getWebappBasePath: function() {
    return webAppPath;
  },

  getStaticUrl: function() {
    return this.getWebappBasePath() + this.staticUrl;
  },

  getPluginUrl: function() {
    return this.getWebappBasePath() + this.pluginUrl;
  },

  getCggPluginUrl: function() {
    return this.getWebappBasePath() + this.cggPluginUrl;
  },

  getUnbasedCggPluginUrl: function() {
    return this.cggPluginUrl;
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
    return (!filePath || "/null/null/null" == filePath || encodeURIComponent("/null/null/null") == filePath );
  },

  getSaikuUiPluginUrl: function() {
    return this.getWebappBasePath() + this.saikuUiPluginUrl;
  },

  getNewDashboardUrl: function() {
    return this.getPluginUrl() + this.newDashboardUrl;
  }
};


var SynchronizeRequests = {

  doPost: function(templateParams) {

    $.post(wd.cde.endpoints.getPluginUrl() + "SyncTemplates", templateParams, function(result) {
      try {
        var json = Util.parseJsonResult(result);
        if(json.status === "true") {

          NotifyBarUtils.successNotifyBar("Template saved successfully");
        } else {
          throw json.result;
        }
      } catch(e) {
        NotifyBarUtils.errorNotifyBar("Errors saving template", e);
      }
    });
  },

  doGetJson: function(loadParams) {
    var myself = this;

    $.getJSON(wd.cde.endpoints.getPluginUrl() + "SyncTemplates", loadParams, function(json) {
      try {
        if(json.status) {

          var templates = json.result;
          var selectTemplate = undefined;

          var templatesCount = 0;
          var myTemplatesCount = 0;
          var _templates = '<div class="template-scroll">';
          var _myTemplates = '<div class="template-scroll">';

          for(var v in templates) {
            if(templates.hasOwnProperty(v)) {
              var template = templates[v];
              var title = templates[v].structure.layout.title;
              var imgSource = templates[v].img;
              var extraClass;

              if(template.type == "default") {
                extraClass = (++templatesCount)%3 === 0 ? 'last': '';
                _templates += '<div class="popup-template-container ' + extraClass + '">\n' +
                '  <span class="popup-label">' + title + '</span>\n' +
                '  <div class="popup-template">\n' +
                '    <img id="' + v + '" src="' + imgSource + '"/>\n' +
                '  </div>\n' +
                '</div>\n';
              } else if(template.type == "custom") {
                extraClass = (++myTemplatesCount)%3 === 0 ? 'last': '';
                _myTemplates += '<div class="popup-template-container ' + extraClass + '">' +
                '  <span class="popup-label">' + title + '</span>' +
                '  <div class="popup-template">' +
                '    <img id="' + v + '" src="' + imgSource + '"/>' +
                '  </div>' +
                '</div>';
              }
            }
          }

          var _templatesWrapper = '' +
              '<div class="popup-header-container">\n' +
              '  <div class="popup-title-container">Apply Template</div>\n' +
              '</div>\n' +
              '<div class="popup-body-container template-popup-container">\n' + _templates + '</div></div>';

          var _myTemplatesWrapper = '' +
              '<div class="popup-header-container">\n' +
              '  <div class="popup-title-container">Apply Custom Template</div>\n' +
              '</div>\n' +
              '<div class="popup-body-container template-popup-container">\n' + _myTemplates + '</div></div>';

          var loaded = function() {
            var $popup = $(this);
            $popup.addClass('template-popup');
            CDFDDUtils.movePopupButtons($popup);

            $('div.popup-template').click(function() {
              var container = $(this);
              $('div.popup-template').removeClass('template-selected');
              container.addClass('template-selected');
              selectTemplate = templates[container.find("img").attr("id")];
            });

            $('div.popup-template-container').slice(-3).addClass('bottom'); //last 3 elements
          };



          var callback = function(v, m, f) {
            if(v == 1 && selectTemplate != undefined) {
              var overwriteComponents = selectTemplate.structure.components.rows.length != 0;
              var overwriteDatasources = selectTemplate.structure.datasources.rows.length != 0;
              var promptPrefix = 'popup';
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

              message = '' +
              '<div class="popup-header-container">\n' +
              '  <div class="popup-title-container">Load Template</div>\n' +
              '</div>\n' +
              '<div class="popup-body-container layout-popup">\n' + message + '</div>';

              $.prompt(message, {
                buttons: {Ok: true, Cancel: false}, prefix: promptPrefix,
                loaded: function() {
                  var $popup = $(this);
                  $popup.addClass('settings-popup');
                  CDFDDUtils.movePopupButtons($popup);
                },
                callback: function(v, m, f) {
                  if(v) {
                    if(!selectTemplate.structure.components.rows.length) {
                      selectTemplate.structure.components.rows = cdfdd.dashboardData.components.rows;
                    }

                    if(!selectTemplate.structure.datasources.rows.length) {
                      selectTemplate.structure.datasources.rows = cdfdd.dashboardData.datasources.rows;
                    }

                    cdfdd.dashboardData = selectTemplate.structure;
                    cdfdd.layout.init();
                    cdfdd.components.initTables();
                    cdfdd.datasources.initTables();
                  }
                }
              });
            }
          };

          var promptTemplates = {
            loaded: loaded,
            buttons: myTemplatesCount > 0 ? {MyTemplates: 2, Ok: 1, Cancel: 0} : {Ok: 1, Cancel: 0},
            top: "40px",
            prefix: 'popup',
            callback: callback,
            submit: function(v, m, f) {
              if(v != 2) return true;
              $.prompt.close();
              $.prompt(_myTemplatesWrapper, promptMyTemplates, {prefix: "popup"});
            }
          };

          var promptMyTemplates = {
            loaded: loaded,
            buttons: {Back: 2, Ok: 1, Cancel: 0},
            top: "40px",
            prefix: 'popup',
            callback: callback,
            submit: function(v, m, f) {
              if(v != 2) return true;
              $.prompt.close();
              $.prompt(_templatesWrapper, promptTemplates, {prefix: "popup"});
            }
          };

          $.prompt(_templatesWrapper, promptTemplates, {prefix: "popup"});
        } else {
          throw json.result;
        }
      } catch(e) {
        NotifyBarUtils.errorNotifyBar("Error loading templates", e);
      }
    });
  },

  createFile: function(params) {
    $.post(ExternalEditor.getWriteUrl(), params,
        function(result) {
          if(result.indexOf('saved ok') < 0) {
            alert(result);
          }
        }
    );
  }

};


var OlapWizardRequests = {

  olapObject: function(params, container, myself, direction) {
    $.getJSON(wd.cde.endpoints.getPluginUrl() + "OlapUtils?operation=GetLevelMembersStructure", params, function(json) {
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

    $.getJSON(wd.cde.endpoints.getPluginUrl() + "OlapUtils?operation=GetOlapCubes", params, function(json) {
      if(json && json.status == "true") {

        var catalogs = json.result.catalogs;
        myself.setCatalogs(catalogs);

        myself.logger.info("Got correct response from getCubes: " + catalogs);

        var _catalogWrapper = $("#cdfddOlapCatalogSelector");
        var _cubeWrapper = $("#cdfddOlapCubeSelector");
        _catalogWrapper.append('' +
        '<span class="popup-label">Catalog</span>\n' +
        '<select id="cdfddOlapCatalogSelect" class="popup-select" onchange="WizardManager.getWizardManager(\'' + myself.wizardId + '\').catalogSelected()"></select>');
        _cubeWrapper.append('' +
        '<span class="popup-label">Cube</span>\n' +
        '<select id="cdfddOlapCubeSelect" class="popup-select" onchange="WizardManager.getWizardManager(\'' + myself.wizardId + '\').cubeSelected()" disabled></select>');

        var catalogSelect =  $("select#cdfddOlapCatalogSelect", _catalogWrapper);
        var cubeSelect = $("select#cdfddOlapCubeSelect", _cubeWrapper);

        CDFDDUtils.buildPopupSelect(catalogSelect, {});
        CDFDDUtils.buildPopupSelect(cubeSelect, {});

        $.each(catalogs, function(i, catalog) {
          $("select#cdfddOlapCatalogSelect", _catalogWrapper).append("<option>" + catalog.name + "</option>");
        });

      } else {
        alert(json.result);
      }
    });
  },

  olapCubeSelected: function(params, selectedCube, selectedCatalog, myself) {

    $.getJSON(wd.cde.endpoints.getPluginUrl() + "OlapUtils?operation=GetCubeStructure", params, function(json) {
      if(json.status == "true") {

        myself.logger.info("Got correct response from GetCubeStructure");

        /* Dimensions */
        var dimensions = json.result.dimensions;

        var dimensionIdx = 0;
        var dimensionSelect = $('#cdfddOlapDimensionSelector');
        var dimensionHolder = $('<div id="prompt-dimensions-accordion" class="prompt-wizard-accordion"></div>');
        $('#prompt-dimensions-accordion').remove();
        dimensionSelect
            .append(dimensionHolder);

        $.each(dimensions, function(i, dimension) {
          var hierarchies = dimension.hierarchies;
          $.each(hierarchies, function(j, hierarchy) {
            var name = hierarchy.caption == undefined ? hierarchy.name : hierarchy.caption;
            var hierarchyId = "dimRow-" + name;//+ (++dimensionIdx);


            dimensionHolder.append('' +
            '<div>\n' +
            '  <h3 class="prompt-wizard-elements">' + name + '</h3>' +
            '  <div id="' + hierarchyId + '"><ul></ul></div>\n' +
            '</div>');

            var levels = hierarchy.levels;
            var $hierarchyHolder = $('#' + hierarchyId + ' ul');
            $.each(levels, function(k, level) {
              var levelId = "Dimension-" + (dimensionIdx++);
              var levelName = level.caption == undefined ? level.name : level.caption;

              $hierarchyHolder.append('<li id="' + levelId + '" class="draggableDimension olapObject prompt-wizard-elements">' + levelName + '</li>');

              level.hierarchy = hierarchy;
              level.catalog = selectedCatalog;
              level.cube = selectedCube;
              myself.addOlapObject(WizardOlapObjectManager.DIMENSION, level);
            });
          });
        });

        dimensionHolder.accordion({
          header: 'h3',
          active: false,
          heightStyle: "content",
          collapsible: true
        });

        $(".draggableDimension", dimensionHolder).draggable({helper: 'clone'});

        $('.prompt-wizard-caption', dimensionSelect)
            .removeClass('disabled')
            .off()
            .click(function() {
              $(this).parent().toggleClass('collapsed');
            });


        /* Measures */
        var measures = json.result.measures;

        var measureIdx = 0;
        var measureSelect = $('#cdfddOlapMeasureSelector');
        var measuresHolder = $('<div id="prompt-measures-accordion" class="prompt-wizard-accordion"><div><ul></ul></div></div>');

        $('#prompt-measures-accordion').remove();
        measureSelect
            .append(measuresHolder);

        $.each(measures, function(i, measure) {
          var measureId = "Measure-" + (measureIdx++);
          var name = measure.caption == undefined ? measure.name : measure.caption;
          measuresHolder.find('ul').append('<li id="' + measureId + '" class="draggableMeasure olapObject prompt-wizard-elements">' + name + '</li>');
          myself.addOlapObject(WizardOlapObjectManager.MEASURE, measure);

        });

        $("li.draggableMeasure", measuresHolder).draggable({
          helper: 'clone',
          type: "Measure",

          start: function(event, ui) {
            var originalRow = $(this);
            var dragObjElements = ui.helper;

            originalRow.addClass('dragging-element');
          },

          stop: function(event, ui) {
            var originalRow = $(this);

            originalRow.removeClass('dragging-element');
          }
        });

        $('.prompt-wizard-caption', measureSelect)
            .removeClass('disabled')
            .off()
            .click(function() {
              $(this).parent().toggleClass('collapsed');
            });

        myself.getAvailableFilters();
      } else {
        alert(json.result);
      }
    });
  }
};


var StylesRequests = {

  syncStyles: function(myself) {

    $.getJSON(wd.cde.endpoints.getPluginUrl() + "SyncStyles", {
      operation: "listStyles"
    }, function(json) {
      myself.styles = json.result;
    });
  },

  listStyleRenderers: function(myself) {

    $.getJSON(wd.cde.endpoints.getPluginUrl() + "listRenderers", {
      operation: "listStyles"
    }, function(json) {
      myself.renderers = json.result;

    });
  },

  initStyles: function(saveSettingsParams, wcdf, myself, callback) {

    var refreshTitle = function(title) {
      var content = title + '<div class="cdfdd-title-status"></div>';
      $("div.cdfdd-title")
          .empty()
          .html(content)
          .attr('title', title);
    };

    $.post(wd.cde.endpoints.getPluginUrl() + "Syncronize", saveSettingsParams, function(result) {
      try {
        var json = eval("(" + result + ")");
        if(json.status == "true") {
          myself.setDashboardWcdf(wcdf);
          refreshTitle(wcdf.title);
          callback();
        } else {
          throw json.result;
        }
      } catch(e) {
        NotifyBarUtils.errorNotifyBar("Errors initializing settings", e);
      }
    });
  }
};


var SaveRequests = {

  saveSettings: function(saveSettingsParams, cdfdd, wcdf, myself) {

    $.post(wd.cde.endpoints.getPluginUrl() + "Syncronize", saveSettingsParams, function(result) {
      try {
        var json = eval("(" + result + ")");
        if(json.status == "true") {
          myself.setDashboardWcdf(wcdf);
          // We need to reload the layout engine in case the rendererType changed
          cdfdd.layout.init();
          NotifyBarUtils.successNotifyBar("Dashboard Settings saved successfully");
        } else {
          throw json.result;
        }
      } catch (e) {
        NotifyBarUtils.errorNotifyBar("Errors saving settings", e);
      }
    });
  },

  saveDashboard: function(saveParams, stripArgs) {

    var successFunction = function(result) {
      //$.getJSON("/pentaho/content/pentaho-cdf-dd/Syncronize", saveParams, function(json) {
      try {
        var json = eval("(" + result + ")");
        if(json.status == "true") {
          if(stripArgs.needsReload) {
            window.location.reload();
          } else {
            CDFDDUtils.markAsClean();
            NotifyBarUtils.successNotifyBar("Dashboard saved successfully");
          }
        } else {
          throw json.result;
        }
      } catch(e) {
        NotifyBarUtils.errorNotifyBar("Errors saving file", e);
      }
    };

    // CDF-271 $.browser is depricated
    var rv;
    var re = new RegExp("MSIE ([0-9]{1,}[\.0-9]{0,})");
    if(re.exec(navigator.userAgent) != null) {
      rv = parseFloat(RegExp.$1);
    }

    if(rv && rv < 10) {
      Dashboards.log("Dashboard can't be saved using multipart/form-data, it will not save large Dashboards");
      $.post(wd.cde.endpoints.getPluginUrl() + "Syncronize", saveParams, successFunction);
    } else {
      var $uploadForm = $('<form action="' + wd.cde.endpoints.getPluginUrl() + 'Syncronize" method="post" enctype="multipart/form-data">');
      $uploadForm.ajaxForm({
        data: saveParams,
        success: successFunction
      });
      $uploadForm.submit();
    }
  },

  saveAsDashboard: function(saveAsParams, selectedFolder, selectedFile, myself) {

    var successFunction = function(result) {
      try {
        var json = eval("(" + result + ")");
        if(json.status == "true") {
          if(selectedFolder[0] == "/") {
            selectedFolder = selectedFolder.substring(1, selectedFolder.length);
          }
          var solutionPath = selectedFolder.split("/");
          myself.initStyles(function() {
            //cdfdd.setExitNotification(false);
            window.location = window.location.protocol + "//" + window.location.host + wd.cde.endpoints.getPluginUrl() + 'Edit?solution=' + solutionPath[0] + "&path=" + solutionPath.slice(1).join("/") + "&file=" + encodeURIComponent( selectedFile );
          });
        } else {
          throw json.result;
        }
      } catch(e) {
        NotifyBarUtils.errorNotifyBar("Errors saving file", e);
      }
    };

    // CDF-271 $.browser is depricated
    var rv;
    var re = new RegExp("MSIE ([0-9]{1,}[\.0-9]{0,})");
    if(re.exec(navigator.userAgent) != null) {
      rv = parseFloat(RegExp.$1);
    }

    if(rv && rv < 10) {
      Dashboards.log("Dashboard can't be saved using multipart/form-data, it will not save large Dashboards");
      $.post(wd.cde.endpoints.getPluginUrl() + "Syncronize", saveAsParams, successFunction);
    } else {
      var $uploadForm = $('<form action="' + wd.cde.endpoints.getPluginUrl() + 'Syncronize" method="post" enctype="multipart/form-data">');
      $uploadForm.ajaxForm({
        data: saveAsParams,
        success: successFunction
      });
      $uploadForm.submit();
    }
  },

  saveAsWidget: function(saveAsParams, selectedFolder, selectedFile, myself) {

    var successFunction = function(result) {
      try {
        var json = JSON.parse(result);
        if(json.status == "true") {
          if(selectedFolder[0] == "/") {
            selectedFolder = selectedFolder.substring(1, selectedFolder.length);
          }
          var solutionPath = selectedFolder.split("/");
          var wcdf = myself.getDashboardWcdf();
          // TODO: dashboard is being saved twice. This also needs to be fixed..
          wcdf.title = saveAsParams.title;
          wcdf.description = saveAsParams.description;
          wcdf.widgetName = saveAsParams.widgetName;
          wcdf.widget = true;
          myself.saveSettingsRequest(wcdf);
          myself.initStyles(function() {
            window.location = window.location.protocol + "//" + window.location.host + wd.cde.endpoints.getPluginUrl() + 'Edit?solution=' + solutionPath[0] + "&path=" + solutionPath.slice(1).join("/") + "&file=" + encodeURIComponent( selectedFile );
          });
        } else {
          throw json.result;
        }
      } catch(e) {
        NotifyBarUtils.errorNotifyBar("Errors saving file", e);
      }
    };

    // CDF-271 $.browser is depricated
    var rv;
    var re = new RegExp("MSIE ([0-9]{1,}[\.0-9]{0,})");
    if(re.exec(navigator.userAgent) != null) {
      rv = parseFloat(RegExp.$1);
    }

    if(rv && rv < 10) {
      Dashboards.log("Dashboard can't be saved using multipart/form-data, it will not save large Dashboards");
      $.post(wd.cde.endpoints.getPluginUrl() + "Syncronize", saveAsParams, successFunction);
    } else {
      var $uploadForm = $('<form action="' + wd.cde.endpoints.getPluginUrl() + 'Syncronize" method="post" enctype="multipart/form-data">');
      $uploadForm.ajaxForm({
        data: saveAsParams,
        success: successFunction
      });
      $uploadForm.submit();
    }
  }
};

var LoadRequests = {

  loadDashboard: function(loadParams, myself) {
    if(wd.cde.endpoints.isEmptyFilePath(loadParams.file)) {
      loadParams.file = "";
    }

    $.post(wd.cde.endpoints.getPluginUrl() + "Syncronize", loadParams, function(result) {
      var json = eval("(" + eval("(" + result + ")").result + ")");
      if(json && json.status == "true") {
        myself.setDashboardData(myself.unstrip(json.result.data));
        myself.setDashboardWcdf(json.result.wcdf);
        myself.init();
      } else {
        alert(json && json.result);
      }
    });
  }
};

var PreviewRequests = {

  previewDashboard: function(saveParams, _href) {

    var syncUrl = wd.cde.endpoints.getPluginUrl() + "Syncronize";
    var deletePreviewFiles = function() {
      var deleteData = {
        operation: "deletepreview",
        file: cdfdd.getDashboardData().filename
      };
      $.post(syncUrl, deleteData);
    };

    var successFunction = function(result) {
      try {
        var json = eval("(" + result + ")");
        if(json.status == "true") {
          $.fancybox({
            type: "iframe",
            closeBtn: true,
            autoSize: false,
            href: _href,
            width: $(window).width(),
            height: $(window).height(),
            onClosed: deletePreviewFiles,
            onError: deletePreviewFiles
          });
        } else {
        }
      } catch(e) {
        NotifyBarUtils.errorNotifyBar("Errors saving file", e);
      }
    };

    // CDF-271 $.browser is depricated
    var rv;
    var re = new RegExp("MSIE ([0-9]{1,}[\.0-9]{0,})");
    if(re.exec(navigator.userAgent) != null) {
      rv = parseFloat(RegExp.$1);
    }

    if(rv && rv < 10) {
      Dashboards.log("Dashboard can't be saved using multipart/form-data, it will not save large Dashboards");
      $.post(syncUrl, saveParams, successFunction);
    } else {
      var $uploadForm = $('<form action="' + syncUrl + '" method="post" enctype="multipart/form-data">');
      $uploadForm.ajaxForm({
        data: saveParams,
        success: successFunction
      });
      $uploadForm.submit();
    }
  },

  getPreviewUrl: function(solution, path, file) {
    var _href;
    if(solution == 'system') {
      // CPK Dashboard
      path = path.split('/');
      var pluginName = (path.length > 0 ) ? path[0] : "",
          endpointName = file.replace('_tmp.cdfde', "").replace('_tmp.wcdf', "").toLowerCase();
      _href = wd.cde.endpoints.getPluginUrl().replace("pentaho-cdf-dd", pluginName) + endpointName + "?pluginId=" + pluginName + "&mode=preview";
    } else {
      // Regular Dashboard
      _href = wd.cde.endpoints.getPluginUrl() + "Render?" + "solution=" + solution + "&path=" + path + "&file=" + file
          + "&bypassCache=true&root=" + window.location.host;
    }

    return _href
  }
};

var SolutionTreeRequests = {

  getExplorerFolderEndpoint: function(url) {
    return url.replace("Syncronize", "ExploreFolder");
  }
};


var PluginRequests = {

  getCDEPlugins: function(onSuccess) {
    $.ajax({
      url: "getCDEplugins",
      dataType: 'json',
      data: [],
      success: onSuccess
    });
  }
};

var VersionRequests = {
  getGetVersion: function() {
    return wd.cde.endpoints.getPluginUrl() + "getVersion";
  },

  getCheckVersion: function() {
    return wd.cde.endpoints.getPluginUrl() + "checkVersion";
  }
};

var ExternalEditor = {
  getEditorUrl: function() {
    return wd.cde.endpoints.getPluginUrl() + "extEditor";
  },

  getGetUrl: function() {
    return wd.cde.endpoints.getPluginUrl() + "getFile";
  },

  getCanEditUrl: function() {
    return wd.cde.endpoints.getPluginUrl() + "canEdit";
  },

  getWriteUrl: function() {
    return wd.cde.endpoints.getPluginUrl() + "writeFile";
  }
};

var OlapUtils = {
  getOlapCubesUrl: function() {
    return wd.cde.endpoints.getPluginUrl() + "OlapUtils?operation=GetOlapCubes";
  }
};

var Cgg = {
  getCggDrawUrl: function() {
    return window.location.href.substring(0, window.location.href.indexOf("content") - 1) + wd.cde.endpoints.getUnbasedCggPluginUrl() + "Draw";
  }
};

var SettingsHelper = {
  getExtraPromptContent: function(){
    return '';
  },

  callExtraContentSubmit: function(){
  },

  getStyles: function(wcdf, myself){
    return myself.styles;
  },

  getSelectedStyle: function(wcdf){
    return wcdf.style;
  }
};
