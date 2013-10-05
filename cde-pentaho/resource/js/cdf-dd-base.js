var Endpoints = {

    staticUrl: "/pentaho/content/pentaho-cdf-dd/",
    pluginUrl: "/pentaho/content/pentaho-cdf-dd/",
    cssResourceUrl: "/getCssResource?resource=",
    imageResourceUrl: "/getResource?resource=",
    jsResourceUrl: "/getJsResource?resource=",

    getStaticUrl: function () {
        return this.staticUrl;
    },

    getPluginUrl: function () {
        return this.pluginUrl;
    },

    getCssResourceUrl: function () {
        return this.pluginUrl + this.cssResourceUrl;
    },

    getImageResourceUrl: function () {
        return this.pluginUrl + this.imageResourceUrl;
    },

    getJsResourceUrl: function () {
        return this.pluginUrl + this.jsResourceUrl;
    }
};


var SynchronizeRequests = {

    doPost: function (templateParams) {

        $.post(Endpoints.getPluginUrl()+"SyncTemplates", templateParams, function (result) {
            var json = Util.parseJsonResult(result);
            if (json.status == "true") {
                $.notifyBar({ html: "Template saved successfully", delay: 1000 });
            }
            else {
                $.notifyBar({ html: "Errors saving template: " + json.result });
            }
        });
    },

    doGetJson: function (loadParams) {

        $.getJSON(Endpoints.getPluginUrl()+"SyncTemplates", loadParams, function (json) {
            if (json.status) {

                templates = json.result;
                var selectTemplate = undefined;
                var myTemplatesCount = 0;
                var _templates = '<h2 style="padding:10px; line-height: 20px;">Apply Template</h2><hr><div class="templates"><a class="prev disabled"></a><div class="scrollable"><div id="thumbs" class="thumbs">';
                var _myTemplates = '<h2 style="padding:10px; line-height: 20px;">Apply Custom Template</h2><hr><div class="templates"><a class="prev disabled"></a><div class="scrollable"><div id="thumbs" class="thumbs">';
                for (v in templates) {
                    if (templates.hasOwnProperty(v)) {
                        if (templates[v].type == "default")
                            _templates += '<div><img id="' + v + '" src="' + templates[v].img + '"/><p>' + templates[v].structure.layout.title + '</p></div>';
                        else if (templates[v].type == "custom") {
                            _myTemplates += '<div><img id="' + v + '" src="' + templates[v].img + '"/><p>' + templates[v].structure.layout.title + '</p></div>';
                            myTemplatesCount++;
                        }
                    }
                }
                _templates += '</div></div><a class="next"></a></div>';
                _myTemplates += '</div></div><a class="next"></a></div>';
                var loaded = function () {
                    selectTemplate = undefined;
                    $("div.scrollable").scrollable({size: 3, items: '#thumbs', hoverClass: 'hover'});
                    $(function () {
                        $("div.scrollable:eq(0) div.thumbs div").bind('click', function () {
                            selectTemplate = templates[$(this).find("img").attr("id")];
                        });
                    });
                };

                var callback = function (v, m, f) {
                    if (v == 1 && selectTemplate != undefined) {
                        $.prompt('Are you sure you want to load the template? ', { buttons: { Ok: true, Cancel: false}, prefix: "popupTemplate",
                            callback: function (v, m, f) {
                                if (v) {
                                    cdfdd.dashboardData = selectTemplate.structure;
                                    cdfdd.layout.init();
                                    cdfdd.components.init();
                                    cdfdd.datasources.init();
                                }
                            }});
                    }
                };

                var promptTemplates = {
                    loaded: loaded,
                    buttons: myTemplatesCount > 0 ? { MyTemplates: 2, Ok: 1, Cancel: 0 } : {Ok: 1, Cancel: 0},
                    opacity: 0.2,
                    prefix: 'popupTemplate',
                    callback: callback,
                    submit: function (v, m, f) {
                        if (v != 2) return true;
                        $.prompt.close();
                        $.prompt(_myTemplates, promptMyTemplates, {prefix: "popupTemplate"});
                    }};

                var promptMyTemplates = {
                    loaded: loaded,
                    buttons: { Back: 2, Ok: 1, Cancel: 0 },
                    opacity: 0.2,
                    prefix: 'popupTemplate',
                    callback: callback,
                    submit: function (v, m, f) {
                        if (v != 2) return true;
                        $.prompt.close();
                        $.prompt(_templates, promptTemplates, {prefix: "popupTemplate"});
                    }};

                $.prompt(_templates, promptTemplates, {prefix: "popupTemplate"});
            }
            else
                $.notifyBar({ html: "Error loading templates: " + json.result });
        });
    }

};


var OlapWizardRequests = {

    olapObject: function (params, container, myself) {

        $.getJSON(Endpoints.getPluginUrl() + "OlapUtils", params, function (json) {
            if (json.status == "true") {
                myself.membersArray = json.result.members;

                if (myself.membersArray.length == 0) {
                    myself.membersArray = Util.clone(myself.initialMembersArray);
                    myself.member = myself.initialMember;
                } else {
                    myself.member = myself.membersArray[0].qualifiedName;
                }

                if (direction == 'down') {
                    myself.memberDepth++;
                } else {
                    myself.memberDepth--;
                }

                myself.render(container);
                myself.processChange();
            }
        });
    },

    olapManager: function (params, myself) {

        $.getJSON(Endpoints.getPluginUrl() + "OlapUtils", params, function (json) {
            if (json && json.status == "true") {

                var catalogs = json.result.catalogs;
                myself.setCatalogs(catalogs);

                myself.logger.info("Got correct response from getCubes: " + catalogs);

                var _selector = $("#cdfddOlapCubeSelector");
                _selector.append(
                    '<select id="cdfddOlapCatalogSelect" class="small" onchange="WizardManager.getWizardManager(\'' + myself.wizardId + '\').catalogSelected()"><option value="-"> Select catalog </option></select><br/>');
                _selector.append(
                    '<select id="cdfddOlapCubeSelect" class="small" onchange="WizardManager.getWizardManager(\'' + myself.wizardId + '\').cubeSelected()" ><option value="-"> Select cube </option></select>');

                $.each(catalogs, function (i, catalog) {
                    $("select#cdfddOlapCatalogSelect", _selector).append("<option>" + catalog.name + "</option>");
                });

            }
            else {
                alert(json.result);
            }
        });
    },

    olapCubeSelected: function (params, selectedCube, selectedCatalog, myself) {

        $.getJSON(Endpoints.getPluginUrl() + "OlapUtils", params, function (json) {
            if (json.status == "true") {

                myself.logger.info("Got correct response from GetCubeStructure");

                var dimensions = json.result.dimensions;

                var dimensionIdx = 0;
                var dimensionTBody = $("#cdfddOlapDimensionSelector > tbody");
                dimensionTBody.empty();
                $.each(dimensions, function (i, dimension) {
                    var hierarchies = dimension.hierarchies;
                    $.each(hierarchies, function (j, hierarchy) {
                        var hierarchyId = "dimRow-" + (++dimensionIdx);
                        dimensionTBody.append("<tr id='" + hierarchyId + "'><td>" + hierarchy.caption + "</td></tr>");

                        var levels = hierarchy.levels;
                        $.each(levels, function (k, level) {
                            var levelId = "dimRow-" + (++dimensionIdx);
                            dimensionTBody.append("<tr id='" + levelId + "' class='olapObject child-of-" + hierarchyId + "'><td class='draggableDimension'>" + level.caption + "</td></tr>");
                            level.hierarchy = hierarchy;
                            level.catalog = selectedCatalog;
                            level.cube = selectedCube;
                            myself.addOlapObject(WizardOlapObjectManager.DIMENSION, level);
                        });
                    });


                });
                dimensionTBody.parent().treeTable();
                $("td.draggableDimension", dimensionTBody).draggable({helper: 'clone'});

                // Measures

                var measures = json.result.measures;

                var measureIdx = 0;
                var measureTBody = $("#cdfddOlapMeasureSelector > tbody");
                measureTBody.empty();
                $.each(measures, function (i, measure) {
                    var measureId = "levelRow-" + (++measureIdx);
                    measureTBody.append("<tr id='" + measureId + "' class='olapObject'><td class='draggableMeasure'>" + measure.caption + "</td></tr>");
                    myself.addOlapObject(WizardOlapObjectManager.MEASURE, measure);

                });
                measureTBody.parent().treeTable();
                $("td.draggableMeasure", measureTBody).draggable({helper: 'clone', type: "Measure"});

                myself.getAvailableFilters();
            }
            else {
                alert(json.result);
            }
        });
    }
};


var StylesRequests = {

    syncStyles: function (myself) {

        $.getJSON(Endpoints.getPluginUrl() + "SyncStyles", {
            operation: "listStyles"
        }, function (json) {
            myself.styles = json.result;
        });
    },

    listStyleRenderers: function (myself) {

        $.getJSON(Endpoints.getPluginUrl() + "listRenderers", {
            operation: "listStyles"
        }, function (json) {
            myself.renderers = json.result;

        });
    },

    initStyles: function (saveSettingsParams, wcdf, myself, callback) {

        $.post(CDFDDDataUrl, saveSettingsParams, function (result) {
            var json = eval("(" + result + ")");
            if (json.status == "true") {
                myself.setDashboardWcdf(wcdf);
                callback();
            } else {
                $.notifyBar({
                    html: "Errors initializing settings: " + json.result
                });
            }
        });
    }
};


var SaveRequests = {

    saveSettings: function (saveSettingsParams, cdfdd, wcdf, myself) {

        $.post(Endpoints.getPluginUrl() + "Syncronize", saveSettingsParams, function (result) {
            try {
                var json = eval("(" + result + ")");
                if (json.status == "true") {
                    myself.setDashboardWcdf(wcdf);
                    // We need to reload the layout engine in case the rendererType changed
                    cdfdd.layout.init();
                    $.notifyBar({
                        html: "Dashboard Settings saved successfully",
                        delay: 1000
                    });
                } else {
                    throw json.result;
                }
            } catch (e) {
                $.notifyBar({
                    html: "Errors saving settings: " + e
                });
            }
        });
    },

    saveDashboard: function (saveParams, stripArgs) {

        $.post(Endpoints.getPluginUrl() + "Syncronize", saveParams, function (result) {
            //$.getJSON("/pentaho/content/pentaho-cdf-dd/Syncronize", saveParams, function(json) {
            var json = eval("(" + result + ")");
            if (json.status == "true") {
                if (stripArgs.needsReload) {
                    window.location.reload();
                } else {
                    $.notifyBar({
                        html: "Dashboard saved successfully",
                        delay: 1000
                    });
                }
            } else {
                $.notifyBar({
                    html: "Errors saving file: " + json.result
                });
            }
        });
    },

    saveAsDashboard: function (saveAsParams, selectedFolder, selectedFile, myself) {

        $.post(Endpoints.getPluginUrl() + "Syncronize", saveAsParams, function (result) {
            var json = eval("(" + result + ")");
            if (json.status == "true") {
                if (selectedFolder[0] == "/") selectedFolder = selectedFolder.substring(1, selectedFolder.length);
                var solutionPath = selectedFolder.split("/");
                myself.initStyles(function () {
                    //cdfdd.setExitNotification(false);
                    window.location = window.location.origin + Endpoints.getPluginUrl() + 'Edit?solution=' + solutionPath[0] + "&path=" + solutionPath.slice(1).join("/") + "&file=" + selectedFile;
                });
            } else
                $.notifyBar({
                    html: "Errors saving file: " + json.result
                });
        });
    },

    saveAsWidget: function (saveAsParams, selectedFolder, selectedFile, myself) {

        $.post(Endpoints.getPluginUrl() + "Syncronize", saveAsParams, function (result) {
            var json = JSON.parse(result);
            if (json.status == "true") {
                if (selectedFolder[0] == "/") {
                    selectedFolder = selectedFolder.substring(1, selectedFolder.length);
                }
                var solutionPath = selectedFolder.split("/");
                var wcdf = myself.getDashboardWcdf();
                // TODO: dashboard is being saved twice. This also needs to be fixed..
                wcdf.title = saveAsParams.title;
                wcdf.description = saveAsParams.description;
                wcdf.widgetName = saveAsParams.widgetName;
                wcdf.widget = true;
                myself.saveSettingsRequest(wcdf);
                myself.initStyles(function () {
                    window.location = window.location.origin + Endpoints.getPluginUrl() + '/Edit?solution=' + solutionPath[0] + "&path=" + solutionPath.slice(1).join("/") + "&file=" + selectedFile;
                });
            } else {
                $.notifyBar({
                    html: "Errors saving file: " + json.result
                });
            }
        });
    },
};

var LoadRequests = {

    loadDashboard: function (loadParams, myself) {
        $.post(Endpoints.getPluginUrl() + "Syncronize", loadParams, function (result) {
            var json = eval("(" + result + ")");
            if (json && json.status == "true") {
                myself.setDashboardData(myself.unstrip(json.result.data));
                myself.setDashboardWcdf(json.result.wcdf);
                myself.init();
            } else {
                alert(json && json.result);
            }
        });
    }
};

var PreviewRequests = {

    previewDashboard: function (saveParams, _href) {
        $.post(Endpoints.getPluginUrl() + "Syncronize", saveParams, function (result) {
            var json = eval("(" + result + ")");
            if (json.status == "true") {
                $.fancybox({
                    type: "iframe",
                    href: _href,
                    width: $(window).width(),
                    height: $(window).height()
                });
            } else {
                $.notifyBar({
                    html: "Errors saving file: " + json.result
                });
            }
        });
    }
};

