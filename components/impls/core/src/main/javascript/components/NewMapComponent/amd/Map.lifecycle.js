/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

define([
  "amd!cdf/lib/underscore"
], function(_) {
  "use strict";
  return {
    maybeToggleBlock: function(block) {
      if (!this.isSilent()) {
        block ? this.block() : this.unblock();
      }
    },

    getQueryData: function() {
      var query = this.queryState = this.query = this.dashboard.getQuery(this.queryDefinition);
      query.setAjaxOptions({async: true});
      query.fetchData(
        this.parameters,
        this.getSuccessHandler(_.bind(this.onDataReady, this)),
        this.getErrorHandler());
    },

    _concludeUpdate: function() {
      // google mapEngine implementation will still fetch data asynchronously before ca
      // so only here can we finish the lifecycle.
      this.postExec();
      this.maybeToggleBlock(false);
    }
  };

});
