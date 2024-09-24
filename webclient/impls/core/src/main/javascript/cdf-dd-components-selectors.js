/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

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
