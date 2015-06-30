/*
 * Selector Model describes the behaviour for the selector
 * as a whole.
 */

var wd = wd || {};
wd.components = wd.components || {};
wd.components.olapSelector = wd.components.olapSelector || {};


var OlapSelectorModel = Backbone.Model.extend({
    defaults: {
        "title": "",
        "search": true,
        "multiselect": true,
        "multilevelselect": true,
        "deselectDescendants": false,
        "searchterm": "",
        "collapsed": true,
        "values": null,
        "level": "",
        "paginate": true,
        "pageStart": 0,
        "pageSize": 42,
        "morePages": false,
        "totalRecords": 0,
        "mode": "level",
        olapUtils: null,
        "levels": null,
        "breadcrumb": [],
        "preselected": [],
        "parameters": []
    },

    initialize: function() {

      var values = new Backbone.Collection();
      this.set("values",values);
      values.comparator = function(left, right) {
          var leftLevel = left.get("level"),
              rightLevel = right.get("level"),
              leftMember = left.get("name"),
              rightMember = right.get("name"),
              cmpLevel = leftLevel.localeCompare(rightLevel),
              cmpMember = leftMember.localeCompare(rightMember);
          return cmpLevel != 0 ? cmpLevel/Math.abs(cmpLevel) :
              cmpMember != 0 ? cmpMember/Math.abs(cmpMember) :
              0;
      };
      values.model = OptionModel;
      this.updateValues();
      this.set("levels",new Backbone.Collection());
      var levels = this.get("levels");
      levels.model = LevelModel;
      this.updateLevels();
      this.processLevelSelection();
      levels.on("change:selected",this.processLevelSelection,this);
      this.setupEvents();
      this.preSelectValues();
    },

    preSelectValues: function() {
      var myself = this,
          olapUtils = this.get("olapUtils"),
          defaultSelect = myself.get("preselected"),
          levelsArray = myself.get("levels");

      for(var i = 0; i < levelsArray.length; i++) {
        options = {
            pageSize: this.get("pageSize"),
            pageStart: this.get("pageStart"),
            searchTerm: this.get("searchterm"),
            level: levelsArray.at(i).get("name")
        };

        olapUtils.getPaginatedLevelMembers(options ,function(v) {

          var toChange = myself.processPreSelectValues(v.members, defaultSelect);

          myself.addPreSelectValues(toChange, myself);

        });
      }
    },

    processPreSelectValues: function(members, defaultVals) {
      var values = [],
          selectedLevelDepth = this.getSelectedLevels().at(0).get("depth"),
          memberDepth;

      if(!defaultVals.length)  {
        return members;
      }

      for(var c = 0; c < members.length; c++) {
        level = members[c];
        memberDepth = (level.qualifiedName.split(".").length-1);
        for(var p = 0; p < defaultVals.length; p++) {
          if(level.qualifiedName === defaultVals[p]) {
            level.selected = true;
            values.push(level);
            break;
          } else if(selectedLevelDepth === memberDepth) {
            values.push(level);
            break;
          }
        }
      }
      return values;
    },

    addPreSelectValues: function(toChange, myself) {
      var newValue,
          depth,
          values = myself.get("values"),
          maxLevel = this.get("levels").length - 1;

      for(var c = 0; c < toChange.length; c++) {
        newValue = toChange[c];
        depth = newValue.qualifiedName.match(/(\[[^\]]+]\.?)/g).length - 2;

        newValue.level = myself.get("levels").at(depth).id;
        newValue.canDrillDown = depth < maxLevel;

        values.add(newValue, {
          silent: true
        });
      }
      values.trigger("add");
    },


    updateLevels: function(){
      var olapUtils = this.get("olapUtils"),
      h = olapUtils.getHierarchy();

      if (h.hasAll){

          var allMember = {
              name: h.defaultMember,
              qualifiedName: h.defaultMemberQualifiedName,
              allMember: true
          }
          this.get("levels").add(allMember);
      }

      this.get("levels").add(olapUtils.getLevels(),{
          silent:true
      });
    },

    processLevelSelection: function(evt){
      // Make sure there's one and only one selected
      if(this.getSelectedLevels().length == 0){

          if(evt == null){
              this.get("levels").at(0).set({
                  "selected":true
              });
          }
          else{
              evt.set({
                  selected:true
              });
          }
          return; // we'll be back
      }

      if(this.getSelectedLevels().size() > 1){

          _(this.getSelectedLevels().without(evt)).each(function(f){
              f.set({
                  selected:false
              })
          });
          // Trigger a change
          this.set("pageStart",0);
          return; // we'll be back
      }
    },

    getSelectedLevels: function(){
        return new Backbone.Collection(this.get("levels").where({
            selected: true
        }));
    },

    setupEvents: function() {
      var values = this.get("values");
      values.on("change:selected",this.updateSelection,this);
      values.on("change:drill",this.drillDown,this);
      this.on("page",this.fetchValues,this,this);
      this.on("change:collapsed", this.handleCollapse,this);
      this.on("change:searchterm",this.updateSearch,this);
      this.on("drillUp",this.drillUp,this);
      this.get("levels").on("select",this.changeLevel,this);
    },

    handleCollapse: function(m, isCollapsed) {
      if(isCollapsed) {
        return;
      }
      /* When expanding the selector, we store the selection
       * state so that it can be restored later on. We only
       * need the qualified names to be able to do that.
       */
      var oldValues = this.get("values")
          .where({selected:true})
          .map(function(e){return e.get("qualifiedName");});
      this.set("oldValues",oldValues);

    },

    apply: function() {
      this.toggleCollapsed();
    },

    cancel: function() {
      /* When cancelling a selection, we only need to restore
       * the selected attribute for all the members that were
       * selected when the last state save occurred
       */
      var oldValues = this.get("oldValues");
      this.get("values").each(function(e){
        e.set("selected",oldValues.indexOf(e.get("qualifiedName")) > -1);
      });
      this.get("values").trigger("select");
      this.toggleCollapsed();
    },

    drillDown: function(m, drill) {
      this.get("breadcrumb").push(m);
      var levels = this.get("levels"),
        olapUtils = this.get("olapUtils"),
        defaultQualified = olapUtils.getHierarchy().defaultMemberQualifiedName,
        atTopLevel,
        selectedLevel = levels.where({selected:true})[0],
        qualifiedName = selectedLevel.get("qualifiedName"),
        idx = levels.indexOf(selectedLevel);
      selectedLevel.set("selected",false);
      atTopLevel =  qualifiedName == defaultQualified;
      if(atTopLevel) {
        levels.at(idx+2).set("selected",true);
      } else {
        levels.at(idx+1).set("selected",true);
      }
      this.set("pageStart",0,{silent:true});
      this.trigger("drill");
      this.fetchValues();
    },

    drillUp: function() {
      var crumbtrail = this.get("breadcrumb");
      if(crumbtrail.length){
        crumbtrail.pop();
        var levels = this.get("levels"),
          selectedLevel = levels.where({selected:true})[0],
          idx = levels.indexOf(selectedLevel);
        selectedLevel.set("selected",false);
        this.set("pageStart",0,{silent:true});
        levels.at(idx-1).set("selected",true);
        this.trigger("drill");
        this.fetchValues();
      }
    },

    changeLevel: function(m, value) {
      /*
       * We don't really want to update if we're just
       * being notified that a value was deselected
       */
      if(m && value === false) {
        return;
      }
      this.set("breadcrumb",[]);
      this.trigger("drill");
      this.set("pageStart",0);
      this.fetchValues();
    },

    fetchValues: function(m, value) {
      var selectedLevel =  this.get("levels").where({selected:true})[0],
          olapUtils = this.get("olapUtils"),
          options = {
            pageSize: this.get("pageSize"),
            pageStart: this.get("pageStart"),
            searchTerm: this.get("searchterm")
          },
          breadcrumb = this.get("breadcrumb"),
          level;
      if(breadcrumb && breadcrumb.length) {
        var l = breadcrumb.length - 1;
        options.startMember = breadcrumb[l].get("qualifiedName");
      }
      level = selectedLevel ? selectedLevel.get("name") : "";
      if(!level || level == olapUtils.getHierarchy().defaultMember) {
        options.level = olapUtils.getLevels()[0].name;
      } else {
        options.level = level;
      }

      paramArray = this.get("parameters");
      if(paramArray.length > 0) {
        var nameArray = _.keys(paramArray);

        for(pos = 0; pos < paramArray.length; pos++) {
          options[nameArray[pos]] = paramArray[nameArray[pos]];

        }
      }

      var myself = this;
      this.get("olapUtils").getPaginatedLevelMembers(options,function(v){
        myself.set("morePages",v.more);
        myself.addPage(v.members);
      });
    },

    addPage: function(newValues) {
        var v,
        newValue,
        idx = this.get("pageStart");
        values = this.get("values");
        for(v = 0; v < newValues.length;v++) {
          var newValue = newValues[v],
              found;
          newValue.level = this.getSelectedLevels().at(0).get("qualifiedName");
          found = values.detect(function(model){
            return model.get("level") == newValue.level &&
                model.get("qualifiedName") == newValue.qualifiedName;
          });
          if (!found) {
            values.add(newValue, {
              silent:true
            });
          }
        }
        values.trigger("add");
    },

    nextPage: function() {
      if(!this.get("morePages")) {
        return;
      }
      var start = this.get("pageStart"),
      size = this.get("pageSize");
      this.set("pageStart", start + size);
      this.trigger("page");
    },

    prevPage: function(){
      if(this.get("pageStart") == 0) {
        return;
      }
      var start = this.get("pageStart"),
      size = this.get("pageSize");
      this.set("pageStart", Math.max(0,start - size));
      this.trigger("page");
    },

    firstPage: function(){
      if(this.get("pageStart") == 0) {
        return;
      }
      this.set("pageStart", 0);
      this.trigger("page");
    },

    lastPage: function() {
        var total = this.get("totalRecords"),
        size = this.get("pageSize");
        this.set("pageStart", total - total % size);
      this.trigger("page");
    },

    goToPage: function(page) {
      var size = this.get("pageSize");
      this.set("pageStart", page * size);
      this.trigger("page");
    },

    updateValues: function(values) {
        this.get("values").reset(values);
    },

    updateSelection: function(m, selected) {
        /* No special action needs to take
         * place if we're deselecting stuff*/
        if(!selected) {
          return
        }

        var isMultiselect = this.get("multiselect"),
            deselectDescendants = this.get("deselectDescendants"),
            isMultilevel = this.get("multilevelselect"),
            selectedLevel = this.getSelectedLevels().at(0).get("qualifiedName"),
            memberName = m.get("qualifiedName");
        _(this.get("values").without(m)).each(function(other){
            /*
             * If we're working with single-select, we need to ensure
             * that only the last pick is marked as selected.
             */
            if(!isMultiselect) {
              other.set("selected",false);
            }
            /* Chosing both an element and its children can cause double-counting
             * issues so we should deselect the child elements if the option for
             * deselectDescendants is set on the component.
             */
            if (deselectDescendants && other.get("qualifiedName").indexOf(memberName) > -1) {
              other.set("selected",false);
            }
            /* If the option is set to only allow selection from a single level, then
             * we need to clear out selections from other levels
             */
            if(!isMultilevel && selectedLevel != other.get("level")) {
              other.set("selected", false);
            }
        });
    },

    clearSelection: function() {
        this.get("values").each(function(m){
            m.set("selected",false);
        })
    },

    toggleCollapsed: function(predicate){
        this.set("collapsed", ( _.isBoolean(predicate) ) ? predicate : !this.get("collapsed") );
    },

    selectedValues: function() {
        return _(this.get("values").where({
            selected: true
        })).map(function(m){
            return m.get("value")
        });
    },

    updateSearch: function() {
      var term = this.get("searchterm");
      this.set("pageStart",0,{silent:true});
      this.fetchValues();
    },

    notifyUpdate: function (){
      this.trigger('notify', this);
    }
});

/*
 * Option Model describes the behaviour for each individual
 * option within the selector.
 */
var OptionModel = Backbone.Model.extend({
    defaults: {
        "idx": 0,
        "name": "",
        "value": null,
        "level": null,
        "drill": false,
        "selected": false
    },

    toggleSelected: function(){
        this.set("selected",!this.get("selected"));
    }
});

var LevelModel = OptionModel.extend({

    defaults: {},

    initialize: function(o){

        // Bind changing qualifiedName to id and name to value
        this.on("change:qualifiedName change:name",function(o){
            Dashboards.log("Detected changes");
            this.set("id",o.get("qualifiedName"));
            this.set("label",o.get("name"));
        });
        this.set("id",this.get("qualifiedName"));
        this.set("label",this.get("name"));

    }

});


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
        _(this.levelViews).each(function(v){
            levelContainer.append(v.render().el);
        });
    },

    initialize: function() {

        this.initializeOptions();
        this.configureListeners();
        this.levelViews = [];
        this.model.get("levels").each(function(m){
            this.levelViews.push(new LevelView({
                model:m
            }));
        }, this);   },

    configureListeners: function() {
        this.model.on("change", this.render, this);
        this.model.on("drill", this.renderCrumbtrail, this);
        this.model.on("change:collapsed", this.updateCollapsed, this);
        this.model.on("page", this.renderPages, this);
        var values = this.model.get("values");
        values.on("add remove reset",this.updateOptions, this)
        values.on("select", this.updateOptions, this);
        values.on("notify", this.notifyUpdate, this);
    },

    initializeOptions: function() {
        this._selectedViews = [];
        this._selectedViewsOut = [];
        this._optionViews = [];
        this.model.get("values").each(function(m){
            this._optionViews.push(new OptionView({
                model:m
            }));
            if (m.get("selected")) {
                this._selectedViews.push(new SelectionView({
                    model:m
                }));
                this._selectedViewsOut.push(new SelectionViewOut({
                    model:m
                }));
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
      var currentLevelName = this.model.get("levels").where({selected:true})[0].get("name"),
          crumbtrail = this.model.get("breadcrumb");
      if(crumbtrail.length > 0){
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
      this.$el.find(".pagination .next").toggleClass("disabled",!m.get("morePages"));
      this.$el.find(".pagination .prev").toggleClass("disabled",!m.get("pageStart"));
      this.$el.find(".pagination .first").toggleClass("disabled",!m.get("pageStart"));
    },
    renderOptions: function() {
        var optionContainer = this.$el.find(".rightArea .options").empty(),
            selectionContainer = this.$el.find(".leftArea .selection").empty(),
            selectionContainerOut = this.$el.find(".outsideArea .selection").empty(),
            myself = this,
            minIdx = this.model.get('pageStart'),
            maxIdx = this.model.get('pageStart') + this.model.get('pageSize');
        _(this._optionViews).chain().filter(this.isVisible,this).slice(minIdx,maxIdx).each(function(v){
          optionContainer.append(v.render().el);
        },this);
        _(this._selectedViewsOut).each(function(v){
            selectionContainerOut.append(v.render().el);
        });
        _(this._selectedViews).each(function(v){
            selectionContainer.append(v.render().el);
        });
    },

    updateOptions: function() {
        this.initializeOptions();
        this.renderOptions();
        this.highlightParents();
    },

    notifyUpdate: function (){
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
        drillVisible = drillTerm == null || model.get("qualifiedName").indexOf(drillTerm.get("qualifiedName")) > -1;
      return levelVisible && searchVisible && drillVisible;
    },

    highlightParents: function() {
      var parentModel = this.model,
          selectedMembers = parentModel.get("values").where({selected:true}).map(function(m){return m.get("qualifiedName");});
      _(this._optionViews).each(function (v){
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
        if (childrenSelected.length > 0) {
          v.$el.find(".drill-down .label").text(childrenSelected.length);
        } else {
          v.$el.find(".drill-down .label").html("&nbsp;");
        }
      },this);
    },

    toggleSelected: function(m, selected) {
        var vInner,vOuter,v;
        if (selected) {
            vInner = new SelectionView({
                model:m
            });
            this.$el.find(".leftArea .selection").append(vInner.render().el);
            this._selectedViews.push(vInner);

            vOuter = new SelectionViewOut({
                model:m
            });
            this.$el.find(".outsideArea .selection").append(vOuter.render().el);
            this._selectedViews.push(vOuter);
          } else {
            v = _(this._selectedViews).filter(function(v) {
                return v.model === m
            });
            var myself = this;
            _.each(v,function(e){
              e.remove();
              myself._selectedViews = _(myself._selectedViews).without(e);
            });

            v = _(this._selectedViewsOut).filter(function(v) {
                return v.model === m
            });
            var myself = this;
            _.each(v,function(e){
              e.remove();
              myself._selectedViewsOut = _(myself._selectedViewsOut).without(e);
            });
        }
    },

    drillUp: function() {
      this.model.trigger("drillUp");
    },

    cancel: function(){
        this.model.cancel();
    },

    apply: function(){
        this.model.apply();
    },

    toggleCollapsed: function(){
        this.model.toggleCollapsed();
    },

    updateCollapsed: function() {

        $('.olapSelectorComponent').addClass('collapsed').removeClass('expanded');
        if (this.model.get("collapsed")) {
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

var OptionView = Backbone.View.extend({

    tagName: "span",
    template: null,
    events: {
        "click .target": "toggleSelection",
        "click .drill-down-enabled": "drillDown"
    },
    initialize: function() {
        this.setTemplate();
        this.model.on("change:selected",this.updateSelectionDisplay,this);
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

    setTemplate: function(){
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
