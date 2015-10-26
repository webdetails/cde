define(["cdf/dashboard/Utils"], function(Utils) {
  return {
    setSparklineOpts: function(that, forceLine) {
      that.setAddInOptions("templateType","sparkline", function(state) {
        if(forceLine) {
            return {
              type: 'line',
              height: 50,
              width: 120
            };
        } else if(state.id == "Sparkline") {
          return {
            type: 'bar',
            height: 50,
            barWidth: 8
          };
        } else {
          return {
            width: 120,
            type: 'bullet',
            height: '12',
            targetColor: '#007fff',
            performanceColor: '#cccccc',
            rangeColors: ['transparent','transparent','transparent']
          };
        }
      });
    },
    setBubbleOpts: function(that) {
      that.setAddInOptions("templateType","bubble", {
        containerSize: 60,
        color: "#FFE085"
      });
    },
    setBulletChartOpts: function(that) {
      that.setAddInOptions("templateType","bulletChart", {
        chartOpts: {
          height: 40,
          bulletSize: 16,
          bulletSpacing: 150,
          bulletMargin: 5,
          bulletRuleLabel_text: function(value) {
            return Utils.numberFormat(value, "#A");
          },
        }
      });
    },
    setLinkOpts: function(that) {
      that.setAddInOptions("templateType","hyperlink", {
        pattern: /\[(.*?)\]/g
      });
    },
    setCCCChartOpts: function(that) {
      that.setAddInOptions("templateType","cccChart", {
        type: 'PieChart',
        chartOpts: {
          width: 200,
          height: 200,
          seriesInRows: false,
          crossTabMode: false,
          timeSeries: false,
          legendVisible: false,
          valuesLabelStyle: 'inside',
          valuesOptimizeLegibility: true,
          valueFormat: function(value) {
            return Utils.numberFormat(value, "#A");
          },
          clearSelectionMode: false,
          selectable: false,
          clickable: false,
          //interactive: false,
          hoverable:  true,
          valuesFont: 'normal 9px "Open Sans"',
          slice_innerRadiusEx: '50%',
          slice_strokeStyle:   'white',
          colors: [
            '#333333', '#777777', '#FFC20F', '#FFE085',
            '#00325b', '#005CA7', '#0086F4', '#39A74A',
            '#63CA73', '#80BCA3', '#655643', '#BF4D28'
          ]
        }
      });
    }
  };
});