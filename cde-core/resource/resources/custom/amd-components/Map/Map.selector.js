define([], function () {

  return {

    /**
     * Gets the current selection state
     * @method getValue
     * @public
     * @return {Array} List of strings containing the IDs of the selected items,
     * in the same format as they would be written to the parameter
     */
    getValue: function () {
      var selectedItems = this.model.leafs()
        .filter(function(m){
          return m.getSelection() === true;
        })
        .map(function(m){
          return m.get('id');
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
    setValue: function (idList) {
      if (this.model) {
        this.model.setSelectedItems(idList);
      } else {
        throw 'Model is not initialized';
      }
      return this;
    },

    updateSelection: function () {
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
    processChange: function () {
      console.debug('processChange was called: ', (new Date()).toISOString());
      this.dashboard.processChange(this.name);
      return this;
    }
  };


});