wd = wd || {};
wd.helpers = wd.helpers || {};

wd.helpers.views = {
    getViewsEndpoint: function(){
        return "/plugin/pentaho-cdf/api/views";
    },
    getSaveViewsEndpoint: function(){
        return getViewsEndpoint() + "/save";
    },
    getDeleteViewsEndpoint: function(){
        return getViewsEndpoint() + "/delete";
    },
    getListViewsEndpoint: function(){
        return getViewsEndpoint() + "/list";
    },
    getUrl: function(solution, file, path, view){
        var newPath = (solution + path + file).replace(/\//g, ':').replace(/\+/g,"%20");
        return webAppPath + "/api/repos/" + newPath + "/generatedContent?view=" + view.replace(/\+/g,"%20");
    }
}