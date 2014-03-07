
var NewSelectorComponent = UnmanagedComponent.extend({
  
  pageStart: 0,
  pageSize: 54,    

  update: function() {
    $.extend(this.options,this);
    this.ph = $("#" + this.htmlObject).empty();
    var redraw = _.bind(this.redraw,this);
    if (typeof this.valuesArray != "undefined" && this.valuesArray.length > 0) {
      this.synchronous(redraw,this.valuesArray);
    } else {
      var params = Dashboards.propertiesArrayToObject( this.parameters );
      var pattern = (this.selectorModel) ?
          this.selectorModel.get("searchterm"):
          "";
      params[this.searchParam] = "'" + pattern + "'";
      this.parameters = Dashboards.objectToPropertiesArray(params);

      this.triggerQuery(this.chartDefinition,redraw,{
        pageSize: this.pageSize
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
      this.selectorModel = new models.pagingSelector.SelectorModel(modelOptions);
    } else {
      this.selectorModel.set(modelOptions);
    }
    this.selectorModel.updateValues(values);
    if(!this.selectorView) {
      this.selectorView = new views.pagingSelector.SelectorView({
        model: this.selectorModel,
        el: this.ph.get(0)
      });
    }
    this.selectorView.render();
    /* Listen to all the stuff we need to. We first need to make sure to clear
     * out the old bindings, so as not to leak memory.
     */
    this.selectorModel.off('change:searchterm', this.update);
    this.selectorModel.on('change:searchterm', this.update, this);
    this.selectorModel.off('change:pageSize', this.pagingHandler);
    this.selectorModel.on('change:pageSize', this.pagingHandler, this);
    this.selectorModel.off('change:pageStart', this.pagingHandler);
    this.selectorModel.on('change:pageStart', this.pagingHandler, this);
    /* We trigger a change on the parameter if the user just collapsed the
     * selector, or if unselecting values whlie the selector's collapsed*/
    this.selectorModel.off("change:collapsed", this.handleCollapse);
    this.selectorModel.on("change:collapsed", this.handleCollapse, this);

    this.timeout = 0;
    var values = this.selectorModel.get("values");
    values.off("change:selected", this.handleSelectionChange);
    values.on("change:selected", this.handleSelectionChange,this);
  },

  handleCollapse: function(evt){
    if(evt.changed.collapsed) Dashboards.processChange(this.name);
  },

  handleSelectionChange: function(evt){
    if(!evt.changed.selected && this.selectorModel.get("collapsed")) {
      /* Wrap the processChange in a setTimeout so that consecutive
       * selection removals only trigger a single parameter change
       */
      if(this.timeout !== 0) {
        clearTimeout(this.timetimeout);
      };
      var myself = this;
      this.timeout = setTimeout(function(){
        Dashboards.processChange(myself.name);
        timeout = 0;
      }, 1500);
    }
  },

  pagingHandler: function() {
    var redraw = this.getSuccessHandler(_.bind(function(data){
      var values = this.values(data);
      this.selectorModel.addPage(values);
    },this));
    this.queryState.pageStartingAt(this.selectorModel.get("pageStart"), redraw);
  },

  getValue: function() {
    return this.selectorModel.selectedValues();
  }
}); 
