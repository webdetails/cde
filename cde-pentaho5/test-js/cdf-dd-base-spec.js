describe("CDF-DD-BASE tests", function() {
  beforeEach(function() {
    LoadRequests = {
      loadDashboard: function () {}
    }
    cdfdd.layout = {init: function() {}};
    cdfdd.components = {initTemplate: function() {}};
    cdfdd.datasources = {initTemplate: function() {}};

    cdfdd.dashboardData = {
      layout: { rows: ["layout_test"]},
      components: { rows: ["comp_test"]},
      datasources: { rows: ["data_test"]}
    };

  });

  it("test load template when the template is empty",function() {

    SynchronizeRequests.selectTemplate = {
        structure: {
            layout: { rows: ["layout_temp"]},
            components: { rows: []},
            datasources: { rows: []}
        }
    };

    SynchronizeRequests.callbackLoadTemplate(true);

    expect( cdfdd.dashboardData.layout.rows[0] == "layout_temp" ).toBeTruthy()
    expect( cdfdd.dashboardData.components.rows[0] == "comp_test" ).toBeTruthy();
    expect( cdfdd.dashboardData.datasources.rows[0] == "data_test" ).toBeTruthy();

  });

  it("test load Template when the template is not empty", function() {

    SynchronizeRequests.selectTemplate = {
      "structure": {
        layout: { "rows": ["layout_templ"]},
        components: { rows: ["comp_templ"]},
        datasources: { rows: ["data_templ"]}
      }
    };

    SynchronizeRequests.callbackLoadTemplate(true);

    expect( cdfdd.dashboardData.layout.rows[0] == "layout_templ" ).toBeTruthy()
    expect( cdfdd.dashboardData.components.rows[0] == "comp_templ" ).toBeTruthy();
    expect( cdfdd.dashboardData.datasources.rows[0] == "data_templ" ).toBeTruthy();
  });

});
