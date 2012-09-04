


var ExportButtonComponent = BaseComponent.extend({

  ph: undefined,
  tc: undefined,
  
/* BUILD THE COMPONENT */

  update: function(){
    
    var myself = this;
    $.extend(this.options,this);

    this.ph = $("#" + this.htmlObject);
    this.ph.empty();
    var bar = $('<span class="exportButton"></span>').appendTo(this.ph);
    var component = Dashboards.getComponentByName( "render_" + this.componentName );
    
    var overrideParameters = Dashboards.propertiesArrayToObject(myself.parameters);    

    bar.text( this.label ).click( function(){
      component.query.exportData(myself.outputType, overrideParameters, myself.getFilterSettings(component));
    } );
  },
  
  //add filtering to export if it's a data table
  getFilterSettings: function(component){
    var extraSettings = {};
	if(component.type == "Table"){
	  //var dtOptions = component.getTableOptions(component.chartDefinition);
	  //if(dtOptions.bFilter) {
	  var dtOptions = component.ph.dataTableSettings[0];
	  if(dtOptions.oFeatures.bFilter) {
		var searchInput = component.ph.find('input');
		if(searchInput) {
		  extraSettings.dtFilter = searchInput.val();
		  if(dtOptions.aoColumns){
			var idxs = [];
		    for(var i=0; i< dtOptions.aoColumns.length;i++){
			  if(dtOptions.aoColumns[i].bSearchable){
			    idxs.push(i);
		      }
			}
			extraSettings.dtSearchableColumns = idxs.join(',');
		  }
		}
	  }
	}
	return extraSettings;
  }

});
