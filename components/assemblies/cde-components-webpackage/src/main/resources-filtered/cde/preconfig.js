/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/
/* globals requireCfg, getVersionedModuleId */

var mappedModuleId = getVersionedModuleId('cde/components');

requireCfg.map = requireCfg.map || {};
requireCfg.map[mappedModuleId] = requireCfg.map[mappedModuleId] || {};
requireCfg.map['*'] = requireCfg.map['*'] || {};
requireCfg.packages = requireCfg.packages || [];

// In 'cde-core-impl' CdfRunJsDashboardWriter.java assumes this path exists
// and that it points to ResourceApi's resource endpoint.
requireCfg.paths['cde/resources'] = "/cxf/cde/resources";

// TODO is this needed?
// requireCfg.paths['cde/repo'] = resourcesPath + '/public/cde';
