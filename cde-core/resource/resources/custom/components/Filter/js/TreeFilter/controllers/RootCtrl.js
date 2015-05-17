'use strict';

/**
 * @module TreeFilter
 * @submodule Controllers
 */
(function(_, BaseBone, BaseView, LoggerMixin, Controllers) {
  'use strict';

  /**
   * General-purpose controller
   * @class RootCtrl
   * @constructor
   * @uses TreeFilter.Logger
   * @extends Backbone.View
   */
  return Controllers.RootCtrl = BaseBone.extendWithEvents(Base).extend(LoggerMixin).extend({
    constructor: function(args) {
      $.extend(this, _.pick(args, ['model', 'view', 'configuration']));
      if (this.view) {
        this.bindToView(this.view);
      }
      return this;
    },
    bindToView: function(view) {
      var bindings, that;
      bindings = {
        'selected': this.onSelection,
        'toggleCollapse': this.onToggleCollapse,
        'control:only-this': this.onOnlyThis,
        'control:apply': this.onApply,
        'control:cancel': this.onCancel,
        'click:outside': this.onClickOutside
      };
      that = this;
      _.each(bindings, function(callback, event) {
        return that.listenTo(view, event, callback);
      });
      return this;
    },

    /**
     * Event handling
     */

    /**
     * Acts upon the model whenever the user selected something.
     * Delegates work to the current selection strategy
     * @method onSelection
     * @chainable
     */
    onSelection: function(model) {
      this.configuration.selectionStrategy.strategy.changeSelection(model);
      return this;
    },

    /**
     * Informs the model that the user chose to commit the current selection
     * Delegates work to the current selection strategy
     * @method onApply
     * @chainable
     */
    onApply: function(model) {
      this.configuration.selectionStrategy.strategy.applySelection(model);
      return this;
    },

    /**
     * Informs the model that the user chose to revert to the last saved selection
     * Delegates work to the current selection strategy
     * @method onCancel
     * @chainable
     */
    onCancel: function(model) {
      model.restoreSelectedItems();
      model.root().set('isCollapsed', true);
      return this;
    },
    onToggleCollapse: function(model) {
      var newState, oldState;
      this.debug("Setting isCollapsed");
      if (model.get('isDisabled') === true) {
        newState = true;
      } else {
        oldState = model.get('isCollapsed');
        newState = !oldState;
      }
      model.set('isCollapsed', newState);
      return this;
    },
    onClickOutside: function(model) {
      model.set('isCollapsed', true);
      return this;
    },
    onOnlyThis: function(model) {
      this.debug("Setting Only This");
      this.model.root().setSelection(TreeFilter.Enum.select.NONE);
      this.configuration.selectionStrategy.strategy.setSelection(TreeFilter.Enum.select.ALL, model);
      return this;
    }
  });
})(_, BaseBone, BaseView, TreeFilter.Logger, TreeFilter.Controllers);
