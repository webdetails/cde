'use strict';

/**
 * @module TreeFilter
 * @submodule Controllers
 */
(function(_, TreeFilter, Controllers, Tree) {
  'use strict';

  /**
   * Controller responsible for managing the hierarchy of views and controllers.
  #
   * When data is added to the model, the Manager reacts by creating
   * the appropriate views and respective controllers
  #
   * @class Manager
   * @constructor
   * @extends Tree
   */
  Controllers.Manager = Tree.extend({
    ID: 'TreeFilter.Controllers.Manager',
    defaults: {
      model: null,
      view: null,
      controller: null,
      configuration: null
    },
    constructor: function(options) {
      this.base.apply(this, arguments);
      this.updateChildren();
      return this;
    },
    initialize: function(options) {
      if (this.get('view') == null) {
        this.addViewAndController(this.get('model'));
      }
      this.applyBindings();
      return this;
    },
    close: function() {
      this.get('view').close();
      this.get('controller').stopListening().off();
      this.stopListening();
      this.off();
      this.clear();
      return this;
    },
    applyBindings: function() {
      var bindings, that, throttleFilter, throttleScroll;
      that = this;
      throttleScroll = function(f) {
        var throttleTimeMilliseconds;
        throttleTimeMilliseconds = that.get('configuration').pagination.throttleTimeMilliseconds;
        return _.throttle(f, throttleTimeMilliseconds || 0, {
          trailing: false
        });
      };
      throttleFilter = function(f) {
        var throttleTimeMilliseconds;
        throttleTimeMilliseconds = that.get('view').config.view.throttleTimeMilliseconds;
        return _.throttle(f, throttleTimeMilliseconds || 0, {
          leading: false
        });
      };

      /**
       * Declare bindings to model and view
       */
      bindings = {
        model: {
          'add': this.onNewData,
          'change:selectedItems': this.onApply,
          'selection': this.sortSiblings
        },
        view: {
          'filter': throttleFilter(this.onFilterChange),
          'scroll:reached:top': throttleScroll(this.getPreviousPage),
          'scroll:reached:bottom': throttleScroll(this.getNextPage)
        }
      };

      /**
       * Create listeners
       */
      that = this;
      _.each(bindings, function(bindingList, object) {
        return _.each(bindingList, function(method, event) {
          return that.listenTo(that.attributes[object], event, _.bind(method, that));
        });
      });
      this.on('post:child:selection request:child:sort', this.sortChildren);
      this.on('post:child:add', _.throttle(this.onUpdateChildren, 1000, {
        leading: false
      }));
      return this;
    },
    addViewAndController: function(newModel) {

      /**
       * Decide which view to use
       */
      var Controller, View, childConfig, configuration, controller, newController, newView, shareController, target, that;
      shareController = true;
      if (this.parent() != null) {

        /**
         * This node is either a Group or an Item
         * Use the parent's configuration
         */
        that = this.parent();
        configuration = that.get('configuration');
        childConfig = configuration[that.get('view').type].view.childConfig;
        target = that.get('view').createChildNode();
        if (newModel.children()) {
          View = TreeFilter.Views[childConfig.withChildrenPrototype];
        } else {
          View = TreeFilter.Views[childConfig.withoutChildrenPrototype];
        }
        Controller = TreeFilter.Controllers.RootCtrl;
        controller = that.get('controller');
      } else {

        /**
         * This node is the Root.
         * A configuration object must have been passed as an option
         */
        configuration = this.get('configuration');
        target = configuration.target;
        View = TreeFilter.Views.Root;
        Controller = TreeFilter.Controllers.RootCtrl;
        controller = null;
      }

      /**
       * Create new view
       */
      newView = new View({
        model: newModel,
        configuration: configuration,
        target: target
      });
      this.set('view', newView);

      /**
       * Reuse the existing controller, or create a new one, if needed
       */
      if (shareController === true && controller !== null) {
        newController = controller;
        newController.bindToView(newView);
      } else {
        newController = new Controller({
          model: newModel,
          view: newView,
          configuration: configuration
        });
      }
      this.set('controller', newController);
      this.debug("addViewAndController is done for " + (newModel.get('id')) + " : " + (newModel.get('label')));
      return this;
    },
    onNewData: function(item, collection, obj) {
      var itemParent;
      this.debug("New data (" + (item.get('label')) + ") caught by " + (this.get('model').get('label')));
      itemParent = this.where({
        model: item.parent()
      });
      if (itemParent.length === 1) {
        return itemParent[0].trigger("post:child:add");
      }
    },
    onUpdateChildren: function() {
      this.debug("New data added to " + (this.get('model').get('label')) + " : updating children");
      this.updateChildren();
      this.restoreScroll();
      return this.trigger('post:update:children', this);
    },
    restoreScroll: function() {
      if (this.get('view')._scrollBar != null) {
        this.debug("This group has a scrollbar");
        if (this.previousPosition != null) {
          this.debug("Scrolling back");
          this.get('view').setScrollBarAt(this.previousPosition);
          return this.previousPosition = null;
        }
      }
    },

    /**
     * Pagination
     */
    getNextPage: function(model, event) {
      var orderedChildren, ref, sorter;
      sorter = this.getSorter();
      if (_.isFunction(sorter)) {
        orderedChildren = this.children().sortBy(function(m, idx) {
          return sorter(m.get('model'), idx);
        });
        this.previousPosition = (ref = _.last(orderedChildren, 2)[0]) != null ? ref.get('view').$el : void 0;
      }
      return this.getPage('next', model, event);
    },
    getPreviousPage: function(model, event) {
      var orderedChildren, ref, sorter;
      sorter = this.getSorter();
      if (_.isFunction(sorter)) {
        orderedChildren = this.children().sortBy(function(m, idx) {
          return sorter(m.get('model'), idx);
        });
        this.previousPosition = (ref = _.first(orderedChildren, 2)[1]) != null ? ref.get('view').$el : void 0;
      }
      return this.getPage('previous', model, event);
    },
    getPage: function(page, model, event) {
      var deferred;
      this.debug("Item " + (model.get('label')) + " requested page " + page);
      deferred = this.requestPage(page, this._searchPattern);
      return deferred;
    },
    requestPage: function(page, searchPattern) {
      var deferred, getPage, that;
      getPage = this.get('configuration').pagination.getPage;
      if (!_.isFunction(getPage)) {
        return this;
      }
      that = this;
      deferred = getPage(page, searchPattern).then(function(json) {
        if (json.resultset != null) {
          return that.debug("getPage: got " + json.resultset.length + " more items");
        } else {
          return that.debug("getPage: no more items");
        }
      });
      return deferred;
    },

    /**
     * Child management
     */
    updateChildren: function() {
      var models;
      models = this.get('model').children();
      if (models != null) {
        models.each((function(_this) {
          return function(m) {
            var hasModel;
            if (_this.children()) {
              hasModel = _.any(_this.children().map(function(child) {
                return child.get('model') === m;
              }));
            } else {
              hasModel = false;
            }
            if (!hasModel) {
              _this.debug("adding child model " + (m.get('label')));
              return _this.addChild(m);
            }
          };
        })(this));
        this.sortChildren();
        this.get('view').updateScrollBar();
      }
      return this;
    },

    /**
     * Create a new manager for this MVC tuple
     * @method addChild
     * @chainable
     */
    addChild: function(newModel) {
      var newManager;
      newManager = {
        model: newModel,
        configuration: this.get('configuration')
      };
      this.add(newManager);
      return this;
    },
    removeChild: function(model) {
      throw new Error("NotImplemented");
      return this;
    },
    sortSiblings: function(model) {
      this.debug("sortSiblings: " + (this.get('model').get('label')) + " was triggered from " + (model.get('label')) + ":" + (model.getSelection()));
      if (this.get('model') !== model) {
        return this;
      }
      if (this.parent()) {
        return this.parent().trigger('request:child:sort');
      }
    },
    getSorter: function() {
      var configuration, customSorter, type;
      type = this.children().first().get('view').type;
      customSorter = this.get('configuration')[type].sorter;
      if (!customSorter) {
        return void 0;
      }
      configuration = this.get('configuration');
      if (_.isFunction(customSorter)) {
        return function(model, idx) {
          return customSorter(null, model, configuration);
        };
      } else if (_.isArray(customSorter)) {

        /**
         * Use multiple sorters, one after the other
         */
        return function(model, idx) {
          return _.chain(customSorter).map(function(sorter) {
            return sorter(null, model, configuration);
          }).join('').value();
        };
      }
    },
    sortChildren: function() {
      var $nursery, children, customSorter, orderedChildren, sorter;
      if (!this.children()) {
        return this;
      }
      customSorter = this.getSorter();
      if (_.isFunction(customSorter)) {
        sorter = function(child, idx) {
          return customSorter(child.item.get('model'), idx);
        };
        $nursery = this.get('view').getChildrenContainer();
        $nursery.hide();
        children = this._detachChildren();
        orderedChildren = _.sortBy(children, sorter);
        this._appendChildren(orderedChildren);
        $nursery.show();
      }
      return this;
    },
    _detachChildren: function() {
      var children;
      if (this.children()) {
        children = this.children().map(function(child) {
          var result;
          result = {
            item: child,
            target: child.get('view').$el.detach()
          };
          return result;
        });
      } else {
        children = null;
      }
      return children;
    },
    _appendChildren: function(children) {
      if (children != null) {
        _.each(children, (function(_this) {
          return function(child) {
            return _this.get('view').appendChildNode(child.target);
          };
        })(this));
      }
      return this;
    },

    /**
     * React to the user typing in the search box
     * @method onFilterChange
     * @param {String} text
     * @for Manager
     */
    onFilterChange: function(text) {
      var filter, that;
      this._searchPattern = text.trim().toLowerCase();
      filter = _.bind(function() {
        var isMatch;
        isMatch = this.filter(this._searchPattern);
        return this.get('model').setVisibility(true);
      }, this);
      if (this.get('configuration').search.serverSide === true) {
        that = this;
        this.requestPage(0, this._searchPattern).then(function() {
          _.defer(filter);
        });
      }
      _.defer(filter);
    },
    filter: function(text, prefix) {

      /**
       * decide on item visibility based on a match to a filter string
       * The children are processed first in order to ensure the visibility is reset correctly
       * if the user decides to delete/clear the search box
       */
      var fullString, isMatch;
      fullString = _.chain(['label']).map((function(_this) {
        return function(property) {
          return _this.get('model').get(property);
        };
      })(this)).compact().value().join(' ');
      if (prefix) {
        fullString = prefix + fullString;
      }
      if (this.children()) {
        isMatch = _.any(this.children().map(function(manager) {
          var childIsMatch;
          childIsMatch = manager.filter(text, fullString);
          manager.get('model').setVisibility(childIsMatch);
          return childIsMatch;
        }));
      } else if (_.isEmpty(text)) {
        isMatch = true;
      } else {
        isMatch = fullString.toLowerCase().match(text.toLowerCase()) != null;
        this.debug("fullstring  " + fullString + " match to " + text + ": " + isMatch);
      }
      this.get('model').setVisibility(isMatch);
      return isMatch;
    },

    /**
     * Management of selected items
     */
    onApply: function(model) {
      return this.onFilterChange('');
    }
  });
})(_, TreeFilter, TreeFilter.Controllers, TreeFilter.Models.Tree);
