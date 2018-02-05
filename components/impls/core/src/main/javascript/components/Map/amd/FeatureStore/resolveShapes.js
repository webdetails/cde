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
