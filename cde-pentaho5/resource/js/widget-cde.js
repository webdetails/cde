/*!
 * Copyright 2002 - 2014 Webdetails, a Pentaho company. All rights reserved.
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


var deps = ["dashboards/oss-module", "dashboards/dashboard-module"];

if(document.location.href.indexOf("debug=true") > 0) {
  deps = ["dashboards/oss-module", "dashboards/pentaho-dashboard-controller"];
}

pen.define("cde/widget-cde", deps,
    function(ossm, pdc) {

      PentahoCdeComponent = BaseComponent.extend({
        staticParameters: true,
        type: "PentahoCdeComponent",
        iconImgSrc: '../../../../../../content/pentaho-cdf-dd/resources/wcdfFileType.png',
        executeAtStart: true,
        options: {
          "showParameters": false

        },
        isDirty: false,
        setDirty: function(isDirty) {
          this.isDirty = isDirty;


        },
        outputParameters: [],

        update: function() {
          var cdeRef = this.genXaction();
          var pathId = ":" + cdeRef.replace(/\//g, ":");
          pathId = pathId.replace("#", "%23");
          pathId = pathId.replace("{", "%7B");
          pathId = pathId.replace("}", "%7D");
          pathId = pathId.replace("<", "%3C");
          pathId = pathId.replace(">", "%3E");
          pathId = pathId.replace("+", "%2B");

          var me = this,
              url = webAppPath + "/api/repos/" + pathId + "/generatedContent";

          if(!this.parameters) {
            this.parameters = "";
          }

          for(var i = 0; i < this.parameters.length; i++) {
            var key = this.parameters[i][0];
            var value = this.parameters[i][1] == "" ? this.parameters[i][2] : Dashboards
                .getParameterValue(this.parameters[i][1]);
            if(value == "NIL") {
              value = this.parameters[i][2];
            }
            if(i == 0) {
              url += "?";
            } else {
              url += "&";
            }
            url += encodeURIComponent(key) + "=" + encodeURIComponent(value);
          }

          $("#" + me.htmlObject).html(
                  "<iframe style='width:100%;height:100%;border:0px' frameborder='0' border='0' src='" +
                  url + "'/>"
          );
        },

        //gets the runUrl and paramServiceUrl from the server then alerts the caller via the callback
        setSolutionPathAction: function(solution, path, action, callback) {

          this.solution = solution;
          this.path = path;
          this.action = action;

          // parameter service url from file details; tells us how to render the content;
          var paramServiceUrl = '';
          var runUrl = '';
          var cdeRef = this.genXaction();
          // get the base url so that we can call the sol repo service
          // save a reference to this for use in nested functions
          var thisComponent = this;
          var pathId = ":" + cdeRef.replace(/\//g, ":");
          callback.onfinish();

        },

        editWidget: function() {
          var cdeRef = currentWidget.genXaction();
          var pathId = ":" + cdeRef.replace(/\//g, ":");
          pathId = pathId.replace("#", "%23");
          pathId = pathId.replace("{", "%7B");
          pathId = pathId.replace("}", "%7D");
          pathId = pathId.replace("<", "%3C");
          pathId = pathId.replace(">", "%3E");
          pathId = pathId.replace("+", "%2B");

          var me = this,
              url = webAppPath + "/api/repos/" + pathId + "/edit";

          window.open(url);

        },

        genXaction: function() {
          var gen = this.solution == null ? '' : this.solution;
          if(this.path != null) {
            if(gen.length > 0 && gen.substr(gen.length - 1, 1) != '/') {

              gen += '/';
            }

            gen += this.path;
          }
          if(this.action != null) {
            if(gen.length > 0 && gen.substr(gen.length - 1, 1) != '/') {

              gen += '/';
            }

            gen += this.action;
          }

          return gen;
        },

        getGUID: function() {
          if(this.GUID == null) {

            this.GUID = WidgetHelper.generateGUID();
          }

          return this.GUID;
        }
      });

      PentahoCdeComponent.newInstance = function(cderef, localizedFileName) {
        try {
          var widget = new PentahoCdeComponent();
          widget.GUID = WidgetHelper.generateGUID();
          widget.localizedName = localizedFileName;
          // used in GWT properties panel
          widget.iframe = true;
          widget.autoSubmit = true;
          widget.parameters = [];
          var selectedWidgetIndex = pentahoDashboardController.getSelectedWidget();
          widget.name = 'widget' + selectedWidgetIndex;
          widget.htmlObject = 'content-area-Panel_' + selectedWidgetIndex;
          var vals = XActionHelper.parseXaction(cderef);

          widget.xactionPath = cderef;
          widget.setSolutionPathAction(vals[0], vals[1], vals[2], new
              function() {
                this.onfinish = function() {
                  //widget.refreshParameters();
                  currentWidget = widget;

                  var details = XActionHelper.genXaction(widget.solution, widget.path, widget.action);
                  PropertiesPanelHelper.initPropertiesPanel(details);
                };
              });
        } catch (e) {
          alert(e);
        }
      };

      PentahoDashboardController.registerComponentForFileType("wcdf", PentahoCdeComponent);
      PentahoDashboardController.registerWidgetType(new PentahoCdeComponent());
    });

pen.require(["cde/widget-cde"], function() {
});
