function Scroller(element,options,$_){
  var myself = this,
    _options = options || {},
    $ = $_ || jQuery,
    $holder = $(element),
    $wrapper,
    $mask,
    $status,
    _count = $holder.children().length,
    _currentPage = 0,
    _width = _options.width,
    _duration = (_options.duration || 300) + "ms";

  var scrollLeft = {
      "-webkit-transition-property": "left",
      "-moz-transition-property": "left",
      "-webkit-transition-duration": _duration,
      "-moz-transition-duration": _duration,
      position: 'relative',
      left: 0
    };

  /*******************
   * Private Methods *
   *******************/

  function init() {
    createStructure();
    bindControls();
    createStatus();
  };

  /**/
  function cleanup() {
    resetAnimation();
    $holder.insertBefore($wrapper);
    // $holder.css('left',0);
    $wrapper.remove();
    $wrapper = undefined;
  };

  function fitToContainer() {
    var availableSpace = $wrapper.parent().width();
    _width = availableSpace;
    cleanup();
    init();
  };

  function resetAnimation (){
    $holder.removeClass('bounce-first');
    $holder.removeClass('bounce-last');
  };
  
  function createStructure() {
    $holder.wrap("<div class='mask'>").addClass('bounce');
    $mask = $holder.parent();
    $mask.wrap("<div class='scroller-wrapper'>");
    $mask.width(_width);
    $wrapper = $mask.parent();
    $holder.css('left', - _width * _currentPage);
    $holder.find(".cdfCarouselItem:not(:nth-child("+(_currentPage+1)+")) *").hide();
    $holder.bind("webkitAnimationEnd", resetAnimation);
    $holder.bind("animationend", resetAnimation);
    $status = $("<div class='status'></div>").appendTo($wrapper);
  };

  function bindControls() {
    $mask.swipeleft(function(){
      myself.scrollLeft();
    });
    $mask.swiperight(function(){
      myself.scrollRight();
    });
  };
  function createStatus() {
    var $bulletList = $("<div class='statusBullets'></div>").appendTo($status);
    for (var i = 0; i < _count; i++){
      $bulletList.append("<div class='statusBullet" + (i === _currentPage ? " active" : "") + "'>&nbsp</li>");
    }
    $("<div class='nav prev'>&nbsp;</div>").prependTo($status).click(function(){
      myself.scrollRight();
      return false;
    });
    $("<div class='nav next'>&nbsp;</div>").appendTo($status).click(function(){
      myself.scrollLeft();
      return false;
    });
  };

  function refreshStatus() {
    $status.find('div.statusBullets .statusBullet.active').removeClass('active');
    $status.find('div.statusBullets div:nth-child(' + (_currentPage+1) + ')').addClass('active');
  }
  /****************
   * External API *
   ****************/

  this.getHolder = function() {
    return $holder;
  };

  /* scrolling procedure:
   *  - display the item you're navigating to,
   *  - scroll,
   *  - hide the item you're navigating from.
   */
  this.scrollLeft = function() {
    var cloneLeft = $.extend({},scrollLeft);
    var $previous, $next;
    if (_currentPage < _count - 1) {
      $previous = $holder.find(".cdfCarouselItem:nth-child("+(_currentPage+1)+") *");
      _currentPage += 1;
      $next = $holder.find(".cdfCarouselItem:nth-child("+(_currentPage+1)+") *");
    } else {
      $holder.removeClass('bounce-last');
      $holder.addClass('bounce-last');
    }
    refreshStatus();
    cloneLeft.left = - _currentPage * _width;
    $next.show();
    $holder.css(cloneLeft);
    $previous.hide();
  };

  this.scrollRight = function() {
    var cloneLeft = $.extend({},scrollLeft);
    if (_currentPage > 0) {
      $previous = $holder.find(".cdfCarouselItem:nth-child("+(_currentPage+1)+") *");
      _currentPage -= 1;
      $next = $holder.find(".cdfCarouselItem:nth-child("+(_currentPage+1)+") *");

    } else {
      $holder.removeClass('bounce-first');
      $holder.addClass('bounce-first');
    }
    cloneLeft.left = - _currentPage * _width;
    $next.show();
    $holder.css(cloneLeft);
    $previous.hide();
    refreshStatus();
  };

  this.fitToContainer = function() {
    fitToContainer();
  }

  /******************
   * Initialization *
   ******************/

   init();
}

