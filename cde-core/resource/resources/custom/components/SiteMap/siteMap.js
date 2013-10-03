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
      list: Mustache.compile(
        "<ul class='siteMap siteMapLevel{{level}}' ></ul>"
      ),
      item: Mustache.compile(
        "<li class='siteMapItem {{classes}}''>" +
        "  <a {{#link}}href='{{link}}'{{/link}}>{{name}}</a>" +
        "</li>"
      )

    },

    update : function() {
        var items = [],
            myself = this;

        if(typeof this.siteMapSelectedParameter !== "undefined" && this.siteMapSelectedParameter != ""){
            this.selected = Dashboards.getParameterValue( this.siteMapSelectedParameter )
        }

        // Dashboards.log("Sitemap structure length: " + siteMapParameter.length + "; Selected: " + this.selected);
        this.ph = $("#" + this.htmlObject).empty();

        if (this.ajaxUrl){
          var opts = { 
            url: this.ajaxUrl
          }
          if ( _.isEmpty( this.parameters) ){
            opts.data = Dashboards.propertiesArrayToObject(this.ajaxData);
          }
          myself.fetchItems( opts, function (items){
            myself.renderList( myself.ph, items, 0);
          });
        } else if (this.siteMapParameter){
          this.renderList( this.ph, Dashboards.getParameterValue( this.siteMapParameter ) , 0 );
        }

        // mark as selected all ancestors
        this.ph.find(".siteMapItem.siteMapSelected").parents(".siteMapItem").addClass("siteMapSelected");
        this.ph.find(".siteMapItem.siteMapInitial").parents(".siteMapItem").addClass("siteMapInitial");

    },

    fetchItems: function (overrides, callback){
      var myself = this;
      overrides = overrides || {};
      var ajaxOpts = {
        type: 'GET',
        success: function(json) {
          callback(json);
        }, 
        dataType: 'json',
        async: true
      };
      ajaxOpts = _.extend( {}, ajaxOpts, overrides);
      $.ajax( ajaxOpts );
    },

    renderList: function(ph, arr, level){

        var myself=this;
        var list = $( myself.templates.list({level: level}) );

        $.each( arr, function(n,l){
            var item = $( myself.templates.item({
              name: l.name || l.id || "",
              link: l.link,
              classes: l.classes || ""    
            }));
            if(!l.link && typeof l.action === "function"){ 
                // Add a click action to this
                item.find('a').click(function(){
                    l.action(item);
                    // Now: Remove all previous selected classes and add this
                    myself.ph.find(".siteMapItem.siteMapSelected").removeClass("siteMapSelected");
                    $(this).parents(".siteMapItem").addClass("siteMapSelected");
                    Dashboards.fireChange(myself.siteMapSelectedParameter,typeof l.id !== "undefined"?l.id:l.name);
                });
            }
                
            // Is this one selected? Later we'll also need to mark all ancestor with a class
            if(typeof(l.id) !== "undefined"? l.id == myself.selected: l.name == myself.selected){
                item.addClass("siteMapSelected siteMapInitial");
            }

            if(l.sublinks && l.sublinks.length > 0)
                myself.renderList(item, l.sublinks, level + 1);

            item.appendTo(list);
        });
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

