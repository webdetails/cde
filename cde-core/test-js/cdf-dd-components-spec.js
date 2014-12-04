describe("CDF-DD-COMPONENTS tests", function() {

  it("Template Load Test", function() {

    var cp = new ComponentsPanel;
    cp.componentsPallete = new PalleteManager(ComponentsPanel.PALLETE);

    spyOn(cp, "init").and.callFake(function() {
      alert("Components initialized");
    });
    cp.initTables();

    expect( cp.initPallete ).toBe(false);

  });

});
