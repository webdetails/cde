/*!
 * Copyright 2002 - 2015 Webdetails, a Pentaho company.  All rights reserved.
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


// TODO: review usage of wd.cdv.cdv


define([
  'cdf/components/BaseComponent',
  'cdf/Logger',
  'cdf/lib/jquery',
  'cdf/lib/underscore',
  'cdf/lib/mustache',
  './TextEditorComponent',
  './PopupComponent',
  'css!./TextEditorComponent'],
  function(
    BaseComponent,
    Logger,
    $,
    _,
    Mustache,
    TextEditorComponent,
    PopupComponent) {

  /*
   *  Popup text editor component 
   */

  var PopupTextEditorComponent = BaseComponent.extend({

    $ph: undefined,
    isInitialized: false,
    textEditor: undefined,
    textEditorPopupId: "popupTextEditorId",
    isQueryPreviewShown: false,
    testPromptPopup: undefined,
    $testPromptPopupObj: undefined,
    defaultButtons: [
    {
      clazz: "run",
      label: "Preview Test", 
      callback: function() {
        this.runTest();
      }
    },   
    {
      clazz: "previewQuery",
      label: "Query results", 
      callback: function() {
        this.toggleQueryResults();
      }
    },
    {
      clazz: "close",
      label: "Close", 
      callback: function() {
        this.hide();
      }
    }],
        
    /* // Default settings
     * file: the file to edit
     */ 

    initialize: function() {
        
      Logger.log("Initializing PopupTextEditorComponent");
      this.isInitialized = true;  
     
      // We need to create a placeholder for this
      this.$ph = $("#" + this.textEditorPopupId);
      if(this.$ph.length > 0) {
        // we found one already?
        Logger.log("[PopupTextEditorComponent] Unexpected - Found an element with id " + this.popupTextEditorDefautlId);
      } else {
        this.$ph = $("<div id='" + this.textEditorPopupId + "'></div>").appendTo("body");
      }
             
      // Also generate a textEditorComponent
      this.textEditor = new TextEditorComponent({
        name: "popupInnerTextEditorComponent", 
        type: "TextEditorComponent", 
        file: undefined,  // will be set later
        htmlObject: this.textEditorPopupId,
        extraButtons: this.getButtons(),
        saveCallback: this.saveCallback
              
      });

      this.dashboard.addComponents([this.textEditor]);
     
    },

    update: function() {

      var myself = this;
      myself.isQueryPreviewShown = false;
      
      if(!myself.isInitialized) {
        myself.initialize();
      }

      // Update the text component
      
      myself.textEditor.update();

    },
    
    show: function() {

      this.$ph.find(">div.textEditorComponent").height($(window).height());
      this.$ph.slideDown();

    },

    hide: function() {
        
      this.$ph.slideUp();

    },

    runTest: function() {

      var env = this.setupEnvironment();
      if(env) {
        var test = this.getTestToOperate(env, this.runTestCallback, $("button.previewQuery", this.$ph));            
      }

    },
    
    runTestCallback: function(env, test) {

      var myself = this;
      myself.textEditor.notify("Running test...");
      env.cdv.runTest(test, { 
        callback: function(result) {
          myself.textEditor.notify(result.getTestResultDescription());
        }
      });

    },
    
    toggleQueryResults: function() {

      if(this.isQueryPreviewShown) {
        // Hide it
        this.isQueryPreviewShown = this.textEditor.toggleRightPanel();
        return;
      }
      
      
      // Ok - Try to open it and run the test
      var env = this.setupEnvironment();
      if(env) {
        var test = this.getTestToOperate(env, this.toggleQueryResultsCallback, $("button.run", this.$ph));
      }

    },
    
    toggleQueryResultsCallback: function(env, test) {
        
      var myself = this;
      myself.textEditor.notify("Running query...");

      env.cdv.executeQuery(test, null, function(test,opts,queryResult) {
        myself.textEditor.notify("Queries ran in " + queryResult.duration + "ms");
            
        myself.textEditor.getRightPanel()
          .html("<pre>" + JSON.stringify(queryResult.resultset, undefined, 2) + "</pre>");
        
        myself.isQueryPreviewShown = myself.textEditor.toggleRightPanel();
        Logger.log("Toggling!");
          
      });
        
    },

    getButtons: function() {
        
      var myself = this;
      
      var _extraButtons = myself.extraButtons || [];
      
      _.chain(myself.defaultButtons).each(function(b) {
        b.callback = _.bind(b.callback, myself);
      })
      return myself.defaultButtons.concat(_extraButtons);
    },

    setFile: function(_file) {
      this.file = _file;
      this.textEditor.setFile(_file);
    },
    
    setupEnvironment: function() {
        
      // Get source
      
      var src = this.textEditor.getEditorWindow().editor.getContents();
      debugger;
      var mask = {
        cdv: wd.cdv.cdv({isServerSide: false})
      };
      
      // mask global properties 
      for(p in this) {
        mask[p] = undefined;
      }
      // execute script in private context
      try {
        (new Function( "with(this) { " + src + "}")).call(mask);
      } catch(err) {
        Logger.err(err);
        return null;
      }
      
      return mask;
        
    },
  
    getTestToOperate: function(env, operationCallback, target) {
        
      var myself = this;
              
      // How many tests do we have? If only one, return
      
      var flattenedTests = env.cdv.listTestsFlatten().sort(function(a, b) {
        return (a.group + a.name) >=  (b.group + b.name)
      });
      
      
      if(flattenedTests.length == 1) {
        // return it
        operationCallback
          .call(myself, env, env.cdv.getTest(flattenedTests[0].group, flattenedTests[0].name));
        return;
      }
      
      
      // We need to prompt for the test
      if(!myself.testPromptPopup) {
 
        // Generate a popup component for us
        myself.testPromptPopup = new PopupComponent({
          name: "testPromptPopup", 
          type:"PopupComponent", 
          htmlObject: 'testPromptPopupObj',
          gravity: "S",
          draggable: false,
          closeOnClickOutside: true
        });
        
        myself.$testPromptPopupObj = $("<div id='testPromptPopupObj'></div>").appendTo("body");
        
        myself.dashboard.addComponents([myself.testPromptPopup]);
        myself.testPromptPopup.update();
        
        // Allow customization
        myself.$testPromptPopupObj.parent("div.popupComponent").addClass("testPromptPopup");

      }
      
      var template = '<div class="testChooserWrapper">'
        + '<div class="title">Multiple tests found. Choose the one you want:</div>'
        +'<div class="testChooserButtons">{{#tests}}<button> {{group}} - {{name}}</button>{{/tests}}</div></div>';
      this.$testPromptPopupObj.html(Mustache.render(
        template,
        {
          tests: flattenedTests 
        }));
      

      this.$testPromptPopupObj.off("click","button");
      this.$testPromptPopupObj.on("click","button",function(evt) {
        var idx = $(this).prevAll("button").length;
        operationCallback.call(myself, env, env.cdv.getTest(flattenedTests[idx].group, flattenedTests[idx].name));
       
        myself.testPromptPopup.hide();
      })
      
      this.testPromptPopup.popup(target);
      
      return;
        
    }

  });

  return PopupTextEditorComponent;

});
