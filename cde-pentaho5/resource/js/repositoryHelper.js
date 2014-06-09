wd = wd || {};
wd.helpers = wd.helpers || {};

wd.helpers.repository = {
    getRsourceUrl: function(){
        return "res";
    },

    getBaseSolutionPluginRoot: function(){
        return "/public/";
    },

    getWidgetsLocation: function(){
    	//widgets are stored in a plugin specific folder (currently it is /public/cde/widgets/)
    	return "/public/cde/widgets/"
    }

}