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

define([
  './PopupComponent',
  'cdf/components/CggComponent.ext',
  'cdf/dashboard/Utils',
  'cdf/Logger',
  'cdf/lib/jquery',
  'amd!cdf/lib/jquery.fancybox',
  'css!./ExportPopupComponent'],
  function(PopupComponent, CggComponentExt, Utils, Logger, $) {

  var ExportPopupComponent = PopupComponent.extend({

    ph: undefined,
    arrow: undefined,
    content: undefined,
    cancel: undefined,
    dataComponent: undefined,
    chartComponent: undefined,
    baseSize: 200,
    scalingFactor: 1.5,

    clone: function(parameterRemap,componentRemap,htmlRemap) {
      var dataComponent = this.dataComponent,
        chartComponent = this.chartComponent;
      delete this.dataComponent;
      delete this.chartComponent;
      var that = this.base(parameterRemap,componentRemap,htmlRemap);
      if(dataComponent) {
        this.dataComponent = dataComponent;
        that.dataComponent = componentRemap[dataComponent.name] || dataComponent;
      }
      if(chartComponent) {
          this.chartComponent = chartComponent;
        var truncated = /render_(.*)/.test(chartComponent.name)
          ? chartComponent.name.match(/render_(.*)/)[1]
          : null;
        if(componentRemap[chartComponent.name]) {
          that.chartComponent = this.dashboard.getComponentByName(componentRemap[chartComponent.name]);
          that.chartExportComponent = componentRemap[chartComponent.name];
        } else if(truncated && componentRemap[truncated]) {
          that.chartComponent = this.dashboard.getComponentByName("render_" + componentRemap[truncated]);
          that.chartExportComponent = componentRemap[truncated];
        } else {
          that.chartComponent = chartComponent;
        }
          that.chartComponent = componentRemap[chartComponent.name] || chartComponent;
      }
      return that;
    },

    update: function() {
      var myself = this;

      if(myself.ph) {
        myself.ph.remove();
      }
      
      myself.chartComponent = myself.dashboard.getComponentByName("render_" + myself.chartExportComponent);
      myself.dataComponent = myself.dashboard.getComponentByName("render_" + myself.dataExportComponent);
          
      myself.ph = $('<div>');
      $("#" + myself.htmlObject).empty();
      var link = $('<div class="popupTitle">');
      link.text(myself.title || 'Export');
      link.click(function(e) {
        myself.popup(link);
        e.stopPropagation();
      });
      $("#" + myself.htmlObject).append(link);
      
      if(myself.chartComponent) {
        var realChartExportLabel = "Export Chart";
        if(myself.chartExportLabel && myself.chartExportLabel.length > 0) {
          realChartExportLabel = myself.chartExportLabel;
        }
        var chartExportElt = $('<div class="exportElement">');
        chartExportElt.text(realChartExportLabel);
        chartExportElt.click(function() {
          myself.exportChart();
        });
        chartExportElt.appendTo(myself.ph);
      }
      
      if(myself.dataComponent) {
        var realTableExportLabel = "Export Data";
        if(myself.dataExportLabel && myself.dataExportLabel.length > 0) {
          realTableExportLabel = myself.dataExportLabel;
        }
        var dataExportElt = $('<div class="exportElement">');
        dataExportElt.text(realTableExportLabel);
        dataExportElt.click(function() {
          myself.exportData();
        });
        dataExportElt.appendTo(myself.ph);
      }
      
      $(myself.contentLinks).each(function(i, elt) {
        var popupElt = $('<div class="exportElement">');
        popupElt.text(elt[0]);
        popupElt.click(elt[1]);
        popupElt.appendTo(myself.ph);
      });
      
      myself.ph.hide().appendTo($('body'));
      myself.ph.addClass('popupComponent');
      myself.ph.addClass('exportOptions');
      myself.cancel = $("<a>&nbsp;</a>");
      myself.cancel.addClass("close").click(function() {
        myself.hide();
      });
      
      myself.cancel.appendTo(myself.ph);
      myself.arrow = $("<div class='arrow'>").appendTo(myself.ph);
      
    },
    
    popup: function(target,gravity) {

      var myself = this;

      myself.base(target, gravity);
        
      var docClick = function(e) {
        var x = e.pageX;
        var y = e.pageY;
        var linkPos = $("#" + myself.htmlObject).position();

        if((x < linkPos.left || x > linkPos.left + $("#" + myself.htmlObject).width()) ||
          (y < linkPos.top || y > linkPos.top + $("#" + myself.htmlObject).height())) {

          myself.hide();            
          $(document).unbind('click', docClick);
        }
      };            
      $(document).click(docClick);
    
    },
    
    exportData: function(det) {
      var effectiveExportType = det == undefined ? this.dataExportType : det;
      
      Logger.log("Exporting to " + effectiveExportType);

      // metadata is a special parameter,
      // carries important info for dashboard operation, 
      // but has no data so isn't exported
      var parameters = this.dataComponent.parameters.slice();
      for(var i = 0; i < parameters.length; i++) {
        if(parameters[i][0] === 'metadata') {
          parameters[i] = parameters[i].slice();
          parameters[i][1] = 'false';
          break;
        }
      }

      var cd = this.dataComponent.chartDefinition || this.dataComponent.queryDefinition;
      
      var query = this.dashboard.getQuery(cd);
      
      query.exportData(effectiveExportType, parameters, {
        filename: this.dataExportAttachmentName + "." + effectiveExportType
      });
    },

    getExportChartOptions: function() {
      //4.x has fullPath and 5.0 has path, this can go away when cdf gets refactored
      var loc = (this.dashboard.context.fullPath) ?
          this.dashboard.context.fullPath.replace(/[^\/]+$/, "") :
          this.dashboard.context.path.replace(/[^\/]+$/, "");

      var options = {
        outputType: this.chartExportType,
        script: loc + this.chartExportComponent + '.js'
      };

      var parameters = this.chartComponent.parameters;

      // Get parameter values; metadata is a special parameter, carries important
      // info for dashboard operation but has no data so isn't exported
      for(var i = 0, L = parameters.length; i < L; i++) {
        var name = parameters[i][0];
        var param = parameters[i][1];
        var value = Utils.ev(this.dashboard.getParameterValue(param));

        if(value !== undefined) {
          options['param' + name] = name != 'metadata' ? value : 'false';
        }
      }

      // Check debug level and pass as parameter
      var level = Logger.debug;
      if(level > 1) {
        options['paramdebug'] = true;
        options['paramdebugLevel'] = level;
      }

      return options;
    },

    getExportChartUrl: function(options) {
      return CggComponentExt.getCggDrawUrl() + '?' + $.param(options);
    },

    exportChart: function() {
      var options = this.getExportChartOptions();

      // Get query
      Logger.log("Exporting to " + options.outputType);

      var url = this.getExportChartUrl(options);
      var myself = this;
      var masterDiv = $('<div class="exportChartMasterDiv">');
      //Style later
      var totalWidth = Math.max(800, this.chartComponent.chartDefinition.width);
      var popupButtonsDiv = $("<div class='exportChartPopupButtons'>");
      masterDiv.append(popupButtonsDiv);
     
      var titleDiv = $("<div class='exportChartTitle'>Export Options</div>");
      popupButtonsDiv.append(titleDiv);
     
      var smallButton = $("<div class='exportChartPopupButton exportChartButtonNotLast'>Small</div>");
      smallButton.click(function() {
        $('.exportChartPopupButtonClicked').each(function(i, elt) {
          $(elt).removeClass('exportChartPopupButtonClicked');
        });
        $(this).addClass('exportChartPopupButtonClicked');      
        $('#width').attr('disabled', true); 
        $('#height').attr('disabled', true); 
        
        $('#width').val(myself.baseSize);
        $('#height').val(myself.baseSize *
          (myself.chartComponent.chartDefinition.height / myself.chartComponent.chartDefinition.width));
     
      });
      popupButtonsDiv.append(smallButton);

      var mediumButton = $("<div class='exportChartPopupButton exportChartButtonNotLast exportChartButtonMiddle'>Medium</div>");
      mediumButton.click(function() {
       
        $('.exportChartPopupButtonClicked').each(function(i, elt) {
          $(elt).removeClass('exportChartPopupButtonClicked')
        });
        $(this).addClass('exportChartPopupButtonClicked'); 
      
        $('#width').attr('disabled', true); 
        $('#height').attr('disabled', true); 
        var size = myself.baseSize * myself.scalingFactor;
        $('#width').val(size);
        $('#height').val(size * (myself.chartComponent.chartDefinition.height / myself.chartComponent.chartDefinition.width));      
      
      });
     
      mediumButton.getComponentData = function() {
        return [(myself.chartComponent.chartDefinition.width), (myself.chartComponent.chartDefinition.height)];
      };
     
      popupButtonsDiv.append(mediumButton);
     
      var largeButton = $("<div class='exportChartPopupButton exportChartButtonNotLast exportChartButtonMiddle'>Large</div>");
      largeButton.click(function() {
        $('.exportChartPopupButtonClicked').each(function(i, elt) {
          $(elt).removeClass('exportChartPopupButtonClicked');
        });
        $(this).addClass('exportChartPopupButtonClicked');      
      
        $('#width').attr('disabled', true); 
        $('#height').attr('disabled', true);
        
        var size = myself.baseSize * myself.scalingFactor * myself.scalingFactor;
        $('#width').val(size);
        $('#height').val(size * (myself.chartComponent.chartDefinition.height / myself.chartComponent.chartDefinition.width));   

      });

      popupButtonsDiv.append(largeButton);
     
      var customButton = $("<div class='exportChartPopupButton exportChartButtonMiddle'>Custom</div>");
      customButton.click(function() {
        $('.exportChartPopupButtonClicked').each(function(i, elt) {
          $(elt).removeClass('exportChartPopupButtonClicked')
        });
        $(this).addClass('exportChartPopupButtonClicked'); 
        $('#width').removeAttr('disabled'); 
        $('#height').removeAttr('disabled'); 
      
        $('#width').val(myself.chartComponent.chartDefinition.width);
        $('#height').val(myself.chartComponent.chartDefinition.height);
            
      });
     
      popupButtonsDiv.append(customButton);

      var inputsWidthDiv = $("<div class='exportChartInput'>&nbsp;&nbsp;&gt;&nbsp;&nbsp;&nbsp;Width:&nbsp;<input id='width' " +
        "disabled='true' value='" + this.chartComponent.chartDefinition.width +
        "' onChange='javascript:$(\"#height\").val($(\"#width\").val() * " +
        (myself.chartComponent.chartDefinition.height/myself.chartComponent.chartDefinition.width) + ");' type='text'></div>");

      popupButtonsDiv.append(inputsWidthDiv);

      var inputsHeightDiv = $("<div class='exportChartInput'>Height:&nbsp;</span><input id='height' disabled='true' value='" +
        this.chartComponent.chartDefinition.height + "' type='text'></div>");

      popupButtonsDiv.append(inputsHeightDiv);   
      var okButton = $("<div class='exportChartPopupButton exportChartOkButton'>Export</div>");
      okButton.click(function() {
        var dimensions, size;
        
        switch($('.exportChartPopupButtonClicked').text()) {
          case "Small":
            dimensions = [myself.baseSize, myself.BaseSize *
              (myself.chartComponent.chartDefinition.height / myself.chartComponent.chartDefinition.width)];            
            break;
          case "Medium":
            size = myself.baseSize * myself.scalingFactor;
            dimensions = [
              size,
              size * (myself.chartComponent.chartDefinition.height / myself.chartComponent.chartDefinition.width)];            
            break;
          case "Large":
            size = myself.baseSize * myself.scalingFactor * myself.scalingFactor;
            dimensions = [
              size,
              size * (myself.chartComponent.chartDefinition.height / myself.chartComponent.chartDefinition.width)];            
            break;        
          case "Custom":
          default:
            dimensions = [$('#width').val(), $('#height').val()];
            break;
        }
      
        var _exportIframe = $('<iframe style="display:none">');
        _exportIframe.detach();
        _exportIframe[0].src = url + "&attachmentName=" +
          myself.dataExportAttachmentName + "." + myself.chartExportType +
          "&paramwidth=" + dimensions[0] + '&paramheight=' + dimensions[1];
        _exportIframe.appendTo($('body'));     
      
      });
      popupButtonsDiv.append(okButton);   
      
      var img = $(
        "<img src='" + url +
        "&paramwidth=" + this.chartComponent.chartDefinition.width +
        "&paramheight=" + this.chartComponent.chartDefinition.height +
        "'/>");
     
      var imgDiv = $("<div class='exportChartImageDiv'>");
      imgDiv.append(img);
      imgDiv.append("&nbsp;");
      masterDiv.append(imgDiv);
      var holderDiv = $('<div class="exportChartMasterDivHolder">');
      holderDiv.append(masterDiv);

      $.fancybox({
        type: "html",
        closeBtn: true,
        content: holderDiv,
        width: totalWidth,
        height: this.chartComponent.chartDefinition.height + 60,
        autoDimensions: false
      });

    }
    
  });

  return ExportPopupComponent;

});
