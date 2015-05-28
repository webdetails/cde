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
  'amd!cdf/lib/backbone',
  'amd!cdf/lib/underscore'],
  function(Logger, Backbone, _) {

  /*
   * Selector Model describes the behaviour for the selector
   * as a whole.
   */

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

        return cmpLevel != 0
          ? cmpLevel/Math.abs(cmpLevel)
          : cmpMember != 0
          ? cmpMember/Math.abs(cmpMember)
          : 0;
      };

      values.model = OptionModel;
      this.updateValues();
      this.set("levels", new Backbone.Collection());
      var levels = this.get("levels");
      levels.model = LevelModel;
      this.updateLevels();
      this.processLevelSelection();
      levels.on("change:selected", this.processLevelSelection, this);
      this.setupEvents();
      this.preSelectValues();
    },

    preSelectValues: function() {
      var myself = this,
          olapUtils = this.get("olapUtils"),
          defaultSelect = myself.get("preselected"),
          levelsArray = myself.get("levels");

      for(var i = 0; i < levelsArray.length; i++) {
        var options = {
          pageSize: this.get("pageSize"),
          pageStart: this.get("pageStart"),
          searchTerm: this.get("searchterm"),
          level: levelsArray.at(i).get("name")
        };

        olapUtils.getPaginatedLevelMembers(options, function(v) {
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
        var level = members[c];
        memberDepth = (level.qualifiedName.split(".").length - 1);
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

        values.add(newValue, {silent: true});
      }
      values.trigger("add");
    },

    updateLevels: function() {
      var olapUtils = this.get("olapUtils"),
      h = olapUtils.getHierarchy();

      if(h.hasAll) {
        var allMember = {
            name: h.defaultMember,
            qualifiedName: h.defaultMemberQualifiedName,
            allMember: true
        };
        this.get("levels").add(allMember);
      }

      this.get("levels").add(
        olapUtils.getLevels(),
        {silent: true});
    },

    processLevelSelection: function(evt) {
      // Make sure there's one and only one selected
      if(this.getSelectedLevels().length == 0) {

        if(evt == null) {
          this.get("levels").at(0).set({"selected": true});
        } else {
          evt.set({selected: true});
        }
        return; // we'll be back
      }

      if(this.getSelectedLevels().size() > 1) {

        _(this.getSelectedLevels().without(evt)).each(function(f) {
          f.set({selected: false})
        });
        // Trigger a change
        this.set("pageStart", 0);
        return; // we'll be back
      }
    },

    getSelectedLevels: function() {
      return new Backbone.Collection(
        this.get("levels").where({selected: true}));
    },

    setupEvents: function() {
      var values = this.get("values");
      values.on("change:selected", this.updateSelection, this);
      values.on("change:drill", this.drillDown, this);
      this.on("page", this.fetchValues, this, this);
      this.on("change:collapsed", this.handleCollapse, this);
      this.on("change:searchterm", this.updateSearch, this);
      this.on("drillUp", this.drillUp, this);
      this.get("levels").on("select", this.changeLevel, this);
    },

    handleCollapse: function(m, isCollapsed) {
      if(isCollapsed) { return; }
      /* When expanding the selector, we store the selection
       * state so that it can be restored later on. We only
       * need the qualified names to be able to do that.
       */
      var oldValues = this.get("values")
        .where({selected: true})
        .map(function(e) { return e.get("qualifiedName"); });
      this.set("oldValues", oldValues);

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
      this.get("values").each(function(e) {
        e.set("selected", oldValues.indexOf(e.get("qualifiedName")) > -1);
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
        selectedLevel = levels.where({selected: true})[0],
        qualifiedName = selectedLevel.get("qualifiedName"),
        idx = levels.indexOf(selectedLevel);
      selectedLevel.set("selected",false);
      atTopLevel =  qualifiedName == defaultQualified;
      if(atTopLevel) {
        levels.at(idx+2).set("selected", true);
      } else {
        levels.at(idx+1).set("selected", true);
      }
      this.set("pageStart", 0, {silent: true});
      this.trigger("drill");
      this.fetchValues();
    },

    drillUp: function() {
      var crumbtrail = this.get("breadcrumb");
      if(crumbtrail.length) {
        crumbtrail.pop();
        var levels = this.get("levels"),
          selectedLevel = levels.where({selected: true})[0],
          idx = levels.indexOf(selectedLevel);
        selectedLevel.set("selected", false);
        this.set("pageStart",0,{silent: true});
        levels.at(idx - 1).set("selected", true);
        this.trigger("drill");
        this.fetchValues();
      }
    },

    changeLevel: function(m, value) {
      /*
       * We don't really want to update if we're just
       * being notified that a value was deselected
       */
      if(m && value === false) { return; }
      this.set("breadcrumb", []);
      this.trigger("drill");
      this.set("pageStart", 0);
      this.fetchValues();
    },

    fetchValues: function(m, value) {
      var selectedLevel =  this.get("levels").where({selected: true})[0],
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

      var paramArray = this.get("parameters");
      if(paramArray.length > 0) {
        var nameArray = _.keys(paramArray);

        for(var pos = 0; pos < paramArray.length; pos++) {
          options[nameArray[pos]] = paramArray[nameArray[pos]];
        }
      }

      var myself = this;
      this.get("olapUtils").getPaginatedLevelMembers(options, function(v) {
        myself.set("morePages", v.more);
        myself.addPage(v.members);
      });
    },

    addPage: function(newValues) {
      var v,
          newValue,
          idx = this.get("pageStart"),
          values = this.get("values");
      for(v = 0; v < newValues.length;v++) {
        var newValue = newValues[v], found;
        newValue.level = this.getSelectedLevels().at(0).get("qualifiedName");
        found = values.detect(function(model) {
          return model.get("level") == newValue.level &&
            model.get("qualifiedName") == newValue.qualifiedName;
        });

        if(!found) {
          values.add(newValue, {silent: true});
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

    prevPage: function() {
      if(this.get("pageStart") == 0) {
        return;
      }
      var start = this.get("pageStart"),
      size = this.get("pageSize");
      this.set("pageStart", Math.max(0, start - size));
      this.trigger("page");
    },

    firstPage: function() {
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
      _(this.get("values").without(m)).each(function(other) {
        /*
         * If we're working with single-select, we need to ensure
         * that only the last pick is marked as selected.
         */
        if(!isMultiselect) {
          other.set("selected", false);
        }
        /* Chosing both an element and its children can cause double-counting
         * issues so we should deselect the child elements if the option for
         * deselectDescendants is set on the component.
         */
        if(deselectDescendants && other.get("qualifiedName").indexOf(memberName) > -1) {
          other.set("selected", false);
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
      this.get("values").each(function(m) {
        m.set("selected", false);
      })
    },

    toggleCollapsed: function(predicate){
      this.set("collapsed", (_.isBoolean(predicate)) ? predicate : !this.get("collapsed"));
    },

    selectedValues: function() {
      return _(this.get("values").where({
        selected: true
      })).map(function(m) {
        return m.get("value");
      });
    },

    updateSearch: function() {
      var term = this.get("searchterm");
      this.set("pageStart", 0, {silent: true});
      this.fetchValues();
    },

    notifyUpdate: function() {
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

    toggleSelected: function() {
      this.set("selected", !this.get("selected"));
    }
  });

  var LevelModel = OptionModel.extend({

    defaults: {},

    initialize: function(o) {
      // Bind changing qualifiedName to id and name to value
      this.on("change:qualifiedName change:name", function(o) {
        Logger.log("Detected changes");
        this.set("id", o.get("qualifiedName"));
        this.set("label", o.get("name"));
      });
      this.set("id", this.get("qualifiedName"));
      this.set("label", this.get("name"));
    }

  });

  return OlapSelectorModel;

});

