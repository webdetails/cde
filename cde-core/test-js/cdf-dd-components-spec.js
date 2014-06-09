describe("CDF-DD-COMPONENTS tests", function() {

  it("Template Load Test", function() {

    var cp = new ComponentsPanel;
    cp.componentsPallete = new PalleteManager(ComponentsPanel.PALLETE);

    spyOn(cp, "init").andCallFake(function() {
      alert("Components initialized");
    });
    cp.initTemplate();

    expect( cp.loadingTemplate ).toBeTruthy();

  });

});
