/*!
 * Copyright 2002 - 2015 Webdetails, a Pentaho company. All rights reserved.
 *
 * This software was developed by Webdetails and is provided under the terms
 * of the Mozilla Public License, Version 2.0, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */

// JFreeChart
var ChartTypeRenderer = SelectRenderer.extend({

  selectData: {
    'BarChart':  'Bar Chart',
    'PieChart':  'Pie Chart',
    'LineChart': 'Line Chart',
    'AreaChart': 'Area Chart'
  }
});

var DomainPeriodTypeRenderer = SelectRenderer.extend({

  selectData: {
    'Year':        'Year',
    'Quarter':     'Quarter',
    'Month':       'Month',
    'Week':        'Week',
    'Day':         'Day',
    'Hour':        'Hour',
    'Minute':      'Minute',
    'Second':      'Second',
    'Millisecond': 'Millisecond'
  }
});

var OrientationRenderer = SelectRenderer.extend({

  selectData: {
    'vertical':   'Vertical',
    'horizontal': 'Horizontal'
  }
});

var DomainLabelRotationRenderer = SelectRenderer.extend({

  selectData: {
    'up':   'Clockwise',
    'down': 'CounterClockWise'
  }
});

var DatasetTypeRenderer = SelectRenderer.extend({

  selectData: {
    'CategoryDataset':      'Category Dataset',
    'TimeSeriesCollection': 'TimeSeries Collection'
  }
});

var TopCountAxisRenderer = SelectRenderer.extend({

  selectData: {
    'rows':     'Rows',
    'columns ': 'Columns'
  }
});

var UrlTemplateRenderer = CellRenderer.extend({


	constructor: function(){
		this.base();
		this.logger = new Logger("StringRenderer");
		this.logger.debug("Creating new StringRenderer");
	},
	
	render: function(placeholder, value, callback){
		//this.value = value;
		
		var _editArea = $('<td><div style="float:left"><code></code></div><div class="edit" style="float:right"></div></td>');
		this.editArea = _editArea;
		_editArea.find("code").text(value);
		
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
							html:'Choose Click Behaviour:&nbsp;&nbsp;<select id="clickBehaviour">\n' +
'					<option value="linkToAnotherDashboard">Link to Another Dashboard</option>\n' +
'					<option value="fireChange">Fire Change</option>\n' +
'					<option value="jumpToUrl">Jump to Url</option></select>',
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
											{root: '/',script: SolutionTreeRequests.getExplorerFolderEndpoint(CDFDDDataUrl)+ "?fileExtensions=.wcdf&access=execute",expandSpeed: 1000, collapseSpeed: 1000, multiFolder: false,folderClick: 
											function(obj,folder){if($(".selectedFolder").length > 0)$(".selectedFolder").attr("class","");$(obj).attr("class","selectedFolder");}}, 
											function(file) {selectedFile = file;$(".selectedFile").attr("class","");$("a[rel='" + file + "']").attr("class","selectedFile");});
									}
										
								}
										return false;
							}
				},
				linkToAnotherDashboard: {
					html:'Link to Another Dashaboard<hr/>\n' +
'						Choose Dashboard:\n' +
'						<div id="container_id" class="urltargetfolderexplorer">&nbsp;</div>\n' +
'						<span class="linkToAnotherDashboardlabel">Store parameters at var:&nbsp;</span><input class="linkToAnotherDashboardinput1" type="text" id="parameterName" value=""/><br/>\n' +
'						<span class="linkToAnotherDashboardlabel">Store series at var:&nbsp;</span><input class="linkToAnotherDashboardinput2" type="text" id="seriesName" value="" />',
					buttons: { Back: -1, Ok:1, Cancel: 0 },
					focus: 1,
					submit:function(v,m,f){
						if(v == 0) return true;
						if(v == 1 && selectedFile.length == 0)
							$.prompt("Please select a dashboard file!",{prefix:"popup"}); 
						else if(v==-1)
							$.prompt.goToState('chooseClickBehaviour');
						else{
							if(v == 1){
								myself.callback(callback, 'linkToAnotherDashboard',selectedFile,$("#parameterName").val(),$("#seriesName").val());
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
							myself.callback(callback, 'fireChange',$("#fireChange_parameters_container").val());
							$.prompt.close();
						}
										return false;	
					}
					},
				jumpToUrl: {
								html:'Jump to Url:<hr/>\n' +
'					<span>Url:&nbsp;&nbsp;</span><input class="utlTemplateTargetInput" type="text" id="urlTarget" value="' + value + '"/>',
								buttons: { Back: -1, Ok:1, Cancel: 0 },
								focus: 1,
								 submit:function(v,m,f){
						if(v == 0) return true;
						if(v==-1)
							$.prompt.goToState('chooseClickBehaviour');
						else if(v == 1){
							myself.callback(callback, 'jumpToUrl',$("#urlTarget").val());
							$.prompt.close();
						}
										return false;	
					}
					},
				skip: {
								html:'Url Template:<hr/>\n' +
'					<textarea class="urlTemplateInput" type="text" id="urlTemplate" >' + value + '</textarea>',
								buttons: { Back: -1, Ok:1, Cancel: 0 },
								focus: 1,
								 submit:function(v,m,f){
						if(v == 0) return true;
						if(v==-1)
							$.prompt.goToState('chooseClickBehaviour');
						else if(v == 1){
							myself.callback(callback, 'jumpToUrl',$("#urlTarget").val());
							$.prompt.close();
							myself.callback(callback, 'skip',$("#urlTemplate").val());
						}
										return false;	
					}
				}
			};

			$.prompt(urlTemplateWizard,{prefix:"popup"}); 
		
		}).appendTo($("div.edit",_editArea));

		_editArea.appendTo(placeholder);
	},
		
		callback :function(callback, clickBehaviour,param1,param2,param3){
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
				}
				if(param3.length  > 0){
					value+="&param" + param3 + "={SERIE}";
				}
			}
			else if(clickBehaviour == "fireChange"){
				value = "javascript:Dashboards.fireChange('" + param1 + "','{PARAM}')";
			}
			else {
				value = param1;
			}
			
			this.value = value;
			this.editArea.find("code").text(value);
			callback(value);
		},

		validate: function(settings, original){
			return true;
		}

	});
