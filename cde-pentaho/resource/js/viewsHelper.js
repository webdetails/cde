wd = wd || {};
wd.helpers = wd.helpers || {};

wd.helpers.views = {
    getViewsEndpoint: function(){
        return "/content/pentaho-cdf/Views";
    },
    getSaveViewsEndpoint: function(){
        return getViewsEndpoint() + "?method=saveView";
    },
    getDeleteViewsEndpoint: function(){
        return getViewsEndpoint() + "?method=deleteView";
    },
    getListViewsEndpoint: function(){
        return getViewsEndpoint() + "?method=listViews";
    },
    getUrl: function(solution, file, path, view){
        /* $.params uses + to encode spaces, we need
         * to use uri encoding-style "%20" instead
         */
        var params = $.param({
            file: this.file,
            path: this.path,
            solution: this.solution,
            view: this.name
        }).replace(/\+/g,"%20");
        if(/\.(wcdf|cdfde)$/.test(this.file)) {
            /* CDE mode*/
            return webAppPath + "/content/pentaho-cdf-dd/Render?" + params
        } else {
            /* CDF mode */
            return webAppPath + "/content/pentaho-cdf/renderWcdf?" + params
        }
    }
}