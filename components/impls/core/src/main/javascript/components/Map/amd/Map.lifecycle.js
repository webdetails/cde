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
