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

// OLAP - Members wizard
var OlapMembersWizardEntry = PalleteWizardEntry.extend({

  id: "MDXMEBMERSWIZARD_ENTRY",
  name: "OLAP Selector wizard",
  description: "Generates a query to fetch the members of a dimension. Useful to use with selectors",
  category: "Wizards",
  categoryDesc: "Wizards",

  renderWizard: function() {

    return WizardManager.getWizardManager('OLAP_PARAMETER_WIZARD').render();

  },

  apply: function() {

    WizardManager.getWizardManager('OLAP_PARAMETER_WIZARD').apply();

  }


});
CDFDDDatasourcesArray.push(new OlapMembersWizardEntry());

var OlapChartWizardEntry = PalleteWizardEntry.extend({

  id: "OLAP_CHART_WIZARD_ENTRY",
  name: "OLAP Chart wizard",
  description: "Generates a chart.",
  category: "Wizards",
  categoryDesc: "Wizards",

  renderWizard: function() {
    return WizardManager.getWizardManager('OLAP_CHART_WIZARD').render();
  },

  apply: function() {
    WizardManager.getWizardManager('OLAP_CHART_WIZARD').apply();
  }
});
CDFDDDatasourcesArray.push(new OlapChartWizardEntry());

var SaikuOlapWizardEntry = PalleteWizardEntry.extend({

  id: "SAIKU_OLAP_WIZARD_ENTRY",
  name: "Saiku OLAP Wizard",
  description: "Use Saiku to generate an OLAP Query",
  category: "Wizards",
  categoryDesc: "Wizards",

  renderWizard: function() {
    return WizardManager.getWizardManager('SAIKU_OLAP_WIZARD').render();
  },

  apply: function() {
    WizardManager.getWizardManager('SAIKU_OLAP_WIZARD').apply();
  }
});
CDFDDDatasourcesArray.push(new SaikuOlapWizardEntry());

/*
var DatasourcesMdxMembersWizardModel = BaseModel.extend({}, {
  MODEL: 'DatasourcesMdxMembersWizardModel',
  getStub: function() {
    var _stub = {
      id: TableManager.generateGUID(),
      type: DatasourcesMdxMembersWizardModel.MODEL,
      typeDesc: "Mdx members",
      parent: IndexManager.ROOTID, properties: []
    };

    _stub.properties.push(PropertiesManager.getProperty("name"));
    _stub.properties.push(PropertiesManager.getProperty("jndi"));
    _stub.properties.push(PropertiesManager.getProperty("catalog"));
    _stub.properties.push(PropertiesManager.getProperty("cube"));
    _stub.properties.push(PropertiesManager.getProperty("query"));

    return _stub;
  }
});
BaseModel.registerModel(DatasourcesMdxMembersWizardModel);
*/
