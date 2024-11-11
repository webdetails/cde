/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


define([
  'cdf/components/BaseComponent',
  'cdf/Logger',
  'cdf/lib/jquery',
  'cdf/dashboard/Utils'
], function(BaseComponent, Logger, $, Utils) {

  return BaseComponent.extend({
    visible: false,
    update: function() {
      this.executeRequest(this);
    },
    
    parseXML: function(sText) {
      if(!sText) {
        return null;
      }
      var xmlDoc;
      try { //Firefox, Mozilla, Opera, etc.
        return (new DOMParser()).parseFromString(sText, "text/xml");
      } catch(e) {
        try { //Internet Explorer
          xmlDoc = new ActiveXObject("Microsoft.XMLDOM");
          xmlDoc.async = "false";
          xmlDoc.loadXML(sText);
          return xmlDoc;
        } catch(e) {
        }
      }
      Logger.error('XML is invalid or no XML parser found');
      return null;
    },
    
    executeRequest: function(object) {
      var myself = this;
      var url = object.url;
      var ajaxRequestType = object.ajaxRequestType;
      var params = object.parameters;
      var asyncCall = object.asyncCall;
      
      if(url == undefined) {
        Logger.error("Fatal - No url passed");
        return;
      }
      if(ajaxRequestType == undefined) {
        ajaxRequestType = "json";
      }
      if(params != undefined) {
        var containsTime = false;
        for(var i = 0; i < params.length; i++) {
          var value;
          if(params[i][0] == 'time') {
            value = new Date().getTime();
            containsTime = true;
          } else {
            value = myself.dashboard.getParameterValue(params[i][0]);
          }
          params[i][1] = value;
        }
        containsTime ? params.push(['time', new Date().getTime()]) : 0;
        params = Utils.propertiesArrayToObject(params);

      } else {
        params = {};
      }
      
      if(asyncCall == undefined) {
        asyncCall = true;
      }
      
      $.ajax({
        url: url,
        type: "GET",
        dataType: ajaxRequestType,
        async: asyncCall,
        data: params,
        complete: function(XMLHttpRequest, textStatus) {
          var values = XMLHttpRequest.responseText;
          var changedValues = undefined;
          
          if(values == undefined) {
            Logger.error("Found error: Empty Data");
            return;
          }

          if(this.dataType == "xml" || this.dataType == "html") {
            values = myself.parseXML(values);
            if(values == null) {
              return;
            }

            var nodeList = values.getElementsByTagName('return');
            if(nodeList.length > 0 && nodeList[0].firstChild) {
              values = nodeList[0].firstChild.nodeValue;
            } else {
              return;
            }
            
            values = $.parseJSON(values);
          } else if(this.dataType == "json") {
            values = $.parseJSON(values);
          } else if(this.dataType != "script" && this.dataType != "text") {
            Logger.error("Found error: Unknown returned format");
            return;
          }
          
          if(typeof(object.postFetch) == 'function') {
            changedValues = object.postFetch(values);
          }
          if(changedValues != undefined) {
            values = changedValues;
          }
    
          if(object.resultvar != undefined) {
            myself.dashboard.fireChange(object.resultvar, values);
          }
        },
        error: function(XMLHttpRequest, textStatus, errorThrown) {
          Logger.error("Found error: " + XMLHttpRequest + " - " + textStatus + ", Error: " +  errorThrown);
        }
      });
    }
  });

});
