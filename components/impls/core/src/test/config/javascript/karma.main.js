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

// Find and inject tests using require
(function() {
  var karma = window.__karma__;

  var tests = [];
  for(var file in karma.files) {
    if((/test.*\-spec\.js$/).test(file)) {
      tests.push(file);
    }
  }

  requireCfg['baseUrl'] = '/base';
  requirejs.config(requireCfg);

  console.log = function() {};
  console.info = function() {};
  console.debug = function() {};
  console.warn = function() {};

  // Ask Require.js to load all test files and start test run
  require(tests, karma.start);
})();
