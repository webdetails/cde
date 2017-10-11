/*!
 * Copyright 2002 - 2017 Webdetails, a Hitachi Vantara company. All rights reserved.
 *
 * This software was developed by Webdetails and is provided under the terms
 * of the Mozilla Public License, Version 2.0, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */

define([], function() {
  "use strict";
  return {

    /**
     * Gets the current selection state
     * @method getValue
     * @public
     * @return {Array} List of strings containing the IDs of the selected items,
     * in the same format as they would be written to the parameter
     */
    getValue: function() {
      var selectedItems = this.model.leafs()
        .filter(function(m) {
          return m.getSelection() === true;
        })
        .map(function(m) {
          return m.get("id");
        })
        .value();
      return selectedItems;
    },

    /**
     * Sets the selection state
     * @method setValue
     * @public
     * @param {Array} value List of strings containing the IDs of the selected items,
     * which will be written to the parameter
     * @chainable
     */
    setValue: function(idList) {
      if (this.model) {
        this.model.setSelectedItems(idList);
      } else {
        throw "Model is not initialized";
      }
      return this;
    },

    updateSelection: function() {
      // Mark selected model items
      var idList = this.dashboard.getParameterValue(this.parameter);
      this.setValue(idList);
    },

    /**
     * Implement's CDF logic for updating the state of the parameter, by
     * invoking Dashboards.processChange()
     * @method processChange
     * @public
     * @param {Array} value List of strings containing the IDs of the selected items,
     * in the same format as they would be written to the parameter
     */
    processChange: function() {
      //console.debug('processChange was called: ', (new Date()).toISOString());
      this.dashboard.processChange(this.name);
      return this;
    }
  };

});
