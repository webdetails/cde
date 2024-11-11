/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

describe("CDF-DD-COMPONENTS-OTHERS-TESTS", function () {
  webAppPath = "/pentaho";
  var tableManager = new TableManager('test-tableManager');
  var dataServiceNameRenderer = new DataServiceNameRenderer();
  var streamingDataServiceNameRenderer = new StreamingDataServiceNameRenderer();

  describe("Testing DataServiceNameRenderer", function () {
    var xmlDoc,
        servicesEl,
        service1El,
        service2El;

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

    it("should correctly extract data services names from the XML", function () {
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
        'ds_test_1': 'ds_test_1',
        'ds_test_2': 'ds_test_2'
      };

      dataServiceNameRenderer.parseXml(xmlDoc);
      expect(dataServiceNameRenderer.selectData).toEqual(expectedResult);
    });

  });

});
