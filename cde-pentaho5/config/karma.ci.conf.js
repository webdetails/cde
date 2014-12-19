// Karma configuration
// Generated on Fri Nov 15 2013 00:09:22 GMT+0000 (GMT Standard Time)

module.exports = function(config) {
  config.set({

    // base path, that will be used to resolve files and exclude
    basePath: '../',

    // frameworks to use
    frameworks: ['jasmine', 'requirejs'],

    // list of files / patterns to load in the browser
    files: [

      // CDF
      { pattern: 'js-lib/expanded/pentaho-cdf/js/**/*.js', included: false },
      { pattern: 'js-lib/expanded/pentaho-cdf/js/**/*.css', included: false },

      { pattern: 'test-js/**/*-spec.js', included: false },
      { pattern: 'test-js/*.js', included: true },
      'config/context.js',
      { pattern: 'test-js/**/*.ext.js', included: true },
      'build-res/requireCfg-raw.js',
      'config/require-config.js'

    ],

    // list of files to exclude
    exclude: [
      'test-js/legacy/**/*.js',
      'js-lib/expanded/pentaho-cdf/js/cdf-module.js',
      'js-lib/expanded/pentaho-cdf/js/*-require-cfg.js',
      'js-lib/expanded/pentaho-cdf/js/lib/*-require-cfg.js',
      'js-lib/expanded/pentaho-cdf/js/compressed/**/*'
    ],

    preprocessors: {'resource/resources/custom/amd-components/**/*.js': 'coverage'},

    // test results reporter to use
    // possible values: 'dots', 'progress', 'junit', 'growl', 'coverage'
    reporters: ['progress', 'junit', 'html', 'coverage'],

    coverageReporter: {
      type: 'cobertura',
      dir:  'bin/test/coverage/reports/'
    },

    junitReporter: {
      outputFile: 'bin/test/test-results.xml',
      suite:      'unit'
    },

    // the default configuration
    htmlReporter: {
      outputDir:    'bin/test/karma_html',
      templatePath: 'node_modules/karma-html-reporter/jasmine_template.html'
    },

    //hostname
    hostname: ['localhost'],

    // web server port
    port: 9876,

    // enable / disable colors in the output (reporters and logs)
    colors: true,

    // level of logging
    // possible values: config.LOG_DISABLE || config.LOG_ERROR || config.LOG_WARN || config.LOG_INFO || config.LOG_DEBUG
    logLevel: config.LOG_INFO,

    // enable / disable watching file and executing tests whenever any file changes
    autoWatch: false,

    // Start these browsers, currently available:
    // - Chrome
    // - ChromeCanary
    // - Firefox
    // - Opera (has to be installed with `npm install karma-opera-launcher`)
    // - Safari (only Mac; has to be installed with `npm install karma-safari-launcher`)
    // - PhantomJS
    // - IE (only Windows; has to be installed with `npm install karma-ie-launcher`)
    browsers: ['PhantomJS'],//, 'Firefox', 'IE', 'PhantomJS'],

    // If browser does not capture in given timeout [ms], kill it
    captureTimeout: 60000,

    // Continuous Integration mode
    // if true, it capture browsers, run tests and exit
    singleRun: true,

    plugins: [
      'karma-jasmine',
      'karma-requirejs',
      'karma-junit-reporter',
      'karma-html-reporter',
      'karma-coverage',
      'karma-phantomjs-launcher'
    ]
  });
};
