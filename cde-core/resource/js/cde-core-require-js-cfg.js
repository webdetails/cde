/*!
 * Copyright 2002 - 2014 Webdetails, a Pentaho company.  All rights reserved.
 *
 * This software was developed by Webdetails and is provided under the terms
 * of the Mozilla Public License, Version 2.0, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to  http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */

 /**
 * Configuration file for cde core
 */

(function() {
  var requirePaths = requireCfg.paths;

  requireCfg.urlArgs = "ts=" + (new Date()).getTime();

  var prefix;
  if(typeof KARMA_RUN !== "undefined") { // test
    prefix = requirePaths['cde/components'] = 'resource/resources/custom/amd-components';

  } else if(typeof CONTEXT_PATH !== "undefined") { // production vs debug
    prefix = requirePaths['cde/components'] = CONTEXT_PATH + 'api/repos/pentaho-cdf-dd/resources/custom/amd-components';
    requirePaths['cde/repo/components'] = CONTEXT_PATH + 'plugin/pentaho-cdf-dd/api/resources/public/cde/components';

  } else if(typeof FULLY_QUALIFIED_URL != "undefined") { // embedded production vs debug
    prefix = requirePaths['cde/components'] = FULLY_QUALIFIED_URL + 'api/repos/pentaho-cdf-dd/resources/custom/amd-components';
    requirePaths['cde/repo/components'] = FULLY_QUALIFIED_URL + 'plugin/pentaho-cdf-dd/api/resources/public/cde/components';

  } else { // build
    prefix = requirePaths['cde/components'] = '../resources/custom/amd-components';
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

})();
