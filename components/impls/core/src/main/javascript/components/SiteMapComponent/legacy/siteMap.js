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
      item:
        "<li class='siteMapItem {{classes}}'>" +
        "<a href='{{link}}'>{{name}}</a></li>"
    },

    update : function() {
      var items = [],
          myself = this;

      if(typeof myself.siteMapSelectedParameter !== "undefined" && myself.siteMapSelectedParameter != "") {
        myself.selected = Dashboards.getParameterValue(myself.siteMapSelectedParameter);
      }

      myself.ph = $("#" + myself.htmlObject).empty();

      if(myself.ajaxUrl) {
        var opts = {url: myself.ajaxUrl};

        if(_.isEmpty(myself.parameters)) {
          opts.data = Dashboards.propertiesArrayToObject(myself.ajaxData);
        }
        myself.fetchItems(opts, function(items) {
          myself.renderList(myself.ph, items, 0);
        });
      } else {
        if(myself.siteMapParameter) {
          myself.renderList(myself.ph, Dashboards.getParameterValue(myself.siteMapParameter), 0);
        }
      }

      // mark as selected all ancestors
      myself.ph.find(".siteMapItem.siteMapSelected").parents(".siteMapItem").addClass("siteMapSelected");
      myself.ph.find(".siteMapItem.siteMapInitial").parents(".siteMapItem").addClass("siteMapInitial");

    },

    fetchItems: function (overrides, callback) {
      var myself = this;
      overrides = overrides || {};
      var ajaxOpts = {
        type: 'GET',
        success: function(json) {callback(json);},
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
              Dashboards.fireChange(myself.siteMapSelectedParameter, lid);
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
