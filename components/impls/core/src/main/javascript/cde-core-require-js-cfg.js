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

/**
 * Configuration file for cde core
 */

(function() {

  /* globals KARMA_RUN, CONTEXT_PATH, FULL_QUALIFIED_URL, */

  requireCfg.map = requireCfg.map || {};
  requireCfg.map['*'] = requireCfg.map['*'] || {};
  requireCfg.packages = requireCfg.packages || [];

  var isDebug = typeof document === "undefined" || document.location.href.indexOf("debug=true") > 0;

  var componentsPath;
  if (typeof KARMA_RUN !== "undefined") { // unit tests
    requireCfg.paths['cde'] = 'src/main/javascript';
    componentsPath = 'src/main/javascript/components';

  } else {
    var cdeResources = 'plugin/pentaho-cdf-dd/api/resources';
    var cdeRepo = cdeResources + '/public/cde';
    var cdeComponents = cdeResources + '/resources' + (isDebug ? '/components' : '/components-compressed');

    if (typeof CONTEXT_PATH !== "undefined") { // production

      componentsPath = requireCfg.paths['cde/components'] = CONTEXT_PATH + cdeComponents;

      requireCfg.paths['cde/resources'] = CONTEXT_PATH + cdeResources;
      requireCfg.paths['cde/repo'] = CONTEXT_PATH + cdeRepo;

    } else if (typeof FULL_QUALIFIED_URL !== "undefined") { // embedded

      componentsPath = requireCfg.paths['cde/components'] = FULL_QUALIFIED_URL + cdeComponents;

      requireCfg.paths['cde/resources'] = FULL_QUALIFIED_URL + cdeResources;
      requireCfg.paths['cde/repo'] = FULL_QUALIFIED_URL + cdeRepo;

    } else { // build
      requireCfg.paths['cde'] = 'cde';
      componentsPath = 'cde/components';
    }
  }

  [
    'PopupComponent',
    'ExportButtonComponent',
    'AjaxRequestComponent',
    'CggComponent',
    'DuplicateComponent',
    'NewSelectorComponent',
    'RaphaelComponent',
    'RelatedContentComponent',
    'SiteMapComponent',
    'ViewManagerComponent',
    'GoogleAnalyticsComponent',
    'DashboardComponent',
    'NewMapComponent'          // moved from Pentaho
  ].forEach(function(component) {
    requireCfg.packages.push({
      name: 'cde/components/' + component,
      location: componentsPath + '/' + component + '/amd',
      main: component
    })
  });

  // components that share a common package location are mapped to the appropriate subfolder
  requireCfg.map['*']['cde/components/ExportPopupComponent'] = 'cde/components/PopupComponent/ExportPopupComponent';
  requireCfg.map['*']['cde/components/CggDialComponent'] = 'cde/components/CggComponent/CggDialComponent';

})();
