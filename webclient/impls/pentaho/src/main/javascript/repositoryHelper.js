/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



wd = wd || {};
wd.helpers = wd.helpers || {};

wd.helpers.repository = {
  getRsourceUrl: function() {
    return "res";
  },

  getBaseSolutionPluginRoot: function() {
    return "/public/";
  },

  getWidgetsLocation: function() {
    //widgets are stored in a plugin specific folder (currently it is /public/cde/widgets/)
    return "/public/cde/widgets/";
  }
};
