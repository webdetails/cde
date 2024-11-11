/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


var ResultRenderer = ParameterRenderer.extend({});

var PaginationTypeRenderer = SelectRenderer.extend({

  selectData: {
    'simple':         'Simple',
    'simple_numbers': 'Simple Numbers',
    'full':           'Full',
    'full_numbers':   'Full Numbers',
    'two_button':     'Two buttons'
  }

});
var AccessRenderer = SelectRenderer.extend({

  selectData: {
    'public':  'Public',
    'private': 'Private'
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
    'include': 'Include',
    'exclude': 'Exclude'
  }
});

var BandedModeRenderer = SelectRenderer.extend({

  selectData: {
    'compact': 'Compact',
    'classic': 'Classic'
  }
});
var TableStyleRenderer = SelectRenderer.extend({

  selectData: {
    'themeroller': 'New',
    'classic':     'Classic',
    'bootstrap':   'Bootstrap'
  }
});

var MenuModeRenderer = SelectRenderer.extend({

  selectData: {
    'horizontal': 'Horizontal',
    'vertical':   'Vertical'
  }
});


var AnchorRenderer = SelectRenderer.extend({

  selectData: {
    'top':    'Top',
    'bottom': 'Bottom',
    'left':   'Left',
    'right':  'Right'
  }
});

var AnchorCenterRenderer = SelectRenderer.extend({

  selectData: {
    'center': 'Center',
    'top':    'Top',
    'bottom': 'Bottom',
    'left':   'Left',
    'right':  'Right'
  }
});


var SquareAlignRenderer = SelectRenderer.extend({

  selectData: {
    'left':   'Left',
    'center': 'Center',
    'right':  'Right',
    'top':    'Top',
    'middle': 'Middle',
    'bottom': 'Bottom'
  }
});

var LeftRightRenderer = SelectRenderer.extend({

  selectData: {
    'left':  'Left',
    'right': 'Right'
  }
});

var TopBottomRenderer = SelectRenderer.extend({

  selectData: {
    'top':    'Top',
    'bottom': 'Bottom'
  }
});

var WindowModeRenderer = SelectRenderer.extend({

    selectData: {
        'TIME_BASED': 'Time Based',
        'ROW_BASED': 'Row Based'
    }
});

var DataServiceNameRenderer = SelectRenderer.extend({
  selectData: {},
  streaming: false,

  parseXml: function(xml) {
    var myself = this;
    $xml = $(xml);
    $.each($xml.find('services > service > name'), function(idx, node) {
      var name = node.textContent;
      myself.selectData[name] = name;
    });
  },

  getDataInit:  function() {
    var url = webAppPath + '/kettle/listServices?streaming=' + this.streaming;

    $.ajax({
      type: "GET",
      url: url,
      dataType: "xml",
      success: this.parseXml.bind(this)
    });
  }
});

var StreamingDataServiceNameRenderer = DataServiceNameRenderer.extend({
  selectData: {},
  streaming: true
});