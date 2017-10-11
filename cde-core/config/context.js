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

requireCfg = {
  paths: {},
  shim: {}
};

var ENVIRONMENT_CONFIG = {
  paths: {
    "cdf": "bin/test-js/cdf/js",
    "cdf/lib": "bin/test-js/cdf/js/lib"
  }
};

var KARMA_RUN = true;

var SESSION_NAME = "dummy";
var SESSION_LOCALE = "en-US";
var CONTEXT_PATH = "/pentaho/";
