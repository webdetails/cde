var AjaxRequestComponent = BaseComponent.extend({
  visible: false,
  update : function() {
    this.executeRequest(this);
  },
  
  parseXML : function(sText) {
    if( !sText ) {
        return null;
    }
    var xmlDoc;
    try { //Firefox, Mozilla, Opera, etc.
        parser=new DOMParser();
        xmlDoc=parser.parseFromString(sText,"text/xml");
        return xmlDoc;
    } catch(e){
        try { //Internet Explorer
            xmlDoc=new ActiveXObject("Microsoft.XMLDOM");
            xmlDoc.async="false";
            xmlDoc.loadXML(sText);
            return xmlDoc;
        } catch(e) {
        }
    }
    alert('XML is invalid or no XML parser found');
    return null;
  },
  
  executeRequest: function(object){
    var myself = this;
    var url = object.url;
    var ajaxRequestType = object.ajaxRequestType;
    var params = object.parameters;
    var asyncCall = object.asyncCall;
    
    if(url == undefined){
      Dashboards.log("Fatal - No url passed","error");
      return;
    }
    if(ajaxRequestType == undefined){
      ajaxRequestType = "json";
    }
    if(params != undefined){
      var containsTime = false;
      for(var i = 0; i < params.length; i++){
        var value;
        if(params[i][0] == 'time'){
          value = new Date().getTime();
          containsTime = true;
        } 
        else value = Dashboards.getParameterValue(params[i][0]);
        params[i][1] = value;
      }
      containsTime? params.push(['time',new Date().getTime()]) : 0;
      params = Dashboards.propertiesArrayToObject(params);

    } else {
      params = {};
    }
    
    if(asyncCall == undefined) asyncCall = true;
    
    
    $.ajax({
      url: url,
      type: "GET",
      dataType: ajaxRequestType,
      async: asyncCall,
      data: params,
      complete: function (XMLHttpRequest, textStatus) {
          var values = XMLHttpRequest.responseText;
          var changedValues = undefined;
          
          if(values == undefined) {
            Dashboards.log("Found error: Empty Data");
            return;
          }
 
          if(this.dataType == "xml" || this.dataType == "html"){
            var xmlDoc;
            try { //Firefox, Mozilla, Opera, etc.
                parser=new DOMParser();
                xmlDoc=parser.parseFromString(values,"text/xml");

            } catch(e){
                try { //Internet Explorer
                    xmlDoc=new ActiveXObject("Microsoft.XMLDOM");
                    xmlDoc.async="false";
                    xmlDoc.loadXML(values);
                    values = xmlDoc;
                } catch(e) {
                    Dashboards.log('XML is invalid or no XML parser found');
                }
            }
            values=xmlDoc;

            var nodeList = values.getElementsByTagName('return');
            if( nodeList.length > 0 && nodeList[0].firstChild ) {
              values = nodeList[0].firstChild.nodeValue;
            }
            else return;
            
            values = $.parseJSON(values);
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
            Dashboards.fireChange(object.resultvar, values);
          }
      },
      error: function (XMLHttpRequest, textStatus, errorThrown) {
        Dashboards.log("Found error: " + XMLHttpRequest + " - " + textStatus + ", Error: " +  errorThrown,"error");
      }
    }
    );
  }
});