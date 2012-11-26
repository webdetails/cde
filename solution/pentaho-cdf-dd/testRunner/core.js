describe("The CDF framework", function() {
  var myDashboard = _.extend({},Dashboards);
  
  /*
   * Our setup consists of adding a bunch of componets to CDF.
   */
  myDashboard.init();
  var shouldUpdate = window.shouldUpdate = {
    name: "shouldUpdate",
    type: "managedFreeform",
    preExecution: function() {},
    customfunction: function() {},
    postExecution: function(){}
  };
  var shouldNotUpdate = window.shouldNotUpdate = {
    name: "shouldNotUpdate",
    type: "managedFreeform",
    preExecution: function() {return false;},
    customfunction: function() {},
    postExecution: function(){}
  };

  myDashboard.addComponents([window.shouldUpdate, shouldNotUpdate]);
 
  /*
   * Test Core Lifecycle
   */ 
  it("updates Components",function() {
    
    spyOn(shouldUpdate,"preExecution").andCallThrough();
    spyOn(shouldUpdate,"customfunction").andCallThrough();
    spyOn(shouldUpdate,"postExecution").andCallThrough();

    myDashboard.update(shouldUpdate);
    waits(100);
    runs(function() {
      expect(shouldUpdate.preExecution).toHaveBeenCalled();
      expect(shouldUpdate.postExecution).toHaveBeenCalled();
      expect(shouldUpdate.customfunction).toHaveBeenCalled();
    })
  });

  it("lets preExecution cancel updates",function() {
    
    spyOn(shouldNotUpdate,"preExecution").andCallThrough();
    spyOn(shouldNotUpdate,"customfunction").andCallThrough();
    spyOn(shouldNotUpdate,"postExecution").andCallThrough();

    myDashboard.update(shouldNotUpdate);
    waits(100);
    runs(function(){
      expect(shouldNotUpdate.preExecution).toHaveBeenCalled();
      expect(shouldNotUpdate.postExecution).not.toHaveBeenCalled();
      expect(shouldNotUpdate.customfunction).not.toHaveBeenCalled();
    });
  });

  /*
   * Test Parameter setting and syncing
   */
  it("sets parameters", function() {
    myDashboard.setParameter("parentParam",1);
    expect(myDashboard.getParameterValue("parentParam")).toEqual(1);
    myDashboard.setParameter("parentParam",2);
    expect(myDashboard.getParameterValue("parentParam")).toEqual(2);
  });

  it("syncs parameters", function() {
    myDashboard.setParameter("parentParam",1);
    myDashboard.setParameter("childParam",0);
    /* Test initial syncing */
    myDashboard.syncParameters("parentParam","childParam");
    expect(myDashboard.getParameterValue("childParam")).toEqual(1);
    /* Test change propagation */
    myDashboard.fireChange("parentParam",2);
    expect(myDashboard.getParameterValue("childParam")).toEqual(2);
  });

 it("triggers postInit when all components have finished rendering", function() {
    myDashboard.postInit = function() {};
    spyOn(myDashboard, "postInit");
    spy = myDashboard.postInit;
    myDashboard.init();
    waits(500);
    runs(function(){
      expect(spy).toHaveBeenCalled();
    });
  });

});


