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
  'cdf/Logger',
  'cdf/lib/jquery',
  'amd!cdf/lib/backbone',
  'amd!cdf/lib/underscore',
  'cdf/lib/mustache'],
  function(Logger, $, Backbone, _, Mustache) {

  /*
   * Selector View displays the selector model
   */

  var OlapSelectorView = Backbone.View.extend({

    events: {
      "click .title": "toggleCollapsed",
      "click .first": "firstPage",
      "click .prev": "prevPage",
      "click .next": "nextPage",
      "click .last": "lastPage",
      "click .pages span": "goToPage",
      "change .search input": "updateSearch",
      "click .validate" : "apply",
      "click .cancel" : "cancel",
      "click .breadcrumb .name" : "drillUp"
    },

    levelViews: [],

    renderLevels: function() {
      var levelContainer = this.$el.find(".levels").empty();
      _(this.levelViews).each(function(v) {
        levelContainer.append(v.render().el);
      });
    },

    initialize: function() {
      this.initializeOptions();
      this.configureListeners();
      this.levelViews = [];
      this.model.get("levels").each(function(m) {
        this.levelViews.push(new LevelView({model:m}));
      }, this);
    },

    configureListeners: function() {
      this.model.on("change", this.render, this);
      this.model.on("drill", this.renderCrumbtrail, this);
      this.model.on("change:collapsed", this.updateCollapsed, this);
      this.model.on("page", this.renderPages, this);
      var values = this.model.get("values");
      values.on("add remove reset",this.updateOptions, this);
      values.on("select", this.updateOptions, this);
      values.on("notify", this.notifyUpdate, this);
    },

    initializeOptions: function() {
      this._selectedViews = [];
      this._selectedViewsOut = [];
      this._optionViews = [];
      this.model.get("values").each(function(m) {
        this._optionViews.push(new OptionView({model: m}));
        if(m.get("selected")) {
          this._selectedViews.push(new SelectionView({model: m}));
          this._selectedViewsOut.push(new SelectionViewOut({model: m}));
        }
      }, this);
    },

    updateSearch: function(evt) {
      this.model.set("searchterm", evt.target.value);
    },

    render: function() {
      /*
       * Draw the selector proper
       */
      this.$el.html(Mustache.render(templates.olapSelector.main, this.model.toJSON()));
      this.renderLevels();
      this.updateCollapsed();
      this.renderLevels();
      this.renderOptions();
      this.renderPages();
      this.renderCrumbtrail();
      this.delegateEvents();
    },

    renderCrumbtrail: function() {
      var currentLevelName = this.model.get("levels").where({selected: true})[0].get("name"),
          crumbtrail = this.model.get("breadcrumb");
      if(crumbtrail.length > 0) {
        var breadcrumb = crumbtrail[crumbtrail.length - 1];
        this.$el.find(".breadcrumb").html(Mustache.render(
          templates.olapSelector.crumbtrail,
          {level: currentLevelName, name: breadcrumb.get("name")}));
      } else {
        this.$el.find(".breadcrumb").empty();
      }
    },

    renderPages: function() {
      var m = this.model;
      this.$el.find(".pagination .next").toggleClass("disabled", !m.get("morePages"));
      this.$el.find(".pagination .prev").toggleClass("disabled", !m.get("pageStart"));
      this.$el.find(".pagination .first").toggleClass("disabled", !m.get("pageStart"));
    },
    renderOptions: function() {
      var optionContainer = this.$el.find(".rightArea .options").empty(),
          selectionContainer = this.$el.find(".leftArea .selection").empty(),
          selectionContainerOut = this.$el.find(".outsideArea .selection").empty(),
          myself = this,
          minIdx = this.model.get('pageStart'),
          maxIdx = this.model.get('pageStart') + this.model.get('pageSize');

      _(this._optionViews).chain().filter(this.isVisible, this)
        .slice(minIdx, maxIdx).each(function(v) {
          optionContainer.append(v.render().el);
        }, this);

      _(this._selectedViewsOut).each(function(v) {
        selectionContainerOut.append(v.render().el);
      });

      _(this._selectedViews).each(function(v) {
        selectionContainer.append(v.render().el);
      });
    },

    updateOptions: function() {
      this.initializeOptions();
      this.renderOptions();
      this.highlightParents();
    },

    notifyUpdate: function() {
      // TODO
      this.model.toggleCollapsed(false);
      this.model.toggleCollapsed(true);
    },

    isVisible: function(view) {
      var parentModel = this.model,
          model = view.model,
          levelVisible = parentModel.getSelectedLevels().at(0).get("id") == model.get("level"),
          searchTerm = parentModel.get("searchterm"),
          searchVisible = searchTerm == null || RegExp(searchTerm,"i").test(model.get("name")),
          drillTerm = _.last(parentModel.get("breadcrumb")),
          drillVisible = drillTerm == null
            || model.get("qualifiedName").indexOf(drillTerm.get("qualifiedName")) > -1;

      return levelVisible && searchVisible && drillVisible;
    },

    highlightParents: function() {
      var parentModel = this.model,
        selectedMembers = parentModel.get("values").where({selected:true}).map(function(m) {
          return m.get("qualifiedName");
        });
      _(this._optionViews).each(function(v) {
        var name = v.model.get("qualifiedName");
        var childrenSelected = _(selectedMembers).chain()
            .without(v.model.get("qualifiedName"))
            .filter(function(m){return m.indexOf(name) > -1;})
            .value();
        /*
         * childrenSelected will always have at least the
         * member itself, so we need to check that length > 1
         */
        v.$el.toggleClass("highlight",childrenSelected.length > 0);
        if(childrenSelected.length > 0) {
          v.$el.find(".drill-down .label").text(childrenSelected.length);
        } else {
          v.$el.find(".drill-down .label").html("&nbsp;");
        }
      },this);
    },

    toggleSelected: function(m, selected) {
      var vInner,vOuter,v;
      if(selected) {
        vInner = new SelectionView({model:m});
        this.$el.find(".leftArea .selection").append(vInner.render().el);
        this._selectedViews.push(vInner);

        vOuter = new SelectionViewOut({model:m});
        this.$el.find(".outsideArea .selection").append(vOuter.render().el);
        this._selectedViews.push(vOuter);
      } else {
        v = _(this._selectedViews).filter(function(v) {return v.model === m;});
        var myself = this;
        _.each(v,function(e) {
          e.remove();
          myself._selectedViews = _(myself._selectedViews).without(e);
        });

        v = _(this._selectedViewsOut).filter(function(v) {return v.model === m;});
        var myself = this;
        _.each(v,function(e) {
          e.remove();
          myself._selectedViewsOut = _(myself._selectedViewsOut).without(e);
        });
      }
    },

    drillUp: function() {
      this.model.trigger("drillUp");
    },

    cancel: function() {
      this.model.cancel();
    },

    apply: function() {
      this.model.apply();
    },

    toggleCollapsed: function() {
      this.model.toggleCollapsed();
    },

    updateCollapsed: function() {
      $('.olapSelectorComponent').addClass('collapsed').removeClass('expanded');
      if(this.model.get("collapsed")) {
        this.$el.find('.olapSelectorComponent').addClass('collapsed').removeClass('expanded');
      } else {
        this.$el.find('.olapSelectorComponent').removeClass('collapsed').addClass('expanded');
        var optionList = this.$el.find(".optionList"),
            minimumMargin = 10,
            topEdge = optionList.offset().top,
            bottomEdge = topEdge + optionList.outerHeight(),
            topLimit = $(window).scrollTop(),
            bottomLimit = topLimit + $(window).height(),
            offset = bottomEdge <= bottomLimit ? 0 : bottomLimit - bottomEdge - minimumMargin;

            offset = topEdge - offset >= topLimit ? offset : topLimit - topEdge + minimumMargin;
            Logger.log("Offset is " + offset + " after correction");
            optionList.css("top", (optionList.position().top + offset) + "px");
      }
    },

    nextPage: function() {
      this.model.nextPage();
    },
    prevPage: function() {
      this.model.prevPage();
    },
    firstPage: function() {
      this.model.firstPage();
    },
    lastPage: function() {
      this.model.lastPage();
    },
    goToPage: function(evt) {
      var page = $(evt.target).attr("data-page");
      this.model.goToPage(page);
    }
  });

  /*
   * Option View displays a selector option, as it is represented
   * in the option listing
   */
  var OptionView = Backbone.View.extend({
    tagName: "span",
    template: null,
    events: {
      "click .target": "toggleSelection",
      "click .drill-down-enabled": "drillDown"
    },

    initialize: function() {
      this.setTemplate();
      this.model.on("change:selected", this.updateSelectionDisplay, this);
    },

    setTemplate: function(){
      this.template = templates.olapSelector.option
    },

    drillDown: function() {
      var val = this.model.get("drill");
      this.model.set("drill", !val);
    },

    render: function() {
      this.$el.html(Mustache.render(this.template, this.model.toJSON()));
      this.$el.addClass('item');
      this.updateSelectionDisplay();
      this.delegateEvents();
      return this;
    },

    toggleSelection: function() {
      this.model.toggleSelected();
      this.model.trigger("select");
    },

    updateSelectionDisplay: function() {
      if(this.model.get('selected')) {
        this.$el.addClass('selected');
      } else {
        this.$el.removeClass('selected');
      }
    }

  });

  var LevelView = OptionView.extend({
    tagName: "span",
    template: null,
    setTemplate: function() {
      this.template = templates.olapSelector.level;
    }
  });

  /*
   * Selection View displays a selector option, as it is represented
   * in the listing of selected options.
   */
  var SelectionView = Backbone.View.extend({
    tagName: "li",
    events: {
      "click .remove": "unselect"
    },

    render: function() {
      this.$el.html(Mustache.render(templates.olapSelector.picked, this.model.toJSON()));
      this.delegateEvents();
      return this;
    },

    unselect: function() {
      this.model.set("selected", false);
      this.model.trigger("select");
    }
  });

  var SelectionViewOut = SelectionView.extend({
    unselect: function() {
      this.model.set("selected", false);
      this.model.trigger("select");
      this.model.trigger("notify");
    }
  });

  /*
   * TEMPLATES
   */
  var templates = templates || {};
  templates.olapSelector = {};
  templates.olapSelector.main =
    "<div class='olapSelectorComponent'>" +
    " <div class='pulldown'>"+
    "   <div class='title'>{{title}}</div>"+
    "     <div class='optionList'>"+
    "       <div class='leftArea'>" +
    "         <div class='header'>Select Level</div>" +
    "         <div class='levels'></div>" +
    "         <div class='selectionPanel'>" +
    "           <div class='label'>Selected Filters</div>" +
    "           <ul class='selection'></ul>" +
    "         </div>" +
    "       </div>" +
    "       <div class='rightArea {{#paginate}}paginate{{/paginate}}'>" +
    "         <div class='header'>" +
    /* Don't touch the indentation! Indentating this
     * would create some pretty annoying text nodes
     */
    "<div class='breadcrumb'>Breadcrumb &#x21FE; Content</div>"+
    "<div class='search'>"+
    "             <input type='text' placeholder='Search...' value='{{searchterm}}'/>"+
    "             <div class='cancel'>&nbsp;</div>"+
    "           </div>"+
    "         </div>"+
    "         <div class='options'></div>"+
    "         <div class='paginationContainer'>"+
    "           <div class='pagination'>"+
    "             <div class='prev paginateButton'>Previous Page<div class='arrow'>&nbsp;</div></div>"+
    "             <div class='next paginateButton'>Next Page<div class='arrow'>&nbsp;</div></div>"+
    "           </div>"+
    "         </div>"+
    "         <div class='footer'>" +
    "           <div class='button cancel'>Cancel</div>"+
    "           <div class='button validate'>Apply</div>"+
    "         </div>" +
    "       </div>"+
    "     </div>" +
    " </div>" +
    " <div class='outsideArea'>"+
    "   <ul class='selection'></ul>"+
    " </div>"+
    "</div>";

  templates.olapSelector.option =
    "<div class='target'>" +
    " <span class='name' title='{{name}}'>{{name}}</span>" +
    " <span class='check'>&nbsp;</span>"+
    "</div>" +
    "{{#canDrillDown}}<div class='drill-down drill-down-enabled'>{{/canDrillDown}}" +
    "{{^canDrillDown}}<div class='drill-down drill-down-disabled'>{{/canDrillDown}}" +
    "<span class='label'>&nbsp;</span></div>";

  templates.olapSelector.picked =
    "<div class='target'>" +
    "  <span class='name' title='{{name}}'>{{name}}</span>" +
    "  <div class='remove'>&nbsp;</div>" +
    "</div>";

  templates.olapSelector.levels =
    "<div class='levelTitle'>Levels</div>"+
    "<div class='levels options'></div>";


  templates.olapSelector.level =
    "<div class='target'>" +
    "  <span class='name' title='{{label}}'>{{label}}</span>" +
    "</div>";


  templates.olapSelector.crumbtrail =
    "<span class='level'>{{level}}</span>" +
    "<span class='separator'>&nbsp;</span>" +
    "<span class='name'>{{name}}</span>";

  return OlapSelectorView;

});
