CacheCleanerComponent = BaseComponent.extend({
  /*
   * Max Depth:
   * 1. Catalog
   * 2. Cube
   * 3. Dimension
   * 4. Hierarchy
   * 5+ Member (might be quite deep)
   */
  maxDepth: 4,
  updateDelay: 200,
  breadcrumb: [],
  catalogs: [],
  dimensions: [],
  levelLabels: [
    "Catalog",
    "Cube",
    "Dimension",
    "Hierarchy",
    "Member"
  ],
  update: function() {
    var myself = this,
        handler = function(response) {
          return myself.handleResponse(response);
        };
    this.handler = handler;
    this.triggerQuery(this.handler);
  },

  triggerQuery: function(handler) {
    var catalog = this.breadcrumb[0],
        cube = this.breadcrumb[1],
        dimension = this.breadcrumb[2],
        hierarchy = this.breadcrumb[3],
        member = this.breadcrumb.length >= 4 ?
            this.breadcrumb[this.breadcrumb.length - 1]:
            null;
    /* Bigger breadcrumbs mean deeper searches. First level
     * is catalog, second is cube, third and beyond is a
     * dimension or a specific member thereof.
     */
    switch(this.breadcrumb.length){
      case 0: /* Nothing selected yet, get catalog list */
        this.cubeListing(handler);
        break;
      case 1: /* Catalog selected, we already have the cube list, so pass it directly */
        handler(this.catalogs[catalog.index]);
        break;
      case 2: /* Catalog and cube selected, we need the dimensions for the cube */
        this.cubeStructure(catalog.value,cube.value,handler);
        break;
      case 3: /* Catalog, cube and dimension picked, we already have the hierarchy list to pick from */
        handler(this.dimensions[dimension.index]);
        break;
      default: /* Else, we're interested in the children of a specific member */
        this.memberStructure(catalog.value,cube.value,member.value,handler);
    }
  },

  handleResponse: function(resp) {
    /* We expect certain properties to be available
     * on the response object, depending on the level
     * we're browsing at. From those properties, we can
     * determine what level the response refers to, and
     * we build a standardized (index, value, label)
     * object for each item in the response, which will
     * then be used to draw the list.
     */
    var opts, next;
    if (resp.catalogs) {
      this.catalogs = resp.catalogs;
      opts = resp.catalogs.map(function(e,i){
        return {
          index: i,
          value: e.name,
          label: e.name
        };
      });
    } else if (resp.cubes) {
      opts = resp.cubes.map(function(e,i){
        tokens = e.match(/.*\[name=(.*),id=(.*)\]/);
        return {
          index: i,
          value: tokens[2],
          label: tokens[1]
        };
      });
    } else if (resp.dimensions) {
      this.dimensions = resp.dimensions;
      opts = resp.dimensions.map(function(e,i){
        return {
          index: i,
          value: e.name,
          label: e.name
        };
      });  
    } else if (resp.hierarchies) {
      opts = resp.hierarchies.map(function(e,i){
        return {
          index: i,
          value: e.qualifiedName,
          label: e.name
        }
      });  
    }
    /* Hoorah, we have the item list,
     * and we can now display it.  
     */
    this.displayList(opts);
  },

  displayList: function(items) {
    var myself = this,
        $ph = $("<div class='cacheCleanerComponent'>").appendTo($("#" + this.htmlObject).empty()),
        $title = $("<li class='cacheItem title WDdataCell'>Your Selection</li>"),
        $list = $("<ul class='cacheItemList'>").appendTo($ph),
        title,
        levelIndex = Math.min(this.levelLabels.length,this.breadcrumb.length),
        levelLabel = this.levelLabels[levelIndex];

    /* Add a label indicating what level we're working at */
    if (this.breadcrumb.length < this.maxDepth) {
      $list.append("<li class='cacheItem WDdataCell'>Choose a "+levelLabel+ "</li>");
    }
    /* Now add the actual items for our current level */
    $.each(items, function(i,e){
      myself.drawActiveItem($list,e,i);
    });

    /* We only show the label for the selection only if
     * there's an actual selection, but we still need it
     * to exist for the animations.
     */
    $title.appendTo($list);
    if (!myself.breadcrumb.length) {
      $title.css('visibility','hidden');
    }
    this.title = $title;
    $("<div class='button'>Reset</div>").click(function(){
      myself.breadcrumb = [];
      myself.update();
    }).appendTo($title);

    /* Add inactive breadcrumb items */
    $.each(this.breadcrumb.slice().reverse(), function(i,e){
        myself.drawBreadCrumbItem($list,e,i);
    });

    /* Show the contents */
    setTimeout(function(){$ph.css('opacity','1')},1);

    /* please, sir, can we have a reference
     * to the list from outside this method?
     */
     this.list = $list;
  },

  drawBreadCrumbItem: function($ph,item,index) {
    var myself = this,
        $item = $("<li class='cacheItem inactive'>"),
        $refine;
    
    $item.text(item.label);
    $("<div>Unselect</div>").addClass('button').click(
      function(){
        myself.breadcrumb = myself.breadcrumb.slice(0,myself.breadcrumb.length - index - 1);
        $item.css('top',myself.title.position().top - $item.position().top);
        setTimeout(function(){myself.update();},myself.updateDelay);
      }).appendTo($item);
    $item.append("<div class='fauxButton'>URL</div>");
    $item.append("<div class='fauxButton'>Clean</div>");
    $item.appendTo($ph);
  },

  drawActiveItem: function($ph,item,index) {
    var myself = this,
        $item = $("<li class='cacheItem'>"),
        $refine;
    
    $item.text(item.label);

    $refine = $("<div class='button'>Refine</div>");
 
   /* We only want the refine button to be active if
    * we can drill down any further than we have
    */
    if (this.breadcrumb.length < this.maxDepth - 1) {
      $refine.addClass('button').click(function(){
        if(myself.breadcrumb.length < myself.maxDepth - 1) {
          myself.breadcrumb.push(item);
          $item.css('top',myself.title.position().top - $item.position().top);
          setTimeout(function(){myself.update();},myself.updateDelay);
        }
      });
    } else {
      $refine.addClass('fauxButton');
    }
    $refine.appendTo($item);
    $("<div class='button'>URL</div>").click(function(){
      myself.displayURL(item);
    }).appendTo($item);
    $("<div class='button'>Clean</div>").click(function(){
      myself.clearCache(item,this);
    }).appendTo($item);
    $item.appendTo($ph);
  },

  clearCache: function(item,target) {
    var $target = $(target);
    $.ajax({
      type: 'GET',
      url: this.getURL(item),
      success: function(response){
        var text = $target.text();
        $target.text('Done').addClass('success');
        setTimeout(function(){$target.text(text).removeClass('success')},1100);
      },
      error: function(response, xhr, err) {
        application.popupEngine.getPopup('close').show({
          header:'Something went wrong',
          content:'There was an issue clearing the cache. Please refer to the' + 
                ' server logs for more information. The server response was: ' + 
                err
        });
      }
    });
  },

  displayURL: function(item) {
    var myself = this,
        url = window.location.host + this.getURL(item),
        $title = $("<li class='cacheItem title WDdataCell'>Clear the cache with this URL:</li>"),
        $url = $("<li class='cacheItem'><textarea rows='2' cols='100'>"+this.getURL(item)+"</textarea></li>"),
        ta = $url.find('textarea').get(0);

 
    $("<div class='button'>Dismiss</div>").click(function() {
        $url.css('height','0');
        $url.css('min-height','0');
        $url.css('padding','0');
        $title.css('height','0');
        $title.css('min-height','0');
        $title.css('padding','0');
      setTimeout(function(){
        $url.remove();
        $title.remove();
      },myself.updateDelay);
    }).appendTo($title);

    if(this.$url){
      this.$url.remove()
      this.$urltitle.remove()
    }
    this.$url = $url;
    this.$urltitle = $title;
    this.list.prepend($url);
    this.list.prepend($title);
    ta.focus();
    ta.select();
  },

  getURL: function(item) {
    var webservice = 'MondrianCacheCleanService',
        methods = ["clearCatalog","clearCube","clearDimension","clearHierarchy"],
        parameters = ['catalog','cube','dimension','hierarchy'],
        p, param,
        urlParams = {},
        urlMethod;

    for (p = 0; p < parameters.length; p++ ) {
        if(this.breadcrumb.length <= p) break;
        param = parameters[p];
        urlParams[param] = this.breadcrumb[p].value;
    }
    urlParams[parameters[p]] = item.value;
    urlMethod = methods[p];
    urlMethod = '/pentaho/content/ws-run/' + webservice + '/' + urlMethod;
    return window.location.protocol + "//" + window.location.host + urlMethod + "?" +  $.param(urlParams);
  },

  cubeListing: function(callback) {
    $.getJSON("OlapUtils", {
        operation:"GetOlapCubes"
      },callback);
  },
  cubeStructure: function(catalog, cube, callback){
    $.getJSON("OlapUtils", {
        operation:"GetCubeStructure",
        catalog: catalog,
        cube: cube
      }, callback);
  },
  memberStructure: function(catalog, cube, member, callback){
    $.getJSON("OlapUtils", {
        operation:"GetLevelMembersStructure",
        catalog: catalog,
        cube: cube,
        member: member,
        direction: "down"
      }, callback);
  }
});

