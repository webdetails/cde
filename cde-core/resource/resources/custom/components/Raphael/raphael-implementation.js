var RaphaelComponent = BaseComponent.extend({
  update : function() {
    var myself = this;
    this.customfunction.apply(myself, this.parameters ? this.parameters : []);
  }
});
