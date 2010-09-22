
var WizardManager = Base.extend({

		wizardId: "BASEWIZARD",
		logger: {},
		title: "NOT SET",
		stub: {},

		constructor: function(){
			this.logger = new Logger("WizardManager" );

			// Register this tablemanager in the global area
			WizardManager.register(this);
		},

		init : function(){

			this.logger.info("Initializing WizardManager");

			this.resetAll();
			this.initWizard();

			// Initialize jqModal
			$("#" + WizardManager.MAIN_DIALOG).jqm({modal: true }); 

		},
		
		render: function(_stub){

			this.stub = _stub;
			this.init();
			this.resetWizard();
			this.renderWizard();
			$('#'+WizardManager.MAIN_DIALOG).jqmShow();
		
		},

		resetAll: function(){ 
			// reset All
			$("#"+WizardManager.MAIN_DIALOG).empty();
		},

		resetWizard: function(){ 
			// reset body
			$("#"+WizardManager.WIZARD_LEFT_SECTION).empty();
			$("#"+WizardManager.WIZARD_CENTER_SECTION).empty();
			//$("#"+WizardManager.WIZARD_RIGHT_SECTION).empty();
		},

		renderWizard: function(){
			this.logger.warn("renderWizard: Method not implmemented");
			// $("#" + WizardManager.WIZARD_BODY).append( content );
		},

		initWizard: function(){
		
			var wizard = '\
			<div class="container">  \
			<div class="span-24 last round cdfdd-wizard-title"><h1>' + this.getTitle() + '</h1></div>\
			<div id="wizardDialogBody">\
			  <form onsubmit="return false">\
				<div id="wizardDialogLeftSection" class="cdfdd-wizard-left span-5 round">\
				</div>\
				<div id="wizardDialogCenterSection" class="cdfdd-wizard-center span-19 last round ">\
				</div>\
			  </form>\
			</div>\
			<!-- buttons -->\
			<div class="clear span-15" >&nbsp;</div>\
			<div class="cdfdd-wizard-buttons span-9 last round">\
			<form onsubmit="return false;">\
			<input id="cdfdd-wizard-button-ok" type="button" disabled onclick="WizardManager.getWizardManager(\''+ this.getWizardId() +'\').buttonOk()" value="Ok"></input> \
			<input id="cdfdd-wizard-button-cancel" type="button" onclick="WizardManager.getWizardManager(\''+ this.getWizardId() +'\').buttonCancel()" value="Cancel"></input>\
			</form>\
			</div>\
			';

			$("#" + WizardManager.MAIN_DIALOG).append( wizard );
			
			$(".cdfdd-wizard-title").corner();
		},

		
		buttonOk: function(){

			this.logger.warn("OK Button pressed - not implemented yet");
			$('#'+ WizardManager.MAIN_DIALOG).jqmHide();
			return false;
			
		},
		
		buttonCancel: function(){

			this.logger.warn("Cancel Button pressed - not implemented yet");
			$('#'+ WizardManager.MAIN_DIALOG).jqmHide();
			
		},
		
		// Accessors
		setWizardId: function(wizardId){this.wizardId = wizardId},
		getWizardId: function(){return this.wizardId},
		setTitle: function(title){this.title = title},
		getTitle: function(){return this.title}

	},{
		MAIN_DIALOG: "wizardDialog",
		WIZARD_BODY: "wizardDialogBody",
		WIZARD_LEFT_SECTION: "wizardDialogLeftSection",
		WIZARD_CENTER_SECTION: "wizardDialogCenterSection",
		WIZARD_RIGHT_SECTION: "wizardDialogRightSection",

		wizardManagers: {},

		register: function(wizardManager){
			WizardManager.wizardManagers[wizardManager.getWizardId()] = wizardManager;
		},

		getWizardManager: function(id){
			return WizardManager.wizardManagers[id];
		},

		executeOperation: function(wizardManagerId,idx){

			var wizardManager = WizardManager.getWizardManager(wizardManagerId);
			wizardManager.getOperations()[idx].execute(wizardManager);
		},

		globalInit: function(){

			// Enable the table selectors
			$("td.draggableDimension, td.draggableMeasure, td.draggableFilter").live("mouseover",function() {
					var myself = $(this);

					myself.parent().parent().find("td").removeClass("ui-state-active");
					myself.addClass("ui-state-active");

				});
		}

	});


var OlapWizard = WizardManager.extend({

		wizardId: "OLAP_WIZARD",
		title: "OLAP Wizard",
		catalogs: {},
		olapObjects: {DimensionTableData: [],MeasureTableData: [],FilterTableData : []},
		selectedWizardObjects: {rows:[], columns:[], filters:[]},
		selectedOptions: {},
		levelType: "",
		levelDirection: "",

		constructor: function(){
			this.base();
			this.logger = new Logger("OlapWizard" );
		},

		init: function(){
			this.base();
			this.resetOlapObjects();
			this.resetSelectedWizardObjects();
		},

		renderWizard: function(){
		
			
			var leftSectionContent = '\
				<div class="cdfdd-wizard-olap-cubes span-5 round last">\
					<h3>Cube:</h3>\
					    <div id="cdfddOlapCubeSelector" class="centeredText"></div>\
					</div>\
					<div class="cdfdd-wizard-olap-dimensions span-5 round last">\
					    <div id="cdfddOlapDimensionDiv" class="centeredText">\
							<table id="cdfddOlapDimensionSelector" class="cdfdd small ui-reset ui-clearfix ui-component ui-hover-state">\
							<caption class="ui-state-hover">Dimensions</caption>\
							<thead>\
							</thead>\
							<tbody class="ui-widget-content">\
							</tbody>\
							</table>\
							<table id="cdfddOlapMeasureSelector" class="cdfdd small ui-reset ui-clearfix ui-component ui-hover-state">\
							<caption class="ui-state-hover">Measures</caption>\
							<thead>\
							</thead>\
							<tbody class="ui-widget-content">\
							</tbody>\
							</table>\
							<table id="cdfddOlapFilterSelector" class="cdfdd small ui-reset ui-clearfix ui-component ui-hover-state">\
							<caption class="ui-state-hover">Filters</caption>\
							<thead>\
							</thead>\
							<tbody class="ui-widget-content">\
							</tbody>\
							</table>\
						</div>\
					</div>';
					
			var centerSectionContent = '\
				<div class="span-19 round last">\
					<div class="span-13">\
						<div id="cdfdd-olap-preview" >\
							<i>Preview Area:</i><input id="cdfdd-olap-enable-preview" type="checkbox" onchange="WizardManager.getWizardManager(\''+ this.getWizardId() + '\').processChange()" value="true" checked="checked"></input>\
							<div id="cdfdd-olap-preview-area"></div>\
						</div>\
						<div id="cdfdd-olap-rows" class="cdfdd-olap-droppable round"><i>Rows: </i><br/><span class="options"></span></div>\
						<div id="cdfdd-olap-columns" class="cdfdd-olap-droppable round"><i>Columns: </i><br/><span class="options"></span></div>\
						<div id="cdfdd-olap-filters" class="cdfdd-olap-droppable round"><i>Filters: </i><br/><span class="options"></span></div>\
					</div>\
					<div id="cdfdd-component-options" class="span-6 cdfdd-wizard-components-options last round"></div>\
				</div>';
				
			$("#" + WizardManager.WIZARD_LEFT_SECTION).append( leftSectionContent );
			$("#" + WizardManager.WIZARD_CENTER_SECTION).append( centerSectionContent );

			this.renderMainOlapArea();
			this.renderCubeSelector();

			$(".round","#" + WizardManager.WIZARD_BODY).corner();

			// Accept droppable
			var myself = this;
			$("#cdfdd-olap-rows").droppable({
					accept: this.getDroppableRows(),
					hoverClass: 'ui-state-active',	
					drop: function(ev,ui){
						WizardManager.getWizardManager(myself.wizardId).processDroppable("rows",$(this),$(ui.draggable));
					}
				});
			$("#cdfdd-olap-columns").droppable({
					accept: this.getDroppableColumns(),
					hoverClass: 'ui-state-active',	
					drop: function(ev,ui){
						WizardManager.getWizardManager(myself.wizardId).processDroppable("columns",$(this),$(ui.draggable));
					}
				});
				
			$("#cdfdd-olap-filters").droppable({
					accept: this.getDroppableFilters(),
					hoverClass: 'ui-state-active',	
					drop: function(ev,ui){
						WizardManager.getWizardManager(myself.wizardId).processDroppable("filters",$(this),$(ui.draggable));
					}
				});
		
		},
		
		getDroppableRows: function(){
			this.logger.error("getDroppableRows not implemented");
		},

		getDroppableColumns: function(){
			this.logger.error("getDroppableColumns not implemented");
		},

		getDroppableFilters: function(){
			this.logger.error("getDroppableFilters not implemented");
		},

		getMainOlapArea: function(){
		
			this.logger.error("getMainOlapArea not implemented");
		},

		renderCubeSelector: function(){
		
			// Fetch list of cubes from server
			var params = {
				operation: "GetOlapCubes"
			};
			var myself = this;

			$.getJSON(CDFDDServerUrl + "OlapUtils", params, function(json) {
					if(json.status == "true"){

						var catalogs = json.result.catalogs;
						myself.setCatalogs(catalogs);

						myself.logger.info("Got correct response from getCubes: " + catalogs);

						var _selector = $("#cdfddOlapCubeSelector");
						_selector.append(
							'<select id="cdfddOlapCatalogSelect" class="small" onchange="WizardManager.getWizardManager(\''+ myself.wizardId +'\').catalogSelected()"><option value="-"> Select catalog </option></select><br/>');
						_selector.append(
							'<select id="cdfddOlapCubeSelect" class="small" onchange="WizardManager.getWizardManager(\''+ myself.wizardId +'\').cubeSelected()" ><option value="-"> Select cube </option></select>');

						$.each(catalogs,function(i,catalog){
								$("select#cdfddOlapCatalogSelect",_selector).append("<option>"+catalog.name+"</option>");
							});

					}
					else {
						alert(json.result)
					}
				});

		},

		renderDimensions: function(){
			this.logger.warn("renderDimensions - Not done yet");
		},

		catalogSelected: function(){
		
			var _selector = $("#cdfddOlapCubeSelector");
			var selectedCatalog = this.getCatalog();
		
			var cubes = selectedCatalog.cubes;
			$("select#cdfddOlapCubeSelect",_selector).empty();
			$.each(cubes,function(i,cube){
					$("select#cdfddOlapCubeSelect",_selector).append("<option>"+cube.name+"</option>");
				});
			this.cubeSelected();

		},

		cubeSelected: function(){

			var selectedCatalog = $("#cdfddOlapCatalogSelect").val();
			var selectedCube = $("#cdfddOlapCubeSelect").val();
			this.logger.debug("Cube Selected: " + selectedCube);
			
			this.resetOlapObjects();
			this.resetSelectedWizardObjects();

			// Fetch dimension structure of selected cube from server
			var params = {
				operation: "GetCubeStructure",
				catalog: selectedCatalog,
				cube: selectedCube
			};
			var myself = this;

			$.getJSON(CDFDDServerUrl + "OlapUtils", params, function(json) {
					if(json.status == "true"){

						myself.logger.info("Got correct response from GetCubeStructure");

						var dimensions = json.result.dimensions;

						var dimensionIdx = 0;
						var dimensionTBody = $("#cdfddOlapDimensionSelector > tbody");
						dimensionTBody.empty();
						$.each(dimensions,function(i,dimension){
								var hierarchies = dimension.hierarchies;
								$.each(hierarchies,function(j,hierarchy){
										var hierarchyId = "dimRow-"+(++dimensionIdx);
										dimensionTBody.append("<tr id='"+ hierarchyId +"'><td>"+hierarchy.name+"</td></tr>");

										var levels = hierarchy.levels;
										$.each(levels,function(k,level){
												var levelId = "dimRow-"+(++dimensionIdx);
												dimensionTBody.append("<tr id='"+ levelId +"' class='olapObject child-of-"+hierarchyId+"'><td class='draggableDimension'>"+level.name+"</td></tr>");
												level.hierarchy = hierarchy;level.catalog = selectedCatalog;level.cube = selectedCube;
												myself.addOlapObject(WizardOlapObjectManager.DIMENSION,level);
											});
									});


							});
						dimensionTBody.parent().treeTable();
						$("td.draggableDimension",dimensionTBody).draggable({helper:'clone'});

						// Measures

						var measures = json.result.measures;

						var measureIdx = 0;
						var measureTBody = $("#cdfddOlapMeasureSelector > tbody");
						measureTBody.empty();
						$.each(measures,function(i,measure){
								var measureId = "levelRow-"+(++measureIdx);
								measureTBody.append("<tr id='"+ measureId +"' class='olapObject'><td class='draggableMeasure'>"+measure.name+"</td></tr>");
								myself.addOlapObject(WizardOlapObjectManager.MEASURE,measure);

							});
						measureTBody.parent().treeTable();
						$("td.draggableMeasure",measureTBody).draggable({helper:'clone',type: "Measure"});

						myself.getAvailableFilters();
					}
					else {
						alert(json.result)
					}
				});

		},
		
		getAvailableFilters: function(){

			var myself = this;
			var filters = Panel.getPanel(ComponentsPanel.MAIN_PANEL).getParameters(); 
			var filterTBody = $("#cdfddOlapFilterSelector > tbody");
			filterTBody.empty();
			$.each(filters,function(i,filter){
					if(filter.dimension != undefined){
						filterTBody.append("<tr class='olapObject'><td class='draggableFilter'\">"+filter.properties[0].value+"</td></tr>");
						myself.addOlapObject(WizardOlapObjectManager.FILTER,filter);
					}
				});
			filterTBody.parent().treeTable();
			$("td.draggableFilter",filterTBody).draggable({helper:'clone'});
		},
		
		processDroppable: function(type,drop,drag){
			var myself = this;
			var levelIdx = drag.parent().prevAll("tr.olapObject").length;
			var wizardObjectType  = drag.attr("class").replace(/draggable/,"").split(" ")[0];
			
			//1. Add selected olap Object
			var wizardObject = this.addSelectedWizardObject(type,wizardObjectType,this.getOlapObject(wizardObjectType,levelIdx));
			
			//2. Render olap Object
			var container = drop.find(".options");
			var htmlWizardObject = wizardObject.render(container);

			//3. Add clear button.
			var clearButtonContainer = $('<div class="cdfdd-olap-clearButton"></div>');
			var clearButton = $('<a border="0"><img src="getResource?resource=/images/clear.gif">&nbsp;</a>');
			$(clearButton).bind('click',function(){
				myself.removeSelectdWizardObject(type,wizardObject);clearButtonContainer.remove();myself.processChange()});
			container.append(clearButtonContainer.append(clearButton));
			
			this.processChange();
		},
		
		removeSelectdWizardObject: function(type, object){
			var index = this.selectedWizardObjects[type].indexOf(object);
			this.selectedWizardObjects[type][index].clear();
			this.selectedWizardObjects[type].splice(index,1);
		},

		processChange: function(){
			this.logger.warn("processChange - Not done yet");
		},

		getCatalog: function(){
		
			var catalog;
			var selectedCatalog = $("#cdfddOlapCatalogSelect").val();
			$.each(this.getCatalogs(),function(i,cat){
			
				if(cat.name == selectedCatalog){
					catalog=cat;
					return false;
				}
			});
			return catalog;

		},

		getCube: function(){
			return $("#cdfddOlapCubeSelect").val();
		},

		// Accessors
		setCatalogs: function(catalogs){this.catalogs = catalogs},
		getCatalogs: function(){return this.catalogs},
		
		setSelectedOptions: function(selectedOptions){this.selectedOptions = selectedOptions},
		getSelectedOptions: function(){return this.selectedOptions},
		
		addOlapObject: function(type,object){
			this.olapObjects[type + "TableData"].push(object);
		},
		
		getOlapObject: function(type,index){return this.olapObjects[type + "TableData"][index]},
		
		addSelectedWizardObject: function(type,olapType,object){
			var selectedWizarObject = WizardOlapObjectManager.getOlapObject(olapType,object);
			var myself = this;
			selectedWizarObject.setProcessChange(function(){myself.processChange()});
			this.selectedWizardObjects[type].push(selectedWizarObject);
			return selectedWizarObject;
		},
		
		getSelectedWizardObject: function(type,index){return this.selectedWizardObjects[type][index]},
		
		getSelectedRowsValue: function(preview){
			var rows = [];
			for(o in this.selectedWizardObjects.rows){
				rows.push(this.selectedWizardObjects.rows[o].getValue(preview));
			}
			return rows;
		},
		
		getSelectedColumnsValue: function(preview){
			var columns = [];
			for(o in this.selectedWizardObjects.columns){
				columns.push(this.selectedWizardObjects.columns[o].getValue(preview));
			}
			return columns;
		},
		
		getListenners: function(){
			var listeners = "[";
			for(objs in this.selectedWizardObjects)
				for(o in this.selectedWizardObjects[objs]){
					if(this.selectedWizardObjects[objs][o].getId != undefined) listeners+= "\"" + this.selectedWizardObjects[objs][o].getId() + "\",";
				}
			return listeners.length > 1 ? listeners.replace(/,$/,"]") : "";
		},
		
		// Resets
		
		resetOlapObjects: function(){
			this.olapObjects = {DimensionTableData: [],MeasureTableData: [],FilterTableData : []};
		},
		
		resetSelectedWizardObjects: function(){
			this.selectedWizardObjects = {rows:[], columns:[], filters:[]};
		},

		buildQuery: function(preview,topCount){
			
			var rows = this.getSelectedRowsValue(preview).join(" * ");
			var columns = this.getSelectedColumnsValue(preview).join(" * ");
			var cube = this.getSelectedOptions().cube;
			var sets = [];
			var members = [];
			var conditions = [];
			
			for(o in this.selectedWizardObjects.filters){
				var filterValue = this.selectedWizardObjects.filters[o].getFilterValue(preview);
				if(typeof filterValue == 'object'){ 
					if(filterValue.set != undefined) sets.push(filterValue.set);
					if(filterValue.member != undefined) members.push(filterValue.member);
					if(filterValue.condition != undefined) conditions.push(filterValue.condition);
				}
				else
					conditions.push(filterValue);
			}
			
			var preRows = topCount != undefined ? "TopCount(" : "";
			var posRows = topCount != undefined ? "," + topCount + ")" : "";
			var nonEmptyPreStr = columns.length > 0 ? "NON EMPTY(" : "";
			var nonEmptyPosStr = columns.length > 0 ? ")" : "";
			
			var query = sets.join(" , \n") + members.join(" , \n") + " select " + nonEmptyPreStr + preRows + "{" + rows + "} " + posRows + nonEmptyPosStr + " on ROWS, \n " + nonEmptyPreStr + "{" + columns + "}" + nonEmptyPosStr + " on Columns \n from [" + cube + "]";
		
			if(conditions.length > 0)
				query += "\n where (" + conditions.join(" , ") + ")";
		
			return sets.length > 0  || members.length > 0 ? "with \n" + query : query;
		
		}

	},{
	});


var OlapParameterWizard = OlapWizard.extend({

		wizardId: "OLAP_PARAMETER_WIZARD",

		constructor: function(){
			this.base();
			this.logger = new Logger("OlapParameterWizard" );
		},

		renderMainOlapArea: function(){
			
			var content = $('\
			<div class="cdfdd-component-options-label">Name: <input id="cdfdd-olap-parameter-name" class="invalid" type="text" onchange="WizardManager.getWizardManager(\''+ this.getWizardId() +'\').processChange()" ></input></div>\
			<hr/>\
			<div class="cdfdd-component-options-label">Type:<select class="cdfdd-component-options-type" onchange="WizardManager.getWizardManager(\''+ this.getWizardId() +'\').processChange()" id="cdfdd-olap-parameter-type" name="cdfdd-olap-parameter-type"></select></div>\
			<div class="cdfdd-component-options-label">Html Object:<select class="cdfdd-component-options-htmlobject" onchange="WizardManager.getWizardManager(\''+ this.getWizardId() +'\').processChange()" id="cdfdd-olap-parameter-htmlobject" name="cdfdd-olap-parameter-htmlobject"></select></div>\
			<div class="cdfdd-component-options-label">Top Count:<select class="cdfdd-component-options-topCount" onchange="WizardManager.getWizardManager(\''+ this.getWizardId() +'\').processChange()" id="cdfdd-olap-parameter-topcount" name="cdfdd-olap-parameter-topcount"></select></div>\
			');

			var olapMain = $("#cdfdd-component-options");
			var selector = $("#cdfdd-olap-parameter-type",content);
			selector.append('<option value="selectComponent">Select</option>');
			selector.append('<option value="selectMultiComponent">Select Multiple</option>');
			selector.append('<option value="checkComponent">Check Box</option>');
			selector.append('<option value="radioComponent">Radio Box</option>');
			selector.append('<option value="autocompleteBoxComponent">Autocomplete</option>');
			var topCountSelector = $("#cdfdd-olap-parameter-topcount",content);
			var topCounts = ["",5,10,15,20,25,50,100];
			for(t in topCounts)
				topCountSelector.append('<option value="' + topCounts[t] +'">' + topCounts[t] +'</option>');
			topCountSelector.val(50);
			
			var data = Panel.getPanel(LayoutPanel.MAIN_PANEL).getHtmlObjects();
			var htmlObject = $("#cdfdd-olap-parameter-htmlobject",content);
			$.each(data,function(i,val){
					htmlObject.append('<option value="' + val.properties[0].value + '" : >' + val.properties[0].value + '</option>');
				});
			
			olapMain.html(content);

		},

		getDroppableRows: function(){
			return "td.draggableDimension, td.draggableMeasure, td.draggableFilter";
		},

		getDroppableColumns: function(){
			return "td.draggableDimension, td.draggableMeasure, td.draggableFilter";
		},

		getDroppableFilters: function(){
			return "td.draggableDimension, td.draggableMeasure, td.draggableFilter";
		},

		processChange: function(){
			
			// Clean preview
			$("#cdfdd-olap-preview-area").empty();
			var preview = $("#cdfdd-olap-enable-preview:checked").length>0;
			var catalog = this.getCatalog();
			var valid = this.selectedWizardObjects.rows.length > 0 && catalog != undefined;
			var topCount = $("#cdfdd-olap-parameter-topcount").val();

			if(valid & preview){

				this.getSelectedOptions().name = $("#cdfdd-olap-parameter-name").val();
				this.getSelectedOptions().type = $("#cdfdd-olap-parameter-type").val();
				this.getSelectedOptions().jndi = catalog.jndi;
				this.getSelectedOptions().schema = catalog.schema;
				this.getSelectedOptions().cube = this.getCube();

				this.getSelectedOptions().query = this.buildQuery(true,topCount.length > 0 ? topCount : undefined);
				this.preview();
			}

			// Name
			var name = $("#cdfdd-olap-parameter-name").val();
			if(name.length>0){
				$("#cdfdd-olap-parameter-name").removeClass("invalid");
				valid = valid & true;
			}
			else{
				$("#cdfdd-olap-parameter-name").addClass("invalid");
				valid = false;
			}

			if(valid){
				$("#cdfdd-wizard-button-ok").removeAttr("disabled");
			}
			else{
				$("#cdfdd-wizard-button-ok").attr("disabled","disabled");
			}
				
		},

		preview: function(){
			
			this.logger.debug("Launching preview");

			// Build cdf component

			CDFDDPreviewComponent = 
				{
					name: "CDFDDPreviewComponent",
					type: this.getSelectedOptions().type,
					valueAsId: true,
					valuesArray: [['1','Lisbon'],['2','Dusseldorf']],
					size: this.getSelectedOptions().type=="selectMultiComponent"?5:1,
					selectMulti: true,
					queryDefinition: {
						queryType: 'mdx',
						jndi: this.getSelectedOptions().jndi,
						catalog: this.getSelectedOptions().schema,
						query: this.getSelectedOptions().query
					},

					htmlObject: "cdfdd-olap-preview-area",
					executeAtStart: true,
					preChange: function(c){return false}
				};
			Dashboards.components = [];
			Dashboards.init([CDFDDPreviewComponent]);
		
		},


		buttonOk: function(){

			this.apply();
			$('#'+ WizardManager.MAIN_DIALOG).jqmHide();

			
		},

		apply: function(){
		
			// Generate parameter

			$('#'+ WizardManager.MAIN_DIALOG).jqmHide();

			// 1 - Add query to datasources
			// 2 - Add parameter to components
			// 3 - Add selector to components

			// 1
			var datasourcesPalleteManager = PalleteManager.getPalleteManager(DatasourcesPanel.PALLETE_ID);
			var datasourcesTableManager = datasourcesPalleteManager.getLinkedTableManager();

			var queryModel = BaseModel.getModel(DatasourcesMdxModel.MODEL);
			var datasourceStub = queryModel.getStub();

			CDFDDUtils.getProperty(datasourceStub,"name").value = this.getSelectedOptions().name+"Query";
			CDFDDUtils.getProperty(datasourceStub,"jndi").value = this.getSelectedOptions().jndi;
			CDFDDUtils.getProperty(datasourceStub,"catalog").value = this.getSelectedOptions().schema;
			CDFDDUtils.getProperty(datasourceStub,"cube").value = this.getSelectedOptions().cube;
			CDFDDUtils.getProperty(datasourceStub,"mdxquery").value = this.buildQuery(false);

			var mdxEntry = new MdxEntry();
			var insertAtIdx = datasourcesTableManager.createOrGetParent(mdxEntry.getCategory(), mdxEntry.getCategoryDesc());
			datasourceStub.parent = mdxEntry.getCategory();
			datasourcesTableManager.insertAtIdx(datasourceStub,insertAtIdx);

			// 2
			var componentsPalleteManager = PalleteManager.getPalleteManager(ComponentsPanel.PALLETE_ID);
			var componentsTableManager = componentsPalleteManager.getLinkedTableManager();

			var parameterModel = BaseModel.getModel(ComponentsOlapParameterModel.MODEL);
			var parameterStub = parameterModel.getStub();
			var parameterId = this.getSelectedOptions().name + "Parameter"
			var dimension = this.selectedWizardObjects.rows[0].member;
			var type =  this.getSelectedOptions().type;
			
			CDFDDUtils.getProperty(parameterStub,"name").value = parameterId;
			CDFDDUtils.getProperty(parameterStub,"propertyValue").value = dimension;
			
			parameterStub.dimension = dimension;

			var parameterEntry = new ParameterEntry();
			var insertAtIdx = componentsTableManager.createOrGetParent(parameterEntry.getCategory(), parameterEntry.getCategoryDesc());
			parameterStub.parent = parameterEntry.getCategory();
			componentsTableManager.insertAtIdx(parameterStub,insertAtIdx);
			
			// 3
			
			var model = "";
			if (type == "selectComponent"){
				model = ComponentsSelectModel.MODEL;
			}else if (type == "selectMultiComponent"){
				model = ComponentsSelectMultiModel.MODEL;
			}else if (type == "autocompleteBoxComponent"){
				model = ComponentsAutoCompleteBoxModel.MODEL;
			}else if (type == "radioComponent"){
				model = ComponentsRadioModel.MODEL;
			}else if (type == "checkComponent"){
				model = ComponentsCheckModel.MODEL;
			}else{
				this.logger.error("Not done yet!");
				alert("Type" + type +" not implemented");
			}

			var selectorModel = BaseModel.getModel(model);
			var selectorStub = selectorModel.getStub();
			
			CDFDDUtils.getProperty(selectorStub,"name").value = this.getSelectedOptions().name+"Selector";
			CDFDDUtils.getProperty(selectorStub,"parameter").value = this.getSelectedOptions().name+"Parameter";
			CDFDDUtils.getProperty(selectorStub,"dataSource").value = this.getSelectedOptions().name+"Query";
			CDFDDUtils.getProperty(selectorStub,"htmlObject").value =  $("#cdfdd-olap-parameter-htmlobject").val();
			CDFDDUtils.getProperty(selectorStub,"preChange").value =  this.getJavaScriptParameterFunction(type , dimension);;
			
			var listenners = this.getListenners();
			if(listenners.length > 0)
				CDFDDUtils.getProperty(selectorStub,"listeners").value = listenners;

			var selectEntry = new SelectEntry();
			var insertAtIdx = componentsTableManager.createOrGetParent(selectEntry.getCategory(), selectEntry.getCategoryDesc());
			selectorStub.parent = selectEntry.getCategory();
			componentsTableManager.insertAtIdx(selectorStub,insertAtIdx);

		},
		
		getJavaScriptParameterFunction: function(type, member){
			var _function = 'function(value){\n\nif(value == "" || value == "' + member + '")\n\treturn "' + member + '";\n';
			if(type == "selectComponent" || type =="radioComponent")
				_function+= 'else\n\tvalue = "' + member + '.[" + value + "]";\n';
			else
				_function+= 'else{\n\tvar values = []; \n\tfor(v in value) \n\tvalues.push("' + member + '.[" + value[v] + "]");\n\tvalue = values.join(\',\');\n}\n';
				
			return _function + '\nreturn value;\n\n}';
		}


	},{
	
	});
var wizard = new OlapParameterWizard();


var OlapChartWizard = OlapWizard.extend({

		wizardId: "OLAP_CHART_WIZARD",

		constructor: function(){
			this.base();
			this.logger = new Logger("OlapChartWizard" );
		},

		renderMainOlapArea: function(){
			
			var content = $('\
			<div class="cdfdd-component-options-label">Name: <input id="cdfdd-olap-parameter-name" class="invalid" type="text" onchange="WizardManager.getWizardManager(\''+ this.getWizardId() +'\').processChange()" ></input></div>\
			<hr/>\
			<div class="cdfdd-component-options-label">Type:<select class="cdfdd-component-options-type" onchange="WizardManager.getWizardManager(\''+ this.getWizardId() +'\').processChange()" id="cdfdd-olap-parameter-type" name="cdfdd-olap-parameter-type"></select></div>\
			<div class="cdfdd-component-options-label">Html Object:<select class="cdfdd-component-options-htmlobject" onchange="WizardManager.getWizardManager(\''+ this.getWizardId() +'\').processChange()" id="cdfdd-olap-parameter-htmlobject" name="cdfdd-olap-parameter-htmlobject"></select></div>\
			<div class="cdfdd-component-options-label">Chart Type:<select class="cdfdd-component-options-chartType" select onchange="WizardManager.getWizardManager(\''+ this.getWizardId() +'\').processChange()" id="cdfdd-olap-parameter-chart-type" name="cdfdd-olap-parameter-chart-type"></select></div>\
			<div class="cdfdd-component-options-label">Orientation:<select class="cdfdd-component-options-orientation" onchange="WizardManager.getWizardManager(\''+ this.getWizardId() +'\').processChange()" id="cdfdd-olap-parameter-orientation" name="cdfdd-olap-parameter-orientation"></select></div>\
			<div class="cdfdd-component-options-label">Top Count:<select class="cdfdd-component-options-topCount" onchange="WizardManager.getWizardManager(\''+ this.getWizardId() +'\').processChange()" id="cdfdd-olap-parameter-topcount" name="cdfdd-olap-parameter-topcount"></select></div>\
			');
			
			var appendOption = function(obj, options){
				for(v in options)
					obj.append('<option value="' + options[v][0] + '">' + options[v][1] + '</option>');
			};

			var componentOptions = $("#cdfdd-component-options");
			var typeSelector = $("#cdfdd-olap-parameter-type",content);
			appendOption(typeSelector,[["BarChart","Bar Chart"],["PieChart","Pie Chart"],["LineChart","Line Chart"]]);
			var topCountSelector = $("#cdfdd-olap-parameter-topcount",content);
			appendOption(topCountSelector,[["",""],["5","5"],["10","10"],["15","15"]]);
			var chartTypeSelector = $("#cdfdd-olap-parameter-chart-type",content);
			appendOption(chartTypeSelector,[["jFreeChart","JFree Chart"],["openFlashChart","OpenFlash Chart"]]);
			var orientationSelector = $("#cdfdd-olap-parameter-orientation",content);
			appendOption(orientationSelector,[["horizontal","Horizontal"],["vertical","Vertical"]]);
			componentOptions.html(content);
			
			var data = Panel.getPanel(LayoutPanel.MAIN_PANEL).getHtmlObjects();
			var htmlObject = $("#cdfdd-olap-parameter-htmlobject",content);
			$.each(data,function(i,val){
					htmlObject.append('<option value="' + val.properties[0].value + '" : >' + val.properties[0].value + '</option>');
				});
		},

		getDroppableRows: function(){
			return "td.draggableDimension, td.draggableMeasure, td.draggableFilter";
		},

		getDroppableColumns: function(){
			return "td.draggableDimension, td.draggableMeasure, td.draggableFilter";
		},

		getDroppableFilters: function(){
			return "td.draggableDimension, td.draggableMeasure, td.draggableFilter";
		},

		processChange: function(){
			
			// Clean preview
			$("#cdfdd-olap-preview-area").empty();
			var preview = $("#cdfdd-olap-enable-preview:checked").length>0;
			var catalog = this.getCatalog();
			var valid = this.selectedWizardObjects.rows.length > 0 && this.selectedWizardObjects.columns.length > 0  && catalog != undefined;;

			if(valid & preview){

				this.getSelectedOptions().name = $("#cdfdd-olap-parameter-name").val();
				this.getSelectedOptions().chartType = $("#cdfdd-olap-parameter-chart-type").val();
				this.getSelectedOptions().type = $("#cdfdd-olap-parameter-type").val();
				this.getSelectedOptions().topCount = $("#cdfdd-olap-parameter-topcount").val();
				this.getSelectedOptions().orientation = $("#cdfdd-olap-parameter-orientation").val();
				this.getSelectedOptions().jndi = catalog.jndi;
				this.getSelectedOptions().schema = catalog.schema;
				this.getSelectedOptions().cube = this.getCube();

				this.getSelectedOptions().query = this.buildQuery(true);
				this.preview();
			}

			// Name
			var name = $("#cdfdd-olap-parameter-name").val();
			if(name.length>0){
				$("#cdfdd-olap-parameter-name").removeClass("invalid");
				valid = valid & true;
			}
			else{
				$("#cdfdd-olap-parameter-name").addClass("invalid");
				valid = false;
			}

			if(valid){
				$("#cdfdd-wizard-button-ok").removeAttr("disabled");
			}
			else{
				
				$("#cdfdd-wizard-button-ok").attr("disabled","disabled");
			}
				
		},

		preview: function(){
			
			this.logger.debug("Launching preview");

			// Build cdf component
			CDFDDPreviewComponentDefinition = 
				{
					width: 380,
					height: 175,
					chartType: this.getSelectedOptions().type,
					datasetType: "CategoryDataset",
					is3d: false,
					byRow: false,
					isStacked: false,
					includeLegend: false,
					interiorGap: 0.4,
					domainLabelRotation: 0,
					backgroundColor: "#E2F0B7",
					title: "",
					topCount: this.getSelectedOptions().topCount,
					orientation: this.getSelectedOptions().orientation,
					queryType: 'mdx',
					jndi: this.getSelectedOptions().jndi,
					catalog: this.getSelectedOptions().schema,
					query: this.getSelectedOptions().query					
				};
				
			CDFDDPreviewComponent = 
				{
					name: "CDFDDPreviewComponent",
					type: this.getSelectedOptions().chartType,
					chartDefinition: CDFDDPreviewComponentDefinition,
					htmlObject: "cdfdd-olap-preview-area",
					executeAtStart: true
				};	
				
				
			Dashboards.components = [];
			Dashboards.init([CDFDDPreviewComponent]);

		},


		buttonOk: function(){

			this.apply();
			$('#'+ WizardManager.MAIN_DIALOG).jqmHide();

			
		},

		apply: function(){
		
			// Generate parameter

			//var olapParameter = BaseModel.getModel('ComponentsParameter');

			// Populate main stub
			$('#'+ WizardManager.MAIN_DIALOG).jqmHide();


			// 1. Add query to datasources
			// 2. Add chart to components
			
			var datasourcesPalleteManager = PalleteManager.getPalleteManager(DatasourcesPanel.PALLETE_ID);
			var datasourcesTableManager = datasourcesPalleteManager.getLinkedTableManager();

			var queryModel = BaseModel.getModel(DatasourcesMdxModel.MODEL);
			var datasourceStub = queryModel.getStub();

			CDFDDUtils.getProperty(datasourceStub,"name").value = this.getSelectedOptions().name+"Query";
			CDFDDUtils.getProperty(datasourceStub,"jndi").value = this.getSelectedOptions().jndi;
			CDFDDUtils.getProperty(datasourceStub,"catalog").value = this.getSelectedOptions().schema;
			CDFDDUtils.getProperty(datasourceStub,"cube").value = this.getSelectedOptions().cube;
			CDFDDUtils.getProperty(datasourceStub,"mdxquery").value = this.buildQuery(false);


			var mdxEntry = new MdxEntry();
			var insertAtIdx = datasourcesTableManager.createOrGetParent(mdxEntry.getCategory(), mdxEntry.getCategoryDesc());
			datasourceStub.parent = mdxEntry.getCategory();
			datasourcesTableManager.insertAtIdx(datasourceStub,insertAtIdx);

			
			// 2
			//var type =  this.getSelectedOptions().type;
			
			var componentsPalleteManager = PalleteManager.getPalleteManager(ComponentsPanel.PALLETE_ID);
			var componentsTableManager = componentsPalleteManager.getLinkedTableManager();
			var cindexManager = componentsTableManager.getTableModel().getIndexManager();
			var model = this.getSelectedOptions().chartType == "jFreeChart" ? ComponentsJFreeChartModel.MODEL : ComponentsOpenFlashChartModel.MODEL ;
			

			var chartModel = BaseModel.getModel(model);
			var chartStub = chartModel.getStub();
			

			CDFDDUtils.getProperty(chartStub,"name").value = this.getSelectedOptions().name+"Chart";
			CDFDDUtils.getProperty(chartStub,"chartType").value = this.getSelectedOptions().type;
			CDFDDUtils.getProperty(chartStub,"htmlObject").value = $("#cdfdd-olap-parameter-htmlobject").val();
			CDFDDUtils.getProperty(chartStub,"dataSource").value = this.getSelectedOptions().name+"Query";
			CDFDDUtils.getProperty(chartStub,"topCount").value = this.getSelectedOptions().topCount;
			CDFDDUtils.getProperty(chartStub,"height").value = "300";
			CDFDDUtils.getProperty(chartStub,"width").value = "400";
			
			var listenners = this.getListenners();
			if(listenners.length > 0)
				CDFDDUtils.getProperty(chartStub,"listeners").value = listenners;

			var chartEntry = new JFreeChartEntry();
			var insertAtIdx = componentsTableManager.createOrGetParent(chartEntry.getCategory(), chartEntry.getCategoryDesc());
			chartStub.parent = chartEntry.getCategory();
			componentsTableManager.insertAtIdx(chartStub,insertAtIdx);
		}


	},{
	
	});
var wizard = new OlapChartWizard();


