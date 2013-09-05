describe("The Query class", function() {

  it("calls the query callback", function() {
    var ajax = spyOn(jQuery,"ajax").andCallFake(function(options){
      options.success();
    });
    var query = new Query({dataAccessId: "foo", path:"bar"});
    var handler = {
      success: function(){}
    }
    spyOn(handler,"success");
    query.fetchData({},handler.success);
    expect(handler.success).toHaveBeenCalled();
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

