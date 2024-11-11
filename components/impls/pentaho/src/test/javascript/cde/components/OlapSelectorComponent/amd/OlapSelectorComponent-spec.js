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


define([
  'cdf/Dashboard.Clean',
  'cde/components/OlapSelectorComponent',
  'cdf/lib/jquery'
], function(Dashboard, OlapSelectorComponent, $) {

  /**
   * ## The Olap Selector Component
   */
  describe("The Olap Selector Component #", function() {
    var dashboard = new Dashboard();

    dashboard.init();

    dashboard.addParameter("param1", "");

    var olapSelectorComponent = new OlapSelectorComponent({
      type: "OlapSelectorComponent",
      name: "olapSelectorComponent",
      title: "Olap Selector",
      parameter: "param1",
      executeAtStart: true,
      htmlObject: "sampleObjectOlapSelect",
      parameters: [],
      catalog: "mondrian:/SteelWheels",
      cube: "SteelWheelsSales",
      dimensionName: "Customers",
      multiSelect: true,
      listeners: []
    });

    dashboard.addComponent(olapSelectorComponent);

    /**
     * ## The Olap Selector Component # allows a dashboard to execute update
     */
    it("allows a dashboard to execute update", function(done) {
      spyOn(olapSelectorComponent, 'update').and.callThrough();
      spyOn($, "ajax").and.callFake(function(params) {
        params.success({
          "result": {
            "catalogs": [{"cubes": [{"id": "Quadrant Analysis","name": "Quadrant Analysis"}], "jndi": "SampleData", "name": "SampleData", "schema": "mondrian:/SampleData"},{"cubes": [{"id": "SteelWheelsSales","name": "SteelWheelsSales"}], "jndi": "SampleData", "name": "SteelWheels", "schema": "mondrian:/SteelWheels"}],
            "dimensions": [{"caption":"Markets","hierarchies":[{"caption":"Markets","defaultMember":"All Markets","defaultMemberQualifiedName":"[Markets].[All Markets]","levels":[{"caption":"Territory","depth":1,"name":"Territory","qualifiedName":"[Markets].[Territory]","type":"level"},{"caption":"Country","depth":2,"name":"Country","qualifiedName":"[Markets].[Country]","type":"level"},{"caption":"State Province","depth":3,"name":"State Province","qualifiedName":"[Markets].[State Province]","type":"level"},{"caption":"City","depth":4,"name":"City","qualifiedName":"[Markets].[City]","type":"level"}],"name":"Markets","qualifiedName":"[Markets]","type":"hierarchy"}],"name":"Markets","type":"StandardDimension"},{"caption":"Customers","hierarchies":[{"caption":"Customers","defaultMember":"All Customers","defaultMemberQualifiedName":"[Customers].[All Customers]","levels":[{"caption":"Customer","depth":1,"name":"Customer","qualifiedName":"[Customers].[Customer]","type":"level"}],"name":"Customers","qualifiedName":"[Customers]","type":"hierarchy"}],"name":"Customers","type":"StandardDimension"},{"caption":"Product","hierarchies":[{"caption":"Product","defaultMember":"All Products","defaultMemberQualifiedName":"[Product].[All Products]","levels":[{"caption":"Line","depth":1,"name":"Line","qualifiedName":"[Product].[Line]","type":"level"},{"caption":"Vendor","depth":2,"name":"Vendor","qualifiedName":"[Product].[Vendor]","type":"level"},{"caption":"Product","depth":3,"name":"Product","qualifiedName":"[Product].[Product]","type":"level"}],"name":"Product","qualifiedName":"[Product]","type":"hierarchy"}],"name":"Product","type":"StandardDimension"},{"caption":"Time","hierarchies":[{"caption":"Time","defaultMember":"All Years","defaultMemberQualifiedName":"[Time].[All Years]","levels":[{"caption":"Years","depth":1,"name":"Years","qualifiedName":"[Time].[Years]","type":"level"},{"caption":"Quarters","depth":2,"name":"Quarters","qualifiedName":"[Time].[Quarters]","type":"level"},{"caption":"Months","depth":3,"name":"Months","qualifiedName":"[Time].[Months]","type":"level"}],"name":"Time","qualifiedName":"[Time]","type":"hierarchy"}],"name":"Time","type":"TimeDimension"},{"caption":"Order Status","hierarchies":[{"caption":"Order Status","defaultMember":"All Status Types","defaultMemberQualifiedName":"[Order Status].[All Status Types]","levels":[{"caption":"Type","depth":1,"name":"Type","qualifiedName":"[Order Status].[Type]","type":"level"}],"name":"Order Status","qualifiedName":"[Order Status]","type":"hierarchy"}],"name":"Order Status","type":"StandardDimension"}],"measures":[{"caption":"Quantity","memberType":"MEASURE","name":"Quantity","qualifiedName":"[Measures].[Quantity]","type":"measure"},{"caption":"Sales","memberType":"MEASURE","name":"Sales","qualifiedName":"[Measures].[Sales]","type":"measure"},{"caption":"Fact Count","memberType":"MEASURE","name":"Fact Count","qualifiedName":"[Measures].[Fact Count]","type":"measure"}],
            "members": [{"caption":"Alpha Cognac","memberType":"REGULAR","name":"Alpha Cognac","qualifiedName":"[Customers].[Alpha Cognac]","type":"member"},{"caption":"American Souvenirs Inc","memberType":"REGULAR","name":"American Souvenirs Inc","qualifiedName":"[Customers].[American Souvenirs Inc]","type":"member"},{"caption":"Amica Models & Co.","memberType":"REGULAR","name":"Amica Models & Co.","qualifiedName":"[Customers].[Amica Models & Co.]","type":"member"},{"caption":"ANG Resellers","memberType":"REGULAR","name":"ANG Resellers","qualifiedName":"[Customers].[ANG Resellers]","type":"member"},{"caption":"Anna's Decorations, Ltd","memberType":"REGULAR","name":"Anna's Decorations, Ltd","qualifiedName":"[Customers].[Anna's Decorations, Ltd]","type":"member"},{"caption":"Anton Designs, Ltd.","memberType":"REGULAR","name":"Anton Designs, Ltd.","qualifiedName":"[Customers].[Anton Designs, Ltd.]","type":"member"},{"caption":"Asian Shopping Network, Co","memberType":"REGULAR","name":"Asian Shopping Network, Co","qualifiedName":"[Customers].[Asian Shopping Network, Co]","type":"member"},{"caption":"Asian Treasures, Inc.","memberType":"REGULAR","name":"Asian Treasures, Inc.","qualifiedName":"[Customers].[Asian Treasures, Inc.]","type":"member"},{"caption":"Atelier graphique","memberType":"REGULAR","name":"Atelier graphique","qualifiedName":"[Customers].[Atelier graphique]","type":"member"},{"caption":"Australian Collectables, Ltd","memberType":"REGULAR","name":"Australian Collectables, Ltd","qualifiedName":"[Customers].[Australian Collectables, Ltd]","type":"member"},{"caption":"Australian Collectors, Co.","memberType":"REGULAR","name":"Australian Collectors, Co.","qualifiedName":"[Customers].[Australian Collectors, Co.]","type":"member"},{"caption":"Australian Gift Network, Co","memberType":"REGULAR","name":"Australian Gift Network, Co","qualifiedName":"[Customers].[Australian Gift Network, Co]","type":"member"},{"caption":"Auto Associés & Cie.","memberType":"REGULAR","name":"Auto Associés & Cie.","qualifiedName":"[Customers].[Auto Associés & Cie.]","type":"member"},{"caption":"Auto Canal+ Petit","memberType":"REGULAR","name":"Auto Canal+ Petit","qualifiedName":"[Customers].[Auto Canal+ Petit]","type":"member"},{"caption":"Auto-Moto Classics Inc.","memberType":"REGULAR","name":"Auto-Moto Classics Inc.","qualifiedName":"[Customers].[Auto-Moto Classics Inc.]","type":"member"},{"caption":"AV Stores, Co.","memberType":"REGULAR","name":"AV Stores, Co.","qualifiedName":"[Customers].[AV Stores, Co.]","type":"member"},{"caption":"Baane Mini Imports","memberType":"REGULAR","name":"Baane Mini Imports","qualifiedName":"[Customers].[Baane Mini Imports]","type":"member"},{"caption":"Bavarian Collectables Imports, Co.","memberType":"REGULAR","name":"Bavarian Collectables Imports, Co.","qualifiedName":"[Customers].[Bavarian Collectables Imports, Co.]","type":"member"},{"caption":"BG&E Collectables","memberType":"REGULAR","name":"BG&E Collectables","qualifiedName":"[Customers].[BG&E Collectables]","type":"member"},{"caption":"Blauer See Auto, Co.","memberType":"REGULAR","name":"Blauer See Auto, Co.","qualifiedName":"[Customers].[Blauer See Auto, Co.]","type":"member"},{"caption":"Boards & Toys Co.","memberType":"REGULAR","name":"Boards & Toys Co.","qualifiedName":"[Customers].[Boards & Toys Co.]","type":"member"},{"caption":"CAF Imports","memberType":"REGULAR","name":"CAF Imports","qualifiedName":"[Customers].[CAF Imports]","type":"member"},{"caption":"Cambridge Collectables Co.","memberType":"REGULAR","name":"Cambridge Collectables Co.","qualifiedName":"[Customers].[Cambridge Collectables Co.]","type":"member"},{"caption":"Canadian Gift Exchange Network","memberType":"REGULAR","name":"Canadian Gift Exchange Network","qualifiedName":"[Customers].[Canadian Gift Exchange Network]","type":"member"},{"caption":"Classic Gift Ideas, Inc","memberType":"REGULAR","name":"Classic Gift Ideas, Inc","qualifiedName":"[Customers].[Classic Gift Ideas, Inc]","type":"member"},{"caption":"Classic Legends Inc.","memberType":"REGULAR","name":"Classic Legends Inc.","qualifiedName":"[Customers].[Classic Legends Inc.]","type":"member"},{"caption":"Clover Collections, Co.","memberType":"REGULAR","name":"Clover Collections, Co.","qualifiedName":"[Customers].[Clover Collections, Co.]","type":"member"},{"caption":"Collectable Mini Designs Co.","memberType":"REGULAR","name":"Collectable Mini Designs Co.","qualifiedName":"[Customers].[Collectable Mini Designs Co.]","type":"member"},{"caption":"Collectables For Less Inc.","memberType":"REGULAR","name":"Collectables For Less Inc.","qualifiedName":"[Customers].[Collectables For Less Inc.]","type":"member"},{"caption":"Corporate Gift Ideas Co.","memberType":"REGULAR","name":"Corporate Gift Ideas Co.","qualifiedName":"[Customers].[Corporate Gift Ideas Co.]","type":"member"},{"caption":"Corrida Auto Replicas, Ltd","memberType":"REGULAR","name":"Corrida Auto Replicas, Ltd","qualifiedName":"[Customers].[Corrida Auto Replicas, Ltd]","type":"member"},{"caption":"Cramer Spezialitäten, Ltd","memberType":"REGULAR","name":"Cramer Spezialitäten, Ltd","qualifiedName":"[Customers].[Cramer Spezialitäten, Ltd]","type":"member"},{"caption":"Cruz & Sons Co.","memberType":"REGULAR","name":"Cruz & Sons Co.","qualifiedName":"[Customers].[Cruz & Sons Co.]","type":"member"},{"caption":"Daedalus Designs Imports","memberType":"REGULAR","name":"Daedalus Designs Imports","qualifiedName":"[Customers].[Daedalus Designs Imports]","type":"member"},{"caption":"Danish Wholesale Imports","memberType":"REGULAR","name":"Danish Wholesale Imports","qualifiedName":"[Customers].[Danish Wholesale Imports]","type":"member"},{"caption":"Der Hund Imports","memberType":"REGULAR","name":"Der Hund Imports","qualifiedName":"[Customers].[Der Hund Imports]","type":"member"},{"caption":"Diecast Classics Inc.","memberType":"REGULAR","name":"Diecast Classics Inc.","qualifiedName":"[Customers].[Diecast Classics Inc.]","type":"member"},{"caption":"Diecast Collectables","memberType":"REGULAR","name":"Diecast Collectables","qualifiedName":"[Customers].[Diecast Collectables]","type":"member"},{"caption":"Double Decker Gift Stores, Ltd","memberType":"REGULAR","name":"Double Decker Gift Stores, Ltd","qualifiedName":"[Customers].[Double Decker Gift Stores, Ltd]","type":"member"},{"caption":"Down Under Souveniers, Inc","memberType":"REGULAR","name":"Down Under Souveniers, Inc","qualifiedName":"[Customers].[Down Under Souveniers, Inc]","type":"member"},{"caption":"Dragon Souveniers, Ltd.","memberType":"REGULAR","name":"Dragon Souveniers, Ltd.","qualifiedName":"[Customers].[Dragon Souveniers, Ltd.]","type":"member"},{"caption":"Enaco Distributors","memberType":"REGULAR","name":"Enaco Distributors","qualifiedName":"[Customers].[Enaco Distributors]","type":"member"}],
            "more": true
          },
          "status": "true"
        });
      });

      // listen to cdf:postExecution event
      olapSelectorComponent.once("cdf:postExecution", function() {
        expect(olapSelectorComponent.update).toHaveBeenCalled();
        done();
      });

      dashboard.update(olapSelectorComponent);
    });
  });
});
