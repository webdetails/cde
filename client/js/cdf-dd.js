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
    });
    $('input',this).autocomplete(settings.autocomplete);
  }
});

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
    
    //// Enable alert when leaving page
    //this.setExitNotification(true);

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
          case 71:
            if (e.shiftKey){
              cdfdd.cggDialog();
            }
            break;
          case 191:
            if (e.shiftKey){
              cdfdd.toggleHelp();
            }
            break;
          case 86:
            if (e.shiftKey){ //shift+v
              ComponentValidations.validateComponents();
            }
            break;
        }
      });
    });


    // Activate tooltips - Note: Disabled since last style change
    // $(".tooltip").tooltip({showURL: false });

    // Load styles list
    var myself = this;
    $.getJSON("SyncStyles", {
      operation: "listStyles"
    }, function(json) {
      myself.styles = json.result;
                                        
    });
    $.getJSON("listRenderers", {
      operation: "listStyles"
    }, function(json) {
      myself.renderers = json.result;
                                        
    });
  },

  initStyles: function(callback) {
    var myself = this;
    if (myself.styles.length > 0) {
      var wcdf = myself.getDashboardWcdf();
      // Default to Clean or the first available style if Clean isn't available
      var cleanStyle = myself.styles.indexOf('Clean');
      if(!wcdf.style) {
				wcdf.style = myself.styles[cleanStyle >= 0 ? cleanStyle : 0];
			}
      //only set style setting
      var saveSettingsParams = {
        operation: "saveSettings",
        file: CDFDDFileName.replace(".cdfde",".wcdf"),
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
    };
			
    $.post(CDFDDDataUrl, loadParams, function(result) {
      var json = eval("(" + result + ")");
      if(json.status == "true"){
        myself.setDashboardData(myself.unstrip(json.result.data));
        myself.setDashboardWcdf(json.result.wcdf);
        myself.init();
      }
      else {
        alert(json.result);
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
      cdfstructure: JSON.stringify(this.strip(this.dashboardData),"",1)
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
      this.saveAs(false);
    }

  },

  strip: function(original){

    // accepted values
    var strip = {
      type: true,
      name: true,
      value: true,
      url: true
    };

    // Removes extra information and saves space
    var o = Util.clone(original);
    $.each(o,function(i,t){
      if(typeof t !== "object"){
        return;
      }
      $.each(t.rows,function(j,c){
        $.each(c.properties,function(k,properties){
          $.each(properties,function(p){
            if(!strip[p])
              delete properties[p];
          });
        });
      });
    });
    return o;

  },

  unstrip: function(original){

    // accepted values
    var strip = {
      type: true,
      name: true,
      value: true,
      url: true
    };

    // Removes extra information and saves space
    var myself = this;
    var o = Util.clone(original);
    $.each(o,function(i,t){
      if(typeof t !== "object"){
        return true;
      }
      myself.logger.debug("  unstrip: " + i + ", " + t);
      $.each(t.rows,function(j,c){
        myself.logger.debug("  unstrip component "+ c.type +" property count: " + c.properties.length );
        $.each(c.properties,function(idx,p){
          try {
            var property = PropertiesManager.getPropertyType(p.name).stub;
            for (var attr in property) if (property.hasOwnProperty(attr)) {
              if (!p.hasOwnProperty(attr)){
                p[attr] = property[attr];
              }
            }
          } catch (e) {
            Dashboards.log(p.name + ": " + e);
          }
        });
      });
    });
    return o;

  },


  toggleHelp: function(){
    $("#keyboard_shortcuts_help").toggle();
  },
  
  newDashboard: function(){
    var myself = this;
    $.prompt('Are you sure you want to start a new dashboard?<br/>Unsaved changes will be lost.',{
      buttons: {
        Ok: true,
        Cancel: false
      } ,
      callback: function(v,m,f){
        if(v) myself.saveAs(true);
      }
    });
  },
		
  saveAs: function(fromScratch){
		
    var selectedFolder = "";
    var selectedFile = "";
    var selectedTitle = this.getDashboardWcdf().title;
    var selectedDescription = this.getDashboardWcdf().description;
    var myself = this;
    var content = '<div class="saveaslabel">Save as:</div>\n' +
'								<div id="container_id" class="folderexplorer" width="400px"></div>\n' +
'									<span class="folderexplorerfilelabel">File Name:</span>\n' +
'									<span class="folderexplorerfileinput"><input id="fileInput"  type="text"></input></span>\n' +
'								<hr class="filexplorerhr"/>\n' +
'								<span class="folderexplorerextralabel" >Extra Information:</span><br/>\n' +
'									<span class="folderexplorerextralabels" >Title:</span><input id="titleInput" class="folderexplorertitleinput" type="text" value="' + selectedTitle +'"></input>\n' +
'									<br/><span class="folderexplorerextralabels" >Description:</span><input id="descriptionInput"  class="folderexplorerdescinput" type="text" value="' + selectedDescription +'"></input>';
							
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
          script: CDFDDDataUrl.replace("Syncronize","ExploreFolder?fileExtensions=.wcdf&access=create"),
          expandSpeed: 1000,
          collapseSpeed: 1000,
          multiFolder: false,
          folderClick:
          function(obj,folder){
            if($(".selectedFolder").length > 0)$(".selectedFolder").attr("class","");
            $(obj).attr("class","selectedFolder");
            selectedFolder = folder;
            $("#fileInput").val("");
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

            CDFDDFileName = selectedFolder + selectedFile;
            myself.dashboardData.filename = CDFDDFileName;

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
                if(selectedFolder[0] == "/") selectedFolder = selectedFolder.substring(1,selectedFolder.length);
                var solutionPath = selectedFolder.split("/");
                myself.initStyles(function(){
                  //cdfdd.setExitNotification(false);
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
		
  footerMode: function (mode) {

	  var version = {
	      
	      versionGetInfo: null,
	      versionCheckInfo: null,
	
	      getInfo: function(url) {
	      	 var versionInfo = '';
		      $.get(url, function (result) {
		          if (!result) {
		              Dashboards.log('CDFF-DD: ' + url + ' error');
		              return;
		          }
		          versionInfo = result;
		      });
		      return versionInfo;
	      },
	      
	      getVersion:  function () {
	      
	          var versionCheckUrl = '/pentaho/content/pentaho-cdf-dd/getVersion';
		      versionGetInfo = this.getInfo(versionCheckUrl);
		      return versionGetInfo;
		  },
	      	      
	      checkVersion:  function () {
	      
	      	  var versionCheckUrl = '/pentaho/content/pentaho-cdf-dd/checkVersion';
		      var versionCheckInfo = this.getInfo(versionCheckUrl);
		      var msg = '';
		      
	          versionCheckInfo = JSON.parse(versionCheckInfo);
	
	          switch (versionCheckInfo.result) {
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
		  var fileref=document.createElement("link");
		  fileref.setAttribute("rel", "stylesheet");
		  fileref.setAttribute("type", "text/css");
		  fileref.setAttribute("href", fileRef);
		  document.getElementsByTagName("head")[0].appendChild(fileref);
	  }
	   
	  var removeCSS = function(fileRef) {
		  var allCtrl = document.getElementsByTagName('link');
		  for (var i=allCtrl.length; i>=0; i--)  {
			  if (allCtrl[i] && allCtrl[i].getAttribute('href')!=null && allCtrl[i].getAttribute('href').indexOf(fileRef)!=-1)
			  	allCtrl[i].parentNode.removeChild(allCtrl[i]);
		  }
	  } 	
	  
      var htmlHref = "/pentaho/content/pentaho-cdf-dd/static/" + mode + ".html" ;
	  var cssFileRef = "/pentaho/content/pentaho-cdf-dd/css/" + mode + ".css"  ;

      $.fancybox({
          href: htmlHref,
          autoDimensions: false,
          width: 950,
          height: 600,
          padding: 0,
          margin: 0,
          onStart: function() {addCSS(cssFileRef);}, 
          onClosed: function() {removeCSS(cssFileRef);}       
      });
      
      if (mode == 'about.fancybox') {
	      $('#fancybox-content .version').html(version.getVersion());
	      $('#fancybox-content .message').html(version.checkVersion());
      }
   
   },
  
   previewMode: function(){
    
    if (CDFDDFileName == "/null/null/null") {
      $.notifyBar({
        html: "Need to save an initial dashboard before previews are available."
      });
      return;
    }
    
    var fullPath =  CDFDDFileName.split("/");
    var solution = fullPath[1];
    var path = fullPath.slice(2,fullPath.length-1).join("/");
    var file = fullPath[fullPath.length-1].replace(".cdfde","_tmp.cdfde");
    ;
    var style = this.getDashboardWcdf().style;

    this.logger.info("Saving temporary dashboard...");

    //temporarily set the filename to tmp
    var tmpFileName = CDFDDFileName.replace(".cdfde","_tmp.cdfde");
    this.dashboardData.filename = tmpFileName;
    var serializedDashboard = JSON.stringify(this.dashboardData,"",2);
    this.dashboardData.filename = CDFDDFileName;

    var saveParams = {
      operation: "save",
      file: tmpFileName,
      //cdfstructure: JSON.toJSONString(this.dashboardData,true)
      cdfstructure: serializedDashboard//JSON.stringify(this.dashboardData,"",2)
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
      else {
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
    var myself = this;
    var ready = true;
    function sCallback() {
        if (myself.styles.length > 0 && myself.renderers.length > 0) {
          myself.saveSettingsCallback();
        }
    }
    if (this.styles.length == 0){
      ready = false;
      $.getJSON("SyncStyles", {
        operation: "listStyles"
      }, function(json) {
        myself.styles = json.result;
        sCallback();
      });
    };
    if (this.renderers.length == 0){
      ready = false;
      $.getJSON("listRenderers", {},
        function(json) {
          myself.renderers = json.result;
          sCallback();
      });
    };
    if(ready){
      this.saveSettingsCallback();
    }
		
  },

  saveSettingsCallback: function(){
			
    var title = this.getDashboardWcdf().title;
    var description = this.getDashboardWcdf().description;
    var author = this.getDashboardWcdf().author;
    var style = this.getDashboardWcdf().style;
    var rendererType = this.getDashboardWcdf().rendererType;
    var myself = this;
    var content = '\n' +
'				<span><b>Settings:</b></span><br/><hr/>\n' +
'				<span>Title:</span><br/><input class="cdf_settings_input" id="titleInput" type="text" value="' + title +'"></input><br/>\n' +
'				<span>Author:</span><br/><input class="cdf_settings_input" id="authorInput" type="text" value="' + author +'"></input>\n' +
'				<span>Description:</span><br/><textarea class="cdf_settings_textarea" id="descriptionInput">' + description + '</textarea>\n' +
'				<span>Style:</span><br/><select class="cdf_settings_input" id="styleInput">';

    $.each(this.styles,function(i,obj){
      content += ('<option value="'+ obj +'" ' + (style==obj?'selected':'') +' >'+ obj+'</option>');
    });
    content += '</select><span>Dashboard Type:</span><br/><select class="cdf_settings_input" id="rendererInput">';
    $.each(this.renderers,function(i,obj){
      content += ('<option value="'+ obj +'" ' + (rendererType==obj?'selected':'') +' >'+ obj+'</option>');
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
        rendererType = $("#rendererInput").val(); 
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
            style: style,
            rendererType: rendererType
          };
						
          $.post(CDFDDDataUrl, saveSettingsParams, function(result) {
            var json = eval("(" + result + ")");
            if(json.status == "true"){
              myself.setDashboardWcdf({
                title: title,
                author: author,
                description: description,
                style: style,
                rendererType: rendererType
              });
              // We need to reload the layout engine in case the rendererType changed
              cdfdd.layout.init();
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
    this.dashboardData = dashboardData;
  },
  getDashboardData: function(){
    return this.dashboardData;
  },
  setDashboardWcdf: function(dashboardWcdf){
    this.dashboardWcdf = dashboardWcdf;
  },
  getDashboardWcdf: function(){
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
    var cggCandidates = components.filter(function(e){
      return e.meta_cdwSupport == 'true';
    });
        
    var ph = $('#cggDialog');
    ph = ph.length > 0 ? ph : $("<div id='cggDialog' style='display:none'></div>").appendTo($("body")).jqm();
    
    
    // cgg url:
    var cggUrl = window.location.href.substring(0,window.location.href.indexOf("content")) + "content/cgg/Draw?script=" +
      CDFDDFileName.substring(0,CDFDDFileName.lastIndexOf("/")) + "/";
    
    ph.empty(); 
    
    ph.append("<h3>Choose what charts to render as CGG</h3>"+
      "<p>CDE can generate CGG scripts to allow charts to be exported. Choose the ones you want, and files will be generated when you save the dashboard</p>");
    cggCandidates.map(function(e){
      
      var section = $("<div/>");
      
      var checkbox = $("<input type='checkbox'>");
      checkbox[0].checked = (e.meta_cdwRender  == 'true' || false);
      checkbox.change(function(){
        e.meta_cdwRender = "" + this.checked;
      });
      var name = '',
      title = '';
      e.properties.map(function(p){
        if (p.name == 'title') {
          title = p.value;
        } else if (p.name == 'name') {
          name = p.value;
        }
      });
      var label = "<span class='label'>" + (title !== '' ? title : name) + "</span>";
      section.append(checkbox);
      section.append(label);
      
      var showUrlButton = $("<button class='showUrl'>Url</button>").click(function(evt){
        
        Dashboards.log("Toggle Url show");
        var $t = $(this).toggleClass("active");
        $t.parent().find(".urlPreviewer").toggleClass("collapsed").find("input").select();
        
        
        
      });
      section.append(showUrlButton).append("<br />");

      $("<div class='urlPreviewer collapsed'><input type='text' value = '"+cggUrl+name+".js&outputType=png"+"'></input></div>").appendTo(section);

      section.appendTo(ph);

    });
    
    var $close = $("<div class='cggDialogClose'><button>Close</button></div>");
    $close.find("button").click(function(evt){
      ph.jqmHide();
    });
    ph.append($close);
    ph.jqmShow();

  }
},
{
  LAYOUT: function(){
    return $("table#layout-div tbody");
  },
  PANELS: function(){
    return $("#cdfdd-panels");
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

    return '\n' +
'			<div id="panel-' + this.id + '" class="span-24 last ' + Panel.GUID + '">\n' +
'			<div class="panel-content">' + this.getContent() + '</div> \n' +
'			</div>';
  //'<h2 class="panel-title">'+this.name+'</h2> '
  },

  getContent: function(){

    return '<span class="highlight">Not done yet</span>';
  },

  setId: function(id){
    this.id = id;
  },
  getId: function(){
    return this.id;
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
        $(x).attr("src",$(x).attr("src").replace(/(.*)\/X?(.*)/,"$1/$2"));
      //enable
      //$(x).attr("src", $(x).attr("src").replace(DISABLED_STR,ENABLED_STR));
      }else{
        $(x).attr("src",$(x).attr("src").replace(/(.*)\/X?(.*)/,"$1/X$2"));
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
    this.log(this.ERROR,str);
  },
  warn : function(str){
    this.log(this.WARN,str);
  },
  info : function(str){
    this.log(this.INFO,str);
  },
  debug : function(str){
    this.log(this.DEBUG,str);
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
      if(typeof stub.properties == 'undefined'){
        return null;
      }

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
      
      var t = $(this);
      var ph = t.next('.multiSelectOptions');
      
      ph.find('LABEL').removeClass('hover');
      t.addClass('active').next('.multiSelectOptions').show();

      // Position it
      // The following line overrides the default
      //var offset = $(this).closes("td").position();
      var offset = []
      ph.css({
        top:  offset.top + t.outerHeight() + 'px'
      })
      .css({
        left: offset.left + 'px'
      });

      // Disappear on hover out
      var timer = '';
      ph.unbind("mouseenter mouseleave").mouseenter( function() {
        Dashboards.log("enter");
        clearTimeout(timer);
      }).mouseleave(function() {
        Dashboards.log("leave");
        timer = setTimeout(function(){
          t.multiSelectOptionsHide();
          ph.unbind("mouseenter mouseleave");
         //'jQuery(multiSelectCurrent).multiSelectOptionsHide(); $(multiSelectCurrent).unbind("mouseenter");' 
        }, 250);
      });

    }
  })

});

