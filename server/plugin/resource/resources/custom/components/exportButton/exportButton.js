


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
    var comp = Dashboards.getComponentByName( "render_" + myself.componentName );
    var overrideParameters = Dashboards.propertiesArrayToObject(myself.parameters);    
    bar.text( myself.label ).click( function(){
      var foundQuery = false;
      for (n in myself.queryObjectNames) {
        if(comp[n] && comp[n] instanceof Query){
            comp[n].exportData(myself.outputType, overrideParameters);
            foundQuery = true;
            break;
        }
      }
      if (!foundQuery) { 
        Dashboards.log( myself.name + ": could not find a query object on " + myself.componentName ); 
      }
    });
  }

});
