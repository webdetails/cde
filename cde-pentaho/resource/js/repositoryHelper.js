wd = wd || {};
wd.helpers = wd.helpers || {};

wd.helpers.repository = {
    getRsourceUrl: function(){
        return "getResource";
    },

    getBaseSolutionPluginRoot: function(){
        return "/";
    },

    getWidgetsLocation: function(){
    	//widgets are stored in a plugin specific folder (currently it is /cde/widgets/)
    	return "/cde/widgets/"
    }

}