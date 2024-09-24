/*!
 * Copyright 2002 - 2017 Webdetails, a Hitachi Vantara company. All rights reserved.
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
