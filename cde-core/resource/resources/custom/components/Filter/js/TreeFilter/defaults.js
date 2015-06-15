'use strict';
(function(TreeFilter) {
  return TreeFilter.defaults = function() {

    /**
     * Default settings
     * @module TreeSelect
     * @submodule defaults
     * @main
     */
    var defaults, privateDefaults;
    privateDefaults = {
      logLevel: 1,
      pagination: {
        throttleTimeMilliseconds: 500
      },
      Root: {
        renderers: void 0,
        sorter: void 0,
        view: {
          styles: [],
          throttleTimeMilliseconds: 10,
          templates: {},
          slots: {
            selection: '.filter-root-control',
            header: '.filter-root-header',
            footer: '.filter-root-footer',
            children: '.filter-root-items',
            overlay: '.filter-overlay'
          },
          childConfig: {
            withChildrenPrototype: 'Group',
            withoutChildrenPrototype: 'Item',
            className: 'filter-root-child',
            appendTo: '.filter-root-items'
          },
          overlaySimulateClick: true
        }
      },
      Group: {
        renderers: void 0,
        sorter: void 0,
        view: {
          throttleTimeMilliseconds: 10,
          templates: {},
          slots: {
            selection: '.filter-group-header:eq(0)',
            children: '.filter-group-items'
          },
          childConfig: {
            withChildrenPrototype: 'Group',
            withoutChildrenPrototype: 'Item',
            className: 'filter-group-child'
          }
        }
      },
      Item: {
        renderers: void 0,
        sorter: void 0,
        view: {
          styles: [],
          throttleTimeMilliseconds: 10,
          templates: {},
          slots: {
            selection: '.filter-item-container'
          }
        }
      }
    };
    return defaults = $.extend(true, {}, privateDefaults, {

      /**
       * @property pagination
       * @type Object
       * @for defaults
       */
      pagination: {
        pageSize: Infinity
      },
      search: {
        serverSide: false
      },
      selectionStrategy: {
        type: 'LimitedSelect',
        limit: 500
      },

      /**
       * Configuration of the Root
       * @property Root
       * @type Object
       * @for defaults
       */
      Root: {
        options: {
          className: 'multi-select',
          styles: void 0,
          showCommitButtons: true,
          showFilter: false,
          showGroupSelection: true,
          showButtonOnlyThis: false,
          showSelectedItems: false,
          showNumberOfSelectedItems: true,
          showValue: false,
          showIcons: true,
          scrollThreshold: 12,
          isResizable: true,
          useOverlay: true
        },
        strings: {
          isDisabled: 'Unavailable',
          allItems: 'All',
          noItems: 'None',
          groupSelection: 'All',
          btnApply: 'Apply',
          btnCancel: 'Cancel'
        },
        view: {
          scrollbar: {
            engine: 'mCustomScrollbar',
            options: {
              theme: 'dark',
              alwaysTriggerOffsets: false,
              onTotalScrollOffset: 100
            }
          }
        }
      },

      /**
       * Configuration of the Group
       * @property Group
       * @type Object
       */
      Group: {
        options: {
          showFilter: false,
          showCommitButtons: false,
          showGroupSelection: false,
          showButtonOnlyThis: false,
          showButtonCollapse: false,
          showValue: false,
          showIcons: true,
          scrollThreshold: Infinity,
          isResizable: false
        },
        strings: {
          allItems: 'All',
          noItems: 'None',
          groupSelection: 'All',
          btnApply: 'Apply',
          btnCancel: 'Cancel'
        }
      },

      /**
       * Configuration of the Item
       * @property Item
       * @type Object
       */
      Item: {
        options: {
          showButtonOnlyThis: false,
          showValue: false,
          showIcons: true
        },
        strings: {
          btnOnlyThis: 'Only'
        }
      }
    });
  };
})(TreeFilter);
