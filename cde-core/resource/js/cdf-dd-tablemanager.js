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

var TableManager = Base.extend({

  id: "",
  tableId: "",
  logger: {},
  title: "Title",
  tableModel: {},
  isSelectedCell: false,
  isSelectedGroupCell: false,
  hasAdvancedProperties: false,
  selectedCell: [],
  droppedOnId: "",
  operations: [],
  linkedTableManager: undefined,
  linkedTableManagerOperation: undefined,
  parentTableManager: undefined,
  cellRendererPool: {},

  constructor: function(id) {
    this.logger = new Logger("TableManager - " + id);
    this.id = id;
    this.tableId = "table-" + id;

    // set a Default Table Model
    // this.setTableModel(new TableModel());

    // Register this tablemanager in the global area
    TableManager.register(this);
  },

  init: function() {

    this.reset();
    $("#" + this.id).append(this.newTable());
    this.render();
  },

  reset: function() {
    $("#" + this.id).empty();
  },

  render: function() {
    this.logger.debug("Rendering table " + this.getTableId());

    // Create headers;
    var headerRows = $("<tr></tr>");
    var myself = this;
    $.each(myself.getTableModel().getColumnNames(), function(i, val) {
      var _header = $('<th><div class="tableHeader ui-state-default">' + val + '</div></th>');

      if(typeof myself.getTableModel().getColumnSizes() != 'undefined') {
        _header.attr('width', myself.getTableModel().getColumnSizes()[i]);
      }
      _header.appendTo(headerRows);
    });
    headerRows.appendTo("#" + this.getTableId() + " > thead");

    // Create rows
    var data = this.getTableModel().getData() || [];

    for(var i = 0; i < data.length; i++) {
      if(typeof(data[i]) === "object") {
        myself.addRow(data[i]);
      }
    }

    $("#" + this.getTableId()).treeTable();
    this.updateOperations();
  },

  newTable: function(args) {
    var table = '' +
        '<div class="tableContainer">\n' +
        ' <div class="tableCaption ui-state-default">\n' +
        '   <div class="simpleProperties propertiesSelected">' + this.title + '</div>\n' +
        '   <div id="' + this.tableId + 'Operations" style="float: right" class="cdfdd-operations"></div>\n' +
        (this.hasAdvancedProperties == true ? '<span style="float:left">&nbsp;&nbsp;/&nbsp;&nbsp;</span><div class="advancedProperties propertiesUnSelected">Advanced Properties</div>\n' : '') +
        ' </div>\n' +
        ' <div class="scrollContainer">\n' +
        '   <table id="' + this.tableId + '" class="' + this.tableId + ' myTreeTable cdfdd ui-reset ui-clearfix ui-component ui-hover-state">\n' +
        '     <thead>\n' +
        '     </thead>\n' +
        '     <tbody class="ui-widget-content">\n' +
        '     </tbody>\n' +
        '   </table>\n' +
        ' </div>\n' +
        '</div>\n';

    return table;
  },

  addRow: function(row, pos) {
    // Adds row. -1 to add to the end of the list

    // Merge default options here
    // this.logger.debug("Adding row type "+ row.type  +" to table " + this.getTableId());
    var _model = BaseModel.getModel(row.type);
    if(typeof _model != 'undefined') {
      this.extendProperties(row, _model.getStub());
    }

    var rowObj = $('<tr></tr>');

    // Get id
    var _id;
    try {
      _id = this.getTableModel().getRowId()(row);
      rowObj.attr("id", _id);
    } catch (e) {
      this.logger.error("Error evaluating id expression " + this.getTableModel().getRowId() + ": " + e);
    }

    var _parent;
    var _parentExpression = this.getTableModel().getParentId();
    // parentId?
    try {
      if(typeof _parentExpression != 'undefined') {
        _parent = _parentExpression(row);
        if(typeof _parent != 'undefined' && _parent != IndexManager.ROOTID) {
          //this.logger.debug("Row parent: " + _parent );
          rowObj.addClass("child-of-" + _parent);
        }
      }
    } catch (e) {
      this.logger.error("Error evaluating parent expression " + _parentExpression + ": " + e);
    }

    // Add columns
    var columnExpressions = this.getTableModel().getColumnGetExpressions();
    for(var i in columnExpressions) {
      if(columnExpressions.hasOwnProperty(i)) {
        this.renderColumn(rowObj, row, i);
      }
    }

    var selector = "table#" + this.getTableId() + " tbody";
    if(pos < 0 || pos == undefined) {
      $(selector).append(rowObj);
      //rowObj.appendTo($(selector));
      //$(selector).append(html); // TODO <<-- undefined variable !!
    } else {
      var _selector = $(selector + " > tr:eq(" + pos + ")");
      _selector.length == 1 ? _selector.before(rowObj) : $(selector).append(rowObj);
    }

    this.dragAndDrop(row, _id);

    return _id;

  },

  dragAndDrop: function(row, id) {
    var layoutTreeName = "table-" + LayoutPanel.TREE;
    var tableManager = TableManager.getTableManager(layoutTreeName);
    // CDF-271, jQuery 1.9.1 has a stricter selector syntax http://api.jquery.com/category/selectors/
    var layoutTableSelector = 'table#' + layoutTreeName + ' tbody';

    $(layoutTableSelector + ' #' + id).draggable({
      revert: 'invalid',
      helper: 'clone',
      axis: 'y',
      cursor: 'auto',
      disabled: tableManager.disableDragObj(row),
      delay: 100,
      opacity: 0.50,

      //Events
      start: function(event, ui) {
        var originalRow = $(this);
        var originalRowElements = originalRow.find('td');
        var dragObjElements = ui.helper.find('td');

        originalRow.addClass('dragging_element');
        $('body').addClass('dragging_cursor');

        originalRowElements.each(function(i, elem) {
          var width = $(elem).width();
          dragObjElements.eq(i).width(width);
        });
      },
      stop: function(event, ui) {
        var originalRow = $(this);

        originalRow.removeClass('dragging_element');
        $('body').removeClass('dragging_cursor');
      }

    }).droppable({
      accept: function(dragObj) {
        if($(layoutTableSelector).find(dragObj).length) {
          var dropId = $(this).attr('id');
          var dragId = dragObj.attr('id');
          return !tableManager.disableDrop(dragId, dropId);
        } else {
          return false;
        }
      },

      //Events
      activate: function(event, ui) {
        var dropId = $(this).attr('id');
        var dragId = ui.draggable.attr('id');
        if(tableManager.canMoveInto(dragId, dropId)) {
          $(this).droppable('option', 'hoverClass', 'layout_hover_dropInto');
        } else {
          $(this).droppable('option', 'hoverClass', 'layout_hover_moveTo');
        }
      },
      drop: function(event, ui) {
        ui.helper.attr('class', '');
        tableManager.removeExtraHoverStyles($(this));

        var dropId = $(this).attr('id');
        tableManager.setDroppedOnId(dropId);

        var moveOperation = new MoveToOperation();
        var command = new RowOperationCommand(moveOperation, tableManager);

        Commands.executeCommand(command);
      },
      over: function(event, ui) {
        tableManager.addExtraHoverStyles(ui.draggable, $(this));
      },

      out: function(event, ui) {
        tableManager.removeExtraHoverStyles($(this));
      }
    });
  },

  addExtraHoverStyles: function(dragRow, hoverRow) {
    var indexManager = this.getTableModel().getIndexManager();
    var dragYPos = dragRow.position().top;
    var dropYPos = hoverRow.position().top;

    if(hoverRow.hasClass('layout_hover_dropInto')) {
      return undefined;
    }

    if(dragYPos > dropYPos) {
      hoverRow.addClass('layout_hover_moveTo_up');
    } else if(!hoverRow.hasClass('parent') || hoverRow.hasClass('collapsed')) {
      hoverRow.addClass('layout_hover_moveTo_down');
    } else {
      var hoverRowId = hoverRow.attr('id');
      while(hoverRow.hasClass('parent') && hoverRow.hasClass('expanded')) {
        var children = indexManager.getIndex()[hoverRowId].children;
        hoverRowId = children[children.length - 1].id;
        hoverRow = $('#' + hoverRowId);
      }
      hoverRow.addClass('layout_hover_moveTo_down');
    }
  },

  removeExtraHoverStyles: function(row) {
    var indexManager = this.getTableModel().getIndexManager();

    row.removeClass('layout_hover_moveTo_up').removeClass('layout_hover_moveTo_down');

    if(row.hasClass('parent') && row.hasClass('expanded')) {
      var rowId = row.attr('id');
      while(row.hasClass('parent') && row.hasClass('expanded')) {
        var children = indexManager.getIndex()[rowId].children;
        rowId = children[children.length - 1].id;
        row = $('#' + rowId);
      }
      row.removeClass('layout_hover_moveTo_up').removeClass('layout_hover_moveTo_down');
    }
  },

  disableDragObj: function(row) {
    var dragRT = row.type;
    return !CellOperations.isDraggable(dragRT);
  },

  disableDrop: function(dragId, dropId) {
    var notToRoot = [LayoutImageModel.MODEL, LayoutHtmlModel.MODEL, LayoutColumnModel.MODEL, LayoutBootstrapColumnModel.MODEL]
    var indexManager = this.getTableModel().getIndexManager();
    var rowIndex = indexManager.getIndex();

    var dragParentId = rowIndex[dragId].parent;
    var dropParentId = rowIndex[dropId].parent;
    var dragRT = rowIndex[dragId].type;

    //disable drop on (sub-)children of drag and on direct parent of drag
    if(this.isChildrenOfObj(dragId, dropId) || dragParentId == dropId) {
      return true;
    }

    //disable drop when dragObj cant moveInto and moveTo dropObj
    if(!this.canMoveTo(dragId, dropId) && !this.canMoveInto(dragId, dropId)) {
      return true;
    }

    if(dropParentId != IndexManager.ROOTID) {
      //disable drop when dragObj cant moveInto dropObj and dropObj parent
      return !this.canMoveInto(dragId, dropParentId) && !this.canMoveInto(dragId, dropId);
    } else {
      if($.inArray(dragRT, notToRoot) > -1 && !this.canMoveInto(dragId, dropId)) {
        return true;
      }
    }

    return false;
  },

  isChildrenOfObj: function(objId, dropId) {
    var rowIndex = this.getTableModel().getIndexManager().getIndex();
    var children = _.extend([], rowIndex[objId].children);

    while(children.length) {
      var child = children.pop();
      if(child.id == dropId) {
        return true;
      }
      children = children.concat(rowIndex[child.id].children);
    }
    return false;
  },

  canMoveInto: function(dragId, dropId) {
    var rowIndex = this.getTableModel().getIndexManager().getIndex();

    var dropRT = rowIndex[dropId].type;
    var dragRT = rowIndex[dragId].type;

    return $.inArray(dropRT, CellOperations.getCanMoveInto(dragRT)) > -1;
  },

  canMoveTo: function(dragId, dropId) {
    var rowIndex = this.getTableModel().getIndexManager().getIndex();

    var dropRT = rowIndex[dropId].type;
    var dragRT = rowIndex[dragId].type;

    return $.inArray(dropRT, CellOperations.getCanMoveTo(dragRT)) > -1;
  },

  renderColumn: function(tr, row, colIdx) {
    var tm = this.getTableModel();
    var ct = tm.getColumnTypes()[colIdx];
    var _type = typeof ct == 'function' ? ct(row) : ct;

    if(!(typeof tm.getEditable() == 'undefined' ? false : tm.getEditable()[colIdx])) {
      _type = "Label";
    }

    var renderer = this.cellRendererPool[_type];
    if(!renderer) {
      var RendererClass = window[_type + "Renderer"];
      try {
        if(!RendererClass) {
          throw new Error("Undefined renderer class '" + _type + "Renderer" + "'");
        }

        renderer = new RendererClass(this);

        this.cellRendererPool[_type] = renderer;

      } catch (e) {
        this.logger.warn("Error creating renderer: " + e);
        renderer = new CellRenderer(this);
      }
    }

    var myself = this;
    var options = {
      tooltip: ""
    };

    if(_type === "Label") {
      options.tooltip = row.tooltip;
    }

    return renderer.render(tr, tm.getColumnGetExpressions()[colIdx](row), function(value) {
      var oldValue = row.value;
      if(oldValue != value) {
        var command = new ChangePropertyCommand(myself, row, value);
        Commands.executeCommand(command);
      }

      // Renderer this column
      tr.find("td:eq(" + colIdx + ")").remove();
      myself.renderColumn(tr, row, colIdx);
    }, options);

  },

  renderColumnByRow: function(row, colIdx) {

    if(typeof colIdx == "undefined") {
      colIdx = 1;
    }

    var rowIdx = this.getTableModel().getRowIndexByName(row.name);
    var tr = $("#" + this.getId()).find("tbody > tr:eq(" + rowIdx + ")");
    tr.find("td:eq(1)").remove();
    this.renderColumn(tr, row, colIdx);

  },

  updateTreeTable: function(rowId) {

    if(rowId != IndexManager.ROOTID) {

      var _parentQ = $('#' + this.getTableId() + " > tbody > tr#" + rowId);
      _parentQ.removeClass("initialized");
      _parentQ.removeClass("parent");
      $("> td > span.expander", _parentQ).remove();
      _parentQ.initializeTreeTableNode();
      _parentQ.expand();
    }

  },

  insertAtIdx: function(_stub, insertAtIdx) {

    // Insert it on the dataModel
    this.getTableModel().getData().splice(insertAtIdx, 0, _stub);
    this.getTableModel().getIndexManager().updateIndex();
    var newId = this.addRow(_stub, insertAtIdx);

    // Update treeTable:
    this.updateTreeTable(_stub.parent);

    // focus the newly created line
    this.selectCell(insertAtIdx, 1, 'simple');

  },

  createOrGetParent: function(category, categoryDesc) {
    // Does this exist? If yes, return the last position
    var indexManager = this.getTableModel().getIndexManager();
    var cat = indexManager.getIndex()[category];
    if(typeof cat == 'undefined') {
      // Create it and return the last idx
      var _stub = {
        id: category,
        name: categoryDesc,
        type: "Label",
        typeDesc: "<i>Group</i>",
        parent: IndexManager.ROOTID,
        properties: [
          {
            name: "Group",
            description: "Group",
            value: categoryDesc,
            type: "Label"
          }
        ]
      };
      insertAtIdx = this.getTableModel().getData().length;
      this.insertAtIdx(_stub, insertAtIdx);
      return insertAtIdx + 1;

    } else {
      // Append at the end
      return cat.index + cat.children.length + 1;
    }
  },

  updateOperations: function() {

    var operations = this.getOperations();
    this.logger.debug("Found " + operations.length + " operations for this cell");

    if(operations.length) {
      var _opsNode = $("#" + this.getTableId() + "Operations");
      _opsNode.empty();

      var myself = this;
      $.each(operations, function(i, _operation) {
        if(typeof _operation != 'undefined') {
          _opsNode.append(_operation.getHtml(myself, i));
        }
      });
    }
  },

  cellClick: function(row, col, classType) {
    // Update operations
    if(typeof this.getLinkedTableManager() != 'undefined') {
      this.getLinkedTableManager().cellUnselected();
    }

    this.isSelectedCell = true;
    this.isSelectedGroupCell = this.getTableModel().getEvaluatedRowType(row) === 'Label';
    this.selectedCell = [row, col];
    this.updateOperations();
    this.fireDependencies(row, col, classType);

    $('#' + this.getId()).addClass('selectedTable');
  },

  cellUnselected: function() {
    this.isSelectedCell = false;
    this.isSelectedGroupCell = false;
    this.selectedCell = [];
    this.cleanSelections();
    this.updateOperations();
    this.cleanDependencies();
    if(typeof this.getLinkedTableManager() != 'undefined') {
      this.getLinkedTableManager().cellUnselected();
    }
  },

  selectCell: function(row, col, classType) {

    // Unselect
    this.cleanSelections();
    var $table = $('#' + this.getTableId());
    $table.click();
    $table.find("tbody > tr:eq(" + row + ")").addClass("ui-state-active");
    this.scrollTo(row);

    // Uncomment following cells to enable td highlight
    //$('#'+this.getTableId() + " > tbody > tr:eq("+ row +") > td:eq("+ col + ")").addClass("ui-state-active");

    // Fire cellClicked; get id
    this.cellClick(row, col, classType);

  },

  scrollTo: function(row) {
    function getPosition(element) {
      var top = element.position().top;
      var bottom = top + element.outerHeight(true);

      return {top: top, bottom: bottom};
    }

    var rowId = this.getTableModel().getEvaluatedId(row),
        $row = $('#' + rowId),
        $scroll = $('#' + this.getId() + ' .scrollContainer'),
        needScrollDown = true,
        needScrollUp = true;

    while(needScrollDown || needScrollUp) {
      var scrollTo = $scroll.scrollTop(),
          $row_position = getPosition($row),
          $scroll_position = getPosition($scroll);

      needScrollDown = $row_position.bottom > $scroll_position.bottom;
      needScrollUp = $row_position.top < $scroll_position.top;

      if(needScrollDown) {
        scrollTo += $row.outerHeight(true);
      }

      if(needScrollUp) {
        scrollTo -= $row.outerHeight(true);
      }

      $scroll.scrollTop(scrollTo);
    }
  },

  selectCellBefore: function() {
    if(this.isSelectedCell) {
      var tableModel = this.getTableModel();
      var indexManager = tableModel.getIndexManager();
      var rowIdx = this.getSelectedCell()[0];
      var rowId = tableModel.getEvaluatedId(rowIdx);
      var prevIdx;

      if(indexManager.isRootFirstChild(rowId)) {
        this.selectCell(0, 0, 'simple');
        return undefined;
      }

      var prevRow = indexManager.getPreviousSibling(rowId);

      if(prevRow != undefined) {
        var id = prevRow.id;
        while(indexManager.isParent(id) && $('#' + id).hasClass('expanded')) {
          prevRow = indexManager.getLastChild(id, 1);
          id = prevRow.id;
        }
        prevIdx = prevRow.index;
      } else {
        prevIdx = rowIdx - 1;
      }

      this.logger.debug('Moving row from ' + rowIdx + ' to ' + prevIdx);
      this.selectCell(prevIdx, 0, 'simple');
    } else if($('#' + this.getTableId() + ' tbody tr').length) {
      this.selectCell(0, 0, 'simple');
    }
  },

  selectCellAfter: function() {
    if(this.isSelectedCell) {
      var tableModel = this.getTableModel();
      var indexManager = tableModel.getIndexManager();
      var rowIdx = this.getSelectedCell()[0];
      var rowId = tableModel.getEvaluatedId(rowIdx);
      var isCollapsed = $('#' + rowId).hasClass('collapsed');
      var isParent = indexManager.isParent(rowId);
      var nextIdx;

      if((isParent && isCollapsed && indexManager.isRootLastChild(rowId))) {
        return undefined;
      }

      if(tableModel.isLastRow(rowId)) {
        this.selectCell(rowIdx, 0, 'simple');
        return undefined;
      }

      var nextRow = indexManager.getNextSibling(rowId);

      if(isParent && isCollapsed) {
        var id = rowId;
        while(nextRow == undefined) {
          id = indexManager.getParent(id).id;
          nextRow = indexManager.getNextSibling(id);
        }
        nextIdx = nextRow.index;
      } else {
        nextIdx = rowIdx + 1;
      }

      this.logger.debug('Moving row from ' + rowIdx + ' to ' + nextIdx);
      this.selectCell(nextIdx, 0, 'simple');
    } else if($('#' + this.getTableId() + ' tbody tr').length) {
      this.selectCell(0, 0, 'simple');
    }
  },

  expandCell: function() {
    if(this.isSelectedCell) {
      var rowIdx = this.getSelectedCell()[0];
      var rowId = this.getTableModel().getEvaluatedId(rowIdx);

      var row = $("#" + rowId);

      if(row.hasClass('parent') && row.hasClass('collapsed')) {
        row.toggleBranch();
        this.logger.debug('Expanding row on position ' + rowIdx);
      }

    } else if($('#' + this.getTableId() + ' tbody tr').length) {
      this.selectCell(0, 0, 'simple');
    }
  },

  collapseCell: function() {
    if(this.isSelectedCell) {
      var rowIdx = this.getSelectedCell()[0];
      var rowId = this.getTableModel().getEvaluatedId(rowIdx);

      var row = $("#" + rowId);

      if (row.hasClass('parent') && row.hasClass('expanded')) {
        row.toggleBranch();
        this.logger.debug('Collapsing row on position ' + rowIdx);
      }

    } else if($('#' + this.getTableId() + ' tbody tr').length) {
        this.selectCell(0, 0, 'simple');
    }
  },

  cleanSelections: function() {

    $('#' + this.getTableId()).find("tr.ui-state-active").removeClass("ui-state-active"); // Deselect currently ui-state-active rows

    // Uncomment following cells to enable td highlight
    //$('#'+this.getTableId()).find("tr td.ui-state-active").removeClass("ui-state-active"); // Deselect currently ui-state-active rows
  },

  fireDependencies: function(row, col, classType) {
    if(typeof this.getLinkedTableManager() != 'undefined') {

      var data = this.getLinkedTableManagerOperation()(this.getTableModel().getData()[row], classType);

      var tableManager = this.getLinkedTableManager();

      tableManager.getTableModel().setData(data);
      tableManager.cleanSelections();
      tableManager.init();
    }
  },

  cleanDependencies: function() {
    if(typeof this.getLinkedTableManager() != 'undefined') {
      var tableManager = this.getLinkedTableManager();

      tableManager.getTableModel().setData([]);
      tableManager.cleanSelections();
      tableManager.init();
    }
  },

  extendProperties: function(row, stub) {
    // 1 - get names in `row`
    // 2 - get names in `stub`
    // 3 - add to `row` new ones from `stub`
    var pRow = {};
    var rowProps = row.properties || (row.properties = []);

    // Index names of properties already in `row`
    $.each(rowProps, function(i, p) {
      pRow[p.name] = p;
    });

    $.each(stub.properties, function(i, s) {
      if(!pRow[s.name]) {
        rowProps.push(s);
      }
    });

    // Sort properties again, by #order
    // With the exceptions:
    // * The "name" property, which is forced to take the first place.
    // * V1 - properties are all placed alphabetically between
    //        standard component props and V2 props
    // * ?  - last ones
    rowProps.sort(function(p1, p2) {
      if(p1.name === 'name') {
        return -1;
      }
      if(p2.name === 'name') {
        return  1;
      }

      var p1Desc = p1.description;
      var p2Desc = p2.description;

      var p1Undef = p1Desc.charAt(0) === '?';
      var p2Undef = p2Desc.charAt(0) === '?';

      if(p1Undef) {
        if(!p2Undef) {
          return +1;
        }
      } else if(p2Undef) {
        if(!p1Undef) {
          return -1;
        }
      }

      var p1V1 = !!CDFDD.DISCONTINUED_PROP_PATTERN.exec(p1Desc);
      var p2V1 = !!CDFDD.DISCONTINUED_PROP_PATTERN.exec(p2Desc);
      if(p1V1 && p2V1) {
        return p1Desc < p2Desc ? -1 :
                p1Desc > p2Desc ? 1 : 0;
      }

      if(p1V1) {
        // All standard properties (that are maintained)
        return p2.order < 100 ? 1 : -1;
      }

      if(p2V1) {
        // All standard properties (that are maintained)
        return p1.order < 100 ? -1 : 1;
      }

      // Order
      return p1.order < p2.order ? -1 :
              p1.order > p2.order ? 1 :
                p1.name.localeCompare(p2.name);
    });
  },


  // Accessors
  setId: function(id) {
    this.id = id;
  },
  getId: function() {
    return this.id;
  },
  setTitle: function(title) {
    this.title = title;
  },
  getTitle: function() {
    return this.title;
  },
  setTableId: function(tableId) {
    this.tableId = tableId;
  },
  getTableId: function() {
    return this.tableId;
  },
  setTableModel: function(tableModel) {
    this.tableModel = tableModel;
  },
  getTableModel: function() {
    return this.tableModel;
  },
  setOperations: function(operations) {
    this.operations = operations;
  },
  getOperations: function() {
    return this.operations;
  },
  setSelectedCell: function(selectedCell) {
    this.selectedCell = selectedCell;
  },
  getSelectedCell: function() {
    return this.selectedCell;
  },
  setDroppedOnId: function(droppedOnId) {
    this.droppedOnId = droppedOnId;
  },
  getDroppedOnId: function() {
    return this.droppedOnId;
  },
  setLinkedTableManager: function(linkedTableManager) {
    this.linkedTableManager = linkedTableManager;
    linkedTableManager.parentTableManager = this;
  },
  getLinkedTableManager: function() {
    return this.linkedTableManager;
  },
  setLinkedTableManagerOperation: function(linkedTableManagerOperation) {
    this.linkedTableManagerOperation = linkedTableManagerOperation;
  },
  getLinkedTableManagerOperation: function() {
    return this.linkedTableManagerOperation;
  }

}, {
  tableManagers: {},

  register: function(tableManager) {
    TableManager.tableManagers[tableManager.getTableId()] = tableManager;
  },

  getTableManager: function(id) {
    return TableManager.tableManagers[id];
  },

  executeOperation: function(tableManagerId, idx) {

    var tableManager = TableManager.getTableManager(tableManagerId);
    var operation = tableManager.getOperations()[idx];
    var command = new RowOperationCommand(operation, tableManager);

    Commands.executeCommand(command);
  },

  globalInit: function() {

    // Enable the table selectors
    $(document).on("mousedown", "table.myTreeTable tbody tr td", function() {
      var myself = $(this);

      // get Current Id:
      var row = myself.parent().prevAll().length;
      var col = myself.prevAll().length;

      var wasSelected = myself.hasClass("selected");
      var _tableManager = TableManager.getTableManager(myself.closest("table").attr("id"));

      if(!wasSelected) {
        _tableManager.selectCell(row, col, 'simple');
      } else {
        _tableManager.cellUnselected();
      }

    });


    $(document).on("click", ".advancedProperties", function() {
      var tbody = $("#table-" + ComponentsPanel.PROPERTIES + " tbody");
      tbody.fadeOut(300);
      setTimeout(function() {
        var myself = $("#table-" + ComponentsPanel.COMPONENTS + " .ui-state-active td");
        if(myself.length > 0) {
          var row = myself.parent().prevAll().length;
          var col = myself.prevAll().length;
          var _tableManager = TableManager.getTableManager(myself.closest("table").attr("id"));
          _tableManager.selectCell(row, col, 'advanced');
          $(".advancedProperties").attr("class", "advancedProperties propertiesSelected");
          $(".advancedProperties").parent().find(".simpleProperties").attr("class", "simpleProperties propertiesUnSelected");

        }
      }, 500);
    });

    $(document).on("click", ".simpleProperties", function() {
      var tbody = $("#table-" + ComponentsPanel.PROPERTIES + " tbody");
      tbody.fadeOut(300);
      setTimeout(function() {
        var myself = $("#table-" + ComponentsPanel.COMPONENTS + " .ui-state-active td");
        if(myself.length > 0) {
          var row = myself.parent().prevAll().length;
          var col = myself.prevAll().length;
          var _tableManager = TableManager.getTableManager(myself.closest("table").attr("id"));
          _tableManager.selectCell(row, col, 'simple');
          $(".advancedProperties").attr("class", "advancedProperties propertiesUnSelected");
          $(".advancedProperties").parent().find(".simpleProperties").attr("class", "simpleProperties propertiesSelected");
        }

      }, 500);

    });

  },

  S4: function() {
    return (((1 + Math.random()) * 0x10000) | 0).toString(16).substring(1);
  },

  generateGUID: function() {
    return (TableManager.S4() + TableManager.S4() + "-" + TableManager.S4() +
        "-" + TableManager.S4() + "-" + TableManager.S4() +
        "-" + TableManager.S4() + TableManager.S4() + TableManager.S4());
  }

});


var TableModel = Base.extend({

  id: "row.id",
  logger: {},
  data: [],
  indexManager: {},
  columnId: undefined,
  columnNames: [],
  columnGetExpressions: [],
  columnTypes: [],
  columnSizes: undefined,
  editable: undefined,
  columnSetExpressions: [],
  rowId: "row.id",
  parentId: undefined,
  rowType: "row.type",

  constructor: function(id) {
    this.logger = new Logger("TableModel" + id);
    this.id = id;
    this.setIndexManager(new IndexManager(this));

    this.init();
  },

  getEvaluatedId: function(rowNumber) {
    try {
      var row = this.data[rowNumber];
      return this.getRowId()(row);
    } catch (e) {
      this.logger.error("Error getting id " + e);
    }
  },

  getRowByName: function(name) {
    var row;
    $.each(this.data, function(i, r) {
      if(r.name == name) {
        row = r;
        return;
      }
    });
    return row;
  },


  getRowIndexByName: function(name) {
    var idx;
    $.each(this.data, function(i, r) {
      if(r.name == name) {
        idx = i;
        return false;
      }
    });
    return idx;
  },

  getEvaluatedRowType: function(rowNumber) {
    try {
      var row = this.data[rowNumber];
      return this.getRowType()(row);
    } catch (e) {
      this.logger.error("Error getting row type: " + e);
    }
  },

  isFirstRow: function(rowId) {
    return this.data[0].id == rowId;
  },

  isLastRow: function(rowId) {
    return this.data[this.data.length - 1].id == rowId;
  },

  init: function() {
    // Do nothing
  },

  setId: function(id) {
    this.id = id;
  },
  getId: function() {
    return this.id;
  },
  setData: function(data) {
    this.data = data;
    this.getIndexManager().updateIndex();
  },
  getData: function() {
    return this.data;
  },
  setIndexManager: function(indexManager) {
    this.indexManager = indexManager;
  },
  getIndexManager: function() {
    return this.indexManager;
  },
  setColumnNames: function(columnNames) {
    this.columnNames = columnNames;
  },
  getColumnNames: function() {
    return this.columnNames;
  },
  setColumnGetExpressions: function(columnGetExpressions) {
    this.columnGetExpressions = columnGetExpressions;
  },
  getColumnGetExpressions: function() {
    return this.columnGetExpressions;
  },
  setColumnSetExpressions: function(columnSetExpressions) {
    this.columnSetExpressions = columnSetExpressions;
  },
  getColumnSetExpressions: function() {
    return this.columnSetExpressions;
  },
  setColumnTypes: function(columnTypes) {
    this.columnTypes = columnTypes;
  },
  getColumnTypes: function() {
    return this.columnTypes;
  },
  setColumnSizes: function(columnSizes) {
    this.columnSizes = columnSizes;
  },
  getColumnSizes: function() {
    return this.columnSizes;
  },
  setEditable: function(editable) {
    this.editable = editable;
  },
  getEditable: function() {
    return this.editable;
  },
  setRowId: function(rowId) {
    this.rowId = rowId;
  },
  getRowId: function() {
    return this.rowId;
  },
  setRowType: function(rowType) {
    this.rowType = rowType;
  },
  getRowType: function() {
    return this.rowType;
  },
  setParentId: function(parentId) {
    this.parentId = parentId;
  },
  getParentId: function() {
    return this.parentId;
  }
});


// Properties Table Model
var PropertiesTableModel = TableModel.extend({

  constructor: function(id) {
    this.logger = new Logger("TableModel" + id);
    this.id = id;
    this.setIndexManager(new IndexManager(this));

    this.setColumnNames(['Property', 'Value']);
    this.setColumnGetExpressions([function(row) {
      return row.description;
    }, function(row) {
      return row.value;
    }]);
    this.setColumnSetExpressions([undefined, function(row, value) {
      row.value = value;
    }]);
    this.setColumnTypes(['String', function(row) {
      return row.type;
    }]);
    this.setColumnSizes(['40%', '60%']);
    this.setEditable([false, true]);
    this.setRowId(function(row) {
      var rowId;
      if(row.id == null) {
        rowId = TableManager.generateGUID();
        row.id = rowId;
      } else {
        rowId = row.id;
      }
      return rowId;
    });
    this.setRowType(function(row) {
      return row.type;
    });

    this.init();
  }
});


var CellRenderer = Base.extend({

  logger: {},
  tableManager: null,

  constructor: function(tableManager) {
    this.logger = new Logger("CellRenderer");
    this.logger.debug("Creating new CellRenderer");
    this.tableManager = tableManager;
  },

  // Defaults to a common string type
  //  render: function(row,placeholder, getExpression,setExpression,editable){

  render: function(placeholder, value, callback, options) {
    $("<td>" + value + "</td>").appendTo(placeholder);
  },

  getTableManager: function() {
    return this.tableManager;
  },

  notificationPopup: function(title, message) {
    // this is being triggered twice, we don't want to show a popup if a popup is already shown
    if ( $("#popupbox").length > 0) {
      return false;
    }
    var popupHeader = '' +
      '<div class="popup-header-container">\n' +
      '  <div class="popup-title-container">' + title + '</div>\n' +
      '</div>\n';
    var popupBody = '' +
      '<div class="popup-body-notification">\n' +
      '  <div class="popup-body-header clearfix">' + message + '  </div>' +
      '</div>';
    var htmlContent = $('<div>')
        .append(popupHeader + popupBody)
        .html();

    $.prompt(htmlContent, {
      buttons: {
        Ok: false
      },
      prefix: "popup",
      loaded: function() {
        CDFDDUtils.movePopupButtons($(this));
      }
    });
  }
});


var LabelRenderer = CellRenderer.extend({

  constructor: function(tableManager) {
    this.base(tableManager);
    this.logger = new Logger("LabelRenderer");
    this.logger.debug("Creating new LabelRenderer");
  },

  render: function(placeholder, value, callback, options) {
    var tooltip = options && options.tooltip;
    if(tooltip) {
      $('<td title="' + Dashboards.escapeHtml(tooltip) + '">' + value + '</td>').appendTo(placeholder);
    } else {
      this.base.apply(this, arguments);
    }
  }
});


var StringRenderer = CellRenderer.extend({

  constructor: function(tableManager) {
    this.base(tableManager);
    this.logger = new Logger("StringRenderer");
    this.logger.debug("Creating new StringRenderer");
  },

  render: function(placeholder, value, callback) {

    var _editArea = $("<td>" + value + "</td>");
    var myself = this;
    _editArea.editable(function(value, settings) {
      myself.logger.debug("Saving new value: " + value);
      callback(value);

      return value;
    }, {
      cssclass: "cdfddInput",
      select: true,
      onblur: "submit",
      onsubmit: function(settings, original) {
        return myself.validate($('input', this).val());
      }
    });
    _editArea.appendTo(placeholder);

  },

  validate: function(settings, original) {
    return true;
  }
});

var IdRenderer = StringRenderer.extend({

  constructor: function(tableManager) {
    this.base(tableManager);
    this.logger = new Logger("IdRenderer");
    this.logger.debug("Creating new IdRenderer");
  },

  validate: function(value) {

    if(cdfdd.dashboardWcdf.widget) {
      if(!value.match(/^[\${}:a-zA-Z0-9_.]*$/)) {
        this.notificationPopup('Invalid Input', 'Argument ' + value + ' invalid. Can only contain alphanumeric characters, the special _ and . characters and the {p:name} construct.');
        return false;
      }
    } else if(!value.match(/^[a-zA-Z0-9_.]*$/)) {
      this.notificationPopup('Invalid Input', 'Argument ' + value + ' invalid. Can only contain alphanumeric characters and the special _ and . characters');
      return false;
    }
    return true;
  }
});


var IntegerRenderer = StringRenderer.extend({

  constructor: function(tableManager) {
    this.base(tableManager);
    this.logger = new Logger("IntegerRenderer");
    this.logger.debug("Creating new IntegerRenderer");
  },

  validate: function(value) {

    if(!value.match(/^[-]?\d*$/)) {
      this.notificationPopup('Invalid Input', 'Argument ' + value + ' must be numeric');
      return false;
    }
    return true;
  }
});

var FloatRenderer = StringRenderer.extend({

  constructor: function(tableManager) {
    this.base(tableManager);
    this.logger = new Logger("FloatRenderer");
    this.logger.debug("Creating new FloatRenderer");
  },

  validate: function(value) {

    if(!value.match(/^[-]?\d*\.?\d*$/)) {
      this.notificationPopup('Invalid Input', 'Argument ' + value + ' must be numeric');
      return false;
    }
    return true;
  }
});

var SelectRenderer = CellRenderer.extend({

  //if has autocomplete behavior, i.e. filter selectable values by what's being typed
  isAutoComplete: true,

  //selectable values to display
  selectData: {},
  revertedSelectData: {},
  autocompleteArray: [],

  constructor: function(tableManager) {
    this.base(tableManager);
    this.logger = new Logger("SelectRenderer");
    this.logger.debug("Creating new SelectRenderer");

    this.getDataInit();
  },

  render: function(placeholder, value, callback) {

    var data = this.processData() || this.getData();
    var label;
    if(value == null) {
      label = "";
    } else {
      label = (!this.isArray && typeof data[value] !== "undefined") ?
          data[value] :
          value;
    }

    var _editArea = $("<td>" + label + "</td>");
    data = null;

    var myself = this;

    _editArea.editable(function(value /*,settings*/) {
      var valueId;
      if(!myself.isArray && Object.prototype.hasOwnProperty.call(myself.revertedSelectData, value)) {
        valueId = myself.revertedSelectData[value];
      } else {
        valueId = value;
      }

      myself.logger.debug("Saving new value: " + valueId);
      callback(valueId);
      myself.postChange(valueId);
      return value;
    }, {
      type: "autocomplete",
      tooltip: "Click to edit...",
      onblur: "submit",
      autocomplete: {
        source: function(req, add) {
          myself.autoCompleteRequest(req, add);
        },
        minLength: 0,
        focus: function(event, data2) {
          if(data2 != undefined) { $('input', _editArea).val(data2.item.value); }
        },
        delay: this.getDelay()
      },
      onsubmit: function(/*settings, original*/) {
        return myself.validate($('input', this).val());
      },
      height: 12
    });

    _editArea.appendTo(placeholder);
  },

  autoCompleteRequest: function(req, add) {
    if(this.isAutoComplete) {
      add(jQuery.grep(this.autocompleteArray, function(elt/*, i*/) {
        var target = $.isArray(elt) ? elt[1] : elt;
        return target.toLowerCase().indexOf(req.term.toLowerCase()) == 0;
      }));
    } else {
      add(this.autocompleteArray);
    }
  },

  getDelay: function() {
    return 300;
  },

  processData: function() {
    var data = this.getData();
    this.isArray = $.isArray(data);
    if(this.isArray) {
      this.autocompleteArray = data;
    } else {
      // Get the correct values and inverting the selectData
      this.autocompleteArray = [];
      this.revertedSelectData = {};
      for(var id in data) {
        if(data.hasOwnProperty(id)) {
          var label = data[id];
          this.autocompleteArray.push(label);
          this.revertedSelectData[label] = id;
        }
      }
    }

    return data;
  },

  validate: function(/*settings, original*/) {
    return true;
  },

  getData: function() {
    // Default implementation
    return this.selectData;
  },

  getDataInit: function() {
    // Default implementation - do nothing
  },

  postChange: function() {
    // Default implementation - do nothing
  }
});

var ChartExportTypeRenderer = SelectRenderer.extend({
  selectData: {
    'png': 'png',
    'svg': 'svg'
  }
});

var DataExportTypeRenderer = SelectRenderer.extend({
  selectData: {
    'xls': 'xls',
    'csv': 'csv',
    'xml': 'xml',
    'json': 'json'
  }
});

var ComponentToExportRenderer = SelectRenderer.extend({

  getData: function() {
    var data = _.extend({}, this.selectData);
    var components = cdfdd.dashboardData.components.rows;

    var validComponents = this.filterComponents(components);

    validComponents.map(function(comp) {
      var compName = comp.properties[0].value;
      data[compName] = compName;
    });

    return data;
  },

  filterComponents: function(components) {
    return components.filter(function(comp) {
      var isNameEmpty = comp.properties && comp.properties[0].value === "";
      var hasCdwSupport = comp.meta_cdwSupport === 'true';
      var isTableComponent = comp.type === 'ComponentsTable';

      return (hasCdwSupport || isTableComponent) && !isNameEmpty;
    });
  }
});

var ChartComponentToExportRenderer = ComponentToExportRenderer.extend({

  prevSelectedValue: '',

  filterComponents: function(components) {
    return components.filter(function(comp) {
      var isNameEmpty = comp.properties && comp.properties[0].value === "";
      var hasCdwSupport = comp.meta_cdwSupport === 'true';

      return hasCdwSupport && !isNameEmpty;
    });
  },

  postChange: function(componentName) {
    var components = cdfdd.dashboardData.components.rows;
    var myself = this;

    if(this.prevSelectedValue != componentName) {
      components.map(function(comp) {
        if(comp.properties[0].value == componentName) {
          comp.meta_cdwRender = 'true';
        }

        if(comp.properties[0].value == myself.prevSelectedValue) {
          comp.meta_cdwRender = 'false';
        }
      });
      this.prevSelectedValue = componentName;
    }
  }
});

var BooleanRenderer = SelectRenderer.extend({

  isAutoComplete: false,

  selectData: {
    'true': 'True',
    'false': 'False'
  }
});

var SelectMultiRenderer = CellRenderer.extend({

  value: null,

  constructor: function(tableManager) {
    this.base(tableManager);
    this.logger = new Logger("SelectMultiRenderer");
    this.logger.debug("Creating new SelectMultiRenderer");
  },

  render: function(placeholder, value, callback) {

    this.value = value;
    var myself = this;
    var _editArea = $("<td>" + myself.getFormattedValue(value) + "</td>");
    _editArea.editable(function(value, settings) {

      var selector = $(this);
      var val = selector.find("input").val();
      if(typeof myself.postProcessValue == "function") {
        val = myself.postProcessValue(val);
      }
      var value = "['" + val.replace(/, /g, "','") + "']";
      if(value == "['${p:Select options}']") {
        value = "[]";
      }
      myself.logger.debug("Saving new value: " + value);
      callback(value);

      return value;
    }, {
      cssclass: "cdfddInput",
      data: this.getData(),
      type: 'selectMulti',
      submit: 'OK',
      height: 12,
      onsubmit: function(settings, original) {
        return myself.validate($('input', this).val());
      }
    });
    _editArea.appendTo(placeholder);
  },

  validate: function(settings, original) {
    return true;
  },

  getData: function() {
    return '{"A": "Alpha","B":"Beta"}';
  },

  getFormattedValue: function(value) {
    return value;
  }
});


var RoundCornersRenderer = SelectRenderer.extend({

  selectData: {
    '': 'Simple',
    'cdfdd-round': 'Round',
    'cdfdd-bevel': 'Bevel',
    'cdfdd-notch': 'Notch',
    'cdfdd-bite': 'Bite',
    'cdfdd-bevel_top': 'Top Bevel',
    'cdfdd-dog_tr': 'Dog TR'
  }
});


var TextAlignRenderer = SelectRenderer.extend({

  selectData: {
    'left': 'Left',
    'center': 'Center',
    'right': 'Right'
  }
});


var ColorRenderer = CellRenderer.extend({

  constructor: function(tableManager) {
    this.base(tableManager);
    this.logger = new Logger("ColorRenderer");
    this.logger.debug("Creating new ColorRenderer");
    this.id = 0;
  },

  getId: function() {
    return this.id++;
  },

  render: function(placeholder, value, callback) {

    this.placeholder = placeholder;

    var id = this.getId();
    var inputId = "#colorpicker_input_" + id;
    var checkId = "#colorpicker_check_" + id;
    var _editArea = $('<td><form onsubmit="return false" class="cdfddInput"><input id="colorpicker_check_' + id + '" class="colorcheck" type="checkbox"></input><input id="colorpicker_input_' + id + '" class="colorinput" type="text" size="7" maxlength="7"></input></form></td>');
    var myself = this;
    var fixHex = function(hex) {
      var length = 6 - hex.length;
      //account for 3 digits color codes
      if(length == 3) {
        var r = hex.charAt(0),
            g = hex.charAt(1),
            b = hex.charAt(2);
        hex = r + r + g + g + b + b;
      } else if(length > 0) {
        for(var i = 0; i < length; i++) {
          hex = "0".concat(hex);
        }
      }
      if(/(^#[0-9A-F]{6}$)|(^#[0-9A-F]{3}$)/i.test("#" + hex)) {
        return hex.toLowerCase();
      } else {
        return "";
      }
    };
    $(checkId, _editArea).bind("click", function() {

      if($(this).is(":checked")) {
        $(inputId, _editArea).attr("disabled", false);
        $(inputId).trigger("click").focus();
      } else {
        $(inputId, _editArea).val("");
        $(inputId, _editArea).attr("disabled", true);
        callback("");
      }
    });
    this.updateValueState(value, _editArea, inputId, checkId);
    $(inputId, _editArea).ColorPicker({
      onSubmit: function(hsb, hex, rgb, el) {
        $(el).val("#" + hex);
        $(el).ColorPickerHide();
        callback("#" + hex);
      },
      onBeforeShow: function() {
        $(this).ColorPickerSetColor(this.value.substring(1));
      }
    }).bind('keydown', function(evt) {
      $(this).ColorPickerShow();
      var fixedHex = fixHex(this.value.indexOf('#') > -1 ? this.value.substring(1) : this.value);
      $(this).ColorPickerSetColor(fixedHex);
      if(evt.keyCode == 13) {
        $(this).ColorPickerHide();
        if(fixedHex.length > 0) {
          fixedHex = "#".concat(fixedHex);
        } else {
          alert("Incorrect hex color code");
        }
        callback(fixedHex);
      }
      $(checkId, placeholder).attr("checked", "true");
    });
    _editArea.appendTo(placeholder);
  },

  updateValueState: function(value, placeholder, inputId, checkId) {
    // set checkbox and textarea state
    if(value == '') {
      $(checkId, placeholder).removeAttr("checked");
      $(checkId, placeholder).css("background-color", "#ffffff");
      $(inputId, placeholder).attr("disabled", true);
    } else {
      $(checkId, placeholder).attr("checked", "true");
      $(inputId, placeholder).removeAttr("disabled");
      $(inputId, placeholder).val(value);
    }

  }
});

var TextAreaRenderer = CellRenderer.extend({

  value: null,

  constructor: function(tableManager) {
    this.base(tableManager);
    this.logger = new Logger("TextAreaRenderer");
    this.logger.debug("Creating new TextAreaRenderer");
  },

  render: function(placeholder, value, callback) {

    // Storing the var for later use when render() is not called again
    this.value = value;

    var _editArea = $('<td><div style="float:left"><code></code></div><div class="edit" style="float:right"></div></td>');
    _editArea.find("code").text(this.getFormattedValue(value));
    var myself = this;
    var _prompt = $('<button class="cdfddInput">...</button>').bind("click", function() {

      var _inner = 'Edit<br /><textarea wrap="off" cols="80" class="cdfddEdit" name="textarea">' + myself.value + '</textarea>';
      // Store what we need in a global var
      cdfdd.textarea = [myself, placeholder, myself.value, callback];
      $.prompt(_inner, {
        buttons: {
          Ok: true,
          Cancel: false
        },
        callback: myself.callback,
        opacity: 0.2,
        prefix: 'popup'
      });
    }).appendTo($("div.edit", _editArea));

    _editArea.appendTo(placeholder);

  },

  callback: function(v, m, f) {
    if(v) {
      // set value. We need to add a space to prevent a string like function(){}
      // to be interpreted by json as a function instead of a string
      var value = f.textarea;
      this.value = value;
      if(value.length != 0 && value.substr(value.length - 1, 1) != " ") {
        value = value + " ";
      }
      cdfdd.textarea[3](value);
      $("code", cdfdd.textarea[1]).text(cdfdd.textarea[0].getFormattedValue(value));
    }
    delete cdfdd.textarea;
  },


  getFormattedValue: function(_value) {

    if(_value.length > 30) {
      _value = _value.substring(0, 20) + " (...)";
    }
    return _value;
  }
});


var CodeRenderer = CellRenderer.extend({

  value: null,
  editor: null,

  constructor: function(tableManager) {
    this.base(tableManager);
    this.logger = new Logger("CodeRenderer");
    this.logger.debug("Creating new CodeRenderer");
  },

  render: function(placeholder, value, callback) {

    // Storing the var for later use when render() is not called again
    this.value = value;

    var _editArea = $('<td><div style="float:left"><code></code></div><div class="edit" style="float:right"></div></td>');
    _editArea.find("code").text(this.getFormattedValue(value));
    var myself = this;
    var _prompt = $('<button class="cdfddInput">...</button>').bind("click", function() {
      var _inner = '' +
          '<div class="popup-header-container">\n' +
          '  <div class="popup-title-container">Edit</div>\n' +
          '</div>\n' +
          '<div class="popup-body-container">\n' +
          '  <pre id="codeArea" class="cdfddEdit" name="textarea"></pre>\n' +
          '</div>';
      // Store what we need in a global var
      cdfdd.textarea = [myself, placeholder, myself.value, callback];
      $.prompt(_inner, {
        buttons: {
          Ok: true,
          Cancel: false
        },
        loaded: function() {
          var $popup = $(this);

          $popup.addClass('edit-popup')
          CDFDDUtils.movePopupButtons($popup);

          //editor
          myself.editor = new CodeEditor();
          myself.editor.initEditor("codeArea");
          myself.editor.setTheme(null);//if null the default is used ("ace/theme/twilight" is the default)
          myself.editor.setMode(myself.getCodeType());
          myself.editor.setContents(myself.value);
        },

        callback: myself.callback,
        opacity: 0.2,
        prefix: 'popup'
      });


    }).appendTo($("div.edit", _editArea));

    _editArea.appendTo(placeholder);
  },

  callback: function(v, m, f) {
    if(v) {
      // set value. We need to add a space to prevent a string like function(){}
      // to be interpreted by json as a function instead of a string
      var value = cdfdd.textarea[0].editor.getContents();
      this.value = value;
      if(value.length != 0 && value.substr(value.length - 1, 1) != " ") {
        value = value + " ";
      }
      cdfdd.textarea[3](value);
      $("code", cdfdd.textarea[1]).text(cdfdd.textarea[0].getFormattedValue(value));
    }
    delete cdfdd.textarea;
  },

  getCodeType: function() {
    return 'javascript'; //TODO:
  },

  getFormattedValue: function(_value) {

    if(_value.length > 30) {
      _value = _value.substring(0, 20) + " (...)";
    }
    return _value;
  }
});


var EditExtensionPointsRenderer = CodeRenderer.extend({

  index: null,

  constructor: function(tableManager) {
    this.base(tableManager);
    this.logger = new Logger("EditExtensionPointsRender");
    this.logger.debug("Creating new EditExtensionPointsRender");
  },

  render: function(placeholder, value, callback) {

    // Storing the var for later use when render() is not called again
    this.value = value;
    if(placeholder.length) {
      this.index = placeholder[0].id.replace(/[a-zA-Z_]+/g, "");
    }

    var myself = this;
    var _inner = '' +
        '<div class="popup-header-container">\n' +
        '  <div class="popup-title-container">Edit</div>\n' +
        '</div>\n' +
        '<div class="popup-body-container">\n' +
        '  <pre id="codeArea" class="cdfddEdit" name="textarea"></pre>\n' +
        '</div>';
    // Store what we need in a global var
    cdfdd.textarea = [myself, callback];
    var prompt = $.prompt(_inner, {
      buttons: {
        Ok: true,
        Cancel: false
      },
      loaded: function() {
        var $popup = $(this);

        $popup.addClass('edit-popup')
        CDFDDUtils.movePopupButtons($popup);

        //editor
        myself.editor = new CodeEditor();
        myself.editor.initEditor("codeArea");
        myself.editor.setTheme(null);//if null the default is used ("ace/theme/twilight" is the default)
        myself.editor.setMode(myself.getCodeType());
        myself.editor.setContents(myself.value);
      },

      callback: myself.callback,
      opacity: 0.2,
      prefix: 'popup'
    });
    return prompt;
  },

  callback: function(v, m, f) {
    if(v) {
      var value = cdfdd.textarea[0].editor.getContents();
      var index = cdfdd.textarea[0].index;
      this.value = value;

      cdfdd.textarea[1](value, index);
    }
    delete cdfdd.textarea;
  }

});


var HtmlRenderer = CodeRenderer.extend({//TextAreaRenderer.extend({

  getCodeType: function() {
    return 'html';
  }
});

var ResourceRenderer = CodeRenderer.extend({ //TextAreaRenderer.extend({

  getCodeType: function() {
    var rtype = this.getTableManager().getTableModel().getRowByName("resourceType");
    if(rtype != null) {
      return rtype.value.toLowerCase();
    } else {
      return null;
    }
  }
});


var DateRenderer = CellRenderer.extend({

  callback: undefined,

  constructor: function(tableManager) {
    this.base(tableManager);
    this.logger = new Logger("DateRenderer");
    this.logger.debug("Creating new DateRenderer");
  },

  render: function(placeholder, value, callback) {

    this.callback = callback;

    var _editArea = $("<td>" + this.getFormattedValue(value) + "</td>");
    var myself = this;

    _editArea.editable(function(value, settings) {
      myself.logger.debug("Saving new value: " + value);
      if(value != 'pickDate') {
        callback(value);
      }

      return myself.getFormattedValue(value);
    }, {
      cssclass: "cdfddInput",
      data: this.getData(value),
      type: 'select',
      submit: 'OK',
      height: 12,
      onsubmit: function(settings, original) {
        var selectedValue = $(this.children()[0]).val();
        if(selectedValue == 'pickDate') {
          myself.pickDate($(this.children()[0]));
          return false;
        }
        return myself.validate();
      }
    });
    _editArea.appendTo(placeholder);
  },

  pickDate: function(input) {
    var myself = this;
    this.datePicker = $("<input/>").css("width", "80px");
    $(input).replaceWith(this.datePicker);
    this.datePicker.datepicker({
      dateFormat: 'yy-mm-dd',
      changeMonth: true,
      changeYear: true,
      onSelect: function(date, input) {
        myself.callback(date);
      }
    });
    this.datePicker.datepicker('show');
  },

  validate: function(settings, original) {
    return true;
  },

  getData: function(value) {
    var data = Panel.getPanel(ComponentsPanel.MAIN_PANEL).getParameters();
    var _str = "{'today':'Today','yesterday':'Yesterday','lastWeek':'One week ago','lastMonth':'One month ago','monthStart':'First day of month','yearStart':'First day of year','pickDate':'Pick Date', 'selected':'" + value + "'}";

    return _str;
  },

  getFormattedValue: function(selectedValue) {

    var date;

    if(selectedValue.match(/\d{4}-\d{2}-\d{2}/)) {
      var dateArray = selectedValue.split('-');
      // Date(d,m,y) expects month to be 0-11, date picker gives us 1-12
      date = new Date(dateArray[0], dateArray[1] - 1, dateArray[2]);
      return  this.toDateString(date);
    } else {
      if(selectedValue == 'pickDate') {
        return this.toDateString(this.datePicker.datepicker('getDate'));
      }
      return selectedValue;
    }
  },

  toDateString: function(d) {
    var currentMonth = "0" + (d.getMonth() + 1);
    var currentDay = "0" + (d.getDate());
    return d.getFullYear() + "-" + (currentMonth.substring(currentMonth.length - 2, currentMonth.length)) + "-" + (currentDay.substring(currentDay.length - 2, currentDay.length));
  }
});

var DateRangeRenderer = DateRenderer.extend({

  pickDate: function(input) {
    this.datePicker = $("<input/>").css("width", "80px");
    $(input).replaceWith(this.datePicker);

    var offset = this.datePicker.offset();
    var myself = this;

    var a = this.datePicker.daterangepicker({
      posX: offset.left - 400,
      posY: offset.top - 100,
      dateFormat: 'yy-mm-dd',
      onDateSelect: function(rangeA, rangeB) {
        myself.rangeA = rangeA;
        myself.rangeB = rangeB;
      }
    });

    this.datePicker.click();
  },

  getData: function(value) {
    var data = Panel.getPanel(ComponentsPanel.MAIN_PANEL).getParameters();
    var _str = "{'monthToDay':'Month to day','yearToDay':'Year to day','pickDate':'Pick Dates', 'selected':'" + (value) + "'}";

    return _str;
  },

  getFormattedValue: function(value) {
    var selectedValue = value;
    if(selectedValue == 'pickDate') {
      return  this.rangeA + " - " + this.rangeB;
    }

    var date = new Date();
    if(selectedValue == "monthToDay") {
      date.setDate(1);
    } else if(selectedValue == "yearToDay") {
      date.setMonth(0);
      date.setDate(1);
    }

    return  this.toDateString(date) + " " + this.toDateString(new Date());
  }
});


var ResourceFileRenderer = CellRenderer.extend({

  callback: null,

  editor: null,
  fileName: null,
  //if allows folder selection to create a new file
  createNew: true,

  renderEditorButton: function() {
    var myself = this;
    return $('<button class="cdfddInput">...</button>').click(function() {
      if(myself.fileName == null) { return; }
      myself.fileName = myself.getFileName($(".cdfdd-resourceFileNameRender:visible").text());
      var url = ExternalEditor.getEditorUrl() + "?path=" + myself.fileName + "&mode=" + myself.getResourceType();
      var _inner = "<iframe id=externalEditor src='" + url + "' width='800px' height='400px' ></iframe>";

      var contentWrapper = '' +
          '<div class="popup-header-container">\n' +
          '  <div class="popup-title-container">Edit</div>\n' +
          '</div>\n' +
          '<div class="popup-body-container">\n' + _inner + '</div>';

      var confirmWrapper = '' +
          '<div class="popup-header-container">\n' +
          '  <div class="popup-title-container">Confirm</div>\n' +
          '</div>\n' +
          '<div class="popup-body-container">\n' +
          '  <div ><span>Any unsaved changes will be lost. Continue anyway?</span></div>' +
          '</div>';

      // Store what we need in a global var
      var action;
      var extEditor = {

        edit: {// external editor
          html: contentWrapper,
          buttons: {
            'Open File in new Tab/Window': 'newtab',
            'Close': 'close'
          },
          opacity: 0.2,
          top: '40px',
          prefix: 'brownJqi',
          submit: function(val, msg, form) {
            action = val;
            var status = $('iframe#externalEditor').contents().find('#infoArea');
            if(status != null && status.text().indexOf('*') > -1) {
              $.prompt.goToState('confirm');
              return false;
            } else {
              if(action == 'newtab') {
                window.open(url);
              }
              return true;
            }

          }
        },

        confirm: {//confirm exit //TODO: style elsewhere
          html: confirmWrapper,
          buttons: { Yes: true, Cancel: false },
          submit: function(val, msg, form) {
            if(val) {
              if(action == 'newtab') {
                window.open(url);
              }
              //$.prompt.close();
              return true;
            } else {
              $.prompt.goToState('edit');
              return false;
            }
          }
        }
      };

      $.prompt(extEditor, {
        //opacity: 0.2,
        prefix: 'popup',
        loaded: function() {
          var $popup = $(this);
          $popup.addClass('external-editor-popup');
          CDFDDUtils.movePopupButtons($popup.find('#popup_state_edit'));
          CDFDDUtils.movePopupButtons($popup.find('#popup_state_confirm'));
        },
        top: '40px'

      });
    });
  },

  /////////////////
  constructor: function(tableManager) {
    this.base(tableManager);
    this.logger = new Logger("ResourceFileRenderer");
    this.logger.debug("Creating new ResourceFileRenderer");
  },

  render: function(placeholder, value, callback) {

    this.callback = callback;
    this.value = value;

    this.validate(value);

    var content = $('<td></td>');
    var _editArea = $('<div class="cdfdd-resourceFileNameRender" >' + value + '</div>');
    var _fileExplorer = $('<button class="cdfdd-resourceFileExplorerRender"> ^ </button>');
    var myself = this;

    var _prompt = this.renderEditorButton();

    content.append(_editArea);
    content.append(_fileExplorer);
    content.append(_prompt);

    _editArea.editable(function(value, settings) {
      myself.logger.debug("Saving new value: " + value);
      callback(value);
      return value;
    }, {
      cssclass: "cdfddInput",
      select: true,
      onsubmit: function(settings, original) {
        var value = $('input', this).val();
        myself.fileName = value;
        return myself.validate(value);
      }
    });

    var fileExtensions = this.getFileExtensions();
    _fileExplorer.bind('click', function() {

      var fileExplorerLabel = 'Choose existing file' + (myself.createNew ? ', or select a folder to create one' : '');
      var fileExplorerContent = '' +
        '<div class="popup-header-container">\n' +
        '  <div class="popup-title-container"></div>\n' +
        '</div>\n' +
        '<div class="popup-body-container">\n' +
        '  <div class="popup-label">' + fileExplorerLabel + '</div>\n' +
        '  <div id="container_id" class="urltargetfolderexplorer"></div>\n' +
        '</div>';

      var newFileContent = '' +
          '<div class="popup-header-container">\n' +
          '  <div class="popup-title-container">Create File</div>\n' +
          '</div>\n' +
          '<div class="popup-body-container layout-popup">\n' +
          '  <div class="popup-input-container bottom">\n' +
          '    <div class="popup-label">New File</div>\n' +
          '    <input class="popup-text-input" name="fileName"/>' +
          '  </div>\n' +
          '</div>';

      var selectedFile = "";
      var selectedFolder = "";

      var openOrNew = {
        browse: {// file explorer
          html: fileExplorerContent,
          buttons: {
            Ok: true,
            Cancel: false
          },
          opacity: 0.2,
          submit: function(v, m, f) {
            if(v) {
              if(selectedFile.length > 0) {
                myself.fileName = selectedFile;
                var file = myself.formatSelection(selectedFile);
                _editArea.text(file);
                myself.callback(file);
                return true;
              } else if(selectedFolder.length > 0) {
                if(myself.createNew) {
                  $.prompt.goToState('newFile');
                }
                return false;
              }
            }
            return true;
          }
        },

        newFile: {// new file prompt when folder selected
          html: newFileContent,
          buttons: {
            Ok: true,
            Cancel: false
          },
          submit: function(v, m, f) {
            if(v) {
              //new file
              selectedFile = selectedFolder + f.fileName;

              //check extension
              var ext = selectedFile.substring(selectedFile.lastIndexOf('.') + 1);
              if('.' + ext != myself.getFileExtensions()) {
                selectedFile += myself.getFileExtensions();
              }

              myself.fileName = selectedFile;
              var file = myself.formatSelection(selectedFile);
              _editArea.text(file);
              var params = {
                createNew: true,
                path: selectedFile,
                data: ""
              };
              SynchronizeRequests.createFile(params);
              myself.callback(file);
              return true;
            } else {
              $.prompt.goToState('browse');
              return false;
            }
          }
        }
      };

      $.prompt(openOrNew, {
        opacity: '0.2',
        prefix: "popup",
        loaded: function() {
          selectedFile = "";

          var $this = $(this);
          $this.addClass('choose-file-popup');
          CDFDDUtils.movePopupButtons($this.find('#popup_state_browse'));
          CDFDDUtils.movePopupButtons($this.find('#popup_state_newFile'));

          $('#container_id').fileTree({
                root: '/',
                script: SolutionTreeRequests.getExplorerFolderEndpoint(CDFDDDataUrl) + "?fileExtensions=" + fileExtensions + "&showHiddenFiles=true" + (CDFDDFileName != "" ? "&dashboardPath=" + CDFDDFileName : ""),
                expandSpeed: 1000,
                collapseSpeed: 1000,
                multiFolder: false,
                folderClick: function(obj, folder) {
                  if($(".selectedFolder").length > 0) { $(".selectedFolder").attr("class", ""); }
                  $(obj).attr("class", "selectedFolder");
                  selectedFolder = folder;//TODO:
                }
              },
              function(file) {
                selectedFile = file;
                $(".selectedFile").attr("class", "");
                $("a[rel='" + file + "']").attr("class", "selectedFile");
              });
        }
      });

    });

    content.appendTo(placeholder);

  },

  getResourceType: function() {
    var rtype = this.getTableManager().getTableModel().getRowByName("resourceType");
    if(rtype != null) {
      return rtype.value.toLowerCase();
    } else {
      return null;
    }

  },

  getFileExtensions: function() {
    return this.getResourceType() == "css" ? ".css" : ".js";
  },

  formatSelection: function(file) {
    var isSystem = false;
    var finalPath = "";
    var dashFile = CDFDDFileName;

    if(file.charAt(0) != '/') {
      file = "/" + file;
    }

    if(dashFile != null && dashFile.indexOf("/system") == 0) {
      var systemDir = "system";
      var pluginDir = dashFile.split('/')[2];
      file = "/" + systemDir + "/" + pluginDir + file;
      isSystem = true;
    }

    var common = true;
    var splitFile = file.split("/");
    if(dashFile == "") {
      //the path is forced to start by slash because the cde editor is called without name in the context of
      //creating a new dashboard in the solution repository. In this case all paths must be absolute. In system
      //dashboards, the file name must exists and start by /system, so this scenario is not applied
      finalPath = "/";
    }

    var splitPath = dashFile.split("/");
    var i = 0;

    while(common) {
      if(splitFile[i] !== splitPath[i]) {
        common = false;
      }
      i += 1;
    }

    $.each(splitPath.slice(i), function(i, j) {
      finalPath += "../";
    });

    finalPath += splitFile.slice(i - 1).join('/').replace(/\/+/g, "/");

    return this.giveContext(isSystem, finalPath);

  },

  giveContext: function(isSystem, path) {
    if(isSystem) {
      return "${system:" + path + "}";
    } else {
      return "${solution:" + path + "}";
    }
  },

  getFileName: function(settings) {
    var fileName;
    if(settings.indexOf('${res:') > -1 || settings.indexOf('${solution:') > -1) {
      var toReplace = settings.substring(0, settings.indexOf(':') + 1);
      fileName = settings.replace(toReplace, '').replace('}', '');

      if(fileName.charAt(0) != '/') { //relative path, append dashboard location
        fileName = this.getAbsoluteFileName(fileName);
      }

    } else if(settings.indexOf('${system:') > -1) {
      fileName = settings.replace('${system:', '').replace('}', '');

      if(fileName.charAt(0) != '/') { //relative path, append dashboard location
        fileName = this.getAbsoluteFileName(fileName);
      } else {
        fileName = "/system/" + CDFDDFileName.split('/')[2] + fileName;
      }

    } else if(settings != null && settings != '') { //needs a solution path
      fileName = settings;

    } else {
      fileName = null;

    }

    return fileName;
  },

  getAbsoluteFileName: function(fileName) {

    var basePath = CDFDDFileName;
    if(basePath == null) {
      this.fileName = null;
      return;
    }

    var lastSep = basePath.lastIndexOf('/');
    basePath = basePath.substring(0, lastSep);
    if(fileName.indexOf('..') > -1) {
      var base = basePath.split('/');
      var file = fileName.split('/');
      var baseEnd = base.length;
      var fileStart = 0;
      while(file[fileStart] == '..' && baseEnd > 0) {
        fileStart++;
        baseEnd--;
      }
      fileName = base.slice(0, baseEnd).concat(file.slice(fileStart)).join('/');
    } else {
      fileName = basePath + '/' + fileName;
    }
    return fileName.replace(/\/+/g, "/");
  },

  setFileName: function(settings) { //set .fileName if possible
    this.fileName = this.getFileName(settings);

  },

  validate: function(settings, original) {
    if(settings != '' && ( this.fileName == null || settings != this.formatSelection(this.fileName))) {
      this.setFileName(settings);//if set manually
    }
    return true;
  }
});
