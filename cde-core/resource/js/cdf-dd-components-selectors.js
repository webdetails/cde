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

// Parameter
var ParameterRenderer = SelectRenderer.extend({

  getData: function() {
    var r = {};
    _.each(Panel.getPanel(ComponentsPanel.MAIN_PANEL).getParameters(), function(o) {
      var p = o.properties[0].value;
      r["${p:" + p + "}"] = p;
    });
    return r;
  }
});

// Listeners
var ListenersRenderer = SelectMultiRenderer.extend({

  getData: function() {
    var data = Panel.getPanel(ComponentsPanel.MAIN_PANEL).getParameters();
    var _str = "{";
    $.each(data, function(i, val) {
      _str += "'" + val.properties[0].value + "': '" + val.properties[0].value + "',";
    });

    _str += " 'selected':" + (this.value.replace(/\$\{p:(.+?)\}/g, '$1')) + "}";
    return _str;
  },

  postProcessValue: function(val) {
    var processed = val.split(", ").map(function(v) {
      if(v.indexOf('Dashboards.storage') == 0) return v; else return "${p:" + v + "}";
    }).join(", ");
    return processed;
  },

  getFormattedValue: function(value) {
    var v = value.replace(/','/g, "', '").replace(/\$\{p:(.+?)\}/g, '$1');
    if(v.length > 20) v = v.substring(0, 20) + " (...)";
    return v;
  }
});

var ArrayParameterRenderer = ListenersRenderer.extend({});

var DatasourceRenderer = SelectRenderer.extend({

  getData: function() {

    return Panel.getPanel(DatasourcesPanel.MAIN_PANEL).getDatasources().map(function(o) {
      return o.properties[0].value;
    });
  }

});

var HtmlObjectRenderer = SelectRenderer.extend({

  getData: function() {
    var r = {};
    _.each(Panel.getPanel(LayoutPanel.MAIN_PANEL).getHtmlObjects(),function(o){
      var h = o.properties[0].value;
      r["${h:" + h + "}"] = h;
    });
    return r;
  }
});

var MatchTypeRenderer = SelectRenderer.extend({

  isAutoComplete: false,

  selectData: {
    'fromStart': 'From Start',
    'all':       'All'
  }
});
