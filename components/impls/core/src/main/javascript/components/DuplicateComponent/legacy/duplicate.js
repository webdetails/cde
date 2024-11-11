/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


var DuplicateComponent = BaseComponent.extend({

  update: function() {
    var myself = this,
      ph = $("#" + myself.htmlObject).empty(),
      link = $("<a href='javascript:;'>Duplicate</a>");
    link.click(function() {
      myself.duplicate();
    });
    link.appendTo(ph);
  },

  duplicate: function(parameterValues) {
    var myself = this;

    var cdePrefix = "render_";
    parameterValues = parameterValues || {};

    if(!Dashboards.duplicateIndex) {
      Dashboards.duplicateIndex = 0;
    }
    Dashboards.duplicateIndex += 1;
    var suffix = "_" + Dashboards.duplicateIndex;
    
    var params = {};

    $.each(myself.parameters, function(i, p) {
      var param =  p + suffix; 
      Dashboards.setBookmarkable(param,Dashboards.isBookmarkable(p));
      Dashboards.setParameter(param,parameterValues[p] || Dashboards.getParameterValue(p));
      params[p] = param;
    });

    var comps = {};

    $.each(myself.components, function(i, c) {
      var comp =  c + suffix; 
      comps[c] = comp;
    });

    // clone target HTML element and it's content
    var htmlRemap = {};
    htmlRemap[myself.targetHtmlObject] = (myself.targetHtmlObject + suffix).replace(/([^\\])\$/g, '$1\\$');
    var newPh = $("#" + myself.targetHtmlObject).clone();
    newPh.attr("id", newPh.attr("id") + suffix);  
    newPh.find("[id]").each(function(i, e) {
      var $e = $(e);
      $e.attr("id", $e.attr("id") + suffix);  
    });

    // append cloned HTML to target container, or after target HTML element
    if(myself.targetContainer) {
      newPh.appendTo("#" + myself.targetContainer);  
    } else {
      newPh.insertAfter("#" + myself.targetHtmlObject);
    }

    // clone target components, add to dashboard and execute update
    for(c in myself.components) {
      var cName = myself.components[c];
      cName = RegExp("^"+ cdePrefix).test(cName) ? cName : cdePrefix + cName;
      var component = Dashboards.getComponent(cName);
      if(component) {
        htmlRemap[component.htmlObject] = (component.htmlObject + suffix).replace(/([^\\])\$/g,'$1\\$');
        var clone = component.clone(params, comps, htmlRemap);
        clone.name = clone.name + suffix;
        window[clone.name] = clone;
        Dashboards.addComponents([clone]);
        Dashboards.update(clone);
      }
    }
  },

  clone: function(parameterRemap, componentRemap, htmlRemap) {
    Dashboards.log("This function is deprecated. Please use targetComponent.clone(...), see BaseComponent in CDF (core.js) for more details.", "warn");
    var that = this.base(parameterRemap, componentRemap, htmlRemap);
    that.targetHtmlObject = htmlRemap[that.targetHtmlObject];
    if(that.parameters) {
      that.parameters = that.parameters.map(function(param) {
        if(param in parameterRemap) {
          return parameterRemap[param];
        } else {
          return param;
        }
      });
    }
    return that;
  }
});
