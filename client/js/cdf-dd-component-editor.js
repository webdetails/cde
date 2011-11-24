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
        
"        <COMPONENTNAME>.render.call(myself);\n" +
        
 "       /* USE QUERY ?\n" +
        
  "      var query = new Query(myself.chartDefinition);\n" +

"        query.fetchData(myself.parameters, function(values) {\n" +
"            var changedValues = undefined;\n" +
"            if((typeof(myself.postFetch)=='function')){\n" +
"                changedValues = myself.postFetch(values);\n" +
"                $('#' + this.htmlObject).append('<div id=\"'+ this.htmlObject  +'protovis\"></div>');\n" +
"            }\n" +
"            if (changedValues != undefined) {\n" + 
"                values = changedValues;\n" +
"            }\n" +
"            KpiListComponent.render.call(myself,values);\n" +
"        });\n" +        
"        */\n" +
"    }\n" +
"},\n" +
"{\n" +    
"    render: function(values){\n" +        
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
'<script src="content/pentaho-cdf/js/jquery.js" type="text/javascript" language="javascript"></script>\n' +
'<script src="content/pentaho-cdf/js/jquery.ui.js" type="text/javascript" language="javascript"></script>\n' +
'<script src="content/pentaho-cdf/js/jquery.blockUI.js" type="text/javascript" language="javascript"></script>\n' +
'<script src="content/pentaho-cdf/js/Base.js" type="text/javascript" language="javascript"></script>\n' +
'<script src="content/pentaho-cdf/js/Dashboards.js" type="text/javascript" language="javascript"></script>\n' +
'<script src="content/pentaho-cdf/js/CoreComponents.js" type="text/javascript" language="javascript"></script>\n' +
'<script src="content/pentaho-cdf/js/daterangepicker/daterangepicker.jQuery.js"  type="text/javascript" language="javascript"></script>\n' +
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
'            <Property>executeAtStart</Property>\n' + 
'            <Property>htmlObject</Property>\n' + 
'            <Property>tooltip</Property>\n' + 
'            <Property>parameters</Property>\n' + 
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
