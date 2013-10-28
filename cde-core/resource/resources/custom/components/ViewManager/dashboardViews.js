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
    "key": null,
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
    for(p in modelParams) if (modelParams.hasOwnProperty(p)){
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
    id: name,
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
    "currentView": "Unsaved View",
    "views": null,
    "viewCount": 0
  },

  initViews: function(views) {
    if(Dashboards.view) {
      this.set("currentView",Dashboards.view.name,{silent:true});
    }
    var viewCollection = new Backbone.Collection();
    viewCollection.model = wd.cdf.views.DashboardView;
    viewCollection.reset(views);
    this.set("views",viewCollection,{silent:true});
    this.set("viewCount",viewCollection.size(),{silent:true});
    this.change();
  }
});

wd.cdf.views.ViewManagerView = Backbone.View.extend({

  events: {
    'click .tab': "clickTab",
    'click .manage-views, .save-panel .cancel': "toggleViewManager",
    'click .view-item .delete': "deleteView",
    'click .save-panel .save': "save"
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
    _(this.tabs).each(function(t){
      /* Render the tab */
      tabs.append(templates.viewManagerTab(t));
      /* Render the panel corresponding to the tab */
      tabContents.append(templates[t.template](this.model.toJSON()));
    },this);
    tabs.children().slice(0,1).addClass('selected');
    tabContents.children().slice(1).hide();
    /* */
  },

  renderViewList: function() {
    var $views = this.$(".list-panel .views").empty();
    this.model.get("views").each(function(e){
      var $view = $(templates.viewListItem(e.toJSON()));
      $view.data("model",e);
      $views.append($view);
    });
    
  },
  toggleViewManager: function(){
    this.$('.manage-views').toggleClass("active");
    this.$('.view-manager').toggle();
  },

  render: function() {
    this.$el.html(templates.viewManager(this.model.toJSON()));
    this.renderTabs();
    this.renderViewList();
    this.$(".view-manager").hide();
  },

  save: function() {
    var name = this.$(".save-properties .name").val(),
        desc = this.$(".save-properties .description").val(),
        view = wd.cdf.views.newDashboardView(name, desc).toJSON(),
        args;
        view.params = Base64.encode(JSON.stringify(view.params)),
        old = this.model.get("views").filter(function(m){return m.get("name") == name})[0];

    if(old) {
      view.key = old.get("key");
    }
    args = {
      view: JSON.stringify(view)
    };
    var myself = this;
    $.post(webAppPath + wd.helpers.views.getSaveViewsEndpoint(),$.param(args),function(){
      Dashboards.view = view;
      myself.model.set("currentView",name);
      myself.model.trigger("update");
    });
  },

  deleteView: function(evt) {
    var view = $(evt.target).parent().data("model"),
        args = {
          name: view.get("name")
        };
    var myself = this;
    $.post(webAppPath + wd.helpers.views.getDeleteViewsEndpoint(),$.param(args),function(){
      myself.model.get("views").remove(view);
      myself.model.trigger("update");
    });
  }

});

templates.viewManager =  Mustache.compile(
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
  "</div>");

templates.viewManagerTab = Mustache.compile(
  "<div class='tab' data-target='{{selector}}'>" +
  "{{label}}" + 
  "</div>");

templates.viewListPanel =  Mustache.compile(
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
  "</div>");
 
templates.viewListItem =  Mustache.compile(
  "<div class='view-item'>" +
  " <a class='name' href='{{url}}'>{{name}}</a>" +
  " <span class='delete'></span>" +
  "</div>");

templates.viewSubscriptionPanel =  Mustache.compile(
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
  "</div>");

templates.viewSavePanel =  Mustache.compile(
  "<div class='save-panel panel'>" +
  " <div class='current-view'>" +
  "   <span class='label'>Current View:</span>" + 
  "   <span class='value'>{{currentView}}</span>" + 
  " </div>" +
  " <div class='save-properties'>" +
  "   <div><span class='label'>Title</span><input type='text' class='name' value='{{currentView}}'></div>" + 
  "   <div><span class='label'>Description</span><textarea class='description' placeholder='Enter a description'>{{description}}</textarea></div>" + 
  " </div>" + 
  " <div class='save-actions'>" +
  "   <div class='big-button active save'>Save</div>" + 
  "   <div class='big-button cancel'>Cancel</div>" + 
  " </div>" + 
  "</div>");
/*
 * 
 */
