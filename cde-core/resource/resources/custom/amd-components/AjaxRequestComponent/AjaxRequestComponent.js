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
  'cdf/components/BaseComponent',
  'cdf/Logger',
  'cdf/lib/jquery',
  'cdf/dashboard/Utils'],
  function(BaseComponent, Logger, $, Utils) {

  var AjaxRequestComponent = BaseComponent.extend({
    visible: false,
    update : function() {
      this.executeRequest(this);
    },
    
    parseXML : function(sText) {
      if(!sText) {
        return null;
      }
      var xmlDoc;
      try { //Firefox, Mozilla, Opera, etc.
        parser = new DOMParser();
        xmlDoc = parser.parseFromString(sText, "text/xml");
        return xmlDoc;
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
            var xmlDoc;
            try { //Firefox, Mozilla, Opera, etc.
              parser = new DOMParser();
              xmlDoc = parser.parseFromString(values, "text/xml");
            } catch(e) {
              try { //Internet Explorer
                xmlDoc = new ActiveXObject("Microsoft.XMLDOM");
                xmlDoc.async = "false";
                xmlDoc.loadXML(values);
                values = xmlDoc;
              } catch(e) {
                Logger.error('XML is invalid or no XML parser found');
              }
            }
            values = xmlDoc;

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

  return AjaxRequestComponent;

});
