/*!
 * Copyright 2002 - 2015 Webdetails, a Pentaho company. All rights reserved.
 *
 * This software was developed by Webdetails and is provided under the terms
 * of the Mozilla Public License, Version 2.0, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */

module.exports = function(config) {
  config.set({

    // base path, that will be used to resolve files and exclude
    basePath: '../',

    // frameworks to use
    frameworks: ['jasmine', 'requirejs'],

    // list of files / patterns to load in the browser
    files: [
      'test-resources/js/jquery.js',
      'resource/js/jquery.jqModal.js',
      'resource/js/jquery.corner.js',
      'test-resources/js/underscore.js',
      'resource/js/jquery.jeditable.js',
      'resource/js/jquery.impromptu.js',
      'resource/js/Base.js',
      'test-js/legacy/testUtils.js',
      'test-js/legacy/mockDashboards.js',
      'resource/js/cdf-dd-config.js',
      'resource/js/jquery.treeTable.js',
      'resource/resources/ace/src/ace.js',
      'resource/js/cdf-dd-aceWrapper.js',
      'resource/js/cdf-dd-tablemanager.js',
      'resource/js/cdf-dd-indexmanager.js',
      'resource/js/cdf-dd-wizardmanager.js',
      'resource/js/cdf-dd-propertiesmanager.js',
      'resource/js/cdf-dd.js',
      'resource/js/cdf-dd-commands.js',
      'resource/js/cdf-dd-palletemanager.js',
      'resource/js/cdf-dd-tableoperations.js',
      'resource/js/cdf-dd-layout-mobile.js',
      'resource/js/cdf-dd-layout.js',
      'resource/js/cdf-dd-components.js',
      'resource/js/cdf-dd-components-generic.js',
      'resource/js/cdf-dd-prompt-properties.js',
      'resource/js/cdf-dd-prompt-wizard.js',
      'resource/js/cdf-dd-datasources.js',
      'test-js/legacy/main.js',

      {pattern: 'test-js/legacy/**/*-spec.js', included: false}
    ],

    // list of files to exclude
    exclude: [],

    preprocessors: {"resource/js/cdf-dd.js" : 'coverage'},

    // test results reporter to use
    // possible values: 'dots', 'progress', 'junit', 'growl', 'coverage'
    reporters: ['progress', 'junit', 'html', 'coverage'],

    coverageReporter: {
      type: 'cobertura',
      dir:  'bin/test-reports-legacy/coverage/reports/'
    },

    junitReporter: {
      outputFile: 'bin/test-reports-legacy/test-results.xml',
      suite:      'unit'
    },

    // the default configuration
    htmlReporter: {
      outputDir:    'bin/test-reports-legacy/karma_html',
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

    // The configuration setting tells Karma how long to wait (in milliseconds) after any changes have occurred before starting the test process again.
    //autoWatchBatchDelay: 250,

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
    
    // to avoid DISCONNECTED messages
    // see https://github.com/karma-runner/karma/issues/598
    browserDisconnectTimeout : 10000, // default 2000
    browserDisconnectTolerance : 1, // default 0
    browserNoActivityTimeout : 60000, //default 10000

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
