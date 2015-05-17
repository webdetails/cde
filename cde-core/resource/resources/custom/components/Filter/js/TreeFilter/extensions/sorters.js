'use strict';
(function($, Extensions) {
  return $.extend(true, Extensions.Sorters, {
    selectedOnTop: function(model, idx) {
      var result;
      result = model.getSelection() ? 'A' : 'Z';
      return result += idx;
    },
    sameOrder: function(model, idx) {
      var result;
      return result = idx;
    },
    sortAlphabetically: function(model, idx) {
      var result;
      return result = model.get('label');
    },
    sortByValue: function(model, idx) {
      var result;
      return result = -(model.get('value')) || 0;
    }
  });
})($, TreeFilter.Extensions);
