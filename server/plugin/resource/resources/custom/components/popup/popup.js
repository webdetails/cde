


var PopupComponent = BaseComponent.extend({

  ph: undefined,
  arrow: undefined,
  content: undefined,

  update: function(){
    var myself = this;
    if (this.ph) {this.ph.remove();}
    this.ph = $('<div>');
    this.content = $("#" + this.htmlObject).appendTo(this.ph);
    this.ph.hide().appendTo($('body'));
    this.ph.addClass('tooltipComponent');
    var link = $("<a>&nbsp;</a>");
    link.addClass("close").click(function(){
        myself.hide();
    });
    link.appendTo(this.ph);
    this.arrow = $("<div class='arrow'>").appendTo(this.ph);
  },

  popup: function(target,gravity) {
    var pos = target.offset(),
      css = {
        'top': 'auto',
        'bottom': 'auto',
        'left': 'auto',
        'right': 'auto'
      },
      minimumDistance = 20, /* Minimum distance from the edges */
      vertexOffset = 27, /* How much clearance we need to display the vertex */
      vertexSize = 45, /* How big is the vertex along the tooltip edge */
      targetOffset,
      phHeight = this.ph.outerHeight(),
      phWidth = this.ph.outerWidth();
 
    /* Allow overriding this.gravity */
    gravity = gravity || this.gravity;
    /* Clear positioning for the arrow */
    this.arrow.css({top: "", left: "", bottom: "", right: ""});
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
      maxHeight = document.height - minimumDistance;

    switch(gravity) {
      /*************** NORTH ***************/
      case 'N':
        css.left = this.center(target.outerWidth(),phWidth,pos.left,minWidth,maxWidth);
        css.top = this.offset(target.height(),phHeight,pos.top,vertexOffset,minHeight,maxHeight,'near');
        targetOffset = pos.left - css.left; 
        this.arrow.css('left',this.center(target.outerWidth(),vertexSize,targetOffset,0,phWidth));
        this.ph.addClass(css.top < pos.top ? 'north':'south');
        break;

      /*************** SOUTH ***************/
      case 'S':
        css.left = this.center(target.outerWidth(),phWidth,pos.left,minWidth,maxWidth);
        targetOffset = pos.left - css.left; 
        this.arrow.css('left',this.center(target.outerWidth(),vertexSize,targetOffset,0,phWidth));
        css.top = this.offset(target.height(),phHeight,pos.top,vertexOffset,minHeight,maxHeight,'far');
        this.ph.addClass(css.top < pos.top ? 'north':'south');
        break;

      /*************** WEST ***************/
      case 'W':
        css.top = this.center(target.outerHeight(),phHeight,pos.top,minHeight,maxHeight);
        css.left = this.offset(target.width(),phWidth,pos.left,vertexOffset,minWidth,maxWidth,'near');
        targetOffset = pos.top - css.top; 
        this.arrow.css('top',this.center(target.outerHeight(),vertexSize,targetOffset,0,phHeight));
        this.ph.addClass(css.left < pos.left ? 'west':'east');
        break;

      /*************** EAST ***************/
      case 'E':
        css.top = this.center(target.outerHeight(),phHeight,pos.top,minHeight,maxHeight);
        css.left = this.offset(target.width(),phWidth,pos.left,vertexOffset,minWidth,maxWidth,'far');
        targetOffset = pos.top - css.top; 
        this.arrow.css('top',this.center(target.outerHeight(),vertexSize,targetOffset,0,phHeight));
        this.ph.addClass(css.left < pos.left ? 'west':'east');
        break;
    }
    this.ph.css(css);
    this.ph.show();
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
