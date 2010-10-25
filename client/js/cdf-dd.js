// Base class and general utils

if (!Array.prototype.map)
{
  Array.prototype.map = function(fun /*, thisp */)
  {
    "use strict";

    if (this === void 0 || this === null)
      throw new TypeError();

    var t = Object(this);
    var len = t.length >>> 0;
    if (typeof fun !== "function")
      throw new TypeError();

    var res = new Array(len);
    var thisp = arguments[1];
    for (var i = 0; i < len; i++)
    {
      if (i in t)
        res[i] = fun.call(thisp, t[i], i, t);
    }

    return res;
  };
}


$.editable.addInputType('autocomplete', {
  element : $.editable.types.text.element,
  plugin : function(settings, original) {
    $('input').focus(function(){
      this.select();
    })
    $('input',this).autocomplete(settings.autocomplete);
  }
});

var CDFDD = Base.extend({

  logger: "",
  dashboardData: {},
  dashboardWcdf: {},
  styles: [],

  // Panels
  layout: {},
  components: {},
  datasources: {},
  wizards: {},

  constructor: function(){

    this.logger = new Logger("CDFDD");
    this.logger.info("Initializing CDFDD");

    // Common stuff
    TableManager.globalInit();
    WizardManager.globalInit();
  },


  init: function(){

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

    // Keyboard shortcuts
    $(function(){
      $(document).keydown(function(e){
        if ($(e.target).is('input, textarea')){
          return;
        }
        else{
          //Dashboards.log("Target event:" + e.target)
        }


        if ($(e.target).is('input, textarea')){
          switch(e.which){
            case 38:
              Dashboards.log("Go up");
              break;
            case 40:
              Dashboards.log("Go down");
              break;
          }

        }

        switch(e.which){
          case 49:
            $(".cdfdd-modes").find("a:eq(0)").click();
            break;
          case 50:
            $(".cdfdd-modes").find("a:eq(1)").click();
            break;
          case 51:
            $(".cdfdd-modes").find("a:eq(2)").click();
            break;
          case 83:
            if (e.shiftKey){
              cdfdd.save();
            }
            break;
          case 80:
            if (e.shiftKey){
              cdfdd.previewMode();
            }
            break;
          case 191:
            if (e.shiftKey){
              cdfdd.toggleHelp();
            }
            break;
        }
      })
    })


    // Activate tooltips - Note: Disabled since last style change
    // $(".tooltip").tooltip({showURL: false });

    // Load styles list
    var myself = this;
    $.getJSON("SyncStyles", {
      operation: "listStyles"
    }, function(json) {
      myself.styles = json.result;
                                        
    });

  },

  initStyles: function(callback) {
    var myself = this;
    if (myself.styles.length > 0) {
      var wcdf = myself.getDashboardWcdf();
      // Default to Clean or the first available style if Clean isn't available
      var cleanStyle = myself.styles.indexOf('Clean');
      wcdf.style = myself.styles[cleanStyle >= 0 ? cleanStyle : 0];
      var saveSettingsParams = {
        operation: "saveSettings",
        file: CDFDDFileName.replace(".cdfde",".wcdf"),
        title: wcdf.title,
        author: wcdf.author,
        description: wcdf.description,
        style: wcdf.style
      };
      $.post(CDFDDDataUrl, saveSettingsParams, function(result) {
        var json = eval("(" + result + ")");
        if(json.status == "true"){
          myself.setDashboardWcdf(wcdf);
          callback();
        } else {
          $.notifyBar({
            html: "Errors initializing settings: " + json.result
          });
        }
      });
    }
  },

  load: function(){

    this.logger.info("Loading dashboard...");

    var myself = this;
    var loadParams = {
      operation: "load",
      file: CDFDDFileName
    }
			
    $.post(CDFDDDataUrl, loadParams, function(result) {
      var json = eval("(" + result + ")");
      if(json.status == "true"){
        myself.setDashboardData(json.result.data);
        myself.setDashboardWcdf(json.result.wcdf);
        myself.init();
      }
      else {
        alert(json.result)
      }
    });
  },

  save: function(){

    this.logger.info("Saving dashboard...");
    this.dashboardData.filename = CDFDDFileName;
    var saveParams = {
      operation: "save",
      file: CDFDDFileName,
      // cdfstructure: JSON.toJSONString(this.dashboardData,true)
      cdfstructure: JSON.stringify(this.dashboardData,"",2)
    };
    if (CDFDDFileName != "/null/null/null") {
      $.post(CDFDDDataUrl, saveParams, function(result) {
        //$.getJSON("/pentaho/content/pentaho-cdf-dd/Syncronize", saveParams, function(json) {
        var json = eval("(" + result + ")");
        if(json.status == "true"){
          $.notifyBar({
            html: "Dashboard saved successfully",
            delay: 1000
          });
        }
        else{
          $.notifyBar({
            html: "Errors saving file: " + json.result
          });
        }
      });
    }
    else {
      this.saveAs(true);
    }

  },

  toggleHelp: function(){
    $("#keyboard_shortcuts_help").toggle();
  },
		
  saveAs: function(fromScratch){
		
    var selectedFolder = "";
    var selectedFile = "";
    var selectedTitle = this.getDashboardWcdf().title;
    var selectedDescription = this.getDashboardWcdf().description;
    var myself = this;
    var content = '<div class="saveaslabel">Save as:</div>\
								<div id="container_id" class="folderexplorer" width="400px"></div>\
									<span class="folderexplorerfilelabel">File Name:</span>\
									<span class="folderexplorerfileinput"><input id="fileInput"  type="text"></input></span>\
								<hr class="filexplorerhr"/>\
								<span class="folderexplorerextralabel" >Extra Information:</span><br/>\
									<span class="folderexplorerextralabels" >Title:</span><input id="titleInput" class="folderexplorertitleinput" type="text" value="' + selectedTitle +'"></input>\
									<br/><span class="folderexplorerextralabels" >Description:</span><input id="descriptionInput"  class="folderexplorerdescinput" type="text" value="' + selectedDescription +'"></input>';
							
    $.prompt(content,{
      loaded: function(){
        $('#fileInput').change(function(){
          selectedFile = this.value;
        });
        $('#titleInput').change(function(){
          selectedTitle = this.value;
        });
        $('#descriptionInput').change(function(){
          selectedDescription = this.value;
        });
        $('#container_id').fileTree(
        {
          root: '/',
          script: CDFDDDataUrl.replace("Syncronize","ExploreFolder?fileExtensions=.wcdf"),
          expandSpeed: 1000,
          collapseSpeed: 1000,
          multiFolder: false,
          folderClick:
          function(obj,folder){
            if($(".selectedFolder").length > 0)$(".selectedFolder").attr("class","");
            $(obj).attr("class","selectedFolder");
            selectedFolder = folder;
            $("#fileInput").val("")
          }
        },
        function(file) {
          $("#fileInput").val(file.replace(selectedFolder,""));
          selectedFile = $("#fileInput").val();
        });
      },
      buttons: {
        Ok: true,
        Cancel: false
      },
      opacity: 0.2,
      prefix: 'treeTableNewJqi',
      callback: function(v,m,f){
        if(v){
					
          if(selectedFile.indexOf(".") != -1 && (selectedFile.length < 5 || selectedFile.lastIndexOf(".wcdf") != selectedFile.length-5))
            $.prompt('Invalid file extension. Must be .wcdf');
          else if(selectedFolder.length == 0)
            $.prompt('Please choose destination folder.');
          else if(selectedFile.length == 0)
            $.prompt('Please enter the file name.');
							
          else if(selectedFile.length > 0){
            if(selectedFile.indexOf(".wcdf") == -1) selectedFile += ".wcdf";
            //var saveAsParams = {operation: fromScratch  ? "newFile" : "saveas", file: selectedFolder + selectedFile, title: selectedTitle, description: selectedDescription, cdfstructure: JSON.toJSONString(myself.dashboardData,true) };
            var saveAsParams = {
              operation: fromScratch  ? "newFile" : "saveas",
              file: selectedFolder + selectedFile,
              title: selectedTitle,
              description: selectedDescription,
              cdfstructure: JSON.stringify(myself.dashboardData,"",2)
            };
            $.post(CDFDDDataUrl, saveAsParams, function(result) {
              var json = eval("(" + result + ")");
              if(json.status == "true"){
                CDFDDFileName = selectedFolder + selectedFile;
                if(selectedFolder[0] == "/") selectedFolder = selectedFolder.substring(1,selectedFolder.length);
                var solutionPath = selectedFolder.split("/");
                myself.initStyles(function(){
                  window.location = '../pentaho-cdf-dd/Edit?solution=' + solutionPath[0] + "&path=" + solutionPath.slice(1).join("/") + "&file=" + selectedFile;
                });
              }
              else
                $.notifyBar({
                  html: "Errors saving file: " + json.result
                });
            });
          }
        }
      }
    });
  },
		
  previewMode: function(){
		
    var fullPath =  CDFDDFileName.split("/");
    var solution = fullPath[1];
    var path = fullPath.slice(2,fullPath.length-1).join("/");
    var file = fullPath[fullPath.length-1].replace(".cdfde","_tmp.cdfde");
    ;
    var style = this.getDashboardWcdf().style;

    this.logger.info("Saving temporary dashboard...");

    var saveParams = {
      operation: "save",
      file: CDFDDFileName.replace(".cdfde","_tmp.cdfde"),
      //cdfstructure: JSON.toJSONString(this.dashboardData,true)
      cdfstructure: JSON.stringify(this.dashboardData,"",2)
    };

    $.post(CDFDDDataUrl, saveParams, function(result) {
      var json = eval("(" + result + ")");
      if(json.status == "true"){
        //window.open("Render?solution=" + solution + "&path=/" + path + "&file=" + file + "&cache=false");
        var _href = CDFDDDataUrl.replace("Syncronize","Render?") + "solution=" + solution + "&path=/" + path + "&file=" + file + "&style=" + style + "&cache=false";
        $.fancybox({
          type:"iframe",
          href:_href,
          width: $(window).width(),
          height:$(window).height()
        });
      }
      else{
        $.notifyBar({
          html: "Errors saving file: " + json.result
        });
      }
    });
  },

  reload: function(){
    this.logger.warn("Reloading dashboard... ");
			
    $.prompt('Are you sure you want to reload? Unsaved changes will be lost.',{
      buttons: {
        Ok: true,
        Cancel: false
      } ,
      callback: function(v,m,f){
        if(v) window.location.reload();
      }
    });
  },

  reset: function(){

    this.logger.info("Resetting dashboad");
    CDFDD.PANELS().empty();
  },
		
  saveSettings: function(){
    if (this.styles.length == 0){
      var myself = this;
      $.getJSON("SyncStyles", {
        operation: "listStyles"
      }, function(json) {
        myself.styles = json.result;
        myself.saveSettingsCallback();
      });
    }else{
      this.saveSettingsCallback();
    }
		
  },

  saveSettingsCallback: function(){
			
    var title = this.getDashboardWcdf().title;
    var description = this.getDashboardWcdf().description;
    var author = this.getDashboardWcdf().author;
    var style = this.getDashboardWcdf().style;
    var myself = this;
    var content = '\
				<span><b>Settings:</b></span><br/><hr/>\
				<span>Title:</span><br/><input class="cdf_settings_input" id="titleInput" type="text" value="' + title +'"></input><br/>\
				<span>Author:</span><br/><input class="cdf_settings_input" id="authorInput" type="text" value="' + author +'"></input>\
				<span>Description:</span><br/><textarea class="cdf_settings_textarea" id="descriptionInput">' + description + '</textarea>\
				<span>Style:</span><br/><select class="cdf_settings_input" id="styleInput">';

    $.each(this.styles,function(i,obj){
      content += ('<option value="'+ obj +'" ' + (style==obj?'selected':'') +' >'+ obj+'</option>');
    });
    content += '</select>';
			
    $.prompt(content,{
      buttons: {
        Save: true,
        Cancel: false
      },
      submit: function(){
        title = $("#titleInput").val();
        author = $("#authorInput").val();
        description = $("#descriptionInput").val();
        style = $("#styleInput").val();
      },
      callback: function(v,m,f){
        if(v){
          myself.logger.info("Saving dashboard settings...");

          var saveSettingsParams = {
            operation: "saveSettings",
            file: CDFDDFileName.replace(".cdfde",".wcdf"),
            title: title,
            author: author,
            description: description,
            style: style
          };
						
          $.post(CDFDDDataUrl, saveSettingsParams, function(result) {
            var json = eval("(" + result + ")");
            if(json.status == "true"){
              myself.setDashboardWcdf({
                title: title,
                author: author,
                description: description,
                style: style
              });
              $.notifyBar({
                html: "Dashboard Settings saved successfully",
                delay: 1000
              });
            }
            else
              $.notifyBar({
                html: "Errors saving settings: " + json.result
              });
          });
        }
      }
    });
  },

  setDashboardData: function(dashboardData){
    this.dashboardData = dashboardData
  },
  getDashboardData: function(){
    return this.dashboardData
  },
  setDashboardWcdf: function(dashboardWcdf){
    this.dashboardWcdf = dashboardWcdf
  },
  getDashboardWcdf: function(){
    return this.dashboardWcdf
  }

},
{
  LAYOUT: function(){
    return $("table#layout-div tbody")
  },
  PANELS: function(){
    return $("#cdfdd-panels")
  }
});


// Panel

var Panel = Base.extend({

  id: "",
  name: "",
  logger: {},

  constructor: function(id){
    this.logger = new Logger("Panel");
    this.id = id;
  },

  init: function(){
    this.logger.info("Initializing panel " + name);

    if($("#panel-" + this.id).length == 0 ){
      CDFDD.PANELS().append(this.getHtml());
    }
  },

  switchTo: function(){
    this.logger.debug("Switching to " + this.name);
    $("div."+Panel.GUID).hide();
    $("div#panel-"+ this.id).show();
  },

  reset: function(){
    $("#"+id).empty();
  },

  getHtml: function(){

    return '\
			<div id="panel-' + this.id + '" class="span-24 last ' + Panel.GUID + '">\
			<div class="panel-content">' + this.getContent() + '</div> \
			</div>';
  //<h2 class="panel-title">'+this.name+'</h2> \
  },

  getContent: function(){

    return '<span class="highlight">Not done yet</span>';
  },

  setId: function(id){
    this.id = id
  },
  getId: function(){
    return this.id
  }



},{
  GUID: "cdfdd-panel",
  panels: {},

  register: function(panel){
    Panel.panels[panel.getId()] = panel;
  },

  getPanel: function(id){
    return Panel.panels[id];
  },

  getRowPropertyValue: function(row, propertyName){
    var output = "";
    $.each(row.properties,function(i,property){
      if (property.name == propertyName){
        output = property.value;
        return false;
      }
    });
    return output;
  },
  enableThisButton: function(doc){
    //var ENABLED_STR = "_active";
    //var DISABLED_STR = "_inactive";
    // Disable other buttons and enable this one
    var a = $(doc);
    var myIdx = a.prevAll("a").length;
    a.parent().find("img").each(function(i,x){
      if (i==myIdx){
        //may be hovered
        Panel.unsetHover(x);
        $(x).attr("src",$(x).attr("src").replace(/(.*)\/X?(.*)/,"$1/$2"))
      //enable
      //$(x).attr("src", $(x).attr("src").replace(DISABLED_STR,ENABLED_STR));
      }else{
        $(x).attr("src",$(x).attr("src").replace(/(.*)\/X?(.*)/,"$1/X$2"))
      //$(x).attr("src", $(x).attr("src").replace(ENABLED_STR,DISABLED_STR));
      //disable
						
      }
					
    });
  },
    
  disableThisButton: function(doc) {
    var a = $(doc);
    var myIdx = a.prevAll("a").length;
    a.parent().find("img").each(function(i,x){
      if (i==myIdx){
        $(x).attr("src",$(x).attr("src").replace(/(.*)\/X?(.*)/,"$1/X$2"));
      }
    });
  },
    

  setHover : function(comp){
    var el = $(comp);
    var src = el.attr("src");

    var xPos = src.search("/X");
    if(xPos != -1){//only hover disabled
      src = src.slice(0, xPos) + "/" + src.slice(xPos + 2);//"/X".length
      el.attr("src", src.slice(0, src.length - ".png".length) + "_mouseover.png" );
    }
  },
    
  unsetHover : function(comp){
    var el = $(comp);
    var src = el.attr("src");

    var hPos = src.search("_mouseover");
    if(hPos != -1){//back to disabled if on hover
      //set disabled (X)
      src = src.replace(/(.*)\/X?(.*)/,"$1/X$2");
      //remove _hover
      el.attr("src", src.slice(0, src.length - "_mouseover.png".length) + ".png" );
    }
  }
		
//		//TODO: temp
//		initHoverPics: function(){
//      var END = ".png";
//      var HEND = "_hover.png";
//      var endLen = END.length;
//      var hendLen = HEND.length;
//
//      var me = $("#" + this.id);
//
//
//
//      var hoverIn = function(){
//        var src = $(this).attr("src");
//        $(this).attr("src", src.slice(0, src.len - endLen) + "_hover.png");
//      }
//      var hoverOut = function(){
//        var src = $(this).attr("src");
//        $(this).attr("src", src.slice(0, src.len - hendLen) + ".png");
//      }
//      me.find("img").each(function(idx, comp){
//        //TODO: check if exists or something
//        //hover( handlerIn(eventObject), handlerOut(eventObject) )
//        if($(comp).attr("src").search(ENABLED_STR) == -1) {//not enabled
//                $(comp).hover
//        }
//      })
//		}
});


// Logger

var Logger = Base.extend({

  ERROR: 0,
  WARN: 1,
  INFO: 2,
  DEBUG: 3,
  name: "",

  logDescription: ["ERROR", "WARN", "INFO", "DEBUG"],

  constructor: function(name){
    this.name = name;
  },

  log: function(level, str){
    if(cdfddLogEnabled && level <= cdfddLogLevel && typeof console != 'undefined'){
      console.log(" - [" + this.name + "] "+ this.logDescription[level]  + ": " +str);
    }
  },
  error : function(str){
    this.log(this.ERROR,str)
  },
  warn : function(str){
    this.log(this.WARN,str)
  },
  info : function(str){
    this.log(this.INFO,str)
  },
  debug : function(str){
    this.log(this.DEBUG,str)
  }

});


// Utility functions

var CDFDDUtils = Base.extend({
  },{
    ev: function(v){
      return (typeof v=='function'?v():v);
    },
    getProperty: function(stub, name){
      var result;
      if(typeof stub.properties == 'undefined')
        return;

      $.each(stub.properties,function(i,p){
        if(p.name == name){
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
  $.editable.types.selectMulti = {
    element : function(settings, original) {
      var select = $('<select multiple="multiple" />');
      $(this).append(select);
      return(select);
    },
    content : function(data, settings, original) {
      // If it is string assume it is an array.
      if (String == data.constructor) {
        eval ('var json = ' + data);
      } else {
        // Otherwise assume it is a hash already.
        var json = data;
      }
      for (var key in json) {
        if (!json.hasOwnProperty(key)) {
          continue;
        }
        if ('selected' == key) {
          continue;
        }
        var option = $('<option />').val(key).append(json[key]);
        $('select', this).append(option);
      }
      // Loop option again to set selected. IE needed this...
      var _selectedHash = {};
      var _selectedArray= json['selected'];
      //eval("_selectedArray = " + json['selected']);
      $.each(_selectedArray,function(i,val){
        _selectedHash[val] = true
      });
      $('select', this).children().each(function() {
        if ( _selectedHash[$(this).val()] ||
          $(this).text() == $.trim(original.revert)) {
          $(this).attr('selected', 'selected');
        }
      });
      $('select', this).multiSelect({
        oneOrMoreSelected: "*"
      },function() {});
    }
  };

  // Remove bug in position() of multipleselect
  $.extend($.fn, {
    multiSelectOptionsShow: function(){
      // Hide any open option boxes
      $('.multiSelect').multiSelectOptionsHide();
      $(this).next('.multiSelectOptions').find('LABEL').removeClass('hover');
      $(this).addClass('active').next('.multiSelectOptions').show();

      // Position it
      // The following line overrides the default
      //var offset = $(this).closes("td").position();
      var offset = []
      $(this).next('.multiSelectOptions').css({
        top:  offset.top + $(this).outerHeight() + 'px'
      });
      $(this).next('.multiSelectOptions').css({
        left: offset.left + 'px'
      });

      // Disappear on hover out
      multiSelectCurrent = $(this);
      var timer = '';
      $(this).next('.multiSelectOptions').hover( function() {
        clearTimeout(timer);
      }, function() {
        timer = setTimeout('jQuery(multiSelectCurrent).multiSelectOptionsHide(); $(multiSelectCurrent).unbind("hover");', 250);
      });

    }
  })

});

