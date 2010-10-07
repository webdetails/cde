var ResultRenderer = ParameterRenderer.extend({});
var PaginationTypeRenderer = SelectRenderer.extend({

		translationHash: {
			'two_button':'Two buttons',
			'full_numbers':'Full numbers'
		},

		getData: function(row){
			return " {'two_button':'Two buttons','full_numbers':'Full numbers','selected':'" + (this.getExpression(row)) + "'}";
		},

		getFormattedExpression: function(row, getExpression){
			return this.translationHash[getExpression(row)];
		}
});
var AccessRenderer = SelectRenderer.extend({

		translationHash: {
			'public':'Public',
			'private':'Private'
		},

		getData: function(row){
			return " {'public':'Public','private':'Private','selected':'" + (this.getExpression(row)) + "'}";
		},

		getFormattedExpression: function(row, getExpression){
			return this.translationHash[getExpression(row)];
		}
});

var OutputModeRenderer = SelectRenderer.extend({

		translationHash: {
			'include':'include',
			'exclude':'Exclude'
		},

		getData: function(row){
			return " {'include':'Include','exclude':'Exclude','selected':'" + (this.getExpression(row)) + "'}";
		},

		getFormattedExpression: function(row, getExpression){
			return this.translationHash[getExpression(row)];
		}
});

var BandedModeRenderer = SelectRenderer.extend({

		translationHash: {
			'compact':'Compact',
			'classic':'Classic'
		},

		getData: function(row){
			return " {'compact':'Compact','classic':'Classic','selected':'" + (this.getExpression(row)) + "'}";
		},

		getFormattedExpression: function(row, getExpression){
			return this.translationHash[getExpression(row)];
		}
});
var TableStyleRenderer = SelectRenderer.extend({

		translationHash: {
			'themeroller':'New',
			'classic':'Classic'
		},

		getData: function(row){
			return " {'themeroller':'New','classic':'Classic','selected':'" + (this.getExpression(row)) + "'}";
		},

		getFormattedExpression: function(row, getExpression){
			return this.translationHash[getExpression(row)];
		}
});

var MenuModeRenderer = SelectRenderer.extend({

		translationHash: {
			'horizontal':'Horizontal', 
			'vertical':'Vertical'
		},

		getData: function(row){
			return " {'horizontal':'Horizontal','vertical':'Vertical', 'selected':'" + (this.getExpression(row)) + "'}";
		},

		getFormattedExpression: function(row, getExpression){
			return this.translationHash[getExpression(row)];
		}
});


var AnchorRenderer = SelectRenderer.extend({

		translationHash: {
			'top':'Top',
			'bottom':'Bottom',
			'left':'Left',
			'right':'Right'
		},

		getData: function(row){
			return " {'top':'Top', 'bottom':'Bottom', 'left':'Left', 'right':'Right', 'selected':'" + (this.getExpression(row)) + "'}";
		},

		getFormattedExpression: function(row, getExpression){
			return this.translationHash[getExpression(row)];
		}
});

var AnchorCenterRenderer = SelectRenderer.extend({

		translationHash: {
			'center':'Center',
      'top':'Top',
			'bottom':'Bottom',
			'left':'Left',
			'right':'Right'
		},

		getData: function(row){
			return " {'center':'Center','top':'Top', 'bottom':'Bottom', 'left':'Left', 'right':'Right', 'selected':'" + (this.getExpression(row)) + "'}";
		},

		getFormattedExpression: function(row, getExpression){
			return this.translationHash[getExpression(row)];
		}
});


var SquareAlignRenderer = SelectRenderer.extend({

		translationHash: {
			'left':'Left',
			'center':'Center',
			'right':'Right',
      'top':'Top',
			'middle':'Middle',
      'bottom':'Bottom'
		},

		getData: function(row){
			return " { 'left':'Left', 'center':'Center','right':'Right','top':'Top','middle':'Middle','bottom':'Bottom', 'selected':'" + (this.getExpression(row)) + "'}";
		},

		getFormattedExpression: function(row, getExpression){
			return this.translationHash[getExpression(row)];
		}
});

var LeftRightRenderer = SelectRenderer.extend({

		translationHash: {
			'left':'Left',
			'right':'Right'
		},

		getData: function(row){
			return " {'left':'Left', 'right':'Right', 'selected':'" + (this.getExpression(row)) + "'}";
		},

		getFormattedExpression: function(row, getExpression){
			return this.translationHash[getExpression(row)];
		}
});


var TopBottomRenderer = SelectRenderer.extend({

		translationHash: {
			'top':'Top',
			'bottom':'Bottom'
		},

		getData: function(row){
			return " {'top':'Top', 'bottom':'Bottom', 'selected':'" + (this.getExpression(row)) + "'}";
		},

		getFormattedExpression: function(row, getExpression){
			return this.translationHash[getExpression(row)];
		}
});