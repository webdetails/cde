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

// Base class and general utils
if(!Array.prototype.map) {
  Array.prototype.map = function(fun /*, thisp */) {
    "use strict";

    if(this === void 0 || this === null) {
      throw new TypeError();
    }

    var t = Object(this);
    var len = t.length >>> 0;
    if(typeof fun !== "function") {
      throw new TypeError();
    }

    var res = new Array(len);
    var thisp = arguments[1];
    for(var i = 0; i < len; i++) {
      if(i in t) {
        res[i] = fun.call(thisp, t[i], i, t);
      }
    }

    return res;
  };
}

$.editable.addInputType('autocomplete', {
  element: $.editable.types.text.element,
  plugin: function(settings, original) {
    $('input').focus(function() {
      this.select();
    });
    $('input', this).autocomplete(settings.autocomplete);
  }
});

Dashboards.syncDebugLevel && Dashboards.syncDebugLevel();

var CDFDD = Base.extend({

  logger: "",
  dashboardData: {},
  dashboardWcdf: {},
  styles: [],
  renderers: [],

  // Panels
  layout: {},
  components: {},
  datasources: {},
  wizards: {},

  constructor: function() {

    this.logger = new Logger("CDFDD");
    this.logger.info("Initializing CDFDD");

    // Common stuff
    TableManager.globalInit();
    WizardManager.globalInit();
  },


  init: function() {

    this.logger.info("Initializing.");

    // Add Panel
    this.layout = new LayoutPanel(LayoutPanel.MAIN_PANEL);
    this.layout.init();

    this.components = new ComponentsPanel(ComponentsPanel.MAIN_PANEL);
    this.components.init();

    this.datasources = new DatasourcesPanel(DatasourcesPanel.MAIN_PANEL);
    this.datasources.init();

    // Wizards panel
    this.wizard = new WizardManager();
    this.wizard.init();

    // Show layout panel
    this.layout.switchTo();
    this.selectedPanelId = LayoutPanel.MAIN_PANEL;

    //// Enable alert when leaving page
    //this.setExitNotification(true);

    // Keyboard shortcuts
    $(function () {
      $(document).keydown(function (e) {
        if ($(e.target).is('input, textarea')) {
          return;
        }

        var activePanel = cdfdd.getActivePanel();
        var activeTable = activePanel.getSelectedTable();

        switch(e.which) {
          /*
           * Utilities
           */
          case 83:
            if(e.shiftKey) { //shift + s
              cdfdd.save();
            }
            break;
          case 80:
            if(e.shiftKey) { //shift + p
              cdfdd.previewMode();
            }
            break;
          case 71:
            if(e.shiftKey) { //shift + g
              cdfdd.cggDialog();
            }
            break;
          case 222: //?
            cdfdd.toggleHelp();
            break;
          case 86:
            if(e.shiftKey) { //shift + v
              ComponentValidations.validateComponents();
            }
            break;
          case 13:
            e.preventDefault();
            if(e.shiftKey) { //shift + enter
              activeTable.cellUnselected();
            } else { //enter
              var rowIdx = activeTable.getSelectedCell()[0];
              var rowId = activeTable.getTableModel().getEvaluatedId(rowIdx);
              var row = $('#' + rowId + ' td:eq(1)');
              row.find('div, div.edit :button, form :input.colorcheck').addBack().click();
            }
            break;

          /*
           * Navigation
           */
          case 97: //Numpad 1
          case 49: //1
            $(".cdfdd-modes").find("a:eq(2)").click();
            break;
          case 98: //Numpad 2
          case 50: //2
            $(".cdfdd-modes").find("a:eq(1)").click();
            break;
          case 99: //Numpad 3
          case 51: //3
            $(".cdfdd-modes").find("a:eq(0)").click();
            break;
          case 38:
            if(e.shiftKey) { //shift + up
              var operation = new MoveUpOperation();
              operation.checkAndExecute(activeTable);
            } else { //up
              activeTable.selectCellBefore();
            }
            break;
          case 40:
            if(e.shiftKey) { //shift+down
              var operation = new MoveDownOperation();
              operation.checkAndExecute(activeTable);
            } else { //down
              activeTable.selectCellAfter();
            }
            break;
          case 37: //left
            activeTable.collapseCell();
            break;
          case 39: //right
            activeTable.expandCell();
            break;
          case 9: //tab
            e.preventDefault();
            var nextTable = activePanel.selectNextTable();
            if(nextTable) {
              var row = nextTable.getSelectedCell();
              row = !row.length ? [0,0] : row;
              nextTable.selectCell(row[0], row[1], 'simple');
            }
            break;

          /*
           * Row Operations
           */
          case 82: //r
            if(e.ctrlKey) { return; }
            e.preventDefault();
            var operation = new LayoutAddRowOperation();
            operation.checkAndExecute(activeTable);
            break;
          case 67: //c
            e.preventDefault();
            var operation = new LayoutAddColumnsOperation();
            operation.checkAndExecute(activeTable);
            break;
          case 72: //h
            e.preventDefault();
            var operation = new LayoutAddHtmlOperation();
            operation.checkAndExecute(activeTable);
            break;
          case 88:
            if(e.shiftKey) { //shift+x
              var operation = new DeleteOperation();
              operation.checkAndExecute(activeTable);
            }
            break;
          case 68:
            if(e.shiftKey) { //shift+d
              var operation = new (activePanel.getDuplicateOperation())();
              operation.checkAndExecute(activeTable);
            }
            break;
        }
      });
    });

    // Activate tooltips - Note: Disabled since last style change
    // $(".tooltip").tooltip({showURL: false });

    // Load styles list
    var myself = this;

    StylesRequests.syncStyles(myself);

    StylesRequests.listStyleRenderers(myself);
  },

  getActivePanel: function() {
    var panel = $('#cdfdd-panels > :visible');
    var panelId = panel.attr('id').replace('panel-', '');
    return Panel.getPanel(panelId);
  },

  initStyles: function(callback) {
    var myself = this;
    if(myself.styles.length > 0) {
      var wcdf = myself.getDashboardWcdf();
      // Default to Clean or the first available style if Clean isn't available
      var cleanStyle = myself.styles.indexOf('Clean');
      if(!wcdf.style) {
        wcdf.style = myself.styles[cleanStyle >= 0 ? cleanStyle : 0];
      }
      var rendererType = myself.renderers.indexOf('bootstrap');
      if(!wcdf.rendererType) {
        wcdf.rendererType = myself.renderers[rendererType >= 0 ? rendererType : 0];
      }
      //only set style setting and renderer type (not title nor description)
      var saveSettingsParams = {
        operation: "saveSettings",
        file: CDFDDFileName.replace(".cdfde", ".wcdf"),
        style: wcdf.style,
        rendererType: wcdf.rendererType
      };

      StylesRequests.initStyles(saveSettingsParams, wcdf, myself, callback);
    }
  },

  isNewFile: function(fileName) {
    //TODO: mashup of all checks found and then some; should be only null/empty
    return fileName == "/" || fileName == "/null/null/null" || fileName == "" || fileName == null;
  },

  load: function() {

    this.logger.info("Loading dashboard...");

    var myself = this;
    var loadParams = {
      operation: "load",
      file: CDFDDFileName
    };

    LoadRequests.loadDashboard(loadParams, myself);

  },

  save: function() {

    this.logger.info("Saving dashboard...");
    this.dashboardData.filename = CDFDDFileName;
    var myself = this;
    _.each(this.components.getComponents(), function(w) {
      if(w.meta_widget) {
        w.meta_wcdf = window[w.type + "Model"].getStub().meta_wcdf;
      }
    });

    var stripArgs = {
      needsReload: false
    };

    var saveParams = {
      operation: "save",
      file: CDFDDFileName,
      cdfstructure: JSON.stringify(this.strip(this.dashboardData, stripArgs), null, 1)
    };

    if(!this.isNewFile(CDFDDFileName)) {
      SaveRequests.saveDashboard(saveParams, stripArgs);
    } else {
      this.combinedSaveAs();
    }
  },

  /*
   * Sample CDFDE JSON structure:
   * 
   * cdeDef = { 
   *   layout: {
   *      rows: [
   *        // One component
   *        {
   *          id:   "", 
   *          name: "",
   *          type: "",
   *          typeDesc: "",
   *          parent: "",
   *          properties: [
   *            // One property
   *            {
   *              name:  "foo",
   *              value: "",
   *              type:  "String"
   *            }
   *          ]
   *        }
   *      ]
   *   }, 
   *   components: {
   *      rows: [...]
   *   },
   *   datasources: {
   *      rows: [...]
   *   },
   *   filename: ""
   * }
   */
  strip: function(original, keyArgs) {
    var me = this;

    // These are the only components' properties' attributes that are kept
    var KEEP_PROP_ATTRS = {
      type: true, // InputType
      name: true, // Alias
      value: true, // Value...
      url: true  // TODO: What's this??
    };

    var debugLevel = Dashboards.debug;

    // Holds the user's response to keeping properties with no defintion.
    var userKeepUndefinedProps = null; // not asked yet
    var userDeletePreviousVersionProps = null; // not asked yet

    // Removes extra information and saves space
    var stripped = Util.clone(original); // deep clone

    // Each SECTION
    $.each(stripped, function(i, section) {
      if(typeof section !== 'object') {
        return;
      }

      // Each COMPONENT
      $.each(section.rows, function(j, comp) {
        var isSpecial = comp.type === 'Label' || comp.type === 'Group';
        var compModel = BaseModel.getModel(comp.type);
        var isUndefinedModel = !compModel || compModel.isUndefined;
        var hasUndefinedProps;
        var hasLoggedComp;
        var logComponent = function(debugLevel) {
          if(!hasLoggedComp && debugLevel >= 2) {
            if(!isSpecial && compModel.isUndefined) {
              hasLoggedComp = true;
              me.logger.warn("    save/strip - component " + me.describeComponent(comp) + " is of undefined type.");
            } else if(debugLevel >= 3) {
              hasLoggedComp = true;
              me.logger.info("    save/strip - component " + me.describeComponent(comp) + ".");
            }
          }
        };

        logComponent(debugLevel);

        var ps = comp.properties;
        var L;
        if(ps && (L = ps.length)) {
          var compatVersion = me._getCompatVersion(ps); // null || >= 0

          // Each PROPERTY
          var k = 0;
          while(k < L) {
            var prop = ps[k];
            var name = prop.name;

            // Had already said that he wants to keep undefineds?
            var keepProp = (userKeepUndefinedProps === true);
            if(!keepProp) {
              // Special property; has no definition, but is saved anyway
              var isSpecialProp = (name === 'Group');
              var isDefinedProp = isSpecialProp || !!compModel.getPropertyUsage(name);
              if(!isDefinedProp) {
                hasUndefinedProps = true;
              }

              // Don't remove properties of unknown/undefined component types
              keepProp = isDefinedProp || isUndefinedModel;

              if(!keepProp && debugLevel >= 2) {
                logComponent(Infinity);
                me.logger.warn("      save/strip - found property of undefined type '" + name + "'.");
              }

              if(!keepProp && userKeepUndefinedProps == null) {
                // Didn't ask the user yet.
                keepProp =
                    userKeepUndefinedProps = !confirm("The dashboard contains components whose properties have no definition (those marked with a ?).\n" +
                        "Would you like to REMOVE those properties?\n" +
                        "The dashboard will be RELOADED after the save operation.");

                if(!keepProp && keyArgs) {
                  keyArgs.needsReload = true;
                }
              }
            }

            if(keepProp &&
                compatVersion != null &&
                userDeletePreviousVersionProps !== false) {

              var match = CDFDD.DISCONTINUED_PROP_PATTERN.exec(prop.description);
              if(match) {
                // Property has a last version.
                // Check if it is lower than the current compatVersion.
                var propLastVersion = +match[1];
                var canDelete = propLastVersion < compatVersion;

                // Have asked the user if he wants to delete?
                if(canDelete && userDeletePreviousVersionProps == null) {

                  canDelete =
                      userDeletePreviousVersionProps = confirm(
                              "The dashboard contains components with deprecated properties.\n" +
                              "Would you like to REMOVE all the deprecated properties?\n" +
                              "The dashboard will be RELOADED after the save operation.");

                  if(userDeletePreviousVersionProps && keyArgs) {
                    keyArgs.needsReload = true;
                  }
                }

                if(canDelete) {
                  keepProp = false;
                }
              }
            }

            // Do it - Keep or Delete
            if(keepProp) {
              // Keep property.
              // Delete unnecessary attributes.
              $.each(prop, function(a) {
                if(!KEEP_PROP_ATTRS[a]) {
                  delete prop[a];
                }
              });
              k++;
            } else {
              ps.splice(k, 1);
              L--;
            }
          }
        }

        if(!isSpecial && (isUndefinedModel || hasUndefinedProps)) {
          // Remove added "?? ", in load/unstrip.
          if(comp.typeDesc && comp.typeDesc.charAt(0) === "?") {
            comp.typeDesc = comp.typeDesc.substr(3);
          }
        }
      });
    });

    return stripped;
  },

  _getCompatVersion: function(ps) {
    var L;
    if(ps && (L = ps.length)) {
      var k = 0;
      while(k < L) {
        var p = ps[k];
        var name = p.name;
        if(name === "cccCompatVersion" || name === "compatVersion") {
          var cv = +p.value;
          if(!isNaN(cv) && cv >= 0) {
            return cv;
          }
          break;
        }
        k++;
      }
    }

    return null;
  },

  /** Adds extra information, previously removed to save space */
  unstrip: function(original) {
    var me = this;
    var beefed = Util.clone(original);
    var debugLevel = Dashboards.debug;

    $.each(beefed, function(i, section) {
      if(typeof section !== "object") {
        return;
      }

      if(debugLevel >= 3) {
        me.logger.info("  load/unstrip - " + i);
      }

      $.each(section.rows, function(j, comp) {

        var isSpecial = comp.type === 'Label';
        var compModel = BaseModel.getModel(comp.type, /*createIfUndefined*/true);

        // Fix component type name over time, reducing number of legacy names around.
        comp.type = compModel.MODEL;
        if(compModel.description) {
          comp.typeDesc = compModel.description;
        }
        // TODO: parent node fixing when model changes.

        var hasUndefinedProps;
        var hasLoggedComp;
        var logComponent = function(debugLevel) {
          if(!hasLoggedComp && debugLevel >= 2) {
            if(!isSpecial && compModel.isUndefined) {
              hasLoggedComp = true;
              me.logger.warn("    load/unstrip - component " + me.describeComponent(comp) + " is of undefined type, and has " + comp.properties.length + " properties.");
            } else if(debugLevel >= 3) {
              hasLoggedComp = true;
              me.logger.info("    load/unstrip - component " + me.describeComponent(comp) + " has " + comp.properties.length + " properties.");
            }
          }
        };

        logComponent(debugLevel);

        $.each(comp.properties, function(idx, prop) {
          try {
            var propName = prop.name;
            var stubAndUsage = compModel.getPropertyStubAndUsage(propName);
            var propStub = stubAndUsage[0];
            var propUsage = stubAndUsage[1];
            if(!propUsage) {
              if(propName !== "Group") {
                hasUndefinedProps = true;
                if(debugLevel >= 2) {
                  logComponent(Infinity);
                  me.logger.warn("      load/unstrip - found property of undefined type '" + propName + "'.");
                }
              }
            } else {
              // Normalize name -> alias
              prop.name = propUsage.alias;
            }

            // Add own attributes of Stub to property, 
            // if it doesn't have them already.
            for(var attr in propStub) {
              if(propStub.hasOwnProperty(attr)) {
                if(!prop.hasOwnProperty(attr)) {
                  prop[attr] = propStub[attr];
                } else if(attr === 'type' && prop[attr] !== propStub[attr]) {
                  // The InputType of the property has changed.
                  // Try to "upgrade" the property usage.
                  me._upgradePropertyType(prop, propStub);
                }
              }
            }
          } catch(e) {
            Dashboards.log(prop.name + ": " + e);
          }
        });

        if(!isSpecial && (compModel.isUndefined || hasUndefinedProps)) {
          // This is removed later upon save, in strip.
          comp.typeDesc = (compModel.isUndefined ? "?? " : "?  ") + comp.typeDesc;
        }
      });
    });

    return beefed;
  },

  _upgradePropertyType: function(p, stub) {
    var oldType = p.type;
    var newType = stub.type;

    // In principle, any type could be upgraded to an array,
    // but it's safer to treat only known types.
    if(newType === 'Array' &&
        ['String', 'Float', 'Integer', 'Boolean']
            .indexOf(oldType) === 0) {
      var value = p.value;
      if(value == null || value === '') {
        value = '[]';
      } else {
        // Ensure string
        value = '' + value;

        // Ensure within brackets
        if(value.indexOf('[') !== 0) {

          // Ensure we have a string
          if(value.indexOf('"') !== 0 && value.indexOf("'") !== 0) {
            value = '"' + value + '"';
          }

          value = '[' + value + ']';
        }
      }

      p.type = newType;
      p.value = value;
    }
  },

  getComponentName: function(comp) {
    var name = CDFDDUtils.getProperty(comp, 'name');
    return name ? name.value : '';
  },

  describeComponent: function(comp) {
    var name = this.getComponentName(comp);
    return (name ? (name + " ") : "") + "[" + comp.type + "]";
  },

  toggleHelp: function() {
    $("#keyboard_shortcuts_help").toggle();
  },

  newDashboard: function() {
    var myself = this;
    var content = '' +
        '<h2>New Dashboard</h2>\n' +
        '<hr/>Are you sure you want to start a new dashboard?<br/>\n' +
        '<span class="description">Unsaved changes will be lost.</span>\n';
    $.prompt(content, {
      buttons: {
        Ok: true,
        Cancel: false
      },
      prefix: "popup",
      callback: function(v, m, f) {
        if(v) myself.saveAs(true);
      }
    });
  },

  saveAs: function(fromScratch) {

    var selectedFolder = "";
    var selectedFile = "";
    var selectedTitle = this.getDashboardWcdf().title;
    var selectedDescription = this.getDashboardWcdf().description;
    var myself = this;
    var content = '' +
        '<h2>Save as:</h2><hr style="background:none;"/>\n' +
        '<div id="container_id" class="folderexplorer" width="400px"></div>\n' +
        '<span class="folderexplorerfilelabel">File Name:</span>\n' +
        '<span class="folderexplorerfileinput"><input id="fileInput"  type="text"/></span>\n' +
        '<hr class="filexplorerhr"/>\n' +
        '<span class="folderexplorerextralabel" >Extra Information:</span><br/>\n' +
        '<span class="folderexplorerextralabels" >Title:</span>\n' +
        '<input id="titleInput" class="folderexplorertitleinput" type="text" value="' + selectedTitle + '"></input><br/>\n' +
        '<span class="folderexplorerextralabels" >Description:</span>\n' +
        '<input id="descriptionInput" class="folderexplorerdescinput" type="text" value="' + selectedDescription + '"></input>';

    $.prompt(content, {
      loaded: function() {
        $('#fileInput').change(function() {
          selectedFile = this.value;
        });
        $('#titleInput').change(function() {
          selectedTitle = this.value;
        });
        $('#descriptionInput').change(function() {
          selectedDescription = this.value;
        });
        $('#container_id').fileTree({
          root: '/',
          script: SolutionTreeRequests.getExplorerFolderEndpoint(CDFDDDataUrl) + "?fileExtensions=.wcdf&access=create",
          expandSpeed: 1000,
          collapseSpeed: 1000,
          multiFolder: false,
          folderClick: function(obj, folder) {
            if($(".selectedFolder").length > 0) $(".selectedFolder").attr("class", "");
            $(obj).attr("class", "selectedFolder");
            selectedFolder = folder;
            $("#fileInput").val("");
          }
        }, function(file) {
          $("#fileInput").val(file.replace(selectedFolder, ""));
          selectedFile = $("#fileInput").val();
        });
      },
      buttons: {
        Ok: true,
        Cancel: false
      },
      opacity: 0.2,
      prefix: 'popup',
      callback: function(v, m, f) {
        if(v) {

          if(selectedFile.indexOf(".") != -1 && (selectedFile.length < 5 || selectedFile.lastIndexOf(".wcdf") != selectedFile.length - 5)) {
            $.prompt('Invalid file extension. Must be .wcdf', {
              prefix: "popup"
            });
          } else if(selectedFolder.length == 0) {
            $.prompt('Please choose destination folder.', {
              prefix: "popup"
            });
          } else if(selectedFile.length == 0) {
            $.prompt('Please enter the file name.', {
              prefix: "popup"
            });
          } else if(selectedFile.length > 0) {
            if(selectedFile.indexOf(".wcdf") == -1) { selectedFile += ".wcdf"; }

            CDFDDFileName = selectedFolder + selectedFile;
            myself.dashboardData.filename = CDFDDFileName;
            _.each(myself.components.getComponents(), function(w) {
              if(w.meta_widget) {
                w.meta_wcdf = window[w.type + "Model"].getStub().meta_wcdf;
              }
            });

            var saveAsParams = {
              operation: fromScratch ? "newFile" : "saveas",
              file: selectedFolder + selectedFile,
              title: selectedTitle,
              description: selectedDescription,
              //cdfstructure: JSON.stringify(myself.dashboardData, null, 1) // TODO: shouldn't it strip, like save does?
              cdfstructure: JSON.stringify(myself.strip(myself.dashboardData, stripArgs), null, 1)
            };

            SaveRequests.saveAsDashboard(saveAsParams, selectedFolder, selectedFile, myself);
          }
        }
      }
    });
  },

  footerMode: function(mode) {

    var version = {

      versionGetInfo: null,
      versionCheckInfo: null,

      getInfo: function(url) {
        var versionInfo = '';
        $.get(url, function(result) {
          if(!result) {
            Dashboards.log('CDFF-DD: ' + url + ' error');
            return;
          }
          versionInfo = result;
        });
        return versionInfo;
      },

      getVersion: function() {

        var versionCheckUrl = VersionRequests.getGetVersion();
        versionGetInfo = this.getInfo(versionCheckUrl);
        return versionGetInfo;
      },

      checkVersion: function() {

        var versionCheckUrl = VersionRequests.getCheckVersion();
        var versionCheckInfo = this.getInfo(versionCheckUrl);
        var msg = '';

        versionCheckInfo = JSON.parse(versionCheckInfo);

        switch(versionCheckInfo.result) {
          case 'update':
            msg = 'You are currently running an outdated version. Please update to the new version <a href="' + versionCheckInfo.downloadUrl + '">here</a>';
            break;
          case 'latest':
            msg = 'Your version is up to date.';
            break;
          case 'inconclusive':
            msg = 'Only ctools branches support version checking. You can download lastest version <a href="' + versionCheckInfo.downloadUrl + '">here</a>';
            break;
          case 'error':
            msg = 'There was an error checking for newer versions: ' + versionCheckInfo.msg;
            break;
        }
        return msg;
      }
    };


    var addCSS = function(fileRef) {
      var fileref = document.createElement("link");
      fileref.setAttribute("rel", "stylesheet");
      fileref.setAttribute("type", "text/css");
      fileref.setAttribute("href", fileRef);
      document.getElementsByTagName("head")[0].appendChild(fileref);
    };

    var removeCSS = function(fileRef) {
      var allCtrl = document.getElementsByTagName('link');
      for(var i = allCtrl.length; i >= 0; i--) {
        if(allCtrl[i] && allCtrl[i].getAttribute('href') != null && allCtrl[i].getAttribute('href').indexOf(fileRef) != -1)
          allCtrl[i].parentNode.removeChild(allCtrl[i]);
      }
    };

    var htmlHref = "./static/" + mode + ".html";
    var cssFileRef = "./css/" + mode + ".css";

    $.fancybox({
      ajax: {
        type: "GET"
      },
      closeBtn: true,
      href: htmlHref,
      autoDimensions: false,
      width: 950,
      height: 600,
      padding: 0,
      margin: 0,
      onStart: function() {
        addCSS(cssFileRef);
      },
      onClosed: function() {
        removeCSS(cssFileRef);
      }
    });

    if(mode == 'about.fancybox') {
      $('#fancybox-content .version').html(version.getVersion());
      $('#fancybox-content .message').html(version.checkVersion());
    }
  },

  previewMode: function() {

    if(this.isNewFile(CDFDDFileName)) {
      $.notifyBar({
        html: "Need to save an initial dashboard before previews are available."
      });
      return;
    }

    var fullPath = CDFDDFileName.split("/");
    var solution = fullPath[1];
    var path = fullPath.slice(2, fullPath.length - 1).join("/");
    var file = fullPath[fullPath.length - 1].replace(".cdfde", "_tmp.wcdf");

    this.logger.info("Saving temporary dashboard...");

    //temporarily set the filename to tmp
    var tmpFileName = CDFDDFileName.replace(".cdfde", "_tmp.wcdf");
    this.dashboardData.filename = tmpFileName;
    var serializedDashboard = JSON.stringify(this.dashboardData, null, 0);
    this.dashboardData.filename = CDFDDFileName;

    var saveParams = {
      operation: "saveas",
      file: tmpFileName,
      cdfstructure: serializedDashboard
    };

    PreviewRequests.previewDashboard(saveParams, PreviewRequests.getPreviewUrl(solution, path, file));
  },


  savePulldown: function(target, evt) {
    var myself = this,
        $pulldown = $(target);
    $pulldown.append(templates.savePulldown());
    $("body").one("click", function() {
      $pulldown.find("ul").remove();
    });
    $pulldown.find(".save-as-dashboard").click(function() {
      myself.saveAs();
    });
    $pulldown.find(".save-as-widget").click(function() {
      myself.saveAsWidget();
    });
    evt.stopPropagation();
  },

  reload: function() {
    this.logger.warn("Reloading dashboard... ");

    $.prompt('<h2>Reload</h2><hr/>Are you sure you want to reload?<br><span class="description">Unsaved changes will be lost.</span>', {
      buttons: {
        Ok: true,
        Cancel: false
      },
      prefix: "popup",
      callback: function(v, m, f) {
        if(v) window.location.reload();
      }
    });
  },

  reset: function() {

    this.logger.info("Resetting dashboad");
    CDFDD.PANELS().empty();
  },

  saveSettings: function() {
    var myself = this;
    var ready = true;

    function sCallback() {
      if(myself.styles.length > 0 && myself.renderers.length > 0) {
        myself.saveSettingsCallback();
      }
    }

    if(this.styles.length == 0) {
      ready = false;

      StylesRequests.syncStyles(myself);
      sCallback();

    }

    if(this.renderers.length == 0) {
      ready = false;

      StylesRequests.listStyleRenderers(myself);
      sCallback();

    }

    if(ready) {
      this.saveSettingsCallback();
    }
  },

  saveSettingsCallback: function() {
    var wcdf = $.extend({}, this.getDashboardWcdf()),
        settingsData = $.extend({
          widgetParameters: []
        }, wcdf),
        myself = this,
        content;

    settingsData.styles = [];
    _.each(this.styles, function(obj) {
      settingsData.styles.push({
        style: obj,
        selected: wcdf.style == obj
      });
    });
    settingsData.renderers = [];
    _.each(this.renderers, function(obj) {
      settingsData.renderers.push({
        renderer: obj,
        selected: wcdf.rendererType == obj
      });
    });
    /* Generate a list of the parameter names */
    var currentParams = cdfdd.getDashboardWcdf().widgetParameters;
    settingsData.parameters = Panel.getPanel(ComponentsPanel.MAIN_PANEL).getParameters()
        .map(function(e) {
          var val = e.properties.filter(function(i) {
            return i.description == "Name";
          })[0].value;
          return {
            parameter: val,
            selected: _.contains(currentParams, val)
          };
        });
    content = '' +
        '<span>' +
        ' <h2>Settings:</h2>' +
        '</span>' +
        '<hr style="background: none;"/>\n' +
        '<span class="title">Title:</span>' +
        '<br/>' +
        '<input class="cdf_settings_input" id="titleInput" type="text" value="{{title}}"/>' +
        '<br/>\n' +
        '{{#widget}}' +
        '<span class="title">Widget Name:</span>' +
        '<br/>' +
        '<input class="cdf_settings_input" id="widgetNameInput" type="text" value="{{widgetName}}"/>' +
        '<br/>\n' +
        '{{/widget}}' +
        '<span class="title">Author:</span>' +
        '<br/>' +
        '<input class="cdf_settings_input" id="authorInput" type="text" value="{{author}}"/>' +
        '<span class="title">Description:</span>' +
        '<br/>' +
        '<textarea class="cdf_settings_textarea" id="descriptionInput">{{description}}</textarea>\n' +
        '<span class="title">Style:</span>' +
        '<br/>' +
        '<select class="cdf_settings_input" id="styleInput">\n' +
        '{{#styles}}' +
        '<option value="{{style}}" {{#selected}}selected{{/selected}}>{{style}}</option>\n' +
        '{{/styles}}' +
        '</select>' +
        '<hr style="background:none;"/>' +
        '<span class="title">Dashboard Type:</span><br/><select class="cdf_settings_input" id="rendererInput">\n' +
        '{{#renderers}}' +
        '   <option value="{{renderer}}" {{#selected}}selected{{/selected}}>{{renderer}}</option>\n' +
        '{{/renderers}}' +
        '</select>' +
        '{{#widget}}' +
        '<span>' +
        '  <br>' +
        '  <b>Widget Parameters:</b>' +
        '</span>' +
        '<br>' +
        '<span id="widgetParameters">' +
        ' <div style=" max-height: 110px; overflow: auto; ">' +
        ' {{#parameters}}' +
        '     <input type="checkbox" name="{{parameter}}" value="{{parameter}}" {{#selected}}checked{{/selected}} style=" position: relative; top: 4px; "><span>{{parameter}}</span><br>\n' +
        ' {{/parameters}}' +
        '</span>' +
        ' </div>' +
        '{{/widget}}';

    content = Mustache.render(content, settingsData);
    $.prompt(content, {
      buttons: {
        Save: true,
        Cancel: false
      },
      prefix: "popup",
      submit: function() {
        wcdf.title = $("#titleInput").val();
        wcdf.author = $("#authorInput").val();
        wcdf.description = $("#descriptionInput").val();
        wcdf.style = $("#styleInput").val();
        wcdf.widgetName = $("#widgetNameInput").val();
        wcdf.rendererType = $("#rendererInput").val();
        wcdf.widgetParameters = [];
        $("#widgetParameters input[type='checkbox']:checked")
            .each(function(i, e) {
              wcdf.widgetParameters.push(e.value);
            });
      },
      callback: function(v, m, f) {
        if(v) {
          /* Validations */
          var validInputs = true;
          if(wcdf.widget) {
            if(!/^[a-zA-Z0-9_]*$/.test(wcdf.widgetName)) {
              $.prompt('Invalid characters in widget name. Only alphanumeric characters and \'_\' are allowed.', {
                prefix: "popup"
              });
              validInputs = false;
            } else if(wcdf.widgetName.length == 0) {
              if(wcdf.title.length == 0) {
                $.prompt('No widget name provided. Tried to use title instead but title is empty.', {
                  prefix: "popup"
                });
                validInputs = false;
              } else {
                wcdf.widgetName = wcdf.title.replace(/[^a-zA-Z0-9_]/g, "");
              }
            }
          }

          if(validInputs) {
            myself.saveSettingsRequest(wcdf);
          }
        }
      }
    });
  },

  combinedSaveAs: function(fromScratch) {

    var selectedTitle = "",
        selectedDescription = "",
        selectedFile = "",
        selectedFolder = "";
    var myself = this;
    var radioButtons = '' +
        '<form>\n' +
        ' <table>\n' +
        '   <tr style="font-weight: normal;">\n' +
        '     <td style="width:50%;margin: 0;padding: 0;">\n' +
        '       <div style=" width: 15px; padding: 0; margin: 0; float: left; "><input type="radio" name="saveAsRadio" value="dashboard" id="dashRadio" style="width:100%;" checked></div>\n' +
        '       <div style="width:80%; float: right;padding: 0;margin: 0;"><span style="top: -2px; width: 20%;">Dashboard</span></div>\n' +
        '     </td>\n' +
        '     <td style="width:50%;margin: 0;padding: 0;">\n' +
        '       <div style="width:15px; float:left;"><input type="radio" name="saveAsRadio" value="widget" id="widgetRadio" style="width:100%;"></div>\n' +
        '       <div style="width:80%; float: right;"><span style="top: -2px; width: 20%;">Widget</span></div>\n' +
        '     </td>\n' +
        '   </tr>\n' +
        '  </table>\n' +
        '</form>\n';

    var widgetFieldContent = '' +
        '<div style="width:20%; float:left;position: relative;top: 2px; left:0px;">\n' +
        ' <span class="folderexplorerfilelabel" style="width:100%; left:0;">Widget Name: *</span>\n' +
        '</div>\n' +
        '<div style="width:80%;float:right;">\n' +
        ' <span style="top:0; left: 0; ">\n' +
        '   <input id="componentInput"  type="text" value="" style="width: 100%;vertical-align: middle;margin: 0;"/>\n' +
        ' </span>\n' +
        '</div>\n' +
        '<hr class="filexplorerhr"/>\n';

    var fileInfo = '' +
        '<div id="container_id" class="folderexplorer" width="400px"></div>\n' +
        '<div style="height:25px;padding-top: 10px;">\n' +
        ' <div style="float: left; width:20%;position: relative;top: 7px;">' +
        '   <span class="folderexplorerfilelabel" style="float: left;width: 100%; left:0;">File Name: *</span>' +
        ' </div>\n' +
        ' <div style="float: right;width:80%;">' +
        '   <table>' +
        '     <tr>' +
        '       <td style="padding:0;">' +
        '         <span style=" top: 0px; left:0;"><input id="fileInput"  type="text" value="" style="width: 100%;vertical-align: middle;margin: 0;"/></span>' +
        '       </td>' +
        '       <td style="width:200px;">' +
        radioButtons +
        '       </td>' +
        '     </tr>' +
        '   </table>' +
        ' </div>' +
        '</div>\n' +
        '<br>\n' +
        '<div class="widgetField"></div>\n' +
        '<hr class="saveHr">' +
        '<span class="folderexplorerextralabel" style="left:0px;">- Extra Information -</span><br/>\n' +
        '<div>\n' +
        ' <div style="float:left; width:20%;">\n' +
        '   <span class="folderexplorerextralabels" style="font-weight: normal;">Title:</span>\n' +
        ' </div>\n' +
        ' <div style="float:right; width:80%;">\n' +
        '   <input id="titleInput" class="folderexplorertitleinput" type="text" value="' + selectedTitle + '" style="width: 100%;float: left;margin: 0;padding: 0;left: 0;"></input>\n' +
        '  </div>\n' +
        '</div>\n' +
        '<hr>' +
        ' <div>\n' +
        '   <div style="float:left; width:20%;">' +
        '     <span class="folderexplorerextralabels" style="font-weight: normal;">Description:</span>\n' +
        '   </div>\n' +
        '   <div style="float:right; width:80%;">' +
        '     <input id="descriptionInput"  class="folderexplorerdescinput" type="text" value="' + selectedDescription + '" style="width: 100%;float: left;margin: 0;padding: 0;left: 0;"></input>\n' +
        '   </div><br>\n' +
        '</div>\n';

    var content = '' +
        '<h2>Save as...</h2>\n' +
        '<hr/>\n' +
        '<div style="">' + fileInfo + '</div>\n';

    $.prompt(content, {
      prefix: "popup",
      buttons: {
        Ok: 1,
        Cancel: 0
      },
      loaded: function() {

        $("#popup").css("width", "515px");
        $(".widgetField").hide();
        $(".widgetField").append(widgetFieldContent);

        $('#container_id').fileTree({
          root: '/',
          script: SolutionTreeRequests.getExplorerFolderEndpoint(CDFDDDataUrl) + "?fileExtensions=.wcdf&access=create",
          expandSpeed: 1000,
          collapseSpeed: 1000,
          multiFolder: false,
          folderClick: function(obj, folder) {
            if($(".selectedFolder").length > 0) $(".selectedFolder").attr("class", "");
            $(obj).attr("class", "selectedFolder");
            selectedFolder = folder;
            $("#fileInput").val("");
          }
        }, function(file) {
          $("#fileInput").val(file.replace(selectedFolder, ""));
          selectedFile = $("#fileInput").val();
        });

        $("#dashRadio").click(function(event) {
          $("#container_id").show();
          $(".widgetField").hide();
        });

        $("#widgetRadio").click(function(event) {
          $("#container_id").hide();
          $(".widgetField").show();
        });
      },
      submit: function(v, m, f) {
        if(v == 1) {
          function isValidField(field) {
            return (field != null && field != undefined);
          }

          function isValidFieldNotEmpty(field) {
            return (field != null && field != undefined && field != "");
          }

          /*In case of Dashboards
           the propper means will be used
           */
          if($('input[name=saveAsRadio]:checked').val() == "dashboard") {

            selectedFile = $('#fileInput').val();
            selectedTitle = isValidField($("#titleInput").val()) ? $("#titleInput").val() : cdfdd.getDashboardWcdf().title;
            selectedDescription = isValidFieldNotEmpty($("#descriptionInput").val()) ? $("#descriptionInput").val() : cdfdd.getDashboardWcdf().description;

            if(selectedFile.indexOf(".") != -1 && (selectedFile.length < 5 || selectedFile.lastIndexOf(".wcdf") != selectedFile.length - 5)) {
              $.prompt('Invalid file extension. Must be .wcdf', {
                prefix: "popup"
              });
            } else if(selectedFolder.length == 0) {
              $.prompt('Please choose destination folder.', {
                prefix: "popup"
              });
            } else if(selectedFile.length == 0) {
              $.prompt('Please enter the file name.', {
                prefix: "popup"
              });
            }

            if(selectedFile.indexOf(".wcdf") == -1) { selectedFile += ".wcdf"; }

            CDFDDFileName = selectedFolder + selectedFile;
            cdfdd.dashboardData.filename = CDFDDFileName;
            var stripArgs = {
              needsReload: false
            };

            var saveAsParams = {
              operation: fromScratch ? "newFile" : "saveas",
              file: selectedFolder + selectedFile,
              title: selectedTitle,
              description: selectedDescription,
              //cdfstructure: JSON.stringify(cdfdd.dashboardData, "", 1) // TODO: shouldn't it strip, like save does?
              cdfstructure: JSON.stringify(cdfdd.strip(cdfdd.dashboardData, stripArgs), "", 1)
            };

            SaveRequests.saveAsDashboard(saveAsParams, selectedFolder, selectedFile, myself);
          }
          /*In case of Widgets
           the propper means will be used
           */
          else if($('input[name=saveAsRadio]:checked').val() == "widget") {
            selectedFolder = wd.helpers.repository.getWidgetsLocation();
            selectedFile = $('#fileInput').val();
            selectedTitle = isValidField($("#titleInput").val()) ? $("#titleInput").val() : cdfdd.getDashboardWcdf().title;
            selectedDescription = isValidFieldNotEmpty($("#descriptionInput").val()) ? $("#descriptionInput").val() : cdfdd.getDashboardWcdf().description;
            var selectedWidgetName = $("#componentInput").val();

            /* Validations */
            var validInputs = true;
            if(!/^[a-zA-Z0-9_]*$/.test(selectedWidgetName)) {
              $.prompt('Invalid characters in widget name. Only alphanumeric characters and \'_\' are allowed.', {prefix: "popup"});
              validInputs = false;
            } else if(selectedWidgetName.length == 0) {
              if(selectedTitle.length == 0) {
                $.prompt('No widget name provided. Tried to use title instead but title is empty.', {prefix: "popup"});
                validInputs = false;
              } else {
                selectedWidgetName = selectedTitle.replace(/[^a-zA-Z0-9_]/g, "");
              }
            }

            if(validInputs) {
              if(selectedFile.indexOf(".wcdf") == -1) {
                selectedFile += ".wcdf";
              }

              CDFDDFileName = selectedFolder + selectedFile;
              myself.dashboardData.filename = CDFDDFileName;

              var saveAsParams = {
                operation: fromScratch ? "newFile" : "saveas",
                file: selectedFolder + selectedFile,
                title: selectedTitle,
                description: selectedDescription,
                //cdfstructure: JSON.stringify(myself.dashboardData, null, 1),
                cdfstructure: JSON.stringify(myself.strip(myself.dashboardData, stripArgs), null, 1),
                widgetName: selectedWidgetName
              };

              SaveRequests.saveAsWidget(saveAsParams, selectedFolder, selectedFile, myself);
            }

          }

          return false;
        }
      }
    });

  },
  saveAsWidget: function(fromScratch) {

    var selectedFolder = wd.helpers.repository.getWidgetsLocation(),
        selectedFile = "",
        selectedTitle = this.getDashboardWcdf().title,
        selectedDescription = this.getDashboardWcdf().description,
        selectedWidgetName = "",
        options = {
          title: selectedTitle,
          description: selectedDescription
        },
        myself = this,
        content = templates.saveAsWidget(options);

    $.prompt(content, {
      loaded: function() {
        $(this).addClass('save-as-widget');
        $('#fileInput').change(function() {
          selectedFile = this.value;
        });
        $('#titleInput').change(function() {
          selectedTitle = this.value;
        });
        $('#descriptionInput').change(function() {
          selectedDescription = this.value;
        });
        $('#componentInput').change(function() {
          selectedWidgetName = this.value;
        });
      },
      buttons: {
        Ok: true,
        Cancel: false
      },
      prefix: "popup",
      opacity: 0.2,
      classes: 'save-as-widget',
      callback: function(v, m, f) {
        if(v) {

          /* Validations */
          var validInputs = true;
          if(selectedFile.indexOf(".") > -1 && !/\.wcdf$/.test(selectedFile)) {
            $.prompt('Invalid file extension. Must be .wcdf', {prefix: "popup"});
            validInputs = false;
          } else if(!/^[a-zA-Z0-9_]*$/.test(selectedWidgetName)) {
            $.prompt('Invalid characters in widget name. Only alphanumeric characters and \'_\' are allowed.', {prefix: "popup"});
            validInputs = false;
          } else if(selectedWidgetName.length == 0) {
            if(selectedTitle.length == 0) {
              $.prompt('No widget name provided. Tried to use title instead but title is empty.', {prefix: "popup"});
              validInputs = false;
            } else {
              selectedWidgetName = selectedTitle.replace(/[^a-zA-Z0-9_]/g, "");
            }
          } else if(selectedFile.length <= 0) {
            validInputs = false;
          }

          if(validInputs) {
            if(selectedFile.indexOf(".wcdf") == -1) {
              selectedFile += ".wcdf";
            }

            CDFDDFileName = selectedFolder + selectedFile;
            myself.dashboardData.filename = CDFDDFileName;
            _.each(myself.components.getComponents(), function(w) {
              if(w.meta_widget) {
                w.meta_wcdf = window[w.type + "Model"].getStub().meta_wcdf;
              }
            });

            var saveAsParams = {
              operation: fromScratch ? "newFile" : "saveas",
              file: selectedFolder + selectedFile,
              title: selectedTitle,
              description: selectedDescription,
              //cdfstructure: JSON.stringify(myself.dashboardData, null, 1),
              cdfstructure: JSON.stringify(myself.strip(myself.dashboardData, stripArgs), null, 1),
              widgetName: selectedWidgetName
            };

            SaveRequests.saveAsWidget(saveAsParams, selectedFolder, selectedFile, myself);
          }
        }
      }
    });
  },

  saveSettingsRequest: function(wcdf) {
    var myself = this;
    this.logger.info("Saving dashboard settings...");
    var saveSettingsParams = $.extend({
      operation: "saveSettings",
      file: CDFDDFileName.replace(".cdfde", ".wcdf")
    }, wcdf);

    var newRenderer = saveSettingsParams.rendererType;
    var oldRenderer = cdfdd.getDashboardWcdf().rendererType;
    var toUpdate = [];

    //if the renderer type changed, then check for the existence of Table Components
    //that needs to update the style property
    if(newRenderer != oldRenderer) {
      var components = cdfdd.getDashboardData().components.rows;
      _.each(components, function(comp, i) {
        if(comp.parent == "OTHERCOMPONENTS" && comp.type == "ComponentsTable") {
          _.each(comp.properties, function(prop, j) {
            if(prop.name == "tableStyle") {
              if(( newRenderer != "bootstrap" && prop.value == "bootstrap" ) ||
                  ( newRenderer == "bootstrap" && prop.value != "bootstrap" )) {
                toUpdate.push(prop);
                return;
              }
            }
          });
        }
      });
    }

    if(toUpdate.length) {
      var message = Dashboards.i18nSupport.prop('SaveSettings.INFO_UPDATE_TABLE_STYLE_PROP') + '\n' +
          Dashboards.i18nSupport.prop('SaveSettings.CONFIRMATION_UPDATE_TABLE_STYLE_PROP');
      var updateStyle = confirm(message);

      if(updateStyle) {
        _.each(toUpdate, function(prop, index) {
          if(newRenderer == "bootstrap") {
            prop.value = "bootstrap";
          } else {
            prop.value = "themeroller";
          }
        });
        cdfdd.components.initTables();
      }
    }

    SaveRequests.saveSettings(saveSettingsParams, cdfdd, wcdf, myself);

  },

  setDashboardData: function(dashboardData) {
    this.dashboardData = dashboardData;
  },
  getDashboardData: function() {
    return this.dashboardData;
  },
  setDashboardWcdf: function(dashboardWcdf) {
    this.dashboardWcdf = dashboardWcdf;
  },
  getDashboardWcdf: function() {
    return this.dashboardWcdf;
  },

  //setExitNotification: function(enable){
  //  if(window.parent && window.parent != window){//only do this outside of puc (puc's close tab won't trigger this, only closing puc)
  //    return;
  //  }
  //
  //  if(enable){
  //    window.onbeforeunload = function(e) { return 'Any unsaved changes will be lost.'; }
  //  }
  //  else {
  //    window.onbeforeunload = function() {null};
  //  }
  //},

  cggDialog: function() {
    var components = cdfdd.dashboardData.components.rows;
    var cggCandidates = components.filter(function(e) {
      return e.meta_cdwSupport == 'true';
    });

    var ph = $('#cggDialog');
    ph = ph.length > 0 ? ph : $("<div id='cggDialog' style='display:none'></div>").appendTo($("body")).jqm();

    // cgg url:
    var cggUrl = Cgg.getCggDrawUrl() + "?script=" + CDFDDFileName.substring(0, CDFDDFileName.lastIndexOf("/")) + "/";

    ph.empty();
    ph.append("<h3>Choose what charts to render as CGG</h3>" +
        "<p>CDE can generate CGG scripts to allow charts to be exported. Choose the ones you want, and files will be generated when you save the dashboard</p>");
    cggCandidates.map(function(e) {

      var section = $("<div/>");

      var checkbox = $("<input type='checkbox'>");
      checkbox[0].checked = (e.meta_cdwRender == 'true' || false);
      checkbox.change(function() {
        e.meta_cdwRender = "" + this.checked;
      });

      var componentName = '',
          title = '';
      e.properties.map(function(p) {
        if(p.name == 'title') {
          title = p.value;
        } else if(p.name == 'name') {
          componentName = p.value;
        }
      });
      var label = "<span class='label'>" + (title !== '' ? title : componentName) + "</span>";
      section.append(checkbox);
      section.append(label);

      var showUrlButton = $("<button class='showUrl'>Url</button>").click(function(evt) {

        Dashboards.log("Toggle Url show");
        var $t = $(this).toggleClass("active");
        $t.parent().find(".urlPreviewer").toggleClass("collapsed").find("input").select();

      });
      section.append(showUrlButton).append("<br />");

      // append dashboard name with component name and extension (.js)
      var scriptFileName = CDFDDFileName.replace(/^.*[\\\/]/, '').split('.')[0] + '_' + componentName + ".js";

      $("<div class='urlPreviewer collapsed'><input type='text' value = '" + cggUrl + scriptFileName + "&outputType=png" + "'></input></div>").appendTo(section);
      section.appendTo(ph);
    });

    var $close = $("<div class='cggDialogClose'><button>Close</button></div>");
    $close.find("button").click(function(evt) {
      ph.jqmHide();
    });
    ph.append($close);
    ph.jqmShow();
  }
}, {
  LAYOUT: function() {
    return $("table#layout-div tbody");
  },
  PANELS: function() {
    return $("#cdfdd-panels");
  },

  // The captured number is the last version where the property was defined.
  DISCONTINUED_PROP_PATTERN: /^V(\d+)\s*-/
});


// Panel
var Panel = Base.extend({

  id: "",
  name: "",
  logger: {},

  constructor: function(id) {
    this.logger = new Logger("Panel");
    this.id = id;
  },

  init: function() {
    this.logger.info("Initializing panel " + name);

    if($("#panel-" + this.id).length == 0) {
      CDFDD.PANELS().append(this.getHtml());
    }
  },

  switchTo: function() {
    this.logger.debug("Switching to " + this.name);
    CDFDD.selectedPanelId = this.id;
    $("div." + Panel.GUID).hide();
    $("div#panel-" + this.id).show();
  },

  reset: function() {
    $("#" + id).empty();
  },

  getHtml: function() {

    return '' +
        '<div id="panel-' + this.id + '" class="span-24 last ' + Panel.GUID + '">\n' +
        ' <div class="panel-content">' + this.getContent() + '</div>\n' +
        '</div>\n';
  },

  getContent: function() {
    return '<span class="highlight">Not done yet</span>';
  },

  setId: function(id) {
    this.id = id;
  },
  getId: function() {
    return this.id;
  }

}, {
  GUID: "cdfdd-panel",
  panels: {},

  register: function(panel) {
    Panel.panels[panel.getId()] = panel;
  },

  getPanel: function(id) {
    return Panel.panels[id];
  },

  getRowPropertyValue: function(row, propertyName) {
    var output = "";
    $.each(row.properties, function(i, property) {
      if(property.name == propertyName) {
        output = property.value;
        return false;
      }
    });
    return output;
  },
  enableThisButton: function(doc) {
    //var ENABLED_STR = "_active";
    //var DISABLED_STR = "_inactive";
    // Disable other buttons and enable this one
    var a = $(doc);
    $(".panelButton").removeClass("panelButton-active");
    a.parent().addClass("panelButton-active");
  },

  disableThisButton: function(doc) {
    var a = $(doc);
    var myIdx = a.prevAll("a").length;
    a.parent().find("img").each(function(i, x) {
      if(i == myIdx) {
        $(x).attr("src", $(x).attr("src").replace(/(.*)\/X?(.*)/, "$1/X$2"));
      }
    });
  },

  setHover: function(comp) {
    var el = $(comp);
    var src = el.attr("src");

    var xPos = src.search("/X");
    if(xPos != -1) { //only hover disabled
      src = src.slice(0, xPos) + "/" + src.slice(xPos + 2); //"/X".length
      el.attr("src", src.slice(0, src.length - ".png".length) + "_mouseover.png");
    }
  },

  unsetHover: function(comp) {
    var el = $(comp);
    var src = el.attr("src");

    var hPos = src.search("_mouseover");
    if(hPos != -1) { //back to disabled if on hover
      //set disabled (X)
      src = src.replace(/(.*)\/X?(.*)/, "$1/X$2");
      //remove _hover
      el.attr("src", src.slice(0, src.length - "_mouseover.png".length) + ".png");
    }
  }

});


// Logger
var Logger = Base.extend({

  ERROR: 0,
  WARN: 1,
  INFO: 2,
  DEBUG: 3,
  name: "",

  logDescription: ["ERROR", "WARN", "INFO", "DEBUG"],

  constructor: function(name) {
    this.name = name;
  },

  log: function(level, str) {
    if(cdfddLogEnabled && level <= cdfddLogLevel && typeof console != 'undefined') {
      console.log(" - [" + this.name + "] " + this.logDescription[level] + ": " + str);
    }
  },
  error: function(str) {
    this.log(this.ERROR, str);
  },
  warn: function(str) {
    this.log(this.WARN, str);
  },
  info: function(str) {
    this.log(this.INFO, str);
  },
  debug: function(str) {
    this.log(this.DEBUG, str);
  }
});


// Utility functions
var CDFDDUtils = Base.extend({}, {
  ev: function(v) {
    return (typeof v === 'function' ? v() : v);
  },

  getProperty: function(stub, name) {
    var result;
    if(!stub.properties) {
      return null;
    }

    $.each(stub.properties, function(i, p) {
      if(p.name === name) {
        result = p;
        return false;
      }
    });

    return result;
  }
});

var cdfdd;
$(function() {

  cdfdd = new CDFDD();
  cdfdd.load();

  // Temp stuff
  // WizardManager.globalInit();
  // var wizard = new OlapParameterWizard();
  // wizard.init();

  // Extend jeditable
  /*
   * Jeditable - jQuery in place edit plugin
   *
   * Copyright (c) 2006-2009 Mika Tuupola, Dylan Verheul
   *
   * Licensed under the MIT license:
   *   http://www.opensource.org/licenses/mit-license.php
   *
   * Project home:
   *   http://www.appelsiini.net/projects/jeditable
   *
   * Based on editable by Dylan Verheul <dylan_at_dyve.net>:
   *    http://www.dyve.net/jquery/?editable
   *
   * The MIT License (MIT)
   * Copyright (c) 2006-2009 Mika Tuupola, Dylan Verheul
   * Permission is hereby granted, free of charge, to any person obtaining a copy
   * of this software and associated documentation files (the "Software"), to deal
   * in the Software without restriction, including without limitation the rights
   * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
   * copies of the Software, and to permit persons to whom the Software is
   * furnished to do so, subject to the following conditions:

   * The above copyright notice and this permission notice shall be included in
   * all copies or substantial portions of the Software.

   * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
   * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
   * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
   * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
   * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
   * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
   * THE SOFTWARE.
   */
  $.editable.types.selectMulti = {
    element: function(settings, original) {
      var select = $('<select multiple="multiple" />');
      $(this).append(select);
      return (select);
    },
    content: function(data, settings, original) {
      // If it is string assume it is an array.
      if(String == data.constructor) {
        eval('var json = ' + data);
      } else {
        // Otherwise assume it is a hash already.
        var json = data;
      }
      for(var key in json) {
        if(!json.hasOwnProperty(key)) {
          continue;
        }
        if('selected' == key) {
          continue;
        }
        var option = $('<option />').val(key).append(json[key]);
        $('select', this).append(option);
      }
      // Loop option again to set selected. IE needed this...
      var _selectedHash = {};
      var _selectedArray = json['selected'];
      //eval("_selectedArray = " + json['selected']);
      $.each(_selectedArray, function(i, val) {
        _selectedHash[val] = true
      });
      $('select', this).children().each(function() {
        if(_selectedHash[$(this).val()] ||
            $(this).text() == $.trim(original.revert)) {
          $(this).attr('selected', 'selected');
        }
      });
      $('select', this).multiSelect({
        oneOrMoreSelected: "*"
      }, function() {
      });
    }
  };

  /* End Jeditable attribution */
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

  // Remove bug in position() of multipleselect
  $.extend($.fn, {
    multiSelectOptionsShow: function() {
      // Hide any open option boxes
      $('.multiSelect').multiSelectOptionsHide();

      var t = $(this);
      var ph = t.next('.multiSelectOptions');

      ph.find('LABEL').removeClass('hover');
      t.addClass('active').next('.multiSelectOptions').show();

      // Position it
      // The following line overrides the default
      //var offset = $(this).closes("td").position();
      var offset = [];
      ph.css({
        top: offset.top + t.outerHeight() + 'px'
      }).css({
        left: offset.left + 'px'
      });

      // Disappear on hover out
      var timer = '';
      ph.unbind("mouseenter mouseleave").mouseenter(function() {
        Dashboards.log("enter");
        clearTimeout(timer);
      }).mouseleave(function() {
        Dashboards.log("leave");
        timer = setTimeout(function() {
          t.multiSelectOptionsHide();
          ph.unbind("mouseenter mouseleave");
          //'jQuery(multiSelectCurrent).multiSelectOptionsHide(); $(multiSelectCurrent).unbind("mouseenter");'
        }, 250);
      });

    }
  })

});

templates = {};
templates.savePulldown = Mustache.compile(
        "<ul class='controlOptions'>" +
        " <li class='item popup'>Save As Dashboard</li>" +
        " <li class='item popup'>Save As Widget</li>" +
        "</ul>");

templates.saveAsWidget = Mustache.compile(
        '<h2>Save as Widget:</h2><hr/>\n' +
        ' <span class="folderexplorerfilelabel">File Name:</span>\n' +
        ' <input id="fileInput" class="folderexplorerfileinput" type="text" style="width:100%;"/>\n' +
        ' <hr class="filexplorerhr"/>\n' +
        ' <span class="folderexplorerextralabel" >-Extra Information-</span><br/>\n' +
        ' <span class="folderexplorerextralabels" >Title:</span>' +
        ' <input id="titleInput" class="folderexplorertitleinput" type="text" value="{{title}}"/><br/>\n' +
        ' <span class="folderexplorerextralabels" >Description:</span>' +
        ' <input id="descriptionInput"  class="folderexplorerdescinput" type="text" value="{{description}}"/>');
