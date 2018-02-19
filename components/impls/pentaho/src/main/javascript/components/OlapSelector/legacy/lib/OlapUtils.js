wd = wd || {};
wd.utils = wd.utils || {};
wd.utils.OlapUtils = function(spec){
    
    
    var defaults = {
        
        url: wd.helpers.olap.getServiceUrl(),
        extraParams: {}
    //catalog: "FoodMart/FoodMart.xml",
    //cube: "Sales",
    //hierarchy: "Product"
    };



    var myself = {};
    myself.options = $.extend({},defaults,spec);
    
    /* Private */
    var isInitialized = false;
    var catalog = myself.options.catalog;
    var cube = myself.options.cube;
    var allSelectedObjects = [];
    
    var catalogs = [];
    var cubeStructureCache = {};
    
    var olapOperations = {
        GET_OLAP_CUBES: wd.helpers.olap.getCubesUrl(),
        GET_CUBE_STRUCTURE: wd.helpers.olap.getCubeStructureUrl(),
        GET_PAGINATED_LEVEL_MEMBERS: wd.helpers.olap.getPaginatedLevelMembersUrl(),
        GET_MEMBER_STRUCTURE: wd.helpers.olap.getLevelMembersStructureUrl()
    };
    
    
    myself.initialize = function(){
    
        if(!isInitialized){
            myself.initCatalogs();
            isInitialized = true;
        }
        
        
    }
    
    myself.setOptions = function(_args){
        myself.options = $.extend(myself.options,_args);
    }

    myself.setCatalog = function (catalog) {
        myself.catalog = catalog;
        myself.options.catalog = catalog;
    }

    myself.initCatalogs = function(){
        
        wd.debug("Getting info from cube");
       
        var res = myself.callOlapUtilsSync({

            operation: olapOperations.GET_OLAP_CUBES
        });

        catalogs = res.catalogs;

        wd.info("[OlapUtils] Successfully got catalog information");
    
    }
    
    myself.resetCubeStructure = function(_args){
    
        var catalog = myself.getSelectedCatalogName(_args);
        var cube = myself.getSelectedCubeName(_args);
        var cacheKey = catalog + "::" + cube;

        if(cacheKey){
            delete cubeStructureCache[cacheKey];
        }
        else{
            cubeStructureCache = {};
        }
        
        return true;
    }
    
    
    myself.getCatalogs = function(){
        
        return catalogs;
        
    }
    
    myself.getCubes = function(_args){
        
        var catalog = myself.getSelectedCatalogName(_args);
        var entry =  _.find(catalogs,function(c){
            return c.schema.indexOf(catalog)>=0;
        });
            
        return entry?entry.cubes:null;
    }


    myself.getCubeStructure = function(_args){

        var catalog = myself.getSelectedCatalogName(_args);
        
        var cube = myself.getSelectedCubeName(_args);
        var cacheKey = catalog + "::" + cube;
        
        if(!catalog || !cube){
            wd.error("Catalog or Cube not specified");
            return null;
        }
        
        if(cubeStructureCache[cacheKey]){
            return cubeStructureCache[cacheKey];
        }
        
        
        var params = {
            operation: olapOperations.GET_CUBE_STRUCTURE,
            catalog: catalog,
            cube: cube
        // jndi: this.jndi
        };
        
        // make a sync call
        var result = myself.callOlapUtilsSync(params);
        
        cubeStructureCache[cacheKey] = result; 
        return result;
        
    }

    myself.getCube = function(_args){
        return myself.getCubeStructure(_args);
    }

    myself.getDimensions = function(_args){

        var cubeStructure = myself.getCubeStructure(_args);
        return cubeStructure!=null ? cubeStructure.dimensions:null;
        
    }
    
    myself.getDimension = function(_args){
        
        var dimension = myself.getSelectedDimensionName(_args);
        var cubeStructure = myself.getCubeStructure(_args);
        var d = _.find(cubeStructure.dimensions,function(d){
            return d.name == dimension
        });
        return d;
    }

    myself.getHierarchies = function(_args){
        
        var d = myself.getDimension(_args);
        return d!=null?d.hierarchies:null;
        
    }

    myself.getHierarchy = function(_args){
        
        var hierarchyName = myself.getSelectedHierarchyName(_args);
        var h = _.find(myself.getHierarchies(_args),function(hier){
            return hier.name == hierarchyName
        });
        return h;
    }


    myself.getLevels = function(_args){

        var h = myself.getHierarchy(_args);
        return h!=null?h.levels:null;
    }
    
    myself.getLevel = function(_args){
        var levelName = myself.getSelectedLevelName(_args);
        var l = _.find(myself.getLevels(_args),function(level){
            return level.name == levelName
        });
        return l;
    }

    myself.getPaginatedLevelMembers = function(_args, callback){

        var defaults = {
            operation: olapOperations.GET_PAGINATED_LEVEL_MEMBERS,
            startMember: "",
            pageStart: 0,
            pageSize: 100,
            searchTerm: "",
            context: ""
        } 

        var params = $.extend({},defaults,_args);
        
        params.catalog = myself.getSelectedCatalogName(_args);
        params.cube = myself.getSelectedCubeName(_args);
         
        
        var l = myself.getLevel(_args);
        params.level = l.qualifiedName;


        myself.callOlapUtils(params, function(json){
            
            var members = json.members;
            wd.debug("Got results for paginatedLevelMembers: " + _(members).pluck("name").join(", "));
            

            if(callback){
                callback(json);
            }
            
        });

    };
    
    myself.getOlapUtilsUrl = function(){
        
        return myself.options.url
        
    };
    
    
    myself.getSelectedCatalogName = function(_args){

        var catalog = $.extend({},myself.options,_args).catalog;
        return catalog;
        
    };


    myself.getSelectedCubeName = function(_args){
        return $.extend({},myself.options,_args).cube;
    };

    myself.getSelectedDimensionName = function(_args){
        return $.extend({},myself.options,_args).dimension;
    };
    
    myself.getSelectedHierarchyName = function(_args){
        
        // If we don't explicitly select a hierarchy, we'll use the default
        var h = $.extend({},myself.options,_args).hierarchy;
        if(h == null){
            
            h = myself.getHierarchies(_args)[0].name;
            myself.options.hierarchy = h;
            wd.info("No hierarchy explicitly selected - setting the default one to '" + h + "'");
        }
        
        return h;
        
    };

    myself.getSelectedLevelName = function(_args){
        return $.extend({},myself.options,_args).level;
    };
    
    myself.callOlapUtilsSync = function(params) {
        
        return myself.callOlapUtils(params, undefined, undefined, true);
        
    }
    
    myself.callOlapUtils = function(params, callback, errorCallback, sync) {
        var myself = this;
        
        var ret;
        
        $.ajax({
            type: "GET",
            url: myself.getOlapUtilsUrl() + params.operation,
            data: $.extend({}, myself.options.extraParams, params),
            dataType: "json",
            success: function(json){
                if(json && json.status == "true" && json.result){
                    
                    // sync only sets the value
                    if(sync){
                        ret = json.result
                    }
                    else{
                        callback(json.result);                        
                    }
                }
                else {
                    if(typeof(errorCallback) != 'function' ) errorCallback = alert;
                    return errorCallback(json);
                    
                }
            },
            async: !sync
        });
        
        return ret;
          
    };

    

    /* Constructor args */
    myself.initialize();
    wd.info("OlapUtils initialized!");


    return myself;
    
    
}
    
    
    
    
