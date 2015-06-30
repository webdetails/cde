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

/*
 * Selector Model describes the behaviour for the selector
 * as a whole.
 */

define([
  'cdf/lib/jquery',
  'amd!cdf/lib/backbone',
  'amd!cdf/lib/underscore'],
  function($, Backbone, _) {

  var models = models || {};
  models.pagingSelector = {};

  models.pagingSelector.SelectorModel = Backbone.Model.extend({
    defaults: {
      "title": "",
      "search": true,
      "advancedSearch": false,
      "multiselect": true,
      "searchterm": "",
      "collapsed": true,
      "values": null,
      "pageStart": 0,
      "pageSize": Infinity,
      "totalRecords": 0
    },

    initialize: function(values) {
      this.set("values",new Backbone.Collection());
      var values = this.get("values");
      values.comparator = function(val) {return val.get("idx");};
      values.model = models.pagingSelector.OptionModel;
      values.on("change:selected", this.updateSelection,this);
      this.on("change:searchterm", this.updateSearch,this);
      this.updateValues(values);
    },

    addPage: function(newValues) {
      var v,
      newValue,
      values = this.get("values");
      for(v = 0; v < newValues.length;v++) {
        var newValue = newValues[v],
        found = values.detect(function(model) {
          return model.get("idx") == newValue.idx;
        });
        if(!found) {
          values.add(newValues[v], {silent:true});
        }
      }
      values.trigger("add");
    },

    nextPage: function() {
      var start = this.get("pageStart"),
      size = this.get("pageSize"),
      total = this.get("totalRecords"),
      requestedPage = start + size,
      lastPage = total - total % size;
      this.set("pageStart", Math.min(requestedPage, lastPage));
    },
    prevPage: function() {
      var start = this.get("pageStart"),
      size = this.get("pageSize");
      this.set("pageStart", Math.max(0, start - size));
    },
    firstPage: function() {
      this.set("pageStart", 0);
    },
    lastPage: function() {
      var total = this.get("totalRecords"),
      size = this.get("pageSize");
      this.set("pageStart", total - total % size);
    },
    goToPage: function(page) {
      var total = this.get("totalRecords"),
      size = this.get("pageSize"),
      requestedPage = page * size,
      lastPage = total - total % size;
      this.set("pageStart", Math.min(requestedPage, lastPage));

    },

    updateValues: function(values) {
      this.get("values").reset(values);
    },

    updateSelection: function(m, selected) {
      /*
      * If we're working with single-select, we need to insure
      * that only the last pick is marked as selected.
      */
      if(selected && !this.get("multiselect")) {
        //this.get("selection").reset([m]);
        _(this.get("values").without(m)).each(function(other) {
          other.set("selected", false);
        });
      }
    },

    clearSelection: function() {
      this.get("values").each(function(m) {
        m.set("selected", false);
      })
    },

    toggleCollapsed: function() {
      this.set("collapsed",!this.get("collapsed"));
    },

    selectedValues: function() {
      return _(this.get("values")
        .where({selected: true}))
        .map(function(m) {return m.get("value");});
    },

    updateSearch: function() {
      var term = this.get("searchterm");
      var pattern = new RegExp(term);
      this.get("values").each(function(m) {
        var match = pattern.test(m.get('label')) ||
          (this.get("advancedSearch") && pattern.test(m.get("value")));
        m.set("visible", match);
      },this);
    }
  });

  /*
  * Option Model describes the behaviour for each individual
  * option within the selector.
  */
  models.pagingSelector.OptionModel = Backbone.Model.extend({
    defaults: {
      "idx": 0,
      "label": "",
      "value": null,
      "visible": true,
      "selected": false,
      "new": false
    },

    toggleSelected: function() {
      this.set("selected", !this.get("selected"));
    }
  });

  return models;

});
