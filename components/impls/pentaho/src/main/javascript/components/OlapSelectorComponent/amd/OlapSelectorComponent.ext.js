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



define([
  'pentaho/environment'
], function(environment) {

  return {
    getServiceUrl: function() {
      return environment.server.root + "plugin/pentaho-cdf-dd/api/olap/";
    },

    getCubesUrl: function() {
      return "getCubes";
    },

    getCubeStructureUrl: function() {
      return "getCubeStructure";
    },

    getPaginatedLevelMembersUrl: function() {
      return "getPaginatedLevelMembers";
    },

    getLevelMembersStructureUrl: function() {
      return "getLevelMembersStructure";
    }
  };

});
