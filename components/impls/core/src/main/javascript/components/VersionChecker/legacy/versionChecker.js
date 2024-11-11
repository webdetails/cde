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
