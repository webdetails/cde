// JFreeChart


var ChartTypeRenderer = SelectRenderer.extend({

		selectData: {
			'BarChart':'Bar Chart',
			'PieChart':'Pie Chart', 
			'LineChart':'Line Chart',
			'AreaChart':'Area Chart'
		}
});





var DomainPeriodTypeRenderer = SelectRenderer.extend({

		selectData: {
			'Year':'Year',
			'Quarter':'Quarter', 
			'Month':'Month',
			'Week':'Week',
			'Day':'Day',
			'Hour':'Hour',
			'Minute':'Minute',
			'Second':'Second',
			'Millisecond':'Millisecond'
		}
});


var OrientationRenderer = SelectRenderer.extend({

		selectData: {
			'vertical':'Vertical',
			'horizontal':'Horizontal'
		}
		
});



var DomainLabelRotationRenderer = SelectRenderer.extend({

		selectData: {
			'up':'Clockwise',
			'down':'CounterClockWise'
		}
});






var DatasetTypeRenderer = SelectRenderer.extend({

		selectData: {
			'CategoryDataset':'Category Dataset',
			'TimeSeriesCollection':'TimeSeries Collection'
		}
});


var TopCountAxisRenderer = SelectRenderer.extend({

		selectData: {
			'rows':'Rows',
			'columns ':'Columns '
		}
});


var UrlTemplateRenderer = CellRenderer.extend({

		constructor: function(){
			this.base();
			this.logger = new Logger("StringRenderer");
			this.logger.debug("Creating new StringRenderer");
		},

		render: function(row,placeholder, getExpression,setExpression,editable, getRow){
			
			if(editable){
				
					this.row = row;
					this.getExpression = getExpression;
					this.setExpression = setExpression;
					this.placeholder = placeholder;
					this.getRow = getRow;
					
					var _editArea = $('<td><div style="float:left"><code></code></div><div class="edit" style="float:right"></div></td>');
					this.editArea = _editArea;

					_editArea.find("code").text(getExpression(row));
					
					var myself=this;
					var selectedFile = "";
					
					var data = Panel.getPanel(ComponentsPanel.MAIN_PANEL).getParameters(); 
					var strData = "{";
					var chooseParameterSelector = '<select id="fireChange_parameters_container">';
					$.each(data,function(i,val){
						var parameter = val.properties[0].value;
						strData += "'" + parameter + "': '" + parameter + "',"  ;
						chooseParameterSelector+= '<option value="' + parameter + '">' + parameter + '</option>';
					});
					strData+=" 'selected':"+ "''" + "}";
					chooseParameterSelector+= '</select>';

					var _prompt = $('<button class="cdfddInput">...</button>').bind("click",function(){
					
						var urlTemplateWizard = {
						      chooseClickBehaviour: {
						            html:'Choose Click Behaviour:&nbsp;&nbsp;<select id="clickBehaviour">\
										<option value="linkToAnotherDashboard">Link to Another Dashboard</option>\
										<option value="fireChange">Fire Change</option>\
										<option value="jumpToUrl">Jump to Url</option></select>',
						            buttons: { Cancel: 0, Next: 1, Skip: 2 },
						            focus: 1,
						            submit:function(v,m,f){
						                  if(v == 0) return true;
										  if(v == 2) $.prompt.goToState('skip');
						                  else{
											var clickBehaviour = $("#clickBehaviour").val();
						                        $.prompt.goToState(clickBehaviour);
												if(clickBehaviour == "linkToAnotherDashboard"){
													selectedFile = "";
													$('#container_id').fileTree(
														{root: '/',script: CDFDDDataUrl.replace("Syncronize","ExploreFolder?fileExtensions=.wcdf"),expandSpeed: 1000, collapseSpeed: 1000, multiFolder: false,folderClick: 
														function(obj,folder){if($(".selectedFolder").length > 0)$(".selectedFolder").attr("class","");$(obj).attr("class","selectedFolder");}}, 
														function(file) {selectedFile = file;$(".selectedFile").attr("class","");$("a[rel='" + file + "']").attr("class","selectedFile");});
												}
													
										  }
						                  return false;
						            }
						      },
						      linkToAnotherDashboard: {
						            html:'Link to Another Dashaboard<hr/>\
									Choose Dashboard:\
									<div id="container_id" class="urltargetfolderexplorer">&nbsp;</div>\
									<span class="linkToAnotherDashboardlabel">Store parameters at var:&nbsp;</span><input class="linkToAnotherDashboardinput1" type="text" id="parameterName" value=""/><br/>\
									<span class="linkToAnotherDashboardlabel">Store series at var:&nbsp;</span><input class="linkToAnotherDashboardinput2" type="text" id="seriesName" value="" />',
						            buttons: { Back: -1, Ok:1, Cancel: 0 },
						            focus: 1,
						            submit:function(v,m,f){
										if(v == 0) return true;
										if(v == 1 && selectedFile.length == 0)
											$.prompt("Please select a dashboard file!"); 
										else if(v==-1)
											$.prompt.goToState('chooseClickBehaviour');
										else{
											if(v == 1){
												myself.callback('linkToAnotherDashboard',selectedFile,$("#parameterName").val(),$("#seriesName").val());
											}
											$.prompt.close();
										}
						                return false;
						            }
						      },
							  fireChange: {
						            html:'Fire Change<hr/>Choose Parameter:&nbsp;&nbsp;' + chooseParameterSelector,
						            buttons: { Back: -1, Ok:1, Cancel: 0 },
						            focus: 1,
						            submit:function(v,m,f){
										if(v == 0) return true;
										if(v==-1)
											$.prompt.goToState('chooseClickBehaviour');
										else if(v == 1){
											myself.callback('fireChange',$("#fireChange_parameters_container").val());
											$.prompt.close();
										}
						                return false;	
									}
						      },
							  jumpToUrl: {
						            html:'Jump to Url:<hr/>\
									<span>Url:&nbsp;&nbsp;</span><input class="utlTemplateTargetInput" type="text" id="urlTarget" value="' + getExpression(row) + '"/>',
						            buttons: { Back: -1, Ok:1, Cancel: 0 },
						            focus: 1,
						             submit:function(v,m,f){
										if(v == 0) return true;
										if(v==-1)
											$.prompt.goToState('chooseClickBehaviour');
										else if(v == 1){
											myself.callback('jumpToUrl',$("#urlTarget").val());
											$.prompt.close();
										}
						                return false;	
									}
						      },
							  skip: {
						            html:'Url Template:<hr/>\
									<textarea class="urlTemplateInput" type="text" id="urlTemplate" >' + getExpression(row) + '</textarea>',
						            buttons: { Back: -1, Ok:1, Cancel: 0 },
						            focus: 1,
						             submit:function(v,m,f){
										if(v == 0) return true;
										if(v==-1)
											$.prompt.goToState('chooseClickBehaviour');
										else if(v == 1){
											myself.callback('jumpToUrl',$("#urlTarget").val());
											$.prompt.close();
											myself.callback('skip',$("#urlTemplate").val());
										}
						                return false;	
									}
						      }
						};

						$.prompt(urlTemplateWizard); 
					
					}).appendTo($("div.edit",_editArea));

					_editArea.appendTo(placeholder);
				}
			else{
				$("<td>"+  getExpression(row) +"</td>").appendTo(placeholder);
			}
		},
		
		callback :function(clickBehaviour,param1,param2,param3){
			var value = "";
			
			if(clickBehaviour == "linkToAnotherDashboard"){
				var solutionPath = param1.split("/");
				var solution = solutionPath[1];
				var file = solutionPath[solutionPath.length-1];
				var path = "/";
				if(solutionPath.length >3)
					path += solutionPath.slice(2,solutionPath.length-1).join("/")
				value = CDFDDServerUrl + "Render?solution=" + solution + "&path=" + path + "&file=" + file;
				if(param2.length  > 0){
					value+="&param" + param2 + "={PARAM}";
					//this.setExpression(this.getRow("parameterName"),"PARAM");
				}
				if(param3.length  > 0){
					value+="&param" + param3 + "={SERIE}";
					//this.setExpression(this.getRow("seriesName"),"SERIE");
				}
			}
			else if(clickBehaviour == "fireChange"){
				value = "javascript:Dashboards.fireChange('" + param1 + "','{PARAM}')";
				//this.setExpression(this.getRow("parameterName"),"PARAM");
			}
			else 
				value = param1;
			
			
			this.setExpression(this.row,value);
			this.editArea.find("code").text(value);
		},

		validate: function(settings, original){
			return true;
		}

	});
