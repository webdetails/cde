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

/* View Manager custom component for server version 4.8 */

var wd = wd || {}
wd.cdf = wd.cdf || {};
wd.cdf.views = wd.cdf.views || {};
var templates = templates || {};
templates.dashboardViews = templates.dashboardViews || {}; 

wd.cdf.views.getUrl = function() {
  return wd.helpers.views.getUrl(this.solution, this.path, this.file, this.name);
};

wd.cdf.views.DashboardView = Backbone.Model.extend({
  defaults: {
    "id": "",
    "name": "",
    "description": "",
    "params": {},
    "unbound": [],
    "user": "",
    "solution": "",
    "path": "",
    "file": "",
    "url": wd.cdf.views.getUrl,
    "timestamp": 0
  },

  /*
   * Test Whether the DashboardView is synced with the dashboard
   */
  isSynced: function() {
    var modelParams = this.get("parameters"),
        dashboardParams = Dashboards.getViewParameters();
    /* If the two parameter lists have mismatched sizes, then they're definitely different */
    if(Object.keys(modelParams).length !== Object.keys(dashboardParams).length) {
      return false;
    }
    /* Search for mismatched keys, or similar keys with mismatched values */
    for(p in modelParams) if (modelParams.hasOwnProperty(p)) {
      if (!dashboardParams.hasOwnProperty(p) || dashboardParams[p] !== modelParams[p]) {
        return false;
      }
    }
    /* Same size, and we matched all keys and values -- they're the same,
     * so the object is synced with the dashboard state
     */
    return true;
  },

  /*
   * Sync DashboardView with the dashboard parameters
   */
  syncWithDashboard: function() {
    this.set("parameters", Dashboards.getViewParameters());
  }
});

wd.cdf.views.Report = Backbone.Model.extend({
  defaults: {
    "name": "",
    "label": "",
    "dashboardView": null,
    "params": {},
    "user": "",
    "timestamp": 0
  }
});

wd.cdf.views.newDashboardView = function(name,description) {
  var m = {
    id: null,
    name: name,
    description: description,
    params: Dashboards.getViewParameters(),
    unbound: Dashboards.getUnboundParameters(),
    user: Dashboards.context.user,
    solution: Dashboards.context.solution,
    path: Dashboards.context.path,
    file: Dashboards.context.file,
    timestamp: (new Date()).getTime()
  };
  return new wd.cdf.views.DashboardView(m);
};

/**************************
 * View Manager Component *
 **************************/

wd.cdf.views.ViewManager = Backbone.Model.extend({
  defaults: {
    "currentView": "Unsaved",
    "currentDesc": "",
    "views": null,
    "viewCount": 0
  },

  initViews: function(views) {
    var viewCollection = new Backbone.Collection();
    viewCollection.model = wd.cdf.views.DashboardView;
    viewCollection.reset(views);
    this.set("views", viewCollection, {silent:true});
    this.set("viewCount", viewCollection.size(), {silent:true});

    // if current view is unsaved, inform user
    if(Dashboards.view){
      var current = viewCollection.filter(function(m) {return m.get("name") === Dashboards.view.name;})[0];
      if(current) {
        this.set("currentView", Dashboards.view.name, {silent:true});
        this.set("currentDesc", Dashboards.view.description, {silent:true});
        return;
      }
    }
    this.set("currentView", "Unsaved", {silent:true});
    this.set("currentDesc", "", {silent:true});
  }
});

wd.cdf.views.ViewManagerView = Backbone.View.extend({

  events: {
    'click .tab': "clickTab",
    'click .manage-views, .save-panel .cancel': "toggleViewManager",
    'click .view-item .delete': "deleteView",
    'click .save-panel .save': "saveView"
  },

  tabs: [
    {label: "List", selector: ".list-panel", template:"viewListPanel"},
    /*{label: "Subscribe", selector: ".subscrition-panel", template: "viewSubscriptionPanel"},*/
    {label: "Save", selector: ".save-panel", template: "viewSavePanel"}
  ],

  initialize: function() {
    this.configureListeners();
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

  configureListeners: function() {
    this.model.on("change",this.render,this);
  },

  renderTabs: function() {
    var tabs = this.$(".tabs").empty(),
        tabContents =  this.$(".tab-contents").empty();
    _(this.tabs).each(function(t) {
      /* Render the tab */
      tabs.append(Mustache.render(templates.dashboardViews.viewManagerTab, t));
      /* Render the panel corresponding to the tab */
      tabContents.append(Mustache.render(templates.dashboardViews[t.template], this.model.toJSON()));
    }, this);
    tabs.children().slice(0, 1).addClass('selected');
    tabContents.children().slice(1).hide();
    /* */
  },

  renderViewList: function() {
    var $views = this.$(".list-panel .views").empty();
    this.model.get("views").each(function(e) {
      var $view = $(Mustache.render(templates.dashboardViews.viewListItem, e.toJSON()));
      $view.data("model", e);
      $views.append($view);
    });
    
  },
  toggleViewManager: function() {
    this.$('.manage-views').toggleClass("active");
    this.$('.view-manager').toggle();
  },

  render: function() {
    this.$el.html(Mustache.render(templates.dashboardViews.viewManager, this.model.toJSON()));
    this.renderTabs();
    this.renderViewList();
    this.$(".view-manager").hide();
  },

  saveView: function() {
    var myself = this,
        name = this.$(".save-properties .name").val(),
        desc = this.$(".save-properties .description").val(),
        view = wd.cdf.views.newDashboardView(name, desc).toJSON();

    // Base64 encode view parameters (OrientDB issue)
    view.params = Base64.encode(JSON.stringify(Dashboards.getViewParameters()));
    

    // set id if it's an update
    var old = this.model.get("views").filter(function(m) {return m.get("name") === name;})[0];
    if(old) {
      view.id = old.get("id");
    }

    $.ajax({
      type: "POST",
      url: webAppPath + wd.helpers.views.getSaveViewsEndpoint(),
      //contentType: 'application/json',
      dataType: 'json',
      processData: false,
      data: $.param({name: name, view: JSON.stringify(view)}),
      success: function(response) {
        if(response.status === "error") {
          Dashboards.log(response.message, "warn");
          return;
        }
        Dashboards.view = response.result;
        Dashboards.restoreView();
        view = Dashboards.view;
        myself.model.get("views").add(view, {merge: true, silent: true});
        myself.model.set("currentView", view.name, {silent: true});
        myself.model.set("currentDesc", view.description, {silent: true});
        myself.model.trigger("update");
      },
      error: function() {
        Dashboards.log("Error saving view", "warn");
      },
      cache: false
    });
  },

  deleteView: function(evt) {
    var myself = this,
        view = $(evt.target).parent().data("model");

    $.ajax({
      type: "POST",
      url: webAppPath + wd.helpers.views.getDeleteViewsEndpoint(),
      //contentType: 'application/json',
      dataType: 'json',
      processData: false,
      data: $.param({name: view.get("name")}),
      success: function(response) {
        if(response.status === "error") {
          Dashboards.log(response.message, "warn");
          return;
        }
        myself.model.get("views").remove(view, {silent: true});
        // if we just deleted the current Dashboards.view previously saved
        // we need to remove the id so an update on a deleted view is not triggered
        if(Dashboards.view && Dashboards.view.name === view.get("name")) {
          Dashboards.view.id = null;
        }
        if(myself.model.get("currentView") === view.get("name")) {
          myself.model.set("currentView", "Unsaved", {silent: true});
          myself.model.set("currentDesc", "", {silent: true});
        }
        myself.model.trigger("update");
      },
      error: function() {
        Dashboards.log("Error deleting view", "warn");
      },
      cache: false
    });
  }

});

templates.dashboardViews.viewManager =
  "<div class='view-manager-component'>" +
  " <div class='current-view'>" + 
  "   <span class='label'>Current View: </span>" +
  "   <span class='value'>{{currentView}}</span>" +
  " </div>" +
  " <div class='big-button manage-views'>Manage Views</div>" +
  " <div class='view-manager'>" +
  "   <div class='tabs'></div>" +
  "   <div class='tab-contents'></div>" +
  " </div>" +
  "</div>";

templates.dashboardViews.viewManagerTab =
  "<div class='tab' data-target='{{selector}}'>" +
  "{{label}}" + 
  "</div>";

templates.dashboardViews.viewListPanel =
  "<div class='list-panel panel'>" +
  " <div class='total-views'>" + 
  "   <span class='label'>Total Views: </span>" +
  "   <span class='value'>{{viewCount}}</span>" +
  " </div>" +
  " <div class='views'></div>" +
  " <div class='view-all'>" +
  "   <span class='label'>View All</span>" +
  "   <span class='description'>(go to View Manager)</span>" +
  " </div>" + 
  "</div>";
 
templates.dashboardViews.viewListItem =
  "<div class='view-item'>" +
  " <a class='name' href='{{url}}'>{{name}}</a>" +
  " <span class='delete'></span>" +
  "</div>";

templates.dashboardViews.viewSubscriptionPanel =
  "<div class='subscrition-panel panel'>" +
  " <div class='current-view'>" +
  "   <span class='label'>Current View:</span>" + 
  "   <span class='value'></span>" + 
  " </div>" +
  " <div class='unsaved'>"+
  "   <div>Your current view is not saved. To subscribe," +
  "     you must first save it. Do you wish to proceed?" +
  "   </div>" +
  "   <div class='button save'>Save</div>" + 
  "   <div class='button cancel'>Cancel</div>" + 
  " </div>" +
  " <div class='subscription-properties'>" +
  "   <span class='label'></span>" +
  "   <input type='radio'>" +
  " </div>" + 
  "</div>";

templates.dashboardViews.viewSavePanel =
  "<div class='save-panel panel'>" +
  " <div class='current-view'>" +
  "   <span class='label'>Current View:</span>" + 
  "   <span class='value'>{{currentView}}</span>" + 
  " </div>" +
  " <div class='save-properties'>" +
  "   <div><span class='label'>Title</span><input type='text' class='name' value='{{currentView}}'></div>" + 
  "   <div><span class='label'>Description</span><textarea class='description' placeholder='Enter a description'>{{currentDesc}}</textarea></div>" + 
  " </div>" + 
  " <div class='save-actions'>" +
  "   <div class='big-button active save'>Save</div>" + 
  "   <div class='big-button cancel'>Cancel</div>" + 
  " </div>" + 
  "</div>";
