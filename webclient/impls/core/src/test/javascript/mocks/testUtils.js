var getTestTableManager = function(id) {
  id = id || "test-tableManager";
  return new TableManager(id);
};

var getTestTableModel = function(id) {
  id = id || "test-tableModel";
  var tableModel = new TableModel(id);
  var rowId = function(row) { return row.id; };
  tableModel.setRowId(rowId);
  var rowType = function(row) { return row.type; };
  tableModel.setRowType(rowType);
  var parentId = function(row) { return row.parent; };
  tableModel.setParentId(parentId);
  return tableModel;
};

/*
 * > resource
 * > row
 *   - col
 *     > image
 *   - col2
 *     > html
 * > space
 * > freeForm
 * > bootstrapPanel
 *   - bootstrapPanelHeader
 *   - bootstrapPanelBody
 *   - bootstrapPanelFooter
 */
var exampleData_1 = [
  { 'id': 'resource', 'parent': "UnIqEiD", 'type': "LayoutResourceCode",
    properties: [{ 'name': "name", 'value': "resource" }] },
  { 'id': 'row', 'parent': "UnIqEiD", 'type': "LayoutRow",
    properties: [{ 'name': "name", 'value': "row" }] },
  { 'id': 'col', 'parent': "row", 'type': "LayoutColumn",
    properties: [{ 'name': "name", 'value': "col" }] },
  { 'id': 'image', 'parent': "col", 'type': "LayoutImage",
    properties: [{ 'name': "name", 'value': "image" }] },
  { 'id': 'col-2', 'parent': "row", 'type': "LayoutBootstrapColumn",
    properties: [{ 'name': "name", 'value': "col-2" }] },
  { 'id': 'html', 'parent': "col-2", 'type': "LayoutHtml",
    properties: [{ 'name': "name", 'value': "html" }] },
  { 'id': 'space', 'parent': "UnIqEiD", 'type': "LayoutSpace",
    properties: [{ 'name': "name", 'value': "space" }] },
  { 'id': 'freeForm', 'parent': "UnIqEiD", 'type': "LayoutFreeForm",
    properties: [{ 'name': "name", 'value': "freeForm" }] },
  { 'id': 'bootstrapPanel', 'parent': "UnIqEiD", 'type': "BootstrapPanel",
    properties: [{ 'name': "name", 'value': "bootstrapPanel" }] },
  { 'id': 'bootstrapPanelHeader', 'parent': "bootstrapPanel", 'type': "BootstrapPanelHeader",
    properties: [{ 'name': "name", 'value': "bootstrapPanelHeader" }] },
  { 'id': 'bootstrapPanelBody', 'parent': "bootstrapPanel", 'type': "BootstrapPanelBody",
    properties: [{ 'name': "name", 'value': "bootstrapPanelBody" }] },
  { 'id': 'bootstrapPanelFooter', 'parent': "bootstrapPanel", 'type': "BootstrapPanelFooter",
    properties: [{ 'name': "name", 'value': "bootstrapPanelFooter" }] }
];

/*
 * > first
 *   - first-child
 *   - first-child-2
 *     > first-child-2-child
 * > second
 */
var exampleData_2 = [
  { 'id': 'first', 'parent': "UnIqEiD", 'type': "LayoutRow",
    properties: [{ 'name': "name", 'value': "first" }] },
  { 'id': 'first-child', 'parent': "first", 'type': "LayoutColumn",
    properties: [{ 'name': "name", 'value': "first-child" }] },
  { 'id': 'first-child-2', 'parent': "first", 'type': "LayoutColumn",
    properties: [{ 'name': "name", 'value': "first-child-2" }] },
  { 'id': 'first-child-2-child', 'parent': "first-child-2", 'type': "LayoutRow",
    properties: [{ 'name': "name", 'value': "first-child-2-child" }] },
  { 'id': 'second', 'parent': "UnIqEiD", 'type': "LayoutRow",
    properties: [{ 'name': "name", 'value': "second" }] }
];

var CDFDDDataUrl = "/fake/path";
