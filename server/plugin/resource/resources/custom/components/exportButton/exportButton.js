


var ExportButtonComponent = BaseComponent.extend({

  ph: undefined,
  tc: undefined,
  names: { queryState:null, query:null };
  
/* BUILD THE COMPONENT */

  update: function(){
    
    var myself = this;
    $.extend(this.options,this);

    this.ph = $("#" + this.htmlObject);
    this.ph.empty();
    var bar = $('<span class="exportButton"></span>').appendTo(this.ph);
    var comp = Dashboards.getComponentByName( "render_" + this.componentName );
    var overrideParameters = Dashboards.propertiesArrayToObject(myself.parameters);    
    bar.text( this.label ).click( function(){
      for (n in this.names) {
        if(comp[n] && comp[n] instanceof Query){
            comp[n].exportData(myself.outputType, overrideParameters);
            break;
        }
      }
    });
  }

});
