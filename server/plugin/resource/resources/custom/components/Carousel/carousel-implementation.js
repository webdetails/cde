function CarouselComponent() {
  /* core CDF stuff */
  this.visible = true;

  /* Some basics */
  var myself = this;

  /* iScroll Setup */

  function onScrollEnd() {
    myself.statusbar.find(".active").removeClass("active");
    myself.statusbar.find(":nth-child("+(this.currPageX + 1) +")").addClass("active");
  };
  var _iScroll,
    _settings = {
    snap: true,
    momentum: false,
    hScrollbar: false,
    onScrollEnd: onScrollEnd
  };

  /* Initial structure needed to build the carousel */
  var _root,
    _scroller,
    _statusbar,
    _children;

  /* Update needs to:
   * 1. Rebuild the support structure
   * 2. Acquire and reposition the components we're grouping
   * 3. Initialize the carousel scroller
   */
  this.update = function() {
    myself = this;
    rebuildStructure();
    /* iScroll expects a naked DOM element, not a jQuery object */
    _iScroll = new iScroll(_scroller[0],_settings);
  },

  /* Rebuilding the structure
   * We assume that the htmlObject's contents are what we're going to be drawing
   */
  function rebuildStructure() {
    _root = _root || $("#" + this.htmlObject));
    _children = _children || _root.children();
    _root.empty();
    _root.addClass('carouselComponent');
    _scroller = $("<div></div>").appendTo(_root);
    _scroller.addClass('scroller');
    _statusbar = $("<ul></ul>").appendTo(_root);
    _statusbar.addClass('status');
  },

  function rebuildCarousel() {
    for (i in _children) if (_children.hasOwnKey(i)) {
      $("<li>"+i+"</li>").appendTo(_statusbar).addClass("statusitem");
    
    }
    _statusbar.find(':first-child').addClass('active');
  },

  this.nextItem = function() {
  },

  this.previousItem = function() {
  }

  return this;
};
