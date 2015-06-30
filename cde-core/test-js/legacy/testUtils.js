var getTestTableManager = function(id) {
  id = id || "test-tableManager"
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

var exampleData_1 = [
  { 'id': 'resource', 'parent': "UnIqEiD", 'type': "LayoutResourceCode" },
  { 'id': 'row', 'parent': "UnIqEiD", 'type': "LayoutRow" },
  { 'id': 'col', 'parent': "row", 'type': "LayoutColumn" },
  { 'id': 'image', 'parent': "col", 'type': "LayoutImage" },
  { 'id': 'col-2', 'parent': "row", 'type': "LayoutBootstrapColumn" },
  { 'id': 'html', 'parent': "col-2", 'type': "LayoutHtml" },
  { 'id': 'space', 'parent': "UnIqEiD", 'type': "LayoutSpace" },
  { 'id': 'freeForm', 'parent': "UnIqEiD", 'type': "LayoutFreeForm" },
  { 'id': 'bootstrapPanel', 'parent': "UnIqEiD", 'type': "BootstrapPanel" },
  { 'id': 'bootstrapPanelHeader', 'parent': "bootstrapPanel", 'type': "BootstrapPanelHeader" },
  { 'id': 'bootstrapPanelBody', 'parent': "bootstrapPanel", 'type': "BootstrapPanelBody" },
  { 'id': 'bootstrapPanelFooter', 'parent': "bootstrapPanel", 'type': "BootstrapPanelFooter" }
];

var exampleData_2 = [
  { 'id': 'first', 'parent': "UnIqEiD", 'type': "LayoutRow", properties: [{ 'name': "name", 'value': "first" }] },
  { 'id': 'first-child', 'parent': "first", 'type': "LayoutColumn", properties: [{ 'name': "name", 'value': "first-child" }] },
  { 'id': 'first-child-2', 'parent': "first", 'type': "LayoutColumn", properties: [{ 'name': "name", 'value': "first-child-2" }] },
  { 'id': 'first-child-2-child', 'parent': "first-child-2", 'type': "LayoutRow", properties: [{ 'name': "name", 'value': "first-child-2-child" }] },
  { 'id': 'second', 'parent': "UnIqEiD", 'type': "LayoutRow", properties: [{ 'name': "name", 'value': "second" }] }
];
