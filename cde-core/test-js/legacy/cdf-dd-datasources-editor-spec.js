/*!
 * Copyright 2002 - 2015 Webdetails, a Pentaho company.  All rights reserved.
 *
 * This software was developed by Webdetails and is provided under the terms
 * of the Mozilla Public License, Version 2.0, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to  http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */

describe("Datasources Editor Test #", function() {

	var tableManager = new TableManager('test-tableManager');
	var tableModel = new TableModel('test-tableModel');
	tableManager.setTableModel(tableModel);

	spyOn(tableModel, 'getColumnSetExpressions').and.callFake(function() {return [function(){}, function(){}]});

	$("<div id='wizardDialog'></div>").appendTo('body');
	$("<div class='cdfdd-wizard-title'></div>").appendTo('body');
	$("<div id='cdfdd-wizard-button-ok'></div>").appendTo("body");

	var mdxPh = $("<div></div>");
	var mqlPh = $("<div></div>");
	var scriptablePh = $("<div></div>");
	var jsonScriptablePh = $("<div></div>");
	var xPathPh = $("<div></div>");

	new CurrentMdxQueryRenderer(tableManager).render(mdxPh, "");
	new MqlQueryRenderer(tableManager).render(mqlPh, "");
	new ScriptableQueryRenderer(tableManager).render(scriptablePh, "");
	new JsonScriptableQueryRenderer(tableManager).render(jsonScriptablePh, "");
	new XPathQueryRenderer(tableManager).render(xPathPh, "");

	var buttons = [
		mdxPh.find("button"),
		mqlPh.find("button"),
		scriptablePh.find("button"),
		jsonScriptablePh.find("button"),
		xPathPh.find("button")
	];

	var panelSpy = {
		getParameters: function() {
			return ""
		}
	};
	
	var prepCounter = 0;
	beforeEach(function(done) {
		spyOn(Panel, "getPanel").and.returnValue(panelSpy);
		buttons[prepCounter++].click();
		setTimeout(done, 100);
	});

	afterEach(function(done) {
		$("#wizardDialog").empty();
		done();
	});

	it("MDX editor has the correct query template", function(done) {
		var result = $(".ace_line").text().replace(/\s/g, "");
		var expected = "select{}ONCOLUMNS,{}ONROWSfrom[]";
		expect(result).toBe(expected);
		done();
	});

	it("MQL editor has the correct query template", function(done) {
		var result = $(".ace_line").text().replace(/\s/g, "");
		var expected = '<![CDATA[<?xmlversion="1.0"encoding="UTF-8"?><mql><domain_type></domain_type><domain_id></domain_id><model_id></model_id><model_name></model_name><selections><!--Exampleselection<selection><view>CAT_ORDERS</view><column>BC_ORDERS_ORDERDATE</column></selection>--></selections><constraints><!--Exampleconstraint<constraint><operator>AND</operator><condition>[CAT_ORDERS.BC_ORDERDETAILS_QUANTITYORDERED]&gt;70</condition></constraint>--></constraints><orders/></mql>]]>';
		expect(result).toBe(expected);
		done();
	});

	it("Scriptable editor has the correct query template", function(done) {
		var result = $(".ace_line").text().replace(/\s/g, "");
		var expected = 'importorg.pentaho.reporting.engine.classic.core.util.TypedTableModel;String[]columnNames=newString[]{"value","name2"};Class[]columnTypes=newClass[]{Integer.class,String.class};TypedTableModelmodel=newTypedTableModel(columnNames,columnTypes);model.addRow(newObject[]{newInteger("0"),newString("Name")});returnmodel;';
		expect(result).toBe(expected);
		done();
	});

	it("JsonScriptable editor has the correct query template", function(done) {
		var result = $(".ace_line").text().replace(/\s/g, "");
		var expected = '{"resultset":[["Name",0]],"metadata":[{"colIndex":0,"colType":"String","colName":"value"},{"colIndex":1,"colType":"Integer","colName":"name2"}]}';
		expect(result).toBe(expected);
		done();
	});

	it("XPath editor has the correct query template", function(done) {
		var result = $(".ace_line").text().replace(/\s/g, "");
		var expected = '/*/*[CUSTOMERS_CUSTOMERNUMBER=103]';
		expect(result).toBe(expected);
		done();
	});
});