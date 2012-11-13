/* This is a reset for the 
 */

$.blockUI.defaults.fadeIn = 0;
$.blockUI.defaults.message = '<div style="padding: 15px;"><img src="res/system/pentaho-cdf-dd/resources/mobile/img/spinner.gif" />';
$.blockUI.defaults.css.left = '50%';
$.blockUI.defaults.css.top = '50%';
$.blockUI.defaults.css.marginLeft = '-210px';
$.blockUI.defaults.css.marginTop = '-220px';
$.blockUI.defaults.css.width = '420px';
$.blockUI.defaults.css.height = '440px';

$.blockUI.defaults.css.opacity = '.8';
$.blockUI.defaults.css['-webkit-border-radius'] = '10px'; 
$.blockUI.defaults.css['-moz-border-radius'] = '10px';

Dashboards.blockUIwithDrag = function() {
  if (typeof Dashboards.i18nSupport !== "undefined" && Dashboards.i18nSupport != null) {
    // If i18n support is enabled process the message accordingly
    $.blockUI.defaults.message = '<div style="padding: 15px;"><img src="' + webAppPath + '/content/pentaho-cdf/resources/style/images/busy.gif" /><h3>' + Dashboards.i18nSupport.prop('processing.message') + '</h3></div>';
  }

  $.blockUI();
  var handle = $('<div id="blockUIDragHandle"> <a href="javascript:$.unblockUI()" title="Click to unblock">X</a></div>')
  $("div.blockUI.blockMsg").prepend(handle);
  $("div.blockUI.blockMsg").draggable({
    handle: "#blockUIDragHandle"
  });
};

var orientationChangeThreshold = 700;

function CDFMobile() {

    var myself = this;
    // expose our local jquery with jquerymobile to the outside world

    var _dashboard = {};
    var _carousels = [];

    var _title = $("#title"),
    _navSelector = $('#navSelector'),
    _scriptHolder = $("#scriptHolder"),
    _staging = $("#placeholder"),
    _content = $('#cmdContent'),
    _dashboard = $("#dashboard"),
    _toolbar = $("#toolbar"),
    _filters = $("#filtersPanel"),    
    _filtersContent = $("#filtersContent"),
    _innerFilters = $("#innerFilters"),
    _filterToolbar = $("#filtersActions");

    var _jqmTheme = 'a';

    var _magicalHeightOffset = 25;
    var _enabledRotation = true;
    if (typeof window.enabledRotation == "boolean")
        _enabledRotation = false;


    this.enabledRotation = function (value) {
      _enabledRotation = value;
    }

    this.redrawDashboard = function() {
       // _title.text(_dashboard.meta.title);

        _innerFilters.empty().append($("#filters"));
        _dashboard.empty().append(_staging.children(':not(script)'));
        updateNavigation();
        reloadCarousels();
    };

    function captureClick(callback) {
        return function(ev) {
            callback.call(myself);
            ev.stopPropagation();
        };
    }

    function handleClick(callback) {
        return function(ev) {
            callback.call(myself);
        };
    }

    this.loadDashboard = function(target) {
      var queryArgs = $.extend({},this.parameters,target);
      var queryStrings = [];
      for (key in queryArgs) if (queryArgs.hasOwnProperty(key)) {
        queryStrings.push( escape(key) + "=" + escape(queryArgs[key]));
      }
      var queryString = queryStrings.join("&")
      window.location = window.location.pathname + "?" + queryString;
    };

    this.redrawToolbar = function(buttons) {
        redrawNavBar(buttons, _toolbar);
    };

    this.redrawFiltersToolbar = function(buttons) {
        redrawNavBar(buttons, _filterToolbar);
    };
    this.resizeAll = function() {
        resizeCharts();
        for (var i = 0; i < _carousels.length; i++) {
            var car = _carousels[i];
            resizeCarousel(car.getHolder());
            car.fitToContainer();
        }
                       
    }


     this.checkOrientation = function(){

      var filtersPanel = $('#filtersPanel');
      var filtersHeader = $('#filtersHeader');

      //If we are on filter panel do nothing		
      if (filtersPanel.hasClass('ui-page-active') && filtersHeader.css('display') != 'none') 
        return;

      if (this.shouldSwitchToHorizontal()) {                
        $('#toolbar').css('display', 'none');
        filtersPanel.addClass('ui-body-b ui-page-active');
        filtersPanel.css('width', '200px');

        var mainContentDiv = $('#mainContent');

        mainContentDiv.css('margin-left', '200px');
        mainContentDiv.width(mainContentDiv.parent().width() - 200);

        filtersHeader.css('display', 'none');
        $('#filtersActions').css('display', 'none');
        $('#filters span.label').css('width','100%');
        $('#filters span.label').css('position','static');
        $('#filters span.selector').css('width', '100%');
        $('#filters span.filter span.selector div.ui-select').css('width', '80%');
      } else {
        $('#toolbar').css('display', 'block');          
        filtersPanel.removeClass('ui-body-b ui-page-active');
        filtersPanel.css('width', '100%');

        $('#mainContent').css('margin-left', '0px');          
        $('#mainContent').width('100%');

        filtersHeader.css('display', '');
        $('#filtersActions').css('display', '');
        $('#filters span.label').css('width','30%');          
        $('#filters span.label').css('position','absolute');
        $('#filters span.selector').css('width', '70%');
        $('#filters span.filter span.selector div.ui-select').css('width', '60%');          
      }
    };


    this.shouldSwitchToHorizontal = function() {
      if (!_enabledRotation)
        return false;
      return (window.innerWidth / window.innerHeight >= 1) && window.innerWidth > orientationChangeThreshold;
    };

    function resizeCharts() {
        var headerHeight = 0;
        var $header = $.mobile.activePage.find('[data-role=header]');
        if ($header) headerHeight = $header.height();
        var footerHeight = 0;
        var $footer = $.mobile.activePage.find('[data-role=footer]');
        if ($footer) footerHeight = $footer.height();


        var headerFooterPadding = headerHeight + footerHeight;
        var widthMult,
        heightMult;


        var horizontal = myself.shouldSwitchToHorizontal();
            
//        var myself = this;
        var charts = Dashboards.components.filter(function(comp) {
            return /^ccc/.test(comp.type);
        });
        $.each(charts,
        function(i, comp) {
            /* First thing first: don't even try to resize charts that haven't
             * been initialized!
             */
            if (!comp.chart || !comp.htmlObject) {
                return;
            }

                               
            var $e = $("#" + comp.htmlObject + " svg"),
            e = $e[0],
            /* Next step is measuring the available space for our charts. We always
             * have the full window width available to us, but that's not the case
             * with the height, so we trim out the space we know must be reserved.
             */
            windowWidth = window.innerWidth,
            windowHeight = window.innerHeight,
            availableWidth = windowWidth - 20 - (horizontal?200:0),
            availableHeight = windowHeight - headerFooterPadding - _magicalHeightOffset,
            /* In the name of sanity, we'd rather calculate everything relative
             * to the original sizes, rather than the last calculated size, so
             * we'll store/retrieve the original values in a data attribute.
             */                  
            originalHeight = $e.attr('data-originalHeight') || $e.parent().height(),
            originalWidth = $e.attr('data-originalWidth') || $e.parent().width();

           
            if (e == undefined) return;
           

            $e.attr('data-originalHeight', originalHeight);
            $e.attr('data-originalWidth', originalWidth);

            /* Next we calculate the ratios between original and available space.
             * To keep the original proportions, we have to multiply both axes
             * by the same ratio. If there's more available height than width,
             * we're in portrait mode, so we'll use the height ratio to make the
             * chart fit within the available space. If the width is bigger,
             * we're in landscape mode. Rather than having a tiny chart we'll use
             *the full width, and overflow vertically as needed -- effectively a
             * zoom mode.
             */
            var heightRatio = availableHeight / originalHeight,
            widthRatio = availableWidth / originalWidth,
            availableRatio = horizontal?heightRatio: (windowHeight > windowWidth ? heightRatio : widthRatio),
            targetWidth = originalWidth * availableRatio,
            targetHeight = originalHeight * availableRatio;


            /* Finally, set the width and height to our desired values for the chart
             * object, the component and the svg. We also need to give the svg a
             * viewBox, or the svg will think we just enlarged its canvas.
             */

            comp.chart.options.width = targetWidth;
            comp.chart.options.height = targetHeight;

            comp.chartDefinition.width = targetWidth;
            comp.chartDefinition.height = targetHeight;

            e.setAttribute('width', targetWidth);
            e.setAttribute('height', targetHeight);
            e.setAttribute('viewBox', '0 0 ' + originalWidth + ' ' + originalHeight);
        });
    };

    /* The navigation pull-down menu gets its data from the loaded
     * dashboard. We expect to find a mobileNav component with a
     * navList() method that provides a listing of the dashboards
     * you can navigate to from your present location. if such a
     * component isn't found, we assume that this is a dead-end
     * dashboard and hide the navigation pull-down instead.
     */
    function updateNavigation() {
        /* First we check for the existence of the mobileNav component.
         * We check for either the bare mobileNav name, or the the
         * CDE-style render_mobileNav name.
         * If it doesn't exist, we just hide the navigation pull-down.
         */
        var navComponent = window.mobileNav || window.render_mobileNav;
        if (!navComponent) {
            _navSelector.hide();
            return;
        }
        /* 
     */
        var dashboardList = navComponent.navList();

    };

    /*  
    */
    function navigationCallback(event) {

        };

    function redrawNavBar(buttons, loc) {
        if (buttons.length) {
            var toolbar = $("<ul></ul>"),
            buttonWidth = Math.round(10000 / buttons.length) / 100;
            for (b in buttons) if (buttons.hasOwnProperty(b)) {
                var bdata = buttons[b];
                var button = $("<li>").appendTo(toolbar);
                var link = $("<a></a>").appendTo(button);
                link.attr("data-icon", bdata.icon);
                if (bdata.location) {
                    link.attr("href", bdata.location);
                    link.click(handleClick(bdata.callback));
                } else if (bdata.rel) {
                    link.attr("data-rel", bdata.rel);
                    link.click(handleClick(bdata.callback));
                } else {
                    link.click(captureClick(bdata.callback));
                }
                if (bdata.transition) {
                    link.attr("data-transition", bdata.transition);
                }

                link.text(bdata.label);
            }
            $("<div data-role='navbar'></div>").append(toolbar).appendTo(loc.empty());
        }
    };

    function recalculateHeight(content, title, toolbar) {
        var padding = parseInt(content.parent().css('padding-top').match(/[0-9]+/)[0], 10) +
        parseInt(content.parent().css('padding-bottom').match(/[0-9]+/)[0], 10);
        var height = window.innerHeight - (padding + title.parent().outerHeight() + toolbar.outerHeight());
        content.height(height);
    };

    function showFilters() {

    };

    function reloadCarousels() {
        _carousels.length = 0;
        $('.cdfCarouselHolder').each(function(i, e) {
            _carousels.push(createCarousel(e));
        });
    };

    function resizeCarousel(element) {
      
        var headerHeight = 0;
        var $header = $.mobile.activePage.find('[data-role=header]');
        if ($header) headerHeight = $header.height();
        var footerHeight = 0;
        var $footer = $.mobile.activePage.find('[data-role=footer]');
        if ($footer) footerHeight = $footer.height();

        var headerFooterPadding = headerHeight + footerHeight;
        var horizontal = myself.shouldSwitchToHorizontal();
        if (horizontal) headerFooterPadding -= footerHeight;      
        var $element = (element instanceof $ ? element: $(element)),
        contentWidth = _content.innerWidth();
        totalWidth = 0,
        count = 0;

        $(element).find("li.cdfCarouselItem").each(function(i, e) {
            count += 1;
            var $e = $(e);
            $e.width(contentWidth);
            $e.height(window.innerHeight - headerFooterPadding - _magicalHeightOffset);
//            if (window.innerWidth > window.innerHeight && !horizontal) {
              $e.css('overflow-y', 'auto');
              $e.css('overflow-x', 'hidden');
  //          } else {
    //          $e.css('overflow-y', 'hidden');
      //        $e.css('overflow-x', 'hidden');              
        //    }
            totalWidth += $e.outerWidth(true);
//            contentHeight =  $e.height() > contentHeight ? $e.height() : contentHeight;
        });
        $element.width(totalWidth);
  
        return count;
    };

    function createCarousel(element) {
        var $element = $(element);
        resizeCarousel($element.find('ul.cdfCarouself'));
        var scroller = new Scroller($element.find('.cdfCarousel'), {onScroll: function (i) { 
        	if (window.titleChange) {
        		$("#title").html($('li.cdfCarouselItem:nth-child('+(i + 1)+') > .cdfCarouselItemTitle').html());
        	}}},
        $);
        scroller.fitToContainer();
        return scroller;
    }

    function refreshNav() {
        var $nav = $('select#navSelector');
        if (typeof render_navigation != 'undefined') {
            $nav.empty();
            $nav.unbind('change');
            $nav.bind('change',
            function(e) {
                var keys = {};
                var args = $.each($('select#navSelector').val().split('&'),function(i,e) {
                    keys[e[0]] = e[1];
                });
                myself.loadDashboard.call(myself, keys);
            });
            $nav.append("<option data-placeholder='true'>Navigate</option>");
            $.each(render_navigation.navTargets,
            function(i, e) {
                $nav.append("<option value='" + e[1] + "'>" + e[0] + "</option>");
            });
            $nav.show();
            $nav.selectmenu('refresh');
        } else {
            $nav.hide();
            $('span.navigate-button').hide();
        };
    };
    this.refresh = function() {
        Dashboards.log("Refreshing");
        /* If we're going for a reload, we want to
         * bypass the browser cache, so we need the
         * 'true' argument.
         */
        window.location.reload(true);
    };

    this.favorites = function() {
        Dashboards.log("Adding to favorites");
    };

    this.filters = function() {
        };

    this.settings = function() {
        Dashboards.log("Customizing settings");
    };

    this.filtersOk = function() {
        Dashboards.log("Accepting Filters");
        setTimeout(function() {
            cdfmobile.resizeAll();
          },
        1);        
    };

    this.filtersCancel = function() {
        Dashboards.log("Rejecting Filters");
        $.mobile.changePage(
            "#" + $.mobile.urlHistory.getPrev().url,
            {transition:'flip', reverse:true}
        );
    };
    this.refreshSelector = function(component) {
        $("#" + component.htmlObject + " select").attr('data-theme', _jqmTheme).selectmenu();
		if (this.shouldSwitchToHorizontal()) $('#filters span.filter span.selector div.ui-select').css('width', '80%');
    }

    this.cdfLoaded = function() {
        Dashboards.log('cdf-m caught cdf loading');
        refreshNav();
        this.resizeAll();
    }
}
$(function() {
    var parameters = {};
    
    
    
    $.each(location.search.slice(1).split('&').map(function(e) {
        return e.split('=')
    }),
    function(i, e) {
        parameters[e[0]] = e[1]
    });
    window.cdfmobile = new CDFMobile();

    cdfmobile.parameters = parameters;
    
    
    //If we want to specify a different arrangement for the toolbar buttons, 
    //we create a javascript global variable called toolbarButtons that
    //contains the array with the buttons to create   
    if (typeof toolbarButtons == 'undefined') {
      var toolbarButtons = [
        {
          label: "Filters",
          icon: "gear",
          location: "#filtersPanel",
          transition: "flip",
          callback: cdfmobile.filters
      },
      {
          label: "Refresh",
          icon: "refresh",
          callback: cdfmobile.refresh
      }
      ];      
    }
    
    
    cdfmobile.redrawToolbar(toolbarButtons);
    cdfmobile.redrawFiltersToolbar([
    //    {
    //      label: "Cancel",
    //      icon: "delete",
    //      callback: cdfmobile.filtersCancel,
    //    },
    {
        //      label: "Ok",
        label: "Done",
        icon: "check",
        location: "#dashboardView",
        transition: "flip",
        callback: cdfmobile.filtersOk
    }
    ]);
    setTimeout(function() {
        cdfmobile.redrawDashboard();
    },
    20);
    
    


    $(window).bind("orientationchange", function() {cdfmobile.checkOrientation()});               
    $(window).bind('cdfLoaded', function(){
      cdfmobile.cdfLoaded();
    });
    $(window).bind('resize', function() {cdfmobile.checkOrientation();cdfmobile.resizeAll();});
});
