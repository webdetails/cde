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
