wd = wd || {};
wd.helpers = wd.helpers || {};

wd.helpers.cggHelper = {
    getCggDrawUrl: function(){
        return Dashboards.getWebAppPath() + "/plugin/cgg/api/services/draw";
    }
}