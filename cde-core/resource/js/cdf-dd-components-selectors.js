/*!
 * Copyright 2002 - 2017 Webdetails, a Hitachi Vantara company. All rights reserved.
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
var ParameterRenderer = SelectRendererNonForcefull.extend({

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
      _str += '"' + val.properties[0].value + '": "' + val.properties[0].value + '",';
    });

    // this.value is already enclosured, but the enclosure can be both ' and "
    // hence replace all ' with "
    var selected = this.value.replace(/\$\{p:(.+?)\}/g, '$1').replace(/\'/g, '"');
    _str += ' "selected":' + selected + "}";
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

var DatasourceRenderer = SelectRendererNonForcefull.extend({

  getData: function() {

    return Panel.getPanel(DatasourcesPanel.MAIN_PANEL).getDatasources().map(function(o) {
      return o.properties[0].value;
    });
  }

});

var HtmlObjectRenderer = SelectRendererNonForcefull.extend({

  getData: function() {
    var r = {};
    _.each(Panel.getPanel(LayoutPanel.MAIN_PANEL).getHtmlObjects(),function(o){
      var h = o.properties[0].value;
      r[h] = h;
    });
    return r;
  },

  getActualValue: function(value) {
    if(!!value) {
      if(value.indexOf("${h:") == 0 && value.lastIndexOf("}") == (value.length - 1)){
        return value;
      }
      return "${h:" + value + "}";
    }
    return this.base(value);
  },

  getLabel: function(data, value) {
    if(value && value.indexOf("${h:") == 0 && value.lastIndexOf("}") == (value.length - 1)) {
      return value.substring(4, value.length - 1);
    }
    return this.base(data, value);
  }
});

var HtmlTargetRenderer = HtmlObjectRenderer.extend({
  getData: function() {
    var r = {};
    var data = Panel.getPanel(LayoutPanel.MAIN_PANEL).getHtmlTargets();
    _.each(data, function(obj) {
      var h = obj.properties[0].value;
      r[h] = h;
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
