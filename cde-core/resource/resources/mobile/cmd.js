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

    function resizeCharts() {

        var activePage = $.mobile.activePage;
        var headerFooterPadding = ( activePage != undefined ) ?
            activePage.find('[data-role=header]').height() +
            activePage.find('[data-role=footer]').height() : 0;
        var widthMult,
        heightMult;

        var charts = Dashboards.components.filter(function(comp) {
            return /^ccc/.test(comp.type);
        });
        $.each(charts,
        function(i, comp) {
            /* First thing first: don't even try to resize charts that haven't
             * been initialized!
             */
            if (!comp.chart) {
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
            availableWidth = windowWidth - 20,
            availableHeight = windowHeight - headerFooterPadding - 150,
            /* In the name of sanity, we'd rather calculate everything relative
             * to the original sizes, rather than the last calculated size, so
             * we'll store/retrieve the original values in a data attribute.
             */
            originalHeight = $e.attr('data-originalHeight') || $e.parent().height(),
            originalWidth = $e.attr('data-originalWidth') || $e.parent().width();
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
            availableRatio = windowHeight > windowWidth ? heightRatio : widthRatio,
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
        var $element = (element instanceof $ ? element: $(element)),
        contentWidth = _content.innerWidth();
        totalWidth = 0,
        count = 0;

        $(element).find("li.cdfCarouselItem").each(function(i, e) {
            count += 1;
            var $e = $(e);
            $e.width(contentWidth);
            totalWidth += $e.outerWidth(true);
            //contentHeight =  $e.height() > contentHeight ? $e.height() : contentHeight;
        });
        $element.width(totalWidth);

        return count;
    };

    function createCarousel(element) {
        var $element = $(element);
        resizeCarousel($element.find('ul.cdfCarouself'));
        var scroller = new Scroller($element.find('.cdfCarousel'), {},
        $);
        scroller.fitToContainer();
        return scroller;
    }

    function refreshNav() {
        var $nav = $('select#navSelector');
        if ( typeof render_navigation != 'undefined') {
            $nav.empty();
            $nav.unbind('change');
            $nav.bind('change',
            function(e) {
                var keys = {};
                var args = $.each($('select#navSelector').val().split('&'),function(i,e) {
                    var t = e.split('=');
                    keys[t[0]] = t[1]; 
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
    cdfmobile.redrawToolbar([
    //    {
    //      label: "Favorites",
    //      icon: "star",
    //      callback: cdfmobile.favorites
    //    },
    //    {
    //      label: "Settings",
    //      icon: "gear",
    //      callback: cdfmobile.settings
    //    },
    {
        label: "Filters",
        icon: "search",
        location: "#filtersPanel",
        transition: "flip",
        callback: cdfmobile.filters
    },
    {
        label: "Refresh",
        icon: "refresh",
        callback: cdfmobile.refresh
    }
    ]);
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
        callback: cdfmobile.filtersOk,
    }
    ]);
    setTimeout(function() {
        cdfmobile.redrawDashboard();
    },
    20);
    $(window).bind('cdfLoaded', function(){
      cdfmobile.cdfLoaded();
    });
    $(window).bind('resize', cdfmobile.resizeAll);
});
