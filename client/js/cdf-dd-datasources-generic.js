// OLAP

var MdxEntry = PalleteEntry.extend({

  id: "MDX_ENTRY",
  name: "OLAP MDX query",
  description: "MDX query against a OLAP cube",
  category: "LEGACY",
  categoryDesc: "Legacy Datasources",
		
  execute : function(palleteManager){
			
    var myself = this;
    this._execute = this.base;
			
    var content = '<select id="cdfddOlapCatalogSelect" onchange="MdxEntry.getEntry(\'MDX_ENTRY\').catalogSelected()"><option value="-"> Select catalog </option></select><br/>\n' +
'			<select id="cdfddOlapCubeSelect" onchange="MdxEntry.getEntry(\'MDX_ENTRY\').cubeSelected()" ><option value="-"> Select cube </option></select>';
						  
    $.prompt(content,{
      buttons: {
        Ok: true,
        Skip: false
      },
      loaded: function(){
        $.getJSON(CDFDDServerUrl + "OlapUtils", {
          operation: "GetOlapCubes"
        }, function(json) {
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
    var catalog = undefined;
			
    $.each(this.catalogs,function(i,cat){
      if(cat.name == selectedCatalog){
        catalog=cat;
        return;
      };
    });
			
    this.catalog = catalog;
    if(catalog != undefined){
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
  },
		
  getStub: function(){
    return DatasourcesMdxModel.getStub();
  }
		
},{
			
  getEntry: function(id){
    var entry = undefined;
    $.each(CDFDDDatasourcesArray,function(i,ent){
      if(ent.id == id){
        entry = ent;
        return;
      }
    });
    return entry;
  }

});

var QueryRenderer = TextAreaRenderer.extend({});

var DatasourcesMdxModel = BaseModel.extend({
  },{
    MODEL: 'DatasourcesMdxModel',
    getStub: function(){
      var _stub = {
        id: TableManager.generateGUID(),
        type: DatasourcesMdxModel.MODEL,
        typeDesc: "Mdx query",
        parent: IndexManager.ROOTID,
        properties: []
      };

      _stub.properties.push(PropertiesManager.getProperty("name"));
      _stub.properties.push(PropertiesManager.getProperty("jndi"));
      _stub.properties.push(PropertiesManager.getProperty("catalog"));
      _stub.properties.push(PropertiesManager.getProperty("cube"));
      _stub.properties.push(PropertiesManager.getProperty("mdxquery"));

      return _stub;
    }
  });
BaseModel.registerModel(DatasourcesMdxModel);
CDFDDDatasourcesArray.push(new MdxEntry());


// SQL

var SqlEntry = PalleteEntry.extend({

  id: "SQL_ENTRY",
  name: "SQL query",
  description: "SQL query against a datasource",
  category: "LEGACY",
  categoryDesc: "Legacy Datasources",

  getStub: function(){
    return DatasourcesSqlModel.getStub();
  }

});

var DatasourcesSqlModel = BaseModel.extend({
  },{
    MODEL: 'DatasourcesSqlModel',
    getStub: function(){
      var _stub = {
        id: TableManager.generateGUID(),
        type: DatasourcesSqlModel.MODEL,
        typeDesc: "Sql query",
        parent: IndexManager.ROOTID,
        properties: []
      };

      _stub.properties.push(PropertiesManager.getProperty("name"));
      _stub.properties.push(PropertiesManager.getProperty("jndi"));
      _stub.properties.push(PropertiesManager.getProperty("sqlquery"));

      return _stub;
    }
  });
BaseModel.registerModel(DatasourcesSqlModel);
CDFDDDatasourcesArray.push(new SqlEntry());



// XAction

var XactionResultSetEntry = PalleteEntry.extend({

  id: "XACTIONRESULTSET_ENTRY",
  name: "Xaction result set",
  description: "Use a result set returned from a xaction call in the Pentaho BI Server",
  category: "LEGACY",
  categoryDesc: "Legacy Datasources",

  getStub: function(){
    return DatasourcesXactionModel.getStub();
  }

});



var DatasourcesXactionModel = BaseModel.extend({
  },{
    MODEL: 'DatasourcesXactionModel',
    getStub: function(){
      var _stub = {
        id: TableManager.generateGUID(),
        type: DatasourcesXactionModel.MODEL,
        typeDesc: "Xaction resultset",
        parent: IndexManager.ROOTID,
        properties: []
      };

      _stub.properties.push(PropertiesManager.getProperty("name"));
      _stub.properties.push(PropertiesManager.getProperty("solution"));
      _stub.properties.push(PropertiesManager.getProperty("path"));
      _stub.properties.push(PropertiesManager.getProperty("xaction"));
      _stub.properties.push(PropertiesManager.getProperty("parameters"));

      return _stub;
    }
  });
BaseModel.registerModel(DatasourcesXactionModel);
CDFDDDatasourcesArray.push(new XactionResultSetEntry());

// Kettle
var KettleEntry = PalleteEntry.extend({

  id: "KETTLE_ENTRY",
  name: "Kettle transformation",
  description: "Executes a kettle transformation",
  category: "LEGACY",
  categoryDesc: "Legacy Datasources",

  getStub: function(){
    return DatasourcesKettleModel.getStub();
  }

});

var DatasourcesKettleModel = BaseModel.extend({
  },{
    MODEL: 'DatasourcesKettleModel',
    getStub: function(){
      var _stub = {
        id: TableManager.generateGUID(),
        type: DatasourcesKettleModel.MODEL,
        typeDesc: "Kettle transformation",
        parent: IndexManager.ROOTID,
        properties: []
      };

      _stub.properties.push(PropertiesManager.getProperty("name"));
      _stub.properties.push(PropertiesManager.getProperty("directory"));
      _stub.properties.push(PropertiesManager.getProperty("transformation"));
      _stub.properties.push(PropertiesManager.getProperty("importStep"));

      return _stub;
    }
  });
BaseModel.registerModel(DatasourcesKettleModel);
CDFDDDatasourcesArray.push(new KettleEntry());

var CDADataSourceEntry = PalleteEntry.extend({

  id: "CDA_DATASOURCE",
  name: "CDA Datasource",
  description: "CDA Data Source",
  category: "CDA",
  categoryDesc: "Community Data Access",

  getStub: function(){
    return CDADataSourceModel.getStub();
  }

});

var CDADataSourceModel = BaseModel.extend({
  },{
    MODEL: 'DatasourcesCDAModel',
    getStub: function(){
      var _stub = {
        id: TableManager.generateGUID(),
        type: CDADataSourceModel.MODEL,
        typeDesc: "CDA Datasource",
        parent: IndexManager.ROOTID,
        properties: []
      };

      _stub.properties.push(PropertiesManager.getProperty("name"));
      _stub.properties.push(PropertiesManager.getProperty("cdaPath"));
     // _stub.properties.push(PropertiesManager.getProperty("cdaEditor")); //TODO:remove?
      _stub.properties.push(PropertiesManager.getProperty("dataAccessId"));
	  _stub.properties.push(PropertiesManager.getProperty("outputIndexId"));
      return _stub;
    }
  });
BaseModel.registerModel(CDADataSourceModel);
CDFDDDatasourcesArray.push(new CDADataSourceEntry());

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
    var common = true,
    splitFile = file.split("/"),
    splitPath = cdfdd.getDashboardData().filename.split("/"),
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