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
