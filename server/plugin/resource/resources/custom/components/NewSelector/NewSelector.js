
var NewSelectorComponent = BaseComponent.extend({
  
  pageStart: 0,
  pageSize: 54,    

  update: function() {
    $.extend(this.options,this);
    this.ph = $("#" + this.htmlObject).empty();
    var redraw = _.bind(this.redraw,this);
    if (typeof this.valuesArray != "undefined" && this.valuesArray.length > 0) {  
      this.synchronous(redraw,this.valuesArray);
    } else {
      this.triggerQuery(this.chartDefinition,redraw,{
        pageSize: this.pageSize
      });
    }
  }
  xupdate: function(){
    var myself = this;
    $.extend(this.options,this);
    myself.ph = $("#" + myself.htmlObject).empty();
      
    if (typeof this.valuesArray != "undefined" && myself.valuesArray.length > 0 ) {
      myself.redraw(myself.valuesArray);
    } else {
      var croppedCd = $.extend({}, myself.chartDefinition),params;
      delete croppedCd.drawCallback;
      myself.queryState = new Query(croppedCd); 
      params = this.parameters.slice();
      if(this.selectorModel){
        this.selectorModel.set({pageStart:0, pageSize:this.pageSize, pageStep: this.pageStep, multiselect: this.multiselect},{silent:true});
        if(this.selectorModel.get("searchterm")) {
          params.push([this.searchParam, "'" + this.selectorModel.get("searchterm") +"'"]);
        }
      }
      myself.queryState.setPageSize(this.pageSize);
      myself.queryState.fetchData(params, function(data){
        var changedValues = undefined;
        if((typeof(myself.postFetch) == 'function')){
          changedValues = myself.postFetch(data);
        }
        if (changedValues != undefined) {
          data = changedValues;
        }
        myself.redraw(data);
      });
    } 
  },

  values: function(results) {

    var data = results.resultset,
        idx = (results.queryInfo) ? results.queryInfo.pageStart : 0,
        currentValues = Dashboards.getParameterValue(this.parameter),
        vid = this.chartDefinition.valueAsId,
        values = [];
    if(!_.isArray(currentValues)) currentValues = [currentValues];
    _.each(data,function(row){
      var value = row[vid ? 1 : 0],
          v = {
            idx: idx++,
            value: value,
            label: row[1],
            selected: !!(1 + currentValues.indexOf(value)),
            "new": !!row[2]
          };
      values.push(v);
    },this);
    return values;
  },

  redraw: function(data) {

    var values = this.values(data),
        modelOptions = {
          title: this.title,
          pageSize: this.pageSize,
          pageStart: this.pageStart,
          totalRecords: (data.queryInfo) ? data.queryInfo.totalRows : 0,
          multiselect: this.multiselect 
        },
        v, p;

    /* Filter parameter to remove unavailable values */
    v = _.pluck(values,"value"),
    p = Dashboards.getParameterValue(this.parameter);
    p = _.filter(p,function(val){
      return _.include(v,val);
    });
    Dashboards.setParameter(this.parameter, p);
    /* Initialize model and view, if needed */
    if(!this.selectorModel) {
      this.selectorModel = new SelectorModel(modelOptions);
    } else {
      this.selectorModel.set(modelOptions);
    }
    this.selectorModel.updateValues(values);
    if(!this.selectorView) {
      this.selectorView = new SelectorView({
        model: this.selectorModel,
        el: this.ph.get(0)
      });
    }
    this.selectorView.render();
    /* Listen to all the stuff we need to */
    this.selectorModel.on('change:searchterm', this.update, this);
    this.selectorModel.on('change:pageSize', this.pagingHandler, this);
    this.selectorModel.on('change:pageStart', this.pagingHandler, this);
    /* We trigger a change on the parameter if the user just collapsed the
     * selector, or if unselecting values whlie the selector's collapsed*/
    this.selectorModel.on("change:collapsed",function(evt){
      if(evt.changed.collapsed) Dashboards.processChange(this.name);
    },this);

    var timeout = 0;
    this.selectorModel.get("values").on("change:selected",function(evt){
      if(!evt.changed.selected && this.selectorModel.get("collapsed")) {
        /* Wrap the processChange in a setTimeout so that consecutive
         * selection removals only trigger a single parameter change
         */
        if(timeout !== 0) clearTimeout(timeout);
        var myself = this;
        timeout = setTimeout(function(){
          Dashboards.processChange(myself.name);
          timeout = 0;
        }, 1500);
      }
    },this);
  },

  pagingHandler: function() {
    var redraw = this.getSuccessHandler(_.bind(function(data){
      var values = this.values(data);
      this.model.addPage(values);
    },this));
    this.queryState.pageStartingAt(this.model.get("pageStart"), redraw);
  },

  getValue: function() {
    return this.selectorModel.selectedValues();
  }
}); 
