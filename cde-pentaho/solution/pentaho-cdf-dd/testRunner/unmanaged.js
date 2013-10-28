describe("Unmanaged Base Component", function() {
  var myDashboard = _.extend({},Dashboards);
  var basic = window.basic = {
    name: "basic",
    type: "freeform",
    testFlag: 0,
    executeAtStart: true,
    preExecution: function() {},
    customfunction: function() {
      this.testFlag = 0x1;
    },
    postExecution: function(){}
  };

  var freeformQuery = window.freeformQuery = {
    name: "freeformQuery",
    type: "freeform",
    testFlag: 0,
    executeAtStart: true,
    manageCallee: false,
    preExecution: function() {},
    customfunction: function() {
      var redraw = _.bind(this.redraw,this);
      this.triggerQuery({
        dataAccessId: "foo",
        path: "bar" 
      }, redraw);
    },
    postFetch: function(d) {return d;},
    redraw: function() {
      this.testFlag=0x2;
    },
    postExecution: function(){}
  };

  var freeformAjax = window.freeformAjax = {
    name: "freeformAjax",
    type: "freeform",
    testFlag: 0,
    executeAtStart: true,
    manageCallee: false,
    preExecution: function() {},
    customfunction: function() {
      var redraw = _.bind(this.redraw,this);
      this.triggerAjax({
        url: "foo",
        type: "json",
        method: "get",
        path: "bar" 
      }, redraw);
    },
    postFetch: function(d) {return d;},
    redraw: function() {
      this.testFlag = 0x4; 
    },
    postExecution: function(){}
  };

  var componentList = [
    window.basic,
    window.freeformQuery,
    window.freeformAjax
  ];
  myDashboard.addComponents(componentList);

  describe("Synchronous Lifecycle", function() {
    it("calls each event handler exactly once",
      function() {
        spyOn(basic,"preExecution").andCallThrough();
        spyOn(basic,"customfunction").andCallThrough();
        spyOn(basic,"postExecution").andCallThrough();

        myDashboard.update(basic);
        waits(10);
        runs(function(){
          expect(basic.preExecution.callCount).toEqual(1);
          expect(basic.postExecution.callCount).toEqual(1);
          expect(basic.customfunction.callCount).toEqual(1);
        })
      }
    );

    it("lets preExecution cancel updates",
      function() {
        spyOn(basic,"preExecution").andReturn(false);
        spyOn(basic,"customfunction").andCallThrough();
        spyOn(basic,"postExecution").andCallThrough();

        myDashboard.update(basic);
        waits(10);
        runs(function(){
          expect(basic.preExecution.callCount).toEqual(1);
          expect(basic.postExecution).not.toHaveBeenCalled();
          expect(basic.customfunction).not.toHaveBeenCalled();
        });
      }
    );
  });

  /*******************
   * Query Lifecycle *
   *******************/

  describe("Query Lifecycle", function() {
    it("calls each event handler exactly once", function() {
      spyOn(freeformQuery,"preExecution").andCallThrough();
      spyOn(freeformQuery,"customfunction").andCallThrough();
      spyOn(freeformQuery,"postExecution").andCallThrough();
      spyOn(freeformQuery,"postFetch").andCallThrough();
      spyOn(jQuery,"ajax").andCallFake(function(options){
        setTimeout(function(){
          options.success({resultset:[],metadata:[]});
        },100);
      });
      myDashboard.update(freeformQuery);
      waits(200);
      runs(function(){
        expect(freeformQuery.preExecution.callCount).toEqual(1);
        expect(freeformQuery.postExecution.callCount).toEqual(1);
        expect(freeformQuery.postFetch.callCount).toEqual(1);
        expect(freeformQuery.customfunction.callCount).toEqual(1);
      });
    });

    it("overwrites data from postFetch", function() {
      spyOn(freeformQuery,"preExecution");
      spyOn(freeformQuery,"customfunction").andCallThrough();
      spyOn(freeformQuery,"postExecution");
      spyOn(freeformQuery,"postFetch").andReturn({test:true});
      spyOn(freeformQuery,"redraw");
      spyOn(freeformQuery,"block").andCallThrough();
      spyOn(freeformQuery,"unblock").andCallThrough();
      spyOn(jQuery,"ajax").andCallFake(function(options){
        setTimeout(function(){
          options.success({resultset:[],metadata:[]});
        },100);
      });
      myDashboard.update(freeformQuery);
      myDashboard.update(freeformQuery);
      waits(200);
      runs(function(){
        var blockCount = freeformQuery.block.callCount;
        expect(blockCount).toEqual(2);
        expect(freeformQuery.preExecution.callCount).toEqual(2);
        expect(freeformQuery.customfunction.callCount).toEqual(2);
        expect(freeformQuery.postFetch.callCount).toEqual(1);
        expect(freeformQuery.redraw.callCount).toEqual(1);
        expect(freeformQuery.redraw.argsForCall[0][0].test).toBeTruthy();
        expect(freeformQuery.postExecution.callCount).toEqual(1);
        expect(freeformQuery.unblock.callCount).toEqual(blockCount);

      });
    });

       it("doesn't overwrite data if postFetch returns undefined", function() {
      spyOn(freeformQuery,"preExecution");
      spyOn(freeformQuery,"customfunction").andCallThrough();
      spyOn(freeformQuery,"postExecution");
      spyOn(freeformQuery,"postFetch").andReturn(undefined);
      spyOn(freeformQuery,"redraw");
      spyOn(freeformQuery,"block").andCallThrough();
      spyOn(freeformQuery,"unblock").andCallThrough();
      spyOn(jQuery,"ajax").andCallFake(function(options){
        setTimeout(function(){
          options.success({resultset:[],metadata:[]});
        },100);
      });
      myDashboard.update(freeformQuery);
      myDashboard.update(freeformQuery);
      waits(200);
      runs(function(){
        var blockCount = freeformQuery.block.callCount;
        expect(blockCount).toEqual(2);
        expect(freeformQuery.preExecution.callCount).toEqual(2);
        expect(freeformQuery.customfunction.callCount).toEqual(2);
        expect(freeformQuery.postFetch.callCount).toEqual(1);
        expect(freeformQuery.redraw.callCount).toEqual(1);
        expect(freeformQuery.redraw.argsForCall[0][0].resultset).not.toBeUndefined();
        expect(freeformQuery.postExecution.callCount).toEqual(1);
        expect(freeformQuery.unblock.callCount).toEqual(blockCount);

      });
    });

    it("only updates once if called concurrently", function() {
      spyOn(freeformQuery,"preExecution");
      spyOn(freeformQuery,"customfunction").andCallThrough();
      spyOn(freeformQuery,"postExecution");
      spyOn(freeformQuery,"postFetch").andCallThrough();
      spyOn(freeformQuery,"redraw");
      spyOn(freeformQuery,"block").andCallThrough();
      spyOn(freeformQuery,"unblock").andCallThrough();
      spyOn(jQuery,"ajax").andCallFake(function(options){
        setTimeout(function(){
          options.success({resultset:[],metadata:[]});
        },100);
      });
      myDashboard.update(freeformQuery);
      myDashboard.update(freeformQuery);
      waits(200);
      runs(function(){
        var blockCount = freeformQuery.block.callCount;
        expect(blockCount).toEqual(2);
        expect(freeformQuery.preExecution.callCount).toEqual(2);
        expect(freeformQuery.customfunction.callCount).toEqual(2);
        expect(freeformQuery.postFetch.callCount).toEqual(1);
        expect(freeformQuery.redraw.callCount).toEqual(1);
        expect(freeformQuery.postExecution.callCount).toEqual(1);
        expect(freeformQuery.unblock.callCount).toEqual(blockCount);

      });
    });
    it("updates multiple times when not called concurrently", function() {
      spyOn(freeformQuery,"preExecution");
      spyOn(freeformQuery,"customfunction").andCallThrough();
      spyOn(freeformQuery,"postExecution");
      spyOn(freeformQuery,"redraw");
      spyOn(jQuery,"ajax").andCallFake(function(options){
        setTimeout(function(){
          options.success({resultset:[],metadata:[]});
        },100);
      });
      runs(function(){
        myDashboard.update(freeformQuery);
      });
      waits(200);
      runs(function(){
        myDashboard.update(freeformQuery);
      });
      waits(200);
      runs(function(){
        expect(freeformQuery.preExecution.callCount).toEqual(2);
        expect(freeformQuery.customfunction.callCount).toEqual(2);
        expect(freeformQuery.redraw.callCount).toEqual(2);
        expect(freeformQuery.postExecution.callCount).toEqual(2);
      });
    });

    it("lets preExecution cancel updates", function() {
      spyOn(freeformQuery,"preExecution").andReturn(false);
      spyOn(freeformQuery,"customfunction").andCallThrough();
      spyOn(freeformQuery,"postExecution").andCallThrough();
      spyOn(jQuery,"ajax").andCallFake(function(options){
        setTimeout(function(){
          options.success({resultset:[],metadata:[]});
        },100);
      });
      myDashboard.update(freeformQuery);
      waits(200);
      runs(function(){
        expect(freeformQuery.preExecution.callCount).toEqual(1);
        expect(freeformQuery.customfunction.callCount).toEqual(1);
        expect(freeformQuery.postExecution).not.toHaveBeenCalled();
      });
    });
  });

  /******************
   * AJAX Lifecycle *
   ******************/

  describe("AJAX Lifecycle", function() {
    it("calls each event handler exactly once", function() {
      spyOn(freeformAjax,"preExecution").andCallThrough();
      spyOn(freeformAjax,"customfunction").andCallThrough();
      spyOn(freeformAjax,"postExecution").andCallThrough();
      spyOn(freeformAjax,"postFetch").andCallThrough();
      spyOn(freeformAjax,"redraw").andCallThrough();
      spyOn(freeformAjax,"block").andCallThrough();
      spyOn(freeformAjax,"unblock").andCallThrough();
      spyOn(jQuery,"ajax").andCallFake(function(options){
        setTimeout(function(){
          options.success({resultset:[],metadata:[]});
        },100);
      });
      myDashboard.update(freeformAjax);
      waits(200);
      runs(function(){
        expect(freeformAjax.preExecution.callCount).toEqual(1);
        expect(freeformAjax.postExecution.callCount).toEqual(1);
        expect(freeformAjax.postFetch.callCount).toEqual(1);
        expect(freeformAjax.customfunction.callCount).toEqual(1);
        expect(freeformAjax.block.callCount).toEqual(1);
        expect(freeformAjax.unblock.callCount).toEqual(1);
        expect(freeformAjax.redraw.callCount).toEqual(1);
      });
    });
    it("only updates once if called concurrently", function() {
      spyOn(freeformAjax,"preExecution");
      spyOn(freeformAjax,"customfunction").andCallThrough();
      spyOn(freeformAjax,"postExecution");
      spyOn(freeformAjax,"postFetch").andCallThrough();
      spyOn(freeformAjax,"redraw");
      spyOn(freeformAjax,"block").andCallThrough();
      spyOn(freeformAjax,"unblock").andCallThrough();
      spyOn(jQuery,"ajax").andCallFake(function(options){
        setTimeout(function(){
          options.success({resultset:[],metadata:[]});
        },10);
      });
      myDashboard.update(freeformAjax);
      myDashboard.update(freeformAjax);
      waits(100);
      runs(function(){
        var blockCount = freeformAjax.block.callCount;
        expect(blockCount).toEqual(2);
        expect(freeformAjax.preExecution.callCount).toEqual(2);
        expect(freeformAjax.customfunction.callCount).toEqual(2);
        expect(freeformAjax.redraw.callCount).toEqual(1);
        expect(freeformAjax.postFetch.callCount).toEqual(1);
        expect(freeformAjax.postExecution.callCount).toEqual(1);
        expect(freeformAjax.unblock.callCount).toEqual(blockCount);
      });
    });
    it("lets preExecution cancel updates", function() {
      spyOn(freeformAjax,"preExecution").andReturn(false);
      spyOn(freeformAjax,"customfunction").andCallThrough();
      spyOn(freeformAjax,"redraw");
      spyOn(freeformAjax,"postExecution");
      spyOn(jQuery,"ajax").andCallFake(function(options){
        setTimeout(function(){
          options.success({resultset:[],metadata:[]});
        },100);
      });
      myDashboard.update(freeformAjax);
      waits(200);
      runs(function(){
        expect(freeformAjax.preExecution.callCount).toEqual(1);
        expect(freeformAjax.customfunction.callCount).toEqual(1);
        expect(freeformAjax.redraw).not.toHaveBeenCalled();
        expect(freeformAjax.postExecution).not.toHaveBeenCalled();

      });
    });
  });

  it("plays nicely with postInit", function() {
    var expectedFlag = 0x7,
        testFlag = 0;
    spyOn(jQuery,"ajax").andCallFake(function(options){
      setTimeout(function(){
        options.success({resultset:[],metadata:[]});
      },100);
    });

    runs(function() {
      myDashboard.postInit = function() {
        var i; 
        for(i = 0; i < componentList.length;i++) {
          testFlag |= componentList[i].testFlag;
        }
      };
      spyOn(myDashboard, "postInit").andCallThrough();
      myDashboard.init();
    });
    waits(500);
    runs(function(){
      expect(testFlag).toEqual(expectedFlag);
      expect(myDashboard.postInit.callCount).toEqual(1);
    });
  });
});

