/*!
 * Copyright 2002 - 2016 Webdetails, a Pentaho company.  All rights reserved.
 *
 * This software was developed by Webdetails and is provided under the terms
 * of the Mozilla Public License, Version 2.0, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to  http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */

(function() {
    "use strict";

    /*global define:true, pvc:true, def:true */

    function moduleDef(def, pvc) {
        /**
         * @class
         * @name pvc.ext.DetTooltip
         *
         * @classdesc A rich HTML tooltip.
         *
         * To create an instance, use the factory function
         * {@link pvc.ext.detTooltip}.
         *
         * ## Basic Usage
         * Include the extension's files:
         *
         * 1. `detTooltip.js`
         * 2. `detTooltip.css`
         *
         * (If using AMD/Require-JS, the "css" AMD plugin must be registered and
         * will automatically load the accompanying stylesheet).
         *
         * In CDE, add the file or files as dashboard resources.
         *
         * Then, within a CDF chart component's `preExecution` handler, write:
         * ```javascript
         * pvc.ext.detTooltip()
         *      .install(this.chartDefinition);
         * ```
         *
         * ## Usage
         *
         * #### Displaying values in percentage, fallback to value if not available
         *
         * ```javascript

         * pvc.ext.detTooltip()
         *      .measuresValueFormatString("{item.percent.label|item.label}")
         *      .install(this.chartDefinition);
         * ```
         * ## Live examples
         *
         * [Examples page](examples/exts/detTooltip/examples.html).
         */

        /**
         * Creates a rich HTML tooltip formatter.
         * @alias detTooltip
         * @memberOf pvc.ext
         * @function
         * @return {pvc.ext.DetTooltip} A new tooltip formatter.
         */
        function detTooltip() {
            var _mappingByPlot = null;

            var _categoryLabelFormatString = "{caption}:&nbsp;{item.label}";
            var _categoryLabelFormatFunction = defaultFormatFunction;

            var _seriesLabelFormatString = "{item.label}";
            var _seriesLabelFormatFunction = defaultFormatFunction;

            var _measuresLabelFormatString = "{caption}";
            var _measuresLabelFormatFunction = defaultFormatFunction;

            var _measuresValueFormatString = "{item.label}";
            var _measuresValueFormatFunction = defaultFormatFunction;

            var _groupLabelFormatString = "{label}";
            var _groupLabelFormatFunction = defaultFormatFunction;

            function formatter(cd, defaults) {
                // Optional
                var copy = (defaults ? def.setUDefaults : def.copyOwn);
                copy(cd, {
                    tooltipEnabled:     true,
                    tooltipOpacity:     1,
                    tooltipGravity:     "s",
                    tooltipClassName:   "ccc-ext-det-tooltip",
                    tooltipFollowMouse: true
                });

                // Required
                cd.tooltipFormat = formatter.format;
                cd.axisTooltipFormat = formatter.axisTickLabelsFormat;
                cd.legendLabel_tooltip = formatter.legendLabelsFormat;
                return cd;
            }

            /**
             * Gets the visual role mapping for a given plot.
             *
             * Extend the default mapping if available, with one specified in
             * {@link pvc.ext.DetTooltip#plotMappings}.
             *
             * @param {pvc.visual.Plot} plot - The chart plot.
             *
             * @return {Object} The visual role mapping.
             */
            function getPlotMapping(plot) {
              var defaultMapping = getPlotDefaultMapping(plot);
              var mapping = def.copy(defaultMapping);

              var specifiedPlotMapping = _mappingByPlot && _mappingByPlot[plot.type];

              if(specifiedPlotMapping) def.copy(mapping, specifiedPlotMapping);

              return mapping;
            }

            /**
             * Installs this extension in a given chart definition.
             *
             * The formatter instance itself can be called as a function,
             * being equivalent to calling this method.
             *
             * This function defaults the properties:
             * * `tooltipEnabled` — `true`
             * * `tooltipOpacity` — `1`
             * * `tooltipGravity` — `"s"`
             * * `tooltipClassName` — `"ccc-ext-det-tooltip"`
             * * `tooltipFollowMouse` — `true`
             *
             * This function sets the required properties `tooltipFormat` and `axisTooltipFormat`.
             *
             * @name pvc.ext.DetTooltip#install
             * @function
             *
             * @param {Object} cd - The chart definition to extend.
             * @param {boolean} [defaults=false] - Indicates that only required or optional
             *                                     properties not present in the chart definition
             *                                     are set.
             *
             * @return {pvc.ext.DetTooltip} This instance.
             */
            formatter.install = formatter;

            /**
             * Gets or sets the mapping of visual roles for different {@link pvc.visual.Plot} plots.
             *
             * Each plot mapping has three visual roles that can be configured:
             * * `series`   - Accepts zero or one value
             * * `category` - Accepts zero or one value
             * * `measures` - Accepts zero or more values
             *
             * # Usage
             * ## Set a mapping to a "scatter" and a "pie" plot type.
             *
             * ```javascript
             * pvc.ext.detTooltip()
             *      .plotMappings({
             *          "scatter": {
             *              series:   "color",
             *              category: "category",
             *              measures: ["x", "y", "color", "size"]
             *          },
             *          "pie": {
             *              series:   "foo",
             *              category: null,
             *              measures: ["bar"]
             *          }
             *      }).install(this.chartDefinition);
             * ```
             *
             * @alias plotMappings
             * @memberOf pvc.ext.DetTooltip#
             * @function
             * @param {Object} [_] - The new value.
             * @return {pvc.ext.DetTooltip|Object} The property value, when getting, `this` instance, when setting.
             */
            formatter.plotMappings = function(_) {
                if(arguments.length) {
                    _mappingByPlot = _;
                    return formatter;
                }

                return _mappingByPlot;
            };

            /**
             * Formats an HTML tooltip for a given scene.
             *
             * This function can be called on any `this` context,
             * and will always exhibit the same behavior.
             *
             * Normally you would not use this function directly,
             * as {@link pvc.ext.DetTooltip#install}
             * sets this as the chart"s `tooltipFormat` for you.
             *
             * @alias format
             * @memberOf pvc.ext.DetTooltip#
             * @function
             *
             * @param {pvc.visual.Scene} scene - The categorical scene for which to render the tooltip.
             *
             * @return {string} The tooltip HTML string.
             */
            formatter.format = function(scene) {
                var mapping = getPlotMapping(scene.panel().plot);
                var model = buildModel.call(formatter, scene, mapping);

                return detTooltipRenderer.call(formatter, model);
            };

            /**
             * Formats an HTML tooltip for the current axis tick in the scene.
             *
             * Normally you would not use this function directly,
             * as {@link pvc.ext.DetTooltip#install}
             * sets this as the chart"s `axisTooltipFormat` for you.
             *
             * @alias axisTickLabelsFormat
             * @memberOf pvc.ext.DetTooltip#
             * @function
             *
             * @param {pvc.visual.Scene} scene - The scene of the tick for which to render the tooltip.
             *
             * @return {string} The tooltip HTML string.
             */
            formatter.axisTickLabelsFormat = function(scene) {
                return this.pvMark.textAngle() || (this.pvMark.text() !== scene.vars.tick.label) ? detTooltipRenderer.call(formatter, {groups: scene.groups}) : "";
            };

            /**
             * Formats an HTML tooltip for a legend item.
             *
             * Normally you would not use this function directly,
             * as {@link pvc.ext.DetTooltip#install}
             * sets this as the chart"s `legendLabel_tooltip` for you.
             *
             * @alias legendLabelsFormat
             * @memberOf pvc.ext.DetTooltip#
             * @function
             *
             * @param {pvc.visual.Scene} scene - The scene of the legend label for which to render the tooltip.
             *
             * @return {string} The tooltip HTML string.
             */
            formatter.legendLabelsFormat = function(scene) {
                var valueText = scene.vars.value.absLabel || scene.vars.value.label;

                return (this.pvMark.text() !== valueText) ? detTooltipRenderer.call(formatter, {groups: scene.groups}) : "";
            };

            /**
             * Gets or sets the format string for the label of the category. Used by the default label formater.
             *
             * Defaults to "{caption}:&nbsp;{item.label}".
             *
             * @alias categoryLabelFormatString
             * @memberOf pvc.ext.DetTooltip#
             * @function
             *
             * @param {string} [_] - The new value.
             *
             * @return {pvc.ext.DetTooltip|string} The property value, when getting; `this` instance, when setting.
             */
            formatter.categoryLabelFormatString = function(_) {
                if(arguments.length) {
                    _categoryLabelFormatString = _;
                    return formatter;
                }

                return _categoryLabelFormatString;
            };

            /**
             * Gets or sets the format function for the label of the category.
             *
             * The default format function uses the categoryLabelFormatString property for creating the label.
             *
             * Custom formatters receive the full tooltip model object, the category being formatted and the
             * current value of the categoryLabelFormatString property. Must return the formatted label.
             *
             * @alias categoryLabelFormatFunction
             * @memberOf pvc.ext.DetTooltip#
             * @function
             *
             * @param {function} [_] - The new value.
             *
             * @return {pvc.ext.DetTooltip|function} The property value, when getting; `this` instance, when setting.
             */
            formatter.categoryLabelFormatFunction = function(_) {
                if(arguments.length) {
                    _categoryLabelFormatFunction = _;
                    return formatter;
                }

                return _categoryLabelFormatFunction;
            };

            /**
             * Gets or sets the format string for the label of the series. Used by the default label formatter.
             *
             * Defaults to "{item.label}".
             *
             * @alias seriesLabelFormatString
             * @memberOf pvc.ext.DetTooltip#
             * @function
             *
             * @param {string} [_] - The new value.
             *
             * @return {pvc.ext.DetTooltip|string} The property value, when getting; `this` instance, when setting.
             */
            formatter.seriesLabelFormatString = function(_) {
                if(arguments.length) {
                    _seriesLabelFormatString = _;
                    return formatter;
                }

                return _seriesLabelFormatString;
            };

            /**
             * Gets or sets the format function for the label of the series.
             *
             * The default format function uses the seriesLabelFormatString property for creating the label.
             *
             * Custom formatters receive the full tooltip model object, the series being formatted and the
             * current value of the seriesLabelFormatString property. Must return the formatted label.
             *
             * @alias seriesLabelFormatFunction
             * @memberOf pvc.ext.DetTooltip#
             * @function
             *
             * @param {function} [_] - The new value.
             *
             * @return {pvc.ext.DetTooltip|function} The property value, when getting; `this` instance, when setting.
             */
            formatter.seriesLabelFormatFunction = function(_) {
                if(arguments.length) {
                    _seriesLabelFormatFunction = _;
                    return formatter;
                }

                return _seriesLabelFormatFunction;
            };

            /**
             * Gets or sets the format string for the label of the measures. Used by the default label formatter.
             *
             * Defaults to "{caption}".
             *
             * @alias measuresLabelFormatString
             * @memberOf pvc.ext.DetTooltip#
             * @function
             *
             * @param {string} [_] - The new value.
             *
             * @return {pvc.ext.DetTooltip|string} The property value, when getting; `this` instance, when setting.
             */
            formatter.measuresLabelFormatString = function(_) {
                if(arguments.length) {
                    _measuresLabelFormatString = _;
                    return formatter;
                }

                return _measuresLabelFormatString;
            };

            /**
             * Gets or sets the format function for the label of the measures.
             *
             * The default format function uses the measuresLabelFormatString property for creating the label.
             *
             * Custom formatters receive the full tooltip model object, the measure being formatted and the
             * current value of the measuresLabelFormatString property. Must return the formatted label.
             *
             * @alias measuresLabelFormatFunction
             * @memberOf pvc.ext.DetTooltip#
             * @function
             *
             * @param {function} [_] - The new value.
             *
             * @return {pvc.ext.DetTooltip|function} The property value, when getting; `this` instance, when setting.
             */
            formatter.measuresLabelFormatFunction = function(_) {
                if(arguments.length) {
                    _measuresLabelFormatFunction = _;
                    return formatter;
                }

                return _measuresLabelFormatFunction;
            };

            /**
             * Gets or sets the format string for the value of the measures. Used by the default label formatter.
             *
             * Defaults to "{value}".
             *
             * @alias measuresValueFormatString
             * @memberOf pvc.ext.DetTooltip#
             * @function
             *
             * @param {string} [_] - The new value.
             *
             * @return {pvc.ext.DetTooltip|string} The property value, when getting; `this` instance, when setting.
             */
            formatter.measuresValueFormatString = function(_) {
                if(arguments.length) {
                    _measuresValueFormatString = _;
                    return formatter;
                }

                return _measuresValueFormatString;
            };

            /**
             * Gets or sets the format function for the value of the measures.
             *
             * The default format function uses the measuresValueFormatString property for creating the value.
             *
             * Custom formatters receive the full tooltip model object, the measure being formatted and the
             * current value of the measuresValueFormatString property. Must return the formatted value.
             *
             * @alias measuresValueFormatFunction
             * @memberOf pvc.ext.DetTooltip#
             * @function
             *
             * @param {function} [_] - The new value.
             *
             * @return {pvc.ext.DetTooltip|function} The property value, when getting; `this` instance, when setting.
             */
            formatter.measuresValueFormatFunction = function(_) {
                if(arguments.length) {
                    _measuresValueFormatFunction = _;
                    return formatter;
                }

                return _measuresValueFormatFunction;
            };

            /**
             * Gets or sets the format string for a group. Used by the default label formatter.
             *
             * Defaults to "{label}".
             *
             * @alias groupLabelFormatString
             * @memberOf pvc.ext.DetTooltip#
             * @function
             *
             * @param {string} [_] - The new value.
             *
             * @return {pvc.ext.DetTooltip|string} The property value, when getting; `this` instance, when setting.
             */
            formatter.groupLabelFormatString = function(_) {
                if(arguments.length) {
                    _groupLabelFormatString = _;
                    return formatter;
                }

                return _groupLabelFormatString;
            };

            /**
             * Gets or sets the format function for a group.
             *
             * The default format function uses the categoryLabelFormatString property for creating the label.
             *
             * Custom formatters receive the full tooltip model object, the category being formatted and the
             * current value of the categoryLabelFormatString property. Must return the formatted label.
             *
             * @alias groupLabelFormatFunction
             * @memberOf pvc.ext.DetTooltip#
             * @function
             *
             * @param {function} [_] - The new value.
             *
             * @return {pvc.ext.DetTooltip|function} The property value, when getting; `this` instance, when setting.
             */
            formatter.groupLabelFormatFunction = function(_) {
                if(arguments.length) {
                    _groupLabelFormatFunction = _;
                    return formatter;
                }

                return _groupLabelFormatFunction;
            };

            return formatter;
        }

        /**
         * Gets the default mapping for {@link pvc.visual.CategoricalPlot},
         * {@link pvc.visual.PiePlot} or {@link pvc.visual.MetricPointPlot}.
         *
         * @param {pvc.visual.Plot} plot - The chart plot.
         *
         * @return {Object} The default visual role mapping.
         */
        function getPlotDefaultMapping(plot) {
            if(plot instanceof pvc.visual.CategoricalPlot)
                return {
                    series:   "color",
                    category: "category",
                    measures: ["value"]
                };

            if(plot instanceof pvc.visual.PiePlot)
                return {
                    series:   "color",
                    category: null,
                    measures: ["value"]
                };

            if(plot instanceof pvc.visual.MetricPointPlot)
                return {
                    series:   "color",
                    category: "category",
                    measures: ["x", "y", "color", "size"]
                };

            return {
                series:   null,
                category: null,
                measures: null
            }
        }

        /**
         * Builds the tooltip model from the CCC scene information.
         *
         * @param {Object} scene - The CCC scene.
         * @param {Object} mapping - Map of visual roles for current plot type
         *
         * @return {Object} The tooltip model.
         */
        function buildModel(scene, mapping) {
            var tooltipModel = {
                measures: [],
                series:   null,
                category: null
            };

            var plot = scene.panel().plot,
                rootData = scene.chart().data.root,
                mappingMeasures = mapping.measures ? def.array.to(mapping.measures) : null,
                visualRole, item, color;

            // measures mappings have precedence on category and series mappings for
            // non-discrete visual roles.
            var hasMeasureVar = function(aVarName) {
                return !!mappingMeasures && mappingMeasures.indexOf(aVarName) >= 0;
            };

            var varName = mapping.category;
            if(varName) {
                visualRole = plot.visualRoles[varName];
                if(visualRole && visualRole.isBound() && (visualRole.isDiscrete() || !hasMeasureVar(varName))) {
                    item = getVarItem(scene, varName);
                    if(item) tooltipModel.category = {
                        item:    item,
                        caption: buildCompositeLabel(rootData.type, visualRole) // visual role label
                    };

                } else {
                    // Fallback to support scatter chart not having a category visual role
                    // but instead having dimensions in the "_rows_" dimension group.
                    tooltipModel.category = noBoundCategory(scene, "_rows_");

                }
            }

            varName = mapping.series;
            if(varName) {
                visualRole = plot.visualRoles[varName];
                if(visualRole && visualRole.isBound() && (visualRole.isDiscrete() || !hasMeasureVar(varName))) {

                    item = getVarItem(scene, varName);
                    if(item) tooltipModel.series = {
                        color:   varName === "color" ? getColorScaleValue(scene) : null,
                        item:    item,
                        caption: buildCompositeLabel(rootData.type, visualRole)
                    };
                }
            }

            if(mappingMeasures) {
                mappingMeasures.forEach(function(varName) {
                    visualRole = plot.visualRoles[varName];
                    if(visualRole && visualRole.isBound() && !visualRole.isDiscrete()) {

                        item = getVarItem(scene, varName);
                        if(item) tooltipModel.measures.push({
                            color:   varName === "color" ? getColorScaleValue(scene) : null,
                            item:    item,
                            caption: buildCompositeLabel(rootData.type, visualRole)
                        });
                    }
                });

            }

            return tooltipModel;
        }

        /**
         * Gets the item value in a given scene var.
         *
         * @param {Object} scene - The CCC scene.
         * @param {string} varName - The scene var name.
         *
         * @return {?Object} The item value.
         */
        function getVarItem(scene, varName) {
            var item = scene.vars[varName];

            return item && item.value != null ? item : null;
        }

        /**
         * Gets the color scale value of the CCC scene.
         *
         * @param {Object} scene - The CCC scene.
         *
         * @return {string} The color scale value.
         */
        function getColorScaleValue(scene) {
            var rootColor = scene.root.panel().axes.color;

            return rootColor.sceneScale({sceneVarName: 'color'})(scene).color;
        }

        /**
         * Fallback to support scatter chart not having a category mapped
         *
         * @param {Object} scene - The CCC scene.
         * @param {string} varName - The scene var name.
         *
         * @return {*}
         */
        function noBoundCategory(scene, varName) {
            var rootData = scene.chart().data.root;
            var datum    = scene.datum;
            if(datum) {
                var dimNames = rootData.type.groupDimensionsNames(varName, {assertExists: false});
                if(dimNames) {
                    var item = datum.view(dimNames);
                    if(item.value != null) {
                        return {
                            item: item,
                            caption: dimNames.map(function(dimName) {
                                return rootData.type.dimensions(dimName).label;
                            }).join(rootData.labelSep)
                        }
                    }
                }
            }

            return null;
        }

        /**
         * Builds the composite label of a given visual role, taking into account
         * all its dimensions.
         *
         * @param {Object} complexType - The root data type object.
         * @param {Object} visualRole - A chart visual role.
         *
         * @return {string} The composite label.
         */
        function buildCompositeLabel(complexType, visualRole) {
            return visualRole.grouping.dimensionNames().map(function(dimName) {
                return complexType.dimensions(dimName).label;
            }).join(", ");
        }

        /**
         * The default format function replaces tokens in the received format string
         * with values from the current tooltipModel subject (category, series, measure).
         *
         * @param {Object} subject - The current tooltip subject being formated.
         * @param {string} formatString - The format string.
         *
         * @return {string} The formatted text.
         */
        function defaultFormatFunction(subject, formatString) {
            var missingPathMarker = {};
            var scope = function(alternativesText) {
                var alternatives = alternativesText.split("|");
                var i = -1;
                var L = alternatives.length;
                while(++i < L) {
                    var value = def.getPath(subject, alternatives[i], missingPathMarker);
                    if(value !== missingPathMarker)
                        return value == null ? "" : def.html.escape(value.toString());
                }
            };

            return def.format(formatString, scope);
        }

        /**
         * The renderer method is called with a pre-built tooltip model object,
         * and having the formatter instance as `this` context.
         *
         * @param {Object} tooltipModel - The tooltip model object.
         *
         * @return {string} The HTML of the rendered tooltip.
         */
        function detTooltipRenderer(tooltipModel) {
            var baseElement = document.createElement("div");

            if(tooltipModel.groups) {
                var axisTickLabelsElement = document.createElement("ul");
                axisTickLabelsElement.className = "group-container";

                tooltipModel.groups.forEach(function(group) {
                    var axisLabelElement = document.createElement("li");
                    axisLabelElement.className = "group-label";

                    var labelElement = document.createElement("h1");
                    labelElement.innerHTML = defaultFormatFunction(group, this.groupLabelFormatString());

                    axisLabelElement.appendChild(labelElement);

                    axisTickLabelsElement.appendChild(axisLabelElement);
                }, this);

                baseElement.appendChild(axisTickLabelsElement);
            }

            if(tooltipModel.category) {
                var titleElement = document.createElement("h1");
                titleElement.innerHTML = defaultFormatFunction(tooltipModel.category, this.categoryLabelFormatString());

                baseElement.appendChild(titleElement);
            }

            var seriesElement, labelElement;

            if(tooltipModel.series != null) {
                seriesElement = document.createElement("div");
                seriesElement.className = "series";

                renderColorMark(seriesElement, tooltipModel.series);

                labelElement = document.createElement("h2");
                labelElement.innerHTML = defaultFormatFunction(tooltipModel.series, this.seriesLabelFormatString());
                seriesElement.appendChild(labelElement);

                baseElement.appendChild(seriesElement);
            }

            if(tooltipModel.measures) {
                if(tooltipModel.measures.length === 1 && tooltipModel.series) {
                    renderMeasureValue.call(this, seriesElement, tooltipModel.measures[0]);

                } else {
                    var measuresElement = document.createElement("ul");
                    measuresElement.className = "measures-container";

                    tooltipModel.measures.forEach(function(measure) {
                        var measureElement = document.createElement("li");
                        measureElement.className = "measure";

                        renderColorMark(measureElement, measure);

                        labelElement = document.createElement("h3");
                        labelElement.innerHTML = defaultFormatFunction(measure, this.measuresLabelFormatString());

                        measureElement.appendChild(labelElement);

                        renderMeasureValue.call(this, measureElement, measure);

                        measuresElement.appendChild(measureElement);

                    }, this);

                    baseElement.appendChild(measuresElement);
                }
            }

            return baseElement.innerHTML;
        }

        /**
         * The renderer method is called to render a color mark in the given html element,
         * when a visual role has a color associated with it.
         *
         * @param {Element} parentElement - Visual role html element where to place the color mark.
         * @param {Object} roleInfo - The visual role information.
         */
        function renderColorMark(parentElement, roleInfo) {
            if(roleInfo.color) {
                var colorElement = document.createElementNS("http://www.w3.org/2000/svg", "svg");
                colorElement.style.fill = roleInfo.color;
                colorElement.setAttribute("viewBox", "0 0 4 4");
                colorElement.setAttribute("class", "color");

                var circleElement = document.createElementNS("http://www.w3.org/2000/svg", "circle");
                circleElement.setAttribute("cx", "2");
                circleElement.setAttribute("cy", "2");
                circleElement.setAttribute("r", "2");

                colorElement.appendChild(circleElement);
                parentElement.appendChild(colorElement);
            }
        }

        /**
         * The renderer method is called to try to render the value of a measure
         * in the given html element.
         *
         * @param {Element} parentElement - Measure html element where to place the color mark.
         * @param {Object} roleInfo - The measure information.
         */
        function renderMeasureValue(parentElement, roleInfo) {
            if(roleInfo.item != null) {
                var valueElement = document.createElement("span");
                valueElement.innerHTML = defaultFormatFunction(roleInfo, this.measuresValueFormatString());

                parentElement.appendChild(valueElement);
            }
        }

        (pvc.ext || (pvc.ext = {})).detTooltip = detTooltip;

        return detTooltip;
    }

    if(typeof define !== "undefined" && define.amd) {
        define(["cdf/lib/CCC/def", "cdf/lib/CCC/pvc", "css!./detTooltip.css"], moduleDef);
    } else if(typeof pvc !== "undefined") {
        moduleDef(def, pvc);
    }
}());
