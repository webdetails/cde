


var ExportButtonComponent = BaseComponent.extend({

  ph: undefined,
  tc: undefined,
  
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
    bar.text( this.label ).click( function(){
      component.query.exportData(myself.outputType, overrideParameters);
    } );
  }

});
