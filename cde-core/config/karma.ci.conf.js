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

      // CDF
      {pattern: 'bin/test-js/cdf/js/**/*.js', included: false},
      {pattern: 'bin/test-js/cdf/js/**/*.css', included: false},
      
      {pattern: 'resource/resources/custom/amd-components/**/*.css', included: false},
      {pattern: 'resource/resources/custom/amd-components/**/*.js', included: false},
      {pattern: 'test-js/**/*-spec.js', included: false},
      {pattern: 'test-js/*.js', included: true},
      'config/context.js',
      {pattern: 'test-js/**/*.ext.js', included: true},
      'build-res/requireCfg-raw.js',
      'config/require-config.js',
      // fix 404 messages
      {pattern: 'resource/resources/custom/amd-components/**/*.png', watched: false, included: false, served: true},
      {pattern: 'resource/resources/custom/amd-components/**/*.gif', watched: false, included: false, served: true},
      {pattern: 'resource/resources/custom/amd-components/**/*.html', watched: false, included: false, served: true},
      {pattern: 'bin/test-js/cdf/js/**/*.png', watched: false, included: false, served: true},
      {pattern: 'bin/test-js/cdf/js/**/*.gif', watched: false, included: false, served: true}

    ],

    // list of files to exclude
    exclude: [
      'test-js/legacy/**/*.js',
      'bin/test-js/cdf/js/cdf-module.js',
      'bin/test-js/cdf/js/*-require-cfg.js',
      'bin/test-js/cdf/js/lib/*-require-cfg.js',
      'bin/test-js/cdf/js/compressed/**/*'
    ],

    preprocessors: {'resource/resources/custom/amd-components/**/*.js': 'coverage'},

    // test results reporter to use
    // possible values: 'dots', 'progress', 'junit', 'growl', 'coverage'
    reporters: ['progress', 'junit', 'html', 'coverage'],

    coverageReporter: {
      type: 'cobertura',
      dir: 'bin/test-reports/coverage/reports/'
    },

    junitReporter: {
      outputFile: 'bin/test-reports/test-results.xml',
      suite: 'unit'
    },

    // the default configuration
    htmlReporter: {
      outputDir:    'bin/test-reports/karma_html',
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
