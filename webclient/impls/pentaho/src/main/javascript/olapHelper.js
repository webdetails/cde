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

wd.helpers.olap = {
  getServiceUrl: function() {
    return "/plugin/pentaho-cdf-dd/api/olap/";
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
