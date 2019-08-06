/*!
 * Copyright 2002 - 2019 Webdetails, a Hitachi Vantara company. All rights reserved.
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

  staticResUrl: "/api/repos",
  staticUrl: "/api/plugins/pentaho-cdf-dd/files/",
  pluginUrl: "/plugin/pentaho-cdf-dd/api/",
  cggPluginUrl: "/plugin/cgg/api/services/",
  cssResourceUrl: "resources/get?resource=",
  imageResourceUrl: "resources/get?resource=",
  jsResourceUrl: "resources/getJs?resource=",
  saikuUiPluginUrl: "/content/saiku-ui/index.html?biplugin5=true",
  newDashboardUrl: "/wcdf/new",

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

  getNewDashboardUrl: function() {
    return this.getStaticResUrl() + this.newDashboardUrl;
  },

  isEmptyFilePath: function(filePath) {
    return (!filePath || "/" == filePath || encodeURIComponent("/") == filePath );
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
        try {
          if(result && result.status === "true") {
            NotifyBarUtils.successNotifyBar("Template saved successfully");
          } else {
            throw result && result.result;
          }
        } catch(e) {
          NotifyBarUtils.errorNotifyBar("Errors saving template", e);
        }
      }
    });
  },

  doGetJson: function(loadParams) {
    var myself = this;

    $.ajax({
      type: 'POST',
      data: loadParams,
      dataType: 'json',
      url: wd.cde.endpoints.getPluginUrl() + "syncronizer/syncronizeTemplates",
      success: function(result) {
        try {
          if(result && result.status === "true") {
            var templates = result.result;
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

            var _templatesWrapper = CDFDDUtils.wrapPopupTitle('Apply Template') +
                CDFDDUtils.wrapPopupBody(_templates + '</div>', 'template-popup-container');

            var _myTemplatesWrapper = CDFDDUtils.wrapPopupTitle('Apply Custom Template') +
                CDFDDUtils.wrapPopupBody(_myTemplates + '</div>', 'template-popup-container');

            var loaded = function() {
              myself.selectTemplate = undefined;

              $('div.popup-template').click(function() {
                var container = $(this);
                $('div.popup-template').removeClass('template-selected');
                container.addClass('template-selected');
                myself.selectTemplate = templates[container.find("img").attr("id")];
              });

              $('div.popup-template-container').slice(-3).addClass('bottom'); //last 3 elements
            };

            var callback = function(v, m, f) {
              var selectedTemplate = myself.selectTemplate;
              if(v === 1 && selectedTemplate !== undefined) {
                var message = CDFDDUtils.wrapPopupTitle('Load Template') +
                    CDFDDUtils.wrapPopupBody(myself.warningTemplateMessage(selectedTemplate), 'layout-popup');

                CDFDDUtils.prompt(message, {
                  callback: myself.callbackLoadTemplate,
                  popupClass: 'settings-popup'
                });
              }
            };

            var promptTemplates = {
              loaded: loaded,
              popupClass: 'template-popup',
              buttons: myTemplatesCount > 0 ? {MyTemplates: 2, Ok: 1, Cancel: 0} : {Ok: 1, Cancel: 0},
              callback: callback,
              submit: function(v, m, f) {
                if(v != 2) return true;
                $.prompt.close();
                CDFDDUtils.prompt(_myTemplatesWrapper, promptMyTemplates);
              }
            };

            var promptMyTemplates = {
              loaded: loaded,
              buttons: {Back: 2, Ok: 1, Cancel: 0},
              callback: callback,
              submit: function(v, m, f) {
                if(v != 2) return true;
                $.prompt.close();
                CDFDDUtils.prompt(_templatesWrapper, promptTemplates);
              }
            };

            CDFDDUtils.prompt(_templatesWrapper, promptTemplates);

          } else {
            throw result && result.result;
          }
        } catch(e) {
          NotifyBarUtils.errorNotifyBar("Error loading templates", e);
        }
      }
    });
  },

  warningTemplateMessage: function(template) {
    var overwriteComponents = template.structure.components.rows.length != 0;
    var overwriteDatasources = template.structure.datasources.rows.length != 0;

    var message = Dashboards.i18nSupport.prop('SynchronizeRequests.CONFIRMATION_LOAD_TEMPLATE') + '<br><br>';

    if (overwriteComponents && overwriteDatasources) {
      message += Dashboards.i18nSupport.prop('SynchronizeRequests.OVERWRITE_LAYOUT_COMP_DS');
    } else if (overwriteComponents) {
      message += Dashboards.i18nSupport.prop('SynchronizeRequests.OVERWRITE_LAYOUT_COMP');
    } else if (overwriteDatasources) {
      message += Dashboards.i18nSupport.prop('SynchronizeRequests.OVERWRITE_LAYOUT_DS');
    } else {
      message += Dashboards.i18nSupport.prop('SynchronizeRequests.OVERWRITE_LAYOUT');
    }

    return message;
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
  },

  initSettings: function(saveSettingsParams, wcdf, myself, callback) {
    saveSettingsParams.require = wcdf.require;
    $.post(wd.cde.endpoints.getPluginUrl() + "syncronizer/syncronizeDashboard", saveSettingsParams, function(result) {
      try {
        if(result && result.status === "true") {
          myself.setDashboardWcdf(wcdf);
          callback();
        } else {
          throw result && result.result;
        }
      } catch(e) {
        NotifyBarUtils.errorNotifyBar("Errors initializing settings", e);
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

    $.getJSON(wd.cde.endpoints.getPluginUrl() + "olap/getCubeStructure", params, function(json) {
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

    $.getJSON(wd.cde.endpoints.getPluginUrl() + "syncronizer/syncronizeStyles", {
    }, function(json) {
      myself.styles = json.result;
      myself._legacyStyles = [];
      myself._requireStyles = [];
      var pattern = /(.+)Require( - \(.+\))?$/;
      if (myself.styles) {
        for (var i = 0; i < myself.styles.length; i++) {
          var style = json.result[i];
          var reqStyle = pattern.exec(style);
          if(reqStyle) {
            style = reqStyle[1] + (reqStyle[2] !== undefined ? reqStyle[2] : "");
            myself._requireStyles.push(style);
          } else {
            myself._legacyStyles.push(style);
          }
        }
      }
    });
  },

  listStyleRenderers: function(myself) {

    $.getJSON(wd.cde.endpoints.getPluginUrl() + "renderer/listRenderers", {
    }, function(json) {
      myself.renderers = json.result;
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

    var refreshTitle = function(title) {
      var content = title + '<div class="cdfdd-title-status"></div>';
      $("div.cdfdd-title")
          .empty()
          .html(content)
          .attr('title', title);
    };

    $.post(wd.cde.endpoints.getPluginUrl() + "syncronizer/syncronizeDashboard", saveSettingsParams, function(result) {
      try {
        if(result && result.status == "true") {
          myself.setDashboardWcdf(result.result.wcdf);
          refreshTitle(result.result.wcdf.title);
          // We need to reload the layout engine in case the rendererType changed
          cdfdd.layout.init();
          NotifyBarUtils.successNotifyBar("Dashboard Settings saved successfully");
        } else {
          throw result && result.result;
        }
      } catch (e) {
        NotifyBarUtils.errorNotifyBar("Errors saving settings", e);
      }
    });
  },

  saveDashboard: function(saveParams, stripArgs) {

    if(wd.cde.endpoints.isEmptyFilePath(saveParams.file)) {
      saveParams.file = wd.cde.endpoints.getFilePathFromUrl();
    }
    var successFunction = function(result) {
      try {
        if(result && result.status == "true") {
          if(stripArgs.needsReload) {
            window.location.reload();
          } else {
            CDFDDUtils.markAsClean();
            NotifyBarUtils.successNotifyBar("Dashboard saved successfully");
          }
        } else {
          throw result && result.result;
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
      try {
        if(result && result.status === "true") {
          if(selectedFolder[0] === "/") {
            selectedFolder = selectedFolder.substring(1, selectedFolder.length);
          }
          var solutionPath = selectedFolder.split("/");
          myself.initSettings(function() {
            window.location = window.location.protocol + "//" + window.location.host + wd.cde.endpoints.getWebappBasePath() + '/api/repos/:' + selectedFolder.replace(new RegExp("/", "g"), ":") +  encodeURIComponent( selectedFile ) + '/edit';
          });
        } else {
          throw result && result.result;
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
      Dashboards.log(Dashboards.i18nSupport.prop("SaveAsDashboard.MULTIPART_ERROR"));
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
    var saveAsWidgetCallback = this.saveAsWidgetCallback;

    // CDF-271 $.browser is depricated
    var rv;
    var re = new RegExp("MSIE ([0-9]{1,}[\.0-9]{0,})");
    if(re.exec(navigator.userAgent) != null) {
      rv = parseFloat(RegExp.$1);
    }

    if(rv && rv < 10) {
      Dashboards.log("Dashboard can't be saved using multipart/form-data, it will not save large Dashboards");
      $.post(wd.cde.endpoints.getPluginUrl() + "syncronizer/syncronizeDashboard", saveAsParams, function(result) {
        saveAsWidgetCallback(result, saveAsParams.widgetName);
      });
    } else {
      var $uploadForm = $('<form action="' + wd.cde.endpoints.getPluginUrl() + 'syncronizer/saveDashboard" method="post" enctype="multipart/form-data">');
      $uploadForm.ajaxForm({
        data: saveAsParams,
        success: function(result){
          saveAsWidgetCallback(result, saveAsParams.widgetName);
        }
      });
      $uploadForm.submit();
    }

  },

  saveAsWidgetCallback: function(result, widgetName) {
    try {
      if(result && result.status === "true") {

        var selectedFolder = SaveRequests.saveRequestParams.selectedFolder;
        var selectedFile = SaveRequests.saveRequestParams.selectedFile;
        var myself = SaveRequests.saveRequestParams.myself;

        if(selectedFolder[0] === "/") {
          selectedFolder = selectedFolder.substring(1, selectedFolder.length);
        }

        var updateParams = {
          widget: true,
          widgetName: widgetName
        };
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
        throw result && result.result;
      }
    } catch(e) {
      NotifyBarUtils.errorNotifyBar("Errors saving file", e);
    }
  },

  redirect: function(selectedFolder, selectedFile) {
    var path = wd.cde.endpoints.getStaticResUrl() +
        '/:' + selectedFolder.replace(new RegExp("/", "g"), ":") + selectedFile + '/edit';
    location.assign(location.origin + path);
  }

};

var LoadRequests = {

  loadDashboard: function(loadParams, myself) {

    if(wd.cde.endpoints.isEmptyFilePath(loadParams.file)) {
      loadParams.file = wd.cde.endpoints.getFilePathFromUrl();
    }

    $.post(wd.cde.endpoints.getPluginUrl() + "syncronizer/syncronizeDashboard", loadParams, function(result) {
      if(result && result.status === "true") {
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

    var syncUrl = wd.cde.endpoints.getPluginUrl() + "syncronizer/syncronizeDashboard";
    var deletePreviewFiles = function() {
      var deleteData = {
        operation: "deletepreview",
        file: cdfdd.getDashboardData().filename
      };
      $.post(syncUrl, deleteData);
    };

    var successFunction = function(result) {
      try {
        if(result && result.status === "true") {
          $.fancybox.open({
                src: _href,
                type: "iframe",
                baseClass: "cdf-fancybox cdf-fancybox-iframe",
                btnTpl: {
                  smallBtn:
                      '<button type="button" data-fancybox-close class="fancybox-button fancybox-close-small" title="close">' +
                      '<svg id="svg-fancybox-close-small" width="350" height="350" viewbox="0 0 350 350" xmlns="http://www.w3.org/2000/svg"> <!-- Created with Method Draw - http://github.com/duopixel/Method-Draw/ --> <defs>  <filter id="svg_1_blur">   <feGaussianBlur stdDeviation="0" in="SourceGraphic"/>  </filter>  <filter height="200%" width="200%" y="-50%" x="-50%" id="svg_20_blur">   <feGaussianBlur stdDeviation="10" in="SourceGraphic"/>  </filter> </defs> <g>  <title>background</title>  <rect fill="none" id="canvas_background" height="302" width="302" y="-1" x="-1"/>  <g display="none" id="canvasGrid">   <rect fill="url(#gridpattern)" stroke-width="0" y="0" x="0" height="100%" width="100%" id="svg_2"/>  </g> </g> <g>  <title>Layer 1</title>  <ellipse filter="url(#svg_20_blur)" ry="127.5" rx="127.5" id="svg_20" cy="154.5" cx="158.5" stroke-opacity="0" stroke-width="16" stroke="#0f0f00" fill="#000000"/>  <ellipse filter="url(#svg_1_blur)" ry="111" rx="111" id="svg_1" cy="145" cx="159" stroke-width="30" stroke="#ffffff" fill="#000"/>  <path d="m329,164l2,127" id="svg_3"/>  <path d="m329,164l2,127" id="svg_4"/>  <path d="m329,164l2,127" id="svg_5"/>  <path d="m329,164l2,127" id="svg_6"/>  <path d="m329,164l2,127" id="svg_9"/>  <path d="m241,161l2,127" id="svg_10"/>  <path d="m160,79l2,127"/>  <path d="m120,54l2,127"/>  <line transform="rotate(-45, 162, 143.5)" stroke-linecap="null" stroke-linejoin="null" id="svg_7" y2="207" x2="163" y1="80" x1="161" stroke-width="30" stroke="#ffffff" fill="none"/>  <path d="m329,164l2,127" id="svg_11"/>  <path d="m329,164l2,127" id="svg_12"/>  <path d="m329,164l2,127" id="svg_13"/>  <path d="m329,164l2,127" id="svg_14"/>  <path d="m329,164l2,127" id="svg_15"/>  <path d="m239,162l2,127" id="svg_16"/>  <path d="m239,162l2,127" id="svg_17"/>  <path d="m239,162l2,127" id="svg_18"/>  <path d="m239,162l2,127" id="svg_19"/>  <path d="m158,79l2,127"/>  <path d="m118,54l2,127"/>  <line transform="rotate(45, 163, 141.5)" stroke-linecap="null" stroke-linejoin="null" id="svg_8" y2="205" x2="164" y1="78" x1="162" stroke-width="30" stroke="#ffffff" fill="none"/> </g></svg>' +
                      '</button>'
                }
              },
              {
                toolbar  : false,
                smallBtn : true,
                afterClose: deletePreviewFiles,
                iframe: {
                  preload: false,
                  css: {
                    width: $(window).width(),
                    height: $(window).height(),
                    "max-width": "100%",
                    "max-height": "100%"
                  }
                }
              });
        } else {
          throw result && result.result;
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
      Dashboards.log(Dashboards.i18nSupport.prop("PreviewRequests.MULTIPART_ERROR"));
      $.post(syncUrl, saveParams, successFunction);
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

var SettingsHelper = {
  getExtraPromptContent: function() {
    return '' +
        '<div class="popup-checkbox-container">' +
        '  <input type="checkbox" id="require_checkbox" name="require_checkbox" {{#require}}checked{{/require}}>' +
        '  <label title="Asynchronous module definition support" class="popup-input-label" for="require_checkbox">RequireJS Support</label>' +
        '</div>';
  },

  callExtraContentSubmit: function(myself, wcdf){
    var wasRequire = wcdf.require;
    var isRequire = $('#require_checkbox').is(':checked');
    var style = $("#styleInput").val();
    if(isRequire && myself._requireStyles.indexOf(style) > - 1) {
      wcdf.style = style + "Require";
    } else {
      wcdf.style = style;
    }
    if(!wasRequire && isRequire && !confirm(Dashboards.i18nSupport.prop("SaveAsDashboard.REQUIRE_DASHBOARD_SAVE"))) {
      return false;
    }
    wcdf.require = isRequire;
    return true;
  },

  getStyles: function(wcdf, myself){
    return wcdf.require ? myself._requireStyles : myself._legacyStyles;
  },

  getSelectedStyle: function(wcdf){
    var matchedStyle = /(.+)Require( - \(.+\))?$/.exec(wcdf.style);
    if (matchedStyle) {
      return matchedStyle[1];
    } else {
      return wcdf.style;
    }
  }

};
