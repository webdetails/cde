var BogusComponent = BaseComponent.extend({
		update : function() {
			if (this.parameters == undefined) {
				this.parameters = [];
			}

			// clear previous table
			$("#"+this.htmlObject).empty();
			var myself = this;
                        this.customfunction(this.parameters);
                        }
});
	
