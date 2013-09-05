lib('protovis-bundle.js');

elem = document.createElement('g');
elem.setAttribute('id','canvas');
document.lastChild.appendChild(elem);

var render_salesGraphForMarker = {
	type: "cccPieChart",
	name: "render_salesGraphForMarker",
	chartDefinition: {
		width: 75,
		height: 75,
		dataAccessId: "salesGraphQuery",
path: "/plugin-samples/pentaho-cdf-dd/tests/FullMapTest.cda",
		crosstabMode: false,
		seriesInRows: false,
		animate: false,
		clickable: false,
		timeSeries: false,
		timeSeriesFormat: "%Y-%m-%d",
		innerGap: 0.9,
		explodedSliceIndex: 0,
		explodedSliceRadius: 0,
		orientation: "vertical",
		colors: [],
		showValues: false,
		valuesAnchor: "right",
		titlePosition: "top",
		titleSize: 25,
		legend: false,
		legendPosition: "bottom",
		legendAlign: "center",
		showXScale: true,
		xAxisPosition: "bottom",
		xAxisSize: 30,
		showYScale: true,
		yAxisPosition: "left",
		yAxisSize: 50,
		xAxisFullGrid: false,
		yAxisFullGrid: false,
		axisOffset: 0,
		originIsZero: true,
		extensionPoints: []
	},
	parameters: [["customer","CustomerNameParameter"]],
	executeAtStart: true,
	listeners: []

};
var datasource = datasourceFactory.createDatasource('cda');
datasource.setDefinitionFile(render_salesGraphForMarker.chartDefinition.path);
datasource.setDataAccessId('salesGraphQuery');

var paramcustomer = params.get('customer');
paramcustomer = (paramcustomer !== null && paramcustomer !== '')? paramcustomer : 'none';
datasource.setParameter('customer', paramcustomer);
var data = eval('new Object(' + String(datasource.execute()) + ');');
var w = parseInt(params.get('width')) || render_salesGraphForMarker.chartDefinition.width;
var h = parseInt(params.get('height')) || render_salesGraphForMarker.chartDefinition.height;
render_salesGraphForMarker.chartDefinition.width = w; render_salesGraphForMarker.chartDefinition.height = h;
print( 'Width: ' + w +  ' ( ' + typeof w + ' ) ; Height: ' + h +' ( ' + typeof h +' )');
bg = document.createElementNS('http://www.w3.org/2000/svg','rect');bg.setAttribute('id','foo');bg.setAttribute('x','0');bg.setAttribute('y','0');bg.setAttribute('width', w);bg.setAttribute('height',h);bg.setAttribute('style', 'fill:white');document.lastChild.appendChild(bg);renderCccFromComponent(render_salesGraphForMarker, data);
document.lastChild.setAttribute('width', render_salesGraphForMarker.chartDefinition.width);
document.lastChild.setAttribute('height', render_salesGraphForMarker.chartDefinition.height);
