var ProtovisComponent = BaseComponent.extend({
		update : function() {
			if (this.parameters == undefined) {
				this.parameters = [];
			};

			// clear previous table
			$("#"+this.htmlObject).empty();
			var myself = this;
			Dashboards.fetchData(this.chartDefinition, this.parameters,
                            function(values) {
                                changedData = undefined;
                                if((typeof(myself.postFetch)=='function')){
                                    changedData = myself.postFetch(values);
                                };
                                if (changedData != undefined) {
                                  values = changedData;
                                };
                                myself.render(values);
                            });
		},

		render: function(values) {
			$("#" + this.htmlObject).append('<div id="'+ this.htmlObject  +'protovis"></div>');
			var vis = new pv.Panel()
				.canvas(this.htmlObject + "protovis")
				.width(this.width)
				.height(this.height);
			this.customfunction(vis,values);
			vis.root.render();
			//vis.canvas(this.htmlObject + "image");
			//if (this.caption != undefined)
		        //this.buildCaptionWrapper($("#" + this.htmlObject + "protovis"),"");
		},

		processdata: function(values) {
			this.render(values);
		}
/*
		buildCaptionWrapper: function(chart,cdfComponent){
		
			var exportFile = function(type,cd){
				var obj = $.extend({solution: "cdf",path: "components",action:"jtable.xaction",exportType: type},cd);
				Dashboards.post(webAppPath + '/content/pentaho-cdf/Export',obj);
			};
			
			var myself = this;
			var cd = myself.chartDefinition;
			var captionOptions = $.extend({
				title:{
					title: cd.title != undefined ? cd.title : "Details", 
					oclass: 'title'
				},
				chartType:{
					title: "Chart Type",
					show: function(){ return cd.chartType != 'function' && ( cd.chartType == "BarChart" ||  cd.chartType == "PieChart")},
					icon: function(){ return cd.chartType == "BarChart" ? webAppPath + '/content/pentaho-cdf/resources/style/images/pie_icon.png': webAppPath + '/content/pentaho-cdf/resources/style/images/bar_icon.png';},
					oclass: 'options', 
					callback: function(){
						cd.chartType = cd.chartType == "BarChart" ? "PieChart" : "BarChart";
						myself.update();
					}
				},
				excel: {
					title: "Excel",
					icon: webAppPath + '/content/pentaho-cdf/resources/style/images/excel_icon.png', 
					oclass: 'options', 
					callback: function(){
						exportFile("excel",cd);
					}
				},
				csv: {
					title: "CSV",
					icon: webAppPath + '/content/pentaho-cdf/resources/style/images/csv_icon.gif', 
					oclass: 'options', 
					callback: function(){
						exportFile("csv",cd);
					}
				},
				zoom: {
					title:'Zoom', 
					icon: webAppPath + '/content/pentaho-cdf/resources/style/images/magnify.png', 
					oclass: 'options', 
					callback: function(){
						Dashboards.incrementRunningCalls();
						var parameters = myself.getParameters();
						var width = 200,height = 200; var urlTemplate,parameterName = "";
						for(p in parameters){
							if(parameters[p][0] == 'width'){width += parameters[p][1]; parameters[p] = ['width',width]};
							if(parameters[p][0] == 'height'){height += parameters[p][1]; parameters[p] = ['height',height]};
							if(parameters[p][0] == 'parameterName'){parameterName = parameters[p][1]; parameters[p] = ['parameterName','parameterValue']};
							if(parameters[p][0] == 'urlTemplate'){urlTemplate = parameters[p][1]; parameters[p] = ['urlTemplate',"javascript:chartClick('" + myself.name +"','{parameterValue}');"]};
						}
						myself.zoomCallBack = function(value){eval(urlTemplate.replace("{" + parameterName + "}",value));};
						Dashboards.callPentahoAction(myself,"cdf", "components", cdfComponent, parameters,function(jXML){
							if(jXML != null){
								var openWindow = window.open(webAppPath + "/content/pentaho-cdf/js/captify/zoom.html","_blank",'width=' + (width+10) + ',height=' + (height+10));
								var maxTries = 10;
								var loadChart = function(){
									if(openWindow.loadChart != undefined)openWindow.loadChart(jXML.find("ExecuteActivityResponse:first-child").text())
									else if(maxTries> 0) {maxTries-=1;setTimeout(loadChart,500);}
								};
								loadChart();
							}
							Dashboards.decrementRunningCalls();
						});
					}
				},
				details:{
					title:'Details', 
					icon:webAppPath + '/content/pentaho-cdf/resources/style/images/table.png', 
					oclass: 'options', 
					callback: function(){
						myself.pivotDefinition = {jndi: cd.jndi, catalog:cd.catalog, query:cd.query};
						PivotLinkComponent.openPivotLink(myself);
					}
						
				}

			}, cd.caption);
				
			var captionId = myself.htmlObject + 'caption';
			var caption = $('<div id="' + captionId + '" ></div>');
			
			chart.attr("id",myself.htmlObject + 'image');
			chart.attr("rel",myself.htmlObject + "caption");
			chart.attr("class","captify");
			
			for(o in captionOptions){
				var show = captionOptions[o].show == undefined || (typeof captionOptions[o].show=='function'?captionOptions[o].show():captionOptions[o].show) ? true : false;
				
				if (this.chartDefinition.queryType != "mdx" && captionOptions[o].title == "Details") {show = false;};
				if(show){
					var icon = captionOptions[o].icon != undefined ? (typeof captionOptions[o].icon=='function'?captionOptions[o].icon():captionOptions[o].icon) : undefined;
					var op = icon != undefined ? $('<image id ="' + captionId + o + '" src = "' + icon + '"></image>') : $('<span id ="' + captionId + o + '">' + captionOptions[o].title  +'</span>');
					op.attr("class",captionOptions[o].oclass != undefined ? captionOptions[o].oclass : "options");
					op.attr("title",captionOptions[o].title);
					caption.append(op);
				}
			};
			
			$("#" + myself.htmlObject).empty();
			
			var bDetails = $('<div class="caption-details">Details</div>');
			$("#" + myself.htmlObject).append(bDetails);
			$("#" + myself.htmlObject).append(chart);
			$("#" + myself.htmlObject).append(caption);
			
			
			$('div.captify').captify($.extend({bDetails:bDetails, spanWidth: '95%',hideDelay:3000,hasButton:false,opacity:'0.5'}, cd.caption));	
			
			//Add events after captify has finished.
			bDetails.one('capityFinished',function(e,wrapper){
				var chartOffset = chart.offset();
				var bDetailsOffset = bDetails.offset();
				if(chart.length > 1){					
					bDetails.bind("mouseenter",function(){$("#" + myself.htmlObject + 'image').trigger('detailsClick',[this]);});
					bDetails.css("left",bDetails.position().left + $(chart[1]).width() - bDetails.width() - 5);
					bDetails.css("top",bDetails.position().top + $(chart[1]).height() - bDetails.height() );
					//Append map after image				
					$(chart[1]).append(chart[0]);
					
				}
				for(o in captionOptions)
					if(captionOptions[o].callback != undefined)
						$("#" + captionId + o).bind("click",captionOptions[o].callback);
				});
			
		}*/
});
	
