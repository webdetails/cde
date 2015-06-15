'use strict';
(function(_, TreeFilter, Models, Tree) {
  'use strict';

  /**
   * Represents the state of the filter as tree structure.
  #
   * @module TreeFilter
   * @submodule Models
   * @class SelectionTree
   * @constructor
   * @extends Tree
   */
  return Models.SelectionTree = Tree.extend({

    /**
     * @property {Object} [defaults]
     * @private
     * Default values for each node in the selection tree
     */
    defaults: {
      id: void 0,
      label: "Label",
      isSelected: false,
      isVisible: true,
      isCollapsed: true,
      numberOfSelectedItems: 0,
      numberOfItems: 0,
      page: 0
    },
    constructor: function(attributes, options) {
      if ((attributes != null ? attributes.label : void 0) != null) {
        if ((attributes.id == null) || ((options != null ? options.useValueAsId : void 0) === true)) {
          attributes.id = attributes.label;
        }
      }
      return this.base(attributes, options);
    },
    initialize: function() {
      this.base.apply(this, arguments);
      return this.on('add remove', this.update);
    },
    sync: function(action, model, options) {
      this.log("Please " + action + " item " + (model.get('label')));
      return _.each(model.where({
        isSelected: true
      }), function(m) {
        return this.log("Processing " + action + " on item " + (m.get('label')));
      });
    },

    /**
     * sets the selection state of the model
     * @method setSelection
     * @public
     */
    setSelection: function(newState) {
      if (this.getSelection() === newState) {
        return this;
      }
      this.set('isSelected', newState);
      if (newState !== TreeFilter.Enum.select.SOME) {
        if (this.children()) {
          this.children().each(function(child) {
            return child.setSelection(newState);
          });
        }
      }
      if (this.parent()) {
        this.parent().updateSelection();
      }
      return this;
    },

    /**
     * gets the selection state of the model
     * @method getSelection
     * @public
     * @return {Boolean}
     */
    getSelection: function() {
      return this.get('isSelected');
    },
    setAndUpdateSelection: function(newState) {
      this.setSelection(newState);
      this.update();
      return this.trigger('selection', this);
    },
    setVisibility: function(newState) {
      var isVisible;
      isVisible = this.get('isVisible');
      if (isVisible !== newState) {
        return this.set('isVisible', newState);
      }
    },
    getVisibility: function() {
      return this.get('isVisible');
    },
    getSelectedItems: function(field) {
      var getMyself, isSelected;
      getMyself = (function(_this) {
        return function() {
          var value;
          value = _this.get(field || 'id');
          return value;
        };
      })(this);
      isSelected = this.getSelection();
      switch (isSelected) {
        case TreeFilter.Enum.select.SOME:
        case void 0:
          if (this.children()) {
            return _.flatten(this.children().map(function(child) {
              return child.getSelectedItems(field) || [];
            }));
          } else {
            return getMyself();
          }
          break;
        case TreeFilter.Enum.select.ALL:
          return getMyself();
        case TreeFilter.Enum.select.NONE:
          return [];
        default:
          return [];
      }
    },

    /**
     * Mark listed items as selected
     * NOTE: currently acts directly on the model and bypasses any business logic
     * TODO: change implementation to be recursive rather than acting on a flat tree
     * @method setSelectedItems
     */
    setSelectedItems: function(idList) {
      var flatTree;
      flatTree = this.flatten();
      flatTree.filter(function(m) {
        return m.children() == null;
      }).each(function(m) {
        var id;
        id = m.get('id');
        if (_.contains(idList, id)) {
          return m.setSelection(TreeFilter.Enum.select.ALL);
        } else {
          return m.setSelection(TreeFilter.Enum.select.NONE);
        }
      });
      flatTree.filter(function(m) {
        return m.children() != null;
      }).each(function(m) {
        var id;
        id = m.get('id');
        if (_.contains(idList, id)) {
          return m.setSelection(TreeFilter.Enum.select.ALL);
        }
      });
      this.update();
      return this.root().updateSelectedItems({
        silent: true
      });
    },
    updateSelectedItems: function(options) {
      return this.root().set('selectedItems', this._getSelectionSnapshot(), options);
    },
    restoreSelectedItems: function() {
      var selectedItems;
      selectedItems = this.root().get('selectedItems');
      if (selectedItems == null) {
        selectedItems = {
          none: this.flatten()
        };
      }
      selectedItems.none.each(function(m) {
        return m.setSelection(TreeFilter.Enum.select.NONE);
      });
      if (selectedItems.all != null) {
        selectedItems.all.each(function(m) {
          return m.setSelection(TreeFilter.Enum.select.ALL);
        });
      }
      return this.update();
    },
    _getSelectionSnapshot: function() {
      var flatTree, selectionSnapshot;
      flatTree = this.flatten();
      selectionSnapshot = {
        none: flatTree.filter(function(m) {
          return m.getSelection() === TreeFilter.Enum.select.NONE;
        }),
        some: flatTree.filter(function(m) {
          return m.getSelection() === TreeFilter.Enum.select.SOME;
        }),
        all: flatTree.filter(function(m) {
          return m.getSelection() === TreeFilter.Enum.select.ALL;
        })
      };
      return selectionSnapshot;
    },
    update: function() {
      var numberOfServerItems;
      this.root().updateSelection();
      numberOfServerItems = this.root().get('numberOfItemsAtServer');
      if (numberOfServerItems != null) {
        this.root().set('numberOfItems', numberOfServerItems);
      } else {
        this.root().updateCountOfItems('numberOfItems', function(model) {
          return 1;
        });
      }
      this.root().updateCountOfItems('numberOfSelectedItems', function(model) {
        if (model.getSelection() === TreeFilter.Enum.select.ALL) {
          return 1;
        } else {
          return 0;
        }
      });
      return this;
    },
    updateSelection: function() {
      var inferParentSelectionStateFromChildren;
      inferParentSelectionStateFromChildren = function(childrenStates) {
        var all, none;
        all = _.every(childrenStates, function(el) {
          return el === TreeFilter.Enum.select.ALL;
        });
        none = _.every(childrenStates, function(el) {
          return el === TreeFilter.Enum.select.NONE;
        });
        if (all) {
          return TreeFilter.Enum.select.ALL;
        } else if (none) {
          return TreeFilter.Enum.select.NONE;
        } else {
          return TreeFilter.Enum.select.SOME;
        }
      };
      return this.inferSelection(inferParentSelectionStateFromChildren, function(model, isSelected) {
        if (model.children()) {
          if (model.getSelection() !== isSelected) {
            return model.setSelection(isSelected);
          }
        }
      });
    },
    inferSelection: function(logic, callback) {

      /**
       * calculate the current state based on the state of the children
       * and optionally execute a callback
       */
      var bothCallback, itemCallback;
      itemCallback = function(node) {
        return node.getSelection();
      };
      bothCallback = function(node, result) {
        if (_.isFunction(callback)) {
          callback(node, result);
        }
        return result;
      };
      return this.walkDown(itemCallback, logic, bothCallback);
    },
    countItems: function(callback) {
      var count;
      if (this.children()) {
        count = this.children().reduce(function(memo, child) {
          return memo + child.countItems(callback);
        }, 0);
      } else {
        count = callback(this);
      }
      return count;
    },
    updateCountOfItems: function(property, callback) {
      var countItem, setCountOfItems, sumItems;
      countItem = function(model) {
        return callback(model);
      };
      sumItems = function(list) {
        return _.reduce(list, (function(memo, n) {
          return memo + n;
        }), 0);
      };
      setCountOfItems = function(model, count) {
        if (model.children()) {
          model.set(property, count);
        }
        return count;
      };
      return this.walkDown(countItem, sumItems, setCountOfItems);
    },
    countSelectedItems: function() {
      return this.countItems(function(model) {
        if (model.getSelection() === TreeFilter.Enum.select.ALL) {
          return 1;
        } else {
          return 0;
        }
      });
    },
    updateCountOfSelectedItems: function() {
      var countSelectedItem, setSelectedItems, sumSelectedItems;
      countSelectedItem = function(model) {
        if (model.getSelection() === TreeFilter.Enum.select.ALL) {
          return 1;
        } else {
          return 0;
        }
      };
      sumSelectedItems = function(list) {
        return _.reduce(list, (function(memo, n) {
          return memo + n;
        }), 0);
      };
      setSelectedItems = function(model, count) {
        if (model.children()) {
          model.set('numberOfSelectedItems', count);
        }
        return count;
      };
      return this.walkDown(countSelectedItem, sumSelectedItems, setSelectedItems);
    },
    hasChanged: function() {
      var hasChanged, previousSelection;
      previousSelection = this.get('selectedItems');
      if (previousSelection != null) {
        hasChanged = _.any(_.map(this._getSelectionSnapshot(), function(current, state) {
          var intersection, previous;
          previous = previousSelection[state];
          intersection = current.intersection(previous.value()).value();
          return !(current.isEqual(intersection).value() && previous.isEqual(intersection).value());
        }));
      } else {
        hasChanged = false;
      }
      return hasChanged;
    },
    setBusy: function(isBusy) {
      this.root().set('isBusy', isBusy);
      return this;
    },
    isBusy: function() {
      return this.root().get('isBusy');
    }
  });
})(_, TreeFilter, TreeFilter.Models, TreeFilter.Models.Tree);
