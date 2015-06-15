'use strict';

/**
 * @module TreeFilter
 * @submodule Views
 */
(function($, _, BaseView, Mustache, LoggerMixin, Views) {

  /**
   * Abstract base class for all Views
   * @class Abstract
   * @constructor
   * @extends BaseView
   * @uses TreeFilter.Logger
   */
  Views.AbstractView = BaseView.extend(LoggerMixin).extend({
    initialize: function(options) {
      this.configuration = options.configuration;
      this.config = this.configuration[this.type];

      /**
       * Consider user-defined templates
       */
      if (this.config.view.templates != null) {
        $.extend(true, this.template, this.config.view.templates);
      }
      if (this.model) {
        this.bindToModel(this.model);
      }
      this.setElement(options.target);
      this.render();
      return this;
    },
    bindToModel: function(model) {
      this.onChange(model, 'isVisible', this.updateVisibility);
      return this;
    },
    onChange: function(model, properties, callback) {
      var events, props;
      props = properties.split(' ');
      events = _.map(props, function(prop) {
        return 'change:' + prop;
      }).join(' ');
      if (this.config.view.throttleTimeMilliseconds >= 0) {
        this.listenTo(model, events, _.throttle(callback, this.config.view.throttleTime, {
          leading: false
        }));
      } else {
        this.listenTo(model, events, callback);
      }
      return this;
    },
    updateSlot: function(slot) {
      return _.bind(function() {
        var renderer, viewModel;
        viewModel = this.getViewModel();
        renderer = this.renderSlot('slot');
        return renderer.call(this, viewModel);
      }, this);
    },
    renderSlot: function(slot) {
      return _.bind(function(viewModel) {
        var html;
        if (this.template[slot]) {
          html = Mustache.render(this.template[slot], viewModel);
          this.$(this.config.view.slots[slot]).replaceWith(html);
        }
        this.injectContent(slot);
        return TreeFilter.count++;
      }, this);
    },

    /**
     * View methods
     */
    getViewModel: function() {
      var viewOptions;
      viewOptions = _.result(this.config, 'options');
      return $.extend(true, this.model.toJSON(), viewOptions, {
        strings: _.result(this.config, 'strings'),
        selectionStrategy: _.omit(this.configuration.selectionStrategy, 'strategy'),
        isPartiallySelected: this.model.getSelection() === TreeFilter.Enum.select.SOME,
        numberOfChildren: this.model.children() ? this.model.children().length : 0
      });
    },
    injectContent: function(slot) {
      var ref, ref1, renderers, that;
      renderers = (ref = this.config) != null ? (ref1 = ref.renderers) != null ? ref1[slot] : void 0 : void 0;
      if (renderers == null) {
        return;
      }
      this.debug("injecting");
      if (!_.isArray(renderers)) {
        renderers = [renderers];
      }
      that = this;
      _.each(renderers, function(renderer) {
        if (_.isFunction(renderer)) {
          return renderer.call(that, that.$el, that.model, that.configuration);
        }
      });
      return this;
    },

    /**
     * Fully renders the view
     * @method render
     * @chainable
     */
    render: function() {
      var viewModel;
      viewModel = this.getViewModel();
      this.renderSkeleton(viewModel);
      this.renderSelection(viewModel);
      this.updateVisibility(viewModel);
      return this;
    },
    renderSkeleton: function(viewModel) {
      this.$el.html(Mustache.render(this.template.skeleton, viewModel));
      TreeFilter.count++;
      return this;
    },
    updateSelection: function(model, options) {
      var viewModel;
      if (model === this.model) {
        viewModel = this.getViewModel();
        this.renderSelection(viewModel);
      }
      return this;
    },
    renderSelection: function(viewModel) {
      var html;
      html = Mustache.render(this.template.selection, viewModel);
      this.$(this.config.view.slots.selection).replaceWith(html);
      this.injectContent('selection');
      return TreeFilter.count++;
    },
    updateVisibility: function() {
      if (this.model.getVisibility()) {
        return this.$el.show();
      } else {
        return this.$el.hide();
      }
    },

    /**
     * Children management
     */
    getChildrenContainer: function() {
      return this.$(this.config.view.slots.children);
    },
    createChildNode: function() {
      var $child, $target;
      $child = $('<div/>').addClass(this.config.view.childConfig.className);
      $target = this.$(this.config.view.slots.children);
      $child.appendTo($target);
      return $child;
    },
    appendChildNode: function($child) {
      var $target;
      $target = this.$(this.config.view.slots.children);
      $child.appendTo($target);
      return $child;
    },

    /**
     * Scrollbar methods
     */
    updateScrollBar: function() {
      var nItems, needsScrollBar;
      nItems = this.config.options.scrollThreshold;
      needsScrollBar = _.isFinite(this.configuration.pagination.pageSize) && this.configuration.pagination.pageSize > 0;
      needsScrollBar = needsScrollBar || this.type !== 'Item' && this.model.flatten().size().value() > nItems;
      if (needsScrollBar) {
        this.log("There are more than " + nItems + " items, adding scroll bar");
        return this.addScrollBar();
      }
    },
    addScrollBar: function() {
      var $container, options, that;
      if (this._scrollBar != null) {
        return this;
      }
      this.debug("Adding a scrollbar to " + (this.model.get('label')));
      that = this;
      switch (this.config.view.scrollbar.engine) {
        case 'optiscroll':
          this._scrollBar = this.$(this.config.view.slots.children).addClass('optiscroll-content').parent().addClass('optiscroll').optiscroll().off('scrollreachbottom').on('scrollreachbottom', function(event) {
            return that.trigger('scroll:reached:bottom', that.model, event);
          }).off('scrollreachtop').on('scrollreachtop', function(event) {
            return that.trigger('scroll:reached:top', that.model, event);
          }).data('optiscroll');
          break;
        case 'mCustomScrollbar':
          options = $.extend(true, {}, this.config.view.scrollbar.options, {
            callbacks: {
              onTotalScroll: function() {
                return that.trigger('scroll:reached:bottom', that.model);
              },
              onTotalScrollBack: function() {
                return that.trigger('scroll:reached:top', that.model);
              }
            }
          });
          this._scrollBar = this.$(this.config.view.slots.children).parent().mCustomScrollbar(options);
      }
      if (this.config.options.isResizable) {
        $container = this.$(this.config.view.slots.children).parent();
        if (_.isFunction($container.resizable)) {
          $container.resizable({
            handles: 's'
          });
        }
      }
      return this;
    },
    setScrollBarAt: function($tgt) {
      if (this._scrollBar != null) {
        this._scrollBar.scrollIntoView($tgt);
      }
      return this;
    },

    /**
     * Events triggered by the user
     */
    onMouseOver: function(event) {
      var $node;
      $node = this.$(this.config.view.slots.selection);
      $node = this.$('div:eq(0)');
      this.trigger('mouseover', this.model);
      return this;
    },
    onMouseOut: function(event) {
      var $node;
      $node = this.$(this.config.view.slots.selection);
      $node = this.$('div:eq(0)');
      this.trigger('mouseout', this.model);
      return this;
    },
    onSelection: function() {
      this.trigger('selected', this.model);
      return this;
    },
    onApply: function(event) {
      this.trigger('control:apply', this.model);
      return this;
    },
    onCancel: function(event) {
      this.debug("triggered Cancel");
      this.trigger('control:cancel', this.model);
      return this;
    },
    onFilterChange: function(event) {
      var text;
      text = $(event.target).val();
      this.trigger('filter', text, this.model);
      return this;
    },
    onFilterClear: function(event) {
      var text;
      text = '';
      this.$('.filter-filter-input:eq(0)').val(text);
      this.trigger('filter', text, this.model);
      return this;
    },
    onToggleCollapse: function(event) {
      this.debug("triggered collapse");
      this.trigger("toggleCollapse", this.model, event);
      return this;
    },

    /**
     * Boilerplate methods
     */
    close: function() {
      this.remove();
      return this.unbind();

      /**
       * Update tree of views
       */
    }
  });
})($, _, BaseView, Mustache, TreeFilter.Logger, TreeFilter.Views);
