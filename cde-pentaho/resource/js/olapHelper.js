wd = wd || {};
wd.helpers = wd.helpers || {};

wd.helpers.olap = {
    getService: function(){
        return "/content/pentaho-cdf-dd/OlapUtils";
    },

    getCubesUrl: function(){
        return "?operation=GetCubes";
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