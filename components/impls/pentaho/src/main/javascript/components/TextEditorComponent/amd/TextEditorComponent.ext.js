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


define([
  'pentaho/environment'
], function(environment) {

  /*
   *  Text editor component
   */

  return {
    getUrl: function() {
      return environment.server.root + "plugin/pentaho-cdf-dd/api/editor/getExternalEditor?";
    }
  };

});
