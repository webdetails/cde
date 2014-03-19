wd = wd || {};
wd.helpers = wd.helpers || {};

wd.helpers.olap = {
    getServiceUrl: function(){
        return "/content/pentaho-cdf-dd/OlapUtils";
    },

    getCubesUrl: function(){
        return "?operation=GetOlapCubes";
    },

    getCubeStructureUrl: function(){
        return "?operation=GetCubeStructure";
    },

    getPaginatedLevelMembersUrl: function(){
        return "?operation=GetPaginatedLevelMembers";
    },

    getLevelMembersStructureUrl: function(){
        return "?operation=GetLevelMembersStructure";
    }
}