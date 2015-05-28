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

define([
  'cdf/components/BaseComponent',
  'cdf/Logger',
  'cdf/lib/jquery',
  'amd!cdf/lib/underscore',
  'cdf/lib/mustache',
  './TextEditorComponentExt',
  'css!./TextEditorComponent'],
  function(BaseComponent, Logger, $, _, Mustache, TextEditorComponentExt) {

  /*
   *  Text editor component
   */

  var TextEditorComponent = BaseComponent.extend({

    $ph: undefined,
    $rightPanel: undefined,
    isRightPanelShown: false,
    isInitialized: false,
    externalEditor: undefined,
    defaultButtons: [
    {
      clazz: "save",
      label: "Save",
      callback: function() {
        this.save();
        if(typeof this.saveCallback === "function") {
          this.saveCallback();
        }
      }
    }
    ],

    template: function() {
      return "<div class='textEditorComponent'><div class='textEditorControls'>"+
      "<div class='textEditorFile'><span class='fileLabel'>File: </span>{{file}}</div>"+
      "<div class='textEditorButtons'>{{#buttons}}<button class='{{clazz}}'>{{label}}</button>{{/buttons}}</div>" +
      "</div><div class='textEditorNotification'><span class='textEditorNotificationMsg'>Test</span></div>"+
      "<div class='textEditorRightPanel'></div>"+
      "<div class='textEditorIframeContainer'><div class='textEditorIframe'><iframe seamless='true' marginheight='0'></iframe></div>"+
      "</div>";
    },

    /* // Default settings
     * file: the file to edit
     */

    initialize: function() {

      Logger.log("Initializing TextEditorComponent");
      this.isInitialized = true;

      // Do we have an htmlObject? if no, create one. If yes, setup placeholder
      if(this.htmlObject) {
        this.$ph = $("#" + this.htmlObject);
      } else {
        this.$ph = $("<div id='textEditorDefautlId'></div>").appendTo("body");
      }

    },

    update: function() {

      var myself = this;

      //TODO:
      if(myself.parameter) {
        myself.setFile(myself.dashboard.getParameterValue(myself.parameter));
      }

      if(!myself.isInitialized) {
        myself.initialize();
      }

      myself.isRightPanelShown = false;

      // Render the correct structure
      var buttons = myself.getButtons();

      myself.$ph.html(Mustache.render(
        myself.template(),
        {
          file: myself.file || "Unknown file",
          buttons: buttons
        }));

      // bind
      myself.$ph.find(".textEditorControls").on("click", "button", function() {
        var $this = $(this);
        var idx = $this.prevAll("button").length;

        buttons[idx].callback(arguments);
      });

      if(myself.file) {
        myself.loadFile();
      }

    },

    getButtons: function() {

      var myself = this;
      var _extraButtons = myself.extraButtons || [];
      _.chain(myself.defaultButtons).each(function(b) {
        b.callback = _.bind(b.callback, myself);
      });
      return myself.defaultButtons.concat(_extraButtons);

    },

    setFile: function(_file) {
      this.file = _file;
    },

    getFile: function() {
      return this.file;
    },

    loadFile: function() {

      var myself = this;

      // Disable button
      $('button.save', myself.$ph).attr('disabled', true);

      myself.externalEditor = $('iframe', myself.$ph);

      var headerHeight = $('.textEditorControls', myself.$ph).height()
        + $('.textEditorNotification', myself.$ph).height();

      var editorHeight = myself.$ph.height() - headerHeight - 5;

      myself.externalEditor.height(editorHeight);

      myself.externalEditor.load(function() {

        var editorEnv = myself.getEditorWindow();
        editorEnv.listeners.onStatusUpdate = myself.setDirty;
        editorEnv.listeners.notify = function(msg, type) {
          myself.notify(msg);
        };

        $('#notifications').hide();
      });

      myself.externalEditor.attr('src', TextEditorComponentExt.getUrl()
        + 'path=' + this.file + '&theme=ace/theme/eclipse&editorOnly=true');// &width='+width );

    },

    notify: function(msg, level /*todo*/) {
      var $notifications = this.$ph.find(".textEditorNotificationMsg");
      $notifications.text(msg);
      $notifications.show().delay(4000).fadeOut('slow');
    },

    setDirty: function(isDirty) {
      $('button.save',this.$ph).attr('disabled', !isDirty);
    },

    getEditorWindow: function() {
      return this.externalEditor[0].contentWindow;
    },

    save: function() {
      this.getEditorWindow().save();
    },

    getRightPanel: function() {
      return this.$ph.find(".textEditorRightPanel");
    },

    toggleRightPanel: function() {
      this.getRightPanel().toggle();
      this.isRightPanelShown = !this.isRightPanelShown;

      // Force a resize on ace:
      this.getEditorWindow().editor.getEditor().resize();

      return this.isRightPanelShown;
    }

  });

  return TextEditorComponent;

});
