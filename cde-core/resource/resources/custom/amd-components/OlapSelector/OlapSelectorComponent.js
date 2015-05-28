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
  'cdf/lib/jquery',
  'amd!cdf/lib/underscore',
  './OlapSelector/OlapSelectorView',
  './OlapSelector/OlapSelectorModel',
  './OlapSelector/lib/OlapUtils',
  './OlapSelectorComponentExt',
  'css!./OlapSelectorComponent'],
  function(
    BaseComponent,
    Utils,
    $,
    _,
    OlapSelectorView,
    OlapSelectorModel,
    OlapUtils,
    OlapSelectorComponentExt) {

  var OlapSelectorComponent = BaseComponent.extend({

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

      myself.model.on("change:collapsed",function(m, v) {
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
      var params = ( overrides instanceof Array)
        ? Utils.propertiesArrayToObject(overrides)
        : ( overrides || {} );
      var paramValues = {};

      _.each( params , function (value, name) {
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

  return OlapSelectorComponent;

});
