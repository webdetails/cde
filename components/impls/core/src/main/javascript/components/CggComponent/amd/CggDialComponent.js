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



define(['./CggComponent'], function(CggComponent) {

  return CggComponent.extend({
    script: "system/pentaho-cdf-dd/resources/components/CggComponent/amd/charts/dial.js",
    priority: 6,
    
    getScriptUrl: function() {
      return this.script;
    },

    _processParametersCore: function(data) {
      data.paramvalue  = this.dashboard.getParameterValue(this.parameter);
      data.paramcolors = this.colors;
      data.paramscale  = this.intervals;
    }
  });

});
