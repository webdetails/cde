'use strict';
(function(SelectionStrategies) {
  'use strict';

  /**
   * @module TreeFilter
   * @submodule SelectionStrategies
   */

  /**
   * Single Selection
   *  - only one item can be selected at any time
   * @class SingleSelect
   * @extends AbstractSelect
   * @constructor
   */
  return SelectionStrategies.SingleSelect = SelectionStrategies.AbstractSelect.extend({
    ID: 'TreeFilter.SelectionStrategies.SingleSelect',
    setSelection: function(newState, model) {
      if (model.children()) {
        return;
      }
      if (this.isLogicGlobal === true) {
        model.root().setSelection(TreeFilter.Enum.select.NONE);
      } else if (model.getSelection() !== TreeFilter.Enum.select.ALL) {
        if (model.parent()) {
          model.parent().setSelection(TreeFilter.Enum.select.NONE);
        }
      }
      model.setAndUpdateSelection(TreeFilter.Enum.select.ALL);
      return newState;
    },
    changeSelection: function(model) {
      this.base(model);
      return this.applySelection(model);
    }
  });
})(TreeFilter.SelectionStrategies);
