
var pvc = {}

/**
 *
 *  Utility function for logging messages to the console
 *
 */

pvc.log = function(m){

    if (typeof console != "undefined"){
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
})(jQuery);/**
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

        // If we don't have data, we just need to set a "no data" message
        // and go on with life.
        if (this.resultset.length === 0) {
            throw new NoDataException();
        }

        // Disable animation if browser doesn't support it
        if(!$.support.svg){
            this.options.animate = false;
        }

        // Getting data engine and initialize the translator
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
    $(".tipsy").remove();

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

    pvc.log("Prerendering in TimeseriesAbstract");


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
            secondAxisSize: 0 // calculated

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
                elements: this.getAxisOrdinalElements()
            });

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
                elements: this.getAxisOrdinalElements()
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
                elements: this.getAxisOrdinalElements(),
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
                elements: this.getAxisOrdinalElements(),
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
        return this.options.orientation == "vertical" && !this.options.timeSeries;
    },


    /*
     * Indicates if yy is an ordinal scale
     */

    isYAxisOrdinal: function(){
        return this.options.orientation == "horizontal" && !this.options.timeSeries;
    },

    /*
     *  List of elements to use in the axis ordinal
     *
     */
    getAxisOrdinalElements: function(){
        return this.dataEngine.getCategories();
    },



    /*
     * xx scale for categorical charts
     */

    getXScale: function(){

        return this.options.orientation == "vertical"?
        (this.options.timeSeries?this.getTimeseriesScale():this.getOrdinalScale()):
        this.getLinearScale();

    },

    /*
     * yy scale for categorical charts
     */

    getYScale: function(){

        return this.options.orientation == "vertical"?
        this.getLinearScale():
        (this.options.timeSeries?this.getTimeseriesScale():this.getOrdinalScale());
    },

    /*
     * Scale for the ordinal axis. xx if orientation is vertical, yy otherwise
     *
     */
    getOrdinalScale: function(bypassAxis){

        var yAxisSize = bypassAxis?0:this.options.yAxisSize;
        var xAxisSize = bypassAxis?0:this.options.xAxisSize;

        var secondXAxisSize = 0, secondYAxisSize = 0;
        
        if( this.options.orientation == "vertical"){
            secondYAxisSize = bypassAxis?0:this.options.secondAxisSize;
        }
        else{
            secondXAxisSize = bypassAxis?0:this.options.secondAxisSize;
        }

        var scale = new pv.Scale.ordinal(this.dataEngine.getVisibleCategories());

        var size = this.options.orientation=="vertical"?this.basePanel.width:this.basePanel.height;

        if(this.options.orientation=="vertical" && this.options.yAxisPosition == "left"){
            scale.min = yAxisSize;
            scale.max = size - secondYAxisSize;
        }
        else if(this.options.orientation=="vertical" && this.options.yAxisPosition == "right"){
            scale.min = secondYAxisSize;
            scale.max = size-yAxisSize;
        }
        else{
            scale.min = secondYAxisSize;
            scale.max = size - xAxisSize - secondXAxisSize;
        }
        scale.splitBanded( scale.min, scale.max, this.options.panelSizeRatio);
        return scale;



    },

    /*
     * Scale for the linear axis. yy if orientation is vertical, xx otherwise
     *
     */
    getLinearScale: function(bypassAxis){

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
        if(min > 0 && this.options.originIsZero){
            min = 0
        }

        // Adding a small offset to the scale:
        var offset = (max - min) * this.options.axisOffset;
        var scale = new pv.Scale.linear(min - offset,max + offset)


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
    getTimeseriesScale: function(bypassAxis){

        var yAxisSize = bypassAxis?0:this.options.yAxisSize;
        var xAxisSize = bypassAxis?0:this.options.xAxisSize;
        var secondAxisSize = bypassAxis?0:this.options.secondAxisSize;

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

        if(min > 0 && this.options.secondAxisOriginIsZero){
            min = 0
        }

        // Adding a small offset to the scale:
        var offset = (max - min) * this.options.secondAxisOffset;
        var scale = new pv.Scale.linear(min - offset,max + offset)


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
    pvLabel: null,
    pvRuleGrid: null,

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
        this.extend(this.pvLabel, this.panelName + "Label_");
        this.extend(this.pvRuleGrid, this.panelName + "Grid_");

    },


    setScale: function(scale){
        this.scale = scale;
    },

    renderAxis: function(){

        var scaleRange = this.scale.range();
        this.pvRule = this.pvPanel
        .add(pv.Rule)
        .strokeStyle(this.tickColor)
        [pvc.BasePanel.oppositeAnchor[this.anchor]](0)
        [pvc.BasePanel.relativeAnchor[this.anchor]](this.scale.min)
        [pvc.BasePanel.paralelLength[this.anchor]](this.scale.max - this.scale.min)

        if (this.ordinal == true){
            this.renderOrdinalAxis();
        }
        else{
            this.renderLinearAxis();
      
        }
    
    },
  

    renderOrdinalAxis: function(){

        var myself = this;
    
        this.pvLabel = this.pvRule.add(pv.Label)
        .data(this.elements)
        [pvc.BasePanel.paralelLength[this.anchor]](null)
        [pvc.BasePanel.oppositeAnchor[this.anchor]](10)
        [pvc.BasePanel.relativeAnchor[this.anchor]](function(d){
            return myself.scale(d) + myself.scale.range().band/2;
        })
        .textAlign("center")
        .textBaseline("middle")
        .text(pv.identity)
        .font("9px sans-serif")
    },


    renderLinearAxis: function(){

        var myself = this;
    
        var scale = this.scale;

        this.pvLabel = this.pvRule.add(pv.Rule)
        .data(this.scale.ticks())
        [pvc.BasePanel.paralelLength[this.anchor]](null)
        [pvc.BasePanel.oppositeAnchor[this.anchor]](0)
        [pvc.BasePanel.relativeAnchor[this.anchor]](this.scale)
        [pvc.BasePanel.orthogonalLength[this.anchor]](function(d){
            return myself.tickLength/(this.index%2 + 1)
        })
        .strokeStyle(this.tickColor)
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

  constructor: function(o){

    this.base(o);

    var _defaults = {
      showValues: true,
      innerGap: 0.9,
      explodedSliceRadius: 0,
      explodedSliceIndex: null,
      showTooltips: true,
      tooltipFormat: function(s,c,v){
        return c+":  " + v + " (" + Math.round(v/this.sum*100,1) + "%)";
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
      // return colors(d.serieIndex)
      return colors(myself.chart.dataEngine.getVisibleCategoriesIndexes()[this.index])
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
      var v = myself.chart.options.valueFormat(d);
      var s = myself.chart.dataEngine.getVisibleSeries()[this.parent.index]
      var c = myself.chart.dataEngine.getVisibleCategories()[this.index]
      return myself.chart.options.tooltipFormat.call(myself,s,c,v);
    })

    if(this.showTooltips){
      this.pvPie
      .event("mouseover", pv.Behavior.tipsy({
        gravity: "s",
        fade: true
      }));

    }


    if (this.chart.options.clickable){
      this.pvPie
      .cursor("pointer")
      .event("click",function(d){
        var s = myself.chart.dataEngine.getSeries()[this.parent.index]
        var c = myself.chart.dataEngine.getCategories()[this.index]
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

});/**
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
            originIsZero: true,
            axisOffset: 0,
            showTooltips: true,
            orientation: "vertical"
        };


        // Apply options
        $.extend(this.options,_defaults, o);


    },

    preRender: function(){

        this.base();

        pvc.log("Prerendering in barChart");


        this.barChartPanel = new pvc.BarChartPanel(this, {
            stacked: this.options.stacked,
            panelSizeRatio: this.options.panelSizeRatio,
            barSizeRatio: this.options.barSizeRatio,
            maxBarSize: this.options.maxBarSize,
            showValues: this.options.showValues,
            showTooltips: this.options.showTooltips,
            orientation: this.options.orientation
        });

        this.barChartPanel.appendTo(this.basePanel); // Add it

    }

}
);


/*
 * Bar chart panel. Generates a bar chart. Specific options are:
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


pvc.BarChartPanel = pvc.BasePanel.extend({

    _parent: null,
    pvBar: null,
    pvBarLabel: null,
    pvCategoryPanel: null,
    pvSecondLie: null,
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

    constructor: function(chart, options){

        this.base(chart,options);

    },

    create: function(){

        var myself = this;
        this.width = this._parent.width;
        this.height = this._parent.height;

        this.pvPanel = this._parent.getPvPanel().add(this.type)
        .width(this.width)
        .height(this.height)

        var anchor = this.orientation == "vertical"?"bottom":"left";

        // Extend body, resetting axisSizes

        var lScale = this.chart.getLinearScale(true);
        var l2Scale = this.chart.getSecondScale(true);
        var oScale = this.chart.getOrdinalScale(true);

        var colors = this.chart.colors(pv.range(this.chart.dataEngine.getSeriesSize()));
        var colorFunc = function(d){
            return colors(myself.chart.dataEngine.getVisibleSeriesIndexes()[this.parent.index])
        };
        var colorFunc2 = function(d){
            return colors(myself.chart.dataEngine.getVisibleSeriesIndexes()[this.index])
        };
        var maxBarSize;


        // Stacked?
        if (this.stacked){


            maxBarSize = oScale.range().band;
            var bScale = new pv.Scale.ordinal([0])
            .splitBanded(0, oScale.range().band, this.barSizeRatio);


            var barPositionOffset = 0;
            if (maxBarSize > this.maxBarSize){
                barPositionOffset = (maxBarSize - this.maxBarSize)/2 ;
                maxBarSize = this.maxBarSize;
            }
      
     
            this.pvBarPanel = this.pvPanel.add(pv.Layout.Stack)
            /*
      .orient(anchor)
      .order("inside-out")
      .offset("wiggle")*/
            .layers(pvc.padMatrixWithZeros(this.chart.dataEngine.getVisibleTransposedValues()))
            [this.orientation == "vertical"?"y":"x"](function(d){
                return myself.chart.animate(0, lScale(d||0)-lScale(0))
            })
            [anchor](lScale(0))
            [this.orientation == "vertical"?"x":"y"](function(d){
                return oScale(this.index) + barPositionOffset;
            });
            this.pvBar = this.pvBarPanel.layer.add(pv.Bar)
            .data(function(d){
                return d
            })
            //[anchor](lScale(0))
            [pvc.BasePanel.paralelLength[anchor]](maxBarSize)
            .fillStyle(colorFunc)

        /*[pvc.BasePanel.relativeAnchor[anchor]](function(d){
        return this.parent.left() + barPositionOffset
      })*/;

        }
        else{

            var bScale = new pv.Scale.ordinal(this.chart.dataEngine.getVisibleSeriesIndexes())
            .splitBanded(0, oScale.range().band, this.barSizeRatio);

            // We need to take into account the maxValue if our band is higher than that
            maxBarSize = bScale.range().band;
            var barPositionOffset = 0;
            if (maxBarSize > this.maxBarSize){
                barPositionOffset = (maxBarSize - this.maxBarSize)/2 ;
                maxBarSize = this.maxBarSize;
            }

            this.pvBarPanel = this.pvPanel.add(pv.Panel)
            .data(this.chart.dataEngine.getVisibleCategoriesIndexes())
            [pvc.BasePanel.relativeAnchor[anchor]](function(d){
                return oScale(this.index);
            })
            [anchor](0)
            [pvc.BasePanel.paralelLength[anchor]](oScale.range().band)
            [pvc.BasePanel.orthogonalLength[anchor]](this[pvc.BasePanel.orthogonalLength[anchor]])


            this.pvBar = this.pvBarPanel.add(pv.Bar)
            .data(function(d){
                return myself.chart.dataEngine.getVisibleValuesForCategoryIndex(d)
                })
            .fillStyle(colorFunc2)
            [pvc.BasePanel.relativeAnchor[anchor]](function(d){
                return bScale(myself.chart.dataEngine.getVisibleSeriesIndexes()[this.index]) + barPositionOffset;
            })
            [anchor](function(d){
                return lScale(pv.min([0,d]))
            })
            [pvc.BasePanel.orthogonalLength[anchor]](function(d){
                return myself.chart.animate(0, Math.abs(lScale(d||0) - lScale(0)))
            })
            [pvc.BasePanel.paralelLength[anchor]](maxBarSize)

        }


        if(this.chart.options.secondAxis && this.chart.options.secondAxisIndependentScale){
            // Second axis - support for lines
            this.pvSecondLine = this.pvPanel.add(pv.Line)
            .data(function(d){
                return myself.chart.dataEngine.getObjectsForSecondAxis(d, this.timeSeries?function(a,b){
                    return parser.parse(a.category) - parser.parse(b.category);
                    }: null)
                })
            .strokeStyle(this.chart.options.secondAxisColor)
            [pvc.BasePanel.relativeAnchor[anchor]](function(d){
                if(myself.timeSeries){
                    return tScale(parser.parse(d.category));
                }
                else{
                    return oScale(d.category) + oScale.range().band/2;
                }
            })
            [anchor](function(d){
                return myself.chart.animate(0,l2Scale(d.value));
            })

            this.pvSecondDot = this.pvSecondLine.add(pv.Dot)
            .shapeSize(8)
            .lineWidth(1.5)
            .fillStyle(this.chart.options.secondAxisColor)
        }

        // Labels:

        this.pvBar
        .text(function(d){
            var v = myself.chart.options.valueFormat(d);
            var s = myself.chart.dataEngine.getVisibleSeries()[myself.stacked?this.parent.index:this.index]
            var c = myself.chart.dataEngine.getVisibleCategories()[myself.stacked?this.index:this.parent.index]
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
                var s = myself.chart.dataEngine.getSeries()[myself.stacked?this.parent.index:this.index]
                var c = myself.chart.dataEngine.getCategories()[myself.stacked?this.index:this.parent.index]
                return myself.chart.options.clickAction(s,c, d);
            });
        }

        if(this.showValues){
            this.pvBarLabel = this.pvBar
            .anchor("center")
            .add(pv.Label)
            .bottom(0)
            .text(pv.identity)

            // Extend barLabel
            this.extend(this.pvBarLabel,"barLabel_");
        }


        // Extend bar and barPanel
        this.extend(this.pvBar,"barPanel_");
        this.extend(this.pvBar,"bar_");
    

        // Extend body
        this.extend(this.pvPanel,"chart_");

    }

});


/**
 * ScatterAbstract is the class that will be extended by dot, line, stackedline and area charts.
 */

pvc.ScatterAbstract = pvc.CategoricalAbstract.extend({

  scatterChartPanel : null,

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
      panelSizeRatio: 1
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
      tScale = this.chart.getTimeseriesScale(true);
    }
    
    var parser = pv.Format.date(this.timeSeriesFormat);
    
    var maxLineSize;

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
      this.pvLine
      .event("point", pv.Behavior.tipsy({
        gravity: "s",
        fade: true
      }));
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
        return this.series || this.translator.getColumns();
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

        var myself=this;
        return pv.range(this.getSeries().length).filter(function(v){
            return !myself.hiddenData.series[v];
        });
    },

    /*
   * Returns an array with the visible categories. Use only when index information
   * is not required
   *
   */
    getVisibleSeries: function(){

        var myself = this;
        return this.getVisibleSeriesIndexes().map(function(idx){
            return myself.getSerieByIndex(idx);
        })
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

        var myself=this;
        return pv.range(this.getCategories().length).filter(function(v){
            return !myself.hiddenData.categories[v];
        });
    },

    /*
   * Returns an array with the visible categories. Use only when index information
   * is not required
   *
   */
    getVisibleCategories: function(){
  
        var myself = this;
        return this.getVisibleCategoriesIndexes().map(function(idx){
            return myself.getCategoryByIndex(idx);
        })
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
            if(typeof v != "undefined" && v != null){
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
        return this.getVisibleSeriesIndexes().map(function(sIdx){
            return myself.getValuesForSeriesIndex(sIdx)
        })

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
            if(typeof a[idx] != "undefined" && a[idx] != null){
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
        return this.getVisibleSeriesIndexes().map(function(idx){
            return cats[idx]
        })
    },


    /*
   * Returns the object for a given category idx in the form {serie: value}
   *
   */

    getObjectsForCategoryIndex: function(idx){

        var myself = this;
        var ar=[];
        this.getValues()[idx].map(function(a,i){
            if(typeof a != "undefined" && a!= null){
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
    },

    resetDataCache: function(){
        this.series = null;
        this.categories = null;
        this.values = null;
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

        var tree = pv.tree(this.resultset).keys(function(d){
            return [d[0],d[1]]
        }).map();

        // Now, get series and categories:
        var numeratedSeries = pv.numerate(pv.keys(tree));
        var numeratedCategories = pv.numerate(pv.uniq(pv.blend(pv.values(tree).map(function(d){
            return pv.keys(d)
        }))))

        // Finally, itetate through the resultset and build the new values

        this.values = [];
        var categoriesLength = pv.keys(numeratedCategories).length;
        var seriesLength = pv.keys(numeratedSeries).length;

        // Initialize array
        pv.range(0,categoriesLength).map(function(d){
            myself.values[d] = new Array(seriesLength + 1);
            myself.values[d][0] = pv.keys(numeratedCategories)[d]
        })

        this.resultset.map(function(l){

            myself.values[numeratedCategories[l[1]]][numeratedSeries[l[0]] + 1] =
            pvc.sumOrSet(myself.values[numeratedCategories[l[1]]][numeratedSeries[l[0]]+1], l[2]);
        })

        // Create an inicial line with the categories
        var l1 = pv.keys(numeratedSeries);
        l1.splice(0,0,"x");
        this.values.splice(0,0, l1)


    }


});

NoDataException = function() {};