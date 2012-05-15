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
        var self = this;
        var opts = Object.keys(resp).map(function(e,i) {
          var val = typeof resp[e] == "string"? resp[e] : e;
          return {
            index: i,
            value: val,
            label: self.getLabel(val),
            type: typeof resp[e] == "string" ? "leaf" : "branch"
          };
        });
        this.displayList(opts);
  },
  
  getLabel: function(path) {
      //find('repository file[name=cdc-presentation]').attr('description');
      var selector = 'repository';
      var pathElements = path.split('/');
      for(var i=0; i<pathElements.length;i++){
        var fileName = pathElements[i];
        if( fileName != ''){
          if(fileName.endsWith('.cdfde')){//won't be in repository
            fileName = fileName.slice(0,-5) + 'wcdf'; //cdfde.len
          }
          selector += ' file[name="' + fileName + '"]';
        }
      }
      var result = this.repository.find(selector).attr('localized-name');
      if(result) return result;
      else return pathElements[pathElements.length - 1];
  },

  displayList: function(items) {
    var myself = this,
        $ph = $("<div class='cacheCleanerComponent'>").appendTo($("#" + this.htmlObject).empty()),
        $title = $("<div class='breadcrumb'></div>"),
        $breadcrumbHolder = $("<div class='breadcrumbHolder'></div>").text('Your Selection').appendTo($title);
        $list = $("<ul class='cacheItemList'>"),
        title,
        levelLabel = "Dashboard",
        $header = $("<div class='WDdataCell'></div>"),
        $container = $("<div class=WDdataCell2></div>");

    /* Add external container */
    $list.appendTo($container);
    $container.appendTo($ph);

    /* Add a label indicating what level we're working at */
    if (!this.maxDepth || this.breadcrumb.length < this.maxDepth) {
      var header = $('<div class="headerTitle">'+levelLabel +'</div>');
      header.append('<div class="headerSelection">(select a '+levelLabel.toLowerCase() + ')</div>');
      $header.append(header);
        $ph.prepend($header);
    }
    /* Now add the actual items for our current level */
    $.each(items, function(i, e){
      myself.drawActiveItem($list, e, i);
    });

    /* We only show the label for the selection only if
     * there's an actual selection, but we still need it
     * to exist for the animations.
     */
    $title.prependTo($container);
    this.title = $title;
    $("<div class='button'>Reset</div>").click(function(){
      myself.breadcrumb = [];
      myself.update();
    }).appendTo($title);
    
    if (!myself.breadcrumb.length) {
      $title.find('.button').addClass('fauxButton');
    }
    

    /* Add inactive breadcrumb items */
    $.each(this.breadcrumb.slice(), function(i,e){
        myself.drawBreadCrumbItem($breadcrumbHolder,e,i);
    });

    /* Show the contents */
    setTimeout(function(){$ph.css('opacity','1')},1);

    /* please, sir, can we have a reference
     * to the list from outside this method?
     */
     this.list = $ph;
  },

  drawBreadCrumbItem: function($ph,item,index) {
    if($ph.text() == "Your Selection") {
      $ph.text(item.label);
    }
    else {
      $ph.text($ph.text() + ' > '+item.label);
    }
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
        $target.text('Done').addClass('done');
        setTimeout(function(){$target.text(text).removeClass('done')},1100);
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
        $title = $("<div class='title WDdataCell'>URL</div>"),
        $url = $("<div class='WDdataCell2'><textarea readonly='readonly'r>"+this.getURL(item)+"</textarea></div>"),
        $breadcrumbHolder = $("<div class='breadcrumbHolder'>Your Selection</div>").prependTo($("<div class='breadcrumb'> </div>").prependTo($url)),
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
    }).appendTo($breadcrumbHolder.parent());
    
    $.each(this.breadcrumb.slice(), function(i,e){
        myself.drawBreadCrumbItem($breadcrumbHolder,e,i);
    });
    
    $breadcrumbHolder.text($breadcrumbHolder.text()+' > '+item.label);

    if(this.$url){
      this.$url.remove();
      this.$urltitle.remove();
    }
    this.$url = $url;
    this.$urltitle = $title;
    this.list.append($title);
    this.list.append($url);
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
        },
        getDashboardsList = function(){
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
        };
    
    $.get("/pentaho/SolutionRepositoryService?component=getSolutionRepositoryDoc",
      function(resXml){
        myself.repository = $(resXml);        
        getDashboardsList();
      },"xml");

  }
});

