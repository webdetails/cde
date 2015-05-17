/**
 * @module BaseFilter
 * @submodule SelectionStrategies
 */
define([
   'amd!cdf/lib/underscore',
   './AbstractSelect'],
   function( _, BaseFilter ) {

      /**
       * Multiple Selection
       *  - any number of items can be selected
      #
       * @class MultiSelect
       * @extends AbstractSelect
       * @constructor
       */
      BaseFilter.SelectionStrategies.MultiSelect = BaseFilter.SelectionStrategies.AbstractSelect.extend({
        ID: 'BaseFilter.SelectionStrategies.MultiSelect',
        setSelection: function(newState, model) {
          model.setAndUpdateSelection(newState);
          return newState;
        }
      });

      /**
       * Limited (Multiple) Selection
       *  - any number of items can be selected, up to a limit
      #
       * @class LimitedSelect
       * @extends AbstractSelect
       * @constructor
       * @param {Object} options
       */
      BaseFilter.SelectionStrategies.LimitedSelect = BaseFilter.SelectionStrategies.MultiSelect.extend({
        ID: 'BaseFilter.SelectionStrategies.LimitedSelect',
        constructor: function(options) {
          return this.selectionLimit = options.limit || Infinity;
        },
        setSelection: function(newState, model) {
          var allow, numberOfUnselectedItems, oldState, selectedItems;
          allow = true;
          oldState = model.getSelection();
          newState = this.getNewState(oldState);
          if (newState !== BaseFilter.Enum.select.NONE) {
            selectedItems = model.root().get('numberOfSelectedItems');
            if (!_.isFinite(selectedItems)) {
              model.update();
              selectedItems = model.root().get('numberOfSelectedItems');
            }
            if (selectedItems >= this.selectionLimit) {
              this.warn("Cannot allow the selection of  \"" + (model.get('label')) + "\". Selection limit of " + this.selectionLimit + " has been reached.");
              allow = false;
            } else {
              if (model.children()) {
                if (newState === BaseFilter.Enum.select.ALL) {
                  numberOfUnselectedItems = model.flatten().filter(function(m) {
                    return m.children() == null;
                  }).filter(function(m) {
                    return m.getSelection() === BaseFilter.Enum.select.NONE;
                  }).value().length;
                  if (selectedItems + numberOfUnselectedItems >= this.selectionLimit) {
                    this.warn("Cannot allow the selection of \"" + (model.get('label')) + "\". Selection limit of " + this.selectionLimit + " would be reached.");
                    allow = false;
                  }
                }
              }
            }
          }
          if (allow) {
            this.debug("setSelection");
            model.setAndUpdateSelection(newState);
            selectedItems = model.root().get('numberOfSelectedItems');
            model.root().set("reachedSelectionLimit", selectedItems >= this.selectionLimit);
          } else {
            newState = oldState;
          }
          return newState;
        }
      });

    return BaseFilter;
});
