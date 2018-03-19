/*!
 * Copyright 2018 Webdetails, a Hitachi Vantara company. All rights reserved.
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

var requireCfg = {
  waitSeconds: 30,
  paths: {},
  shim: {},
  map: {
    "*": {}
  },
  bundles: {},
  config: {
    "pentaho/service": {}
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
