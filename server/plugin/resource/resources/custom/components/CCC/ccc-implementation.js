var CccComponent = BaseComponent.extend({

  query: null,
  chart: null,

  update : function() {
    if (this.parameters == undefined) {
      this.parameters = [];
    };

    // clear previous table
    $("#"+this.htmlObject).empty();
    var myself = this;


    this.query = new Query(this.chartDefinition);
    pv.listenForPageLoad(function() {
      myself.query.fetchData(myself.parameters, function(values) {
        changedValues = undefined;
        if((typeof(myself.postFetch)=='function')){
          changedValues = myself.postFetch(values);
          $("#" + this.htmlObject).append('<div id="'+ this.htmlObject  +'protovis"></div>');
        }
        if (changedValues != undefined) {
          values = changedValues;
        }
        myself.render(values);
      });
    });
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
    this.chart.setData(values,{
      crosstabMode: this.crosstabMode,
      seriesInRows: this.seriesInRows
    });
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
