wd = wd || {};
wd.helpers = wd.helpers || {};

wd.helpers.olap = {
    getServiceUrl: function(){
        return "/plugin/pentaho-cdf-dd/api/olap/";
    },

    getCubesUrl: function(){
        return "getCubes";
    },

    getCubeStructureUrl: function(){
        return "getCubeStructure";
    },

    getPaginatedLevelMembersUrl: function(){
        return "getPaginatedLevelMembers";
    },

    getLevelMembersStructureUrl: function(){
        return "getLevelMembersStructure";
    }
}