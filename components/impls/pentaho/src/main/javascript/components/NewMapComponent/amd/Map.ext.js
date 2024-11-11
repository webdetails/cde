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


define([
  'pentaho/environment'
], function(environment) {

  return {
    getMarkerImgPath: function() {
      return environment.server.root + 'api/repos/pentaho-cdf-dd/resources/components/NewMapComponent/amd/images/';
    }
  };

});
