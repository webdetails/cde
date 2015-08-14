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

// location.origin is not present in IE8 and IE9
if (!window.location.origin) {
  window.location.origin =
    window.location.protocol + "//" +
      window.location.hostname +
        (window.location.port ? ':' + window.location.port: '');
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

    //// Enable alert when leaving page
    //this.setExitNotification(true);

    // Keyboard shortcuts
    $(function () {
      $(document).keydown(function (e) {
        var target = $(e.target);
        var hasPopup = (target.find('.popup:visible').length || target.find('.jqmWindow:visible').length) > 0 ;
        if(target.is('input, select, textarea') || hasPopup || e.ctrlKey) {
          return;
        }

        e.preventDefault();

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
              var command = new RowOperationCommand(operation, activeTable);

              Commands.executeCommand(command);
            } else { //up
              activeTable.selectCellBefore();
            }
            break;
          case 40:
            if(e.shiftKey) { //shift+down
              var operation = new MoveDownOperation();
              var command = new RowOperationCommand(operation, activeTable);

              Commands.executeCommand(command);
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
            var operation = new LayoutAddRowOperation();
            var command = new RowOperationCommand(operation, activeTable);

            Commands.executeCommand(command);
            break;
          case 67: //c
            var operation = new LayoutAddColumnsOperation();
            var command = new RowOperationCommand(operation, activeTable);

            Commands.executeCommand(command);
            break;
          case 72: //h
            var operation = new LayoutAddHtmlOperation();
            var command = new RowOperationCommand(operation, activeTable);

            Commands.executeCommand(command);
            break;
          case 88:
            if(e.shiftKey) { //shift+x
              var operation = new DeleteOperation();
              var command = new RowOperationCommand(operation, activeTable);

              Commands.executeCommand(command);
            }
            break;
          case 68:
            if(e.shiftKey) { //shift+d
              var operation = new (activePanel.getDuplicateOperation())();
              var command = new RowOperationCommand(operation, activeTable);

              Commands.executeCommand(command);
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
    var url = location.origin + wd.cde.endpoints.getNewDashboardUrl() + '?ts=' + (new Date()).getTime();

    if(!$("div.cdfdd-title-status").hasClass("dirtyStatus") || Commands.executedCommands.length === 0) {
      location.assign(url);
      return;
    }

    var message = 'Are you sure you want to start a new dashboard?<br/><span class="description">Unsaved changes will be lost.</span>';

    var content = '' +
        '<div class="popup-header-container">\n' +
        '  <div class="popup-title-container">New Dashboard</div>\n' +
        '</div>\n' +
        '<div class="popup-body-container">' + message + '</div>';

    $.prompt(content, {
      buttons: {
        Ok: true,
        Cancel: false
      },
      top: "40px",
      prefix: "popup",
      loaded: function() {
        var $popup = $(this);
        $popup.addClass('settings-popup');
        CDFDDUtils.movePopupButtons($popup);
      },
      callback: function(v, m, f) {
        if(v) {
          location.assign(url);
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
      NotifyBarUtils.infoNotifyBar("Need to save an initial dashboard before previews are available.");
      return;
    }

    var separator = CDFDDFileName.indexOf( '%2F' ) != -1 ? '%2F' /* separator in a uri encoded path */ : '/' /*  default non encoded path */ ;

    var fullPath = CDFDDFileName.split( separator );
    var solution = fullPath[1];
    var path = fullPath.slice(2, fullPath.length - 1).join( separator );
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

  reload: function() {
    var self = this;

    if(!$("div.cdfdd-title-status").hasClass("dirtyStatus") || Commands.executedCommands.length === 0) {
      self.logger.warn("Reloading dashboard... ");
      window.location.reload();
      return;
    }

    var message = 'Are you sure you want to reload?<br><span class="description">Unsaved changes will be lost.</span>';

    var content = '' +
        '<div class="popup-header-container">\n' +
        '  <div class="popup-title-container">Reload</div>\n' +
        '</div>\n' +
        '<div class="popup-body-container">' + message + '</div>';

    $.prompt(content, {
      buttons: {
        Ok: true,
        Cancel: false
      },
      top: "40px",
      prefix: "popup",
      loaded: function() {
        var $popup = $(this);
        $popup.addClass('settings-popup');
        CDFDDUtils.movePopupButtons($popup);
      },
      callback: function(v, m, f) {
        if(v) {
          self.logger.warn("Reloading dashboard... ");
          window.location.reload();
        }
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
        myself = this;

    settingsData.styles = [];
    this.styles = SettingsHelper.getStyles(wcdf, this);
    var selectedStyle = SettingsHelper.getSelectedStyle(wcdf);
    _.each(this.styles, function(obj) {
      settingsData.styles.push({
        style: obj,
        selected: selectedStyle == obj
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

    var extraOptions = SettingsHelper.getExtraPromptContent();

    var content = '' +
        '<div class="popup-input-container">\n' +
        '  <span class="popup-label">Title</span>\n' +
        '  <input class="popup-text-input" id="titleInput" placeholder="Insert Text..." type="text" value="{{title}}"/>' +
        '</div>\n' +

        '{{#widget}}' +
        '<div class="popup-input-container">\n' +
        '  <span class="popup-label">Widget Name</span>' +
        '  <input class="popup-text-input" id="widgetNameInput" placeholder="Insert Text..." type="text" value="{{widgetName}}"/>' +
        '</div>\n' +
        '{{/widget}}' +

        '<div class="popup-input-container">\n' +
        '  <span class="popup-label">Author</span>' +
        '  <input class="popup-text-input" id="authorInput" placeholder="Insert Text..." type="text" value="{{author}}"/>' +
        '</div>\n'+

        '<div class="popup-input-container">\n' +
        '  <span class="popup-label">Description</span>' +
        '  <textarea class="popup-text-input" placeholder="Insert Text..." id="descriptionInput">{{description}}</textarea>\n' +
        '</div>\n' +

        '<div class="popup-input-container">\n' +
        '  <span class="popup-label">Style</span>\n' +
        '    <select class="popup-select" id="styleInput">\n' +
        '    <option value=""></option>\n' +
        '      {{#styles}}' +
        '      <option value="{{style}}" {{#selected}}selected{{/selected}}>{{style}}</option>\n' +
        '      {{/styles}}' +
        '    </select>' +
        '</div>\n' +

        '<div class="popup-input-container">\n' +
        '  <span class="popup-label">Dashboard Type</span>\n' +
        '    <select class="popup-select" id="rendererInput">\n' +
        '      <option value=""></option>\n' +
        '      {{#renderers}}' +
        '      <option value="{{renderer}}" {{#selected}}selected{{/selected}}>{{renderer}}</option>\n' +
        '      {{/renderers}}' +
        '    </select>' +
        '</div>\n' +

        extraOptions +

        '{{#widget}}' +
        '<div class="popup-input-container">\n' +
        '  <span class="popup-label">Widget Parameters</span>\n' +
        '  <span id="widgetParameters">' +
        '    <div style=" max-height: 110px; overflow: auto; ">' +
        '      {{#parameters}}' +
        '      <input type="checkbox" name="{{parameter}}" value="{{parameter}}" {{#selected}}checked{{/selected}} style=" position: relative; top: 4px; "><span>{{parameter}}</span><br>\n' +
        '      {{/parameters}}' +
        '    </div>' +
        '  </span>' +
        '</div>\n' +
        '{{/widget}}';

    var rv = "";
    var re = new RegExp("MSIE ([0-9]{1,}[\.0-9]{0,})");
    if(re.exec(navigator.userAgent) != null) {
      rv = parseFloat(RegExp.$1) < 10 ? "ie8" : "";
    }

    var contentWrapper = '' +
        '<div class="popup-header-container">\n' +
        '  <div class="popup-title-container">Settings</div>\n' +
        '</div>\n' +
        '<div class="popup-body-container layout-popup ' + rv + '">\n' + content + '</div>';

    content = Mustache.render(contentWrapper, settingsData);
    $.prompt(content, {
      buttons: {
        Save: true,
        Cancel: false
      },
      top: "40px",
      prefix: "popup",

      loaded: function() {
        var $popup = $(this);

        $popup.addClass('settings-popup save-settings');
        CDFDDUtils.buildPopupSelect($('.popup-select'), {});
        CDFDDUtils.movePopupButtons($popup);
      },

      submit: function(save) {
        if (save) {
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
          SettingsHelper.callExtraContentSubmit(myself, wcdf);
        }
      },
      callback: function(v, m, f) {
        if(v) {
          /* Validations */
          var validInputs = true;
          var message = "";
          var content = "";

          if(wcdf.widget) {
            if(!/^[a-zA-Z0-9_]*$/.test(wcdf.widgetName)) {

              message = 'Invalid characters in widget name. Only alphanumeric characters and \'_\' are allowed.';
              content = '' +
                  '<div class="popup-header-container">\n' +
                  '  <div class="popup-title-container">Settings Validations</div>\n' +
                  '</div>\n' +
                  '<div class="popup-body-container">' + message + '</div>';

              $.prompt(content, {
                top: "40px",
                prefix: "popup",
                loaded: function() {
                  CDFDDUtils.movePopupButtons($(this));
                }
              });
              validInputs = false;
            } else if(wcdf.widgetName.length == 0) {
              if(wcdf.title.length == 0) {

                message = 'No widget name provided. Tried to use title instead but title is empty.';
                content = '' +
                    '<div class="popup-header-container">\n' +
                    '  <div class="popup-title-container">Settings Validations</div>\n' +
                    '</div>\n' +
                    '<div class="popup-body-container">' + message + '</div>';

                $.prompt(content, {
                  top: "40px",
                  prefix: "popup",
                  loaded: function() {
                    CDFDDUtils.movePopupButtons($(this));
                  }
                });
                validInputs = false;
              } else {
                wcdf.widgetName = wcdf.title.replace(/[^a-zA-Z0-9_]/g, "");

                if(wcdf.widgetName.length == 0) {

                  message = 'No widget name provided. Unable to use the title, too many invalid characters.';
                  content = '' +
                      '<div class="popup-header-container">\n' +
                      '  <div class="popup-title-container">Settings Validations</div>\n' +
                      '</div>\n' +
                      '<div class="popup-body-container">' + message + '</div>';

                  $.prompt(content, {
                    top: "40px",
                    prefix: "popup",
                    loaded: function() {
                      CDFDDUtils.movePopupButtons($(this));
                    }
                  });
                  validInputs = false;
                }
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

  combinedSaveAs: function() {

    var selectedTitle = "",
        selectedDescription = "",
        selectedFile = "",
        selectedFolder = "";
    var myself = this;

    var content = '' +
        '<div id="chooseFormatWrapper" class="popup-input-container input-pair">' +
        '  <span class="popup-label">Choose Format</span>' +

        '  <div class="clearfix">' +
        '    <div class="popup-radio-container">' +
        '      <input type="radio" name="saveAsRadio" value="dashboard" id="dashRadio" checked>' +
        '      <label class="popup-input-label" for="dashRadio">Dashboard</label>' +
        '    </div>' +

        '    <div class="popup-radio-container last">' +
        '      <input type="radio" name="saveAsRadio" value="widget" id="widgetRadio">' +
        '      <label class="popup-input-label" for="widgetRadio">Widget</label>' +
        '    </div>' +
        '  </div>' +
        '</div>' +

        '<div id="saveAsFEContainer" class="popup-input-container">' +
        '  <span class="popup-label">Choose Folder</span>' +
        '  <div id="saveAsFolderExplorer" class="folderexplorer"></div>' +
        '</div>' +

        '<div class="popup-input-container">' +
        '  <span class="popup-label">File Name*</span>' +
        '  <input class="popup-text-input" id="fileInput" placeholder="Insert Text..." type="text" value=""/>' +
        '</div>' +

        '<div class="popup-input-container widgetField" style="display: none">' +
        '  <span class="popup-label">Widget Name*</span>' +
        '  <input class="popup-text-input" id="componentInput" placeholder="Insert Text..." type="text" value=""/>' +
        '</div>' +

        '<hr class="saveHr">' +

        '<div class="popup-input-container">' +
        '  <span class="popup-label">Title</span>' +
        '  <input class="popup-text-input" id="titleInput" type="text" placeholder="Insert Text..." value="' + selectedTitle + '"/>' +
        '</div>' +

        '<div class="popup-input-container bottom">' +
        '  <span class="popup-label">Description</span>' +
        '  <input class="popup-text-input" id="fileInput" type="text" placeholder="Insert Text..." value="' + selectedDescription + '"/>' +
        '</div>';

    var rv = "";
    var re = new RegExp("MSIE ([0-9]{1,}[\.0-9]{0,})");
    if(re.exec(navigator.userAgent) != null) {
      rv = parseFloat(RegExp.$1) < 10 ? "ie8" : "";
    }

    var contentWrapper = '' +
        '<div class="popup-header-container">\n' +
        '  <div class="popup-title-container">Save as</div>\n' +
        '</div>\n' +
        '<div class="popup-body-container layout-popup ' + rv + '">' + content + '</div>';

    $.prompt(contentWrapper, {
      prefix: "popup",
      top: "40px",
      buttons: {
        Ok: 1,
        Cancel: 0
      },
      loaded: function() {

        var $popup = $(this);
        $popup.addClass('settings-popup save-dashboard');
        CDFDDUtils.movePopupButtons($popup);

        var widgetField = $(".widgetField");

        $('#saveAsFolderExplorer').fileTree({
          root: '/',
          script: SolutionTreeRequests.getExplorerFolderEndpoint(CDFDDDataUrl) + "?fileExtensions=.wcdf&access=create",
          expandSpeed: 1000,
          collapseSpeed: 1000,
          multiFolder: false,
          folderClick: function(obj, folder) {
            if($(".selectedFolder").length > 0) $(".selectedFolder").attr("class", "");
            $(obj).attr("class", "selectedFolder");
            selectedFolder = folder;
          }
        }, function(file) {

          $("#fileInput").val(file.replace(selectedFolder, ""));
          selectedFile = $("#fileInput").val();
        });

        $("#dashRadio").click(function(event) {
          $("#saveAsFEContainer").show();
          $popup.addClass('save-dashboard').removeClass('save-widget');
          widgetField.hide();
        });

        $("#widgetRadio").click(function(event) {
          $("#saveAsFEContainer").hide();
          $popup.removeClass('save-dashboard').addClass('save-widget');
          widgetField.show();
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

          // validation flag
          var validationFlag = true,
              validationMsg = '';

          // remove leading and trailing white spaces
          selectedFile = $('#fileInput').val().replace(/^\s+/, "").replace(/\s+$/, "");

          // validate file name using RESERVED_CHARS_REGEX_PATTERN from webcontext (added colon char)
          // TODO: use webcontext when it becomes available, include the colon char
          if(!selectedFile || selectedFile == ".wcdf" || /.*[\/\\\t\r\n:]+.*/.test(selectedFile)) {

            validationMsg += (validationMsg !== '' ? '<br>' : '') + 'Please insert a valid file name.';
            validationFlag = false;

          } else if(!/(\.wcdf)$/.test(selectedFile)) {

            // append file extension
            selectedFile += ".wcdf";
          }

          /**
           * Save as Dashboard
           */
          if($('input[name=saveAsRadio]:checked').val() === "dashboard") {

            selectedTitle = isValidField($("#titleInput").val()) ? $("#titleInput").val() : cdfdd.getDashboardWcdf().title;
            selectedDescription = isValidFieldNotEmpty($("#descriptionInput").val()) ? $("#descriptionInput").val() : cdfdd.getDashboardWcdf().description;

            // Validate Folder Name
            if(selectedFolder.length == 0) {

              validationMsg += (validationMsg !== '' ? '<br>' : '') + 'Please choose destination folder.';
              validationFlag = false;
            }

            if(validationFlag) {

              CDFDDFileName = selectedFolder + selectedFile;
              cdfdd.dashboardData.filename = CDFDDFileName;
              var stripArgs = {
                needsReload: false
              };

              var saveAsParams = {
                operation: "saveas",
                file: selectedFolder + selectedFile,
                title: selectedTitle,
                description: selectedDescription,
                cdfstructure: JSON.stringify(cdfdd.strip(cdfdd.dashboardData, stripArgs), "", 1)
              };

              SaveRequests.saveAsDashboard(saveAsParams, selectedFolder, selectedFile, myself);

            } else {
              var validationContent = '' +
                  '<div class="popup-header-container">\n' +
                  '  <div class="popup-title-container"></div>\n' +
                  '</div>\n' +
                  '<div class="popup-body-container">' + validationMsg + '</div>';

              $.prompt(validationContent, {
                top: "40px",
                prefix: "popup",
                loaded: function() {
                  CDFDDUtils.movePopupButtons($(this));
                }
              });
            }
          }

          /**
           * Save as Widget
           */
          else if($('input[name=saveAsRadio]:checked').val() == "widget") {

            selectedFolder = wd.helpers.repository.getWidgetsLocation();
            selectedTitle = isValidField($("#titleInput").val()) ? $("#titleInput").val() : cdfdd.getDashboardWcdf().title;
            selectedDescription = isValidFieldNotEmpty($("#descriptionInput").val()) ? $("#descriptionInput").val() : cdfdd.getDashboardWcdf().description;
            var selectedWidgetName = $("#componentInput").val();

            // Validate Widget Name
            if(!/^[a-zA-Z0-9_]*$/.test(selectedWidgetName)) {

              validationMsg += (validationMsg !== '' ? '<br>' : '') + 'Invalid widget name. Only alphanumeric characters and \'_\' are allowed.';
              validationFlag = false;

            } else if(selectedWidgetName.length == 0) {

              // Try to use title
              if(selectedTitle.length == 0) {

                validationMsg += (validationMsg !== '' ? '<br>' : '') + 'No widget name provided. Unable to use empty title.';
                validationFlag = false;

              } else {

                selectedWidgetName = selectedTitle.replace(/[^a-zA-Z0-9_]/g, "");

                if(selectedWidgetName.length == 0) {

                  validationMsg += (validationMsg !== '' ? '<br>' : '') + 'No widget name provided. Unable to use the title, too many invalid characters.';
                  validationFlag = false;
                }
              }
            }

            if(validationFlag) {

              CDFDDFileName = selectedFolder + selectedFile;
              myself.dashboardData.filename = CDFDDFileName;

              var saveAsParams = {
                operation: "saveas",
                file: selectedFolder + selectedFile,
                title: selectedTitle,
                description: selectedDescription,
                cdfstructure: JSON.stringify(myself.strip(myself.dashboardData, stripArgs), null, 1),
                widgetName: selectedWidgetName
              };

              SaveRequests.saveAsWidget(saveAsParams, selectedFolder, selectedFile, myself);

            } else {
              var validationContent = '' +
                  '<div class="popup-header-container">\n' +
                  '  <div class="popup-title-container"></div>\n' +
                  '</div>\n' +
                  '<div class="popup-body-container">' + validationMsg + '</div>';

              $.prompt(validationContent, {
                top: "40px",
                prefix: "popup",
                loaded: function() {
                  CDFDDUtils.movePopupButtons($(this));
                }
              });
            }
          }

          return false;
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
    var cggUrl = Cgg.getCggDrawUrl() + "?";

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

      var isCggDialComponent = e.type == "ComponentscggDial";
      var dialProperties = {};
      e.properties.map(function(p) {
        switch(p.name) {
          case 'title':
            title = p.value;
            break;
          case 'name':
            componentName = p.value;
            break;
          case 'intervals':
            dialProperties.paramscale = $.parseJSON(p.value);
            break;
          case 'colors':
            dialProperties.paramcolors = $.parseJSON(p.value);
            break;
          case 'width':
            dialProperties.width = p.value;
            break;
          case 'height':
            dialProperties.height = p.value;
            break;
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

      var fullCggUrl = '';
      if(isCggDialComponent) {
        fullCggUrl = cggUrl + 'script=/system/pentaho-cdf-dd/resources/custom/components/cgg/charts/dial.js&'
            + $.param(dialProperties);
      } else {
        // append dashboard name with component name and extension (.js)
        var script = "script=" + CDFDDFileName.substring(0, CDFDDFileName.lastIndexOf("/")) + "/"
            + CDFDDFileName.replace(/^.*[\\\/]/, '').split('.')[0] + '_' + componentName + ".js";
        fullCggUrl = cggUrl + script;
      }
      fullCggUrl += "&outputType=png";

      $("<div class='urlPreviewer collapsed'><input type='text' value = '" + fullCggUrl + "'></input></div>").appendTo(section);
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
  },

  markAsClean: function() {
    $('div.cdfdd-title-status').removeClass('dirtyStatus');
    Commands.cleanExecutedCommands();
  },

  markAsDirty: function() {
    $('div.cdfdd-title-status').addClass('dirtyStatus');
  },

  movePopupButtons: function(container) {
    var headerContainer = container.find('.popup-header-container');
    var popupButtons = container.find('.popupbuttons');

    headerContainer.append(popupButtons);
    popupButtons.show();
  },

  buildPopupSelect: function(selectElem, override) {
    var options = $.extend({
      minimumResultsForSearch: Infinity,
      dropdownCssClass: 'popup-select-dropdown',
      placeholder: 'Select...'
    }, override);

    if(selectElem.is(':empty')) {
      selectElem.append('<option value="">');
    }

    selectElem.select2(options);
  }
});


var NotifyBarUtils = {
  successNotifyBar: function(message) {
    this.notifyBar(message, 'success');
  },

  errorNotifyBar: function(message, error) {
    if(!error) {
      message += ": " + error;
    }

    this.notifyBar(message, 'error');
  },

  infoNotifyBar: function(message) {
    this.notifyBar(message, 'info');
  },

  notifyBar: function(message, type) {
    var notifyObject = $("#notifyBar");
    var notifyHtml = '<div class="notify-bar-icon"></div><div class="notify-bar-message">' + message + '</div>';

    if(!notifyObject.length) {
      notifyObject = $('<div id="notifyBar" class="notify-bar"></div>');
    } else {
      this.cleanStatus();
    }

    switch(type) {
      case 'success':
        notifyObject.addClass('notify-bar-success');
        break;
      case 'error':
        notifyObject.addClass('notify-bar-error');
        break;
      case 'info':
        notifyObject.addClass('notify-bar-info');
        break;
    }

    $.notifyBar({
      jqObject: notifyObject,
      html: notifyHtml,
      delay: 1500
    });
  },

  cleanStatus: function() {
    $("#notifyBar")
        .removeClass('notify-bar-success')
        .removeClass('notify-bar-error')
        .removeClass('notify-bar-info')
  }
};

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
