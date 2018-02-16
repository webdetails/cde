/*!
 * Copyright 2002 - 2018 Webdetails, a Hitachi Vantara company. All rights reserved.
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

fdescribe("CDF-DD-COMPONENTS-OTHERS-TESTS", function () {
  var tableManager = new TableManager('test-tableManager');
  var dataServiceNameRenderer = new DataServiceNameRenderer();

  fdescribe("Testing DataServiceNameRenderer", function () {

    beforeEach(function () {
      dataServicNameRenderer = new DataServiceNameRenderer();
    });

    it("should correctly extract the data services names from the XML", function () {
      var xmlDoc = document.implementation.createDocument("", "", null);
      var servicesEl = xmlDoc.createElement("services");

      var name1El = xmlDoc.createElement("name");
      name1El.appendChild(document.createTextNode("ds_test_1"));
      var service1El = xmlDoc.createElement("service");
      service1El.appendChild(name1El);

      servicesEl.appendChild(service1El);

      var name2El = xmlDoc.createElement("name");
      name2El.appendChild(document.createTextNode("ds_test_2"));
      var service2El = xmlDoc.createElement("service");
      service2El.appendChild(name2El);

      servicesEl.appendChild(service2El);
      xmlDoc.appendChild(servicesEl);

      var expectedResult = {
        'ds_test_1': 'ds_test_1',
        'ds_test_2': 'ds_test_2'
      };

      dataServiceNameRenderer.parseXml(xmlDoc);
      expect(dataServiceNameRenderer.selectData).toEqual(expectedResult);
    });

  });

});
