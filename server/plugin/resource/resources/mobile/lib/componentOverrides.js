SelectComponent = SelectComponent.extend({

  update: function() {
    this.base();
    cdfmobile.refreshSelector(this);
  }
});


