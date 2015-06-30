/*!
 * Copyright 2002 - 2015 Webdetails, a Pentaho company. All rights reserved.
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
