
define([
   'cdf/lib/jquery'],
   function($) {

        /**
         * MVC-based tree-like filter that supports
         * - multiple nested groups
         * - server-side pagination and searching
         * @module BaseFilter
         * @main
         */

        /**
         * Schmiede, mein Hammer, ein hartes Schwert!
         */
        var BaseFilter = {

          /**
           * MVC Models used internally to represent and manipulate information
           * @submodule Models
           * @main
           */
          Models: {},

          /**
           * MVC views that listen to changes in a model and trigger events that will eventually be handled by a Controller
           * @submodule Views
           * @main
           */
          Views: {},

          /**
           * Set of Controllers responsible for handling the interaction between views and models
           * @submodule Controllers
           * @main
           */
          Controllers: {},

          /**
           * Controller-like set of classes design to encapsulate the selection strategy
           * and isolate that "business" logic from lower-level view interaction logic.
          #
           * These classes are singletons passed as part of the configuration objects.
           * @submodule SelectionStrategies
           * @main
           */
          SelectionStrategies: {},

          /**
           * The MVC component consumes data in a specific format.
           * As such, it requires classes that:
           * - Import data to the filter
           * - Export the selection model
           * @submodule DataHandlers
           * @main
           */
          DataHandlers: {},

          /**
           * Extension points: Sorters and Renderers
           * @submodule Extensions
           * @main
           */
          Extensions: {
            Sorters: {},
            Renderers: {}
          },
          defaults: {},
          templates: {},
          count: 0,

          /**
           * Enumerations
           * @module BaseFilter
           * @submodule Enum
           * @main
           */
          Enum: {

            /**
             * @module BaseFilter
             * @submodule Enum
             * Selection states
             * @class select
             * @static
             */
            select: {
              SOME: null,
              NONE: false,
              ALL: true
            },
            selectionStrategy: {
              'LimitedSelect': {
                Root: {
                  options: {
                    className: 'multi-select',
                    showCommitButtons: true,
                    showSelectedItems: false,
                    showNumberOfSelectedItems: true,
                    showGroupSelection: true,
                    label: 'All'
                  }
                },
                Item: {
                  options: {
                    showButtonOnlyThis: true
                  }
                },
                selectionStrategy: {
                  type: 'LimitedSelect',
                  limit: 500
                },
                output: {
                  trigger: 'apply'
                }
              },
              'MultiSelect': {
                Root: {
                  options: {
                    className: 'multi-select',
                    showCommitButtons: true,
                    showSelectedItems: false,
                    showNumberOfSelectedItems: true,
                    showGroupSelection: true,
                    label: 'All'
                  }
                },
                Item: {
                  options: {
                    showButtonOnlyThis: true
                  }
                },
                selectionStrategy: {
                  type: 'MultiSelect'
                },
                output: {
                  trigger: 'apply'
                }
              },
              'SingleSelect': {
                Root: {
                  options: {
                    className: 'single-select',
                    showCommitButtons: false,
                    showSelectedItems: true,
                    showNumberOfSelectedItems: false,
                    showGroupSelection: false
                  }
                },
                Item: {
                  options: {
                    showButtonOnlyThis: false
                  }
                },
                selectionStrategy: {
                  type: 'SingleSelect'
                },
                output: {
                  trigger: 'apply'
                }
              }
            }
          }
        };

    return BaseFilter;
});
