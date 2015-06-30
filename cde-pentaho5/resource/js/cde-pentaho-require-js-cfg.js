/*!
 * Copyright 2002 - 2015 Webdetails, a Pentaho company. All rights reserved.
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

/**
 * Configuration file for cde pentaho version 5
 * Added in [PDB-1555]: Ability to include CDE dashboards in PDB
 */
if(typeof CONTEXT_PATH !== "undefined") { // production
  requireCfg['paths']['cde'] = CONTEXT_PATH + 'content/pentaho-cdf-dd/js';
}


(function() {

  requireCfg.config = requireCfg.config || {};

  var prefix;
  if(typeof KARMA_RUN !== "undefined") { // unit tests
    // TODO: is this necessary?

  } else if(typeof CONTEXT_PATH !== "undefined") { // production
    prefix = CONTEXT_PATH;

  } else if(typeof FULL_QUALIFIED_URL != "undefined") { // embedded
    prefix = FULL_QUALIFIED_URL;

  } else { // build
    // TODO: is this necessary?
  }

  // configure the CDE endpoint to be used by the dash! loader plugin
  requireCfg.config['dash'] = {
    'endpoint': prefix + 'plugin/pentaho-cdf-dd/api/renderer/getDashboard?path='
  };

})();
