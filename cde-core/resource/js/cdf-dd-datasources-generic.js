
var MdxEntryBase = PalleteEntry.extend({
  execute : function(palleteManager) {
    var myself = this;
    this._execute = this.base;
    
    var content = '<select id="cdfddOlapCatalogSelect" onchange="MdxEntry.getEntry(\'MDX_ENTRY\').catalogSelected()"><option value="-"> Select catalog </option></select><br/>\n' +
'			<select id="cdfddOlapCubeSelect" onchange="MdxEntry.getEntry(\'MDX_ENTRY\').cubeSelected()" ><option value="-"> Select cube </option></select>';

    $.prompt(content,{
      buttons: {
        Ok: true,
        Skip: false
      },
      prefix:"popup",
      loaded: function(){
        $.getJSON(OlapUtils.getOlapCubesUrl(), {}, function(json) {
          if(json.status == "true"){
            var catalogs = json.result.catalogs;
            myself.catalogs = catalogs;
            $.each(catalogs,function(i,catalog){
              $("select#cdfddOlapCatalogSelect").append("<option>"+catalog.name+"</option>");
            });
            myself.catalogSelected();
          }
        });
      },
      callback: function(v,m,f){
        var datasourceStub = myself.getStub();
        if(v && myself.catalog != undefined){
          CDFDDUtils.getProperty(datasourceStub,"jndi").value =  myself.catalog.jndi;
          CDFDDUtils.getProperty(datasourceStub,"catalog").value = myself.catalog.schema;
          CDFDDUtils.getProperty(datasourceStub,"cube").value = myself.cube;
        }
        myself._execute(palleteManager,datasourceStub);
      }
    });
  },
		
  catalogSelected: function(){
    var myself = this;
    var selectedCatalog = $("#cdfddOlapCatalogSelect").val();
    var catalog = null;
			
    $.each(this.catalogs,function(i,cat){
      if(cat.name == selectedCatalog){
        catalog = cat;
        return;
      };
    });
			
    this.catalog = catalog;
    if(catalog != null){
      var cubes = catalog.cubes;
      $("select#cdfddOlapCubeSelect").empty();
      $.each(cubes,function(i,cube){
        if(i==0)myself.cube  = cube.name;
        $("select#cdfddOlapCubeSelect").append("<option>"+cube.name+"</option>");
      });
    }
  },
		
  cubeSelected: function(){
    this.cube = $("#cdfddOlapCatalogSelect").val();
  }
},{
  getEntry: function(id){
    var entry = undefined;
    $.each(CDFDDDatasourcesArray,function(i, ent) {
      if(ent.id == id) {
        entry = ent;
        return;
      }
    });
    return entry;
  }
});

var QueryRenderer = TextAreaRenderer.extend({});

var CdaPathRenderer = ResourceFileRenderer.extend({
  getFileExtensions: function(){
    return ".cda";
  },
	
	getResourceType: function(){
		return 'cda';
	},

  formatSelection: function(file){
    return file;
  }
});

var CdaEditorRenderer = StringRenderer.extend({

  constructor: function(tableManager){
    this.base(tableManager);
    this.logger = new Logger("CdaFileRenderer");
    this.logger.debug("Creating new CdaFileRenderer");
  },

  render: function(placeholder, value, callback){

    var _editArea = $("<td></td>");
    var path = value;
    var _prompt = $('<button class="cdfddInput" style="width:auto">Edit in CDA</button>').bind("click",function(){
      CdaEditorRenderer.popup(path);
    });
    _editArea.append(_prompt);
    _editArea.appendTo(placeholder);

  },
  validate: function(settings, original){
    return true;
  }

},{
  popup: function(path)  {

    $("#wizardDialog").empty();
    $("#wizardDialog").append("<iframe src='../cda/editFile?path="+path+"&initialState=exEdit' width='100%' height='95%' ></iframe>");
    $("#wizardDialog").append("<button onclick='$(\"#wizardDialog\").jqmHide()'class='cdfddInput' style='float:right;width: auto'>Close</button>");
    $("#wizardDialog").jqmShow();
  }
});

var CggPathRenderer = ResourceFileRenderer.extend({

  getFileExtensions: function(){
    return ".js";
  },
	
	getResourceType: function(){
		return 'javascript';
	},

  formatSelection: function(file){
    return file;
  }
});

var KtrPathRenderer = ResourceFileRenderer.extend({

  getFileExtensions: function(){
    return ".ktr";
  },

  formatSelection: function(file){
    if (file.charAt(0) != '/') {
      file = "/"+ file;
    }
    var common = true;
    var splitFile = file.split("/");
    var dashFile = cdfdd.getDashboardData().filename;
    if(dashFile == null) {
     return file;
    }
    splitPath = dashFile.split("/"),
    finalPath = "",
    i = 0;
    while (common){
      if (splitFile[i] !== splitPath[i]) {
        common = false;
      }
      i += 1;
    }
       
    $.each(splitPath.slice(i),function(i,j){
      finalPath+="../";
    });
    finalPath += splitFile.slice(i - 1).join('/');
    return finalPath.replace(/\/+/g, "/");
  }
});

var SaikuPathRenderer = ResourceFileRenderer.extend({

  //disallow selecting a folder for new file creation
  createNew: false,
	
  //omit edit button
  renderEditorButton: function(){ return '';},
	
  getFileExtensions: function(){
    return ".saiku";
  },

  formatSelection: function(file){
    return file;
  }
});