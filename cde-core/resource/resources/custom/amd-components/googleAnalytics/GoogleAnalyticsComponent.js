/*!
 * Copyright 2002 - 2015 Webdetails, a Pentaho company.  All rights reserved.
 *
 * This software was developed by Webdetails and is provided under the terms
 * of the Mozilla Public License, Version 2.0, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to  http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */

/* Developed by Sinn Tecnologia -  rodrigo@sinn.com.br */

/*
 * Configure RequireJS amd! loader plugin to load jquery.ga.js which is not
 * compatible with AMD.
 *
 */
require.config({
  config: {
    "amd": {
      shim: {
        "cde/components/googleAnalytics/lib/jquery.ga": {
          exports: "$",        
          deps: {
            "jQuery": "cdf/lib/jquery"
          }
        }
      }
    }
  }
});

define([
  'cdf/components/BaseComponent',
  'amd!./googleAnalytics/lib/jquery.ga'],
  function(BaseComponent, $) {

  var GoogleAnalyticsComponent = BaseComponent.extend({

    update: function() {
      $.globalEval('$(document).ready( function() { $.ga.load("' + this.gaTrackingId + '"); } );');
    }

  });

  return GoogleAnalyticsComponent;

});
