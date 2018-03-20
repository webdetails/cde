/* globals requireCfg, packageInfo, getVersionedModuleId */

var mappedModuleId = getVersionedModuleId('cde/components');

requireCfg.map = requireCfg.map || {};
requireCfg.map[mappedModuleId] = requireCfg.map[mappedModuleId] || {};
requireCfg.map['*'] = requireCfg.map['*'] || {};
requireCfg.packages = requireCfg.packages || [];

var moduleMap = requireCfg.map[mappedModuleId];

// TODO is this needed?
// requireCfg.paths['cde/resources'] = resourcesPath;
// requireCfg.paths['cde/repo'] = resourcesPath + '/public/cde';

Object.keys(moduleMap).forEach(function(key) {
  requireCfg.map['*'][key] = moduleMap[key];
});
