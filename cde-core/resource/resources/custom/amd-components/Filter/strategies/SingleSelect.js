/**
 * @module BaseFilter
 * @submodule SelectionStrategies
 */
define([
   './AbstractSelect'],
   function( BaseFilter ) {

      /**
       * Single Selection
       *  - only one item can be selected at any time
       * @class SingleSelect
       * @extends AbstractSelect
       * @constructor
       */
      BaseFilter.SelectionStrategies.SingleSelect = BaseFilter.SelectionStrategies.AbstractSelect.extend({
        ID: 'BaseFilter.SelectionStrategies.SingleSelect',
        setSelection: function(newState, model) {
          if (model.children()) {
            return;
          }
          if (this.isLogicGlobal === true) {
            model.root().setSelection(BaseFilter.Enum.select.NONE);
          } else if (model.getSelection() !== BaseFilter.Enum.select.ALL) {
            if (model.parent()) {
              model.parent().setSelection(BaseFilter.Enum.select.NONE);
            }
          }
          model.setAndUpdateSelection(BaseFilter.Enum.select.ALL);
          return newState;
        },
        changeSelection: function(model) {
          this.base(model);
          return this.applySelection(model);
        }
      });
    return BaseFilter;
});
