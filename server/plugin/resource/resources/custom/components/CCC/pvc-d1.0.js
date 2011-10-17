
var pvc = {

  debug: false

}

/**
 *
 *  Utility function for logging messages to the console
 *
 */

pvc.log = function(m){

    if (typeof console != "undefined" && pvc.debug){
        console.log("[pvChart]: " + m);
    }
};

/**
 *
 * Evaluates x if it's a function or returns the value otherwise
 *
 */

pvc.ev = function(x){
    return typeof x == "function"?x():x;
};

pvc.sumOrSet = function(v1,v2){
    return typeof v1 == "undefined"?v2:v1+v2;
}

pvc.nonEmpty = function(d){
    return typeof d != "undefined" && d !== null;
}

pvc.padMatrixWithZeros = function(d){
    return d.map(function(v){
        return v.map(function(a){
            return typeof a == "undefined"?0:a;
        })
    })
}

pvc.cloneMatrix = function(m){
    return m.map(function(d){
        return d.slice()
    });
}


/**
 *
 * Implements filter property if not implemented yet
 *
 */
if (!Array.prototype.filter)
{
    Array.prototype.filter = function(fun, thisp)
    {
        var len = this.length >>> 0;
        if (typeof fun != "function")
            throw new TypeError();

        var res = [];
        var thisp = arguments[1];
        for (var i = 0; i < len; i++)
        {
            if (i in this)
            {
                var val = this[i]; // in case fun mutates this
                if (fun.call(thisp, val, i, this))
                    res.push(val);
            }
        }

        return res;
    };
}


/**
 *
 * Implements support for svg detection
 *
 **/
(function($){
    $.support.svg = $.support.svg || document.implementation.hasFeature("http://www.w3.org/TR/SVG11/feature#BasicStructure", "1.1");
})(jQuery);
/**
 * The main component
 */


pvc.Base = Base.extend({

    options: {},
    isPreRendered: false,
    isAnimating: false,

    // data
    dataEngine: null,
    resultset:[],
    metadata: [],

    // panels
    basePanel: null,
    titlePanel: null,
    legendPanel: null,

    legendSource: "series",
    colors: null,

    // renderCallback
    renderCallback: undefined,

    constructor: function(options){
        var myself = this;
        var _defaults = {
            canvas: null,
            width: 400,
            height: 300,
            originalWidth: 400,
            originalHeight: 300,
            crosstabMode: true,
            seriesInRows: false,
            animate: true,

            title: null,
            titlePosition: "top", // options: bottom || left || right
            titleAlign: "center", // left / right / center
            legend: false,
            legendPosition: "bottom",
            colors: null,

            tooltipFormat: function(s,c,v){
                return s+", "+c+":  " + myself.options.valueFormat(v) ;
            },

            valueFormat: function(d){
                return pv.Format.number().fractionDigits(0, 2).format(pv.Format.number().fractionDigits(0, 10).parse(d));
            },
            clickable: false,
            clickAction: function(s, c, v){
                pvc.log("You clicked on series " + s + ", category " + c + ", value " + v);
            }

        };
    
        this.options = {},

        // Apply options
        $.extend(this.options, _defaults);

        // Create DataEngine
        this.dataEngine = new pvc.DataEngine(this);

    },


    /**
     *
     * Building the visualization has 2 stages: First the preRender prepares and
     * build every object that will be used; Later
     *
     */

    preRender: function(){

        pvc.log("Prerendering in pvc");
        // Now's as good a time as any to completely clear out all tipsy tooltips
        try {
            $('.tipsy').remove();
        } catch(e) {
            // Do nothing
        }
        // If we don't have data, we just need to set a "no data" message
        // and go on with life.
        if (!this.allowNoData && this.resultset.length === 0) {
            throw new NoDataException();
        }

        // Disable animation if browser doesn't support it
        if(!$.support.svg){
            this.options.animate = false;
        }

        // Getting data engine and initialize the translator
        this.dataEngine.clearDataCache();
        this.dataEngine.setData(this.metadata,this.resultset);
        this.dataEngine.setCrosstabMode(this.options.crosstabMode);
        this.dataEngine.setSeriesInRows(this.options.seriesInRows);
        this.dataEngine.createTranslator();

        pvc.log(this.dataEngine.getInfo());
        // Create the color info
        if (typeof this.options.colors == 'undefined' || this.options.colors == null || this.options.colors.length == 0){
            this.colors = pv.Colors.category10;
        }
        else{
            this.colors = function() {
                var scale = pv.colors(this.options.colors);
                scale.domain.apply(scale, arguments);
                return scale;
            };
        }


        // create the basePanel. Since we don't have a parent panel we need to
        // manually create the points

        this.basePanel = new pvc.BasePanel(this); // Base panel, no parent
        this.basePanel.setSize(this.options.width, this.options.height);
        this.basePanel.create();
        this.basePanel.getPvPanel().canvas(this.options.canvas);

        // Title
        if (this.options.title != null && this.options.title.lengh != ""){
            this.titlePanel = new pvc.TitlePanel(this, {
                title: this.options.title,
                anchor: this.options.titlePosition,
                titleSize: this.options.titleSize,
                titleAlign: this.options.titleAlign
            });

            this.titlePanel.appendTo(this.basePanel); // Add it

        }


        // Legend
        if (this.options.legend){
            this.legendPanel = new pvc.LegendPanel(this, {
                anchor: this.options.legendPosition,
                legendSize: this.options.legendSize,
                align: this.options.legendAlign,
                minMarginX: this.options.legendMinMarginX,
                minMarginY: this.options.legendMinMarginY,
                textMargin: this.options.legendTextMargin,
                padding: this.options.legendPadding,
                textAdjust: this.options.legendTextAdjust,
                shape: this.options.legendShape,
                markerSize: this.options.legendMarkerSize,
                drawLine: this.options.legendDrawLine,
                drawMarker: this.options.legendDrawMarker
            });

            this.legendPanel.appendTo(this.basePanel); // Add it

        }

        this.isPreRendered = true;

    },

    /**
     *
     * Render the visualization. If not prerendered, do it now
     *
     */

    render: function(bypassAnimation){
        try {
            if(!this.isPreRendered){
                this.preRender();
            }

            if( typeof this.options.renderCallback !== "undefined" ){
                this.options.renderCallback.call(this);
            }
        
            this.basePanel.getPvPanel().render();
    
            if(this.options.animate == true && !bypassAnimation){
                this.isAnimating = true;
                this.basePanel.getPvPanel().transition()
                .duration( 2000)
                .ease("cubic-in-out")
                .start();
            }
        } catch (e) {
            if(e instanceof NoDataException) {

                if (!this.basePanel) {
                    pvc.log("No panel");
                    this.basePanel = new pvc.BasePanel(this); // Base panel, no parent
                    this.basePanel.setSize(this.options.width, this.options.height);
                    this.basePanel.create();
                    this.basePanel.getPvPanel().canvas(this.options.canvas);
                }
                pvc.log("creating message");
                var message = this.basePanel.getPvPanel().anchor("center").add(pv.Label);
                message.text("No data found");
                this.basePanel.extend(message,"noDataMessage_");
                this.basePanel.getPvPanel().render();
            } else {
                // We don't know how to handle this
                throw e;
            }
        }


    },


    /**
     * Method to set the data to the chart. Expected object is the same as what
     * comes from the CDA: {metadata: [], resultset: []}
     */

    setData: function(data, options){
        this.setResultset(data.resultset);
        this.setMetadata(data.metadata);

        $.extend(this.options,options);
    },


    /**
     * Sets the resultset that will be used to build the chart
     */

    setResultset: function(resultset){

        this.resultset = resultset;
        if (resultset.length == 0){
            pvc.log("Warning: Resultset is empty")
        }

    },


    /**
     * Sets the metadata that, optionally, will give more information for building
     * the chart
     */

    setMetadata: function(metadata){

        this.metadata = metadata;
        if (metadata.length == 0){
            pvc.log("Warning: Metadata is empty")
        }

    },

    /*
     * Animation
     */

    animate: function(start, end){

        if (this.options.animate == false || this.isAnimating == true){
            return end;
        }
        else{
            return start
        }

    }



});


/**
 *
 * Base panel. A lot of them will exist here, with some common properties.
 * Each class that extends pvc.base will be responsible to know how to use it
 *
 */
pvc.BasePanel = Base.extend({

    chart: null,
    _parent: null,
    type: pv.Panel, // default one
    height: null,
    width: null,
    anchor: "top",
    pvPanel: null,
    fillColor: "red",
    margins: null,

    constructor: function(chart,options){

        this.chart = chart;
        $.extend(this,options);

        this.margins = {
            top:0,
            right: 0,
            bottom: 0,
            left: 0
        }

    },


    create: function(){

        if(this._parent == null){
            // Should be created for the vis panel only
            this.pvPanel = new pv.Panel();
            this.extend(this.pvPanel,"base_");
        }
        else{
            this.pvPanel = this._parent.pvPanel.add(this.type);
        }

        this.pvPanel
        .width(this.width)
        .height(this.height);

    },


    /*
     *  Create the panel, appending it to the previous one using a specified anchor.
     *
     *  Will:
     *  1) create the panel.
     *  2) subtract it's size from the previous panel's size
     *  3) append it to the previous one in the correct position
     *
     */

    appendTo: function(_parent){

        this._parent = _parent;
        this.create();

        // Reduce size and update margins
        var a = this.anchor;
        if(a == "top" || a == "bottom"){
            this._parent.height -= this.height;
        }
        else{
            this._parent.width -= this.width;
        }


    
        // See where to attach it.
        this.pvPanel[a](this._parent.margins[a]);
        this.pvPanel[pvc.BasePanel.relativeAnchor[a]](this._parent.margins[pvc.BasePanel.relativeAnchor[a]]);

        // update margins
        if(a == "top" || a == "bottom"){
            this._parent.margins[this.anchor] += this.height;
        }
        else{
            this._parent.margins[a] += this.width;
        }

    },


    /**
     *
     * This is the method to be used for the extension points for the specific
     * contents of the chart.
     *already ge a pie chart!
     * Goes through the list of options and, if it matches the prefix, execute that
     * method on the mark. WARNING: It's user's reponsability to make sure some
     * unexisting method won't blow this
     *
     */

    extend: function(mark, prefix){

        for (p in this.chart.options.extensionPoints){
            if (p.indexOf(prefix) == 0){
                var m = p.substring(prefix.length);
                // Distinguish between mark methods and properties
                if (typeof mark[m] === "function") {
                    mark[m](this.chart.options.extensionPoints[p]);
                } else {
                    mark[m] = this.chart.options.extensionPoints[p];
                }
            }

        }

    },

    /*
     * Sets the size for the panel, when he parent panel is undefined
     */

    setSize: function(w,h){
        this.width = w;
        this.height = h;

    },

    /*
     * returns the width of the Panel
     */
    getWidth: function(){
        return this.width
    },

    /*
     * returns the height of the Panel
     */
    getHeight: function(){
        return this.height
    },

    /*
     * Returns the underlying protovis Panel
     */
    getPvPanel: function(){
        return this.pvPanel
    }


},{
    // determine what is the associated method to call to position the labels
    // correctly

    relativeAnchor: {
        top: "left",
        bottom: "left",
        left: "bottom",
        right: "bottom"
    },

    relativeAnchorMirror: {
        top: "right",
        bottom: "right",
        left: "top",
        right: "top"
    },

    oppositeAnchor:{
        top: "bottom",
        bottom: "top",
        left: "right",
        right: "left"
    },

    paralelLength:{
        top: "width",
        bottom: "width",
        right: "height",
        left: "height"
    },

    orthogonalLength:{
        top: "height",
        bottom: "height",
        right: "width",
        left: "width"
    }

})


/*
 * Title panel. Generates the title. Specific options are:
 * <i>title</i> - text. Default: null
 * <i>titlePosition</i> - top / bottom / left / right. Default: top
 * <i>titleSize</i> - The size of the title in pixels. Default: 25
 *
 * Has the following protovis extension points:
 *
 * <i>title_</i> - for the title Panel
 * <i>titleLabel_</i> - for the title Label
 */
pvc.TitlePanel = pvc.BasePanel.extend({
  
    _parent: null,
    pvLabel: null,
    anchor: "top",
    titlePanel: null,
    title: null,
    titleSize: 25,
    titleAlign: "center",
    font: "14px sans-serif",



    constructor: function(chart, options){

        this.base(chart,options);

    },

    create: function(){

        // Size will depend on positioning and font size mainly
    
        if (this.anchor == "top" || this.anchor == "bottom"){
            this.width = this._parent.width;
            this.height = this.titleSize;
        }
        else{
            this.height = this._parent.height;
            this.width = this.titleSize;
        }


        this.pvPanel = this._parent.getPvPanel().add(this.type)
        .width(this.width)
        .height(this.height)

        // Extend title
        this.extend(this.pvPanel,"title_");

        var rotation = {
            top: 0,
            right: Math.PI/2,
            bottom: 0,
            left: -Math.PI/2
        };

        // label
        this.pvLabel = this.pvPanel.add(pv.Label)
        .text(this.title)
        .font(this.font)
        .textAlign("center")
        .textBaseline("middle")
        .bottom(this.height/2)
        .left(this.width/2)
        .textAngle(rotation[this.anchor]);

        // Cases:
        if(this.titleAlign == "center"){
            this.pvLabel
            .bottom(this.height/2)
            .left(this.width/2)
        }
        else{

            this.pvLabel.textAlign(this.titleAlign);

            if ( this.anchor == "top" || this.anchor == "bottom"){

                this.pvLabel.bottom(null).left(null); // reset
                this.pvLabel[this.titleAlign](0)
                .bottom(this.height/2)

            }
            else if (this.anchor == "right"){
                this.titleAlign=="left"?this.pvLabel.bottom(null).top(0):this.pvLabel.bottom(0);
            }
            else if (this.anchor == "left"){
                this.titleAlign=="right"?this.pvLabel.bottom(null).top(0):this.pvLabel.bottom(0);
            }
        }


        // Extend title label
        this.extend(this.pvLabel,"titleLabel_");

    }


});
/*
 * Legend panel. Generates the legend. Specific options are:
 * <i>legend</i> - text. Default: false
 * <i>legendPosition</i> - top / bottom / left / right. Default: bottom
 * <i>legendSize</i> - The size of the legend in pixels. Default: 25
 *
 * Has the following protovis extension points:
 *
 * <i>legend_</i> - for the legend Panel
 * <i>legendRule_</i> - for the legend line (when applicable)
 * <i>legendDot_</i> - for the legend marker (when applicable)
 * <i>legendLabel_</i> - for the legend label
 * 
 */
pvc.LegendPanel = pvc.BasePanel.extend({

  _parent: null,
  pvRule: null,
  pvDot: null,
  pvLabel: null,


  anchor: "bottom",
  align: "left",
  pvLegendPanel: null,
  legend: null,
  legendSize: null,
  minMarginX: 8,
  minMarginY: 20,
  textMargin: 6,
  padding: 24,
  textAdjust: 7,
  shape: "square",
  markerSize: 15,
  drawLine: false,
  drawMarker: true,




  constructor: function(chart, options){

    this.base(chart,options);

  },

  create: function(){
    var myself = this;
    var c = this.chart.colors();
    var x,y;


    //pvc.log("Debug PMartins");

    var data = this.chart.legendSource=="series"?
    this.chart.dataEngine.getSeries():
    this.chart.dataEngine.getCategories();



    //determine the size of the biggest cell
    //Size will depend on positioning and font size mainly
    var maxtext = 0;
    for (i in data){
      maxtext = maxtext < data[i].length?data[i].length:maxtext;
    }
    var cellsize = this.markerSize + maxtext*this.textAdjust;

    var realxsize, realysize;


    if (this.anchor == "top" || this.anchor == "bottom"){
      this.width = this._parent.width;
      this.height = this.legendSize;
      var maxperline = data.length;

      //if the legend is bigger than the available size, multi-line and left align
      if(maxperline*(cellsize + this.padding) - this.padding + myself.minMarginX > this.width){
        this.align = "left";
        maxperline = Math.floor((this.width + this.padding - myself.minMarginX)/(cellsize + this.padding));
      }
      realxsize = maxperline*(cellsize + this.padding) + myself.minMarginX - this.padding;
      realysize = myself.padding*(Math.ceil(data.length/maxperline));

      if(this.heigth == null){
        this.height = realysize;
      }

      //changing margins if the alignment is not "left"
      if(this.align == "right"){
        myself.minMarginX = this.width - realxsize;
      }
      else if (this.align == "center"){
        myself.minMarginX = (this.width - realxsize)/2;
      }

      x = function(){
        var n = Math.ceil(this.index/maxperline);
        return (this.index%maxperline)*(cellsize + myself.padding) + myself.minMarginX;
      }
      myself.minMarginY = (myself.height - realysize)/2;
      y = function(){
        var n = Math.floor(this.index/maxperline); 
        return myself.height  - n*myself.padding - myself.minMarginY - myself.padding/2;
      }

    }
    else{
      this.height = this._parent.height;
      this.width = this.legendSize;
      realxsize = cellsize + this.minMarginX;
      realysize = myself.padding*data.length;
      if(this.align == "middle"){
        myself.minMarginY = (myself.height - realysize + myself.padding)/2  ;
      }
      else if (this.align == "bottom"){
        myself.minMarginY = myself.height - realysize;
      }
      x = myself.minMarginX;
      y = function(){
        return myself.height - this.index*myself.padding - myself.minMarginY;
      }
    }

    if(this.width == null){
      this.width = realxsize;
    }

    this.pvPanel = this._parent.getPvPanel().add(this.type)
    .width(this.width)
    .height(this.height)    



    //********** Markers and Lines ***************************

    this.pvLegendPanel = this.pvPanel.add(pv.Panel)
    .data(data)
    .def("hidden","false")
    .left(x)
    .bottom(y)
    .height(this.markerSize)
    .cursor("pointer")
    .fillStyle(function(){
      return this.hidden()=="true"?"rgba(200,200,200,1)":"rgba(200,200,200,0.0001)";
    })
    .event("click",function(e){

      return myself.toggleVisibility(this.index);

    });

    // defined font function
    var computeDecoration = function(idx){
      if(myself.chart.dataEngine.isVisible(myself.chart.legendSource,idx)){
        return "";
      }
      else{
        return "line-through"
      }
    }
    var computeTextStyle = function(idx){
      if(myself.chart.dataEngine.isVisible(myself.chart.legendSource,idx)){
        return "black"
      }
      else{
        return "#ccc"
      }
    }

    if(this.drawLine == true && this.drawMarker == true){
      
      this.pvRule = this.pvLegendPanel.add(pv.Rule)
      .left(0)
      .width(this.markerSize)
      .lineWidth(1)
      .strokeStyle(function(){
        return c(this.index);
      })

      this.pvDot = this.pvRule.anchor("center").add(pv.Dot)
      .shapeSize(this.markerSize)
      .shape(this.shape)
      .lineWidth(0)
      .fillStyle(function(){
        return c(this.parent.index);
      })

      this.pvLabel = this.pvDot.anchor("right").add(pv.Label)
      .textMargin(myself.textMargin)
    }
    else if(this.drawLine == true){
      
      this.pvRule = this.pvLegendPanel.add(pv.Rule)
      .left(0)
      .width(this.markerSize)
      .lineWidth(1)
      .strokeStyle(function(){
        return c(this.parent.index);
      })

      this.pvLabel = this.pvRule.anchor("right").add(pv.Label)
      .textMargin(myself.textMargin)

    }
    else if(this.drawMarker == true){

      this.pvDot = this.pvLegendPanel.add(pv.Dot)
      .left(this.markerSize/2)
      .shapeSize(this.markerSize)
      .shape(this.shape)
      .lineWidth(0)
      .fillStyle(function(){
        return c(this.parent.index);
      })


      this.pvLabel = this.pvDot.anchor("right").add(pv.Label)
      .textMargin(myself.textMargin)
    
    }

    this.pvLabel
    .textDecoration(function(){
      return computeDecoration(this.parent.index)
    })
    .textStyle(function(){
      return computeTextStyle(this.parent.index)
    })

    // Extend legend
    this.extend(this.pvPanel,"legendArea_");
    this.extend(this.pvLegendPanel,"legendPanel_");
    this.extend(this.pvRule,"legendRule_");
    this.extend(this.pvDot,"legendDot_");
    this.extend(this.pvLabel,"legendLabel_");


  },

  toggleVisibility: function(idx){
    
    pvc.log("Worked. Toggling visibility of index " + idx);
    this.chart.dataEngine.toggleVisibility(this.chart.legendSource,idx);

    // Forcing removal of tipsy legends
    try{
      $(".tipsy").remove();
    }catch(e){
      // Do nothing
    }

    // Rerender chart
    this.chart.preRender();
    this.chart.render(true);
    
    return this.pvLabel;
  }


});

/**
 * TimeseriesAbstract is the base class for all categorical or timeseries
 */

pvc.TimeseriesAbstract = pvc.Base.extend({

  allTimeseriesPanel : null,

  constructor: function(o){

    this.base();

    var _defaults = {
      showAllTimeseries: true,
      allTimeseriesPosition: "bottom",
      allTimeseriesSize: 50
    };


    // Apply options
    $.extend(this.options,_defaults, o);


  },

  preRender: function(){

    this.base();


    // Do we have the timeseries panel? add it

    if (this.options.showAllTimeseries){
      this.allTimeseriesPanel = new pvc.AllTimeseriesPanel(this, {
        anchor: this.options.allTimeseriesPosition,
        allTimeseriesSize: this.options.allTimeseriesSize

      });

      this.allTimeseriesPanel.appendTo(this.basePanel); // Add it

    }

  }

}
)


/*
 * AllTimeseriesPanel panel. Generates a small timeseries panel that the user
 * can use to select the range:
 * <i>allTimeseriesPosition</i> - top / bottom / left / right. Default: top
 * <i>allTimeseriesSize</i> - The size of the timeseries in pixels. Default: 100
 *
 * Has the following protovis extension points:
 *
 * <i>allTimeseries_</i> - for the title Panel
 * 
 */
pvc.AllTimeseriesPanel = pvc.BasePanel.extend({

  _parent: null,
  pvAllTimeseriesPanel: null,
  anchor: "bottom",
  allTimeseriesSize: 50,



  constructor: function(chart, options){

    this.base(chart,options);

  },

  create: function(){

    // Size will depend on positioning and font size mainly

    if (this.anchor == "top" || this.anchor == "bottom"){
      this.width = this._parent.width;
      this.height = this.allTimeseriesSize;
    }
    else{
      this.height = this._parent.height;
      this.width = this.allTimeseriesSize;
    }


    this.pvPanel = this._parent.getPvPanel().add(this.type)
    .width(this.width)
    .height(this.height)

    // Extend panel
    this.extend(this.pvPanel,"allTimeseries_");


  }


});



/**
 * CategoricalAbstract is the base class for all categorical or timeseries
 */

pvc.CategoricalAbstract = pvc.TimeseriesAbstract.extend({

    yAxisPanel : null,
    xAxisPanel : null,
    secondXAxisPanel: null,
    secondYAxisPanel: null,

    yScale: null,
    xScale: null,

    prevMax: null,
    prevMin: null,


    constructor: function(o){

        this.base(o);

        var _defaults = {
            showAllTimeseries: false, // meaningless here
            showXScale: true,
            showYScale: true,
            yAxisPosition: "left",
            xAxisPosition: "bottom",
            yAxisSize: 50,
            xAxisSize: 50,
            xAxisFullGrid: false,
            yAxisFullGrid: false,

            secondAxis: false,
            secondAxisIdx: -1,
            secondAxisIndependentScale: false,
            secondAxisOriginIsZero: true,
            secondAxisOffset: 0,
            secondAxisColor: "blue",
            secondAxisSize: 0, // calculated

            // CvK  added extra parameter for implementation of HeatGrid
            orthoAxisOrdinal: false
        // if orientation==vertical then perpendicular-axis is the y-axis
        //  else perpendicular-axis is the x-axis.
        };


        // Apply options
        $.extend(this.options,_defaults, o);
        // Sanitize some options:
        if (this.options.showYScale == false){
            this.options.yAxisSize = 0
        }
        if (this.options.showXScale == false){
            this.options.xAxisSize = 0
        }

        if(this.options.secondAxis && this.options.secondAxisIndependentScale){
            this.options.secondAxisSize = this.options.orientation=="vertical"?this.options.yAxisSize:this.options.xAxisSize;
        }
        else{
            this.options.secondAxisSize = 0;
        }

    },

    preRender: function(){


        this.base();

        pvc.log("Prerendering in CategoricalAbstract");
        
        // Sanitize some options:
        if (this.options.showYScale == false){
            this.options.yAxisSize = 0
        }
        if (this.options.showXScale == false){
            this.options.xAxisSize = 0
        }

        this.xScale = this.getXScale();
        this.yScale = this.getYScale();
        this.secondScale =  this.options.secondAxisIndependentScale?this.getSecondScale(): this.getLinearScale();


        // Generate axis

        if(this.options.secondAxis)
            this.generateSecondXAxis(); // this goes before the other because of the fullGrid
        this.generateXAxis();
        if(this.options.secondAxis)
            this.generateSecondYAxis(); // this goes before the other because of the fullGrid
        this.generateYAxis();




    },


    /*
     * Generates the X axis. It's in a separate function to allow overriding this value
     */

    generateXAxis: function(){

        if (this.options.showXScale){
            this.xAxisPanel = new pvc.XAxisPanel(this, {
                ordinal: this.isXAxisOrdinal(),
                showAllTimeseries: false,
                anchor: this.options.xAxisPosition,
                axisSize: this.options.xAxisSize,
                oppositeAxisSize: this.options.yAxisSize,
                fullGrid:  this.options.xAxisFullGrid,
                elements: this.getAxisOrdinalElements("x")
            });

            //            this.xAxisPanel.setScale(this.xScale);
            this.xAxisPanel.setScale(this.xScale);
            this.xAxisPanel.appendTo(this.basePanel); // Add it

        }


    },


    /*
     * Generates the Y axis. It's in a separate function to allow overriding this value
     */

    generateYAxis: function(){

        if (this.options.showYScale){
            this.yAxisPanel = new pvc.YAxisPanel(this, {
                ordinal: this.isYAxisOrdinal(),
                showAllTimeseries: false,
                anchor: this.options.yAxisPosition,
                axisSize: this.options.yAxisSize,
                oppositeAxisSize: this.options.xAxisSize,
                fullGrid:  this.options.yAxisFullGrid,
                elements: this.getAxisOrdinalElements("y")
            });

            this.yAxisPanel.setScale(this.yScale);
            this.yAxisPanel.appendTo(this.basePanel); // Add it

        }

    },


    /*
     * Generates the second axis for X, if exists and only for vertical horizontal charts
     */

    generateSecondXAxis: function(){

        if( this.options.secondAxisIndependentScale && this.options.orientation == "horizontal"){
            this.secondXAxisPanel = new pvc.XAxisPanel(this, {
                ordinal: this.isXAxisOrdinal(),
                showAllTimeseries: false,
                anchor: pvc.BasePanel.oppositeAnchor[this.options.xAxisPosition],
                axisSize: this.options.secondAxisSize,
                oppositeAxisSize: this.options.yAxisSize,
                fullGrid:  false, // not supported
                elements: this.getAxisOrdinalElements("x"),
                tickColor: this.options.secondAxisColor
            });

            this.secondXAxisPanel.setScale(this.secondScale);
            this.secondXAxisPanel.appendTo(this.basePanel); // Add it
        }
    },

    /*
     * Generates the second axis for Y, if exists and only for vertical horizontal charts
     */

    generateSecondYAxis: function(){

        if( this.options.secondAxisIndependentScale && this.options.orientation == "vertical"){

            this.secondYAxisPanel = new pvc.YAxisPanel(this, {
                ordinal: this.isYAxisOrdinal(),
                showAllTimeseries: false,
                anchor: pvc.BasePanel.oppositeAnchor[this.options.yAxisPosition],
                axisSize: this.options.secondAxisSize,
                oppositeAxisSize: this.options.xAxisSize,
                fullGrid:  false, // not supported
                elements: this.getAxisOrdinalElements("y"),
                tickColor: this.options.secondAxisColor
            });

            this.secondYAxisPanel.setScale(this.secondScale);
            this.secondYAxisPanel.appendTo(this.basePanel); // Add it

        }
    },



    /*
     * Indicates if xx is an ordinal scale
     */

    isXAxisOrdinal: function(){
        var isOrdinal = false;
        if (this.options.orientation == "vertical") 
            isOrdinal = !(this.options.timeSeries);
        else 
            isOrdinal =  this.options.orthoAxisOrdinal;
        return isOrdinal;
    },


    /*
     * Indicates if yy is an ordinal scale
     */

    isYAxisOrdinal: function(){
        var isOrdinal = false;
        if (this.options.orientation == "vertical")
            isOrdinal =  this.options.orthoAxisOrdinal;
        else
            isOrdinal = !(this.options.timeSeries);
        return isOrdinal;
    },

    /*
     *  List of elements to use in the axis ordinal
     *
     */
    getAxisOrdinalElements: function(axis){
        var onSeries = false;

        // onSeries can only be true if the perpendicular axis is ordinal
        if (this.options.orthoAxisOrdinal) {
            if (axis == "x")
                onSeries = ! (this.options.orientation == "vertical");
            else
                onSeries = this.options.orientation == "vertical";
        }
        
        return onSeries ?
        this.dataEngine.getVisibleSeries() :
        this.dataEngine.getVisibleCategories();
    },



    /*
     * xx scale for categorical charts
     */

    getXScale: function(){
        var scale = null;

        if (this.options.orientation == "vertical") {
            scale = this.options.timeSeries  ?
            this.getTimeseriesScale(false,true)     :
            this.getOrdinalScale();
        } else {
            scale =  (this.options.orthoAxisOrdinal) ?
            this.getPerpOrdinalScale("x")    :
            this.getLinearScale(false,true);
        } 

        return scale;
    },

    /*
     * yy scale for categorical charts
     */

    getYScale: function(){
        var scale = null;
        if (this.options.orientation == "vertical") {
            scale =  (this.options.orthoAxisOrdinal) ?
            this.getPerpOrdinalScale("y")    :
            this.getLinearScale();
        } else { 
            scale = this.options.timeSeries  ?
            this.getTimeseriesScale()     :
            this.getOrdinalScale();
        }
        return scale;
    },

    /*
     * Helper function to facilitate  (refactoring)
     *     - getOrdinalScale()
     *     - getPerpOrdScale()
     *   (CvK)
     */
    getOrdScale: function(bypassAxis, orthoAxis){

        var yAxisSize = bypassAxis?0:this.options.yAxisSize;
        var xAxisSize = bypassAxis?0:this.options.xAxisSize;

        var secondXAxisSize = 0, secondYAxisSize = 0;
        
        if( this.options.orientation == "vertical"){
            secondYAxisSize = bypassAxis?0:this.options.secondAxisSize;
        }
        else{
            secondXAxisSize = bypassAxis?0:this.options.secondAxisSize;
        }

        if (orthoAxis) {   // added by CvK
            var categories = this.dataEngine.getVisibleSeries();
            var scale = new pv.Scale.ordinal(categories);

            if (orthoAxis == "y") {
                scale.min = 0;
                scale.max = this.basePanel.height - xAxisSize;
            } else {   // assume orthoAxis == "x"
                scale.min = yAxisSize;
                scale.max = this.basePanel.width;
            }

        } else {   // orthoAxis == false  (so normal ordinal axis)
            var categories = this.dataEngine.getVisibleCategories();
            var scale = new pv.Scale.ordinal(categories);

            var size = this.options.orientation=="vertical"?
            this.basePanel.width:
            this.basePanel.height;

            if (   this.options.orientation=="vertical"
                && this.options.yAxisPosition == "left"){
                scale.min = yAxisSize;
                scale.max = size - secondYAxisSize;
            }
            else if (   this.options.orientation=="vertical" 
                && this.options.yAxisPosition == "right"){
                scale.min = secondYAxisSize;
                scale.max = size-yAxisSize;
            }
            else{
                scale.min = secondYAxisSize;
                scale.max = size - xAxisSize - secondXAxisSize;
            }

        }  // end else-part -- if (orthoAxis)

        scale.splitBanded( scale.min, scale.max, this.options.panelSizeRatio);
        return scale;
    },

    /*
     * Scale for the ordinal axis. xx if orientation is vertical, yy otherwise
     *
     */
    getOrdinalScale: function(bypassAxis){
        var bpa = (bypassAxis) ? bypassAxis : null;
        var orthoAxis = null;
        var scale = this.getOrdScale(bpa, orthoAxis);
        return scale;
    },
    /*
     * Scale for the perpendicular ordinal axis.
     *     yy if orientation is vertical,
     *     xx otherwise
     *   (CvK)
     */
    getPerpOrdinalScale: function(orthoAxis){
        var bypassAxis = null;
        var scale = this.getOrdScale(bypassAxis, orthoAxis);
        return scale;
    },
    /**
    **/
    getLinearScale: function(bypassAxis,bypassOffset){

        var yAxisSize = bypassAxis?0:this.options.yAxisSize;
        var xAxisSize = bypassAxis?0:this.options.xAxisSize;

        var isVertical = this.options.orientation=="vertical"
        var size = isVertical?this.basePanel.height:this.basePanel.width;

        var max, min;

        if(this.options.stacked){
            max = this.dataEngine.getCategoriesMaxSumOfVisibleSeries();
            min = 0;
        }
        else{
            max = this.dataEngine.getVisibleSeriesAbsoluteMax();
            min = this.dataEngine.getVisibleSeriesAbsoluteMin();

        }
        
        /* If the bounds are the same, things break,
         * so we add a wee bit of variation.
         */
        if (min === max) {
            min = min !== 0 ? min * 0.99 : this.options.originIsZero ? 0 : -0.1;
            max = max !== 0 ? max * 1.01 : 0.1;
        }
        if(min * max > 0 && this.options.originIsZero){
            if(min > 0){
                min = 0;
            }else{
                max = 0;
            }
        }

        // CvK:  added to set bounds
        if(   ('orthoFixedMin' in this.options)
            && (this.options.orthoFixedMin != null)
            && !(isNaN(Number(this.options.orthoFixedMin))))
            min = this.options.orthoFixedMin;
        if(   ('orthoFixedMax' in this.options)
            && (this.options.orthoFixedMax != null)
            && !(isNaN(Number(this.options.orthoFixedMax))))
            max = this.options.orthoFixedMax;


        // Adding a small offset to the scale:
        var offset = (max - min) * this.options.axisOffset;
        offset = bypassOffset?0:offset;
        var scale = new pv.Scale.linear(min - (this.options.originIsZero && min == 0 ? 0 : offset),max + (this.options.originIsZero && max == 0 ? 0 : offset));


        if( !isVertical && this.options.yAxisPosition == "left"){
            scale.min = yAxisSize;
            scale.max = size;
            
        }
        else if( !isVertical && this.options.yAxisPosition == "right"){
            scale.min = 0;
            scale.max = size - yAxisSize;
        }
        else{
            scale.min = 0;
            scale.max = size - xAxisSize;
        }

        scale.range(scale.min, scale.max);
        return scale;

    },

    /*
     * Scale for the timeseries axis. xx if orientation is vertical, yy otherwise
     *
     */
    getTimeseriesScale: function(bypassAxis,bypassOffset){

        var yAxisSize = bypassAxis?0:this.options.yAxisSize;
        var xAxisSize = bypassAxis?0:this.options.xAxisSize;

        var size = this.options.orientation=="vertical"?
        this.basePanel.width:
        this.basePanel.height;

        var parser = pv.Format.date(this.options.timeSeriesFormat);
        var categories =  this.dataEngine.getVisibleCategories().sort(function(a,b){
            return parser.parse(a) - parser.parse(b)
        });


        // Adding a small offset to the scale:
        var max = parser.parse(categories[categories.length -1]);
        var min = parser.parse(categories[0]);        
        var offset = (max.getTime() - min.getTime()) * this.options.axisOffset;
        offset = bypassOffset?0:offset;

        var scale = new pv.Scale.linear(new Date(min.getTime() - offset),new Date(max.getTime() + offset));

        if(this.options.orientation=="vertical" && this.options.yAxisPosition == "left"){
            scale.min = yAxisSize;
            scale.max = size;
            
        }
        else if(this.options.orientation=="vertical" && this.options.yAxisPosition == "right"){
            scale.min = 0;
            scale.max = size - yAxisSize;
        }
        else{
            scale.min = 0;
            scale.max = size - xAxisSize;
        }

        scale.range( scale.min , scale.max);
        return scale;


    },

    /*
     * Scale for the linear axis. yy if orientation is vertical, xx otherwise
     *
     */
    getSecondScale: function(bypassAxis){

        if(!this.options.secondAxis || !this.options.secondAxisIndependentScale){
            return this.getLinearScale(bypassAxis);
        }

        var yAxisSize = bypassAxis?0:this.options.yAxisSize;
        var xAxisSize = bypassAxis?0:this.options.xAxisSize;

        var isVertical = this.options.orientation=="vertical"
        var size = isVertical?this.basePanel.height:this.basePanel.width;

        var max = this.dataEngine.getSecondAxisMax();
        var min = this.dataEngine.getSecondAxisMin();

        if(min * max > 0 && this.options.secondAxisOriginIsZero){
            if(min > 0){
                min = 0;
            }else{
                max = 0;
            }
        }

        // Adding a small offset to the scale:
        var offset = (max - min) * this.options.secondAxisOffset;
        var scale = new pv.Scale.linear(min - (this.options.secondAxisOriginIsZero && min == 0 ? 0 : offset),max + (this.options.secondAxisOriginIsZero && max == 0 ? 0 : offset));


        if( !isVertical && this.options.yAxisPosition == "left"){
            scale.min = yAxisSize;
            scale.max = size;

        }
        else if( !isVertical && this.options.yAxisPosition == "right"){
            scale.min = 0;
            scale.max = size - yAxisSize;
        }
        else{
            scale.min = 0;
            scale.max = size - xAxisSize;
        }

        scale.range(scale.min, scale.max);
        return scale;

    }

}
)


/*
 * AxisPanel panel.
 *
 * 
 */
pvc.AxisPanel = pvc.BasePanel.extend({

    _parent: null,
    pvRule: null,
    pvTicks: null,
    pvLabel: null,
    pvRuleGrid: null,
    pvScale: null,

    ordinal: false,
    anchor: "bottom",
    axisSize: 30,
    tickLength: 6,
    tickColor: "#aaa",
    oppositeAxisSize: 30,
    panelName: "axis", // override
    scale: null,
    fullGrid: false,
    elements: [], // To be used in ordinal scales


    constructor: function(chart, options){

        this.base(chart,options);

    },

    create: function(){

        // Size will depend only on the existence of the labels


        if (this.anchor == "top" || this.anchor == "bottom"){
            this.width = this._parent.width;
            this.height = this.axisSize;
        }
        else{
            this.height = this._parent.height;
            this.width = this.axisSize;
        }


        this.pvPanel = this._parent.getPvPanel().add(this.type)
        .width(this.width)
        .height(this.height)



        this.renderAxis();

        // Extend panel
        this.extend(this.pvPanel, this.panelName + "_");
        this.extend(this.pvRule, this.panelName + "Rule_");
        this.extend(this.pvTicks, this.panelName + "Ticks_");
        this.extend(this.pvLabel, this.panelName + "Label_");
        this.extend(this.pvRuleGrid, this.panelName + "Grid_");


    },


    setScale: function(scale){
        this.scale = scale;
    },

    renderAxis: function(){

        var min, max,myself=this;
        myself.pvScale = this.scale;
        myself.extend(myself.pvScale, myself.panelName + "Scale_");


        if (this.ordinal) {
            min = myself.pvScale.min;
            max = myself.pvScale.max;
        } else {
            var scaleRange = myself.pvScale.range();
            min = scaleRange[0];
            max = scaleRange[1];
        }
        this.pvRule = this.pvPanel
        .add(pv.Rule)
        .strokeStyle(this.tickColor)
        [pvc.BasePanel.oppositeAnchor[this.anchor]](0)
        [pvc.BasePanel.relativeAnchor[this.anchor]](min)
        [pvc.BasePanel.paralelLength[this.anchor]](max - min)

        if (this.ordinal == true){
            this.renderOrdinalAxis();
        }
        else{
            this.renderLinearAxis();
        }
    
    },
  

    renderOrdinalAxis: function(){

        var myself = this;

        var align =  (this.anchor == "bottom" || this.anchor == "top") ?
        "center" : 
        (this.anchor == "left")  ?
        "right" :
        "left";

        this.pvLabel = this.pvRule.add(pv.Label)
        .data(this.elements)
        [pvc.BasePanel.paralelLength[this.anchor]](null)
        [pvc.BasePanel.oppositeAnchor[this.anchor]](10)
        [pvc.BasePanel.relativeAnchor[this.anchor]](function(d){
            return myself.scale(d) + myself.scale.range().band/2;
        })
        .textAlign(align)
        .textBaseline("middle")
        .text(pv.identity)
        .font("9px sans-serif")
    },


    renderLinearAxis: function(){

        var myself = this;
    
        var scale = this.scale;
        
        this.pvTicks = this.pvRule.add(pv.Rule)
        .data(scale.ticks())
        [pvc.BasePanel.paralelLength[this.anchor]](null)
        [pvc.BasePanel.oppositeAnchor[this.anchor]](0)
        [pvc.BasePanel.relativeAnchor[this.anchor]](this.scale)
        [pvc.BasePanel.orthogonalLength[this.anchor]](function(d){
            return myself.tickLength/(this.index%2 + 1)
        })
        .strokeStyle(this.tickColor);

        this.pvLabel = this.pvTicks
        .anchor(this.anchor)
        .add(pv.Label)
        .text(scale.tickFormat)
        .font("9px sans-serif")
        .visible(function(d){
            // mini grids
            if (this.index % 2){
                return false;
            }
            // also, hide the first and last ones
            if( scale(d) == 0  || scale(d) == scale.range()[1] ){
                return false;
            }
            return true;
        })


        // Now do the full grids
        if(this.fullGrid){

            this.pvRuleGrid = this.pvRule.add(pv.Rule)
            .data(scale.ticks())
            .strokeStyle("#f0f0f0")
            [pvc.BasePanel.paralelLength[this.anchor]](null)
            [pvc.BasePanel.oppositeAnchor[this.anchor]](- this._parent[pvc.BasePanel.orthogonalLength[this.anchor]] +
                this[pvc.BasePanel.orthogonalLength[this.anchor]])
            [pvc.BasePanel.relativeAnchor[this.anchor]](scale)
            [pvc.BasePanel.orthogonalLength[this.anchor]](this._parent[pvc.BasePanel.orthogonalLength[this.anchor]] -
                this[pvc.BasePanel.orthogonalLength[this.anchor]])
            .visible(function(d){
                // mini grids
                if (this.index % 2){
                    return false;
                }
                // also, hide the first and last ones
                if( scale(d) == 0  || scale(d) == scale.range()[1] ){
                    return false;
                }
                return true;
            })
        }


    }



});

/*
 * XAxisPanel panel.
 *
 *
 */
pvc.XAxisPanel = pvc.AxisPanel.extend({

    anchor: "bottom",
    panelName: "xAxis",

    constructor: function(chart, options){

        this.base(chart,options);

    }

});


/*
 * YAxisPanel panel.
 *
 *
 */
pvc.YAxisPanel = pvc.AxisPanel.extend({

    anchor: "left",
    panelName: "yAxis",
    pvRule: null,

    constructor: function(chart, options){

        this.base(chart,options);

    }



});



/**
 * PieChart is the main class for generating... pie charts (surprise!).
 */

pvc.PieChart = pvc.Base.extend({

  pieChartPanel : null,
  legendSource: "categories",
  tipsySettings: {
    gravity: "s",
    fade: true
  },

  constructor: function(o){

    this.base(o);

    var _defaults = {
      showValues: true,
      innerGap: 0.9,
      explodedSliceRadius: 0,
      explodedSliceIndex: null,
      showTooltips: true,
      tooltipFormat: function(s,c,v){
        var val = this.chart.options.valueFormat(v);
        return c+":  " + val + " (" + Math.round(v/this.sum*100,1) + "%)";
      }
    };


    // Apply options
    $.extend(this.options,_defaults, o);


  },

  preRender: function(){

    this.base();

    pvc.log("Prerendering in pieChart");


    this.pieChartPanel = new pvc.PieChartPanel(this, {
      innerGap: this.options.innerGap,
      explodedSliceRadius: this.options.explodedSliceRadius,
      explodedSliceIndex: this.options.explodedSliceIndex,
      showValues: this.options.showValues,
      showTooltips: this.options.showTooltips
    });

    this.pieChartPanel.appendTo(this.basePanel); // Add it

  }

}
);


/*
 * Pie chart panel. Generates a pie chart. Specific options are:
 * <i>showValues</i> - Show or hide slice value. Default: false
 * <i>explodedSliceIndex</i> - Index of the slice to explode. Default: null
 * <i>explodedSliceRadius</i> - If one wants a pie with an exploded effect,
 *  specify a value in pixels here. If above argument is specified, explodes
 *  only one slice. Else explodes all. Default: 0
 * <i>innerGap</i> - The percentage of the inner area used by the pie. Default: 0.9 (90%)
 *
 * Has the following protovis extension points:
 *
 * <i>chart_</i> - for the main chart Panel
 * <i>pie_</i> - for the main pie wedge
 * <i>pieLabel_</i> - for the main pie label
 */


pvc.PieChartPanel = pvc.BasePanel.extend({

  _parent: null,
  pvPie: null,
  pvPieLabel: null,
  data: null,

  innerGap: 0.9,
  explodedSliceRadius: 0,
  explodedSliceIndex: null,
  showTooltips: true,
  showValues: true,

  sum: 0,

  constructor: function(chart, options){

    this.base(chart,options);

  },

  create: function(){

    var myself=this;
    this.width = this._parent.width;
    this.height = this._parent.height;

    this.pvPanel = this._parent.getPvPanel().add(this.type)
    .width(this.width)
    .height(this.height)


    // Add the chart. For a pie chart we have one series only

    var colors = this.chart.colors(pv.range(this.chart.dataEngine.getCategoriesSize()));
    var colorFunc = function(d){
      var cIdx = myself.chart.dataEngine.getVisibleCategoriesIndexes()[this.index];
      return colors(cIdx);
    };
    
    this.data = this.chart.dataEngine.getVisibleValuesForSeriesIndex(0);

    this.sum = pv.sum(this.data);
    var a = pv.Scale.linear(0, this.sum).range(0, 2 * Math.PI);
    var r = pv.min([this.width, this.height])/2 * this.innerGap;

    pvc.log("Radius: "+ r + "; Maximum sum: " + this.sum);


    this.pvPie = this.pvPanel.add(pv.Wedge)
    .data(this.data)
    .bottom(function(d){
      return myself.explodeSlice("cos", a, this.index);
    })
    .left(function(d){
      return myself.explodeSlice("sin", a, this.index);
    })
    .outerRadius(function(d){
      return myself.chart.animate(0 , r)
    })
    .fillStyle(colorFunc)
    .angle(function(d){
      return a(d)
    })
    .text(function(d){
      var s = myself.chart.dataEngine.getVisibleSeries()[this.parent.index]
      var c = myself.chart.dataEngine.getVisibleCategories()[this.index]
      return myself.chart.options.tooltipFormat.call(myself,s,c,d);
    })

    if(this.showTooltips){
      this.extend(this.chart.tipsySettings,"tooltip_");
      this.pvPie
      .event("mouseover", pv.Behavior.tipsy(this.chart.tipsySettings));

    }


    if (this.chart.options.clickable){
      this.pvPie
      .cursor("pointer")
      .event("click",function(d){
        var s = myself.chart.dataEngine.getVisibleSeries()[this.parent.index]
        var c = myself.chart.dataEngine.getVisibleCategories()[this.index]
        return myself.chart.options.clickAction(s,c, d);
      });
    }

    // Extend pie
    this.extend(this.pvPie,"pie_");


    this.pvPieLabel = this.pvPie.anchor("outer").add(pv.Label)
    //.textAngle(0)
    .text(function(d){
      return " "+ d.toFixed(2)
    })
    .textMargin(10)
    .visible(this.showValues);

    // Extend pieLabel
    this.extend(this.pvPieLabel,"pieLabel_");


    // Extend body
    this.extend(this.pvPanel,"chart_");


  },

  accumulateAngle: function(a,idx){

    var arr = this.data.slice(0,idx);
    arr.push(this.data[idx]/2);
    var angle = a(pv.sum(arr));
    return angle;

  },

  explodeSlice: function(fun, a, idx){

    var size = 0;
    if(this.explodedSliceIndex == null){
      size = this.explodedSliceRadius
    }
    else{
      size = this.explodedSliceIndex==idx?this.explodedSliceRadius:0;
    }
    return (fun=="cos"?this.height:this.width)/2 + size*Math[fun](this.accumulateAngle(a,idx));

  }

});
/**
 * BarChart is the main class for generating... bar charts (another surprise!).
 */

pvc.BarChart = pvc.CategoricalAbstract.extend({

    barChartPanel : null,

    constructor: function(o){

        this.base(o);

        var _defaults = {
            showValues: true,
            stacked: false,
            panelSizeRatio: 0.9,
            barSizeRatio: 0.9,
            maxBarSize: 2000,
            valuesAnchor: "center",
            originIsZero: true,
            axisOffset: 0,
            showTooltips: true,
            orientation: "vertical",
            orthoFixedMin: null,
            orthoFixedMax: null
        };


        // Apply options
        $.extend(this.options,_defaults, o);


    },

    preRender: function(){

        this.base();

        pvc.log("Prerendering in barChart");


        this.barChartPanel = new pvc.WaterfallChartPanel(this, {
            stacked: this.options.stacked,
            waterfal: false,
            panelSizeRatio: this.options.panelSizeRatio,
            barSizeRatio: this.options.barSizeRatio,
            maxBarSize: this.options.maxBarSize,
            showValues: this.options.showValues,
            valuesAnchor: this.options.valuesAnchor,
            showTooltips: this.options.showTooltips,
            orientation: this.options.orientation
        });

        this.barChartPanel.appendTo(this.basePanel); // Add it

    }

}
);


/***************
 *  removed BarChartPanel  (CvK)
 *
 * Refactored the CODE:  BarChartPanel is now replaced by the
 *    WaterfallChartPanel as the Waterfallchart code is easier to extend.
 *    (in a next refactoringstep we could take the waterfall specific
 *     code out of the Waterfallchart panel out and make 
 *     restore inherence to waterfall being a special case of barChart.
 *
 ***************/




/**
 * ScatterAbstract is the class that will be extended by dot, line, stackedline and area charts.
 */

pvc.ScatterAbstract = pvc.CategoricalAbstract.extend({

  scatterChartPanel : null,
  tipsySettings: {
    gravity: "s",
    fade: true
  },

  constructor: function(o){

    this.base(o);

    var _defaults = {
      showDots: false,
      showLines: false,
      showAreas: false,
      showValues: false,
      showTooltips: true,
      axisOffset: 0.05,
      valuesAnchor: "right",
      stacked: false,
      originIsZero: true,
      orientation: "vertical",
      timeSeries: false,
      timeSeriesFormat: "%Y-%m-%d",
      panelSizeRatio: 1,
      orthoFixedMin: null,
      orthoFixedMax: null
    };


    // Apply options
    $.extend(this.options,_defaults, o);


  },

  preRender: function(){

    this.base();

    pvc.log("Prerendering in ScatterAbstract");


    this.scatterChartPanel = new pvc.ScatterChartPanel(this, {
      stacked: this.options.stacked,
      showValues: this.options.showValues,
      valuesAnchor: this.options.valuesAnchor,
      showLines: this.options.showLines,
      showDots: this.options.showDots,
      showAreas: this.options.showAreas,
      showTooltips: this.options.showTooltips,
      orientation: this.options.orientation,
      timeSeries: this.options.timeSeries,
      timeSeriesFormat: this.options.timeSeriesFormat
    });

    this.scatterChartPanel.appendTo(this.basePanel); // Add it

  }

}
);

/**
 * Dot Chart
 *
 */

pvc.DotChart = pvc.ScatterAbstract.extend({

  constructor: function(o){

    this.base();

    var _defaults = {
      showDots: true,
      showLines: false,
      showAreas: false,
      showValues: false,
      stacked: false
    };

    // Apply options
    $.extend(this.options,_defaults, o);

  }
});


/**
 * Line Chart
 *
 */

pvc.LineChart = pvc.ScatterAbstract.extend({

  constructor: function(o){

    this.base();

    var _defaults = {
      showDots: false, // ask
      showLines: true,
      showAreas: false,
      showValues: false,
      stacked: false
    };

    // Apply options
    $.extend(this.options,_defaults, o);


  }
});



/**
 * Stacked Line Chart
 *
 */

pvc.StackedLineChart = pvc.ScatterAbstract.extend({

  constructor: function(o){

    this.base();

    var _defaults = {
      showDots: false, // ask
      showLines: true,
      showAreas: false,
      showValues: false,
      stacked: true
    };

    // Apply options
    $.extend(this.options,_defaults, o);


  }
});


/**
 * Stacked Area Chart
 *
 */

pvc.StackedAreaChart = pvc.ScatterAbstract.extend({

  constructor: function(o){

    this.base();

    var _defaults = {
      showDots: false, // ask
      showLines: false,
      showAreas: true,
      showValues: false,
      stacked: true
    };

    // Apply options
    $.extend(this.options,_defaults, o);


  }
});



/*
 * Scatter chart panel. Base class for generating the other xy charts. Specific options are:
 * <i>orientation</i> - horizontal or vertical. Default: vertical
 * <i>showDots</i> - Show or hide dots. Default: true
 * <i>showValues</i> - Show or hide line value. Default: false
 * <i>stacked</i> -  Stacked? Default: false
 * <i>panelSizeRatio</i> - Ratio of the band occupied by the pane;. Default: 0.5 (50%)
 * <i>lineSizeRatio</i> - In multiple series, percentage of inner
 * band occupied by lines. Default: 0.5 (50%)
 * <i>maxLineSize</i> - Maximum size of a line in pixels. Default: 2000
 *
 * Has the following protovis extension points:
 *
 * <i>chart_</i> - for the main chart Panel
 * <i>line_</i> - for the actual line
 * <i>linePanel_</i> - for the panel where the lines sit
 * <i>lineDot_</i> - the dots on the line
 * <i>lineLabel_</i> - for the main line label
 */


pvc.ScatterChartPanel = pvc.BasePanel.extend({

  _parent: null,
  pvLine: null,
  pvArea: null,
  pvDot: null,
  pvLabel: null,
  pvCategoryPanel: null,
  data: null,

  timeSeries: false,
  timeSeriesFormat: "%Y-%m-%d",

  stacked: false,
  showAreas: false,
  showLines: true,
  showDots: true,
  showValues: true,
  showTooltips: true,
  valuesAnchor: "right",
  orientation: "vertical",


  constructor: function(chart, options){

    this.base(chart,options);

  },

  create: function(){

    var myself = this;
    this.width = this._parent.width;
    this.height = this._parent.height;

    this.pvPanel = this._parent.getPvPanel().add(this.type)
    .width(this.width)
    .height(this.height);

    // add clipping for bounds
    if  (   (myself.chart.options.orthoFixedMin != null)
      || (myself.chart.options.orthoFixedMax != null) )
      this.pvPanel["overflow"]("hidden");

    if(this.showTooltips || this.chart.options.clickable ){
      this.pvPanel
      .events("all")
      .event("mousemove", pv.Behavior.point(Infinity));
    }

    var anchor = this.orientation == "vertical"?"bottom":"left";

    // Extend body, resetting axisSizes

    var lScale = this.chart.getLinearScale(true);
    var oScale = this.chart.getOrdinalScale(true);
    var tScale;
    if(this.timeSeries){
      tScale = this.chart.getTimeseriesScale(true,true);
    }
    
    var parser = pv.Format.date(this.timeSeriesFormat);
    
    var colors = this.chart.colors(pv.range(this.chart.dataEngine.getSeriesSize()));
    var colorFunc = function(d){
      // return colors(d.serieIndex)
      return colors(myself.chart.dataEngine.getVisibleSeriesIndexes()[this.parent.index])
    };

    // Stacked?
    if (this.stacked){
      
      this.pvScatterPanel = this.pvPanel.add(pv.Layout.Stack)
      .layers(pvc.padMatrixWithZeros(this.chart.dataEngine.getVisibleTransposedValues()))
      [this.orientation == "vertical"?"x":"y"](function(){
        if(myself.timeSeries){
          return tScale(parser.parse(myself.chart.dataEngine.getCategoryByIndex(this.index)));
        }
        else{
          return oScale(myself.chart.dataEngine.getCategoryByIndex(this.index)) + oScale.range().band/2;
        }
      })
      [anchor](lScale(0))
      [this.orientation == "vertical"?"y":"x"](function(d){
        return myself.chart.animate(0,lScale(d)-lScale(0));
      })

      this.pvArea = this.pvScatterPanel.layer.add(pv.Area)
      .fillStyle(this.showAreas?colorFunc:null);

      this.pvLine = this.pvArea.anchor(pvc.BasePanel.oppositeAnchor[anchor]).add(pv.Line)
      .lineWidth(this.showLines?1.5:0.001);
    //[pvc.BasePanel.paralelLength[anchor]](maxLineSize)
      
    }
    else{

      this.pvScatterPanel = this.pvPanel.add(pv.Panel)
      .data(this.chart.dataEngine.getVisibleSeriesIndexes())

      this.pvArea = this.pvScatterPanel.add(pv.Area)
      .fillStyle(this.showAreas?colorFunc:null);

      this.pvLine = this.pvArea.add(pv.Line)
      .data(function(d){
        return myself.chart.dataEngine.getObjectsForSeriesIndex(d, this.timeSeries?function(a,b){
          return parser.parse(a.category) - parser.parse(b.category);
          }: null)
        })
      .lineWidth(this.showLines?1.5:0.001)
      .segmented(true)
      .visible(function(d) {
        return d.value==null?false:true;
      })
      [pvc.BasePanel.relativeAnchor[anchor]](function(d){

        if(myself.timeSeries){
          return tScale(parser.parse(d.category));
        }
        else{
          return oScale(d.category) + oScale.range().band/2;
        }

      })
      [anchor](function(d){
        return myself.chart.animate(0,lScale(d.value));
      })
   

    }

    
    this.pvLine
    .strokeStyle(colorFunc)
    .text(function(d){
      var v, c;
      var s = myself.chart.dataEngine.getVisibleSeries()[this.parent.index]
      if( d != null && typeof d == "object"){
        v = d.value;
        c = d.category
      }
      else{
        v = d
        c = myself.chart.dataEngine.getVisibleCategories()[this.index]
      };
      return myself.chart.options.tooltipFormat.call(myself,s,c,v);
    })

    if(this.showTooltips){
      this.extend(this.chart.tipsySettings,"tooltip_");
      this.pvLine
      .event("point", pv.Behavior.tipsy(this.chart.tipsySettings));
    }

    this.pvDot = this.pvLine.add(pv.Dot)
    .shapeSize(12)
    .lineWidth(1.5)
    .strokeStyle(this.showDots?colorFunc:null)
    .fillStyle(this.showDots?colorFunc:null)
    

    if (this.chart.options.clickable){
      this.pvDot
      .cursor("pointer")
      .event("click",function(d){
        var v, c;
        var s = myself.chart.dataEngine.getSeries()[this.parent.index]
        if(  d != null && typeof d == "object"){
          v = d.value;
          c = d.category
        }
        else{
          v = d
          c = myself.chart.dataEngine.getCategories()[this.index]
        }
        return myself.chart.options.clickAction(s,c, v);
      });
    }



    if(this.showValues){
      this.pvLabel = this.pvDot
      .anchor(this.valuesAnchor)
      .add(pv.Label)
      .bottom(0)
      .text(function(d){
        return myself.chart.options.valueFormat( (d != null && typeof d == "object")?d.value:d)
      })

      // Extend lineLabel
      this.extend(this.pvLabel,"lineLabel_");
    }


    // Extend line and linePanel
    this.extend(this.pvScatterPanel,"scatterPanel_");
    this.extend(this.pvArea,"area_");
    this.extend(this.pvLine,"line_");
    this.extend(this.pvDot,"dot_");
    this.extend(this.pvLabel,"label_");


    // Extend body
    this.extend(this.pvPanel,"chart_");

  }

});

/**
 *
 * Base panel. A lot of them will exist here, with some common properties.
 * Each class that extends pvc.base will be responsible to know how to use it
 *
 */
pvc.DataEngine = Base.extend({

    chart: null,
    metadata: null,
    resultset: null,
    seriesInRows: false,
    crosstabMode: true,
    translator: null,
    series: null,
    categories: null,
    values: null,
    secondAxisValues: null,
    hiddenData: null,
    secondAxis: false, // Do we have double axis?
    secondAxisIdx: 0,
    
    visibleCategoriesIndexes: undefined,
    visibleCategories: undefined,
    visibleSeriesIndexes: undefined,
    visibleSeries: undefined,

    constructor: function(chart){

        this.chart = chart;
        this.hiddenData = {
            series:{},
            categories:{}
        }
      
    },

    setData: function( metadata, resultset){

        this.metadata = metadata;
        this.resultset = resultset;

    },

    /**
     * Creates the appropriate translator
     */

    createTranslator: function(){

        // Create the appropriate translator
        if(this.crosstabMode){
            pvc.log("Creating CrosstabTranslator");
            this.translator = new pvc.CrosstabTranslator();
        }
        else{
            pvc.log("Creating RelationalTranslator");
            this.translator = new pvc.RelationalTranslator();
        }

        this.translator.setData(this.metadata, this.resultset);
        this.translator.prepare(this);

    },

    /*
     * Returns some information on the data points
     */

    getInfo: function(){

        var out = "------------------------------------------\n";
        out+= "Dataset Information\n";

        out+= "  Series ( "+ this.getSeriesSize() +" ): " + this.getSeries().slice(0,10) +"\n";
        out+= "  Categories ( "+ this.getCategoriesSize() +" ): " + this.getCategories().slice(0,10) +"\n";
        out+= "  `- secondAxis: " + this.chart.options.secondAxis + "; secondAxisIndex: " + this.chart.options.secondAxisIdx + "\n";
        out+= "------------------------------------------\n";

        return out;

    },

    /*
     * Returns the series on the underlying data
     *
     */

    getSeries: function(){
        var res = this.series || this.translator.getColumns();
        return res;
    },

    /*
     * Returns a serie on the underlying data by an index
     *
     */

    getSerieByIndex: function(idx){
        return this.getSeries()[idx];
    },


    /*
     * Returns an array with the indexes for the series
     *
     */
    getSeriesIndexes: function(){
        // we'll just return everything
        return pv.range(this.getSeries().length)
    },

    /*
     * Returns an array with the indexes for the visible series
     *
     */
    getVisibleSeriesIndexes: function(){

        if (typeof this.visibleSeriesIndexes === "undefined"){
            
            var myself=this;
            var res =  pv.range(this.getSeries().length).filter(function(v){
                return !myself.hiddenData.series[v];
            });
            this.visibleSeriesIndexes = res;
        }
        
        return this.visibleSeriesIndexes;

    },

    /*
     * Returns an array with the visible categories. Use only when index information
     * is not required
     *
     */
    getVisibleSeries: function(){



        if (typeof this.visibleSeries === "undefined"){
            var myself = this;
            var res = this.getVisibleSeriesIndexes().map(function(idx){
                return myself.getSerieByIndex(idx);
            });
            this.visibleSeries = res;
        }
        
        return this.visibleSeries;
    },


    /*
     * Togles the serie visibility based on an index. Returns true if serie is now
     * visible, false otherwise.
     *
     */

    toggleSerieVisibility: function(idx){

        return this.toggleVisibility("series",idx);

    },


    /*
     * Returns the categories on the underlying data
     *
     */

    getCategories: function(){

        if( this.categories == null ){

            if(this.chart.options.timeSeries){
                var parser = pv.Format.date(this.chart.options.timeSeriesFormat);
                this.categories = this.translator.getRows().sort(function(a,b){
                    return parser.parse(a) - parser.parse(b)
                });

            }
            else{
                this.categories = this.translator.getRows()
            }

        }

        return this.categories;
    },

    getCategoryMin: function() {
        var cat = this.getCategories();
        var min = cat[0];
        for(var i in cat)
            if (cat[i] < min)
                min = cat[i];
        return min;
    },

    getCategoryMax: function() {
        var cat = this.getCategories();
        var max = cat[0];
        for(var i in cat)
            if (cat[i] > max)
                max = cat[i];
        return max;
    },

    /*
     * Returns the categories on the underlying data
     *
     */

    getCategoryByIndex: function(idx){
        return this.getCategories()[idx];
    },

    /*
     * Returns an array with the indexes for the categories
     *
     */
    getCategoriesIndexes: function(){
        // we'll just return everything
        return pv.range(this.getCategories().length)
    },

    /*
     * Returns an array with the indexes for the visible categories
     *
     */
    getVisibleCategoriesIndexes: function(){
        
        if (typeof this.visibleCategoriesIndexes === "undefined"){
            var myself=this;
            var res = pv.range(this.getCategories().length).filter(function(v){
                return !myself.hiddenData.categories[v];
            });
            this.visibleCategoriesIndexes = res;
        }
        
        return this.visibleCategoriesIndexes;
    },

    /*
     * Returns an array with the visible categories. Use only when index information
     * is not required
     *
     */
    getVisibleCategories: function(){
  
        if (typeof this.visibleCategories === "undefined"){
            var myself = this;
            var res = this.getVisibleCategoriesIndexes().map(function(idx){
                return myself.getCategoryByIndex(idx);
            });
        
            this.visibleCategories = res;
        }
        
        return this.visibleCategories;
    },

    /*
     * Togles the category visibility based on an index. Returns true if category is now
     * visible, false otherwise.
     *
     */

    toggleCategoryVisibility: function(idx){

        return this.toggleVisibility("categories",idx);

    },

    /*
     * Togles the visibility of category or series based on an index.
     * Returns true if is now visible, false otherwise.
     *
     */

    toggleVisibility: function(axis,idx){

        // Accepted values for axis: series|categories
        pvc.log("Toggling visibility of " + axis + "["+idx+"]");

        if (typeof this.hiddenData[axis][idx] == "undefined"){
            this.hiddenData[axis][idx] = true;
        }
        else{
            delete this.hiddenData[axis][idx];
        }

    },

    
    /*
     * Clears the cache that's used for optimization
     *
     */

    clearDataCache: function(){
        
        this.visibleCategoriesIndexes = undefined;
        this.visibleCategories = undefined;
        this.visibleSeriesIndexes = undefined;
        this.visibleSeries = undefined;
    
    },

    /*
     * Returns the visibility status of a category or series based on an index.
     * Returns true if is visible, false otherwise.
     *
     */
    isVisible: function(axis,idx){

        // Accepted values for axis: series|categories

        if (typeof this.hiddenData[axis][idx] != "undefined"){
            return !this.hiddenData[axis][idx];
        }
        else{
            return true;
        }

    },


    /*
     * Returns the values for the dataset
     */

    getValues: function(){


        if (this.values == null){
            this.values = this.translator.getValues();
        }
        return this.values;

    },

    /*
     * Returns the values for the second axis of the dataset
     */

    getSecondAxisValues: function(){


        if (this.secondAxisValues == null){
            this.secondAxisValues = this.translator.getSecondAxisValues();
        }
        return this.secondAxisValues;

    },


    /*
     * Returns the object for the second axis in the form {category: catName, value: val}
     *
     */

    getObjectsForSecondAxis: function(sortF){

        var myself = this;
        var ar = [];
        this.getSecondAxisValues().map(function(v,i){
            if(typeof v != "undefined" /* && v != null */ ){
                ar.push({
                    category: myself.getCategories()[i],
                    value: v
                }) ;
            }
        })

        if (typeof sortF == "function"){
            return ar.sort(sortF)
        }
        else
            return ar;
    },
    /*
     * Returns the maximum value for the second axis of the dataset
     */
    getSecondAxisMax:function(){

        return pv.max(this.getSecondAxisValues().filter(pvc.nonEmpty))
    },
    
    /*
     * Returns the minimum value for the second axis of the dataset
     */
    getSecondAxisMin:function(){

        return pv.min(this.getSecondAxisValues().filter(pvc.nonEmpty))
    },



    /*
     * Returns the transposed values for the dataset
     */

    getTransposedValues: function(){


        return pv.transpose(pvc.cloneMatrix(this.getValues()));

    },


    /*
     * Returns the transposed values for the visible dataset
     */

    getVisibleTransposedValues: function(){
        var myself = this;
        var res = this.getVisibleSeriesIndexes().map(function(sIdx){
            return myself.getVisibleValuesForSeriesIndex(sIdx)
        })
        return res;
    },

    /*
     * Returns the values for a given series idx
     *
     */

    getValuesForSeriesIndex: function(idx){
        return this.getValues().map(function(a){
            return a[idx];
        })
    },

    /*
     * Returns the visible values for a given category idx
     *
     */

    getVisibleValuesForSeriesIndex: function(idx){

        var series = this.getValuesForSeriesIndex(idx)
        return this.getVisibleCategoriesIndexes().map(function(idx){
            return series[idx]
        })
    },

    /*
     * Returns the object for a given series idx in the form {category: catName, value: val}
     *
     */

    getObjectsForSeriesIndex: function(idx, sortF){

        var myself = this;
        var ar = [];
        this.getValues().map(function(a,i){
            if(typeof a[idx] != "undefined" /* && a[idx] != null */){
                ar.push({
                    serieIndex: idx,
                    category: myself.getCategories()[i],
                    value: a[idx]
                }) ;
            }
        })

        if (typeof sortF == "function"){
            return ar.sort(sortF)
        }
        else
            return ar;
    },

    /*
     * Returns the values for a given category idx
     *
     */

    getValuesForCategoryIndex: function(idx){
        return this.getValues()[idx];
    },

    /*
     * Returns the visible values for a given category idx
     *
     */

    getVisibleValuesForCategoryIndex: function(idx){

        var cats = this.getValuesForCategoryIndex(idx);
        var res = this.getVisibleSeriesIndexes().map(function(idx){
            return cats[idx]
        });
        return res;
    },


    /*
     * Returns the object for a given category idx in the form {serie: value}
     *
     */

    getObjectsForCategoryIndex: function(idx){

        var myself = this;
        var ar=[];
        this.getValues()[idx].map(function(a,i){
            if(typeof a != "undefined" /* && a!= null */){
                ar.push({
                    categoryIndex: idx,
                    serie: myself.getSeries()[i],
                    value: a
                }) ;
            }
        })
        return ar;
    },

    /*
     * Returns how many series we have
     */

    getSeriesSize: function(){
        return this.getSeries().length;
    },

    /*
     * Returns how many categories, or data points, we have
     */
    getCategoriesSize: function(){
        return this.getCategories().length;
    },

    /**
     * For every category in the data, get the maximum of the sum of the series
     * values.
     *
     */

    getCategoriesMaxSumOfVisibleSeries: function(){

        var myself=this;
        var max = pv.max(pv.range(0,this.getCategoriesSize()).map(function(idx){
            return pv.sum(myself.getVisibleValuesForCategoryIndex(idx).filter(pvc.nonEmpty))
        }));
        pvc.log("getCategoriesMaxSumOfVisibleSeries: " + max);
        return max;
    },

    /**
     * For every serie in the data, get the maximum of the sum of the category
     * values. If only one serie, gets the sum of the value. Useful to build
     * pieCharts
     *
     */

    getVisibleSeriesMaxSum: function(){

        var myself=this;
        var max = pv.max(this.getVisibleSeriesIndexes().map(function(idx){
            return pv.sum(myself.getValuesForSeriesIndex(idx).filter(pvc.nonEmpty))
        }));
        pvc.log("getVisibleSeriesMaxSum: " + max);
        return max;
    },

    /*
     * Get the maximum value in all series
     */
    getVisibleSeriesAbsoluteMax: function(){

        var myself=this;
        var max = pv.max(this.getVisibleSeriesIndexes().map(function(idx){
            return pv.max(myself.getValuesForSeriesIndex(idx).filter(pvc.nonEmpty))
        }));
        pvc.log("getVisibleSeriesAbsoluteMax: " + max);
        return max;
    },

    /*
     * Get the minimum value in all series
     */
    getVisibleSeriesAbsoluteMin: function(){

        var myself=this;
        var min = pv.min(this.getVisibleSeriesIndexes().map(function(idx){
            return pv.min(myself.getValuesForSeriesIndex(idx).filter(pvc.nonEmpty))
        }));
        pvc.log("getVisibleSeriesAbsoluteMin: " + min);
        return min;
    },


    setCrosstabMode: function(crosstabMode){
        this.crosstabMode = crosstabMode;
    },

    isCrosstabMode: function(){
        return this.crosstabMode;
        pv.range(0,this.getSeriesSize())
    },

    setSeriesInRows: function(seriesInRows){
        this.seriesInRows = seriesInRows;
    },

    isSeriesInRows: function(){
        return this.seriesInRows;
    }

});



pvc.DataTranslator = Base.extend({

    dataEngine: null,
    metadata: null,
    resultset: null,
    values: null,
    secondAxisValues: null,

    constructor: function(){
    },


    setData: function(metadata, resultset){
        this.metadata = metadata;
        this.resultset = resultset;
    },


    getValues: function(){


        // Skips first row, skips first col.
        return this.values.slice(1).map(function(a){
            return a.slice(1);
        });
      
    },

    getSecondAxisValues: function(){


        // Skips first row
        return this.secondAxisValues.slice(1);

    },

    getColumns: function(){

        // First column of every row, skipping 1st entry
        return this.values[0].slice(1);
    },

    getRows: function(){


        // first element of every row, skipping 1st one
        return this.values.slice(1).map(function(d){
            return d[0];
        })


    },

    transpose: function(){

        pv.transpose(this.values);
    },


    prepare: function(dataEngine){
        this.dataEngine = dataEngine;
        this.prepareImpl();
        this.postPrepare();
    },

    postPrepare: function(){

        if( this.dataEngine.seriesInRows ){
            this.transpose()
        }
        if(this.dataEngine.chart.options.secondAxis){
            var idx = this.dataEngine.chart.options.secondAxisIdx;
            if (idx>=0){
                idx++; // first row is cat name
            }

            // Transpose, splice, transpose back
            pv.transpose(this.values);
            this.secondAxisValues = this.values.splice(idx , 1)[0];
            pv.transpose(this.values);
        }

    },

    prepareImpl: function(){
    // Specific code goes here - override me
    },

    sort: function(sortFunc){
    // Specify the sorting data - override me
    }


})


pvc.CrosstabTranslator = pvc.DataTranslator.extend({


    prepareImpl: function(){
    
        // All we need to do is to prepend to the result's matrix the series
        // line

        var a1 = this.metadata.slice(1).map(function(d){
            return d.colName;
        });
        a1.splice(0,0,"x");

        this.values = pvc.cloneMatrix(this.resultset);
        this.values.splice(0,0,a1);

    }
  
});


pvc.RelationalTranslator = pvc.DataTranslator.extend({



    prepareImpl: function(){

        var myself = this;

        if(this.metadata.length == 2){
            // Adding a static serie
            this.resultset.map(function(d){
                d.splice(0,0,"Serie");
            })
            this.metadata.splice(0,0,{
                "colIndex":2,
                "colType":"String",
                "colName":"Series"
            })
        }

        /*
        var seenSeries = [],
        seenCategories = [],
        crossTab = [];

        for (r = 0; r < this.resultset.length;r ++) {
            var row = this.resultset[r],
            sIdx = ( idx = seenSeries.indexOf(row[0])) > -1 ? idx + 1: seenSeries.push(row[0]),
            cIdx = ( idx = seenCategories.indexOf(row[1])) > -1 ? idx : seenCategories.push(row[1]) - 1;
            //console.log(row);
            if(!crossTab[cIdx]) crossTab[cIdx] = [];
            crossTab[cIdx][sIdx] = (crossTab[cIdx][sIdx] || 0 ) + row[2];
            crossTab[cIdx][0] = row[1];
        }

        this.values = crossTab;

         */

        var tree = pv.tree(this.resultset).keys(function(d){
            return [d[0],d[1]]
        }).map();
        
        // Now, get series and categories:

        var series = pv.uniq(this.resultset.map(function(d){
            return d[0];
        }));
        var numeratedSeries = pv.numerate(series);

        var categories = pv.uniq(this.resultset.map(function(d){
            return d[1];
        }))
        var numeratedCategories = pv.numerate(categories);


        // Finally, itetate through the resultset and build the new values

        this.values = [];
        var categoriesLength = categories.length;
        var seriesLength = series.length;

        // Initialize array
        pv.range(0,categoriesLength).map(function(d){
            myself.values[d] = new Array(seriesLength + 1);
            myself.values[d][0] = categories[d]
        })

        this.resultset.map(function(l){

            myself.values[numeratedCategories[l[1]]][numeratedSeries[l[0]] + 1] =
            pvc.sumOrSet(myself.values[numeratedCategories[l[1]]][numeratedSeries[l[0]]+1], l[2]);
        })

        // Create an inicial line with the categories
        var l1 = series;
        l1.splice(0,0,"x");
        this.values.splice(0,0, l1)
 
    }


});

NoDataException = function() {};
/**
 * HeatGridChart is the main class for generating... heatGrid charts.
 *  A heatGrid visualizes a matrix of values by a grid (matrix) of *
 *  bars, where the color of the bar represents the actual value.
 *  By default the colors are a range of green values, where
 *  light green represents low values and dark green high values.
 *  A heatGrid contains:
 *     - two categorical axis (both on x and y-axis)
 *     - no legend as series become rows on the perpendicular axis 
 *  Please contact CvK if there are issues with HeatGrid at cde@vinzi.nl.
 */

pvc.HeatGridChart = pvc.CategoricalAbstract.extend({

    heatGridChartPanel : null,

    constructor: function(o){

        this.base(o);

	// enforce some defaults for the HeatGridChart
        this.options.legend = false;
        this.options.orthoAxisOrdinal = true;
        this.options.orginIsZero = true;

        var _defaults = {
            showValues: true,
            //originIsZero: true,
            axisOffset: 0,
            showTooltips: true,
            orientation: "vertical",
            // use a categorical here based on series labels
            scalingType: "linear",    // "normal" (distribution) or "linear"
            normPerBaseCategory: true,
            orthoAxisOrdinal: true,
            numSD: 2,                 // width (only for normal distribution)
            minColor: "white",
            maxColor: "darkgreen",
            nullColor:  "#efc5ad"  // white with a shade of orange
        };

        // Apply options
        $.extend(this.options,_defaults, o);

	// enforce some defaults for the HeatGridChart
        this.options.orthoAxisOrdinal = true;
        this.options.legend = false;
        this.options.orginIsZero = true;

    },

    preRender: function(){

        this.base();

        pvc.log("Prerendering in heatGridChart");


        this.heatGridChartPanel = new pvc.HeatGridChartPanel(this, {
            stacked: this.options.stacked,
            panelSizeRatio: this.options.panelSizeRatio,
            heatGridSizeRatio: this.options.heatGridSizeRatio,
            maxHeatGridSize: this.options.maxHeatGridSize,
            showValues: this.options.showValues,
            showTooltips: this.options.showTooltips,
            orientation: this.options.orientation
        });

        this.heatGridChartPanel.appendTo(this.basePanel); // Add it

    }

}
);


/*
 * HeatGrid chart panel. Generates a heatGrid chart. Specific options are:
 * <i>orientation</i> - horizontal or vertical. Default: vertical
 * <i>showValues</i> - Show or hide heatGrid value. Default: false
 * <i>stacked</i> -  Stacked? Default: false
 * <i>panelSizeRatio</i> - Ratio of the band occupied by the pane;. Default: 0.5 (50%)
 * <i>heatGridSizeRatio</i> - In multiple series, percentage of inner
 * band occupied by heatGrids. Default: 0.5 (50%)
 * <i>maxHeatGridSize</i> - Maximum size of a heatGrid in pixels. Default: 2000
 *
 * Has the following protovis extension points:
 *
 * <i>chart_</i> - for the main chart Panel
 * <i>heatGrid_</i> - for the actual heatGrid
 * <i>heatGridPanel_</i> - for the panel where the heatGrids sit
 * <i>heatGridLabel_</i> - for the main heatGrid label
 */


pvc.HeatGridChartPanel = pvc.BasePanel.extend({

    _parent: null,
    pvHeatGrid: null,
    pvHeatGridLabel: null,
    data: null,

    stacked: false,
    panelSizeRatio: 1,
    heatGridSizeRatio: 0.5,
    showTooltips: true,
    maxHeatGridSize: 200,
    showValues: true,
    orientation: "vertical",


    constructor: function(chart, options){

        this.base(chart,options);

    },

    create: function(){

        var myself = this;
        var opts = this.chart.options;
        this.width = this._parent.width;
        this.height = this._parent.height;

        this.pvPanel = this._parent.getPvPanel().add(this.type)
        .width(this.width)
        .height(this.height)

        var anchor = this.orientation == "vertical"?"bottom":"left";

        // reuse the existings scales
        var xScale = this.chart.xAxisPanel.scale;
        var yScale = this.chart.yAxisPanel.scale;
        
        var cols =  (anchor == "bottom") ? xScale.domain() : yScale.domain();

        var origData = this.chart.dataEngine.getVisibleTransposedValues();
        // create a mapping of the data that shows the columns (rows)
        data = origData.map(function(d){
            return pv.dict(cols, function(){
                return  d[this.index]
            })
        });
        data.reverse();  // the colums are build from top to bottom

        // get an array of scaling functions (one per column)
        var fill = this.getColorScale(data, cols);

        /* The cell dimensions. */
        var w = (xScale.max - xScale.min)/xScale.domain().length;
        var h = (yScale.max - yScale.min)/yScale.domain().length;

        if (anchor != "bottom") {
            var tmp = w;
            w = h;
            h = tmp;
        }

        this.pvHeatGrid = this.pvPanel.add(pv.Panel)
        .data(cols)
        [pvc.BasePanel.relativeAnchor[anchor]](function(){
            return this.index * w
            })
        [pvc.BasePanel.paralelLength[anchor]](w)
        .add(pv.Panel)
        .data(data)
        [pvc.BasePanel.oppositeAnchor[anchor]](function(){
            return this.index * h
        })
        [pvc.BasePanel.orthogonalLength[anchor]](h)
        .fillStyle(function(dat, col){
            return  (dat[col] != null) ? fill[col](dat[col]):opts.nullColor
        })
        .strokeStyle("white")
        .lineWidth(1)
        .antialias(false)
        .text(function(d,f){
          return d[f]});


        // NO SUPPORT for overflow and underflow on HeatGrids

        // NO SUPPORT for SecondAxis on HeatGrids (does not make sense)

        // Labels:

        if(this.showTooltips){
            this.pvHeatGrid
            .event("mouseover", pv.Behavior.tipsy({
                gravity: "s",
                fade: true
            }));
        }

        if (opts.clickable){
            this.pvHeatGrid
            .cursor("pointer")
            .event("click",function(row, rowCol){
                var s = myself.chart.dataEngine.getSeries()[myself.stacked?this.parent.index:this.index]
                var c = myself.chart.dataEngine.getCategories()[myself.stacked?this.index:this.parent.index]
                var d = row[rowCol];
                return myself.chart.options.clickAction(s,c,d);
            });
        }

        if(this.showValues){
            
            var getValue = function(row, rowAgain, rowCol){
                return row[rowCol];
            };
            
            this.pvHeatGridLabel = this.pvHeatGrid
            .anchor("center")
            .add(pv.Label)
            .bottom(0)
            .text(getValue);

            // Extend heatGridLabel
            this.extend(this.pvHeatGridLabel,"heatGridLabel_");
        }


        // Extend heatGrid and heatGridPanel
        this.extend(this.pvHeatGrid,"heatGridPanel_");
        this.extend(this.pvHeatGrid,"heatGrid_");

        // Extend body
        this.extend(this.pvPanel,"chart_");

    },
  
  /***********
   * compute an array of fill-functions. Each column out of "cols" 
   * gets it's own scale function assigned to compute the color
   * for a value. Currently supported scales are:
   *    -  linear (from min to max
   *    -  normal distributed from   -numSD*sd to  numSD*sd 
   *         (where sd is the standards deviation)
   ********/
  getColorScale: function(data, cols) {
    switch (this.chart.options.scalingType) {
    case "normal": return this.getNormalColorScale(data, cols);
    case "linear": return this.getLinearColorScale(data, cols);
    default:
      throw "Invalid option " + this.scaleType + " in HeatGrid"
    }
  },

  getLinearColorScale: function (data, cols){
    var fill;
    var opts = this.chart.options;
    // compute the mean and standard-deviation for each column
    var min = pv.dict(cols, function(f){
      return pv.min(data, function(d){
        return d[f]
      })
    });
    var max = pv.dict(cols, function(f){
      return pv.max(data, function(d){
        return d[f]
      })
    });

    if (opts.normPerBaseCategory)  //  compute a scale-function for each column (each key
      fill = pv.dict(cols, function(f){
        return pv.Scale.linear()
          .domain(min[f], max[f])
          .range(opts.minColor, opts.maxColor)
      });
     else {   // normalize over the whole array
      var theMin = min[cols[0]];
      for (var i=1; i<cols.length; i++)
        if (min[cols[i]] < theMin) theMin = min[cols[i]];

      var theMax = max[cols[0]];
      for (var i=1; i<cols.length; i++)
        if (max[cols[i]] < theMax) theMax = max[cols[i]];

      var scale = pv.Scale.linear()
        .domain(theMin, theMax)
        .range(opts.minColor, opts.maxColor);
      fill = pv.dict(cols, function(f){
        return scale
      })
    }

    return fill;  // run an array of values to compute the colors per column
  },

  getNormalColorScale: function (data, cols){
    var fill;
    var opts = this.chart.options;
    if (opts.normPerBaseCategory) {
      // compute the mean and standard-deviation for each column
      var mean = pv.dict(cols, function(f){
        return pv.mean(data, function(d){
          return d[f]
        })
      });
      var sd = pv.dict(cols, function(f){
        return pv.deviation(data, function(d){
          return d[f]
        })
      });
      //  compute a scale-function for each column (each key)
      fill = pv.dict(cols, function(f){
        return pv.Scale.linear()
          .domain(-opts.numSD * sd[f] + mean[f],
                  opts.numSD * sd[f] + mean[f])
          .range(opts.minColor, opts.maxColor)
      });
    } else {   // normalize over the whole array
      var mean = 0.0, sd = 0.0, count = 0;
      for (var i=0; i<origData.length; i++)
        for(var j=0; j<origData[i].length; j++)
          if (origData[i][j] != null){
            mean += origData[i][j];
            count++;
          }
      mean /= count;
      for (var i=0; i<origData.length; i++)
        for(var j=0; j<origData[i].length; j++)
          if (origData[i][j] != null){
            var variance = origData[i][j] - mean;
            sd += variance*variance;
          }
      sd /= count;
      sd = Math.sqrt(sd);
      
      var scale = pv.Scale.linear()
        .domain(-opts.numSD * sd + mean,
                opts.numSD * sd + mean)
        .range(opts.minColor, opts.maxColor);
      fill = pv.dict(cols, function(f){
        return scale
      })
    }

    return fill;  // run an array of values to compute the colors per column
}


});
/**
 * MetricAbstract is the base class for all chart types that have
 * a two linear axis.
 * If the base-axis is a categorical axis you should use categoricalAbstract.
 * 
 * If you have issues with this class please contact CvK at cde@vinzi.nl 
 */

pvc.MetricAbstract = pvc.CategoricalAbstract.extend({

  constructor: function(o){
    
    this.base(o);
    var _defaults = {
    };

    // Apply options
    $.extend(this.options,_defaults, o);

    return;
  },

  preRender: function(){
    this.base();
    pvc.log("Prerendering in MetricAbstract");
    return;
  },


  /*
   * Indicates if x-axis (horizontal axis) is an ordinal scale
   */
     // CvK: if we move ordinal-ordinal to a separate class this functions
  // can be probably be thrown out as it becomes identical to the
  // parent fucntion.
  isXAxisOrdinal: function(){
    var isOrdinal = false;
    if (this.options.orientation == "vertical") 
      isOrdinal = false;
    else 
      isOrdinal =  this.options.orthoAxisOrdinal;
    return isOrdinal;
  },


  /*
   * Indicates if y-axis (vertical axis) is an ordinal scale
   */
     // CvK: if we move ordinal-ordinal to a separate class this functions
  // can be probably be thrown out as it becomes identical to the
  // parent fucntion.
  
  isYAxisOrdinal: function(){
    var isOrdinal = false;
    if (this.options.orientation == "vertical")
      isOrdinal =  this.options.orthoAxisOrdinal;
    else
      isOrdinal = false;
    return isOrdinal;
  },


  getLinearBaseScale: function(bypassAxis){
    
    var yAxisSize = bypassAxis?0:this.options.yAxisSize;
    var xAxisSize = bypassAxis?0:this.options.xAxisSize;
    
    var isVertical = this.options.orientation=="vertical"
    
    // compute the input-domain of the scale
    var domainMin = this.dataEngine.getCategoryMin();
    var domainMax = this.dataEngine.getCategoryMax();
    // Adding a small relative offset to the scale to prevent that
    // points are located on top of the axis:
    var offset = (domainMax - domainMin) * this.options.axisOffset;
    domainMin -= offset;
    domainMax += offset;
    
    // compute the output-range
    var rangeMin, rangeMax;
    if (isVertical) {
      rangeMin = yAxisSize;
      rangeMax = this.basePanel.width;
    } else {
      rangeMin = 0;
      rangeMax = this.basePanel.height - xAxisSize;
    }
    
    // create the (linear) Scale
    var scale = new pv.Scale.linear()
      .domain(domainMin, domainMax)
      .range(rangeMin, rangeMax);
    
    return scale;
  },

  /*
   * get the scale for the axis with horizontal orientation
   */
  
  getXScale: function(){
    var scale = null;
    
    if (this.options.orientation == "vertical") {
      scale = this.options.timeSeries  ?
        this.getTimeseriesScale()     :
        this.getLinearBaseScale();   // linear is the default
    } else {
      scale = this.getLinearScale();
    } 
    
    return scale;
  },

  /*
   * get the scale for the axis with the vertical orientation.
   */
  
  getYScale: function(){
    var scale = null;
    if (this.options.orientation == "vertical") {
      scale = this.getLinearScale();
    } else { 
      scale = this.options.timeSeries  ?
        this.getTimeseriesScale()     :
        this.getLinearBaseScale();
    }
    return scale;
  }

}  )


/*********
 *  Panel use to draw line and dotCharts
 *     LScatter is for graphs with a linear base-axis
 *
 *  The original ScatterChartPanel was difficult to generalize as
 *  many (scattered) changes were needed in the long create function.
 *     OScatter could be develofor graphs with a ordinal base-axis
 *
 *  Later we might consider to merge LScatter and OScatter again, and 
 *  refactor the general stuff to an abstract base class.
 *********/


/*
 * Scatter chart panel. Base class for generating the other xy charts. Specific options are:
 * <i>orientation</i> - horizontal or vertical. Default: vertical
 * <i>showDots</i> - Show or hide dots. Default: true
 * <i>showValues</i> - Show or hide line value. Default: false
 * <i>stacked</i> -  Stacked? Default: false
 * <i>panelSizeRatio</i> - Ratio of the band occupied by the pane;. Default: 0.5 (50%)
 * <i>lineSizeRatio</i> - In multiple series, percentage of inner
 * band occupied by lines. Default: 0.5 (50%)
 * <i>maxLineSize</i> - Maximum size of a line in pixels. Default: 2000
 *
 * Has the following protovis extension points:
 *
 * <i>chart_</i> - for the main chart Panel
 * <i>line_</i> - for the actual line
 * <i>linePanel_</i> - for the panel where the lines sit
 * <i>lineDot_</i> - the dots on the line
 * <i>lineLabel_</i> - for the main line label
 */


pvc.MetricScatterChartPanel = pvc.BasePanel.extend({

  _parent: null,
  pvLine: null,
  pvArea: null,
  pvDot: null,
  pvLabel: null,
  pvCategoryPanel: null,
  data: null,

  timeSeries: false,
  timeSeriesFormat: "%Y-%m-%d",

  stacked: false,
  showAreas: false,
  showLines: true,
  showDots: true,
  showValues: true,
  showTooltips: true,
  valuesAnchor: "right",
  orientation: "vertical",


  constructor: function(chart, options){

    this.base(chart,options);

  },


  prepareDataFunctions:  function() {
    /*
        This function implements a number of helper functions via
        closures. The helper functions are all stored in this.DF
        Overriding this function allows you to implement
        a different ScatterScart.
     */
    var myself = this;

    var baseScale = this.chart.getLinearBaseScale(true);
    var orthoScale = this.chart.getLinearScale(true); 

    if(this.timeSeries){
      tScale = this.chart.getTimeseriesScale(true);
    }
    
    // create empty container for the functions and data
    myself.DF = {}

    myself.DF.baseValues = this.chart.dataEngine.getVisibleCategories();
    myself.DF.visibleSerieIds = this.chart.dataEngine.getVisibleSeriesIndexes()
//    myself.DF.data = this.chart.dataEngine.getVisibleTransposedValues();

    // calculate a position along the base-axis
    myself.DF.baseCalculation = (myself.timeSeries) ?
      function(d){ return tScale(parser.parse(d.category)); }   :
      function(d) { return baseScale(d.category);  };
      

    // calculate a position along the orthogonal axis
    myself.DF.orthoCalculation = function(d){
      return myself.chart.animate(0, orthoScale(d.value));
    };

    // get a data-series for the ID
    var pFunc = null;
    if (this.timeSeries) {
      var parser = pv.Format.date(this.timeSeriesFormat);
      pFunc = function(a,b){ 
        return parser.parse(a.category)-parser.parse(b.category);};
    }  
    myself.DF.getDataForSerieId = 
      function(d){ var res = myself.chart.dataEngine
            .getObjectsForSeriesIndex(d, pFunc);
            res.sort(function(a, b) {return a.category - b.category; })
            return res;
          };


    var colors = this.chart.colors(
         pv.range(this.chart.dataEngine.getSeriesSize()));
    myself.DF.colorFunc = function(d){
      // return colors(d.serieIndex)
      return colors(myself.chart.dataEngine.getVisibleSeriesIndexes()
              [this.parent.index])
    };

  },

  create: function(){

    var myself = this;
    this.width = this._parent.width;
    this.height = this._parent.height;

    this.pvPanel = this._parent.getPvPanel().add(this.type)
    .width(this.width)
    .height(this.height);

    // add clipping for bounds
    if  (   (myself.chart.options.orthoFixedMin != null)
         || (myself.chart.options.orthoFixedMax != null) )
      this.pvPanel["overflow"]("hidden");

    if(this.showTooltips || this.chart.options.clickable ){
      this.pvPanel
      .events("all")
      .event("mousemove", pv.Behavior.point(Infinity));
    }

    var anchor = this.orientation == "vertical"?"bottom":"left";

    // prepare data and functions when creating (rendering) the chart.
    this.prepareDataFunctions();

    var maxLineSize;

    // Stacked?
    if (this.stacked){

      pvc.log("WARNING: the stacked option of metric charts still needs to be implemented.");

/*    CvK:  have to rewrite this code  
      this.pvScatterPanel = this.pvPanel.add(pv.Layout.Stack)
      .layers(pvc.padMatrixWithZeros(this.chart.dataEngine.getVisibleTransposedValues()))
      [this.orientation == "vertical"?"x":"y"](function(){
        if(myself.timeSeries){
          return tScale(parser.parse(myself.chart.dataEngine.getCategoryByIndex(this.index)));
        }
        else{
          return oScale(myself.chart.dataEngine.getCategoryByIndex(this.index)) + oScale.range().band/2;
        }
      })
      [anchor](lScale(0))
      [this.orientation == "vertical"?"y":"x"](function(d){
        return myself.chart.animate(0,lScale(d)-lScale(0));
      })

      this.pvArea = this.pvScatterPanel.layer.add(pv.Area)
      .fillStyle(this.showAreas?colorFunc:null);

      this.pvLine = this.pvArea.anchor(pvc.BasePanel.oppositeAnchor[anchor]).add(pv.Line)
      .lineWidth(this.showLines?1.5:0.001);
    //[pvc.BasePanel.paralelLength[anchor]](maxLineSize)
    */    
    }
    else {

      // Add the serie identifiers to the scatterPanel
      // CvK: Why do we need a new pvPanel and can't we use existing pvPanel?
      this.pvScatterPanel = this.pvPanel.add(pv.Panel)
           .data(myself.DF.visibleSerieIds);

      // add the area's
      // CvK: why adding area's if showArea
      this.pvArea = this.pvScatterPanel.add(pv.Area)
        .fillStyle(this.showAreas?myself.DF.colorFunc:null);

      var lineWidth = this.showLines ? 1.5 : 0.001;
      // add line and make lines invisible if not needed.
      this.pvLine = this.pvArea.add(pv.Line)
      .data(myself.DF.getDataForSerieId)
      .lineWidth(lineWidth)
      [pvc.BasePanel.relativeAnchor[anchor]](myself.DF.baseCalculation)
      [anchor](myself.DF.orthoCalculation)
    }

    
    this.pvLine
      .strokeStyle(myself.DF.colorFunc)
      .text(function(d){
        var v, c;
        var s = myself.chart.dataEngine.getVisibleSeries()[this.parent.index]
        if( typeof d == "object"){
          v = d.value;
          c = d.category
        }
        else{
          v = d
          c = myself.chart.dataEngine.getVisibleCategories()[this.index]
        };
        return myself.chart.options.tooltipFormat.call(myself,s,c,v);
      })

    if(this.showTooltips){
      this.extend(this.chart.tipsySettings,"tooltip_");
      this.pvLine
        .event("point", pv.Behavior.tipsy(this.chart.tipsySettings));
    }

    this.pvDot = this.pvLine.add(pv.Dot)
    .shapeSize(12)
    .lineWidth(1.5)
    .strokeStyle(this.showDots?myself.DF.colorFunc:null)
    .fillStyle(this.showDots?myself.DF.colorFunc:null)
    

    if (this.chart.options.clickable){
      this.pvDot
      .cursor("pointer")
      .event("click",function(d){
        var v, c;
        var s = myself.chart.dataEngine.getSeries()[this.parent.index]
        if( typeof d == "object"){
          v = d.value;
          c = d.category
        }
        else{
          v = d
          c = myself.chart.dataEngine.getCategories()[this.index]
        }
        return myself.chart.options.clickAction(s,c, v);
      });
    }



    if(this.showValues){
      this.pvLabel = this.pvDot
      .anchor(this.valuesAnchor)
      .add(pv.Label)
      .bottom(0)
      .text(function(d){
        return myself.chart.options.valueFormat(typeof d == "object"?d.value:d)
      })

      // Extend lineLabel
      this.extend(this.pvLabel,"lineLabel_");
    }


    // Extend line and linePanel
    this.extend(this.pvScatterPanel,"scatterPanel_");
    this.extend(this.pvArea,"area_");
    this.extend(this.pvLine,"line_");
    this.extend(this.pvDot,"dot_");
    this.extend(this.pvLabel,"label_");


    // Extend body
    this.extend(this.pvPanel,"chart_");

  }

});






/**
 * ScatterAbstract is the class that will be extended by dot, line, stackedline and area charts.
 */

pvc.MetricScatterAbstract = pvc.MetricAbstract.extend({

  scatterChartPanel : null,
  tipsySettings: {
    gravity: "s",
    fade: true
  },

  constructor: function(o){

    this.base(o);

    var _defaults = {
      showDots: false,
      showLines: false,
      showAreas: false,
      showValues: false,
      showTooltips: true,
      axisOffset: 0.05,
      valuesAnchor: "right",
      stacked: false,
      originIsZero: true,
      orientation: "vertical",
      timeSeries: false,
      timeSeriesFormat: "%Y-%m-%d",
      panelSizeRatio: 1,
      orthoFixedMin: null,
      orthoFixedMax: null
    };


    // Apply options
    $.extend(this.options,_defaults, o);


  },

  preRender: function(){

    this.base();

    pvc.log("Prerendering in MetricScatterAbstract");


    this.scatterChartPanel = new pvc.MetricScatterChartPanel(this, {
      stacked: this.options.stacked,
      showValues: this.options.showValues,
      valuesAnchor: this.options.valuesAnchor,
      showLines: this.options.showLines,
      showDots: this.options.showDots,
      showAreas: this.options.showAreas,
      showTooltips: this.options.showTooltips,
      orientation: this.options.orientation,
      timeSeries: this.options.timeSeries,
      timeSeriesFormat: this.options.timeSeriesFormat
    });

    this.scatterChartPanel.appendTo(this.basePanel); // Add it

  }

}
);

/**
 * Metric Dot Chart
 *
 */

pvc.MetricDotChart = pvc.MetricScatterAbstract.extend({

  constructor: function(o){

    this.base();

    var _defaults = {
      showDots: true,
      showLines: false,
      showAreas: false,
      showValues: false,
      stacked: false
    };

    // Apply options
    $.extend(this.options,_defaults, o);

  }
});


/**
 * Metric Line Chart
 *
 */

pvc.MetricLineChart = pvc.MetricScatterAbstract.extend({

  constructor: function(o){

    this.base();

    var _defaults = {
      showDots: false, // ask
      showLines: true,
      showAreas: false,
      showValues: false,
      stacked: false
    };

    // Apply options
    $.extend(this.options,_defaults, o);


  }
});



/**
 * Metric Stacked Line Chart
 *
 */

pvc.mStackedLineChart = pvc.MetricScatterAbstract.extend({

  constructor: function(o){

    this.base();

    var _defaults = {
      showDots: false, // ask
      showLines: true,
      showAreas: false,
      showValues: false,
      stacked: true
    };

    // Apply options
    $.extend(this.options,_defaults, o);


  }
});


/**
 * Metric Stacked Area Chart
 *
 */

pvc.mStackedAreaChart = pvc.MetricScatterAbstract.extend({

  constructor: function(o){

    this.base();

    var _defaults = {
      showDots: false, // ask
      showLines: false,
      showAreas: true,
      showValues: false,
      stacked: true
    };

    // Apply options
    $.extend(this.options,_defaults, o);


  }
});

/**
 * WaterfallChart is the main class for generating... waterfall charts.
 * 
 * The waterfall chart is an alternative to the pie chart for
 * showing distributions. The advantage of the waterfall chart is that
 * it possibilities to visualize sub-totals and offers more convenient
 * possibilities to compare the size of categories (in a pie-chart you
 * have to compare wedges that are at a different angle, which
 * requires some additional processing/brainpower of the end-user).
 *
 * Waterfall charts are basically Bar-charts with some added
 * functionality. Given the complexity of the added features this
 * class has it's own code-base. However, it would be easy to
 * derive a BarChart class from this class by swithing of a few 
 * features.
 * 
 * If you have an issue or suggestions regarding the Waterfall-charts
 * please contact CvK at cde@vinzi.nl
 */



pvc.WaterfallChart = pvc.CategoricalAbstract.extend({

    wfChartPanel : null,

    constructor: function(o){

        this.base(o);

        var _defaults = {
            showValues: true,
            stacked: true,
            waterfall: true,
            panelSizeRatio: 0.9,
            barSizeRatio: 0.9,
            maxBarSize: 2000,
            originIsZero: true,
            axisOffset: 0,
            showTooltips: true,
            orientation: "vertical",
            orthoFixedMin: null,
            orthoFixedMax: null
        };

        // Apply options
        $.extend(this.options,_defaults, o);

        //  force stacked to be true (default of base-class is false)
        this.options.stacked = true;

        return;
    },

    callWithHiddenFirstSeries: function(callFunc) {
        var res;
        var de = this.dataEngine;

        if (de.isVisible("series", 0)) {
            de.toggleSerieVisibility(0);
            res = callFunc.call(this);
            de.toggleSerieVisibility(0);
        } else
            res = callFunc();

        return res;
    } ,

    preRender: function(){

        // first series are symbolic labels, so hide it such that
        // the axis-range computation is possible in "AbstractCategoricalAxis.
        this.callWithHiddenFirstSeries( this.base );

	var logMessage = "Prerendering a ";
	if (this.options.waterfall)
            logMessage += "WaterfallChart";
	else logMessage +=  ((this.options.stacked) ?
			     "stacked" : "normal")  +  " BarChart";
	pvc.log(logMessage);

        this.wfChartPanel = new pvc.WaterfallChartPanel(this, {
            stacked: this.options.stacked,
            waterfall: this.options.waterfall,
            panelSizeRatio: this.options.panelSizeRatio,
            barSizeRatio: this.options.barSizeRatio,
            maxBarSize: this.options.maxBarSize,
            showValues: this.options.showValues,
            showTooltips: this.options.showTooltips,
            orientation: this.options.orientation
        });

        this.wfChartPanel.appendTo(this.basePanel); // Add it

        return;
    }
}
);


/*
 * Waterfall chart panel (also bar-chart). Generates a bar chart. Specific options are:
 * <i>orientation</i> - horizontal or vertical. Default: vertical
 * <i>showValues</i> - Show or hide bar value. Default: false
 * <i>stacked</i> -  Stacked? Default: false
 * <i>panelSizeRatio</i> - Ratio of the band occupied by the pane;. Default: 0.5 (50%)
 * <i>barSizeRatio</i> - In multiple series, percentage of inner
 * band occupied by bars. Default: 0.5 (50%)
 * <i>maxBarSize</i> - Maximum size of a bar in pixels. Default: 2000
 *
 * Has the following protovis extension points:
 *
 * <i>chart_</i> - for the main chart Panel
 * <i>bar_</i> - for the actual bar
 * <i>barPanel_</i> - for the panel where the bars sit
 * <i>barLabel_</i> - for the main bar label
 */


pvc.WaterfallChartPanel = pvc.BasePanel.extend({

    _parent: null,
    pvBar: null,
    pvBarLabel: null,
    pvWaterfallLine: null,
    pvCategoryPanel: null,
    pvSecondLine: null,
    pvSecondDot: null,
    data: null,
  
    stacked: false,
    panelSizeRatio: 1,
    barSizeRatio: 0.5,
    showTooltips: true,
    maxBarSize: 200,
    showValues: true,
    orientation: "vertical",
    tipsySettings: {
        gravity: "s",
        fade: true
    },
    ruleData: null,


    constructor: function(chart, options){

        this.base(chart,options);

        return;
    },


    callWithHiddenFirstSeries: function(env, callFunc) {
        var res;
        var de = this.chart.dataEngine;

        if (de.isVisible("series", 0)) {
            de.toggleSerieVisibility(0);
            switch (arguments.length) {
                case 2:
                    res = callFunc.call(env);
                    break;
                case 3:
                    res = callFunc.call(env, arguments[2]);
                    break;
                case 4:
                    res = callFunc.call(env, arguments[2], arguments[3]);
                    break;
                default:
                    pvc.log("ERROR: wrong number of arguments in callWithHiddenFirstSeries!!")

            }
            de.toggleSerieVisibility(0);
        } else
            res = callFunc();

        return res;
    } ,


    /****
   *  Functions that transforms a dataset to waterfall-format.
   *
   * The assumption made is that the first category is a tekst column
   * containing one of the following values:
   *    - "U":  If this category (row) needs go upwards (height
   *       increases)
   *    - "D": If the waterfall goes downward.
   *    - other values: the waterfall resets to zero (used represent
   *        intermediate subtotal) Currently subtotals need to be
   *        provided in the dataset.
   *  This function computes the offsets of each bar and stores the
   *  offset in the first category (for stacked charts)
   ****/
  
    constructWaterfall: function(dataset) 
    {
        var cumulated = 0.0;
        var ruleData = [[],[]];
    
        var cats = this.chart.dataEngine.getVisibleCategoriesIndexes(); 
    
        for(var c=0; c<dataset[0].length; c++) {
            var mult;
      
            // store the category
            ruleData[0].push(cats[c]);

            // determine next action (direction)
            if (dataset[0][c] == "U")
                mult = 1.0;
            else if (dataset[0][c] == "D")
                mult = -1.0;
            else {
                mult = 1.0;
                cumulated = 0.0;
            }
            if (mult > 0.0)
                dataset[0][c] = cumulated;
      
            // update the other series and determine new cumulated
            for(var ser=1; ser<dataset.length; ser++) {
                var val = Math.abs(dataset[ser][c]);
                dataset[ser][c] = val;  // negative values not allowed
                // only use negative values for internal usage in waterfall
                cumulated += mult*val;
            }
            if (mult < 0.0)
                dataset[0][c] = cumulated;
            ruleData[1].push(cumulated);
        }
        return ruleData;
    },


    getDataSet:  function() {
        
        //clear needed to force re-fetch of visible series
        this.chart.dataEngine.clearDataCache();
        
        var dataset = null
        // check whether it does not kill the source-data    
        dataset = this.stacked ?  
        pvc.padMatrixWithZeros(this.chart.dataEngine
            .getVisibleTransposedValues()) :
        this.chart.dataEngine.getVisibleCategoriesIndexes();

        if (this.waterfall)
            this.ruleData = this.constructWaterfall(dataset)

        return dataset;
    } ,




    /*
     *   This function implements a number of helper functions in order
     *   to increase the readibily and extendibility of the code by:
     *    1: providing symbolic names to the numerous anonymous
     *        functions that need to be passed to CC
     *    2: by moving large parts of the local variabele (parameters
     *       and scaling functions out of the 'create' function to this
     *       prepareDataFunctions blok. 
     *    3: More sharing of code due to introduction of the 'this.DF'
     *        for storing all helper functions.
     *    4: increased code-sharing between stacked and non-stacked
     *       variant of the bar chart.
     *    The create function is now much cleaner and easier to understand.
     *
     *   These helper functions (closures) are all stored in 'this.DF'
     *
     *   Overriding this 'prepareDataFunctions' allows you to implement
     *   a different ScatterScart.
     *   however, it is also possible to replace specific functions
     *   from the 'this.DF' object.
     */
    prepareDataFunctions:  function(stacked) {
        var myself = this;

        // create empty container for the functions and data
        this.DF = {}

        // first series are symbolic labels, so hide it such that
        // the axis-range computation is possible.
        /*
    var lScale = this.waterfall ?
      this.callWithHiddenFirstSeries(this.chart,
           this.chart.getLinearScale, true):
      this.chart.getLinearScale(true);
*/
        /** start  fix  (need to resolve this nicely  (CvK))**/
        var lScale;
        if (this.waterfall) {
            // compute the dataset
            var ds = this.getDataSet();
            // extract the maximum
            var mx = 0.0 
            for(var c=0; c<ds[0].length; c++) {
                var h = 0.0;
                for(var r=0; r<ds.length; r++)
                    h += ds[r][c];
                if (h > mx)  mx = h;
            }
            // set maximum as a fixed bound
            this.chart.options.orthoFixedMax = mx;	

            lScale = this.chart.getLinearScale(true);

        } else
            lScale = this.chart.getLinearScale(true);
        /** end fix **/
        var l2Scale = this.chart.getSecondScale(true);
        var oScale = this.chart.getOrdinalScale(true);
        var bScale = null;

        // determine barPositionOffset and bScale
        this.DF.maxBarSize = null;
        var barPositionOffset = 0;
        if (stacked) {
            this.DF.maxBarSize = oScale.range().band;

            //CvK: check whether bScale is ever used for stacked graphs!)
            bScale = new pv.Scale.ordinal([0])
            .splitBanded(0, oScale.range().band, this.barSizeRatio);      
      
        } else {
            bScale = new pv.Scale.ordinal(
                this.chart.dataEngine.getVisibleSeriesIndexes())
            .splitBanded(0, oScale.range().band, this.barSizeRatio);
            // We need to take into account the maxValue if our band is higher than that
            this.DF.maxBarSize = bScale.range().band;
        }
        if (this.DF.maxBarSize > this.maxBarSize) {
            barPositionOffset = (this.DF.maxBarSize - this.maxBarSize)/2 ;
            this.DF.maxBarSize = this.maxBarSize;
        }
        // export needed for generated overflow markers.
        this.DF.bScale = bScale;


        /*
     * functions to determine positions along base axis.
     */
        this.DF.basePositionFunc = stacked ?
        function(d){
            var res = oScale(this.index) + barPositionOffset;
            // This function used this pointer instead of d !!
            return res
        } :
        null;

        this.DF.baseRulePosFunc = stacked ?
        function(d){
            var res = oScale(d) + barPositionOffset;
            return res
        } :
        null;

        this.DF.catContainerBasePosFunc = (stacked) ? null :
        function(d){
            return oScale(myself.chart.dataEngine.getVisibleCategories()[d]);
        };

        this.DF.catContainerWidth = (stacked) ? null :
        oScale.range().band;

        this.DF.relBasePosFunc  = (stacked) ? null :
        function(d){
            var res = bScale(myself.chart.dataEngine
                .getVisibleSeriesIndexes()[this.index]) + barPositionOffset;
            return res;
        };

        this.DF.secBasePosFunc = 
        function(d){
            if(myself.timeSeries){
                return tScale(parser.parse(d.category));
            }
            else{
                return oScale(d.category) + oScale.range().band/2;
            }
        };

        /*
     * functions to determine positions along orthogonal axis
     */
        this.DF.orthoBotPos = stacked ?
        lScale(0) :
        function(d){
            return lScale(pv.min([0,d]));
        };

        this.DF.orthoLengthFunc = stacked ? 
        function(d){
            var res = myself.chart.animate(0, lScale(d||0)-lScale(0));
            return res;
        } :
        function(d){
            var res = myself.chart.animate(0, 
                Math.abs(lScale(d||0) - lScale(0)));
            return res;
        };

        this.DF.secOrthoLengthFunc = 
        function(d){
            return myself.chart.animate(0,l2Scale(d.value));
        };


    /*
     * functions to determine the color palette.
     */
        var colors = this.chart.colors(pv.range(this.chart.dataEngine.getSeriesSize()));
        // colorFunc is used for the base dataseries
        this.DF.colorFunc = function(d){
            var ind = this.parent.index;
            if (myself.waterfall) {
                if (ind == 0)
                    return pv.Color.names["transparent"];
            //        ind--;   don't do the ind-- otherwise it doesn't match legend
            }
            return colors (myself.chart.dataEngine
                .getVisibleSeriesIndexes()[ind]);
        };
        // colorFunc2 is used for ....
        this.DF.colorFunc2 = function(d){
            return colors(myself.chart.dataEngine
                .getVisibleSeriesIndexes()[this.index])
        };

        return;
    } ,



    /****
   *  Functions used to draw a set of horizontal rules that connect
   *  the bars that compose the waterfall
   ****/
    drawWaterfalls: function(panel) {
        var ruleData = this.ruleData;

        if (this.stacked)
            this.drawRules(panel, ruleData[0], ruleData[1], 2);
        else
            pvc.log("Waterfall not implemented for none-stacked");
    } ,

    drawRules: function(panel, cats, vals, offset) {
        var data = []; 

        var anchor = this.orientation == "vertical"?"bottom":"left";

        // build the dataset as a hashmap
        var x1 = this.DF.baseRulePosFunc(cats[0]) +offset;
        for(var i=0; i<cats.length-1; i++) 
        // this is the function for stacked data
        {
            var x2 = this.DF.baseRulePosFunc(cats[i+1]) + offset;
            data.push({
                x: x1, 
                y:  this.DF.orthoLengthFunc(vals[i]), 
                w: x2 - x1
            });
            x1 = x2;  // go to next element
        }

        this.pvWaterfallLine = panel.add(pv.Rule)
        .data(data)
        [pvc.BasePanel.relativeAnchor[anchor]](function(d) {
            return d.x
        })
        [anchor](function(d) {
            return d.y
        })
        [pvc.BasePanel.paralelLength[anchor]](function(d) {
            return d.w
        })
        .strokeStyle("#c0c0c0");

        return;
    },



    create: function(){
        var myself = this;
        this.width = this._parent.width;
        this.height = this._parent.height;

        this.pvPanel = this._parent.getPvPanel().add(this.type)
        .width(this.width)
        .height(this.height)

        if  (   (myself.chart.options.orthoFixedMin != null)
            || (myself.chart.options.orthoFixedMax != null) )
            this.pvPanel["overflow"]("hidden");

        var anchor = this.orientation == "vertical"?"bottom":"left";

        // prepare data and functions when creating (rendering) the chart.
        this.prepareDataFunctions(this.stacked);


        var maxBarSize = this.DF.maxBarSize;

        if (this.stacked){
            var dataset = this.getDataSet();

            if (this.orientation == "vertical")
                pvc.log("WARNING: currently the 'horizontal' orientation is not possible for stacked barcharts and waterfall charts (will be implemented later)");

            if (this.waterfall)
                this.drawWaterfalls(this.pvPanel);

            this.pvBarPanel = this.pvPanel.add(pv.Layout.Stack)
            .layers(dataset)
            [this.orientation == "vertical"?"y":"x"](myself.DF.orthoLengthFunc)
            [anchor](myself.DF.orthoBotPos)
            [this.orientation == "vertical"?"x":"y"](myself.DF.basePositionFunc);

            this.pvBar = this.pvBarPanel.layer.add(pv.Bar)
            .data(function(d){
                return d
            })
            [pvc.BasePanel.paralelLength[anchor]](maxBarSize)
            .fillStyle(myself.DF.colorFunc);

        } else {   //  not this.stacked

            // define a container (panel) for each category label.
            // later the individuals bars of series will be drawn in 
            // these panels.
            this.pvBarPanel = this.pvPanel.add(pv.Panel)
            .data(this.getDataSet() )
            [pvc.BasePanel.relativeAnchor[anchor]](myself.DF.catContainerBasePosFunc)
            [anchor](0)
            [pvc.BasePanel.paralelLength[anchor]](myself.DF.catContainerWidth)
            // pvBarPanel[X]  = this[X]  (copy the function)
            [pvc.BasePanel.orthogonalLength[anchor]](
                this[pvc.BasePanel.orthogonalLength[anchor]])

            // next add the bars to the bar-containers in pvBarPanel
            this.pvBar = this.pvBarPanel.add(pv.Bar)
            .data(function(d){
                var res = myself.chart.dataEngine
                .getVisibleValuesForCategoryIndex(d);
                return res;
                })
            .fillStyle(myself.DF.colorFunc2)
            [pvc.BasePanel.relativeAnchor[anchor]](myself.DF.relBasePosFunc)
            [anchor](myself.DF.orthoBotPos)
            [pvc.BasePanel.orthogonalLength[anchor]](myself.DF.orthoLengthFunc)
            [pvc.BasePanel.paralelLength[anchor]](maxBarSize)  ; 

        }  // end of if (stacked)

        // generate red markers if some data falls outside the panel bounds
        this.generateOverflowMarkers(anchor, this.stacked);


        if(this.chart.options.secondAxis){
            // Second axis - support for lines
            this.pvSecondLine = this.pvPanel.add(pv.Line)
            .data(function(d){
                return myself.chart.dataEngine.getObjectsForSecondAxis(d, 
                    this.timeSeries ? function(a,b){
                    return parser.parse(a.category) - parser.parse(b.category);
                    }: null)
                })
            .strokeStyle(this.chart.options.secondAxisColor)
            [pvc.BasePanel.relativeAnchor[anchor]](myself.DF.secBasePosFunc)
            [anchor](myself.DF.secOrthoLengthFunc);

            this.pvSecondDot = this.pvSecondLine.add(pv.Dot)
            .shapeSize(8)
            .lineWidth(1.5)
            .fillStyle(this.chart.options.secondAxisColor)
        }

        // add Labels:
        this.pvBar
        .text(function(d){
            var v = myself.chart.options.valueFormat(d);
            var s = myself.chart.dataEngine
            .getVisibleSeries()[myself.stacked?this.parent.index:this.index]
            var c = myself.chart.dataEngine
            .getVisibleCategories()[myself.stacked?this.index:this.parent.index]
            return myself.chart.options.tooltipFormat.call(myself,s,c,v);
    
        })

        if(this.showTooltips){
            // Extend default
            this.extend(this.tipsySettings,"tooltip_");
            this.pvBar
            .event("mouseover", pv.Behavior.tipsy(this.tipsySettings));
        }


        if (this.chart.options.clickable){
            this.pvBar
            .cursor("pointer")
            .event("click",function(d){
                var s = myself.chart.dataEngine
                .getVisibleSeries()[myself.stacked?this.parent.index:this.index]
                var c = myself.chart.dataEngine
                .getVisibleCategories()[myself.stacked?this.index:this.parent.index]
                return myself.chart.options.clickAction(s,c, d);
            });
        }

        if(this.showValues){
            this.pvBarLabel = this.pvBar
            .anchor("center")
            .add(pv.Label)
            .bottom(0)
            .text(function(d){
                return myself.chart.options.valueFormat(d);
            })
      
            // Extend barLabel
            this.extend(this.pvBarLabel,"barLabel_");
        }

        // Extend waterfall line
	if (this.waterfall)
	    this.extend(this.pvWaterfallLine,"barWaterfallLine_");

        // Extend bar and barPanel
        this.extend(this.pvBar,"barPanel_");
        this.extend(this.pvBar,"bar_");
    

        // Extend body
        this.extend(this.pvPanel,"chart_");

    },


    /*******
   *  Function used to generate overflow and underflowmarkers.
   *  This function is only used when fixedMinX and orthoFixedMax are set
   *
   *******/

    generateOverflowMarkers: function(anchor, stacked)
    {
        var myself = this;

        if (stacked) {
            if (   (myself.chart.options.orthoFixedMin != null)
                || (myself.chart.options.orthoFixedMin != null) )  
                pvc.log("WARNING: overflow markers not implemented for Stacked graph yet");
        } else {
            if      (myself.chart.options.orthoFixedMin != null)
                // CvK: adding markers for datapoints that are off-axis
                //  UNDERFLOW  =  datavalues < orthoFixedMin
                this.doGenOverflMarks(anchor, true, this.DF.maxBarSize, 
                    0, this.DF.bScale,
                    function(d){
                        var res = myself.chart.dataEngine
                        .getVisibleValuesForCategoryIndex(d);
                        // check for off-grid values (and replace by null)
                        var fixedMin = myself.chart.options.orthoFixedMin;
                        for(var i=0; i<res.length; i++)
                            res[i] = (res[i] < fixedMin) ? fixedMin : null; 
                        return res;
                    });
      
            if (myself.chart.options.orthoFixedMax != null)
                // CvK: overflow markers: max > orthoFixedMax
                this.doGenOverflMarks(anchor, false, this.DF.maxBarSize, 
                    Math.PI, this.DF.bScale,
                    function(d){
                        var res = myself.chart.dataEngine
                        .getVisibleValuesForCategoryIndex(d);
                        // check for off-grid values (and replace by null)
                        var fixedMax = myself.chart.options.orthoFixedMax;
                        for(var i=0; i<res.length; i++)
                            res[i] = (res[i] > fixedMax) ? fixedMax : null; 
                        return res;
                    });
        };
        return;
    },

    // helper routine used for both underflow and overflow marks
    doGenOverflMarks: function(anchor, underflow, maxBarSize, angle,
        bScale, dataFunction)
        {
        var myself = this;
        var offGridBarOffset = maxBarSize/2;
    
        var offGridBorderOffset = (underflow) ?
        this.chart.getLinearScale(true).min + 8  :
        this.chart.getLinearScale(true).max - 8   ;
    
        if (this.orientation != "vertical")
            angle += Math.PI/2.0;
    
        this.overflowMarkers = this.pvBarPanel.add(pv.Dot)
        .shape("triangle")
        .shapeSize(10)
        .shapeAngle(angle)
        .lineWidth(1.5)
        .strokeStyle("red")
        .fillStyle("white")
        .data(dataFunction)
        [pvc.BasePanel.relativeAnchor[anchor]](function(d){
            var res = bScale(myself.chart.dataEngine
                .getVisibleSeriesIndexes()[this.index])
            + offGridBarOffset;
            return res;
        })
        [anchor](function(d){ 
            // draw the markers at a fixed position (null values are
            // shown off-grid (-1000)
            return (d != null) ? offGridBorderOffset: -10000;
        }) ;
    }

});
/**
 * Bullet chart generation
 */

pvc.BulletChart = pvc.Base.extend({

  bulletChartPanel : null,
  allowNoData: true,

  constructor: function(o){

    this.base(o);

    var _defaults = {
      showValues: true,
      orientation: "horizontal",
      showTooltips: true,
      legend: false,

      bulletSize: 30,        // Bullet size
      bulletSpacing: 50,     // Spacing between bullets
      bulletMargin: 100,     // Left margin

      // Defaults
      bulletMarkers: [],     // Array of markers to appear
      bulletMeasures: [],    // Array of measures
      bulletRanges: [],      // Ranges
      bulletTitle: "Bullet", // Title
      bulletSubtitle: "",    // Subtitle

      crosstabMode: true,
      seriesInRows: true,

      tipsySettings: {
        gravity: "s",
        fade: true
      }

    };


    // Apply options
    $.extend(this.options,_defaults, o);


  },

  preRender: function(){

    this.base();

    pvc.log("Prerendering in bulletChart");


    this.bulletChartPanel = new pvc.BulletChartPanel(this, {
      showValues: this.options.showValues,
      showTooltips: this.options.showTooltips,
      orientation: this.options.orientation
    });

    this.bulletChartPanel.appendTo(this.basePanel); // Add it

  }

}
);



/*
 * Bullet chart panel. Generates a bar chart. Specific options are:
 * <i>orientation</i> - horizontal or vertical. Default: vertical
 * <i>showValues</i> - Show or hide bar value. Default: false
 *
 * Has the following protovis extension points:
 *
 * <i>chart_</i> - for the main chart Panel
 * <i>bulletsPanel_</i> - for the bullets panel
 * <i>bulletPanel_</i> - for the bullets pv.Layout.Bullet
 * <i>bulletRange_</i> - for the bullet range
 * <i>bulletMeasure_</i> - for the bullet measure
 * <i>bulletMarker_</i> - for the marker
 * <i>bulletRule_</i> - for the axis rule
 * <i>bulletRuleLabel_</i> - for the axis rule label
 * <i>bulletTitle_</i> - for the bullet title
 * <i>bulletSubtitle_</i> - for the main bar label
 */


pvc.BulletChartPanel = pvc.BasePanel.extend({

  _parent: null,
  pvBullets: null,
  pvBullet: null,
  data: null,

  showTooltips: true,
  showValues: true,
  tipsySettings: {
    gravity: "s",
    fade: true
  },

  constructor: function(chart, options){

    this.base(chart,options);

  },

  create: function(){

    var myself = this;
    this.width = this._parent.width;
    this.height = this._parent.height;

    var data = this.buildData();

    this.pvPanel = this._parent.getPvPanel().add(pv.Panel)
    .width(this.width)
    .height(this.height);

    var anchor = myself.chart.options.orientation=="horizontal"?"left":"bottom";
    var size, angle, align, titleOffset, ruleAnchor, leftPos, topPos;
    
    if(myself.chart.options.orientation=="horizontal"){
      size = this.width - this.chart.options.bulletMargin - 20;
      angle=0;
      align = "right";
      titleOffset = 0;
      ruleAnchor = "bottom";
      leftPos = this.chart.options.bulletMargin;
      topPos = function(){
        return this.index * (myself.chart.options.bulletSize + myself.chart.options.bulletSpacing);
      }
    }
    else
    {
      size = this.height - this.chart.options.bulletMargin - 20;
      angle = -Math.PI/2;
      align = "left";
      titleOffset = -12;
      ruleAnchor = "right";
      leftPos = function(){
        return myself.chart.options.bulletMargin + this.index * (myself.chart.options.bulletSize + myself.chart.options.bulletSpacing);
      }
      topPos = undefined;

    }

    this.pvBullets = this.pvPanel.add(pv.Panel)
    .data(data)
    [pvc.BasePanel.orthogonalLength[anchor]](size)
    [pvc.BasePanel.paralelLength[anchor]](this.chart.options.bulletSize)
    .margin(20)
    .left(leftPos) // titles will be on left always
    .top(topPos);
    

    this.pvBullet = this.pvBullets.add(pv.Layout.Bullet)
    .orient(anchor)
    .ranges(function(d){
      return d.ranges
    })
    .measures(function(d){
      return d.measures
    })
    .markers(function(d){
      return d.markers
    });

    this.pvBulletRange = this.pvBullet.range.add(pv.Bar);
    this.pvBulletMeasure = this.pvBullet.measure.add(pv.Bar)
    .text(function(d){
      return myself.chart.options.valueFormat(d);
    });


    this.pvBulletMarker = this.pvBullet.marker.add(pv.Dot)
    .shape("square")
    .fillStyle("white")
    .text(function(d){
      return myself.chart.options.valueFormat(d);
    });


    if(this.showTooltips){
      // Extend default
      this.extend(this.tipsySettings,"tooltip_");
      this.pvBulletMeasure.event("mouseover", pv.Behavior.tipsy(this.tipsySettings));
      this.pvBulletMarker.event("mouseover", pv.Behavior.tipsy(this.tipsySettings));
    }

    this.pvBulletRule = this.pvBullet.tick.add(pv.Rule)

    this.pvBulletRuleLabel = this.pvBulletRule.anchor(ruleAnchor).add(pv.Label)
    .text(this.pvBullet.x.tickFormat);

    this.pvBulletTitle = this.pvBullet.anchor(anchor).add(pv.Label)
    .font("bold 12px sans-serif")
    .textAngle(angle)
    .left(-10)
    .textAlign(align)
    .textBaseline("bottom")
    .left(titleOffset)
    .text(function(d){
      return d.title
    });

    this.pvBulletSubtitle = this.pvBullet.anchor(anchor).add(pv.Label)
    .textStyle("#666")
    .textAngle(angle)
    .textAlign(align)
    .textBaseline("top")
    .left(titleOffset)
    .text(function(d){
      return d.subtitle
    });

    // Extension points
    this.extend(this.pvBullets,"bulletsPanel_");
    this.extend(this.pvBullet,"bulletPanel_");
    this.extend(this.pvBulletRange,"bulletRange_");
    this.extend(this.pvBulletMeasure,"bulletMeasure_");
    this.extend(this.pvBulletMarker,"bulletMarker_");
    this.extend(this.pvBulletRule,"bulletRule_");
    this.extend(this.pvBulletRuleLabel,"bulletRuleLabel_");
    this.extend(this.pvBulletTitle,"bulletTitle_");
    this.extend(this.pvBulletSubtitle,"bulletSubtitle_");

    // Extend body
    this.extend(this.pvPanel,"chart_");

  },

  /*
   * Data array to back up bullet charts; Case 1:
   *
   * <i>1) No data is passed</i> - In this case, we'll grab all the value from the options
   * and generate only one bullet
   *
   */

  buildData: function(){

    pvc.log("In buildData: " + this.chart.dataEngine.getInfo() );


    var defaultData = {
      title: this.chart.options.bulletTitle,
      subtitle: this.chart.options.bulletSubtitle,
      ranges:this.chart.options.bulletRanges,
      measures: this.chart.options.bulletMeasures,
      markers: this.chart.options.bulletMarkers
    };
    
    var data = [];

    if(this.chart.dataEngine.getSeriesSize() == 0 ){
      // No data
      data.push($.extend({},defaultData));

    }
    else{

      // We have data. Iterate through the series.
      var indices = this.chart.dataEngine.getVisibleSeriesIndexes()
      for(var i in indices) if (indices.hasOwnProperty(i)){
        var s = this.chart.dataEngine.getSerieByIndex(i);
        var v = this.chart.dataEngine.getVisibleValuesForSeriesIndex(i);
        var d = $.extend({},defaultData);

        switch(v.length){
          case 0:
            // Value only
            d.measures = [s];
            break;
          case 2:
            // Name, value and markers
            d.markers = [v[1]]
          case 1:
            // name and value
            d.title = s;
            d.measures = [v[0]];
            break;
          default:
            // greater or equal 4
            d.title = s;
            d.subtitle = v[0];
            d.measures = [v[1]];
            d.markers = [v[2]]
            d.ranges = v.slice(3);
        }


        data.push(d);
      }

    }
   
    return data;
  }

});
/**
 * Parallel coordinates offer a way to visualize data and make (sub-)selections
 * on this dataset.
 * This code has been based on a protovis example:
 *    http://vis.stanford.edu/protovis/ex/cars.html
 */


pvc.ParallelCoordinates = pvc.Base.extend({

  parCoordPanel : null,
  legendSource: "categories",
  tipsySettings: {
    gravity: "s",
    fade: true
  },

  constructor: function(o){

    this.base(o);

    var _defaults = {
      topRuleOffset: 30,
      botRuleOffset: 30,
      leftRuleOffset: 60,
      rightRuleOffset: 60,
	// sort the categorical (non-numerical dimensions)
      sortCategorical: true,
	// map numerical dimension too (uniform (possible non-linear)
	// distribution of the observed values)
      mapAllDimensions: true,
	// number of digits after decimal point.
      numDigits: 0
    };


    // Apply options
    $.extend(this.options,_defaults, o);

    return;
  },

  preRender: function(){

    this.base();

    pvc.log("Prerendering in parallelCoordinates");

    this.parCoordPanel = new pvc.ParCoordPanel(this, {
      topRuleOffset : this.options.topRuleOffset,
      botRuleOffset : this.options.botRuleOffset,
      leftRuleOffset : this.options.leftRuleOffset,
      rightRuleOffset : this.options.rightRuleOffset,
      sortCategorical : this.options.sortCategorical,
      mapAllDimensions : this.options.mapAllDimensions,
      numDigits : this.options.numDigits
    });

    this.parCoordPanel.appendTo(this.basePanel); // Add it

    return;
  }

}
);


/*
 * ParCoord chart panel. Generates a serie of Parallel Coordinate axis 
 * and allows you too make selections on these parallel coordinates.
 * The selection will be stored in java-script variables and can be
 * used as part of a where-clause in a parameterized SQL statement.
 * Specific options are:
 *   << to be filled in >>

 * Has the following protovis extension points:
 *
 * <i>chart_</i> - for the main chart Panel
 * <i>parCoord_</i> - for the parallel coordinates
 *    << to be completed >>
 */


pvc.ParCoordPanel = pvc.BasePanel.extend({

  _parent: null,
  pvParCoord: null,

  dimensions: null, 
  dimensionDescr: null,

  data: null,


  constructor: function(chart, options){

    this.base(chart,options);

  },

    /*****
     * retrieve the data from database and transform it to maps.
     *    - this.dimensions: all dimensions
     *    - this.dimensionDescr: description of dimensions
     *    - this.data: array with hashmap per data-point
     *****/
  retrieveData: function () {
    var de = this.chart.dataEngine;
    var numDigit = this.chart.options.numDigits;

    this.dimensions = de.getVisibleCategories();
    var values = de.getValues();

    var dataRowIndex = de.getVisibleSeriesIndexes();
    var pCoordIndex = de.getVisibleCategoriesIndexes();

    var pCoordKeys = de.getCategories();

    /******
     *  Generate a Coordinate mapping. 
     *  This mapping is required for categorical dimensions and
     *  optional for the numerical dimensions (in 4 steps)
     ********/
    // 1: generate an array of coorMapping-functions
    // BEWARE: Only the first row (index 0) is used to test whether 
    // a dimension is categorical or numerical!
    var pCoordMapping = (this.chart.options.mapAllDimensions) ?
      pCoordIndex.map( function(d) {return (isNaN(values[d][0])) ? 
              {categorical: true, len: 0, map: [] } : 
                             {categorical: false, len: 0,
                                 map: [], displayValue: [] }; })
    : pCoordIndex.map( function(d) {return (isNaN(values[d][0])) ? 
              {categorical: true, len: 0, map: [] } : 
              null; }) ;
  
      // 2: and generate a helper-function to update the mapping
      //  For non-categorical value the original-value is store in displayValue
    var coordMapUpdate = function(i, val) {
      var cMap = pCoordMapping[i];
      var k = null; // define in outer scope.
      if (cMap.categorical == false) {
        var keyVal = val.toFixed(numDigit);   // force the number to be a string
        k = cMap.map[keyVal];
        if (k == null) {
          k = cMap.len;
          cMap.len++;
          cMap.map[keyVal] = k;
          cMap.displayValue[keyVal] = val;
        }
      } else {
        k = cMap.map[val];
        if (k == null) {
          k = cMap.len;
          cMap.len++;
          cMap.map[val] = k;
        }
      }
      return k;
    };

    // 3. determine the value to be displayed
    //   for the categorical dimensions map == displayValue
    for(var d in pCoordMapping)
      if (   pCoordMapping[d]
          && pCoordMapping[d].categorical)
        pCoordMapping[d].displayValue = pCoordMapping[d].map

    // 4. apply the sorting of the dimension
    if (   this.chart.options.sortCategorical
        || this.chart.options.mapAllDimensions) {
      // prefill the coordMapping in order to get it in sorted order.
      // sorting is required if all dimensions are mapped!!
      for (var i=0; i<pCoordMapping.length; i++) {
         if (pCoordMapping[i]) {
           // add all data
           for (var col=0; col<values[i].length; col++)
               coordMapUpdate(i, values[i][col]);
           // create a sorted array
           var cMap = pCoordMapping[i].map;
           var sorted = [];
           for(var item in cMap)
             sorted.push(item);
           sorted.sort();
           // and assign a new index to all items
           if (pCoordMapping[i].categorical)
             for(var k=0; k<sorted.length; k++)
               cMap[sorted[k]] = k;
           else
             for(var k=0; k<sorted.length; k++)
               cMap[sorted[k]].index = k;
         }      
      }
    }

    /*************
    *  Generate the full dataset (using the coordinate mapping).
    *  (in 2 steps)
    ******/
    //   1. generate helper-function to transform a data-row to a hashMap
    //   (key-value pairs). 
    //   closure uses pCoordKeys and values
    var generateHashMap = function(col) {
      var record = {};
      for(var i in pCoordIndex) {
         record[pCoordKeys[i]] = (pCoordMapping[i]) ?
              coordMapUpdate(i, values[i][col]) :
              values[i][col];
      }
      return record;
    };
    // 2. generate array with a hashmap per data-point
    this.data = dataRowIndex.map(function(col) { return generateHashMap (col)});

    
    /*************
    *  Generate an array of descriptors for the dimensions (in 3 steps).
    ******/
    // 1. find the dimensions
    var descrVals = this.dimensions.map(function(cat)
           {
             var item = {};
             // the part after "__" is assumed to be the units
             var elements = cat.split("__");
             item.id = cat;
             item.name = elements[0];
             item.unit = (elements.length >1)? elements[1] : "";
             return item;
           });

    // 2. compute the min, max and step(-size) per dimension)
    for(var i=0; i<descrVals.length; i++) {
      var item = descrVals[i];
      var index = pCoordIndex[i];
	// orgRowIndex is the index in the original dataset
	// some indices might be (non-existent/invisible)
      item.orgRowIndex = index;

      // determine min, max and estimate step-size
      var len = values[index].length;
      var theMin, theMax, theMin2, theMax2;

      // two version of the same code (one with mapping and one without)
      if (pCoordMapping[index]) {
        theMin = theMax = theMin2 = theMax2 =
               pCoordMapping[index].displayValue[ values[index][0] ] ;

        for(var k=1; k<len; k++) {
          var v = pCoordMapping[index].displayValue[ values[index][k] ] ;
          if (v < theMin)
          {
            theMin2 = theMin;
            theMin = v;
          }
          if (v > theMax) {
            theMax2 = theMax;
            theMax = v;
          }
        }
      } else {  // no coordinate mapping applied
        theMin = theMax = theMin2 = theMax2 = values[index][0];

        for(var k=1; k<len; k++) {
          var v = values[index][k];
          if (v < theMin)
          {
            theMin2 = theMin;
            theMin = v;
          }
          if (v > theMax) {
            theMax2 = theMax;
            theMax = v;
          }
        }
      }   // end else:  coordinate mapping applied

      var theStep = ((theMax - theMax2) + (theMin2-theMin))/2;
      item.min = theMin;
      item.max = theMax;
      item.step = theStep;

      // 3. and include the mapping (and reverse mapping) 
      item.categorical = false; 
      if (pCoordMapping[index]) {
        item.map = pCoordMapping[index].map;
        item.mapLength = pCoordMapping[index].len;
        item.categorical = pCoordMapping[index].categorical; 

        // create the reverse-mapping from key to original value
        if (item.categorical == false) {
          item.orgValue = [];
          var theMap =  pCoordMapping[index].map;
          for (key in theMap)
            item.orgValue[ theMap[key] ] = 0.0+key;
        }
      }
    }

    // generate a object using the given set of keys and values
    //  (map from keys[i] to vals[i])
    var genKeyVal = function (keys, vals) {
       var record = {};
      for (var i = 0; i<keys.length; i++)
         record[keys[i]] = vals[i];
      return record;
    };
    this.dimensionDescr = genKeyVal(this.dimensions, descrVals);
    
    return;
  } ,





  create: function(){

    var myself = this;
    this.width = this._parent.width;
    this.height = this._parent.height;

    this.pvPanel = this._parent.getPvPanel().add(this.type)
    .width(this.width)
    .height(this.height)

    this.retrieveData();

    // used in the different closures
    var height = this.height,
    numDigits = this.chart.options.numDigits,
    topRuleOffs = this.chart.options.topRuleOffset,
    botRuleOffs = this.chart.options.botRuleOffset,
    leftRuleOffs = this.chart.options.leftRuleOffset,
    rightRulePos = this.width - this.chart.options.rightRuleOffset,
    topRulePos = this.height- topRuleOffs;
    ruleHeight = topRulePos - botRuleOffs,
    labelTopOffs = topRuleOffs - 12,
      // use dims to get the elements of dimDescr in the appropriate order!!
    dims = this.dimensions,
    dimDescr = this.dimensionDescr;

    /*****
     *   Generate the scales x, y and color
     *******/
    // getDimSc is the basis for getDimensionScale and getDimColorScale
    var getDimSc = function(t, addMargin) {
      var theMin = dimDescr[t].min;
      var theMax = dimDescr[t].max;
      var theStep = dimDescr[t].step;
      // add some margin at top and bottom (based on step)
      if (addMargin) {
        theMin -= theStep;
        theMax += theStep;
      }
      return pv.Scale.linear(theMin, theMax)
              .range(botRuleOffs, topRulePos);
    }; 
    var getDimensionScale = function(t) {
	var scale = getDimSc(t, true)
              .range(botRuleOffs, topRulePos);
      var dd = dimDescr[t];
      if (   dd.orgValue
          && (dd.categorical == false)) {
        // map the value to the original value
        var func = function(x) { var res = scale( dd.orgValue[x]);
                      return res; };
        // wire domain() and invert() to the original scale
        func.domain = function() { return scale.domain(); };
        func.invert = function(d) { return scale.invert(d); };
        return func;
      }
      else
        return scale;
    }; 
    var getDimColorScale = function(t) {
	var scale = getDimSc(t, false)
              .range("steelblue", "brown");
        return scale;
    }; 

    var x = pv.Scale.ordinal(dims).splitFlush(leftRuleOffs, rightRulePos);
    var y = pv.dict(dims, getDimensionScale);
    var colors = pv.dict(dims, getDimColorScale);



    /*****
     *   Generate tools for computing selections.
     *******/
    // Interaction state. 
    var filter = pv.dict(dims, function(t) {
      return {min: y[t].domain()[0], max: y[t].domain()[1]};  });
    var active = dims[0];   // choose the active dimension 

    var selectVisible = (this.chart.options.mapAllDimensions) ?
      function(d) { return dims.every(  
	    // all dimension are handled via a mapping.
            function(t) {
              var dd = dimDescr[t];
              var val = (dd.orgValue && (dd.categorical == false)) ?
                    dd.orgValue[d[t]] : d[t];
	      return (val >= filter[t].min) && (val <= filter[t].max); }
        )}
    : function(d) { return dims.every(  
            function(t) {
		// TO DO: check whether this operates correctly for
		// categorical dimensions  (when mapAllDimensions == false
		return (d[t] >= filter[t].min) && (d[t] <= filter[t].max); }
        )};
 

    /*****
     *   generateLinePattern produces a line pattern based on
     *          1. the current dataset.
     *          2. the current filter settings.
     *          3. the provided colorMethod.
     *  The result is an array where each element contains at least
     *            {x1, y1, x2, y2, color}
     *  Two auxiliary fields are 
     *  Furthermore auxiliary functions are provided
     *     - genAuxData: generate the auxiliary dataset (of clean is)
     *     - drawLinePattern
     *     - colorFuncBg
     *     - colorFuncFreq
     *     - colorFuncActive
     *******/
      var auxData = null;
      var genAuxData = function() {
	  if (auxData === null) {
	      // generate a new (reusable) structure.
	      auxData = [];
	      var genNewArray = function (k, l) {
		  // generated an array with null values
		  var arr = []
		  for (var a=0; a<k; a++) {
		      var elem = []
		      for (var b=0; b<l; b++) 
			  elem.push(0);
		      arr.push(0);
		  }
		  return arr;
	      };
	      for(var i =0; i<dims.length -1; i++) {
		  var currDimLen = dimDescr[ dims[i] ].mapLength;
		  var nextDimLen = dimDescr[ dims[i+1] ].mapLength;
		  auxData.push( genNewArray(currDimLen, nextDimLen) )
	      }
	  } else {
	  // re-use the existing data-structure if it exists already
	      for (var a in auxData)
		  for (var b in a)
		      for (c=0; c<b.length; c++)
			  b[c] = 0;
	  }

      };
      var generateLinePattern = function (colFunc) {
	  // find a filtered data-set
	  var filterData = selectVisible(myself.data)

      };
      var drawLinePattern = function (panel, pattern) {
      };
      var colorFuncBg = function() {
	  return "#ddd";
      };


    /*****
     *   Draw the chart and its annotations (except dynamic content)
     *******/
    // Draw the data to the parallel dimensions 
    // (the light grey dataset is a fixed background)
    this.pvParCoord = this.pvPanel.add(pv.Panel)
      .data(myself.data)
      .visible(selectVisible)
      .add(pv.Line)
      .data(dims)
	  .left(function(t, d) { return x(t); } )
      .bottom(function(t, d) { var res = y[t] (d[t]);
			       return res; })
      .strokeStyle("#ddd")
      .lineWidth(1)
      .antialias(false);

    // Rule per dimension.
    rule = this.pvPanel.add(pv.Rule)
      .data(dims)
      .left(x)
      .top(topRuleOffs)
      .bottom(botRuleOffs);

    // Dimension label
    rule.anchor("top").add(pv.Label)
      .top(labelTopOffs)
      .font("bold 10px sans-serif")
      .text(function(d) { return dimDescr[d].name; });


    // add labels on the categorical dimension
    //  compute the array of labels
    var labels = [];
    var labelXoffs = 6,
    labelYoffs = 3;
    for(d in dimDescr) {
      var dim = dimDescr[d];
      if (dim.categorical) {
        var  xVal = x(dim.id) + labelXoffs;
        for (l in dim.map)
          labels[labels.length] = {
            x:  xVal,
            y:  y[dim.id](dim.map[l]) + labelYoffs,
            label: l
          };
      }
    }
    var dimLabels = this.pvPanel.add(pv.Panel)
      .data(labels)
      .add(pv.Label)
      .left(function(d) {return d.x})
      .bottom(function(d) { return d.y})
      .text(function(d) { return d.label})
      .textAlign("left");
    
      
    /*****
     *   Add an additional panel over the top for the dynamic content
     *    (and draw the (full) dataset)
     *******/
    // Draw the selected (changeable) data on a new panel on top
    var change = this.pvPanel.add(pv.Panel);
    var line = change.add(pv.Panel)
      .data(myself.data)
      .visible(selectVisible)
      .add(pv.Line)
      .data(dims)
      .left(function(t, d) { return x(t);})
      .bottom(function(t, d) { return y[t](d[t]); })
      .strokeStyle(function(t, d) { 
        var dd = dimDescr[active];
        var val =  (   dd.orgValue && (dd.categorical == false)) ?
          dd.orgValue[ d[active] ] :
          d[active];
        return colors[active](val);})
      .lineWidth(1);

 

    /*****
     *   Add the user-interaction (mouse-interface)
     *   and the (dynamic) labels of the selection.
     *******/

    // Updater for slider and resizer.
    function update(d) {
      var t = d.dim;
      filter[t].min = Math.max(y[t].domain()[0], y[t].invert(height - d.y - d.dy));
      filter[t].max = Math.min(y[t].domain()[1], y[t].invert(height - d.y));
      active = t;
      change.render();
      return false;
    }

    // Updater for slider and resizer.
    function selectAll(d) {
      if (d.dy < 3) {  // 
        var t = d.dim;
        filter[t].min = Math.max(y[t].domain()[0], y[t].invert(0));
        filter[t].max = Math.min(y[t].domain()[1], y[t].invert(height));
        d.y = botRuleOffs; d.dy = ruleHeight;
        active = t;
        change.render();
      }
      return false;
    }

    // Handle select and drag 
    var handle = change.add(pv.Panel)
      .data(dims.map(function(dim) { return {y:botRuleOffs, dy:ruleHeight, dim:dim}; }))
      .left(function(t) { return x(t.dim) - 30; })
      .width(60)
      .fillStyle("rgba(0,0,0,.001)")
      .cursor("crosshair")
      .event("mousedown", pv.Behavior.select())
      .event("select", update)
      .event("selectend", selectAll)
      .add(pv.Bar)
      .left(25)
      .top(function(d) {return d.y;})
      .width(10)
      .height(function(d) { return d.dy;})
      .fillStyle(function(t) { return  (t.dim == active)
        ? colors[t.dim]((filter[t.dim].max + filter[t.dim].min) / 2)
        : "hsla(0,0,50%,.5)"})
      .strokeStyle("white")
      .cursor("move")
      .event("mousedown", pv.Behavior.drag())
      .event("dragstart", update)
      .event("drag", update);

    handle.anchor("bottom").add(pv.Label)
      .textBaseline("top")
      .text(function(d) { return (dimDescr[d.dim].categorical) ?
                   "" :
                   filter[d.dim].min.toFixed(numDigits) + dimDescr[d.dim].unit;
                 });

    handle.anchor("top").add(pv.Label)
      .textBaseline("bottom")
      .text(function(d) {return (dimDescr[d.dim].categorical) ?
                  "" :
                  filter[d.dim].max.toFixed(numDigits) + dimDescr[d.dim].unit});


    /*****
     *  add the extension points
     *******/

    // Extend ParallelCoordinates
    this.extend(this.pvParCoord,"parCoord_");
    // the parCoord panel is the base-panel (not the colored dynamic overlay)

    // Extend body
    this.extend(this.pvPanel,"chart_");

    return;
  }


});


/**
 * DataTree visualises a data-tree (also called driver tree).
 * It uses a data-sources to obtain the definition of data tree.
 * Each node of the tree can have it's own datasource to visualize the
 * node. 
 */


pvc.DataTree = pvc.Base.extend({

  // the structure of the dataTree is provided by a separate datasource
  structEngine: null,
  structMetadata: null,
  structDataset: null,

  DataTreePanel : null,
  legendSource: "categories",
  tipsySettings: {
    gravity: "s",
    fade: true
  },


  setStructData: function(data){
    this.structDataset = data.resultset;
    if (this.structDataset.length == 0){
      pvc.log("Warning: Structure-dataset is empty")
    }
    this.structMetadata = data.metadata;
    if (this.structMetadata.length == 0){
      pvc.log("Warning: Structure-Metadata is empty")
    }
  },


  constructor: function(o){

    this.base(o);

    var _defaults = {
        // margins around the full tree
      topRuleOffset: 30,  
      botRuleOffset: 30,
      leftRuleOffset: 60,
      rightRuleOffset: 60,
        // box related parameters
      boxplotColor: "grey",
      headerFontsize: 16,
      valueFontsize: 20,
      border:  2,     // bordersize in pixels
      // use perpendicular connector lines  between boxes.
      perpConnector: false, 
      // number of digits (after dot for labels)
      numDigits: 0,
      // the space for the connectors is 15% of the width of a grid cell
      connectorSpace: 0.15,   
      // the vertical space between gridcells is at least 5%
      minVerticalSpace: 0.05,   
      // aspect ratio = width/height  (used to limit AR of the boxes)
      minAspectRatio: 2.0    
    };

    // Apply options
    $.extend(this.options,_defaults, o);

    // Create DataEngine
    this.structEngine = new pvc.DataEngine(this);

    return;
  },

  preRender: function(){

    this.base();

    pvc.log("Prerendering a data-tree");

    // Getting structure-data engine and initialize the translator
    this.structEngine.setData(this.structMetadata,this.structDataset);
    this.structEngine.setCrosstabMode(true);
    this.structEngine.setSeriesInRows(true);
    this.structEngine.createTranslator();
    
    pvc.log(this.structEngine.getInfo());

    this.dataTreePanel = new pvc.DataTreePanel(this, {
      topRuleOffset : this.options.topRuleOffset,
      botRuleOffset : this.options.botRuleOffset,
      leftRuleOffset : this.options.leftRuleOffset,
      rightRuleOffset : this.options.rightRuleOffset,
      boxplotColor:  this.options.boxplotColor,
      valueFontsize: this.options.valueFontsize,
      headerFontsize: this.options.headerFontsize,
      border: this.options.border,
      perpConnector: this.options.perpConnector,
      numDigits: this.options.numDigits,
      minVerticalSpace: this.options.minVerticalSpace,
      connectorSpace: this.options.connectorSpace,
      minAspectRatio: this.options.minAspectRatio
    });

    this.dataTreePanel.appendTo(this.basePanel); // Add it

    return;
  }

}
);


/*
 * DataTree chart panel. 
 *   << to be filled out >>
 *
 * Has the following protovis extension points:
 *
 * <i>chart_</i> - for the main chart Panel
 *    << to be filled out >>
 */


pvc.DataTreePanel = pvc.BasePanel.extend({

  _parent: null,
  pvDataTree: null,

  treeElements: null, 
  structMap: null,
  structArr: null,
  data_: null,

  hRules: null,
  vRules: null,
  rules: null,

  constructor: function(chart, options){

    this.base(chart,options);

  },

  // generating Perpendicular connectors 
  // (only using horizontal and vertical rules)
  // leftLength gives the distance from the left box to the
  // splitting point of the connector
  generatePerpConnectors: function(leftLength) {

    this.hRules = [];
    this.vRules = [];
    this.rules = [];  // also initialize this rule-set

    for(var e in this.structMap) {
      var elem = this.structMap[e];
      if (elem.children != null) {
        var min = +10000, max = -10000;
        var theLeft = elem.left + elem.width;
        this.hRules.push({"left": theLeft,
                    "width": leftLength,
                    "bottom": elem.bottom + elem.height/2});
        theLeft += leftLength;
        for(var i in elem.children) {
          var child = this.structMap[ elem.children[i] ];
          var theBottom = child.bottom + child.height/2;
          if (theBottom > max) max = theBottom;
          if (theBottom < min) min = theBottom;
          this.hRules.push({"left": theLeft,
                      "width": child.left - theLeft,
                      "bottom": theBottom});
        }

        // a vertical rule is only added when needed
        if (max > min)
          this.vRules.push({"left": theLeft,
                      "bottom": min,
                      "height": max - min})
      }
    }
  } ,

  // generate a line segment and add it to rules
  generateLineSegment: function(x1, y1, x2, y2) {
    var line = [];
    line.push({"x":  x1,
               "y":  y1});
    line.push({"x":  x2,
               "y":  y2});
    this.rules.push(line);
  } ,

  // leftLength gives the distance from the left box to the
  // splitting point of the connector
  generateConnectors: function(leftLength) {

    this.hRules = [];
    this.vRules = [];

    if (this.chart.options.perpConnector) {
      this.generatePerpConnectors(leftLength);
      return;
    }

    // this time were using diagonal rules
    this.rules = [];

    for(var e in this.structMap) {
      var elem = this.structMap[e];
      if (elem.children != null) {

        // compute the mid-point
        var min = +10000, max = -10000;
        for(var i in elem.children) {
          var child = this.structMap[ elem.children[i] ];
          var theCenter = child.bottom + child.height/2;
          if (theCenter > max) max = theCenter;
          if (theCenter < min) min = theCenter;
        }
        var mid = (max + min)/2

        var theLeft1 = elem.left + elem.width;
        var theLeft2 = theLeft1 + leftLength;

        // outbound line of the left-hand box
        this.generateLineSegment(theLeft1, elem.bottom + elem.height/2,
                                theLeft2, mid);

        // incoming lines of the right-hand boxes
        for(var i in elem.children) {
          var child = this.structMap[ elem.children[i] ];
          var theCenter = child.bottom + child.height/2;

          this.generateLineSegment(theLeft2, mid,
                                   child.left, theCenter);
        }
      }
    }
    return;
  } ,

  retrieveStructure: function () {
    var de = this.chart.structEngine;
    var opts = this.chart.options;

    var colLabels = de.getVisibleCategories();
    this.treeElements = de.getVisibleSeries();
    var values = de.getValues();

    // if a fifth column is added, then
    //  bottom and height are provided in the dataset.
    var bottomHeightSpecified = (colLabels.length > 4);

    // trim al element labels (to allow for matching without spaces)
    for(var e in this.treeElements) 
      this.treeElements[e] = $.trim(this.treeElements[e]);

    // get the bounds (minimal and maximum column and row indices)
    // first a bounds object with two helper-functions is introduced
    var bounds = [];
    bounds.getElement = function(label) {
      // create the element if it does not exist
      if (bounds[label] == null)
        bounds[label] = {"min": +10000, "max": -10000};
      return bounds[label];
    }
    bounds.addValue = function(label, value) {
      var bnd = bounds.getElement(label);
      if (value < bnd.min)
        bnd.min = value;
      if (value > bnd.max)
        bnd.max = value;
      return bnd;
    }
    for(var e in this.treeElements) {
      var elem = this.treeElements[e];
      var col = elem[0];
      var colnr = col.charCodeAt(0);
      var row = parseInt(elem.slice(1));
      bounds.addValue("__cols", colnr);
      bounds.addValue(col,row);
    }

    // determine parameters to find column-bounds    
    var bnds = bounds.getElement("__cols");
    var gridWidth  = this.innerWidth/(bnds.max - bnds.min + 1); // integer
    var connectorWidth = opts.connectorSpace * gridWidth;
    var cellWidth = gridWidth - connectorWidth;
    var maxCellHeight = cellWidth/opts.minAspectRatio;
    var colBase = bnds.min;
    delete bounds["__cols"];

    // compute additional values for each column
    for (var e in bounds) {
      var bnds = bounds[e];
      if (typeof bnds == "function")
        continue;
      var numRows = bnds.max - bnds.min + 1;

      bnds.gridHeight = this.innerHeight/numRows;
      bnds.cellHeight = bnds.gridHeight*(1.0 - opts.minVerticalSpace);
      if (bnds.cellHeight > maxCellHeight)
        bnds.cellHeight = maxCellHeight;
      bnds.relBottom = (bnds.gridHeight - bnds.cellHeight)/2;
      bnds.numRows = numRows;
    };

    // generate the elements
    var whitespaceQuote = new RegExp ('[\\s\"\']+',"g"); 
    this.structMap = {};
    for(var e in this.treeElements) {
      var box = {};
      var elem = this.treeElements[e];
      box.box_id = elem;
      this.structMap[elem] = box;

      var col = elem[0];
      var colnr = col.charCodeAt(0);
      var row = parseInt(elem.slice(1));
      var bnds = bounds.getElement(col);

      box.colIndex = colnr - colBase;
      box.rowIndex = bnds.numRows - (row - bnds.min) - 1;

      box.left = this.leftOffs + box.colIndex * gridWidth;
      box.width = cellWidth;
      if (bottomHeightSpecified) {
	  box.bottom = values[4][e];
	  box.height = values[5][e];
      } else {
	  box.bottom = this.botOffs + box.rowIndex * bnds.gridHeight
	      + bnds.relBottom;
	  box.height = bnds.cellHeight;
      }
      box.label = values[0][e];
      box.selector = values[1][e];
      box.aggregation = values[2][e];
      var children = values[3][e].replace(whitespaceQuote, " ");
      
      box.children = (children == " " || children ==  "") ?
         null : children.split(" ");
    }

    this.generateConnectors((gridWidth - cellWidth)/2);

    // translate the map to an array (needed by protovis)
    this.structArr = [];
    for(var e in this.structMap) {
      var elem = this.structMap[e];
      this.structArr.push(elem);
    }

    return;
  } ,

  findDataValue: function(key, data) {
    for(var i=0; i < data[0].length; i++)
      if (data[0][ i ] == key)
        return data[1][ i ];

    pvc.log("Error: value with key : "+key+" not found.")
  } ,

  generateBoxPlots: function() {
    var opts = this.chart.options;

    for(var e in this.structArr) {
      var elem = this.structArr[e];
      if (elem.values.length == 0)
        continue;

      elem.subplot = {};
      var sp = elem.subplot;

      // order the data elements from 5% bound to 95% bound
      // and determine the horizontal scale
      var dat = [];
      var margin = 15;
      var rlMargin = elem.width/6;

      // generate empty rule sets (existing sets are overwritten !)
      sp.hRules = [];
      sp.vRules = [];
      sp.marks = [];
      sp.labels = [];

      dat.push(this.findDataValue("_p5", elem.values));
      dat.push(this.findDataValue("_p25", elem.values));
      dat.push(this.findDataValue("_p50", elem.values));
      dat.push(this.findDataValue("_p75", elem.values));
      dat.push(this.findDataValue("_p95", elem.values));

      var noBox = false;

	if (typeof(dat[2]) != "undefined") {
        // switch order (assume computational artifact)
        if (dat[4] < dat[0]) {
          dat = dat.reverse();
          pvc.log(" dataset "+ elem.box_id +
	  	" repaired (_p95 was smaller than _p5)");
          }
        if (dat[4] > dat[0])
          sp.hScale = pv.Scale.linear( dat[0], dat[4]);
        else {
          noBox = true;
          // generate a fake scale centered around dat[0] (== dat[4])
          sp.hScale = pv.Scale.linear( dat[0] - 1e-10, dat[0] + 1e-10);
        }
        sp.hScale.range(elem.left + rlMargin, elem.left + elem.width - rlMargin);
        var avLabel = "" + dat[2];   // prepare the label

        for(var i=0; i< dat.length; i++) dat[i] = sp.hScale( dat[i]) 

        sp.bot = elem.bottom + elem.height / 3,
        sp.top = elem.bottom + 2 * elem.height / 3,
        sp.mid = (sp.top + sp.bot) / 2;   // 2/3 of height
        sp.textBottom = elem.bottom + margin;
        sp.textBottom = sp.bot - opts.valueFontsize - 1;

        // and add the new set of rules for a box-plot.
        var lwa = 3;   // constant for "lineWidth Average"
        if (noBox) {
            sp.vRules.push({"left": dat[0],
                          "bottom": sp.bot,
                          "lWidth": lwa,
                          "height": sp.top - sp.bot});
        } else {
          sp.hRules.push({"left": dat[0],
                        "width":  dat[1] - dat[0],
                        "lWidth": 1,
                        "bottom": sp.mid});
          sp.hRules.push({"left": dat[1],
                        "width":  dat[3] - dat[1],
                        "lWidth": 1,
                        "bottom": sp.bot});
          sp.hRules.push({"left": dat[1],
                        "width":  dat[3] - dat[1],
                        "lWidth": 1,
                        "bottom": sp.top});
          sp.hRules.push({"left": dat[3],
                        "width":  dat[4] - dat[3],
                        "lWidth": 1,
                        "bottom": sp.mid});
          for(var i=0; i<dat.length; i++)
            sp.vRules.push({"left": dat[i],
                          "bottom": sp.bot,
                          "lWidth": (i == 2) ? lwa : 1,
                          "height": sp.top - sp.bot});
        }

        sp.labels.push({left: dat[2],
                      bottom: sp.textBottom,
                      text: this.labelFixedDigits(avLabel),
                      size: opts.smValueFont,
                      color: opts.boxplotColor});
    }
    }
  } ,

  labelFixedDigits: function(value) {
    if (typeof value == "string")
        value = parseFloat(value);

    if (typeof value == "number") {
      var nd = this.chart.options.numDigits;

      value = value.toFixed(nd);
    }

    // translate to a string again
    return "" + value;
  } ,

  addDataPoint: function(key) {
    var opts = this.chart.options;

    for(var e in this.structArr) {
      var elem = this.structArr[e];

      if (elem.values.length == 0)
        continue;
      var value = this.findDataValue(key, elem.values)
      if (typeof value == "undefined")
        continue;

      var sp = elem.subplot;
      var theLeft = sp.hScale(value); 

      var theColor = "green";
      sp.marks.push( {
        left: theLeft,
        bottom: sp.mid,
        color: theColor })
      
      sp.labels.push({left: theLeft,
                      bottom: sp.textBottom,
                      text: this.labelFixedDigits(value),
                      size: opts.valueFont,
                      color: theColor});
    }
    return;
  } , 


  retrieveData: function () {
    var de = this.chart.dataEngine;
    var opts = this.chart.options;

    var colLabels = de.getVisibleCategories();
    var selectors = de.getVisibleSeries();
    var values = de.getValues();
    var selMap = {}
    
    // create empty datasets and selMap
    var numCols = values.length;
    for(var e in this.structArr) {
      var elem = this.structArr[e];
      elem.values = [];
      for(var i=0; i<numCols; i++) elem.values.push([]);
      selMap[ elem.selector ] = elem; 
    }

    // distribute the dataset over the elements based on the selector
    var boxNotFound = {};
    for(var i in selectors) {
      var box = selMap[ selectors[ i ] ];
      if (typeof(box) != "undefined")
        for(var j in values) box.values[j].push(values[ j ][ i ])
      else
        boxNotFound[ selectors[i] ] = true
    }

    for (var sel in boxNotFound)
        pvc.log("Could'nt find box for selector: "+ sel)

    this.generateBoxPlots();

    var whitespaceQuote = new RegExp ('[\\s\"\']+',"g"); 
    var selPar = opts.selectParam.replace(whitespaceQuote, '');
    if (   (selPar != "undefined") 
        && (selPar.length > 0)
        && (typeof window[selPar] != "undefined")) {
      selPar = window[selPar]
      this.addDataPoint(selPar);
    }

    return;
  } ,


  create: function(){

    var myself = this;
    var opts = this.chart.options;

    this.width = this._parent.width;
    this.height = this._parent.height;

    this.pvPanel = this._parent.getPvPanel().add(this.type)
    .width(this.width)
    .height(this.height)


    opts.smValueFontsize = Math.round(0.6 * opts.valueFontsize);
    opts.smValueFont = "" + opts.smValueFontsize + "px sans-serif"
    opts.valueFont = "" + opts.valueFontsize + "px sans-serif";

    // used in the different closures
    var height = this.height,
    topRuleOffs = opts.topRuleOffset,
    botRuleOffs = opts.botRuleOffset,
    leftRuleOffs = opts.leftRuleOffset;

    // set a few parameters which will be used during data-retrieval
    this.innerWidth = this.width - leftRuleOffs - opts.rightRuleOffset;
    this.innerHeight = this.height - topRuleOffs - botRuleOffs;
    this.botOffs = botRuleOffs;
    this.leftOffs = leftRuleOffs;

    // retrieve the data and transform it to the internal representation.
    this.retrieveStructure();

    this.retrieveData();



    /*****
     *   Generate the scales x, y and color
     *******/

/*
pv.Mark.prototype.property("testAdd");
    pv.Mark.prototype.testAdd = function(x) { 
return pv.Label(x);
                      }
*/
    var topMargin = opts.headerFontsize + 3;

    // draw the connectors first (rest has to drawn over the top)
    var rules = this.rules;
    for (var i = 0; i < rules.length; i++) {
      this.pvPanel.add(pv.Line)
        .data(rules[ i ])
        .left(function(d) { return d.x})
        .bottom(function(d) { return d.y})
        .lineWidth(1)
        .strokeStyle("black");
    }
    // draw the data containers with decorations
    this.pvDataTree = this.pvPanel.add(pv.Bar)
      .data(myself.structArr)
      .left(function(d) { return d.left})
      .bottom(function(d) { return d.bottom})
      .height(function(d) { return d.height})
      .width(function(d) { return d.width})
      .fillStyle("green")
//;  this.pvDataTree
    .add(pv.Bar)
//      .data(function(d) {return d; })
      .left(function(d) { return d.left + opts.border})
      .bottom(function(d) { return d.bottom + opts.border})
      .height(function(d) { return d.height - opts.border - topMargin})
      .width(function(d) { return d.width - 2 * opts.border})
      .fillStyle("white")
    .add(pv.Label)
      .text(function(d) { return d.label})
      .textAlign("center")
      .left(function (d) {return  d.left + d.width/2})
      .bottom(function(d) {return d.bottom + d.height 
                - opts.headerFontsize - 5 + opts.headerFontsize/5
})
      .font("" + opts.headerFontsize + "px sans-serif")
      .textStyle("white")
      .fillStyle("blue");

    // add the box-plots
    for(var i=0; i<this.structArr.length; i++) {
      var box = this.structArr[i];
      this.pvPanel.add(pv.Rule)
        .data(box.subplot.hRules)
        .left(function(d) { return d.left})
        .width( function(d) { return d.width})
        .bottom( function(d) { return d.bottom})
        .lineWidth( function(d) { return d.lWidth; })
        .strokeStyle(myself.chart.options.boxplotColor);

      this.pvPanel.add(pv.Rule)
        .data(box.subplot.vRules)
        .left(function(d) { return d.left})
        .height( function(d) { return d.height})
        .bottom( function(d) { return d.bottom})
        .lineWidth( function(d) { return d.lWidth; })
        .strokeStyle(myself.chart.options.boxplotColor);

      this.pvPanel.add(pv.Dot)
        .data(box.subplot.marks)
        .left(function(d) { return d.left })
        .bottom(function(d){ return d.bottom})
        .fillStyle(function(d) {return d.color});


      this.pvPanel.add(pv.Label)
        .data(box.subplot.labels)
        .left(function(d) { return d.left })
        .bottom(function(d){ return d.bottom})
        .font(function(d) { return d.size})
        .text(function(d) { return d.text})
        .textAlign("center")
        .textStyle(function(d) {return d.color});

    }

    // add the connecting rules (perpendicular rules)
    if (opts.perpConnector) {
      this.pvPanel.add(pv.Rule)
        .data(myself.vRules)
        .left(function(d) { return d.left})
        .bottom(function(d) { return d.bottom})
        .height(function(d) { return d.height})
        .strokeStyle("black");
      this.pvPanel.add(pv.Rule)
        .data(myself.hRules)
        .left(function(d) { return d.left})
        .bottom(function(d) { return d.bottom})
        .width(function(d) { return d.width})
        .strokeStyle("black");
    }

    /*****
     *   draw the data-tree
     *******/

    /*****
     *  add the extension points
     *******/

    // Extend the dataTree
    this.extend(this.pvDataTree,"dataTree_");

    // Extend body
    this.extend(this.pvPanel,"chart_");

    return;
  }


});

/**
 * BoxplotChart is the main class for generating... categorical boxplotcharts.
 * 
 * The boxplot is used to represent the distribution of data using:
 *  - a box to represent the region that contains 50% of the datapoints,
 *  - the whiskers to represent the regions that contains 95% of the datapoints, and
 *  - a center line (in the box) that represents the median of the dataset.
 * For more information on boxplots you can visit  http://en.wikipedia.org/wiki/Box_plot
 *
 * If you have an issue or suggestions regarding the ccc BoxPlot-charts
 * please contact CvK at cde@vinzi.nl
 */



pvc.BoxplotChart = pvc.CategoricalAbstract.extend({

    bpChartPanel : null,

    constructor: function(o){

        this.base(o);

        var _defaults = {
            showValues: true,
	    //stacked: false,
	    //            waterfall: false,
            panelSizeRatio: 0.9,
            boxSizeRatio: 0.9,
            maxBarSize: 2000,

            originIsZero: true,
            axisOffset: 0,
            showTooltips: true,
            orientation: "vertical",
	    /* 
            orthoFixedMin: null,
            orthoFixedMax: null */
	    boxplotColor: "darkgreen"  // "grey"
        };

        // Apply options
        $.extend(this.options,_defaults, o);

        //  force stacked to be true (default of base-class is false)
	//        this.options.stacked = true;

        return;
    },


    preRender: function(){

       this.base();

       pvc.log("Prerendering in boxplotChart");


       this.bpChartPanel = new pvc.BoxplotChartPanel(this, {
		//stacked: this.options.stacked,
	    //            waterfall: this.options.waterfall,
            panelSizeRatio: this.options.panelSizeRatio,
            boxSizeRatio: this.options.boxSizeRatio,
            maxBarSize: this.options.maxBarSize,
            showValues: this.options.showValues,
            showTooltips: this.options.showTooltips,
            orientation: this.options.orientation,
	    // boxplot specific options
	    boxplotColor: this.options.boxplotColor
        });

        this.bpChartPanel.appendTo(this.basePanel); // Add it

        return;
    }
}
);


/*
 * Boxplot chart panel generates the actual box-plot with a categorical base-axis.
 * for more information on the options see the documentation file.
 */


pvc.BoxplotChartPanel = pvc.BasePanel.extend({

    _parent: null,
    pvBox: null,
    pvBoxLabel: null,
    /*
    pvWaterfallLine: null,
    pvCategoryPanel: null,
    pvSecondLie: null,
    pvSecondDot: null,
    data: null,
  
    stacked: false,
    */
    panelSizeRatio: 1,
    boxSizeRatio: 0.5,
    boxplotColor: "grey",
    showTooltips: true,
    maxBarSize: 200,
    showValues: true,
    orientation: "vertical",
    tipsySettings: {
        gravity: "s",
        fade: true
    },
    //    ruleData: null,

    hRules: null,
    vRules: null,
    bars: null,


    constructor: function(chart, options){

        this.base(chart,options);

        return;
    },



    getDataSet:  function() {
	    // selection on visibility does not make too much sense here
	    // a box-plot consist of five data-series (and no legend)
        var dataset = this.chart.dataEngine.getVisibleCategoriesIndexes();
        return dataset;
    } ,




    /*
     *   This function implements a number of helper functions in order
     *   to increase the readibily and extendibility of the code by:
     *    1: providing symbolic names (abstractions) to the numerous anonymous
     *        functions that need to be passed to Protovis
     *    2: by moving large parts of the local variabele (parameters
     *       and scaling functions out of the 'create' function to this
     *       prepareDataFunctions blok. 
     *
     *   These helper functions (closures) are all stored in 'this.DF'
     *
     *   Overriding this 'prepareDataFunctions' allows you to implement
     *   a different ScatterScart, however, it is also possible to
     *   replace specific functions from the 'this.DF' object.
     *
     *   Currently I still use a separate chart-type for waterfall/bar plots
     *   and for box-plots.
     */
    prepareDataFunctions:  function() {
        var myself = this;

        // create empty container for the functions and data
        this.DF = {}

        var lScale = this.chart.getLinearScale(true);

        var l2Scale = this.chart.getSecondScale(true);
        var oScale = this.chart.getOrdinalScale(true);
        var bSCale = null;

        // determine barPositionOffset and bScale
        this.DF.maxBarSize = null;
        var barPositionOffset = 0;
	bScale = new pv.Scale.ordinal(
	   this.chart.dataEngine.getVisibleSeriesIndexes())
            .splitBanded(0, oScale.range().band, this.boxSizeRatio);
	// We need to take into account the maxValue if our band 
        // exceeds this value

	this.DF.maxBarSize = bScale.range().band;

        if (this.DF.maxBarSize > this.maxBarSize) {
            barPositionOffset = (this.DF.maxBarSize - this.maxBarSize)/2 ;
            this.DF.maxBarSize = this.maxBarSize;
        }
        // export needed for generated overflow markers.
	//        this.DF.bScale = bScale;


     /*
     * fuctions to determine positions along base axis.
     */
	/*
        this.DF.basePositionFunc = stacked ?
        function(d){
            var res = oScale(this.index) + barPositionOffset;
            // This function used this pointer instead of d !!
            return res
        } :
        null;

        this.DF.baseRulePosFunc = stacked ?
        function(d){
            var res = oScale(d) + barPositionOffset;
            return res
        } :
        null;
	*/

	// find the left side of the container
        this.DF.catContainerBasePosFunc = oScale; 
	/*
	function(d){
            return oScale(this.index);
	    };*/

        this.DF.catContainerWidth = oScale.range().band;

	// find the relative position within this container

        this.DF.relBasePosFunc  = function(d){
            var res = bScale(myself.chart.dataEngine
                .getVisibleSeriesIndexes()[this.index]) + barPositionOffset;
            return res;
        };


        this.DF.secBasePosFunc = 
        function(d){
            if(myself.timeSeries){
                return tScale(parser.parse(d.category));
            }
            else{
                return oScale(d.category) + oScale.range().band/2;
            }
        };

    /*
     * functions to determine positions along orthogonal axis
     */
        this.DF.orthoBotPos = function(d){
            return lScale(pv.min([0,d]));
        };

        this.DF.orthoLengthFunc = function(d){
	    //            var res = myself.chart.animate(0, 
	    //  Math.abs(lScale(d||0) - lScale(0)));
	    res = lScale(d);
            return res;
        };

        this.DF.secOrthoLengthFunc = function(d){
            return myself.chart.animate(0,l2Scale(d.value));
        };


    /*
     * functions to determine the color palette.
     */
        var colors = this.chart.colors(pv.range(this.chart.dataEngine.getSeriesSize()));

	/*  Not used as a box-plot only contains one series??
        // colorFunc is used for the base dataseries
        this.DF.colorFunc = function(d){
            var ind = this.parent.index;
            return colors (myself.chart.dataEngine
                .getVisibleSeriesIndexes()[ind]);
        };
	*/

        // colorFunc2 is used for ....
        this.DF.colorFunc2 = function(d){
            return colors(myself.chart.dataEngine
                .getVisibleSeriesIndexes()[this.index])
        };

        return;
    } ,

  generateBoxPlots: function() {
    var de = this.chart.dataEngine;
    var opts = this.chart.options;
    var colLabels = de.getVisibleCategories();
    var visibleSeries = de.getVisibleSeries();
    var values = de.getValues();

    var lwa = 2;   // lineWidth of average.

    // store the index of the different values
    var median = 0,
    p25 = 1,
    p75 = 2,
    p5 = 3,
    p95 = 4;

    // boxplot covers third of width of container
    var widthBox = this.DF.catContainerWidth/3; 
    // to do: adjust for max-width and minWidth
    var leftOffset = (this.DF.catContainerWidth - widthBox)/2;

    for(var index=0;  index < colLabels.length; index++) {

	// order the data elements from 5% bound to 95% bound
	// and determine the horizontal scale
	var dat = values[index].map(this.DF.orthoLengthFunc);
      
	var leftBox = this.DF.catContainerBasePosFunc(index) + leftOffset,
	    rightBox = leftBox + widthBox,
	    midBox = (leftBox + rightBox)/2;
	
        this.vRules.push({"left": midBox,
		    "height": dat[p25] - dat[p5],
		    "lWidth": 1,
		    "bottom": dat[p5]});
        this.vRules.push({"left": leftBox,
		    "height": dat[p75] - dat[p25],
		    "lWidth": 1,
		    "bottom": dat[p25]});
        this.vRules.push({"left": rightBox,
		    "height": dat[p75] - dat[p25],
		    "lWidth": 1,
		    "bottom": dat[p25]});
        this.vRules.push({"left": midBox,
		    "height": dat[p95] - dat[p75],
		    "lWidth": 1,
		    "bottom": dat[p75]});
        for(var i=0; i<dat.length; i++)
	    this.hRules.push({"left": leftBox,
			"bottom": dat[i],
			"lWidth": (i == median) ? lwa : 1,
			"width": widthBox});

	this.bars.push({"left": leftBox,
		    "bottom": dat[p25],
		    "width": widthBox,
		    "height": dat[p75]-dat[p25],
		    "fillStyle": "limegreen"
	          });
      }

    /*      sp.labels.push({left: dat[2],
                      bottom: sp.textBottom,
                      text: this.labelFixedDigits(avLabel),
                      size: opts.smValueFont,
                      color: opts.boxplotColor});
    */
    //    }
  } ,


  create: function(){
    var myself = this;
    this.width = this._parent.width;
    this.height = this._parent.height;
    
    this.pvPanel = this._parent.getPvPanel().add(this.type)
      .width(this.width)
      .height(this.height)

    this.hRules = [];
    this.vRules = [];
    this.bars = [];


    var anchor = this.orientation == "vertical"?"bottom":"left";

    // prepare data and functions when creating (rendering) the chart.
    this.prepareDataFunctions();

    this.generateBoxPlots();

    var maxBarSize = this.DF.maxBarSize;

    // define a panel for each category label.
    // later the individuals bars of series will be drawn in 
    // these panels.
    this.pvBoxPanel = this.pvPanel.add(pv.Panel);

    // add the box-plots to the chart
      this.pvBoxPanel.add(pv.Bar)
        .data(myself.bars)
        .left(function(d) { return d.left})
        .width( function(d) { return d.width})
        .height( function(d) { return d.height})
        .bottom( function(d) { return d.bottom})
        .fillStyle( function(d) { return d.fillStyle; });

      this.pvBoxPanel.add(pv.Rule)
        .data(myself.hRules)
        .left(function(d) { return d.left})
        .width( function(d) { return d.width})
        .bottom( function(d) { return d.bottom})
        .lineWidth( function(d) { return d.lWidth; })
        .strokeStyle(myself.chart.options.boxplotColor);

      this.pvBoxPanel.add(pv.Rule)
        .data(myself.vRules)
        .left(function(d) { return d.left})
        .height( function(d) { return d.height})
        .bottom( function(d) { return d.bottom})
        .lineWidth( function(d) { return d.lWidth; })
        .strokeStyle(myself.chart.options.boxplotColor);


        if(this.chart.options.secondAxis){
            // Second axis - support for lines
            this.pvSecondLine = this.pvPanel.add(pv.Line)
            .data(function(d){
                return myself.chart.dataEngine.getObjectsForSecondAxis(d, 
                    this.timeSeries ? function(a,b){
                    return parser.parse(a.category) - parser.parse(b.category);
                    }: null)
                })
            .strokeStyle(this.chart.options.secondAxisColor)
            [pvc.BasePanel.relativeAnchor[anchor]](myself.DF.secBasePosFunc)
            [anchor](myself.DF.secOrthoLengthFunc);

            this.pvSecondDot = this.pvSecondLine.add(pv.Dot)
            .shapeSize(8)
            .lineWidth(1.5)
            .fillStyle(this.chart.options.secondAxisColor)
        }

        // add Labels:
        this.pvBoxPanel
        .text(function(d){
            var v = myself.chart.options.valueFormat(d);
            var s = myself.chart.dataEngine
            .getVisibleSeries()[myself.stacked?this.parent.index:this.index]
            var c = myself.chart.dataEngine
            .getVisibleCategories()[myself.stacked?this.index:this.parent.index]
            return myself.chart.options.tooltipFormat.call(myself,s,c,v);
    
        })

        if(this.showTooltips){
            // Extend default
            this.extend(this.tipsySettings,"tooltip_");
            this.pvBoxPanel
            .event("mouseover", pv.Behavior.tipsy(this.tipsySettings));
        }


        if (this.chart.options.clickable){
            this.pvBoxPanel
            .cursor("pointer")
            .event("click",function(d){
                var s = myself.chart.dataEngine
                .getSeries()[myself.stacked?this.parent.index:this.index]
                var c = myself.chart.dataEngine
                .getCategories()[myself.stacked?this.index:this.parent.index]
                return myself.chart.options.clickAction(s,c, d);
            });
        }
    /*  heeft geen data !!
        if(this.showValues){
            this.pvBoxLabel = this.pvBoxPanel
            .anchor("center")
            .add(pv.Label)
            .bottom(0)
            .text(function(d){
                return myself.chart.options.valueFormat(d);
            })
      
            // Extend barLabel
            this.extend(this.pvBoxLabel,"barLabel_");
        }
    */
        // Extend bar and barPanel
        this.extend(this.pvBoxPanel,"boxPanel_");
        this.extend(this.pvBoxPanel,"box_");
    

        // Extend body
        this.extend(this.pvPanel,"chart_");

    }

});
