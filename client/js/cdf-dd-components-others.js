var ResultRenderer = ParameterRenderer.extend({});
var PaginationTypeRenderer = SelectRenderer.extend({

		selectData: {
			'two_button':'Two buttons',
			'full_numbers':'Full numbers'
		}
});
var AccessRenderer = SelectRenderer.extend({

		selectData: {
			'public':'Public',
			'private':'Private'
		}
});

var GravityRenderer = SelectRenderer.extend({

		selectData: {
                    'N': 'Top',
                    'S': 'Bottom',
                    'W': 'Left',
                    'E': 'Right'
		}
});

var OutputModeRenderer = SelectRenderer.extend({

		selectData: {
			'include':'Include',
			'exclude':'Exclude'
		}
});

var BandedModeRenderer = SelectRenderer.extend({

		selectData: {
			'compact':'Compact',
			'classic':'Classic'
		}
});
var TableStyleRenderer = SelectRenderer.extend({

		selectData: {
			'themeroller':'New',
			'classic':'Classic'
		}
});

var MenuModeRenderer = SelectRenderer.extend({

		selectData: {
			'horizontal':'Horizontal', 
			'vertical':'Vertical'
		}
});


var AnchorRenderer = SelectRenderer.extend({

		selectData: {
			'top':'Top',
			'bottom':'Bottom',
			'left':'Left',
			'right':'Right'
		}
});

var AnchorCenterRenderer = SelectRenderer.extend({

		selectData: {
			'center':'Center',
      'top':'Top',
			'bottom':'Bottom',
			'left':'Left',
			'right':'Right'
		}
});


var SquareAlignRenderer = SelectRenderer.extend({

		selectData: {
			'left':'Left',
			'center':'Center',
			'right':'Right',
      'top':'Top',
			'middle':'Middle',
      'bottom':'Bottom'
		}
});

var LeftRightRenderer = SelectRenderer.extend({

		selectData: {
			'left':'Left',
			'right':'Right'
		}
});


var TopBottomRenderer = SelectRenderer.extend({

		selectData: {
			'top':'Top',
			'bottom':'Bottom'
		}
});
