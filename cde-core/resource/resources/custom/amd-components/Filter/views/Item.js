
/**
 * @module BaseFilter
 * @submodule Views
 */

define([
    './Abstract'],
    function( BaseFilter ) {

  /**
   * View for items
   * @class Item
   * @constructor
   * @extends AbstractView
   */
  BaseFilter.Views.Item = BaseFilter.Views.AbstractView.extend({
    type: 'Item',
    ID: 'BaseFilter.Views.Root',
    template: {
      selection: BaseFilter.templates['Item-template'],
      skeleton: BaseFilter.templates['Item-template']
    },
    events: {
      'mouseover .filter-item-body': 'onMouseOver',
      'mouseout  .filter-item-body': 'onMouseOut',
      'click     .filter-item-body': 'onSelection',
      'click     .filter-item-only-this': 'onClickOnlyThis'
    },
    bindToModel: function(model) {
      this.base(model);
      this.onChange(model, 'isSelected', this.updateSelection);
      return this.onChange(model, 'isVisible', this.updateVisibility);
    },
    onClickOnlyThis: function(event) {
      event.stopPropagation();
      return this.trigger('control:only-this', this.model);
    }
  });

  return BaseFilter;
});
