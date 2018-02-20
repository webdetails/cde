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
        callback: function(){
            this.save();
            if(typeof this.saveCallback === "function" ){
                this.saveCallback();
            }
            
        }
    }
    ],
    template: function(){ 
        return "<div class='textEditorComponent'><div class='textEditorControls'>"+
        "<div class='textEditorFile'><span class='fileLabel'>File: </span>{{file}}</div>"+
        "<div class='textEditorButtons'>{{#buttons}}<button class='{{clazz}}'>{{label}}</button>{{/buttons}}</div>" +
        "</div><div class='textEditorNotification'><span class='textEditorNotificationMsg'>Test</span></div>"+
        "<div class='textEditorRightPanel'></div>"+
        "<div class='textEditorIframeContainer'><div class='textEditorIframe'><iframe seamless='true' marginheight='0'></iframe></div>"+
        "</div>"
    },
    
    /* // Default settings
     * file: the file to edit
     */ 

    initialize: function(){
        
        Dashboards.log("Initializing TextEditorComponent")  
        this.isInitialized = true;  
       
        // Do we have an htmlObject? if no, create one. If yes, setup placeholder
        if(this.htmlObject){
            this.$ph = $("#" + this.htmlObject);
        }
        else{
            this.$ph = $("<div id='textEditorDefautlId'></div>").appendTo("body");
        }
        
    
     
    },

    update: function(){
        
        
        var myself = this;

        //TODO:
        if (this.parameter){
            this.setFile( Dashboards.getParameterValue(this.parameter));
        }
        
        if(!this.isInitialized){
            myself.initialize();
        }

        this.isRightPanelShown = false;
        
        
        // Render the correct structure
        var buttons = this.getButtons();
        
        this.$ph.html(Mustache.render(this.template(), {
            file: this.file || "Unknown file", 
            buttons:buttons
        }));
    
        // bind
        this.$ph.find(".textEditorControls").on("click","button",function(){
            var $this = $(this);
            var idx = $this.prevAll("button").length;

            buttons[idx].callback(arguments);
        })
        
        if(this.file){
            this.loadFile();
        }
        
    //alert("Ok!");
       
        
    },
    
    getButtons: function(){
        
        var myself = this;
        var _extraButtons = this.extraButtons || [];
        _.chain(this.defaultButtons).each(function(b){
            b.callback = _.bind(b.callback, myself);
        })
        return this.defaultButtons.concat(_extraButtons);
        
    },
    
    setFile: function(_file){
        this.file = _file;
    },
    
    getFile: function(){
        return this.file;
    },
    
    loadFile: function() {

        var myself=this;
        

        // Disable button
        $('button.save',this.$ph).attr('disabled', true);

        this.externalEditor = $('iframe',this.$ph);
        var headerHeight = $('.textEditorControls', this.$ph).height() + $('.textEditorNotification', this.$ph).height();
        var editorHeight = this.$ph.height() - headerHeight - 5;
        this.externalEditor.height(editorHeight);

        this.externalEditor.load(function()
        {

            var editorEnv = myself.getEditorWindow();
            editorEnv.listeners.onStatusUpdate = myself.setDirty;
            editorEnv.listeners.notify = function(msg, type){
                myself.notify(msg);
            }
      
            $('#notifications').hide();
        });

        this.externalEditor.attr('src',"/pentaho" + wd.helpers.editor.getUrl()+'path=' + this.file + '&theme=ace/theme/eclipse&editorOnly=true');// &width='+width );
        
    },
    
    notify: function(msg, level /*todo*/){
        
        var $notifications = this.$ph.find(".textEditorNotificationMsg");
        $notifications.text(msg);
        $notifications.show().delay(4000).fadeOut('slow');
    },
        
        
    setDirty: function(isDirty){
        $('button.save',this.$ph).attr('disabled', !isDirty);
    },
  
    getEditorWindow: function(){
        return this.externalEditor[0].contentWindow;
    },


    save: function(){
        
        this.getEditorWindow().save();

    },
    
    
    getRightPanel: function(){
        
        return this.$ph.find(".textEditorRightPanel");
        
    },
    
    toggleRightPanel: function(){
        
        this.getRightPanel().toggle();
        this.isRightPanelShown = !this.isRightPanelShown;
        
        // Force a resize on ace:
        this.getEditorWindow().editor.getEditor().resize();
        
        return this.isRightPanelShown;
    }
    


});



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
        callback: function(){
            this.runTest();
        }
    },   
    {
        clazz: "previewQuery",
        label: "Query results", 
        callback: function(){
            this.toggleQueryResults();
        }
    },
    {
        clazz: "close",
        label: "Close", 
        callback: function(){
            this.hide();
        }
    }       
    ],
        
    /* // Default settings
     * file: the file to edit
     */ 

    initialize: function(){
        
        Dashboards.log("Initializing PopupTextEditorComponent")  
        this.isInitialized = true;  
       
        // We need to create a placeholder for this
        this.$ph = $("#"+this.textEditorPopupId);
        if(this.$ph.length > 0){
            // we found one already?
            Dashboards.log("[PopupTextEditorComponent] Unexpected - Found an element with id " + this.popupTextEditorDefautlId)
        }
        else{
            this.$ph = $("<div id='"+this.textEditorPopupId+"'></div>").appendTo("body");
        }
               
        // Also generate a textEditorComponent
        this.textEditor = {
            name: "popupInnerTextEditorComponent", 
            type: "textEditor", 
            file: undefined,  // will be set later
            htmlObject: this.textEditorPopupId,
            extraButtons: this.getButtons(),
            saveCallback: this.saveCallback
                
        };
        
        
        Dashboards.addComponents([this.textEditor]);
     
    },

    update: function(){
        
        
        var myself = this;
        this.isQueryPreviewShown = false;
        
        
        if(!this.isInitialized){
            myself.initialize();
        }

        // Update the text component
        
        this.textEditor.update();

        
    //alert("Ok!");
       
        
    },
    
    show: function(){

        this.$ph.find(">div.textEditorComponent").height($(window).height());
        this.$ph.slideDown();

    },
    
    
    hide: function(){
        
        this.$ph.slideUp();

    },

    runTest: function(){
        
        var env = this.setupEnvironment();
        if(env){
            var test = this.getTestToOperate(env,this.runTestCallback, $("button.previewQuery",this.$ph));            
        }
        
        
    },
    
    runTestCallback: function(env, test){

        var myself = this;
        this.textEditor.notify("Running test...");
        env.cdv.runTest(test, { 
            callback: function(result){
                myself.textEditor.notify(result.getTestResultDescription());
            }
        });
            
            
    },
    
    toggleQueryResults: function(){
      
      
        if(this.isQueryPreviewShown){
            // Hide it
            this.isQueryPreviewShown = this.textEditor.toggleRightPanel();
            return;
        }
        
        
        // Ok - Try to open it and run the test
        var env = this.setupEnvironment();
        if(env){
            var test = this.getTestToOperate(env,this.toggleQueryResultsCallback, $("button.run",this.$ph));
        }
        


      
    },
    
    toggleQueryResultsCallback: function(env,test){
        
        var myself = this;
        this.textEditor.notify("Running query...");

        env.cdv.executeQuery(test,null, function(test,opts,queryResult){
            myself.textEditor.notify("Queries ran in " + queryResult.duration + "ms");
            
                    
            myself.textEditor.getRightPanel().html("<pre>"+JSON.stringify( queryResult.resultset ,undefined,2)+"</pre>");
            myself.isQueryPreviewShown = myself.textEditor.toggleRightPanel();
            Dashboards.log("Toggling!");
            
        });
        
        
    },

    getButtons: function(){
        
        var myself = this;
        
        var _extraButtons = this.extraButtons || [];
        
        _.chain(this.defaultButtons).each(function(b){
            b.callback = _.bind(b.callback, myself);
        })
        return this.defaultButtons.concat(_extraButtons);
    },

    setFile: function(_file){
        this.file = _file;
        this.textEditor.setFile(_file);
    },
    
    
    setupEnvironment: function(){
        
        // Get source
        
        var src = this.textEditor.getEditorWindow().editor.getContents();
        
        var mask = {
            cdv: wd.cdv.cdv({
                isServerSide: false
            })
        };
        
        // mask global properties 
        for (p in this)
            mask[p] = undefined;

        // execute script in private context
        try{
            (new Function( "with(this) { " + src + "}")).call(mask);
        }
        catch(err){
            alert(err);
            return null;
        }
        
        return mask;
        
    },
  
  
    getTestToOperate: function(env, operationCallback, target){
        
        var myself = this;
                
        // How many tests do we have? If only one, return
        
        var flattenedTests = env.cdv.listTestsFlatten().sort(function(a,b){
            return (a.group + a.name) >=  (b.group + b.name)
        });
        
        
        if(flattenedTests.length == 1){
            // return it
            operationCallback.call(myself,env,env.cdv.getTest(flattenedTests[0].group,flattenedTests[0].name));
            return;
        }
        
        
        // We need to prompt for the test
        if(!this.testPromptPopup){
            
                    
            // Generate a popup component for us
            this.testPromptPopup = {
                name: "testPromptPopup", 
                type:"popup", 
                htmlObject: 'testPromptPopupObj',
                gravity: "S",
                draggable: false,
                closeOnClickOutside: true
            }
            
            this.$testPromptPopupObj = $("<div id='testPromptPopupObj'></div>").appendTo("body");
            
            Dashboards.addComponents([this.testPromptPopup]);
            this.testPromptPopup.update();
            
            // Allow customization
            this.$testPromptPopupObj.parent("div.popupComponent").addClass("testPromptPopup");
        
            
        }
        
        var template = '<div class="testChooserWrapper"><div class="title">Multiple tests found. Choose the one you want:</div>'+
        '<div class="testChooserButtons">{{#tests}}<button> {{group}} - {{name}}</button>{{/tests}}</div></div>'
        this.$testPromptPopupObj.html(Mustache.render(template, {
            tests: flattenedTests 
        }));
        

        this.$testPromptPopupObj.off("click","button");
        this.$testPromptPopupObj.on("click","button",function(evt){
            var idx = $(this).prevAll("button").length;
            operationCallback.call(myself,env,env.cdv.getTest(flattenedTests[idx].group,flattenedTests[idx].name));
           
            myself.testPromptPopup.hide();
        })
        
        this.testPromptPopup.popup(target);
        
        return;
        
    }
    
    

});

