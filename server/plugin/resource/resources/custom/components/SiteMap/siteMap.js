
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

    update : function() {

        if(typeof this.siteMapSelectedParameter !== "undefined" && this.siteMapSelectedParameter != ""){
            this.selected = Dashboards.getParameterValue( this.siteMapSelectedParameter )
        }

        var paramVal = Dashboards.getParameterValue( this.siteMapParameter ); // Evaluate the parameter

        // Dashboards.log("Sitemap structure length: " + siteMapParameter.length + "; Selected: " + this.selected);
        this.ph = $("#" + this.htmlObject).empty();

        this.generateUl(this.ph, paramVal , 0);

        // mark as selected all ancestors
        this.ph.find("li.siteMapSelected").parents("li").addClass("siteMapSelected");
        this.ph.find("li.siteMapInitial").parents("li").addClass("siteMapInitial");

    },

    generateUl: function(ph,arr, level){

        var myself=this;
        var ul = $('<ul class="siteMap siteMapLevel'+level+'" ></ul>');

        $.each(arr,function(n,l){
            var li = $("<li></li>").addClass(l.classes || "").appendTo(ul);
            var a = $('<a>'+l.name+'</a>').appendTo(li);
            if(l.link){
                a.attr("href",l.link);
            }
            else if(typeof l.action === "function"){
                
                // Add a click action to this
                a.click(function(){
             
                    l.action(li);
                    // Now: Remove all previous selected classes and add this
                    myself.ph.find("li.siteMapSelected").removeClass("siteMapSelected");
                    $(this).parents("li").addClass("siteMapSelected");
                    Dashboards.fireChange(myself.siteMapSelectedParameter,typeof l.id !== "undefined"?l.id:l.name);

                });
            }
                
        
            // Is this one selected? Later we'll also need to mark all ancestor with a class
            if(typeof(l.id) !== "undefined"? l.id == myself.selected: l.name == myself.selected){
                li.addClass("siteMapSelected siteMapInitial");
            }

            if(l.sublinks && l.sublinks.length > 0)
                myself.generateUl(li, l.sublinks, level + 1);
        });

        ul.appendTo(ph);

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

