/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


//ACE wrapper
var CodeEditor = Base.extend({

  MODES: {
    JAVASCRIPT: 'javascript',
    CSS: 'css',
    XML: 'xml'
  },
  MODE_BASE: 'ace/mode/',
  DEFAULT_MODE: 'text',

  modeMap: { //highlight modes
    'css': 'css',
    'javascript': 'javascript',
    'js': 'javascript',
    'xml': 'xml',
    'cda': 'xml',
    'cdv': 'javascript',
    'html': 'html',
    'sql': 'text',
    'mdx': 'text'
  },

  mode: 'javascript',
  theme: 'ace/theme/textmate',
  editor: null,
  editorId: null,

  initEditor: function(editorId) {
    this.editor = ace.edit(editorId);
    this.editorId = editorId;
    this.setMode(null);
    this.setTheme(null);
  },

  loadFile: function(fileName) {
    var myself = this;
    //check edit permission
    $.get(ExternalEditor.getCanEditUrl(), { path: fileName }, function(result) {
      var readonly = result != 'true';
      myself.setReadOnly(readonly);
      //TODO: can read?..get permissions?...

      //load file contents
      $.get(ExternalEditor.getGetUrl(), { path: fileName }, function(fileContents) {
        myself.setContents(fileContents);
      });
    });
  },

  setContents: function(contents) {
    this.editor.getSession().setValue(contents);
    $(this.editorId).css("font-size", "12px");
    this.editor.focus();
  },

  saveFile: function(fileName, contents, callback) {
    $.ajax({
      url: ExternalEditor.getWriteUrl(),
      type: "POST",
      dataType: "xml",
      data: {
        path: fileName,
        data: contents
      },
      complete: function(data) {
        if(typeof callback == 'function') {
          callback(data);
        }
      }
    });
  },

  getContents: function() {
    return this.editor.getSession().getValue();
  },

  setMode: function(mode) {
    this.mode = this.modeMap[mode];

    if(this.mode == null) {
      this.mode = this.DEFAULT_MODE;
    }

    if(this.editor != null) {
      if(this.mode != null) {
        var HLMode = ace.require(this.MODE_BASE + this.mode).Mode;
        this.editor.getSession().setMode(new HLMode());
      }
    }

  },

  setTheme: function(themePath) {
    this.editor.setTheme((themePath == null || themePath == undefined) ? this.theme : themePath);
  },

  setReadOnly: function(readOnly) {
    if(readOnly == this.editor.getReadOnly()) {
      return;
    } else {
      this.editor.setReadOnly(readOnly);
    }
  },

  isReadOnly: function() {
    return this.editor.getReadOnly();
  },

  insert: function(text) {
    this.editor.insert(text);
  },

  getEditor: function() {
    return this.editor;
  },

  onChange: function(callback) {
    this.editor.getSession().on('change', callback);
  }
});
