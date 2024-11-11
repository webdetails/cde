/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
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
      'target/dependency/cde/webclient/jquery.jeditable.js',
      'target/dependency/cde/webclient/jquery.notifyBar.js',
      'target/dependency/cde/webclient/Base.js',
      'src/test/javascript/mocks/mockDashboards.js',
      'target/dependency/cde/webclient/cdf-dd-config.js',
      'target/dependency/cde/webclient/cdf-dd-tablemanager.js',
      'target/dependency/cde/webclient/cdf-dd-wizardmanager.js',
      'target/dependency/cde/webclient/jquery.form.js',
      'target/dependency/cde/webclient/cdf-dd.js',
      'src/main/javascript/cdf-dd-base.js',
      'src/test/javascript/mocks/mock-cdf-dd-base.js',
      'target/dependency/cde/webclient/cdf-dd-palletemanager.js',
      'target/dependency/cde/webclient/cdf-dd-tableoperations.js',
      'target/dependency/cde/webclient/cdf-dd-layout-mobile.js',
      'target/dependency/cde/webclient/cdf-dd-layout.js',
      'target/dependency/cde/webclient/cdf-dd-components.js',
      'target/dependency/cde/webclient/cdf-dd-datasources.js',
      'src/test/config/javascript/context.js',
      'src/test/config/javascript/karma.main.js',
      {pattern: 'src/test/javascript/**/*-spec.js', included: false}
    ],

    // list of files to exclude
    exclude: [],

    preprocessors: {'src/main/javascript/cdf-dd-base.js': 'coverage'},

    // test results reporter to use
    // possible values: 'dots', 'progress', 'junit', 'growl', 'coverage'
    reporters: ['progress', 'junit', 'html', 'coverage'],

    coverageReporter: {
      type: 'cobertura',
      dir:  'target/test-reports/coverage/reports/'
    },

    //reporter: junit
    junitReporter: {
      outputFile: 'test-results.xml',
      outputDir:  'target/test-reports/junit/reports/',
      suite: 'unit'
    },

    // the default configuration
    htmlReporter: {
      outputDir:    'target/test-reports/karma_html',
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
