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


/* Developed by Sinn Tecnologia -  rodrigo@sinn.com.br */

/*
 * Configure RequireJS amd! loader plugin to load jquery.ga.js which is not
 * compatible with AMD.
 *
 */

var requireConfig = requireCfg.config;

if(!requireConfig['amd']) {
  requireConfig['amd'] = {};
} 
if(!requireConfig['amd']['shim']) {
  requireConfig['amd']['shim'] = {};
}
requireConfig['amd']['shim']["cde/components/GoogleAnalyticsComponent/lib/jquery.ga"] = {
  exports: "jQuery",        
  deps: {
    "cdf/lib/jquery": "jQuery"
  }
};

requirejs.config(requireCfg);

define([
  'cdf/components/BaseComponent',
  'cdf/lib/jquery',
  'amd!./lib/jquery.ga'
], function(BaseComponent, $) {

  return BaseComponent.extend({

    update: function() {
      $.ga.load(this.gaTrackingId);
    }

  });

});
