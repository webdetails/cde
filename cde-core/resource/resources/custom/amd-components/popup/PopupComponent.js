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

define([
  'cdf/components/BaseComponent',
  'cdf/lib/jquery',
  'css!./PopupComponent'],
  function(BaseComponent, $) {

  var duplicateIndex = 1;

  var PopupComponent = BaseComponent.extend({

    ph: undefined,
    arrow: undefined,
    content: undefined,
    cancel: undefined,
    $overlay: undefined,
    popupClass: undefined,
    popupOverlayClass: undefined,
    horizontalScroll: undefined,
    verticalScroll: undefined,
    
    /* // Default settings
    gravity: undefined,
    draggable: true,
    closeOnClickOutside: false,
    */ 

    update: function() {
      var myself = this;

      this.content = $("#" + this.htmlObject).detach();
      this.ph = this.ph ? this.ph.empty() : $('<div>').appendTo($('body'));
      this.content.appendTo(this.ph);
      this.ph.hide();
      this.ph.addClass('popupComponent');

      if(this.popupClass) {
        this.ph.addClass(this.popupClass);
      }
      this.cancel = $("<a>&nbsp;</a>");
      this.cancel.addClass("close").click(function(){
        myself.hide();
      });
      this.cancel.appendTo(this.ph);
      this.arrow = $("<div class='arrow'>").appendTo(this.ph);
      this.content.removeClass('hidePopup');
    },
    clone: function(params,comps,html) {
      var that = this.base(params,comps,html);
      that.ph = this.ph.clone();
      that.ph.insertAfter(this.ph);
      that.ph.hide();
      that.ph.find("[id]").each(function(i,e){
        var $e = $(e);
        var id = $e.attr("id");
        if(id && id in html) {
          $e.attr("id", html[id]);
        } else {
          $e.attr("id",id + '_' + duplicateIndex++);
        }
      });
      return that;
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
      
      /* Draggable/ */
      var draggable = typeof this.draggable === "undefined"?true:this.draggable;

      /* Horizontal Scrollbar */
      if(this.horizontalScroll){
        $("#"+this.htmlObject).css("overflow-x","scroll");
      }

      /* Vertical Scrollbar */
      if(this.verticalScroll){
        $("#"+this.htmlObject).css("overflow-y","scroll");  
      }
      
      /* Close on click outside */
      var closeOnClickOutside = typeof this.closeOnClickOutside === "undefined"?false:this.closeOnClickOutside;
      
      
      /* Clear positioning for the arrow */
      this.arrow.css({
        top: "", 
        left: "", 
        bottom: "", 
        right: ""
      });
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
       * the element. Here we'll just center it vertically with the target.
       *
       * For vertical (N/S) attachment, reverse the vertical and horizontal
       * axes.
       */
      var minWidth = minimumDistance,
      maxWidth = $(document).width() - minimumDistance,
      minHeight = minimumDistance,
      maxHeight = $(document).height() - minimumDistance,
      targetWidth, targetHeight,
      paddingNear, paddingFar;

      switch(gravity) {
        /*************** NORTH ***************/
        case 'N':
          paddingNear = parseInt(target.css('padding-top').replace(/(.*)px/,"$1"),10);
          css.left = this.center(target.outerWidth(),phWidth,pos.left,minWidth,maxWidth);
          targetHeight = "ownerSVGElement" in target[0] ?
          (target.attr("height") ? target.attr("height") - 0 : 0):
          target.height();
          targetOffset = pos.left - css.left - this.ph.css('border-top-width').replace(/(.*)px/,"$1"); 
          css.top = this.offset(targetHeight,phHeight,pos.top+paddingNear,vertexOffset,minHeight,maxHeight,'near');
          this.arrow.css('left',this.center(target.outerWidth(),vertexSize,targetOffset,0,phWidth));
          this.ph.addClass(css.top < pos.top ? 'north':'south');
          break;

        /*************** SOUTH ***************/
        case 'S':
          paddingNear = parseInt(target.css('padding-top').replace(/(.*)px/,"$1"),10);
          targetHeight = "ownerSVGElement" in target[0] ?
          (target.attr("height") ? target.attr("height") - 0 : 0):
          target.height();
          css.left = this.center(target.outerWidth(),phWidth,pos.left,minWidth,maxWidth);
          css.top = this.offset(targetHeight,phHeight,pos.top+paddingNear,vertexOffset,minHeight,maxHeight,'far');
          targetOffset = pos.left - css.left - this.ph.css('border-top-width').replace(/(.*)px/,"$1"); 
          this.arrow.css('left',this.center(target.outerWidth(),vertexSize,targetOffset,0,phWidth));
          this.ph.addClass(css.top < pos.top ? 'north':'south');
          break;

        /*************** WEST ***************/
        case 'W':
          paddingNear = parseInt(target.css('padding-left').replace(/(.*)px/,"$1"),10);

          css.top = this.center(target.outerHeight(),phHeight,pos.top,minHeight,maxHeight);
          targetWidth = "ownerSVGElement" in target[0] ?
          (target.attr("width") ? target.attr("width") - 0 : 0):
          target.width();
          css.left = this.offset(target.width(),phWidth,pos.left+paddingNear,vertexOffset,minWidth,maxWidth,'near');
          targetOffset = pos.top - css.top - this.ph.css('border-left-width').replace(/(.*)px/,"$1");  
          this.arrow.css('top',this.center(target.outerHeight(),vertexSize,targetOffset,0,phHeight));
          this.ph.addClass(css.left < pos.left ? 'west':'east');
          break;

        /*************** EAST ***************/
        case 'E':
          paddingNear = parseInt(target.css('padding-left').replace(/(.*)px/,"$1"),10);
          css.top = this.center(target.outerHeight(),phHeight,pos.top,minHeight,maxHeight);
          targetWidth = "ownerSVGElement" in target[0] ?
          (target.attr("width") ? target.attr("width") - 0 : 0):
          target.width();
          css.left = this.offset(targetWidth,phWidth,pos.left+paddingNear,vertexOffset,minWidth,maxWidth,'far');
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
      var dragHandler = function() {
        myself.arrow.hide();
      };
      this.ph.bind('drag',dragHandler);
      
      if(draggable){
        this.ph.draggable({cancel:"#"+this.htmlObject});    
      }
      var basePos,dragPos;
      this.ph.bind('touchstart',function(e){
        basePos = myself.ph.offset();
        dragPos = {
          left: e.originalEvent.touches[0].pageX, 
          top: e.originalEvent.touches[0].pageY
          };
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
      

      if(closeOnClickOutside){
          
        // Define an overlay so that we can click
        if(!this.$overlay){
          this.$overlay = $('<div id="popupComponentOverlay"></div>');
          if(this.popupOverlayClass) {
            this.$overlay.addClass(this.popupOverlayClass);
          }
        }
        this.$overlay.appendTo("body").click(function(event){
          event.stopPropagation();
          myself.hide();
        })
      }
      $('body').addClass('draggable-popup-fix');
      
    },

    hide: function() {
      
      this.ph.hide();
      if(this.$overlay){
        this.$overlay.unbind('click');
        this.$overlay.detach();
      }
      $('body').removeClass('draggable-popup-fix');
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

  return PopupComponent;

});
