var CccComponent = BaseComponent.extend({

  query: null,
  chart: null,

  update : function() {
    if (this.parameters == undefined) {
      this.parameters = [];
    }

    // clear previous table
    var ph = $("#"+this.htmlObject).empty();
    var myself = this;
    
    // Set up defaults for height and width
    if(typeof(this.chartDefinition.width) === "undefined")
      this.chartDefinition.width = ph.width();

    if(typeof(this.chartDefinition.height) === "undefined")
      this.chartDefinition.height = ph.height();
  
    if (Modernizr.svg) {
      this.renderChart();
    } else {
      pv.listenForPageLoad(function() {myself.renderChart();});
    }
  },

  renderChart: function() {
      var myself = this;
      if(this.chartDefinition.dataAccessId || myself.chartDefinition.query){
      
        this.query = new Query(this.chartDefinition);

        this.query.fetchData(this.parameters, function(values) {
          var changedValues = undefined;
          if((typeof(myself.postFetch)=='function')){
            changedValues = myself.postFetch(values);
            $("#" + this.htmlObject).append('<div id="'+ myself.htmlObject  +'protovis"></div>');
          }
          if (changedValues != undefined) {
            values = changedValues;
          }
          myself.render(values);
        });

      }
      else{
        // initialize the component only
        this.render()
      }
  },

  render: function(values) {

    $("#" + this.htmlObject).append('<div id="'+ this.htmlObject  +'protovis"></div>');

    var o = $.extend({},this.chartDefinition);
    o.canvas = this.htmlObject+'protovis';
    // Extension points
    if(typeof o.extensionPoints != "undefined"){
      var ep = {};
      o.extensionPoints.forEach(function(a){
        ep[a[0]]=a[1];
      });
      o.extensionPoints=ep;
    }
    this.chart =  new this.cccType(o);
    if(arguments.length > 0){
      this.chart.setData(values,{
        crosstabMode: this.crosstabMode,
        seriesInRows: this.seriesInRows
      });
    }
    this.chart.render();
  }

});

var CccDotChartComponent = CccComponent.extend({

  cccType: pvc.DotChart

});

var CccLineChartComponent = CccComponent.extend({

  cccType: pvc.LineChart

});

var CccStackedLineChartComponent = CccComponent.extend({

  cccType: pvc.StackedLineChart

});

var CccStackedAreaChartComponent = CccComponent.extend({

  cccType: pvc.StackedAreaChart

});

var CccBarChartComponent = CccComponent.extend({

  cccType: pvc.BarChart

});

var CccPieChartComponent = CccComponent.extend({

  cccType: pvc.PieChart

});

var CccHeatGridChartComponent = CccComponent.extend({

  cccType: pvc.HeatGridChart

});

var CccBulletChartComponent = CccComponent.extend({

  cccType: pvc.BulletChart

  });

var CccWaterfallChartComponent = CccComponent.extend({

  cccType: pvc.WaterfallChart

});


var CccMetricDotChartComponent = CccComponent.extend({

  cccType: pvc.MetricDotChart

});

var CccMetricLineChartComponent = CccComponent.extend({

  cccType: pvc.MetricLineChart

});
