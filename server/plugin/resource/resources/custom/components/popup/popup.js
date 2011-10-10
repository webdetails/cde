


var PopupComponent = BaseComponent.extend({

  ph: undefined,
  arrow: undefined,
  content: undefined,
  cancel: undefined,

  update: function(){
    var myself = this;
    if (this.ph) {this.ph.remove();}
    this.ph = $('<div>');
    this.content = $("#" + this.htmlObject).appendTo(this.ph);
    this.ph.hide().appendTo($('body'));
    this.ph.addClass('popupComponent');
    this.cancel = $("<a>&nbsp;</a>");
    this.cancel.addClass("close").click(function(){
        myself.hide();
    });
    this.cancel.appendTo(this.ph);
    this.arrow = $("<div class='arrow'>").appendTo(this.ph);
    this.content.removeClass('hidePopup');
  },

  popup: function(target,gravity) {
    var pos = target.offset(),
      css = {
        'top': 'auto',
        'bottom': 'auto',
        'left': 'auto',
        'right': 'auto'
      },
      /* Minimum distance from the edges */
      minimumDistance = 20,
      /* How much clearance we need to display the vertex, 
       * should be (vertex depth - border depth)
       */
      vertexOffset = 18 - 6,
      /* Size of the vertex along the tooltip edge */
      vertexSize = 45, 
      targetOffset,
      phHeight = this.ph.outerHeight(),
      phWidth = this.ph.outerWidth();
 
    /* Allow overriding this.gravity */
    gravity = gravity || this.gravity;
    /* Clear positioning for the arrow */
    this.arrow.css({top: "", left: "", bottom: "", right: ""});
    this.arrow.show();
    this.ph.removeClass('north south east west');
    /* The gravity parameter is what decides where the tooltip
     * attaches to the target element. The tooltip is positioned
     * by setting the tooltip's top and left properties.
     * 
     * For horizontal (E/W) attachment we calculate top so that the
     * tooltip lies centered with the target, and calculate left
     * such that the tooltip will be adjacent to the target, on the
     * E/W side as appropriate. If top is such that the tooltip would
     * lie outside the viewport, we correct it so that it lies at least
     * at minimumDistance pixels from the edges. If left would imply
     * that the tooltip would be outside the viewport, we reverse the
     * gravity.
     *
     * Then we do vertical positioning for the arrow pointing towards
     * the element. Here we'll just
     *
     * For vertical (N/S) attachment, reverse left and top.
     */
    var minWidth = minimumDistance,
      maxWidth = document.width - minimumDistance,
      minHeight = minimumDistance,
      maxHeight = document.height - minimumDistance,
      targetWidth, targetHeight,
      paddingNear, paddingFar;

    switch(gravity) {
      /*************** NORTH ***************/
      case 'N':
        paddingNear = parseInt(target.css('padding-top').replace(/(.*)px/,"$1"),10);
        paddingFar = parseInt(target.css('padding-bottom').replace(/(.*)px/,"$1"),10);
        css.left = this.center(target.outerWidth(),phWidth,pos.left,minWidth,maxWidth);
        targetHeight = "ownerSVGDocument" in target[0] ? target.attr("height") :
            target.outerHeight() - target.css('padding-bottom').replace(/(.*)px/,"$1");
        css.top = this.offset(targetHeight,phHeight,pos.top,vertexOffset,minHeight,maxHeight,'near');
        targetOffset = pos.left - css.left - this.ph.css('border-top-width').replace(/(.*)px/,"$1"); 
        this.arrow.css('left',this.center(target.outerWidth(),vertexSize,targetOffset,0,phWidth));
        this.ph.addClass(css.top < pos.top ? 'north':'south');
        break;

      /*************** SOUTH ***************/
      case 'S':
        paddingNear = parseInt(target.css('padding-top').replace(/(.*)px/,"$1"),10);
        paddingFar = parseInt(target.css('padding-bottom').replace(/(.*)px/,"$1"),10);
        css.left = this.center(target.outerWidth(),phWidth,pos.left,minWidth,maxWidth);
        targetHeight = "ownerSVGDocument" in target[0] ? target.attr("height") :
            target.outerHeight() - target.css('padding-bottom').replace(/(.*)px/,"$1");
        css.top = this.offset(targetHeight,phHeight,pos.top,vertexOffset,minHeight,maxHeight,'far');
        targetOffset = pos.left - css.left - this.ph.css('border-top-width').replace(/(.*)px/,"$1"); 
        this.arrow.css('left',this.center(target.outerWidth(),vertexSize,targetOffset,0,phWidth));
        this.ph.addClass(css.top < pos.top ? 'north':'south');
        break;

      /*************** WEST ***************/
      case 'W':
        paddingNear = parseInt(target.css('padding-left').replace(/(.*)px/,"$1"),10);
        paddingFar = parseInt(target.css('padding-right').replace(/(.*)px/,"$1"),10);

        css.top = this.center(target.outerHeight(),phHeight,pos.top,minHeight,maxHeight);
        css.left = this.offset(target.width()+paddingNear,phWidth,pos.left+paddingNear,vertexOffset,minWidth,maxWidth,'near');
        targetOffset = pos.top - css.top - this.ph.css('border-left-width').replace(/(.*)px/,"$1");  
        this.arrow.css('top',this.center(target.outerHeight(),vertexSize,targetOffset,0,phHeight));
        this.ph.addClass(css.left < pos.left ? 'west':'east');
        break;

      /*************** EAST ***************/
      case 'E':
        paddingNear = parseInt(target.css('padding-left').replace(/(.*)px/,"$1"),10);
        paddingFar = parseInt(target.css('padding-right').replace(/(.*)px/,"$1"),10);
        css.top = this.center(target.outerHeight(),phHeight,pos.top,minHeight,maxHeight);
        css.left = this.offset(target.width()+paddingNear,phWidth,pos.left+paddingNear,vertexOffset,minWidth,maxWidth,'far');
        targetOffset = pos.top - css.top - this.ph.css('border-left-width').replace(/(.*)px/,"$1");  
        this.arrow.css('top',this.center(target.outerHeight(),vertexSize,targetOffset,0,phHeight));
        this.ph.addClass(css.left < pos.left ? 'west':'east');
        break;
    }
    this.ph.css(css);
    this.ph.show();

    var escHandler,
    myself = this;
    escHandler = function(e) {
      if (e.which == 27) {
        myself.ph.hide();
        $(document).unbind('keydown',escHandler);
      }
    };
    $(document).keydown(escHandler);
    var dragHandler;
    dragHandler = function() {
      myself.arrow.hide();
    }
    this.ph.bind('drag',dragHandler);
    this.ph.draggable();
    var basePos,dragPos;
    this.ph.bind('touchstart',function(e){
      basePos = myself.ph.offset();
      dragPos = {left: e.originalEvent.touches[0].pageX, top: e.originalEvent.touches[0].pageY};
    });
    this.ph.bind('touchmove',function(e){
        var finalPos = {
          top: basePos.top + e.originalEvent.touches[0].pageY - dragPos.top,
          left: basePos.left + e.originalEvent.touches[0].pageX - dragPos.left
        };
        myself.ph.offset(finalPos);
        myself.arrow.hide();
        e.preventDefault();
    });
  },

  hide: function() {
    this.ph.hide();
  },

  /* Given the size (width/height) for a target and a placeholder element,
   * the target's offset (left/top) and minimum/maximum values for the
   * available size, calculates the offset for the placeholder such that
   * the placeholder will be as close to centered relative to the target
   * as the available space allows.
   */
  center: function(targetSize, phSize, offset,min,max) {
    var candidate = offset + targetSize / 2 - phSize/2;
    return candidate + phSize > max ? max - phSize : candidate < min ? min : candidate;
  },

  offset: function(targetSize,phSize,offset,gap,min,max,range) {
    var near = offset - phSize - gap,
      far = offset + targetSize + gap,
      nearAdmissible = near > min,
      farAdmissible = far + phSize < max;

      return range == 'near' ? (nearAdmissible || !farAdmissible ? near : far) :
             range == 'far' ? (farAdmissible || !nearAdmissible ? far : near) :
             near;
  }
});
