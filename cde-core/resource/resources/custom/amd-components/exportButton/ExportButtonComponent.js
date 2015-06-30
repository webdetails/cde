/*!
 * Copyright 2002 - 2015 Webdetails, a Pentaho company. All rights reserved.
 *
 * This software was developed by Webdetails and is provided under the terms
 * of the Mozilla Public License, Version 2.0, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */

define([
  'cdf/components/BaseComponent',
  'cdf/dashboard/Utils',
  'cdf/Logger',
  'cdf/lib/jquery',
  'css!./ExportButtonComponent'],
  function(BaseComponent, Utils, Logger, $) {

  var ExportButtonComponent = BaseComponent.extend({

    ph: undefined,
    tc: undefined,
    queryObjectNames: {queryState: null, query: null},
    
    /* BUILD THE COMPONENT */

    update: function() {
      
      var myself = this;
      $.extend(this.options, this);

      myself.ph = $("#" + myself.htmlObject);
      myself.ph.empty();
      var bar = $('<span class="exportButton"></span>').appendTo(myself.ph);
      var componentName = (myself.componentName.indexOf("render_") == 0 ? "" : "render_") + myself.componentName;
      var comp = this.dashboard.getComponentByName(componentName);
      var overrideParameters = Utils.propertiesArrayToObject(myself.parameters);    
      bar.text(myself.label).click(function() {
        var foundQuery = false;
        for(var n in myself.queryObjectNames) {
          if(comp[n]) {
            comp[n].exportData(myself.outputType, overrideParameters, myself.getFilterSettings(comp));
            foundQuery = true;
            break;
          }
        }
        if(!foundQuery) { 
          Logger.log(myself.name + ": could not find a query object on " + myself.compName); 
        }
      });
    },
    
    //add filtering to export if it's a data table
    getFilterSettings: function(component) {
      var extraSettings = {};
      if(component.type == "Table") {
        var dtOptions = component.ph.dataTableSettings[0];
        if(dtOptions.oFeatures.bFilter) {
          var searchInput = component.ph.find('input');
          if(searchInput) {
            extraSettings.dtFilter = searchInput.val();
            if(dtOptions.aoColumns) {
              var idxs = [];
              for(var i = 0; i < dtOptions.aoColumns.length; i++) {
                if(dtOptions.aoColumns[i].bSearchable) {
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

  return ExportButtonComponent;

});
