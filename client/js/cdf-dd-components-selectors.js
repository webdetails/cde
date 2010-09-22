// Parameter
var ParameterRenderer = SelectRenderer.extend({

		getData: function(row){
			var data = Panel.getPanel(ComponentsPanel.MAIN_PANEL).getParameters(); 
			var _str = "{";
			$.each(data,function(i,val){
					_str += "'" + val.properties[0].value + "': '" + val.properties[0].value + "',"  ;
				});

			_str+=" 'selected':'" + (this.getExpression(row)) + "'}";
			return _str;
		},

		getFormattedExpression: function(row, getExpression){
			return getExpression(row);
		}
	});



// Listeners
var ListenersRenderer = SelectMultiRenderer.extend({

		getData: function(row){
			var data = Panel.getPanel(ComponentsPanel.MAIN_PANEL).getParameters(); 
			var _str = "{";
			$.each(data,function(i,val){
					_str += "'" + val.properties[0].value + "': '" + val.properties[0].value + "',"  ;
				});

			_str+=" 'selected':" + (this.getExpression(row)) + "}";
			return _str;
		},

		getFormattedExpression: function(row, getExpression){
			return getExpression(row).replace(/','/g,"', '");;
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

var DatasourceRenderer = SelectRenderer.extend({

		getData: function(row){
			var data = Panel.getPanel(DatasourcesPanel.MAIN_PANEL).getDatasources(); 
			var _str = "{";
			$.each(data,function(i,val){
					_str += "'" + val.properties[0].value + "': '" + val.properties[0].value + "',"  ;
				});

			_str+=" 'selected':'" + (this.getExpression(row)) + "'}";
			return _str;
		},

		getFormattedExpression: function(row, getExpression){
			return getExpression(row);
		}
	});

var CdaDatasourceRenderer = SelectRenderer.extend({

		getData: function(row){
			var data = Panel.getPanel(DatasourcesPanel.MAIN_PANEL).getCdaDatasources(); 
			var _str = "{";
			$.each(data,function(i,val){
					_str += "'" + val.properties[0].value + "': '" + val.properties[0].value + "',"  ;
				});

			_str+=" 'selected':'" + (this.getExpression(row)) + "'}";
			return _str;
		},

		getFormattedExpression: function(row, getExpression){
			return getExpression(row);
		}
	});


var HtmlObjectRenderer = SelectRenderer.extend({

		getData: function(row){
			var data = Panel.getPanel(LayoutPanel.MAIN_PANEL).getHtmlObjects(); 
			var _str = "{";
			$.each(data,function(i,val){
					_str += "'" + val.properties[0].value + "': '" + val.properties[0].value + "',"  ;
				});

			_str+=" 'selected':'" + (this.getExpression(row)) + "'}";
			return _str;
		},

		getFormattedExpression: function(row, getExpression){
			return getExpression(row);
		}
	});
	
var MatchTypeRenderer = SelectRenderer.extend({

		translationHash: {
			'fromStart':'From Start',
			'all':'All'
		},

		getData: function(row){
			return " {'fromStart':'From Start','all':'All','selected':'" + (this.getExpression(row)) + "'}";
		},

		getFormattedExpression: function(row, getExpression){
			return this.translationHash[getExpression(row)];
		}
});
