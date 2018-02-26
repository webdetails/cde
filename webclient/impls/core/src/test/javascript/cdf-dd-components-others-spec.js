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
describe("CDF-DD-COMPONENTS-OTHERS-TESTS", function () {
  var tableManager = new TableManager('test-tableManager');
  var dataServiceNameRenderer = new DataServiceNameRenderer();
  var streamingDataServiceNameRenderer = new StreamingDataServiceNameRenderer();

  describe("Testing DataServiceNameRenderer", function () {
    var xmlDoc,
        servicesEl,
        service1El,
        service2El


    beforeEach(function () {
      // create test xml
      xmlDoc = document.implementation.createDocument("", "", null);
      servicesEl = xmlDoc.createElement("services");

      var name1El = xmlDoc.createElement("name");
      name1El.appendChild(document.createTextNode("ds_test_1"));

      service1El = xmlDoc.createElement("service");
      service1El.appendChild(name1El);

      var name2El = xmlDoc.createElement("name");
      name2El.appendChild(document.createTextNode("ds_test_2"));

      service2El = xmlDoc.createElement("service");
      service2El.appendChild(name2El);
    });

    it("should correctly extract the non streaming data services names from the XML", function () {
      var streaming1El = xmlDoc.createElement("streaming");
      streaming1El.appendChild(document.createTextNode("Y"));
      service1El.appendChild(streaming1El);

      var streaming2El = xmlDoc.createElement("streaming");
      streaming2El.appendChild(document.createTextNode("N"));
      service2El.appendChild(streaming2El);

      servicesEl.appendChild(service1El);
      servicesEl.appendChild(service2El);
      xmlDoc.appendChild(servicesEl);

      var expectedResult = {
        'ds_test_2': 'ds_test_2'
      };

      dataServiceNameRenderer.parseXml(xmlDoc);
      expect(dataServiceNameRenderer.selectData).toEqual(expectedResult);
    });

    it("should correctly extract the streaming data services names from the XML", function () {
      var streaming1El = xmlDoc.createElement("streaming");
      streaming1El.appendChild(document.createTextNode("Y"));
      service1El.appendChild(streaming1El);

      var streaming2El = xmlDoc.createElement("streaming");
      streaming2El.appendChild(document.createTextNode("N"));
      service2El.appendChild(streaming2El);

      servicesEl.appendChild(service1El);
      servicesEl.appendChild(service2El);
      xmlDoc.appendChild(servicesEl);

      var expectedResult = {
        'ds_test_1': 'ds_test_1'
      };

      streamingDataServiceNameRenderer.parseXml(xmlDoc);
      expect(streamingDataServiceNameRenderer.selectData).toEqual(expectedResult);
    });

  });

});
