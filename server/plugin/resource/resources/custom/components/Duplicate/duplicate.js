var DuplicateComponent = BaseComponent.extend({

  update: function() {
    var ph = $("#" + this.htmlObject).empty(),
      myself = this,
      link = $("<a href='javascript:;'>Duplicate</a>");
    link.click(function(){myself.duplicate();});
    link.appendTo(ph);
  },
  duplicate: function(parameterValues) {
    var cdePrefix = "render_";
    parameterValues = parameterValues || {};
    if (!Dashboards.duplicateIndex) {
      Dashboards.duplicateIndex = 0;
    }
    Dashboards.duplicateIndex += 1;
    var dIdx = Dashboards.duplicateIndex,
      suffix = "_" + dIdx;
    
    var params = {}
      $.each(this.parameters,function(i,p){
        
      var param =  p + suffix; 
      Dashboards.setBookmarkable(param,Dashboards.isBookmarkable(p));
      Dashboards.setParameter(param,parameterValues[p] || Dashboards.getParameterValue(p));
      params[p] = param;
    });
    var comps = {}
      $.each(this.components,function(i,c){
        
      var comp =  c + suffix; 
      comps[c] = comp;
    });
    var htmlRemap = {};
    htmlRemap[this.targetHtmlObject] = (this.targetHtmlObject + suffix).replace(/([^\\])\$/g,'$1\\$');
    var newPh = $("#" + this.targetHtmlObject).clone();
    newPh.attr("id",newPh.attr("id") + suffix);  
    newPh.find("[id]").each(function(i,e){
      $e = $(e);
      $e.attr("id",$e.attr("id") + suffix);  
    });
    if (this.targetContainer){
      newPh.appendTo( '#' + this.targetContainer);  
    } else {
      newPh.insertAfter("#" + this.targetHtmlObject);
    }
    for(c in this.components) {
      var cName = this.components[c];
      cName = RegExp("^"+ cdePrefix).test(cName) ? cName : cdePrefix + cName;
      var component = Dashboards.getComponent(cName);
      if (component) {
	      htmlRemap[component.htmlObject] = (component.htmlObject + suffix).replace(/([^\\])\$/g,'$1\\$');
    	  var clone = component.clone(params,comps, htmlRemap);
	      clone.name = clone.name + suffix;
    	  window[clone.name] = clone;
	      Dashboards.addComponents([clone]);
    	  Dashboards.update(clone);
      }
    }
  },

  clone: function(parameterRemap,componentRemap,htmlRemap) {
    var that = this.base(parameterRemap,componentRemap,htmlRemap);
    that.targetHtmlObject = htmlRemap[that.targetHtmlObject];
    if (that.parameters) {
      that.parameters = that.parameters.map(function(param){
        if (param in parameterRemap) {
          return parameterRemap[param];
        } else {
          return param;
        }
      });
    }
    return that;
  }
});
