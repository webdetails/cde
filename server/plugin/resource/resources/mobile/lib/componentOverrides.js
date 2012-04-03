SelectComponent = SelectComponent.extend({

  update: function() {
    this.base();
    cdfmobile.refreshSelector(this);
  }
});



TableComponent = TableComponent.extend({
  processTableComponentResponse: function(values) {
    this.base(values);
    var $ph = $("#" + this.htmlObject + "Table");


/*
$ph.parent().originalLeft = $ph.offset().left;
    $ph.parent().originalTop = $ph.offset().top;
    $ph.parent().die('swipedown');
    $ph.parent().live('swipedown',function() {
      var topNow = $ph.parent().offset().top;
      
      var newTop = topNow + 20;
      if (newTop > $ph.parent().originalTop) newTop = $ph.parent().originalTop;
      $ph.parent().offset({left: $ph.parent().originalLeft, top: newTop});
    }); 
    $ph.parent().die('swipeup');
    $ph.parent().live('swipeup',function() {
      var topNow = $ph.parent().offset().top;
      $ph.parent().offset({left: $ph.parent().originalLeft, top: topNow - 20});

    }); */
  }
    
   
  
},

{
  getDataTableOptions : function(options) {
    var dtData = {};

    if(options.tableStyle == "themeroller"){
      dtData.bJQueryUI = true;
    }
    dtData.bInfo = options.info;
    dtData.iDisplayLength = options.displayLength;
    dtData.bLengthChange = options.lengthChange;
    dtData.bPaginate = options.paginate;
    dtData.bSort = options.sort;
    dtData.bFilter = options.filter;
    dtData.sPaginationType = options.paginationType;  
    dtData.sDom = options.sDom;
    dtData.aaSorting = options.sortBy;
    dtData.oLanguage = options.oLanguage;

    if(options.colHeaders != undefined){
      dtData.aoColumns = new Array(options.colHeaders.length);
      for(var i = 0; i< options.colHeaders.length; i++){
        dtData.aoColumns[i]={}
        dtData.aoColumns[i].sClass="column"+i;
      };
      $.each(options.colHeaders,function(i,val){
        dtData.aoColumns[i].sTitle=val;
        if(val == "") dtData.aoColumns[i].bVisible=false;
      });  // colHeaders
      if(options.colTypes!=undefined){
        $.each(options.colTypes,function(i,val){
          var col = dtData.aoColumns[i];
          // Specific case: hidden cols
          if(val == "hidden") col.bVisible=false;
          col.sClass+=" "+val;
          col.sType=val;

        })
      };  // colTypes
      if(options.colFormats!=undefined){
      // Changes are made directly to the json

      };  // colFormats

      var bAutoWidth = true;
      if(options.colWidths!=undefined){
        $.each(options.colWidths,function(i,val){
          if (val!=null){
            dtData.aoColumns[i].sWidth=val;
            bAutoWidth = false;
          }
        })
      }; //colWidths
      dtData.bAutoWidth = bAutoWidth;

      if(options.colSortable!=undefined){
        $.each(options.colSortable,function(i,val){
          if (val!=null && ( !val || val == "false" ) ){
            dtData.aoColumns[i].bSortable=false
          }
        })
      }; //colSortable
      if(options.colSearchable!=undefined){
        $.each(options.colSearchable,function(i,val){
          if (val!=null && ( !val || val == "false" ) ){
            dtData.aoColumns[i].bSearchable=false
          }
        })
      }; //colSearchable

    }

    return dtData;
  }
});




/* Setup default values for CCC extension points
 *
 * We need some sensible defaults for properties that are
 * only accessible via extension points. To do this, we
 * extract the CCC components from the dashboard (they can be
 * identified through their type, which always starts with
 * 'ccc'), and add the defaults to the extension point object
 * found, if any (or create a new one if none is available)
 */
$(window).bind('cdfAboutToLoad',function(){
  chartDetector = /^ccc/;
  var charts = Dashboards.components.filter(function(component){
    return chartDetector.test(component.type);
  });
  var defaults = {
    yAxisLabel_textStyle: 'black',
    xAxisLabel_textStyle: 'black',
    legendLabel_textStyle: 'black'
  }
  for (c in charts) if (charts.hasOwnProperty(c)) {
    var chart = charts[c].chartDefinition;
    var exts = Dashboards.propertiesArrayToObject(chart.extensionPoints || {});
    exts = $.extend({}, defaults, exts);    
    chart.extensionPoints = Dashboards.objectToPropertiesArray(exts);
  }
});



(function() {
// initializes touch and scroll events
        var supportTouch = $.support.touch,
                scrollEvent = "touchmove scroll",
                touchStartEvent = supportTouch ? "touchstart" : "mousedown",
                touchStopEvent = supportTouch ? "touchend" : "mouseup",
                touchMoveEvent = supportTouch ? "touchmove" : "mousemove";

 // handles swipeup and swipedown
        $.event.special.swipeupdown = {
            setup: function() {
                var thisObject = this;
                var $this = $(thisObject);

                $this.bind(touchStartEvent, function(event) {
                    var data = event.originalEvent.touches ?
                            event.originalEvent.touches[ 0 ] :
                            event,
                            start = {
                                time: (new Date).getTime(),
                                coords: [ data.pageX, data.pageY ],
                                origin: $(event.target)
                            },
                            stop;

                    function moveHandler(event) {
                        if (!start) {
                            return;
                        }

                        var data = event.originalEvent.touches ?
                                event.originalEvent.touches[ 0 ] :
                                event;
                        stop = {
                            time: (new Date).getTime(),
                            coords: [ data.pageX, data.pageY ]
                        };

                        // prevent scrolling
                        if (Math.abs(start.coords[1] - stop.coords[1]) > 10) {
                            event.preventDefault();
                        }
                    }

                    $this
                            .bind(touchMoveEvent, moveHandler)
                            .one(touchStopEvent, function(event) {
                        $this.unbind(touchMoveEvent, moveHandler);
                        if (start && stop) {
                            if (stop.time - start.time < 1000 &&
                                    Math.abs(start.coords[1] - stop.coords[1]) > 30 &&
                                    Math.abs(start.coords[0] - stop.coords[0]) < 75) {
                                start.origin
                                        .trigger("swipeupdown")
                                        .trigger(start.coords[1] > stop.coords[1] ? "swipeup" : "swipedown");
                            }
                        }
                        start = stop = undefined;
                    });
                });
            }
        };

//Adds the events to the jQuery events special collection
        $.each({
            swipedown: "swipeupdown",
            swipeup: "swipeupdown"
        }, function(event, sourceEvent){
            $.event.special[event] = {
                setup: function(){
                    $(this).bind(sourceEvent, $.noop);
                }
            };
        });

    })();