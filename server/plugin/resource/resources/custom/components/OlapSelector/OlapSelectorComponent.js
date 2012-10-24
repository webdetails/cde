
var OlapSelectorComponent = BaseComponent.extend({

  init: function() {
    this.model = new OlapSelectorModel({
      olapUtils: new wd.utils.OlapUtils({
          url: "/pentaho/content/pentaho-cdf-dd/OlapUtils",
          catalog: this.catalog,
          cube: this.cube,
          dimension: this.dimensionName,
      }),
      title: this.title
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
  },

  getValue: function() {
    return _(this.model.get("values").where({selected:true})).map(function(m){return m.get("qualifiedName");});
  }
});
