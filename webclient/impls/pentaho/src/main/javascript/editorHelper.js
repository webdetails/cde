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

wd.helpers.editor = {
  getUrl: function() {
    return "/plugin/pentaho-cdf-dd/api/editor/getExternalEditor?";
  },
  getDashboardParametersUrl: function() {
  	return webAppPath + "/plugin/pentaho-cdf-dd/api/renderer/getDashboardParameters?path="
  },
  getDashboardDataSourcesUrl: function() {
  	return webAppPath + "/plugin/pentaho-cdf-dd/api/renderer/getDashboardDatasources?path="
  }
};
