CdaCacheCleanerComponent = BaseComponent.extend({

  filelist: [],
  filemap: {},
  /*
   * Max Depth:
   * 0. Unlimited
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
    var level,
        member;
    /* Either we're at the surface level, and we need a 
     * listing of the available dashboards, or we're at
     * a level deeper than that, and we just need to
     * extract the correct branch node for the selection
     */
    switch(this.breadcrumb.length){
      case 0:
        /* Nothing selected yet, get file list */
        this.getDashboardList(handler);
        break;
      default:
        /* Use the breadcrumb to drill down 
         * the stored file hierarchy.
         */
        level = this.filemap;
        $.each(this.breadcrumb,function(i,e){
          level = level[e.value];
        });
        handler(level);
    }
  },

  handleResponse: function(resp) {

        /* Object.keys already guarantees that
         * we're filtering through hasOwnProperty()
         */
        opts = Object.keys(resp).map(function(e,i) {
          return {
            index: i,
            value: typeof resp[e] == "string"? resp[e] : e,
            label: e,
            type: typeof resp[e] == "string" ? "leaf" : "branch"
          };
        });
        this.displayList(opts);
  },

  displayList: function(items) {
    var myself = this,
        $ph = $("<div class='cacheCleanerComponent'>").appendTo($("#" + this.htmlObject).empty()),
        $title = $("<li class='cacheItem title WDdataCell'>Your Selection</li>"),
        $list = $("<ul class='cacheItemList'>").appendTo($ph),
        title,
        levelLabel = "Dashboard";

    /* Add a label indicating what level we're working at */
    if (!this.maxDepth || this.breadcrumb.length < this.maxDepth) {
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
        $refine,
        $clean,
        $url;
    
    $item.text(item.label);

    $refine = $("<div class='button'>Browse</div>");
    $clean = $("<div class='button'>Clean</div>");
    $url = $("<div class='button'>URL</div>");
   /* We only want the refine button to be active if
    * we can drill down any further than we have.
    * Reversely, we only want Clean/URL available
    * for the leaf nodes.
    */
    if (item.type == 'branch') {
      $refine.click(function(){
        if(!this.maxDepth || myself.breadcrumb.length < myself.maxDepth - 1) {
          myself.breadcrumb.push(item);
          $item.css('top',myself.title.position().top - $item.position().top);
          setTimeout(function(){myself.update();},myself.updateDelay);
        }
      });
      $url.addClass('fauxButton');
      $clean.addClass('fauxButton');
    } else {
      $refine.addClass('fauxButton');
      $clean.click(function(){
        myself.clearCache(item,this);
      });
      $url.click(function(){
        myself.displayURL(item);
      })
    }
    $refine.appendTo($item);
    $url.appendTo($item);
    $clean.appendTo($item);
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
    var webservice = 'DashboardCacheCleanService',
        methods = ["clearDashboard"],
        parameters = ['dashboard'],
        p, param,
        urlParams = {},
        urlMethod;

    for (p = 0; p < parameters.length; p++ ) {
        if(this.breadcrumb.length <= p) break;
        param = parameters[p];
        urlParams[param] = item.value;
    }
    urlParams[parameters[0]] = item.value;
    urlMethod = methods[0];
    urlMethod = '/pentaho/content/ws-run/' + webservice + '/' + urlMethod;
    return window.location.protocol + "//" + window.location.host + urlMethod + "?" +  $.param(urlParams);
  },

  getDashboardList: function(handler){
    var myself = this,
        addEntry = function(path, map) {
          var entry = path.split('/'),
              i;
          for (i = 0; i < entry.length - 1; i++) {
            if (!map[entry[i]]) map[entry[i]] = {};
            map = map[entry[i]];
          }
          map[entry[i]] = path;
        }
    
    $.get("/pentaho/content/ws-run/DashboardCacheCleanService/getDashboardsList",
      function(resp){
        var results  = JSON.parse($("return",resp).text()).result,
            resultMap = {},
            i;
        for (i = 0; i < results.length; i ++) {
          addEntry(results[i],resultMap);
        }

        myself.filelist = results;
        myself.filemap = resultMap[""];
        handler(resultMap[""]);
      });
  }
});

