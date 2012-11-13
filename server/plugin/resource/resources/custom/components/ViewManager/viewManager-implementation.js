var viewManagerComponent = BaseComponent.extend({
  update: function() {
    function fetchData() {
      $.ajax({
        url: webAppPath + "/content/pentaho-cdf/Views",
        data: {method: "listViews"},
        dataType: 'json',
        success: function(response){
          myself.model.initViews(response.views);
          myself.view.render();
        },
        cache: false
      });
    };
    this.ph = $("#" + this.htmlObject);
    this.model = new wd.cdf.views.ViewManager();
    this.view = new wd.cdf.views.ViewManagerView({  
      el: this.ph.get(0),
      model: this.model
    });
    var myself = this;
    fetchData();
    this.model.on("update",fetchData,this);
  }
});
