/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

module.exports = function(config) {
  config.set({

    // base path, that will be used to resolve files and exclude
    basePath: '../../../../',

    // frameworks to use
    frameworks: ['jasmine', 'requirejs'],

    // list of files / patterns to load in the browser
    files: [
      'src/test/javascript/lib/jquery.js',
      'src/main/javascript/jquery.jqModal.js',
      'src/main/javascript/jquery.corner.js',
      'src/test/javascript/lib/underscore.js',
      'src/main/javascript/jquery.jeditable.js',
      'src/main/javascript/jquery.impromptu.js',
      'src/main/javascript/Base.js',
      'src/test/javascript/mocks/testUtils.js',
      'src/test/javascript/mocks/mockDashboards.js',
      'src/main/javascript/cdf-dd-config.js',
      'src/main/javascript/jquery.treeTable.js',
      'src/main/javascript/jqueryFileTree.js',
      'src/test/javascript/lib/ace/src/ace.js',
      'src/main/javascript/cdf-dd-aceWrapper.js',
      'src/main/javascript/cdf-dd-tablemanager.js',
      'src/main/javascript/cdf-dd-indexmanager.js',
      'src/main/javascript/cdf-dd-wizardmanager.js',
      'src/main/javascript/cdf-dd-propertiesmanager.js',
      'src/main/javascript/cdf-dd.js',
      'src/main/javascript/cdf-dd-commands.js',
      'src/main/javascript/cdf-dd-palletemanager.js',
      'src/main/javascript/cdf-dd-tableoperations.js',
      'src/main/javascript/cdf-dd-layout-mobile.js',
      'src/main/javascript/cdf-dd-layout.js',
      'src/main/javascript/cdf-dd-components.js',
      'src/main/javascript/cdf-dd-components-generic.js',
      'src/main/javascript/cdf-dd-components-selectors.js',
      'src/main/javascript/cdf-dd-components-others.js',
      'src/main/javascript/cdf-dd-prompt-properties.js',
      'src/main/javascript/cdf-dd-prompt-wizard.js',
      'src/main/javascript/cdf-dd-datasources.js',
      'src/test/config/javascript/context.js',
      'src/test/config/javascript/karma.main.js',
      {pattern: 'src/test/javascript/**/*-spec.js', included: false}
    ],

    // list of files to exclude
    exclude: [],

    //preprocessors: {"src/main/javascript/cdf-dd.js": 'coverage'},

    // test results reporter to use
    // possible values: 'dots', 'progress', 'junit', 'growl', 'coverage'
    reporters: ['progress', 'junit', 'html', 'coverage'],

    coverageReporter: {
      type: 'cobertura',
      dir:  'target/test-reports-webclient/coverage/reports/'
    },

    //reporter: junit
    junitReporter: {
      outputFile: 'test-results.xml',
      outputDir:  'target/test-reports-webclient/junit/reports/',
      suite:      'unit'
    },

    // the default configuration
    htmlReporter: {
      outputDir:    'target/test-reports-webclient/karma_html',
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
    autoWatch: true,

    // The configuration setting tells Karma how long to wait (in milliseconds) after any changes have occurred before starting the test process again.
    autoWatchBatchDelay: 250,

    // Start these browsers, currently available:
    // - Chrome
    // - ChromeCanary
    // - Firefox
    // - Opera (has to be installed with `npm install karma-opera-launcher`)
    // - Safari (only Mac; has to be installed with `npm install karma-safari-launcher`)
    // - PhantomJS
    // - IE (only Windows; has to be installed with `npm install karma-ie-launcher`)
    browsers: ['Chrome'],//, 'Firefox', 'IE', 'PhantomJS'],

    // If browser does not capture in given timeout [ms], kill it
    captureTimeout: 600000,

    browserNoActivityTimeout: 600000,

    // Continuous Integration mode
    // if true, it capture browsers, run tests and exit
    singleRun: false,

    plugins: [
      'karma-jasmine',
      'karma-requirejs',
      'karma-junit-reporter',
      'karma-html-reporter',
      'karma-coverage',
      'karma-phantomjs-launcher',
      'karma-chrome-launcher',
      'karma-firefox-launcher'
    ]
  });
};
