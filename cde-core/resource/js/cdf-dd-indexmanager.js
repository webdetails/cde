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

// IndexManager
var IndexManager = Base.extend({

  id: "",
  logger: {},
  index: {},

  constructor: function(tableModel) {
    this.logger = new Logger("IndexManager");
    this.setTableModel(tableModel);
  },

  updateIndex: function() {
    var data = this.getTableModel().getData();
    var idx = {};
    var index = 0;

    // Add root entry
    var root = {};
    root.id = IndexManager.ROOTID;
    root.parent = null;
    root.children = [];
    root.type = _type;
    root.index = index;
    idx[IndexManager.ROOTID] = root;

    for(var rowIndex in data) {
      if(data.hasOwnProperty(rowIndex)) {
        rowIndex = parseFloat(rowIndex);
        if(rowIndex != NaN) {
          var row = data[rowIndex];
          var _id = this.getTableModel().getEvaluatedId(rowIndex);
          var _parent;
          if(typeof this.getTableModel().getParentId() == 'undefined') {
            // Parent folder
            _parent = IndexManager.ROOTID;
          } else {
            // Child folder
            _parent = this.getTableModel().getParentId()(row);
            if(typeof _parent == 'undefined') {
              _parent = IndexManager.ROOTID;
            }
          }
          var _type = this.getTableModel().getRowType()(row);

          var entry = {};
          entry.id = _id;
          entry.parent = _parent;
          entry.children = [];
          entry.type = _type;
          entry.index = rowIndex;
          idx[_id] = entry;
          idx[_parent].children.push(entry);
        }

        index++;
      }
    }

    this.setIndex(idx);
  },

  isParent: function(rowId) {
    return !!this.getIndex()[rowId].children.length;
  },

  isFirstChild: function(rowId) {
    var parentId = this.getParent(rowId).id;

    return this.getIndex()[parentId].children[0].id === rowId;
  },

  isRootFirstChild: function(rowId) {
    var parentId = this.getParent(rowId).id;
    if(parentId === IndexManager.ROOTID) {
      return this.isFirstChild(rowId);
    } else {
      return false;
    }
  },

  isLastChild: function(rowId) {
    var parentId = this.getParent(rowId).id;
    var _children = this.getIndex()[parentId].children;

    return _children[_children.length-1].id === rowId;
  },

  isRootLastChild: function(rowId) {
    var parentId = this.getParent(rowId).id;
    if(parentId === IndexManager.ROOTID) {
      return this.isLastChild(rowId);
    } else {
      return false;
    }
  },

  getParent: function(rowId) {
    var index = this.getIndex();
    return index[index[rowId].parent];
  },

  getBrothers: function(rowId) {
    return this.getIndex()[this.getIndex()[rowId].parent].children;
  },

  getChildIndex: function(rowId) {

    var brothers = this.getBrothers(rowId);
    var idx = -1;
    $.each(brothers, function(i, brother) {
      if(brothers[i].id === rowId) {
        idx = i;
        return false;
      }
    });

    return idx;
  },

  getLastChild: function(rowId, depth) {
    var _children = this.getIndex()[rowId].children;
    var _length = _children.length;
    if(_length == 0 || depth == 0) {
      return this.getIndex()[rowId];
    } else {
      return this.getLastChild(_children[_length - 1].id, depth - 1);
    }
  },

  getPreviousSibling: function(rowId) {
    return this.getBrothers(rowId)[this.getChildIndex(rowId) - 1];
  },

  getNextSibling: function(rowId) {
    return this.getBrothers(rowId)[this.getChildIndex(rowId) + 1];
  },

  setIndex: function(index) {
    this.index = index;
  }, //XXX - index is an object!
  getIndex: function() {
    return this.index;
  },
  setTableModel: function(tableModel) {
    this.tableModel = tableModel;
    this.updateIndex();
  },
  getTableModel: function() {
    return this.tableModel;
  }
}, {
  ROOTID: "UnIqEiD"
});


