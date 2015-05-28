/*
 * Selector Model describes the behaviour for the selector
 * as a whole. 
 */

var views = views || {};
views.pagingSelector = {};
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
    values.on("change:selected",this.updateSelection,this);
    this.on("change:searchterm",this.updateSearch,this);
    this.updateValues(values);
  },

  addPage: function(newValues) {
    var v,
      newValue,
      values = this.get("values");
    for(v = 0; v < newValues.length;v++) {
      var newValue = newValues[v],
          found = values.detect(function(model){
            return model.get("idx") == newValue.idx;
          });
      if (!found) {
        values.add(newValues[v],{silent:true});
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
  prevPage: function(){
    var start = this.get("pageStart"),
        size = this.get("pageSize");
    this.set("pageStart", Math.max(0,start - size));
  },
  firstPage: function(){
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
      _(this.get("values").without(m)).each(function(other){
        other.set("selected",false);
      });
    }
  },

  clearSelection: function() {
    this.get("values").each(function(m){
      m.set("selected",false);
    })
  },

  toggleCollapsed: function(){
    this.set("collapsed",!this.get("collapsed"));
  },

  selectedValues: function() {
    return _(this.get("values").where({selected: true})).map(function(m){return m.get("value")});
  },

  updateSearch: function() {
    var term = this.get("searchterm");
    var pattern = new RegExp(term);
    this.get("values").each(function(m){
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

  toggleSelected: function(){
    this.set("selected",!this.get("selected"));
  }
});

/*
 * Selector View displays the selector model
 */

views.pagingSelector.SelectorView = Backbone.View.extend({


  events: {
    "click .title": "toggleCollapsed",
    "click .first": "firstPage",
    "click .prev": "prevPage",
    "click .next": "nextPage",
    "click .last": "lastPage",
    "click .pages span": "goToPage",
    "change .search input": "updateSearch",
    "keydown .search input": "testChange",
    "click .validate" : "toggleCollapsed"
  },
  initialize: function() {
    
    this.initializeOptions();
    this.configureListeners();
  },

  configureListeners: function() {
    this.model.on("change", this.render, this);
    this.model.on("change:collapsed", this.updateCollapsed, this);
    this.model.on("change:pageStart", this.renderPages, this);
    var values = this.model.get("values");
    values.on("change:selected", this.toggleSelected, this);
    values.on("add remove reset",this.updateOptions, this)
  },

  initializeOptions: function() {
    this._selectedViews = [];
    this._optionViews = [];
    this.model.get("values").each(function(m){
      this._optionViews.push(new views.pagingSelector.OptionView({model:m}));
      if (m.get("selected")) {
        this._selectedViews.push(new views.pagingSelector.SelectionView({model:m}));
      } 
   }, this);

  },

  updateSearch: function(evt) {
    this.model.set("searchterm", evt.target.value);
    evt.stopPropagation();
  },
  
  testChange: function(evt) {
    /* check for Enter, to emulate the FFx/Chrome behaviour for onchange */
    if(evt.which == 13) {
      this.updateSearch(evt);
    }
  },
  render: function() {
    /*
     * Draw the selector proper
     */
    this.$el.html(Mustache.render(templates.pagingSelector.main, this.model.toJSON()));
    this.updateCollapsed();
    this.renderOptions();
    this.renderPages();
  },

  renderPages: function() {
    var m = this.model,
        currPage = Math.ceil(m.get("pageStart") / m.get("pageSize")),
        maxPages = Math.ceil(m.get("totalRecords") / m.get("pageSize")),
        $pages = this.$el.find(".pages"),
        i,
        preSkip = false, postSkip = false;
    $pages.empty();
    for (i = 0;i < maxPages; i++) {
      if(i < 5 || maxPages - i < 5 || Math.abs(currPage - i) < 2) {
        $pages.append($("<span class='page'>"+(i+1)+"</span>").attr("data-page",i).addClass(i == currPage ? "current" : ""));
      } else {
        if(!preSkip && i < currPage) {
          $pages.append("&hellip;");
          preSkip = true;
        } else if (!postSkip && i > currPage) {
          $pages.append("&hellip;");
          postSkip = true;
        }
      }
    }
  },
  renderOptions: function() {
    var optionContainer = this.$el.find(".options").empty(),
        selectionContainer = this.$el.find(".selection").empty(),
        minIdx = this.model.get('pageStart'),
        maxIdx = this.model.get('pageStart') + this.model.get('pageSize');
    _(this._optionViews).each(function(v){
      var idx = v.model.get("idx");
      if(idx < maxIdx && idx >= minIdx) {
        optionContainer.append(v.render().el);
      }
    });
    _(this._selectedViews).each(function(v){
      selectionContainer.append(v.render().el);
    });
  },

  updateOptions: function() {
    this.initializeOptions();
    this.renderOptions();
  },

  toggleSelected: function(m, selected) {
    var v;
    if (selected) {
      v = new views.pagingSelector.SelectionView({model:m});
      this.$el.find(".selection").append(v.render().el);
      this._selectedViews.push(v);
    } else {
      v = _(this._selectedViews).find(function(v) {return v.model === m});
      v.remove();
      this._selectedViews = _(this._selectedViews).without(v);
    }
  },
  toggleCollapsed: function(){
    this.model.toggleCollapsed();
  },

  updateCollapsed: function() {
  
    $('.selectorComponent').addClass('collapsed').removeClass('expanded');
    if (this.model.get("collapsed")) {
      this.$el.find('.selectorComponent').addClass('collapsed').removeClass('expanded');
    } else {

        // TODO: SAPATADA
        $(".datepicker-title").removeClass("datepicker-title-visible");
        $(".datepicker-outerbox").hide();
        $(".manage-views.active").click();


      this.$el.find('.selectorComponent').removeClass('collapsed').addClass('expanded');
      var optionList = this.$el.find(".optionList"),
          minimumMargin = 10,
          topEdge = optionList.offset().top,
          bottomEdge = topEdge + optionList.outerHeight(),
          topLimit = $(window).scrollTop(),
          bottomLimit = topLimit + $(window).height(),
          offset = bottomEdge <= bottomLimit ? 0 : bottomLimit - bottomEdge - minimumMargin;

      offset = topEdge - offset >= topLimit ? offset : topLimit - topEdge + minimumMargin;

      optionList.css("top", (optionList.position().top + offset) + "px");
    }
  },

  nextPage: function() {
    this.model.nextPage();
  },
  prevPage: function(){
    this.model.prevPage();
  },
  firstPage: function(){
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

views.pagingSelector.OptionView = Backbone.View.extend({

  tagName: "span",
  events: {
    "click .target": "toggleSelection"
  },
  initialize: function() {
    this.model.on("change:selected",this.updateSelectionDisplay,this);
    this.model.on("change:visible",this.updateVisibility,this);
  },
  render: function() {
    this.$el.html(Mustache.render(templates.pagingSelector.option, this.model.toJSON()));
    this.$el.addClass('item');  
    this.updateSelectionDisplay();
    this.updateVisibility();
    this.delegateEvents();
    return this;
  },

  toggleSelection: function() {
    this.model.toggleSelected();
  },

  updateVisibility: function() {
    if(this.model.get('visible')) {
      this.$el.show();
    } else {
      this.$el.hide();
    }
  },

  updateSelectionDisplay: function() {
    if(this.model.get('selected')) {
      this.$el.addClass('selected');
    } else {
      this.$el.removeClass('selected');
    }
  }

});

/*
 * Selection View displays a selector option, as it is represented
 * in the listing of selected options.
 */


views.pagingSelector.SelectionView = Backbone.View.extend({
  tagName: "li",
  events: {
    "click .remove": "unselect"
  },

  render: function() {
    this.$el.html(Mustache.render(templates.pagingSelector.picked, this.model.toJSON()));
    this.$el.addClass('item');
    this.delegateEvents();
    return this;
  },

  unselect: function() {
    this.model.set("selected", false);
  }
});

/* 
 * TEMPLATES
 */

var templates = templates || {};
templates.pagingSelector = {};
templates.pagingSelector.main =
  "<div class='selectorComponent'>" +
  "  <div class='pulldown'>"+
  "    <div class='title'>{{title}}</div>"+
  "      <div class='optionList'>"+
  "        <div class='search'>"+
  "          <input type='text' placeholder='Search...' value='{{searchterm}}'/>"+
  "          <div class='cancel'>&nbsp;</div>"+
  "        </div>"+
  "        <div class='options'></div>"+
  "        <div class='paginationContainer'>"+
  "          <div class='pagination'>"+
  "            <div class='first paginateButton'><div class='arrow'>&nbsp;</div></div>"+
  "            <div class='prev paginateButton'><div class='arrow'>&nbsp;</div></div>"+
  "            <div class='pages'></div>"+
  "            <div class='next paginateButton'><div class='arrow'>&nbsp;</div></div>"+
  "            <div class='last paginateButton'><div class='arrow'>&nbsp;</div></div>"+
  "          </div>"+
  "          <div class='validate'><div class='image'>&nbsp;</div></div>"+
  "        </div>"+
  "      </div>" +
  "  </div>" +
  "  <div class='selection'></div>"+
  "</div>";

templates.pagingSelector.option =
  "<div class='target'>" +
  "  <span class='name' title='{{label}}'>{{label}}</span>" +
  "  <span class='check'>&nbsp;</span>"+
  "  {{#new}}<span class='new'>&nbsp;</span>{{/new}}" +
  "</div>";

templates.pagingSelector.picked =
  "<div class='target'>" +
  "  <span class='name' title='{{label}}'>{{label}}</span>" +
  "  <div class='remove'>&nbsp;</div>" +
  "</div>";

