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

define([
  'cdf/components/BaseComponent',
  'cdf/lib/jquery',
  'amd!cdf/lib/underscore',
  'cdf/lib/mustache',
  'cdf/dashboard/Utils'],
  function(BaseComponent, $, _, Mustache, Utils) {

  /**
   *
   * SiteMapComponent
   *
   * Generates a nested structure of ul's based on a parameter
   *
   */

  var SiteMapComponent = BaseComponent.extend({

    ph: undefined,
    selected: "UNUSEDPARAM!@#$",

    templates: {
      list: "<ul class='siteMap siteMapLevel{{level}}'></ul>",
      item: "<li class='siteMapItem {{classes}}'><a href='{{link}}'>{{name}}</a></li>"
    },

    update: function() {
      var items = [],
          myself = this;

      if(typeof myself.siteMapSelectedParameter !== "undefined" && myself.siteMapSelectedParameter != "") {
        myself.selected = myself.dashboard.getParameterValue(myself.siteMapSelectedParameter);
      }

      myself.ph = $("#" + myself.htmlObject).empty();

      if(myself.ajaxUrl) {
        var opts = {url: myself.ajaxUrl};

        if(_.isEmpty(myself.parameters)) {
          opts.data = Utils.propertiesArrayToObject(myself.ajaxData);
        }
        myself.fetchItems(opts, function(items) {
          myself.renderList(myself.ph, items, 0);
        });
      } else {
        if(myself.siteMapParameter) {
          myself.renderList(myself.ph, myself.dashboard.getParameterValue(myself.siteMapParameter), 0);
        }
      }

      // mark as selected all ancestors
      myself.ph.find(".siteMapItem.siteMapSelected").parents(".siteMapItem").addClass("siteMapSelected");
      myself.ph.find(".siteMapItem.siteMapInitial").parents(".siteMapItem").addClass("siteMapInitial");

    },

    fetchItems: function(overrides, callback) {
      var myself = this;
      overrides = overrides || {};
      var ajaxOpts = {
        type: 'GET',
        success: function(json) { callback(json); },
        dataType: 'json',
        async: true
      };
      ajaxOpts = _.extend({}, ajaxOpts, overrides);
      $.ajax(ajaxOpts);
    },

    renderList: function(ph, arr, level) {

      var myself = this;
      var list = $(Mustache.render(myself.templates.list, {level: level}));

      for(var i = -1, len = arr.length; ++i < len;) {

        var l = arr[i];
        var lname = l.name || l.id || "";
        var lid = l.id || l.name;

        var item = $(Mustache.render(
          myself.templates.item,
          {name: lname, link: l.link, classes: l.classes || ""}));

        if(!l.link && typeof l.action === "function") {
          // Add a click action to this
          item.find('a').click(function() {
            l.action(item);
            // Now: Remove all previous selected classes and add this
            myself.ph.find(".siteMapItem.siteMapSelected").removeClass("siteMapSelected");
            $(this).parents(".siteMapItem").addClass("siteMapSelected");

            if(!_.isEmpty(lid)) {
              myself.dashboard.fireChange(myself.siteMapSelectedParameter, lid);
            }
          });
        }

        // Is this one selected? Later we'll also need to mark all ancestor with a class
        if(lid == myself.selected) {
          item.addClass("siteMapSelected siteMapInitial");
        }

        if(l.sublinks && l.sublinks.length > 0) {
          myself.renderList(item, l.sublinks, ++level);
        }
        item.appendTo(list);
      }
      list.appendTo(ph);

    }

  /*
    ,
    siteMapParameter: [
    {
      name: "Link1",
      link: "http://www.webdetails.pt",
      sublinks: []
    },
    {
      name: "Link2",
      link: undefined,
      sublinks: [
      {
        name: "Sublink 1",
        link: "www.google.com"
      },
      {
        name: "Sublink 2",
        link: "www.mozilla.com"
      }
      ]
    },
    {
      name: "Link3",
      link: undefined,
      sublinks: [
      {
        name: "Sublink 31",
        sublinks: [
        {
          name: "Subsublink 311",
          link: "http://www.google.com"
        },
        {
          name: "Sublink 312",
          link: "http://www.mozilla.com"
        }
        ]
      },
      {
        name: "Sublink 32",
        link: "http://www.google.com",
        sublinks: [
        {
          name: "Subsublink 321",
          link: "http://www.google.com"
        },
        {
          name: "Sublink 322",
          link: "http://www.mozilla.com"
        }
        ]
      },

      ]
    }
    ]*/
  });

  return SiteMapComponent;

});
