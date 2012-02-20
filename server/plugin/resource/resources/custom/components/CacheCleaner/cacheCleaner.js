CacheCleanerComponent = BaseComponent.extend({
  /*
   * Max Depth:
   * 1. Catalog
   * 2. Cube
   * 3. Dimension
   * 4. Hierarchy
   * 5+ Member (at whatever depth limit is set)
   */
  maxDepth: 4,
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
    /*
     * Bigger breadcrumbs mean deeper searches. First level
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
    var opts, next;
    if (resp.catalogs) {
      this.catalogs = resp.catalogs;
      opts = resp.catalogs.map(function(e,i){
        return {
          index: i,
          value: e.schema,
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
    this.displayList(opts);
  },

  displayList: function(items) {
    var myself = this,
        $ph = $("<div class='cacheCleanerComponent'>").appendTo($("#" + this.htmlObject).empty()),
        $title = $("<div class='title WDdataCell'>Your Selection</div>"),
        $list = $("<ul class='cacheItemList'>").appendTo($ph),
        title,
        levelIndex = Math.min(this.levelLabels.length,this.breadcrumb.length),
        levelLabel = this.levelLabels[levelIndex];

    /* Add a label indicating what level we're working at */
    if (this.breadcrumb.length < this.maxDepth) {
      $list.append("<li class='cacheItem WDdataCell'>Choose a "+levelLabel+ "</li>");
    }
    /* Now add the actual items for our current level*/
    $.each(items, function(i,e){
      myself.drawActiveItem($list,e,i);
    });

    /* Show a label for the selection, but only if there's something to show */
    if (myself.breadcrumb.length) {
      $title.appendTo($list);
    }
    $("<div class='button'>Reset</div>").click(function(){
      myself.breadcrumb = [];
      myself.update();
    }).appendTo($title);

    /* Add inactive breadcrumb items */
    $.each(this.breadcrumb.slice().reverse(), function(i,e){
        myself.drawBreadCrumbItem($list,e,i);
    });
  },

  drawBreadCrumbItem: function($ph,item,index) {
    var myself = this,
        $item = $("<li class='cacheItem inactive'>"),
        $refine;
    
    $item.text(item.label);
    $("<div>Refine</div>").addClass(index == 0 ?
        'fauxButton':
        'button').click(
      function(){
        myself.breadcrumb = myself.breadcrumb.slice(0,myself.breadcrumb.length - index);
        myself.update();
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

    $("<div class='button'>Refine</div>").click(function(){
      if(myself.breadcrumb.length < myself.maxDepth - 1) {
        myself.breadcrumb.push(item);
        myself.update();
      }
    }).appendTo($item);
    $("<div class='button'>URL</div>").click(function(){
      myself.getURL(item);
    }).appendTo($item);
    $("<div class='button'>Clean</div>").click(function(){
      myself.clearCache(item);
    }).appendTo($item);
    $item.appendTo($ph);
  },

  clearCache: function(item) {
    Dashboards.log('Requested cache clearing for ' + item.label);
  },
  getURL: function(item) {
    Dashboards.log('Requested cache clearing for ' + item.label);
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

