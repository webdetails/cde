/*!
 * Copyright 2018 Webdetails, a Hitachi Vantara company. All rights reserved.
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
    basePath: '../../../../',

    // frameworks to use
    frameworks: ['jasmine', 'requirejs'],

    // list of files / patterns to load in the browser
    files: [
      {pattern: 'target/dependency/cdf/js/**/*.+(js|css)', included: false},
      {pattern: 'src/main/javascript/components/*/amd/**/*.+(js|css)', included: false},
      {pattern: 'src/test/javascript/**/*-spec.js', included: false},
      'src/test/config/javascript/context.js',
      {pattern: 'src/test/javascript/mocks/**/*.js', included: true},
      'target/dependency/cdf/cdf-require-js-cfg.js',
      'src/main/javascript/cde-pentaho-require-js-cfg.js',
      'src/test/config/javascript/karma.main.js',
      // fix 404 messages
      {pattern: 'src/main/javascript/components/*/amd/**/*.+(png|gif|html)', watched: false, included: false, served: true},
      {pattern: 'target/dependency/cdf/js/**/*.+(png|gif)', watched: false, included: false, served: true}
    ],

    // list of files to exclude
    exclude: [
      'target/dependency/cdf/js/cdf-module.js',
      'target/dependency/cdf/js/*-require-cfg.js',
      'target/dependency/cdf/js/lib/*-require-cfg.js',
      'target/dependency/cdf/js/compressed/**/*'
    ],

    //preprocessors: {'src/main/javascript/components/*/amd/**/*.js': 'coverage'},

    // test results reporter to use
    // possible values: 'dots', 'progress', 'junit', 'growl', 'coverage'
    reporters: ['progress', 'junit', 'html', 'coverage'],

    coverageReporter: {
      reporters: [
        {
          type: 'html',
          dir:  'target/test-reports/coverage/html/reports/'
        },
        {
          type: 'cobertura',
          dir:  'target/test-reports/coverage/reports/'
        }
      ]
    },

    //reporter: junit
    junitReporter: {
      outputFile: 'test-results.xml',
      outputDir:  'target/test-reports/junit/reports/',
      suite:      'unit'
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
