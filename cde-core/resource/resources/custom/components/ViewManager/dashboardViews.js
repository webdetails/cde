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

var wd = wd || {}
wd.cdf = wd.cdf || {};
wd.cdf.views = wd.cdf.views || {};

wd.cdf.views.ViewModel = Backbone.Model.extend({
  defaults: {
    name: "Unsaved",
    description: "",
    id: null,
    params: {},
    unbound: [],
    user: "",
    solution: "",
    path: "",
    file: "",
    timestamp: 0
  },

  constructor: function() {
    Backbone.Model.apply(this, arguments);
  }
});

wd.cdf.views.ViewCollection = Backbone.Collection.extend({
  url: function() { return webAppPath + wd.helpers.views.getViewsEndpoint(); },
  model: wd.cdf.views.ViewModel,
  parse: function(response) {
    if(response.status === "error") {
      Dashboards.log(response.result, "warn");
      return;
    }
    return response.result;
  }
});

wd.cdf.views.ViewManagerView = Backbone.View.extend({

  model: wd.cdf.views.ViewModel,

  collection: wd.cdf.views.ViewCollection,

  events: {
    'click .tab': "clickTab",
    'click .manage-views, .save-panel .cancel': "toggleViewManager",
    'click .view-item .delete': "deleteView",
    'click .save-panel .save': "saveView"
  },

  tabs: [
    {label: "List", selector: ".list-panel", template: "viewListPanel"},
    {label: "Save", selector: ".save-panel", template: "viewSavePanel"}
  ],

  initialize: function() {
    this.collection = new wd.cdf.views.ViewCollection();
    this.collection.on("add change sync remove", this.render, this);
    // fetch data from server
    this.collection.fetch();
  },

  render: function() {
    //check if Dashboards.view exists and if there is a model saved with the same name
    var viewName = Dashboards.view ? Dashboards.view.name : undefined,
        viewDesc = Dashboards.view ? Dashboards.view.description : undefined;

    if(viewName) {
      var model = this.collection.filter(function(model) { return model.get("name") == viewName; })[0];
      if(model) {
        viewName = model.get("name");
        viewDesc = model.get("description");
      } else {
        viewName = "Unsaved";
        viewDesc = "";
      }
    } else {
      viewName = "Unsaved";
      viewDesc = "";
    }

    this.$el.html(templates.viewManager({
      currentView: viewName,
      views: this.collection,
      viewCount: this.collection.size()
    }));
    this.renderTabs(viewName, viewDesc);
    this.renderViewList();
    this.$(".view-manager").hide();
  },

  renderTabs: function(viewName, viewDesc) {
    var tabs = this.$(".tabs").empty(),
        tabContents =  this.$(".tab-contents").empty();

    _(this.tabs).each(function(tab) {
      /* Render the tab */
      tabs.append(templates.viewManagerTab(tab));
      /* Render the panel corresponding to the tab */
      if(tab.template === "viewListPanel") {
        tabContents.append(templates[tab.template]({
          "views": this.collection,
          "viewCount": this.collection.size()
        }));
      } else if(tab.template === "viewSavePanel") {
        tabContents.append(templates[tab.template]({
          "currentView": viewName,
          "description": viewDesc
        }));
      }
    }, this);

    tabs.children().slice(0, 1).addClass('selected');
    tabContents.children().slice(1).hide();
  },

  renderViewList: function() {
    var $views = this.$(".list-panel .views").empty();

    this.collection.each(function(model) {
      var $view = $(templates.viewListItem({
        name: model.get("name"),
        viewUrl: wd.helpers.views.getUrl(
          model.get("solution"),
          model.get("path"),
          model.get("file"),
          model.get("name"))}));

      $view.data("model", model);

      $views.append($view);
    });
  },

  clickTab: function(evt) {
    var $tgt = $(evt.target),
        selector = $tgt.data('target');
    /* Make sure only the clicked tab is marked as selected */
    this.$(".tab").removeClass("selected");
    $tgt.addClass("selected");
    /* Make it so only the selected tab has its contents showing*/
    this.$('.panel').hide();
    this.$(selector).show();
  },

  toggleViewManager: function() {
    this.$('.manage-views').toggleClass("active");
    this.$('.view-manager').toggle();
  },

  saveView: function() {
    var myself = this,
        name = myself.$(".save-properties .name").val().trim(),
        desc = myself.$(".save-properties .description").val();

    if(!name || !name.length || name === "Unsaved") {
      Dashboards.log("Failed to save a dashboard view with no name or with the reserved name 'Unsaved'", "warn");
      return;
    }

    var tmpModel = Dashboards.view
      ? new wd.cdf.views.ViewModel(Dashboards.view)
      : new wd.cdf.views.ViewModel();

    tmpModel.set("name", name, {silent: true});
    tmpModel.set("description", desc, {silent: true});
    // OrientDB needs param to be base64 encoded
    tmpModel.set("params", Base64.encode(JSON.stringify(Dashboards.getViewParameters())), {silent: true});
    tmpModel.set("unbound", Dashboards.getUnboundParameters(), {silent: true});
    tmpModel.set("user", Dashboards.context.user, {silent: true});
    tmpModel.set("solution", Dashboards.context.solution, {silent: true});
    tmpModel.set("path", Dashboards.context.path, {silent: true});
    tmpModel.set("file", Dashboards.context.file, {silent: true});
    tmpModel.set("timestamp", (new Date()).getTime(), {silent: true});

    //if it's an update, use the same id (key) value, HTTP PUT is idempotent
    var filtered = myself.collection.filter(function(model) { return model.get("name") == name; });
    if($.isArray(filtered) && filtered.length > 0) {
      tmpModel.set("id", filtered[0].get("id"), {silent: true});
    }

    tmpModel.sync(
      'update',
      tmpModel,
      {
        url: webAppPath + wd.helpers.views.getViewsEndpoint(name),
        contentType: 'application/json',
        dataType: 'json',
        processData: false,
        data: "view=" + JSON.stringify(tmpModel.toJSON()),
        success: function(response) {
          if(response.status === "error") {
            Dashboards.log(response.message, "warn");
            return;
          }

          Dashboards.view = response.result;

          Dashboards.restoreView();

          myself.collection.add(Dashboards.view, {merge: true, silent: false});
        },
        cache: false
      }
    );
  },

  deleteView: function(evt) {
    var myself = this,
        name = $(evt.target).parent().data("model").get("name").trim(),
        tmpModel;

    if(!name || !name.length) {
      Dashboards.log("Failed to delete a dashboard view with no name", "warn");
      return;
    }

    var filtered = myself.collection.filter(function(model) { return model.get("name") == name; })[0];
    if(filtered) {
      tmpModel = filtered;
    }

    // sync with server
    tmpModel.sync(
      'delete',
      tmpModel,
      {
        url: webAppPath + wd.helpers.views.getViewsEndpoint(name),
        contentType: 'application/json',
        dataType: 'json',
        success: function(response) {
          if(response.status === "error") {
            Dashboards.log(response.message, "warn");
            return;
          }
          myself.collection.remove(tmpModel, {silent: false});
          // if we just deleted the current Dashboards.view previously saved
          // we need to remove the id so an update on a deleted view is not triggered
          if(Dashboards.view && Dashboards.view.name === name) {
            Dashboards.view.id = null;
          }
        },
        cache: false
      }
    );
  }
});

// Treat underscore templates as if they had a mustache
// http://underscorejs.org/#template 
var _TemplateSettings = _.templateSettings;
  _.templateSettings = { interpolate: /\{\{(.+?)\}\}/g };

var templates = templates || {};
templates.viewManager = _.template(
  "<div class='view-manager-component'>" +
  "  <div class='current-view'>" +
  "    <span class='label'>Current View: </span>" +
  "    <span class='value'>{{currentView}}</span>" +
  "  </div>" +
  "  <div class='big-button manage-views'>Manage Views</div>" +
  "  <div class='view-manager'>" +
  "    <div class='tabs'></div>" +
  "    <div class='tab-contents'></div>" +
  "  </div>" +
  "</div>");

templates.viewManagerTab = _.template(
  "<div class='tab' data-target='{{selector}}'>" +
  "  {{label}}" +
  "</div>");

templates.viewListPanel = _.template(
  "<div class='list-panel panel'>" +
  "  <div class='total-views'>" +
  "    <span class='label'>Total Views: </span>" +
  "    <span class='value'>{{viewCount}}</span>" +
  "  </div>" +
  "  <div class='views'></div>" +
  "  <div class='view-all'>" +
  "    <span class='label'>View All</span>" +
  "    <span class='description'>(go to View Manager)</span>" +
  "  </div>" +
  "</div>");

templates.viewListItem = _.template(
  "<div class='view-item'>" +
  "  <a class='name' href='{{viewUrl}}'>{{name}}</a>" +
  "  <span class='delete'></span>" +
  "</div>");

templates.viewSavePanel = _.template(
  "<div class='save-panel panel'>" +
  "  <div class='current-view'>" +
  "    <span class='label'>Current View:</span>" +
  "    <span class='value'>{{currentView}}</span>" +
  "  </div>" +
  "  <div class='save-properties'>" +
  "    <div><span class='label'>Name</span><input type='text' class='name' value='{{currentView}}'></div>" +
  "    <div><span class='label'>Description</span><textarea class='description' placeholder='Enter a description'>{{description}}</textarea></div>" +
  "  </div>" +
  "  <div class='save-actions'>" +
  "    <div class='big-button active save'>Save</div>" +
  "    <div class='big-button cancel'>Cancel</div>" +
  "  </div>" +
  "</div>");

_.templateSettings = _TemplateSettings;
