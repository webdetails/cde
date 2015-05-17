
define([
  './base/Filter-base-implementation',
  './strategies/SingleSelect',
  './strategies/MultiSelect',
  './models/SelectionTree',
  './controllers/Manager',
  './data-handlers/InputDataHandler',
  './data-handlers/OutputDataHandler',
  './views/Root',
  './views/Item',
  './views/Group',
  './extensions/renderers',
  './extensions/sorters',
  './addIns/addIns'],
  function( BaseFilter ) {

    return BaseFilter;
});