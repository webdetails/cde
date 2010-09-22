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
			
			var content = '<select id="cdfddOlapCatalogSelect" onchange="MdxEntry.getEntry(\'MDX_ENTRY\').catalogSelected()"><option value="-"> Select catalog </option></select><br/>\
			<select id="cdfddOlapCubeSelect" onchange="MdxEntry.getEntry(\'MDX_ENTRY\').cubeSelected()" ><option value="-"> Select cube </option></select>';
						  
			$.prompt(content,{
				buttons: { Ok: true, Skip: false },
				loaded: function(){
					$.getJSON(CDFDDServerUrl + "OlapUtils", {operation: "GetOlapCubes"}, function(json) {
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
		getStub: function(){ var _stub = { id: TableManager.generateGUID(),
				type: DatasourcesMdxModel.MODEL,
				typeDesc: "Mdx query",
				parent: IndexManager.ROOTID, properties: [] };

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
		getStub: function(){ var _stub = { id: TableManager.generateGUID(),
				type: DatasourcesSqlModel.MODEL,
				typeDesc: "Sql query",
				parent: IndexManager.ROOTID, properties: [] };

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
		getStub: function(){ var _stub = { id: TableManager.generateGUID(),
				type: DatasourcesXactionModel.MODEL,
				typeDesc: "Xaction resultset",
				parent: IndexManager.ROOTID, properties: [] };

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
		getStub: function(){ var _stub = { id: TableManager.generateGUID(),
				type: DatasourcesKettleModel.MODEL,
				typeDesc: "Kettle transformation",
				parent: IndexManager.ROOTID, properties: [] };

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
		getStub: function(){ var _stub = { id: TableManager.generateGUID(),
				type: CDADataSourceModel.MODEL,
				typeDesc: "CDA Datasource",
				parent: IndexManager.ROOTID, properties: [] };

			_stub.properties.push(PropertiesManager.getProperty("name"));
			_stub.properties.push(PropertiesManager.getProperty("path"));
			_stub.properties.push(PropertiesManager.getProperty("cdaEditor"));
			_stub.properties.push(PropertiesManager.getProperty("dataAccessId"));

			return _stub;
		}
	});
BaseModel.registerModel(CDADataSourceModel);
CDFDDDatasourcesArray.push(new CDADataSourceEntry());


var CdaEditorRenderer = StringRenderer.extend({

			constructor: function(){
				this.base();
				this.logger = new Logger("CdaFileRenderer");
				this.logger.debug("Creating new CdaFileRenderer");
			},

			render: function(row,placeholder, getExpression,setExpression,editable){
				if(editable){
					this.setExpression = setExpression;
					this.row = row;
					var myself = this;  
                                        myself.getExpression = getExpression;
                                        var getgetter = function () {return getExpression;};
                                        var getrow = function () {return row;};
					var _editArea = $("<td><span id='editHere'>"+ getExpression(row) +"</span><br></td>");
                                        var _prompt = $('<button class="cdfddInput" style="width:auto">Edit this file</button>').bind("click",function(){
                                            CdaEditorRenderer.popup(myself.getExpression(myself.row));
                                        });
                                        _editArea.append(_prompt);
					_editArea.find("#editHere").editable(function(value,settings){
							myself.logger.debug("Saving new value: " + value );
							myself.setExpression(row,value);


							return value;
						} , {
							cssclass: "cdfddInput",
							select: true,
							onsubmit: function(settings,original){
								return myself.validate($('input',this).val());
							}
						});
					_editArea.appendTo(placeholder);

				}
				else{
					$("<td>"+ getExpression(row) +"</td>").appendTo(placeholder);
				}
			},
			validate: function(settings, original){
				return true;
			}

		},{
                        popup: function(path)  {

                                $("#wizardDialog").empty();
                                $("#wizardDialog").append("<iframe src='/pentaho/content/cda/editFile?path="+path+"&initialState=exEdit' width='100%' height='95%' ></iframe>");
                                $("#wizardDialog").append("<button onclick='$(\"#wizardDialog\").jqmHide()'class='cdfddInput' style='float:right;width: auto'>Close</button>");
                                $("#wizardDialog").jqmShow()
                        }
});
