/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/
/* jQuery treeTable Plugin 2.2.1 - http://ludo.cubicphuse.nl/jquery-plugins/treeTable/ */
(function($) {
  // Helps to make options available to all functions
  // TODO: This gives problems when there are both expandable and non-expandable
  // trees on a page. The options shouldn't be global to all these instances!
  var options;
  
  $.fn.treeTable = function(opts) {
    options = $.extend({}, $.fn.treeTable.defaults, opts);
    
    return this.each(function() {
      $(this).addClass("treeTable").find("tbody tr").each(function() {
        // Initialize root nodes only whenever possible
        if(!options.expandable || $(this)[0].className.search("child-of-") == -1) {
          initialize($(this));
        }
      });
    });
  };
  
  $.fn.treeTable.defaults = {
    childPrefix: "child-of-",
    expandable: true,
    indent: 19,
    initialState: "collapsed",
    treeColumn: 0
  };
  
  // Recursively hide all node's children in a tree
  $.fn.collapse = function() {
    $(this).addClass("collapsed");

    childrenOf($(this)).each(function() {
      initialize($(this));
      
      if(!$(this).hasClass("collapsed")) {
        $(this).collapse();
      }
      
      $(this).hide();
    });
    
    return this;
  };
  
  // Recursively show all node's children in a tree
  $.fn.expand = function() {
    $(this).removeClass("collapsed").addClass("expanded");
    
    childrenOf($(this)).each(function() {
      initialize($(this));
            
      if($(this).is(".expanded.parent")) {
        $(this).expand();
      }
      
      $(this).show();
    });
    
    return this;
  };
  
  // Add an entire branch to +destination+
  $.fn.appendBranchTo = function(destination) {
    var node = $(this);
    var parent = parentOf(node);
    
    var ancestorNames = $.map(ancestorsOf($(destination)), function(a) { return a.id; });
      
    // Conditions:
    // 1: +node+ should not be inserted in a location in a branch if this would
    //    result in +node+ being an ancestor of itself.
    // 2: +node+ should not have a parent OR the destination should not be the
    //    same as +node+'s current parent (this last condition prevents +node+
    //    from being moved to the same location where it already is).
    // 3: +node+ should not be inserted as a child of +node+ itself.
    if($.inArray(node[0].id, ancestorNames) == -1 && (!parent || (destination.id != parent[0].id)) && destination.id != node[0].id) {
      indent(node, ancestorsOf(node).length * options.indent * -1); // Remove indentation
      
      if(parent) { node.removeClass(options.childPrefix + parent[0].id); }
      
      node.addClass(options.childPrefix + destination.id);
      move(node, destination); // Recursively move nodes to new location
      indent(node, ancestorsOf(node).length * options.indent);
    }

    return this;
  };
  
  // Add reverse() function from JS Arrays
  $.fn.reverse = function() {
    return this.pushStack(this.get().reverse(), arguments);
  };

  // Toggle an entire branch
  $.fn.toggleBranch = function() {
    if($(this).hasClass("collapsed")) {
      $(this).expand();
    } else {
      $(this).removeClass("expanded").collapse();
    }

    return this;
  };

  // Initialize node - public function
  $.fn.initializeTreeTableNode = function(){
	  initialize($(this));
  };

  // === Private functions
  
  function ancestorsOf(node) {
    var ancestors = [];
    while(node = parentOf(node)) {
      ancestors[ancestors.length] = node[0];
    }
    return ancestors;
  };
  
  function childrenOf(node) {
    return $("table.treeTable tbody tr." + options.childPrefix + node[0].id);
  };

  function indent(node, value) {
    var cell = $(node.children("td")[options.treeColumn]);
    var padding = parseInt(cell.css("padding-left"), 10) + value;

    cell.css("padding-left", + padding + "px");
    
    childrenOf(node).each(function() {
      indent($(this), value);
    });
  };

  function initialize(node) {
    if(!node.hasClass("initialized")) {
      node.addClass("initialized");

      var childNodes = childrenOf(node);
    
      if(!node.hasClass("parent") && childNodes.length > 0) {
        node.addClass("parent");
      }

      if(node.hasClass("parent")) {
        var cell = $(node.children("td")[options.treeColumn]);
        var padding = parseInt(cell.css("padding-left"), 10) + options.indent;

        childNodes.each(function() {
          $($(this).children("td")[options.treeColumn]).css("padding-left", padding + "px");
        });
      
        if(options.expandable) {
          cell.prepend('<span style="margin-left: -' + options.indent + 'px; padding-left: ' + options.indent + 'px" class="expander"></span>');
          $(cell[0].firstChild).click(function() { node.toggleBranch(); });
        
          // Check for a class set explicitly by the user, otherwise set the default class
          if(!(node.hasClass("expanded") || node.hasClass("collapsed"))) {
            node.addClass(options.initialState);
          }

          if(node.hasClass("collapsed")) {
            node.collapse();
          } else if (node.hasClass("expanded")) {
            node.expand();
          }
        }
      }
    }
  };
  
  function move(node, destination) {
    node.insertAfter(destination);
    childrenOf(node).reverse().each(function() { move($(this), node[0]); });
  };
  
  function parentOf(node) {
    var classNames = node[0].className.split(' ');
    
    for(key in classNames) {
      if(classNames[key].match("child-of-")) {
        return $("#" + classNames[key].substring(9));
      }
    }
  };
})(jQuery);
