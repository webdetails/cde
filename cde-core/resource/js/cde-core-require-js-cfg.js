/*!
 * Copyright 2002 - 2017 Webdetails, a Hitachi Vantara company. All rights reserved.
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

  var isDebug = typeof document == "undefined" || document.location.href.indexOf("debug=true") > 0;

  if(typeof KARMA_RUN !== "undefined") { // unit tests
    requireCfg.paths['cde/components'] = 'resource/resources/custom/amd-components';

  } else if(typeof CONTEXT_PATH !== "undefined") { // production
    requireCfg.paths['cde/components'] = CONTEXT_PATH + 'plugin/pentaho-cdf-dd/api/resources/resources/custom/'
      + (isDebug ? 'amd-components' : 'amd-components-compressed');
    requireCfg.paths['cde/repo/components'] = CONTEXT_PATH + 'plugin/pentaho-cdf-dd/api/resources/public/cde/components';

    requireCfg.paths['cde/resources'] = CONTEXT_PATH + 'plugin/pentaho-cdf-dd/api/resources';

  } else if(typeof FULL_QUALIFIED_URL != "undefined") { // embedded
    requireCfg.paths['cde/components'] = FULL_QUALIFIED_URL + 'plugin/pentaho-cdf-dd/api/resources/resources/custom'
      + (isDebug ? 'amd-components' : 'amd-components-compressed');
    requireCfg.paths['cde/repo/components'] = FULL_QUALIFIED_URL + 'plugin/pentaho-cdf-dd/api/resources/public/cde/components';

    requireCfg.paths['cde/resources'] = FULL_QUALIFIED_URL + 'plugin/pentaho-cdf-dd/api/resources';

  } else { // build
    requireCfg.paths['cde/components'] = 'amd-components';
  }

  requireCfg.map['*']['cde/components/PopupComponent'] = 'cde/components/popup/PopupComponent';
  requireCfg.map['*']['cde/components/ExportPopupComponent'] = 'cde/components/popup/ExportPopupComponent';

  requireCfg.map['*']['cde/components/NewMapComponent'] = 'cde/components/Map/Map';

  requireCfg.map['*']['cde/components/ExportButtonComponent'] = 'cde/components/exportButton/ExportButtonComponent';

  requireCfg.map['*']['cde/components/AjaxRequestComponent'] = 'cde/components/AjaxRequest/AjaxRequestComponent';

  requireCfg.map['*']['cde/components/CggComponent'] = 'cde/components/cgg/CggComponent';
  requireCfg.map['*']['cde/components/CggDialComponent'] = 'cde/components/cgg/CggDialComponent';

  requireCfg.map['*']['cde/components/DuplicateComponent'] = 'cde/components/Duplicate/DuplicateComponent';

  requireCfg.map['*']['cde/components/NewSelectorComponent'] = 'cde/components/NewSelector/NewSelectorComponent';

  requireCfg.map['*']['cde/components/OlapSelectorComponent'] = 'cde/components/OlapSelector/OlapSelectorComponent';

  requireCfg.map['*']['cde/components/RaphaelComponent'] = 'cde/components/Raphael/RaphaelComponent';

  requireCfg.map['*']['cde/components/RelatedContentComponent'] = 'cde/components/RelatedContent/RelatedContentComponent';

  requireCfg.map['*']['cde/components/SiteMapComponent'] = 'cde/components/SiteMap/SiteMapComponent';

  requireCfg.map['*']['cde/components/TextEditorComponent'] = 'cde/components/TextEditor/TextEditorComponent';

  requireCfg.map['*']['cde/components/GMapsOverlayComponent'] = 'cde/components/gmapsoverlay/GMapsOverlayComponent';

  requireCfg.map['*']['cde/components/ViewManagerComponent'] = 'cde/components/ViewManager/ViewManagerComponent';
  
  requireCfg.map['*']['cde/components/GoogleAnalyticsComponent'] = 'cde/components/googleAnalytics/GoogleAnalyticsComponent';

  requireCfg.map['*']['cde/components/DashboardComponent'] = 'cde/components/Dashboard/DashboardComponent';

})();
