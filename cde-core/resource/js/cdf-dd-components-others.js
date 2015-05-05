/*!
 * Copyright 2002 - 2015 Webdetails, a Pentaho company. All rights reserved.
 *
 * This software was developed by Webdetails and is provided under the terms
 * of the Mozilla Public License, Version 2.0, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */

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
