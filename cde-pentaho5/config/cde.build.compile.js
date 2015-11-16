/*
 * requirejs configuration file used to build the compiled/minified CDF js files.
 * Based on https://github.com/jrburke/r.js/blob/master/build/example.build.js
 */

({
  //The directory path to save the output. All relative paths are relative to the build file.
  dir: "../bin/scriptOutput",

  //As of RequireJS 2.0.2, the dir above will be deleted before the
  //build starts again. If you have a big build and are not doing
  //source transforms with onBuildRead/onBuildWrite, then you can
  //set keepBuildDir to true to keep the previous dir. This allows for
  //faster rebuilds, but it could lead to unexpected errors if the
  //built code is transformed in some way.
  keepBuildDir: false,

  //By default, all modules are located relative to this path. If appDir is set, then
  //baseUrl should be specified as relative to the appDir.
  baseUrl: ".",

  //How to optimize all the JS files in the build output directory.
  optimize: "uglify2",

  //Allow CSS optimizations. Allowed values:
  //- "standard": @import inlining and removal of comments, unnecessary
  //whitespace and line returns.
  //Removing line returns may have problems in IE, depending on the type
  //of CSS.
  //- "standard.keepLines": like "standard" but keeps line returns.
  //- "none": skip CSS optimizations.
  //- "standard.keepComments": keeps the file comments, but removes line
  //returns.  (r.js 1.0.8+)
  //- "standard.keepComments.keepLines": keeps the file comments and line
  //returns. (r.js 1.0.8+)
  //- "standard.keepWhitespace": like "standard" but keeps unnecessary whitespace.
  optimizeCss: "standard",

  //The top level directory that contains your app. If this option is used
  //then it assumed your scripts are in a subdirectory under this path.
  //If this option is specified, then all the files from the app directory
  //will be copied to the dir: output area, and baseUrl will assume to be
  //a relative path under this directory.
  appDir: "../build-res/module-scripts",

  //Introduced in 2.1.3: Some situations do not throw and stop the optimizer
  //when an error occurs. However, you may want to have the optimizer stop
  //on certain kinds of errors and you can configure those situations via
  //this option
  throwWhen: {
    //If there is an error calling the minifier for some JavaScript,
    //instead of just skipping that file throw an error.
    optimize: true
  },

  //Set paths for modules. If relative paths, set relative to baseUrl above.
  //If a special value of "empty:" is used for the path value, then that
  //acts like mapping the path to an empty file. It allows the optimizer to
  //resolve the dependency to path, but then does not include it in the output.
  //Useful to map module names that are to resources on a CDN or other
  //http: URL when running in the browser and during an optimization that
  //file should be skipped because it has no dependencies.
  paths: {
    'requireLib': 'require',
    'common-ui/util/URLEncoder': 'empty:', // NewMapComponent

    // map cdf dependencies to empty files, skipping these
    'cdf/components/BaseComponent': 'empty:',
    'cdf/components/UnmanagedComponent': 'empty:',
    'cdf/dashboard/Utils': 'empty:',
    'cdf/AddIn': 'empty:',
    'cdf/Dashboard.Clean': 'empty:',
    'cdf/Logger': 'empty:',
    'cdf/lib/Base': 'empty:',
    'cdf/lib/base64': 'empty:',
    'cdf/lib/jquery': 'empty:',
    'cdf/lib/mustache': 'empty:',
    'cdf/lib/OpenLayers': 'empty:',
    'cdf/lib/OpenStreetMap': 'empty:'
  },

  //By default all the configuration for optimization happens from the command
  //line or by properties in the config file, and configuration that was
  //passed to requirejs as part of the app's runtime "main" JS file is *not*
  //considered. However, if you prefer the "main" JS file configuration
  //to be read for the build so that you do not have to duplicate the values
  //in a separate configuration, set this property to the location of that
  //main JS file. The first requirejs({}), require({}), requirejs.config({}),
  //or require.config({}) call found in that file will be used.
  //As of 2.1.10, mainConfigFile can be an array of values, with the last
  //value's config take precedence over previous values in the array.
  mainConfigFile: '../build-res/requireCfg.js',

  //If using UglifyJS2 for script optimization, these config options can be
  //used to pass configuration values to UglifyJS2.
  //For possible `output` values see:
  //https://github.com/mishoo/UglifyJS2#beautifier-options
  //For possible `compress` values see:
  //https://github.com/mishoo/UglifyJS2#compressor-options
  uglify2: {
    output: {
      max_line_len: 80,
      beautify: false
    },
    warnings: true,
    mangle: true
  },

  //If set to true, any files that were combined into a build bundle will be
  //removed from the output folder.
  removeCombined: true,

  //By default, comments that have a license in them are preserved in the
  //output when a minifier is used in the "optimize" option.
  //However, for a larger built files there could be a lot of
  //comment files that may be better served by having a smaller comment
  //at the top of the file that points to the list of all the licenses.
  //This option will turn off the auto-preservation, but you will need
  //work out how best to surface the license information.
  //NOTE: As of 2.1.7, if using xpcshell to run the optimizer, it cannot
  //parse out comments since its native Reflect parser is used, and does
  //not have the same comments option support as esprima.
  preserveLicenseComments: false,

  //Introduced in 2.1.2 and considered experimental.
  //If the minifier specified in the "optimize" option supports generating
  //source maps for the minified code, then generate them. The source maps
  //generated only translate minified JS to non-minified JS, it does not do
  //anything magical for translating minified JS to transpiled source code.
  //Currently only optimize: "uglify2" is supported when running in node or
  //rhino, and if running in rhino, "closure" with a closure compiler jar
  //build after r1592 (20111114 release).
  //The source files will show up in a browser developer tool that supports
  //source maps as ".js.src" files.
  generateSourceMaps: false,

  //If you only intend to optimize a module (and its dependencies), with
  //a single file as the output, you can specify the module options inline,
  //instead of using the 'modules' section above. 'exclude',
  //'excludeShallow', 'include' and 'insertRequire' are all allowed as siblings
  //to name. The name of the optimized file is specified by 'out'.
  exclude: [
    //According to https://github.com/guybedford/require-css#basic-usage
    'cdf/lib/require-css/normalize'
  ],

  //Sets up a map of module IDs to other module IDs. For more details, see
  //the http://requirejs.org/docs/api.html#config-map docs.
  //map: {},

  //List the modules that will be optimized. All their immediate and deep
  //dependencies will be included in the module's file when the build is
  //done. If that module or any of its dependencies includes i18n bundles,
  //only the root bundles will be included unless the locale: section is set above.
  modules: [
    //Just specifying a module name means that module will be converted into
    //a built file that contains all of its dependencies. If that module or any
    //of its dependencies includes i18n bundles, they may not be included in the
    //built file unless the locale: section is set above.
    {
      name: "cde/components/AjaxRequestComponent",
      //create: true can be used to create the module layer at the given
      //name, if it does not already exist in the source location. If
      //there is a module at the source location with this name, then
      //create: true is superfluous.
      //create: true,

      //Also combines all the dependencies of the modules listed below
      //and any of their dependencies into one file.
      include: [],

      //Exclude the modules listed bellow and their dependencies from the built file. If you want
      //to exclude a module that is also another module being optimized, it is more
      //efficient if you define that module optimization entry before using it
      exclude: [
        // CDF dependencies (overhead, these might already have been loaded)
        "cdf/lib/jquery",
        "cdf/components/BaseComponent",
        "cdf/Logger",
        "cdf/dashboard/Utils"
      ]
    },
    {
      name: "cde/components/CggComponent",
      exclude: [
        // CDF dependencies (overhead, these might already have been loaded)
        "cdf/lib/jquery",
        "cdf/components/UnmanagedComponent"
      ]
    },
    {
      name: "cde/components/CggDialComponent",
      exclude: [
        // CDF dependencies (overhead, these might already have been loaded)
        "cdf/lib/jquery",
        "cdf/components/UnmanagedComponent"
      ]
    },
    {
      name: "cde/components/DashboardComponent",
      exclude: [
        // CDF dependencies (overhead, these might already have been loaded)
        "cdf/components/UnmanagedComponent"
      ]
    },
    {
      name: "cde/components/DuplicateComponent",
      exclude: [
        // CDF libs / components / utils
        "cdf/lib/jquery",
        "cdf/components/BaseComponent",
        "cdf/Logger"
      ]
    },
    {
      name: "cde/components/ExportButtonComponent",
      exclude: [
        // CSSs, otherwise paths (e.g. background images) are rewritten relative to the page and not the CSS file path
        "css!cde/components/ExportButtonComponent",
        // CDF dependencies (overhead, these might already have been loaded)
        "cdf/lib/jquery",
        "cdf/components/BaseComponent",
        "cdf/Logger",
        "cdf/dashboard/Utils"
      ]
    },
    {
      name: "cde/components/GMapsOverlayComponent",
      exclude: [
        // CSSs, otherwise paths (e.g. background images) are rewritten relative to the page and not the CSS file path
        "css!cde/components/GMapsOverlayComponent",
        // CDF dependencies (overhead, these might already have been loaded)
        "cdf/lib/jquery",
        "amd!cdf/lib/underscore",
        "cdf/components/UnmanagedComponent",
        "cdf/Logger"
      ]
    },
    {
      name: "cde/components/GoogleAnalyticsComponent",
      exclude: [
        // exclude resources that depend on requirejs loader plugins,
        // this avoids including requirejs loader plugins from CDF into the output module
        "amd!cde/components/googleAnalytics/lib/jquery.ga",
        // CDF dependencies (overhead, these might already have been loaded)
        "cdf/lib/jquery",
        "cdf/components/BaseComponent"
      ]
    },
    {
      name: "cde/components/Map/Map",
      exclude: [
        // CSSs, otherwise paths (e.g. background images) are rewritten relative to the page and not the CSS file path
        'css!cde/components/Map/Map',
        'css!cde/components/Map/ControlPanel/ControlPanel',
        'css!cde/components/Map/engines/openlayers2/styleOpenLayers2',
        'css!cde/components/Map/engines/google/styleGoogle',
        // CDF dependencies (overhead, these might already have been loaded)
        "cdf/lib/jquery",
        "amd!cdf/lib/underscore",
        "cdf/components/UnmanagedComponent",
        "cdf/Logger",
        'cdf/components/CggComponent.ext',
        'cdf/lib/BaseEvents',
        'cdf/lib/baseSelectionTree/Tree',
        'cdf/lib/baseSelectionTree/BaseSelectionTree',
        // mapengine
        "cdf/lib/Base",
        // mapengine-openlayers
        "cdf/lib/OpenLayers",
        "cdf/lib/OpenStreetMap",
        // map addIns
        "cdf/AddIn",
        "cdf/Dashboard.Clean"
      ]
    },
    {
      name: "cde/components/NewSelectorComponent",
      exclude: [
        // CSSs, otherwise paths (e.g. background images) are rewritten relative to the page and not the CSS file path
        "css!cde/components/NewSelectorComponent",
        // CDF dependencies (overhead, these might already have been loaded)
        "cdf/lib/jquery",
        "amd!cdf/lib/underscore",
        "amd!cdf/lib/backbone",
        "cdf/components/UnmanagedComponent",
        "cdf/dashboard/Utils",
        // views
        "cdf/lib/mustache"
      ]
    },
    {
      name: "cde/components/OlapSelectorComponent",
      exclude: [
        // CSSs, otherwise paths (e.g. background images) are rewritten relative to the page and not the CSS file path
        "css!cde/components/OlapSelectorComponent",
        // CDF dependencies (overhead, these might already have been loaded)
        "cdf/lib/jquery",
        "amd!cdf/lib/underscore",
        "cdf/components/BaseComponent",
        "cdf/dashboard/Utils",
        // OlapSelectorView / OlapSelectorModel
        "cdf/Logger",
        "cdf/lib/mustache",
        "amd!cdf/lib/backbone"
      ]
    },
    {
      name: "cde/components/PopupComponent",
      exclude: [
        // CSSs, otherwise paths (e.g. background images) are rewritten relative to the page and not the CSS file path
        "css!cde/components/PopupComponent",
        // CDF dependencies (overhead, these might already have been loaded)
        "cdf/lib/jquery",
        "cdf/components/BaseComponent"
      ]
    },
    {
      name: "cde/components/ExportPopupComponent",
      exclude: [
        // CSSs, otherwise paths (e.g. background images) are rewritten relative to the page and not the CSS file path
        "css!cde/components/ExportPopupComponent",
        "css!cde/components/PopupComponent",
        // CDF dependencies (overhead, these might already have been loaded)
        "cdf/lib/jquery",
        "amd!cdf/lib/jquery.fancybox",
        "cdf/Logger",
        "cdf/dashboard/Utils",
        // PopupComponent dependency
        "cdf/components/BaseComponent"
      ]
    },
    {
      name: "cde/components/RaphaelComponent",
      exclude: [
        // CDF dependencies (overhead, these might already have been loaded)
        //"cdf/lib/raphael", // don't exclude raphael
        "cdf/components/BaseComponent"
      ]
    },
    {
      name: "cde/components/RelatedContentComponent",
      exclude: [
        // CDF dependencies (overhead, these might already have been loaded)
        "cdf/lib/jquery",
        "cdf/components/BaseComponent"
      ]
    },
    {
      name: "cde/components/SiteMapComponent",
      exclude: [
        // CDF dependencies (overhead, these might already have been loaded)
        "cdf/lib/jquery",
        "amd!cdf/lib/underscore",
        "cdf/lib/mustache",
        "cdf/dashboard/Utils",
        "cdf/components/BaseComponent"
      ]
    },
    {
      name: "cde/components/TextEditorComponent",
      exclude: [
        // CSSs, otherwise paths (e.g. background images) are rewritten relative to the page and not the CSS file path
        "css!cde/components/TextEditorComponent",
        // CDF dependencies (overhead, these might already have been loaded)
        "cdf/lib/jquery",
        "amd!cdf/lib/underscore",
        "cdf/lib/mustache",
        "cdf/Logger",
        "cdf/components/BaseComponent"
      ]
    },
    {
      name: "cde/components/ViewManagerComponent",
      exclude: [
        // CSSs, otherwise paths (e.g. background images) are rewritten relative to the page and not the CSS file path
        "css!cde/components/ViewManagerComponent",
        // CDF dependencies (overhead, these might already have been loaded)
        "cdf/lib/jquery",
        "cdf/components/BaseComponent",
        // ViewManagerModel / ViewManagerView
        "amd!cdf/lib/backbone",
        "amd!cdf/lib/underscore",
        "cdf/lib/base64",
        "cdf/Logger"
      ]
    }
  ]
})
