(function($, Mustache, Extensions) {
  'use strict';
  $.extend(true, Extensions.Renderers, {
    group: function($tgt, model, configuration) {
      var footer, header, viewModel;
      viewModel = model.toJSON();
      header = Mustache.render("{{label}}", viewModel);
      footer = Mustache.render("<a href=\"http://www.google.com\">More about {{label}}</a>", viewModel);
      $tgt.find('.filter-group-title').html(header);
      $tgt.find('.filter-group-footer').html(footer);
      return $tgt.css({
        border: "1px solid rgb(" + (_.random(255)) + "," + (_.random(255)) + "," + (_.random(255)) + ")"
      });
    },
    sumSelected: function($tgt, model, configuration) {
      var filter, total;
      total = model.flatten().filter(function(m) {
        return m.children() == null;
      }).filter(function(m) {
        return m.getSelection() === true;
      }).reduce((function(memo, m) {
        return memo + m.get('value');
      }), 0).value();
      filter = model.isRoot() ? '.filter-root-selection-value' : '.filter-group-selection-value';
      return $tgt.find(filter + ':eq(0)').html(total === 0 ? '' : total);
    },
    Item: function($tgt, model, configuration) {
      var blocks;
      blocks = {
        item: "<span>" + viewModel.label + "</span> <span style='float:right;'>comem</>"
      };
      return $tgt.find('.filter-item-body').html(blocks.item);
    },
    rootHeaderSingleSelect: function($tgt, model, configuration) {
      var header;
      header = model.getSelectedItems()[0] || "None";
      return $tgt.find('.filter-root-header-label').html(header).attr('title', header);
    },
    rootHeaderMultiSelect: function($tgt, model, configuration) {
      var header, viewModel;
      viewModel = model.toJSON();
      header = Mustache.render("<span class=\"filter-root-info-number-selected-items\">\n  {{numberOfSelectedItems}}\n</span>\n<span class=\"filter-root-info-number-items\">\n  of {{numberOfItems}}\n</span>", viewModel);
      if (typeof console !== "undefined" && console !== null) {
        console.log("injecting content on header");
      }
      $tgt.find('.filter-root-header-label').html(header).attr('title', Mustache.render("{{numberOfSelectedItems}}/{{numberOfItems}}", viewModel));
      return $tgt.find('.filter-root-header-label').mouseover(function(event) {
        return typeof console !== "undefined" && console !== null ? console.log("hovering " + viewModel.label) : void 0;
      });
    },
    notificationSelectionLimit: function($tgt, model, configuration) {
      var footer, viewModel;
      viewModel = $.extend(true, model.toJSON(), configuration);
      footer = Mustache.render("{{#reachedSelectionLimit}}\n<div class=\"filter-root-notification\">\n  <div class=\"filter-root-notification-icon\" />\n  <div class=\"filter-root-notification-text\">\n    The selection limit\n    (<span class=\"filter-notification-highlight\">{{Root.options.selectionStrategy.limit}}</span>)\n    for specific items has been reached.\n  </div>\n</div>\n{{/reachedSelectionLimit}}", viewModel);
      return $tgt.find('.filter-root-footer').html(footer);
    }
  });
})($, Mustache, TreeFilter.Extensions);
