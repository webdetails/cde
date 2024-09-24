SelectComponent = SelectComponent.extend({

  update: function() {
    this.base();
    cdfmobile.refreshSelector(this);
  }
});


/* Setup default values for CCC extension points
 *
 * We need some sensible defaults for properties that are
 * only accessible via extension points. To do this, we
 * extract the CCC components from the dashboard (they can be
 * identified through their type, which always starts with
 * 'ccc'), and add the defaults to the extension point object
 * found, if any (or create a new one if none is available)
 */
$(window).bind('cdfAboutToLoad',function(){
  chartDetector = /^ccc/;
  var charts = Dashboards.components.filter(function(component){
    return chartDetector.test(component.type);
  });
  var defaults = {
    yAxisLabel_textStyle: 'white',
    xAxisLabel_textStyle: 'white',
    legendLabel_textStyle: 'white'
  }
  for (c in charts) if (charts.hasOwnProperty(c)) {
    var chart = charts[c].chartDefinition;
    var exts = Dashboards.propertiesArrayToObject(chart.extensionPoints || {});
    exts = $.extend({}, defaults, exts);    
    chart.extensionPoints = Dashboards.objectToPropertiesArray(exts);
  }
});
