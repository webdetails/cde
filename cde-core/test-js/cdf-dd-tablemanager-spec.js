describe("CDF-DD-TABLEMANAGER-TESTS", function() {

  it("correctly formats paths upon file picking",function() {
    var tableManager = new TableManager("test-tableManager");
    var resourceFileRenderer = new ResourceFileRenderer(tableManager);

    var file1 = "myResource.css",
        file2 = "/resources/myResource.css",
        file3 = "/mySolution/resources/myResource.css";

    //solution dashboards
    // - new dashboard
    CDFDDFileName = "";
    expect(resourceFileRenderer.formatSelection(file1)).toBe("${solution:/myResource.css}");
    expect(resourceFileRenderer.formatSelection(file2)).toBe("${solution:/resources/myResource.css}");
    expect(resourceFileRenderer.formatSelection(file3)).toBe("${solution:/mySolution/resources/myResource.css}");

    // - editing a dashboard
    CDFDDFileName = "/mySolution/myDashboard.wcdf";
    expect(resourceFileRenderer.formatSelection(file1)).toBe("${solution:../myResource.css}");
    expect(resourceFileRenderer.formatSelection(file2)).toBe("${solution:../resources/myResource.css}");
    expect(resourceFileRenderer.formatSelection(file3)).toBe("${solution:resources/myResource.css}");

    //system dashboards
    // - new dashboard
    // this case is not applicable because cde new dashboard is only used in solution
    // system dashboards are created and always edited from a previously known file
    CDFDDFileName = "";
    expect(resourceFileRenderer.formatSelection(file1)).toBe("${solution:/myResource.css}");
    expect(resourceFileRenderer.formatSelection(file2)).toBe("${solution:/resources/myResource.css}");
    expect(resourceFileRenderer.formatSelection(file3)).toBe("${solution:/mySolution/resources/myResource.css}");

    // - editing a dashboard
    // system resources always have the plugin folder as root
    CDFDDFileName = "/system/myPlugin/mySolution/myDashboard.wcdf";
    expect(resourceFileRenderer.formatSelection(file1)).toBe("${system:../myResource.css}");
    expect(resourceFileRenderer.formatSelection(file2)).toBe("${system:../resources/myResource.css}");
    expect(resourceFileRenderer.formatSelection(file3)).toBe("${system:resources/myResource.css}");
  });

  it("correctly gets fileName before opening External Editor", function() {
    var tableManager = new TableManager("test-tableManager");
    var resourceFileRenderer = new ResourceFileRenderer(tableManager);

    var file1 = "/myResource.css",
        file2 = "/resources/myResource.css",
        file3 = "/mySolution/resources/myResource.css";

    //solution dashboards
    // - new dashboard
    CDFDDFileName = "";
    expect(resourceFileRenderer.getFileName("${solution:/myResource.css}"))
        .toBe(file1);
    expect(resourceFileRenderer.getFileName("${solution:/resources/myResource.css}"))
        .toBe(file2);
    expect(resourceFileRenderer.getFileName("${solution:/mySolution/resources/myResource.css}"))
        .toBe(file3);

    // - editing a dashboard
    CDFDDFileName = "/mySolution/myDashboard.wcdf";
    expect(resourceFileRenderer.getFileName("${solution:/myResource.css}"))
        .toBe(file1);
    expect(resourceFileRenderer.getFileName("${solution:../myResource.css}"))
        .toBe(file1);
    expect(resourceFileRenderer.getFileName("${solution:../resources/myResource.css}"))
        .toBe(file2);
    expect(resourceFileRenderer.getFileName("${solution:resources/myResource.css}"))
        .toBe(file3);

    //system dashboards
    // - new dashboard
    // this case is not applicable because cde new dashboard is only used in solution
    // system dashboards are created and always edited from a previously known file
    CDFDDFileName = "";
    expect(resourceFileRenderer.getFileName("${solution:/myResource.css}"))
        .toBe(file1);
    expect(resourceFileRenderer.getFileName("${solution:/resources/myResource.css}"))
        .toBe(file2);
    expect(resourceFileRenderer.getFileName("${solution:/mySolution/resources/myResource.css}"))
        .toBe(file3);

    // - editing a dashboard
    // system resources always have the plugin folder as root
    CDFDDFileName = "/system/myPlugin/mySolution/myDashboard.wcdf";
    var systemCtx = "/system/myPlugin";
    expect(resourceFileRenderer.getFileName("${system:/myResource.css}"))
        .toBe(file1);
    expect(resourceFileRenderer.getFileName("${system:../myResource.css}"))
        .toBe(systemCtx + file1);
    expect(resourceFileRenderer.getFileName("${system:../resources/myResource.css}"))
        .toBe(systemCtx + file2)
    expect(resourceFileRenderer.getFileName("${system:resources/myResource.css}"))
        .toBe(systemCtx + file3);
  });

});