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
 * Configuration file for cde pentaho
 * Added in [PDB-1555]: Ability to include CDE dashboards in PDB
 */
if(typeof CONTEXT_PATH !== "undefined") { // production
  requireCfg['paths']['cde'] = CONTEXT_PATH + 'content/pentaho-cdf-dd/js';
}

(function() {

  requireCfg.config = requireCfg.config || {};
  requireCfg.packages = requireCfg.packages || [];

  var isDebug = typeof document === "undefined" || document.location.href.indexOf("debug=true") > 0;

  var basePath;
  var componentsPath;

  if(typeof KARMA_RUN !== "undefined") { // unit tests
    requireCfg.paths['cde'] = 'src/main/javascript';
    componentsPath = 'src/main/javascript/components';

  } else if(typeof CONTEXT_PATH !== "undefined") { // production

    requireCfg.paths['cde/components'] = CONTEXT_PATH + 'plugin/pentaho-cdf-dd/api/resources/resources/'
      + (isDebug ? 'components' : 'components-compressed');

    basePath = CONTEXT_PATH;
    componentsPath = basePath + 'plugin/pentaho-cdf-dd/api/resources/resources/' + (isDebug ? 'components' : 'components-compressed');

  } else if(typeof FULL_QUALIFIED_URL !== "undefined") { // embedded

    requireCfg.paths['cde/components'] = FULL_QUALIFIED_URL + 'plugin/pentaho-cdf-dd/api/resources/resources/'
      + (isDebug ? 'components' : 'components-compressed');

    basePath = FULL_QUALIFIED_URL;
    componentsPath = basePath + 'plugin/pentaho-cdf-dd/api/resources/resources/' + (isDebug ? 'components' : 'components-compressed');

  } else { // build
    componentsPath = 'cde/components';
  }

  // configure the CDE endpoint to be used by the dash! loader plugin
  requireCfg.config['dash'] = {
    'endpoint': basePath + 'plugin/pentaho-cdf-dd/api/renderer/getDashboard?path='
  };

  requireCfg.packages.push({
    name: 'cde/components/GMapsOverlayComponent',
    location: componentsPath + '/GMapsOverlayComponent/amd',
    main: 'GMapsOverlayComponent'
  },
  {
    name: 'cde/components/OlapSelectorComponent',
    location: componentsPath + '/OlapSelectorComponent/amd',
    main: 'OlapSelectorComponent'
  },
  {
    name: 'cde/components/TextEditorComponent',
    location: componentsPath + '/TextEditorComponent/amd',
    main: 'TextEditorComponent'
  });

})();
