// Parameter
var ParameterRenderer = SelectRenderer.extend({

  getData: function(){

    return Panel.getPanel(ComponentsPanel.MAIN_PANEL).getParameters().map(function(o){
      return o.properties[0].value;
    });
  }
});


// Listeners
var ListenersRenderer = SelectMultiRenderer.extend({

  getData: function(){
    var data = Panel.getPanel(ComponentsPanel.MAIN_PANEL).getParameters();
    var _str = "{";
    $.each(data,function(i,val){
      _str += "'" + val.properties[0].value + "': '" + val.properties[0].value + "',"  ;
    });

    _str+=" 'selected':" + (this.value) + "}";
    return _str;
  },

  getFormattedValue: function(value){
  	var v = value.replace(/','/g,"', '");
	if(v.length > 20 ) v = v.substring(0,20) + " (...)";
    return v;
  }
});

var ArrayParameterRenderer = ListenersRenderer.extend({});
	
// dataSource
var DataSourceProperty = BasePropertyType.extend({
  type: "dataSource",
  stub: {
    name: "dataSource",
    description: "Datasource",
    tooltip: "DataSource to be used in this selector",
    type: "Datasource",
    value: "",
    order: 43
  }
});
PropertiesManager.register(new DataSourceProperty());

// dataSource
var JFreeChartDataSourceProperty = DataSourceProperty.extend({
  type: "jFreeChartDataSource",
  stub: {
    name: "jFreeChartDataSource",
    description: "Datasource",
    tooltip: "DataSource to be used in this selector",
    type: "Datasource",
    value: "",
    order: 43
  }
});
PropertiesManager.register(new JFreeChartDataSourceProperty());

var DatasourceRenderer = SelectRenderer.extend({

  getData: function(){

    return Panel.getPanel(DatasourcesPanel.MAIN_PANEL).getDatasources().map(function(o){
      return o.properties[0].value;
    });
  }

});



var HtmlObjectRenderer = SelectRenderer.extend({

  getData: function(){

    return Panel.getPanel(LayoutPanel.MAIN_PANEL).getHtmlObjects().map(function(o){
      return o.properties[0].value;
    });

  }


});
	
var MatchTypeRenderer = SelectRenderer.extend({

	isAutoComplete: false,

  selectData: {
    'fromStart':'From Start',
    'all':'All'
  }
});
