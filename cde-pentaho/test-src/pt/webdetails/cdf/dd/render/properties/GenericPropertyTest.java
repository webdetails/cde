package pt.webdetails.cdf.dd.render.properties;

//import java.util.ArrayList;
//import java.util.List;
//
//import junit.framework.Assert;
//
//import org.junit.Test;

// XXX commented out, redo test with whatever has this functionality now
// GenericProperty was deleted in cda89e00a94d0a2701e918c3f12f661edab8f6ba
public class GenericPropertyTest {
	
	

//	//Literal tests
//	
//	@Test
//	public void testLiteral_0_GetFunctionParameter() {
//		GenericProperty gp = new GenericProperty();
//		String input = "potatos";
//		input.replace("\"", "\\\"").replaceAll("(\\$\\{[^}]*\\})", "\"+ $1 + \"");
//		String result = gp.getFunctionParameter(input, true);
//		Assert.assertEquals("function(){" + (true ? " return \"" + input + "\"" : input) + ";}", result);
//	}
//	
//	@Test
//	public void testLiteral_1_GetFunctionParameter() {
//		GenericProperty gp = new GenericProperty();
//		String input = "functio(){return 'literal';} ";
//		input.replace("\"", "\\\"").replaceAll("(\\$\\{[^}]*\\})", "\"+ $1 + \""); 
//		String result = gp.getFunctionParameter(input, true);
//		Assert.assertEquals("function(){" + (true ? " return \"" + input + "\"" : input) + ";}", result);
//	}
//	
//	@Test
//	public void testLiteral_2_GetFunctionParameter() {
//		GenericProperty gp = new GenericProperty();
//		String input = "function f d() { return 'literal';} ";
//		input.replace("\"", "\\\"").replaceAll("(\\$\\{[^}]*\\})", "\"+ $1 + \"");
//		String result = gp.getFunctionParameter(input, true);
//		Assert.assertEquals("function(){" + (true ? " return \"" + input + "\"" : input) + ";}", result);
//	}
//
//	// Functions testing
//	
//	@Test
//	public void testFunction_0_GetFunctionParameter() {
//		GenericProperty gp = new GenericProperty();
//		String input = "function (){retun \"literal\";} "; 
//		String result = gp.getFunctionParameter(input, true);
//		Assert.assertEquals(input, result);
//	}
//
//	@Test
//	public void testFunction_1_GetFunctionParameter() {
//		GenericProperty gp = new GenericProperty();
//		String input = "function (){retun \"literal\";} "; 
//		String result = gp.getFunctionParameter(input, true);
//		Assert.assertEquals(input, result);
//	}
//
//	@Test
//	public void testFunction_2_GetFunctionParameter() {
//		GenericProperty gp = new GenericProperty();
//		String input = "function () {retun \"literal\";} "; 
//		String result = gp.getFunctionParameter(input, true);
//		Assert.assertEquals(input, result);
//	}
//
//	@Test
//	public void testFunction_3_GetFunctionParameter() {
//		GenericProperty gp = new GenericProperty();
//		String input = "function f() { \rreturn \"literal\";\r}\r\r\r "; 
//		String result = gp.getFunctionParameter(input, true);
//		Assert.assertEquals(input, result);
//	}
//	
//	@Test
//	public void testFunction_4_GetFunctionParameter() {
//		GenericProperty gp = new GenericProperty();
//		String input = "function \n f() { return \"literal\";}"; 
//		String result = gp.getFunctionParameter(input, true);
//		Assert.assertEquals(input, result);
//	}
//	
//	@Test
//	public void testFunction_5_GetFunctionParameter() {
//		GenericProperty gp = new GenericProperty();
//		String input = " function \n()\n {\n int x;\n return \"literal\";\n}\n\n\n "; 
//		String result = gp.getFunctionParameter(input, true);
//		Assert.assertEquals(input, result);
//	}
//	
//	@Test
//	public void testFunction_6_GetFunctionParameter() {
//		GenericProperty gp = new GenericProperty();
//		String input = "function \n() {\n \n return \"literal\";} \n "; 
//		String result = gp.getFunctionParameter(input, true);
//		Assert.assertEquals(input, result);
//	}
//	
//	@Test
//	public void testFunction_7_GetFunctionParameter() {
//		GenericProperty gp = new GenericProperty();
//		String input = "\n function f() \t { return \"blabfffffffffddddla\";\t\t\t\t }"; 
//		String result = gp.getFunctionParameter(input, true);
//		Assert.assertEquals(input, result);
//	}
//	
//	@Test
//	public void testFunction_8_GetFunctionParameter() {
//		GenericProperty gp = new GenericProperty();
//		String input = "\n function f() { return \"blabfffffffffddddla\"; }"; 
//		String result = gp.getFunctionParameter(input, true);
//		Assert.assertEquals(input, result);
//	}
//	
//	@Test
//	public void testFunction_9_GetFunctionParameter() {
//		GenericProperty gp = new GenericProperty();
//		String input = "\n function(x, y){ \n    return \"blabfffffffffddddla\"; \n}"; 
//		String result = gp.getFunctionParameter(input, true);
//		Assert.assertEquals(input, result);
//	}
//	
//	@Test
//	public void testFunction_10_GetFunctionParameter() {
//		GenericProperty gp = new GenericProperty();
//		String input = " \nfunction\nf()\n{\n\nreturn \n\"literal\";\n}\n\n\n "; 
//		String result = gp.getFunctionParameter(input, true);
//		Assert.assertEquals(input, result);
//	}
//	
//	@Test
//	public void testFunction_11_GetFunctionParameter() {
//		GenericProperty gp = new GenericProperty();
//		String input = "function () {\n    \n    var ctn = Dashboards.getParameterValue(\"updatingFiltersCounter\");    \n    if (ctn <= 0){\n        Dashboards.fireChange('applyButtonEvent');\n    }\n}       "; 
//		String result = gp.getFunctionParameter(input, true);
//		Assert.assertEquals(input, result);
//	}
//	
//	@Test
//	public void testFunction_12_GetFunctionParameter() {
//		GenericProperty gp = new GenericProperty();
//		String input = "function () {\n    \n    var ctn = Dashboards.getParameterValue(\"updatingFiltersCounter\"),\n        ph = $('#' + this.htmlObject);\n    \n    if (ctn > 0){\n        this.label = \"Updating Filters...\";\n        ph.addClass('updating');\n    }\n    else{\n        this.label = \"Apply Filters\";\n        ph.removeClass('updating');    \n    }\n}  "; 
//		String result = gp.getFunctionParameter(input, true);
//		Assert.assertEquals(input, result);
//	}
//	@Test
//	public void testFunction_13_GetFunctionParameter() {
//		GenericProperty gp = new GenericProperty();
//		String input = "function () {\n    \n    // Do not execute original component\n    if (this.name === 'render_filterTemplate'){\n        return false;\n    }\n    \n    var a = Dashboards.getParameterValue(\"updatingFiltersCounter\");\n    a++;\n    Dashboards.fireChange(\"updatingFiltersCounter\", a);\n    \n    var dim = Dashboards.getParameterValue( this.parameters[0][1] ),\n        hierarchy = flix.dimensions.getHierarchy( dim ),\n        deps = flix.dimensions.getDependencies(dim),\n        aux = flix.generateSlicer( deps , dim );\n        \n    if (!this.parameter){\n        this.parameter = flix.dimensions.getFilterParam( dim );     \n    } \n    if (!this.title){\n        this.title = flix.dimensions.getLabel( dim ).singular;\n    }\n    if (!this.hasSetListeners){\n        this.listeners = flix.dimensions.getActiveParams(dim);\n        this.hasSetListeners = true;\n    }\n        \n    var newParamsArray = [];    \n    newParamsArray.push( [ \"dimension\" , \"'\" + hierarchy + \"'\" ] ); \n    newParamsArray.push( [ 'selection' , this.parameter ] );\n    newParamsArray.push( [ \"slicer\" , \"'\" + aux.slicer + \"'\" ] );\n    newParamsArray.push( [ \"aggregator\" , \"'\" + aux.aggregator + \"'\" ] );\n    flix.updateQueryParams( this , newParamsArray );\n    \n}"; 
//		String result = gp.getFunctionParameter(input, true);
//		Assert.assertEquals(input, result);
//	}
//	@Test
//	public void testFunction_14_GetFunctionParameter() {
//		GenericProperty gp = new GenericProperty();
//		String input = "function(){\n    this.lifecycle =  { silent: true } ;\n    \n    var a = Dashboards.getParameterValue(\"updatingFiltersCounter\");\n    a--;\n    Dashboards.fireChange(\"updatingFiltersCounter\", a);\n}\n"; 
//		String result = gp.getFunctionParameter(input, true);
//		Assert.assertEquals(input, result);
//	}
//	@Test
//	public void testFunction_15_GetFunctionParameter() {
//		GenericProperty gp = new GenericProperty();
//		String input = "function () {\n    \n  function \n()\n {\n int x;\n return \"literal\";\n}\n\n\n  var f = function \n()\n {\n int x;\n return \"literal\";\n}\n\n\n   if (!this.parameters){ this.parameters = [] };\n    \n    var measureList = flix.layout.getSummaryMeasures(),\n        mdxMeasuresString = flix.mdx.measureArrayToMdx( measureList ),\n        mdxAliasString = flix.generateAlias( measureList );\n    \n    var newParamsArray = [];    \n    newParamsArray.push( [ \"retailerDimension\" , \"'\" + flix.dimensions.getHierarchy( 'retailer' ) + \"'\" ] );\n    newParamsArray.push( [ \"currencySuffix\" , \"'\" + flix.currency.getQuerySuffix() + \"'\" ] );\n    newParamsArray.push( [ \"oemSuffix\" , \"'\" + flix.currency.getOEMSuffix() + \"'\" ] );\n    newParamsArray.push( [ \"dataSince\" , \"'\" + flix.layout.dataSince() + \"'\" ] );   \n    newParamsArray.push( [ \"measures\" , \"'\" + mdxMeasuresString + \"'\" ] );\n    newParamsArray.push( [ \"alias\" , \"'\" + mdxAliasString + \"'\" ] );\n    flix.updateQueryParams( this , newParamsArray );    \n\n}"; 
//		String result = gp.getFunctionParameter(input, true);
//		Assert.assertEquals(input, result);
//	}
//	@Test
//	public void testFunction_16_GetFunctionParameter() {
//		GenericProperty gp = new GenericProperty();
//		String input = "\t  \n function () {\n  {{}()}  \n  function \n()\n {\n int x;\n return \"literal\";\n}\n\n\n  var f = function \n()\n {\n int x;\n return \"literal\";\n}\n\n\n   if (!this.parameters){ this.parameters = [] };\n    \n    var measureList = flix.layout.getSummaryMeasures(),\n        mdxMeasuresString = flix.mdx.measureArrayToMdx( measureList ),\n        mdxAliasString = flix.generateAlias( measureList );\n    \n    var newParamsArray = [];    \n    newParamsArray.push( [ \"retailerDimension\" , \"'\" + flix.dimensions.getHierarchy( 'retailer' ) + \"'\" ] );\n    newParamsArray.push( [ \"currencySuffix\" , \"'\" + flix.currency.getQuerySuffix() + \"'\" ] );\n    newParamsArray.push( [ \"oemSuffix\" , \"'\" + flix.currency.getOEMSuffix() + \"'\" ] );\n    newParamsArray.push( [ \"dataSince\" , \"'\" + flix.layout.dataSince() + \"'\" ] );   \n    newParamsArray.push( [ \"measures\" , \"'\" + mdxMeasuresString + \"'\" ] );\n    newParamsArray.push( [ \"alias\" , \"'\" + mdxAliasString + \"'\" ] );\n    flix.updateQueryParams( this , newParamsArray );    \n\n}"; 
//		//Assert.assertEquals(input, result);
//	}
//	@Test
//	public void testFunction_17_GetFunctionParameter() {
//		GenericProperty gp = new GenericProperty();
//		String input = "function (){\n    \n    if (!this.hasHeaderTooltips){\n        var ph = $('#' + this.htmlObject);\n        \n        flix.tooltips.add2TableHeaders( ph );\n        this.hasHeaderTooltips = true;\n    }\n}"; 
//		String result = gp.getFunctionParameter(input, true);
//		Assert.assertEquals(input, result);
//	}
//	@Test
//	public void testFunction_18_GetFunctionParameter() {
//		GenericProperty gp = new GenericProperty();
//		String input = "function (data){\n    \n    var myself = this;\n    \n    var trendOpts = flix.settings.addIns.trendArrow;\n    myself.setAddInOptions(\"colType\",\"trendArrow\",trendOpts);\n    \n\n    \n    var measureFormatOpts = {\n        measureFormatMap: flix.measures.getFormatMap()\n    }\n    myself.setAddInOptions(\"colType\",\"measureFormatText\",measureFormatOpts);\n    \n    \n    var sparkOpts = flix.settings.addIns.sparkline;\n    $.extend( sparkOpts, {  extension: function (tgt, st, opt){\n        var t = $(tgt);\n        t.bind('click', function(ev){\n            var popupComponent = Dashboards.getComponentByName( \"render_bigSparklinePopup\"),\n                measure = st.rawData.resultset[st.rowIdx][0];\n            Dashboards.fireChange( 'sparklineMeasureParam' , measure );\n            popupComponent.popup( t );\n        });   \n    }});\n    myself.setAddInOptions(\"colType\", \"sparkline2\", sparkOpts);\n    \n    \n    this.hasHeaderTooltips = false;\n    \n    \n}"; 
//		String result = gp.getFunctionParameter(input, true);
//		Assert.assertEquals(input, result);
//	}
//	@Test
//	public void testFunction_19_GetFunctionParameter() {
//		GenericProperty gp = new GenericProperty();
//		String input = "function () {\n        \n    var aux = flix.generateSlicerAndAggregator2( flix.layout.getFilters(), \n                    flix.addRoleExclusions( ));\n\n    var measureListTop = flix.layout.getTableMeasures(),\n        measureListBottom = flix.layout.getTableMeasures2(),\n        mdxMeasuresStringTop = flix.mdx.measureArrayToMdx( measureListTop ),\n        mdxMeasuresStringBottom = flix.mdx.measureArrayToMdx( measureListBottom ),\n        measureList = _.union( measureListTop, measureListBottom),\n        mdxAliasString = flix.generateAlias( measureList );\n\n    var newParamsArray = [];    \n    newParamsArray.push( [ \"slicer\" , \"'\" + aux.slicer + \"'\" ] );\n    newParamsArray.push( [ \"aggregator\" , \"'\" + aux.aggregator + \"'\" ] );\n    newParamsArray.push( [ \"currencySuffix\" , \"'\" + flix.currency.getQuerySuffix() + \"'\" ] );\n    newParamsArray.push( [ \"oemSuffix\" , \"'\" + flix.currency.getOEMSuffix() + \"'\" ] );\n    newParamsArray.push( [ \"retailerDimension\" , \"'\" + flix.dimensions.getHierarchy( 'retailer' ) + \"'\" ] ); \n    newParamsArray.push( [ \"measuresTop\" , \"'\" + mdxMeasuresStringTop + \"'\" ] );\n    newParamsArray.push( [ \"measuresBottom\" , \"'\" + mdxMeasuresStringBottom + \"'\" ] );\n    newParamsArray.push( [ \"alias\" , \"'\" + mdxAliasString + \"'\" ] );\n    newParamsArray.push( [ \"calendar\" , \"'\" + flix.date.getBusinessCalendar() + \"'\"] );\n    flix.updateQueryParams( this , newParamsArray );\n\n    \n}\n    "; 
//		String result = gp.getFunctionParameter(input, true);
//		Assert.assertEquals(input, result);
//	}
//	@Test
//	public void testFunction_20_GetFunctionParameter() {
//		GenericProperty gp = new GenericProperty();
//		String input = "function () {  \n    var dimensionParamName = \"activeDimensionParam\",\n        dimensionParamValue = Dashboards.getParameterValue( this.parameters[0][1] ),\n        newLocation = flix.nav.getSiteContent(\"full\");\n    \n    flix.fireAll( [ [ dimensionParamName, dimensionParamValue] ]);\n    flix.nav.changeLocation( newLocation );\n}"; 
//		String result = gp.getFunctionParameter(input, true);
//		Assert.assertEquals(input, result);
//	}
//	@Test
//	public void testFunction_21_GetFunctionParameter() {
//		GenericProperty gp = new GenericProperty();
//		String input = "function () {\n    \n    if (!this.parameters){\n        this.parameters = [[\"\",\"supportTableParam\"]];\n    }\n    \n    // Do not execute original component\n    if (this.name === 'render_supportTableButton'){\n        return false;\n    }\n    \n\n    \n}"; 
//		String result = gp.getFunctionParameter(input, true);
//		Assert.assertEquals(input, result);
//	}
//	@Test
//	public void testFunction_22_GetFunctionParameter() {
//		GenericProperty gp = new GenericProperty();
//		String input = "function () {\n    \n    // Do not execute original component\n    if (this.name === 'render_supportTable'){\n        return false;\n    }\n    \n    var dim = Dashboards.getParameterValue( this.parameters[0][1] ),\n        hierarchy = flix.dimensions.getHierarchy( dim ),\n        aux = flix.generateSlicer( flix.layout.getFilters() , \n                    flix.addRoleExclusions( dim ) ),\n        rowsSet = flix.generateRowsSet( flix.layout.getFilters(), dim ),\n        measureList = flix.layout.getTopMeasures(),\n        mdxMeasuresString = flix.mdx.measureArrayToMdx( measureList ),\n        mdxAliasString = flix.generateAlias( measureList ),\n        topMeasure = flix.mdx.getMeasure( flix.measures.getMdxMeasure( measureList[0] ) );\n  \n    var newParamsArray = [];    \n    newParamsArray.push( [ \"dimension\" , \"'\" + hierarchy + \"'\" ] );           \n    newParamsArray.push( [ \"slicer\" , \"'\" + aux.slicer + \"'\" ] );\n    newParamsArray.push( [ \"aggregator\" , \"'\" + aux.aggregator + \"'\" ] );\n    newParamsArray.push( [ \"rowsSet\" , \"'\" + rowsSet + \"'\" ] );\n    newParamsArray.push( [ \"currencySuffix\" , \"'\" + flix.currency.getQuerySuffix() + \"'\" ] );\n    newParamsArray.push( [ \"oemSuffix\" , \"'\" + flix.currency.getOEMSuffix() + \"'\" ] );\n    newParamsArray.push( [ \"measures\" , \"'\" + mdxMeasuresString + \"'\" ] );\n    newParamsArray.push( [ \"alias\" , \"'\" + mdxAliasString + \"'\" ] );\n    newParamsArray.push( [ \"topMeasure\" , \"'\" + topMeasure + \"'\" ] );\n    flix.updateQueryParams( this , newParamsArray );\n\n\n        \n    var dimensionParam = \"activeDimensionParam\",\n        memberParam = \"activeMemberParam\",\n        newLocation = flix.nav.getSiteContent(\"detail\"),\n        myself =  this;\n    \n    var clickHandlerOpts = function (state) {      \n        return { \n            clickHandler: function (e) {                       \n                    var mdxName = flix.mdx.getName( state.value , dim );\n                    flix.fireAll( [ [ dimensionParam, dim ],\n                                    [ memberParam, {name: state.value, uniqueName: mdxName } ] ]);\n                    flix.nav.changeLocation( newLocation );\n            }\n        }\n    }\n    this.setAddInOptions(\"colType\",\"clickHandler\", clickHandlerOpts);\n    \n    var formTextOpts = function (state) {      \n        return { \n                valueFormat: function(v, format, st) {\n                    if (isNaN(v) ||  v === null) {\n                        return 'N/A';\n                    }\n                    var ft = flix.measures.getFormat( st.category );\n                    return sprintf( ft, v);\n                }\n        }\n    }\n    this.setAddInOptions(\"colType\",\"measureFormatText\", formTextOpts);\n    \n    if ( dim === \"viewTime\"){\n        this.extraOptions = [ [ 'aaSortingFixed' , [[0,'asc'] ] ],\n                              [ 'iDisplayLength' , 5 ] ];\n    } else {\n        this.extraOptions = [ [ 'aaSortingFixed' , [[1,'desc'] ] ] ];\n    }\n    \n    this.hasTweakedHeaders = false;\n    \n}"; 
//		String result = gp.getFunctionParameter(input, true);
//		Assert.assertEquals(input, result);
//	}
//	@Test
//	public void testFunction_23_GetFunctionParameter() {
//		GenericProperty gp = new GenericProperty();
//		String input = "function () {\n    \n    // Temporary\n    return false;\n    \n    // Do not execute original component\n    if (this.name === 'render_supportTable'){\n        return false;\n    }\n    \n    var dim = Dashboards.getParameterValue( this.parameters[0][1] ),\n        hierarchy = flix.dimensions.getHierarchy( dim ),\n        aux = flix.generateSlicer( flix.layout.getFilters() , \n                    flix.addRoleExclusions( dim ) ),\n        rowsSet = flix.generateRowsSet( flix.layout.getFilters(), dim ),\n        measureList = flix.layout.getTopMeasures(),\n        mdxMeasuresString = flix.mdx.measureArrayToMdx( measureList ),\n        mdxAliasString = flix.generateAlias( measureList ),\n        topMeasure = flix.mdx.getMeasure( flix.measures.getMdxMeasure( measureList[0] ) );\n  \n    var newParamsArray = [];    \n    newParamsArray.push( [ \"dimension\" , \"'\" + hierarchy + \"'\" ] );\n    newParamsArray.push( [ \"retailerDimension\" , \"'\" + flix.dimensions.getHierarchy(\"retailer\") + \"'\" ]);\n    newParamsArray.push( [ \"slicer\" , \"'\" + aux.slicer + \"'\" ] );\n    newParamsArray.push( [ \"aggregator\" , \"'\" + aux.aggregator + \"'\" ] );\n    newParamsArray.push( [ \"rowsSet\" , \"'\" + rowsSet + \"'\" ] );\n    newParamsArray.push( [ \"currencySuffix\" , \"'\" + flix.currency.getQuerySuffix() + \"'\" ] );\n    newParamsArray.push( [ \"oemSuffix\" , \"'\" + flix.currency.getOEMSuffix() + \"'\" ] );\n    newParamsArray.push( [ \"measures\" , \"'\" + mdxMeasuresString + \"'\" ] );\n    newParamsArray.push( [ \"alias\" , \"'\" + mdxAliasString + \"'\" ] );\n    newParamsArray.push( [ \"topMeasure\" , \"'\" + topMeasure + \"'\" ] );\n    flix.updateQueryParams( this , newParamsArray );\n\n\n        \n    var dimensionParam = \"activeDimensionParam\",\n        memberParam = \"activeMemberParam\",\n        newLocation = flix.nav.getSiteContent(\"detail\"),\n        myself =  this;\n    \n    var clickHandlerOpts = function (state) {      \n        return { \n            clickHandler: function (e) {                       \n                    var mdxName = flix.mdx.getName( state.value , dim );\n                    flix.fireAll( [ [ dimensionParam, dim ],\n                                    [ memberParam, {name: state.value, uniqueName: mdxName } ] ]);\n                    flix.nav.changeLocation( newLocation );\n            }\n        }\n    }\n    this.setAddInOptions(\"colType\",\"clickHandler\", clickHandlerOpts);\n    \n    var formTextOpts = function (state) {      \n        return { \n                valueFormat: function(v, format, st) {\n                    if (isNaN(v) ||  v === null) {\n                        return 'N/A';\n                    }\n                    var ft = flix.measures.getFormat( st.category );\n                    return sprintf( ft, v);\n                }\n        }\n    }\n    this.setAddInOptions(\"colType\",\"measureFormatText\", formTextOpts);\n    \n    this.extraOptions.push( [ 'aaSortingFixed' , [[2,'desc'] ] ]);\n    \n    this.hasTweakedHeaders = false;\n    \n}"; 
//		String result = gp.getFunctionParameter(input, true);
//		Assert.assertEquals(input, result);
//	}
//	@Test
//	public void testFunction_24_GetFunctionParameter() {
//		GenericProperty gp = new GenericProperty();
//		String input = "function f() {"; 
//		String result = gp.getFunctionParameter(input, true);
//		Assert.assertEquals(input, result);
//	}
//	

}
