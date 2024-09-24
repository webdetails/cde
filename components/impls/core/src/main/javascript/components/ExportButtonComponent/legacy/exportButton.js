/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

var ExportButtonComponent = BaseComponent.extend({

  ph: undefined,
  tc: undefined,
  queryObjectNames: { queryState:null, query:null },
  
/* BUILD THE COMPONENT */

  update: function(){
    
    var myself = this;
    $.extend(this.options,this);

    myself.ph = $("#" + myself.htmlObject);
    myself.ph.empty();
    var bar = $('<span class="exportButton"></span>').appendTo(myself.ph);
	var componentName = (myself.componentName.indexOf("render_")==0?"":"render_") + myself.componentName;
    var comp = Dashboards.getComponentByName( componentName );
    var overrideParameters = Dashboards.propertiesArrayToObject(myself.parameters);    
    bar.text( myself.label ).click( function(){
      var foundQuery = false;
      for (n in myself.queryObjectNames) {
        if(comp[n]){
            comp[n].exportData(myself.outputType, overrideParameters , myself.getFilterSettings(comp) );
            foundQuery = true;
            break;
        }
      }
      if (!foundQuery) { 
        Dashboards.log( myself.name + ": could not find a query object on " + myself.compName ); 
      }
    });
  },
  
  //add filtering to export if it's a data table
  getFilterSettings: function(component){
    var extraSettings = {
      exportPage: this.exportPage
    };
	if(component.type == "Table"){
	  //var dtOptions = component.getTableOptions(component.chartDefinition);
	  //if(dtOptions.bFilter) {
	  var dtOptions = component.ph.dataTableSettings[0];
	  if(dtOptions.oFeatures.bFilter) {
		var searchInput = component.ph.find('input').filter("[type=search]");
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
