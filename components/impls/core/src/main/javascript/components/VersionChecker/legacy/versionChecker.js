/*!
 * Copyright 2002 - 2017 Webdetails, a Hitachi Vantara company. All rights reserved.
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

var CurrentVersionComponent = BaseComponent.extend({
  ph: null,


  update : function(){
    var self = this;
    this.ph = $('#' + this.htmlObject).empty();
    $.get(this.versionUrl, function(result){
      var msgHolder = $('<div/>').html(result);
      self.ph.append(msgHolder);
    });
  }
  
});

var VersionCheckComponent = BaseComponent.extend({
  
  ph: null,
  
  versionInfo: null,
    
  getMsgUpdate : function(url){
      return 'You are currently running an outdated version. Please update to the new version <a href="'+ url + '">here</a>.';
  },
  
  getMsgLatest: function(){
    return 'Your version is up to date.';
  },
  
  getMsgInconclusive: function(msg, url){
    return 'Only ctools branches support version checking ' + (msg ? '(' + msg + ') .' : '.') + ' You can install lastest version <a href="' + url + '">here</a>';
  },
  
  getMsgError: function(errorMsg){
    return 'There was an error checking for newer versions: ' + errorMsg;
  },
  
  update: function(){
    var self = this;
    this.ph = $("#" + this.htmlObject).empty();
    
    $.get(this.versionCheckUrl, function(result){
      
      if(!result){
        //TODO:log
        return;
      }
      
      try {
        result = JSON.parse(result);
        Dashboards.log("[VERSION CHECK COMPONENT] ### json parsed with no errors ###");
      } catch (e) {
          alert(e.message);
      }
      self.versionInfo = result;
      
      var msg = '';
      //error | inconclusive | update | latest
      switch(result.result){
        case 'update':
          msg = self.getMsgUpdate(result.downloadUrl);
          break;
        case 'latest':
          msg = self.getMsgLatest();
          break;
        case 'error':
          msg = self.getMsgError(result.msg);
          break;
        case 'inconclusive':
          msg = self.getMsgInconclusive(result.msg, result.downloadUrl);
          break;
      }
      
      var msgHolder = $('<div/>').html(msg);
      self.ph.append(msgHolder);
    });
  }
  
  
});
