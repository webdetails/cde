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

var jsTemplateForComponent = "/** \n" +
 "*\n" +
 "* <COMPONENTNAME> \n" +
 "*\n" +
 "* <DESCRIPTION>\n" +
 "*\n" +
 "*/\n" +
"var <COMPONENTNAME> = BaseComponent.extend({\n" +
"    ph: undefined,\n" +
"    update : function() {\n" +
 "       var myself=this;\n" +
 "        //NOT USING QUERY\n "       +
"        this.render.call(myself);\n" +
        
 "       /* USING QUERY\n" +
        
  "      var query = Dashboards.getQuery(myself.queryDefinition);\n" +

"        query.fetchData(myself.parameters, function(values) {\n" +
"            var changedValues = undefined;\n" +
"            if((typeof(myself.postFetch)=='function')){\n" +
"                changedValues = myself.postFetch(values);\n" +
"                $('#' + this.htmlObject).append('<div id=\"'+ this.htmlObject  +'protovis\"></div>');\n" +
"            }\n" +
"            if (changedValues != undefined) {\n" + 
"                values = changedValues;\n" +
"            }\n" +
"            myself.render.call(myself,values);\n" +
"        });\n" +        
"        */\n" +
"    },\n" +
"    render: function(values){\n" +  
"        this.ph = $('#' + this.htmlObject);\n" + 
"         this.ph.html('<COMPONENTNAME>');\n" +
"    }\n" +
"});";


var cssTemplateForComponent = 'div.container {' + 
' padding:5px;' + 
' border: 1px solid red' + 
'}';


var htmlTemplateForComponent = '<html>\n' + 
'<head>\n' +
'\n' +
'<!-- Generico -->\n' +
'<script src="../pentaho-cdf/js/jquery.js" type="text/javascript" language="javascript"></script>\n' +
'<script src="../pentaho-cdf/js/jquery.ui.js" type="text/javascript" language="javascript"></script>\n' +
'<script src="../pentaho-cdf/js/jquery.blockUI.js" type="text/javascript" language="javascript"></script>\n' +
'<script src="../pentaho-cdf/js/Base.js" type="text/javascript" language="javascript"></script>\n' +
'<script src="../pentaho-cdf/js/Dashboards.js" type="text/javascript" language="javascript"></script>\n' +
'<script src="../pentaho-cdf/js/CoreComponents.js" type="text/javascript" language="javascript"></script>\n' +
'<script src="../pentaho-cdf/js/daterangepicker/daterangepicker.jQuery.js"  type="text/javascript" language="javascript"></script>\n' +
'</head>\n' +
'\n' +
'<body>\n' +
'<script>\n' +
'var component = {\n' +
'	name: "my<COMPONENTNAME>",\n' +
'	type: "<COMPONENTNAME>",\n' +
'	htmlObject: "testComponent"\n' +
'};\n' +
'\n' +
'$(function(){\n' +
'	Dashboards.log("Starting test <COMPONENTNAME>");\n' +
'	Dashboards.init([component]);\n' +
'	Dashboards.update(component);\n' +
'});\n' +
'\n' +
'</script>\n' +
'\n' +
'<div class="container">\n' +
'	<div id="firstRow" class="clearfix" style="width:75%">\n' +
'		<div class="<COMPONENTNAME>style" id="testComponent">\n' +
'		</div>\n' +
'	</div>\n' +
'</div>\n' +
'\n' +
'</body>\n' +
'\n' +
'</html>\n';


var componentTemplateForComponent = '<DesignerComponent>\n' + 
'    <Header>\n' + 
'        <Name><COMPONENTNAME></Name>\n' + 
'        <IName><COMPONENTNAME></IName>\n' + 
'        <Description><COMPONENTNAME></Description>\n' + 
'        <Category>CUSTOMCOMPONENTS</Category>\n' + 
'        <CatDescription>Custom</CatDescription>\n' + 
'        <Type>PalleteEntry</Type>\n' + 
'        <Version>1.0</Version>\n' + 
'    </Header>\n' + 
'    <Contents>\n' + 
'        <Model>\n' +
' <!-- Uncomment for datasource usage ' + 
'      <Definition name="queryDefinition">\n' + 
'        <Property type="query">dataSource</Property>\n' + 
'      </Definition>\n' + 
' -->' +
'            <Property>executeAtStart</Property>\n' + 
'            <Property>preExecution</Property>\n' + 
'            <Property>postExecution</Property>\n' + 
'            <Property>refreshPeriod</Property>\n' + 
'            <Property>htmlObject</Property>\n' + 
'            <Property>tooltip</Property>\n' + 
'            <Property name="parameters">xActionArrayParameter</Property>\n' + 
'            <Property>listeners</Property>\n' + 
'        </Model>\n' + 
'        <Implementation>\n' + 
'            <Code src="<COMPONENTNAME>.js" />\n' + 
'            <Styles>\n' + 
'                <Style version="1.0" src="<COMPONENTNAME>.css"><COMPONENTNAME></Style>\n' + 
'            </Styles>\n' + 
'            <Dependencies/>\n' + 
'            <CustomProperties>\n' + 
'            </CustomProperties>\n' + 
'        </Implementation>\n' + 
'    </Contents>\n' + 
'</DesignerComponent>\n';
