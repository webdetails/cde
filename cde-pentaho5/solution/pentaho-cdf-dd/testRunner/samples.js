HelloBaseComponent = BaseComponent.extend({
  update: function() {
    $("#" + this.htmlObject).text("Hello World!");
  }
});



HelloUnmanagedComponent = UnmanagedComponent.extend({
  update: function() {
    var render = _.bind(this.render, this);
    this.synchronous(render);
  },

  render: function() {
    $("#" + this.htmlObject).text("Hello World!");
  }
});



HelloQueryBaseComponent = BaseComponent.extend({
  update: function() {
    var myself = this;
    var query = new Query(myself.queryDefinition);
    query.fetchData(myself.parameters, function(values) {
      var changedValues = undefined;
        if((typeof(myself.postFetch)=='function')){
          changedValues = myself.postFetch(values);                
        }
        if (changedValues !== undefined) {
          values = changedValues;
        }
        myself.render(values);
     });
  },

  render: function(data) {
    $("#" + this.htmlObject).text(JSON.stringify(data));
  }
});



HelloQueryUnmanagedComponent = UnmanagedComponent.extend({
  update: function() {
    var render = _.bind(this.render,this);
    this.triggerQuery(this.queryDefinition, render);
  },

  render: function(data) {
    $("#" + this.htmlObject).text(JSON.stringify(data));
  }
});

describe("Unmanaged Component Samples", function() {
  var myDashboard = _.extend({},Dashboards);


  var mhello = window.mhello = {
    name: "mhello",
    type: "HelloBase",
    htmlObject: 'mhello',
    executeAtStart: true,
  };
  var uhello = window.uhello = {
    name: "uhello",
    type: "HelloUnmanaged",
    htmlObject: 'uhello',
    executeAtStart: true,
  };

  var mquery = window.mquery = {
    name: "mquery",
    type: "HelloQueryBase",
    htmlObject: 'mquery',
    executeAtStart: true,
    queryDefinition: {
      path: "",
      dataAccessId: ""
    }
  }

  var uquery = window.uquery = {
    name: "uquery",
    type: "HelloQueryUnmanaged",
    htmlObject: 'uquery',
    executeAtStart: true,
    queryDefinition: {
      path: "",
      dataAccessId: ""
    }
  }

  var componentList = [
    window.mhello,
    window.uhello,
    window.mquery,
    window.uquery
  ];
  myDashboard.addComponents(componentList);
  it("updates components",function(){
    spyOn(jQuery,"ajax").andCallFake(function(options){
      setTimeout(function(){
        options.success({resultset:[[123,"abc"]],metadata:[]});
      },100);
    });
    myDashboard.init();
    waits(1000);
    runs(function(){
    expect($("#mhello").text()).toEqual("Hello World!");
    expect($("#uhello").text()).toEqual("Hello World!");
    });
  });
});

