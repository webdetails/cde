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


define([
  "cdf/lib/jquery",
  "amd!cdf/lib/underscore",
  "./shapeConversion"
], function($, _, ShapeConversion) {
  "use strict";
  return resolveShapes;

  function resolveShapes(json, mapping, configuration) {
    var addIn = this.getAddIn("ShapeResolver", configuration.addIns.ShapeResolver.name);
    var url = configuration.addIns.ShapeResolver.options.url;
    if (!addIn && url) {
      if (url.endsWith("json") || url.endsWith("js")) {
        addIn = this.getAddIn("ShapeResolver", "simpleJSON");
      } else {
        addIn = this.getAddIn("ShapeResolver", "kml");
      }
    }
    var deferred = $.Deferred();
    if (!addIn) {
      deferred.resolve({});
      return deferred.promise();
    }

    var idList = _.pluck(json.resultset, mapping.id);
    var st = {
      keys: idList, //TODO Consider keys -> ids
      ids: idList,
      tableData: json,
      _simplifyPoints: ShapeConversion.simplifyPoints,
      _parseShapeKey: configuration.addIns.ShapeResolver.options.parseShapeKey,
      _shapeSource: url
    };
    var promise = addIn.call(this, st, this.getAddInOptions("ShapeResolver", addIn.getName()));
    $.when(promise).then(function(result) {
      var shapeDefinitions = _.chain(result)
        .map(function(geoJSONFeature, key) {
          return [key, geoJSONFeature];
        })
        .object()
        .value();
      deferred.resolve(shapeDefinitions);
    });
    return deferred.promise();
  }

});
