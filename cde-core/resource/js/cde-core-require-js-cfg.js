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

/**
 * Configuration file for cde core
 */

(function() {

  var requirePaths = requireCfg.paths;

  var isDebug = typeof document == "undefined" || document.location.href.indexOf("debug=true") > 0;

  var prefix;
  if(typeof KARMA_RUN !== "undefined") { // unit tests
    prefix = requirePaths['cde/components'] = 'resource/resources/custom/amd-components';

  } else if(typeof CONTEXT_PATH !== "undefined") { // production
    prefix = requirePaths['cde/components'] = CONTEXT_PATH + 'plugin/pentaho-cdf-dd/api/resources/resources/custom/'
      + (isDebug ? 'amd-components' : 'amd-components-compressed');
    requirePaths['cde/repo/components'] = CONTEXT_PATH + 'plugin/pentaho-cdf-dd/api/resources/public/cde/components';

    requirePaths['cde/resources'] = CONTEXT_PATH + 'plugin/pentaho-cdf-dd/api/resources';

  } else if(typeof FULL_QUALIFIED_URL != "undefined") { // embedded
    prefix = requirePaths['cde/components'] = FULL_QUALIFIED_URL + 'plugin/pentaho-cdf-dd/api/resources/resources/custom'
      + (isDebug ? 'amd-components' : 'amd-components-compressed');
    requirePaths['cde/repo/components'] = FULL_QUALIFIED_URL + 'plugin/pentaho-cdf-dd/api/resources/public/cde/components';

    requirePaths['cde/resources'] = FULL_QUALIFIED_URL + 'plugin/pentaho-cdf-dd/api/resources';

  } else { // build
    prefix = requirePaths['cde/components'] = 'amd-components';
  }

  requirePaths['cde/components/PopupComponent'] = prefix + '/popup/PopupComponent';
  requirePaths['cde/components/ExportPopupComponent'] = prefix + '/popup/ExportPopupComponent';

  requirePaths['cde/components/NewMapComponent'] = prefix + '/NewMapComponent/NewMapComponent';
  requirePaths['cde/components/NewMapComponentExt'] = prefix + '/NewMapComponent/NewMapComponent.ext';
  requirePaths['cde/components/mapAddIns'] = prefix + '/NewMapComponent/addIns/mapAddIns';
  requirePaths['cde/components/addIns'] = prefix + '/NewMapComponent/addIns';
  requirePaths['cde/components/MapComponentAsyncLoader'] = prefix + '/NewMapComponent/MapComponentAsyncLoader';
  requirePaths['cde/components/MapEngine'] = prefix + '/NewMapComponent/mapengine';
  requirePaths['cde/components/GoogleMapEngine'] = prefix + '/NewMapComponent/mapengine-google';
  requirePaths['cde/components/OpenLayersEngine'] = prefix + '/NewMapComponent/mapengine-openlayers';

  requirePaths['cde/components/ExportButtonComponent'] = prefix + '/exportButton/ExportButtonComponent';

  requirePaths['cde/components/AjaxRequestComponent'] = prefix + '/AjaxRequestComponent/AjaxRequestComponent';

  requirePaths['cde/components/CggComponent'] = prefix + '/cgg/CggComponent';
  requirePaths['cde/components/CggDialComponent'] = prefix + '/cgg/CggDialComponent';

  requirePaths['cde/components/DuplicateComponent'] = prefix + '/Duplicate/DuplicateComponent';

  requirePaths['cde/components/NewSelectorComponent'] = prefix + '/NewSelector/NewSelectorComponent';

  requirePaths['cde/components/OlapSelectorComponent'] = prefix + '/OlapSelector/OlapSelectorComponent';
  requirePaths['cde/components/OlapSelectorComponentExt'] = prefix + '/OlapSelector/OlapSelectorComponent.ext';

  requirePaths['cde/components/RaphaelComponent'] = prefix + '/Raphael/RaphaelComponent';

  requirePaths['cde/components/RelatedContentComponent'] = prefix + '/RelatedContent/RelatedContentComponent';

  requirePaths['cde/components/SiteMapComponent'] = prefix + '/SiteMap/SiteMapComponent';

  requirePaths['cde/components/TextEditorComponent'] = prefix + '/TextEditor/TextEditorComponent';
  requirePaths['cde/components/TextEditorComponentExt'] = prefix + '/TextEditor/TextEditorComponent.ext';

  requirePaths['cde/components/GMapsOverlayComponent'] = prefix + '/gmapsoverlay/GMapsOverlayComponent';
  requirePaths['cde/components/GMapsOverlayComponentExt'] = prefix + '/gmapsoverlay/GMapsOverlayComponent.ext';
  requirePaths['cde/components/GMapEngine'] = prefix + '/gmapsoverlay/GMapEngine';
  requirePaths['cde/components/GMapComponentAsyncLoader'] = prefix + '/gmapsoverlay/GMapComponentAsyncLoader';

  requirePaths['cde/components/ViewManagerComponent'] = prefix + '/ViewManager/ViewManagerComponent';
  requirePaths['cde/components/ViewManagerComponentExt'] = prefix + '/ViewManager/ViewManagerComponent.ext';
  
  requirePaths['cde/components/GoogleAnalyticsComponent'] = prefix + '/googleAnalytics/GoogleAnalyticsComponent';

  requirePaths['cde/components/DashboardComponent'] = prefix + '/DashboardComponent/DashboardComponent';
  requirePaths['cde/components/DashboardComponentExt'] = prefix + '/DashboardComponent/DashboardComponent.ext';

})();
