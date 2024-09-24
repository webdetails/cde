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

describe("Ace Wrapper Test #", function() {
  var editorId = "editDiv";
  var editDiv = undefined;

  beforeEach(function() {
    editDiv = $('<div id="' + editorId + '"></div>');
    editDiv.appendTo('body');
  });

  afterEach(function() {
    editDiv = undefined;
    $('#' + editorId).remove();
  });

  /*
   * Init Editor
   */
  it("initEditor", function() {
    var wrapper = new CodeEditor();
    wrapper.initEditor(editorId);

    expect($('#' + editorId).hasClass("ace_editor")).toBe(true);
    expect(wrapper.editorId).toEqual(editorId);
    expect(wrapper.mode).toEqual("text");
    expect(wrapper.theme).toEqual("ace/theme/textmate");
  });

  /*
   * Set Contents
   */
  it("setContents", function() {
    var contents = "Hello World";
    var wrapper = new CodeEditor();
    wrapper.initEditor(editorId);

    wrapper.setContents(contents);

    expect($("#" + editorId).css("font-size")).toEqual("12px");
    expect(wrapper.editor.isFocused()).toBe(true);
    expect(wrapper.editor.getSession().getValue()).toEqual(contents);
  });
});
