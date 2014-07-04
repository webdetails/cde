describe("CDF-DD-TABLEMANAGER-TESTS", function() {

  it("correctly formats paths upon file picking",function() {
    var tableManager = new TableManager("test-tableManager");
    var resourceFileRenderer = new ResourceFileRenderer(tableManager);

    var file1 = "myResource.css",
        file2 = "/resources/myResource.css",
        file3 = "/mySolution/resources/myResource.css";

    //solution dashboards
    // - new dashboard
    cdfdd.dashboardData.filename = undefined;
    expect(resourceFileRenderer.formatSelection(file1)).toBe("${solution:/myResource.css}");
    expect(resourceFileRenderer.formatSelection(file2)).toBe("${solution:/resources/myResource.css}");
    expect(resourceFileRenderer.formatSelection(file3)).toBe("${solution:/mySolution/resources/myResource.css}");

    // - editing a dashboard
    cdfdd.dashboardData.filename = "/mySolution/myDashboard.wcdf";
    expect(resourceFileRenderer.formatSelection(file1)).toBe("${solution:../myResource.css}");
    expect(resourceFileRenderer.formatSelection(file2)).toBe("${solution:../resources/myResource.css}");
    expect(resourceFileRenderer.formatSelection(file3)).toBe("${solution:resources/myResource.css}");

    //system dashboards
    // - new dashboard
    // this case is not applicable because cde new dashboard is only used in solution
    // system dashboards are created and always edited from a previously known file
    cdfdd.dashboardData.filename = undefined;
    expect(resourceFileRenderer.formatSelection(file1)).toBe("${solution:/myResource.css}");
    expect(resourceFileRenderer.formatSelection(file2)).toBe("${solution:/resources/myResource.css}");
    expect(resourceFileRenderer.formatSelection(file3)).toBe("${solution:/mySolution/resources/myResource.css}");

    // - editing a dashboard
    // system resources always have the plugin folder as root
    cdfdd.dashboardData.filename = "/system/myPlugin/myDashboard.wcdf";
    expect(resourceFileRenderer.formatSelection(file1)).toBe("${system:/myResource.css}");
    expect(resourceFileRenderer.formatSelection(file2)).toBe("${system:/resources/myResource.css}");
    expect(resourceFileRenderer.formatSelection(file3)).toBe("${system:/mySolution/resources/myResource.css}");
  });

});