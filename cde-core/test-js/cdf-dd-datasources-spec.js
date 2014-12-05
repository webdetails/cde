describe("CDF-DD-DATASOURCES tests", function() {

  it("Template Load Test - DataSources", function() {

    var dsp = new DatasourcesPanel;
    dsp.datasourcesPallete = new PalleteManager(DatasourcesPanel.PALLETE);

    spyOn(dsp, "init").and.callFake(function() {
      //init datasources
    });

    dsp.initTables();

    expect( dsp.initPallete ).toBe(false);

  });

});
