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
  'cdf/dashboard/Utils',
  'cdf/lib/jquery',
  'amd!cdf/lib/underscore',
  './OlapSelectorView',
  './OlapSelectorModel',
  './lib/OlapUtils',
  './OlapSelectorComponent.ext',
  'css!./OlapSelectorComponent'
], function(
  BaseComponent,
  Utils,
  $,
  _,
  OlapSelectorView,
  OlapSelectorModel,
  OlapUtils,
  OlapSelectorComponentExt
) {

  return BaseComponent.extend({

    init: function() {
      var olapUtilsInstance = new OlapUtils({
          url: OlapSelectorComponentExt.getServiceUrl(),
          catalog: this.catalog,
          cube: this.cube,
          dimension: this.dimensionName
      });
      
      var cToFind = this.catalog;
      var entry = _.find(olapUtilsInstance.getCatalogs(), function(c) {
        return (c.schema.indexOf(cToFind) >= 0);
      });
     
      if(entry !== false) {
        olapUtilsInstance.setCatalog(entry.name);
      }
      
      this.model = new OlapSelectorModel({
        olapUtils: olapUtilsInstance,
        title: this.title,
        multiselect: this.multiSelect,
        parameters: this.getParamValues(this.parameters),
        preselected: this.getPreSelValue(this.parameter)
      });
      this.view = new OlapSelectorView({
        model: this.model,
        el: $("#" + this.htmlObject).get(0)
      })
    },

    update: function() {
      var myself = this;

      if(!myself.isInitialized) {
        myself.init();
        myself.isInitialized = true;
      }

      myself.model.on("change:collapsed", function(m, v) {
        if(v) {
          myself.dashboard.processChange(myself.name);
        }
      });
      myself.view.render();

      myself.parameters = myself.getParamValues(myself.parameters);
      myself.model.set("parameters", myself.parameters);
    },

    getValue: function() {
      return _(this.model.get("values")
        .where({selected: true}))
        .map(function(m) {return m.get("qualifiedName");});
    },


    getParamValues: function(overrides) {
      var params = (overrides instanceof Array)
        ? Utils.propertiesArrayToObject(overrides)
        : (overrides || {});
      var paramValues = {};

      _.each(params , function(value, name) {
        value = this.dashboard.getParameterValue(value);

        if(_.isObject(value)){
          value = JSON.stringify(value);
        }

        if(typeof value == 'function') {
          value = value();
        }

        paramValues[name] = value;
        paramValues.length = overrides.length;
      });

      return paramValues;
    },

    getPreSelValue: function(param) {
      var paramValue = this.dashboard.getParameterValue(param),
          values = [];

      if(typeof paramValue !== 'undefined'
        && paramValue.length > 0) {
        
        values = JSON.parse(paramValue);
        if(!this.multiSelect) {
          values = new Array(values[0]);
        }
      } 

      return values;
    }
  });

});
