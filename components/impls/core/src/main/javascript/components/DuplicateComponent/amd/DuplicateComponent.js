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


define([
  'cdf/components/BaseComponent',
  'cdf/Logger',
  'cdf/lib/jquery'
], function(BaseComponent, Logger, $) {

  var duplicateIndex = 0;

  return BaseComponent.extend({

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

      duplicateIndex += 1;

      var suffix = "_" + duplicateIndex;
      
      var params = {};

      $.each(myself.parameters, function(i, p) {
        var param =  p + suffix; 
        myself.dashboard.setBookmarkable(param, myself.dashboard.isBookmarkable(p));
        myself.dashboard.setParameter(param, parameterValues[p] || myself.dashboard.getParameterValue(p));
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
      for(var c in myself.components) {
        var cName = myself.components[c];
        cName = RegExp("^"+ cdePrefix).test(cName) ? cName : cdePrefix + cName;
        var component = myself.dashboard.getComponent(cName);
        if(component) {
  	      htmlRemap[component.htmlObject] = (component.htmlObject + suffix).replace(/([^\\])\$/g, '$1\\$');
      	  var clone = component.clone(params, comps, htmlRemap);
  	      clone.name = clone.name + suffix;
  	      myself.dashboard.addComponents([clone]);
      	  myself.dashboard.update(clone);
        }
      }
    },

    clone: function(parameterRemap, componentRemap, htmlRemap) {
      Logger.warn("This function is deprecated. Please use targetComponent.clone(...), see BaseComponent.js in CDF for more details.");
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

});
