var ProtovisComponent = BaseComponent.extend({
		update : function() {
			if (this.parameters == undefined) {
				this.parameters = [];
			};

			// clear previous table
			$("#"+this.htmlObject).empty();
			var myself = this;
			Dashboards.fetchData(this.chartDefinition, this.parameters,
                            function(values) {
                                changedData = undefined;
                                if((typeof(myself.postFetch)=='function')){
                                    changedData = myself.postFetch(values);
                                };
                                if (changedData != undefined) {
                                  values = changedData;
                                };
                                myself.render(values);
                            });
		},

		render: function(values) {
			$("#" + this.htmlObject).append('<div id="'+ this.htmlObject  +'protovis"></div>');
			var vis = new pv.Panel()
				.canvas(this.htmlObject + "protovis")
				.width(this.width)
				.height(this.height);
			this.customfunction(vis,values);
			vis.root.render();
			//vis.canvas(this.htmlObject + "image");
			//if (this.caption != undefined)
		        //this.buildCaptionWrapper($("#" + this.htmlObject + "protovis"),"");
		},

		processdata: function(values) {
			this.render(values);
		}
});
	
