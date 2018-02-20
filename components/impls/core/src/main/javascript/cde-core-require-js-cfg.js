/*!
 * Copyright 2002 - 2018 Webdetails, a Hitachi Vantara company. All rights reserved.
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

/**
 * Configuration file for cde core
 */

(function() {

  requireCfg.map = requireCfg.map || {};
  requireCfg.map['*'] = requireCfg.map['*'] || {};
  requireCfg.packages = requireCfg.packages || [];

  var isDebug = typeof document === "undefined" || document.location.href.indexOf("debug=true") > 0;

  var componentsPath;

  if(typeof KARMA_RUN !== "undefined") { // unit tests
    requireCfg.paths['cde'] = 'src/main/javascript';
    componentsPath = 'src/main/javascript/components';

  } else if(typeof CONTEXT_PATH !== "undefined") { // production

    requireCfg.paths['cde/components'] = CONTEXT_PATH + 'plugin/pentaho-cdf-dd/api/resources/resources/'
      + (isDebug ? 'components' : 'components-compressed');

    componentsPath = CONTEXT_PATH + 'plugin/pentaho-cdf-dd/api/resources/resources/' + (isDebug ? 'components' : 'components-compressed');

    requireCfg.paths['cde/repo'] = CONTEXT_PATH + 'plugin/pentaho-cdf-dd/api/resources/public/cde';

    requireCfg.paths['cde/resources'] = CONTEXT_PATH + 'plugin/pentaho-cdf-dd/api/resources';

  } else if(typeof FULL_QUALIFIED_URL !== "undefined") { // embedded

    requireCfg.paths['cde/components'] = FULL_QUALIFIED_URL + 'plugin/pentaho-cdf-dd/api/resources/resources/'
      + (isDebug ? 'components' : 'components-compressed');

    componentsPath = FULL_QUALIFIED_URL + 'plugin/pentaho-cdf-dd/api/resources/resources/' + (isDebug ? 'components' : 'components-compressed');

    requireCfg.paths['cde/repo'] = FULL_QUALIFIED_URL + 'plugin/pentaho-cdf-dd/api/resources/public/cde';

    requireCfg.paths['cde/resources'] = FULL_QUALIFIED_URL + 'plugin/pentaho-cdf-dd/api/resources';

  } else { // build
    requireCfg.paths['cde'] = 'cde';
    componentsPath = 'cde/components';
  }

  requireCfg.packages.push({
    name: 'cde/components/PopupComponent',
    location: componentsPath + '/PopupComponent/amd',
    main: 'PopupComponent'
  },
  {
    name: 'cde/components/ExportButtonComponent',
    location: componentsPath + '/ExportButtonComponent/amd',
    main: 'ExportButtonComponent'
  },
  {
    name: 'cde/components/AjaxRequestComponent',
    location: componentsPath + '/AjaxRequestComponent/amd',
    main: 'AjaxRequestComponent'
  },
  {
    name: 'cde/components/CggComponent',
    location: componentsPath + '/CggComponent/amd',
    main: 'CggComponent'
  },
  {
    name: 'cde/components/DuplicateComponent',
    location: componentsPath + '/DuplicateComponent/amd',
    main: 'DuplicateComponent'
  },
  {
    name: 'cde/components/NewSelectorComponent',
    location: componentsPath + '/NewSelectorComponent/amd',
    main: 'NewSelectorComponent'
  },
  {
    name: 'cde/components/RaphaelComponent',
    location: componentsPath + '/RaphaelComponent/amd',
    main: 'RaphaelComponent'
  },
  {
    name: 'cde/components/RelatedContentComponent',
    location: componentsPath + '/RelatedContentComponent/amd',
    main: 'RelatedContentComponent'
  },
  {
    name: 'cde/components/SiteMapComponent',
    location: componentsPath + '/SiteMapComponent/amd',
    main: 'SiteMapComponent'
  },
  {
    name: 'cde/components/ViewManagerComponent',
    location: componentsPath + '/ViewManagerComponent/amd',
    main: 'ViewManagerComponent'
  },
  {
    name: 'cde/components/GoogleAnalyticsComponent',
    location: componentsPath + '/GoogleAnalyticsComponent/amd',
    main: 'GoogleAnalyticsComponent'
  },
  {
    name: 'cde/components/DashboardComponent',
    location: componentsPath + '/DashboardComponent/amd',
    main: 'DashboardComponent'
  });

  // components that share a common package location are mapped to the appropriate subfolder
  requireCfg.map['*']['cde/components/ExportPopupComponent'] = 'cde/components/PopupComponent/ExportPopupComponent';
  requireCfg.map['*']['cde/components/CggDialComponent'] = 'cde/components/CggComponent/CggDialComponent';

})();
