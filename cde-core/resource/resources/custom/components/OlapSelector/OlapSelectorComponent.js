var OlapSelectorComponent = BaseComponent.extend({

  init: function() {
    this.model = new OlapSelectorModel({
      olapUtils: new wd.utils.OlapUtils({
          url: Dashboards.getWebAppPath() + wd.helpers.olap.getServiceUrl(),
          catalog: this.catalog,
          cube: this.cube,
          dimension: this.dimensionName
      }),
      title: this.title,
      multiselect: this.multiSelect
    });
    this.view = new OlapSelectorView({
      model: this.model,
      el: $("#" + this.htmlObject).get(0)
    })
  },

  update: function() {
    if(!this.isInitialized) {
      this.init();
      this.isInitialized = true;
    }
    var myself = this;
    this.model.on("change:collapsed",function(m,v){
      if(v){
        Dashboards.processChange(myself.name);
      }
    })
    this.view.render();

    myself.parameters = myself.getParamValues(myself.parameters);
    myself.model.set("parameters", myself.parameters);
  },

  getValue: function() {
    return _(this.model.get("values").where({selected:true})).map(function(m){return m.get("qualifiedName");});
  },


  getParamValues: function(overrides) {
      params = ( overrides instanceof Array) ? Dashboards.propertiesArrayToObject(overrides) : ( overrides || {} );
      paramValues = {};

      _.each( params , function (value, name) {
        value = Dashboards.getParameterValue(value);

        if (_.isObject(value)){
            value = JSON.stringify(value);
        }

        if (typeof value == 'function') {
            value = value();
        }

        paramValues[name] = value;
        paramValues.length = overrides.length;
      });

      return paramValues;
    }
});
