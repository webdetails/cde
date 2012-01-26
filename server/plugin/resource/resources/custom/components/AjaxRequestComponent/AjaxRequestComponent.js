var AjaxRequestComponent = BaseComponent.extend({
  visible: false,
  update : function() {
    this.executeRequest(this);
  },
  executeRequest: function(object){
    var myself = this;
    var url = object.url;
    var ajaxRequestType = object.ajaxRequestType;
    var params = object.parameters;
    
    if(url == undefined){
      Dashboards.log("Fatal - No url passed","error");
      return;
    }
    if(ajaxRequestType == undefined){
      ajaxRequestType = "json";
    }
    if(params != undefined){
      for(var i = 0; i < params.length; i++){
	var value = Dashboards.getParameterValue(params[i][0]);
	params[i][1] = value;
      }
      params = Dashboards.propertiesArrayToObject(params);
    } else {
      params = {};
    }
    
    
    
    $.ajax({
      url: url,
      type: "POST",
      dataType: ajaxRequestType,
      async: true,
      data: params,
      complete: function (XMLHttpRequest, textStatus) {
          var values = XMLHttpRequest.responseText;
          var changedValues = undefined;
          
          if(values == undefined) {
            Dashboards.log("Found error: Empty Data");
	    return;
          }
 
          if(this.dataType == "xml" || this.dataType == "html"){
	    Dashboards.log("Found error: xml and html formats unsuppoted"); //set parameter needs to be changed
            return;
	  } else if(this.dataType == "json") {
            values = $.parseJSON(values);
	  } else if(this.dataType != "script" && this.dataType != "text"){
            Dashboards.log("Found error: Unknown returned format");
	    return;
	  }
          
          if((typeof(object.postFetch)=='function')){
            changedValues = object.postFetch(values);
          }
          if (changedValues != undefined){
            values = changedValues;
          }
    
          if (object.resultvar != undefined){
            Dashboards.setParameter(object.resultvar, values);
          }
      },
      error: function (XMLHttpRequest, textStatus, errorThrown) {
        Dashboards.log("Found error: " + XMLHttpRequest + " - " + textStatus + ", Error: " +  errorThrown,"error");
      }
    }
    );
  }
});