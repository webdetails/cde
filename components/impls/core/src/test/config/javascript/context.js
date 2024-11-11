/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


var requireCfg = {
  waitSeconds: 30,
  paths: {},
  shim: {},
  map: {
    "*": {}
  },
  bundles: {},
  config: {
    "pentaho/modules": {}
  },
  packages: []
};

var KARMA_RUN = true;

// Configure require paths for CDF resources.
var ENVIRONMENT_CONFIG = {
  paths: {
    "cdf": "target/dependency/cdf/js"
  }
};
