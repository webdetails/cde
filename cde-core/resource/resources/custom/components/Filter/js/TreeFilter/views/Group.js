'use strict';

/**
 * @module TreeFilter
 * @submodule Views
 */
(function(Views, Templates) {
  'use strict';

  /**
   * View for groups of items
   * @class Group
   * @constructor
   * @extends AbstractView
   */
  return Views.Group = Views.AbstractView.extend({
    type: 'Group',
    ID: 'TreeFilter.Views.Group',
    template: {
      skeleton: Templates['Group-skeleton'],
      selection: Templates['Group-template']
    },
    events: {
      'change    .filter-filter:eq(0)': 'onFilterChange',
      'keyup     .filter-filter:eq(0)': 'onFilterChange',
      'click     .filter-filter-clear:eq(0)': 'onFilterClear',
      'click     .filter-group-selection': 'onSelection',
      'click     .filter-collapse-icon:eq(0)': 'onToggleCollapse',
      'mouseover .filter-group-container': 'onMouseOver',
      'mouseout  .filter-group-container': 'onMouseOut'
    },
    bindToModel: function(model) {
      this.base(model);
      this.onChange(model, 'isSelected numberOfSelectedItems numberOfItems', this.updateSelection);
      return this.onChange(model, 'isCollapsed', this.updateCollapse);
    },
    updateCollapse: function() {
      var viewModel;
      viewModel = this.getViewModel();
      return this.renderCollapse(viewModel);
    },
    renderCollapse: function(viewModel) {
      var collapsable;
      this.renderSelection(viewModel);
      collapsable = ['.filter-group-body', '.filter-group-footer'].join(', ');
      if (viewModel.isCollapsed) {
        return this.$(collapsable).hide();
      } else {
        return this.$(collapsable).show();
      }
    }
  });
})(TreeFilter.Views, TreeFilter.templates);
