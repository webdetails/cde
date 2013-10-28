describe("The Select Component", function() {

  var myDashboard = _.extend({},Dashboard);

  myDashboard.setParameterValue('selectorTestParameter',1);
  myDashboard.setParameterValue('multiSelectTestParameter',[1,2,3]);

  var selectComponent = window.selectComponent = {
    htmlObject: "selectComponent",
    parameter: 'selectorTestParameter'
  };

  var multiSelectComponent = window.multiSelectComponent = {
    htmlObject: 'multiSelectComponent',
    parameter: 'multiSelectTestParameter'
  };

  var components = [selectComponent, multiSelectComponent];

  myDashboard.addComponents(components);

  it("draws the options", function() {
    myDashboard.update(selectComponent);
    expect(selectComponent).toHaveBeenCalled();
  });

  it("holds the correct value", function() {
    myDashboard.setParameterValue('selectorTestParameter',10);
    myDashboard.update(selectComponent);
    expect(selectComponent.getValue()).toEqual(10);
  });

  it("allows overriding AJAX settings", function() {
    var ajax = spyOn(jQuery,"ajax");

    var query = new Query({dataAccessId: "foo", path:"bar"});
    query.setAjaxOptions({
      type: "GET",
      async: true
    });
    query.fetchData({},function(){});
    var settings = ajax.mostRecentCall.args[0];
    expect(settings.type).toEqual("GET");
    expect(settings.async).toBeTruthy();
  });
});

