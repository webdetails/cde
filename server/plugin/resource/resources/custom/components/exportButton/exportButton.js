


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
      component.query.exportData(myself.outputType, overrideParameters);
    } );
  }

});
