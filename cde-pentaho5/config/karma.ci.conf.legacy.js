/*!
 * Copyright 2002 - 2015 Webdetails, a Pentaho company.  All rights reserved.
 *
 * This software was developed by Webdetails and is provided under the terms
 * of the Mozilla Public License, Version 2.0, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to  http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
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
      '../cde-core/test-resources/js/jquery.js',
      '../cde-core/resource/js/jquery.jeditable.js',
      '../cde-core/resource/js/jquery.notifyBar.js',
      '../cde-core/resource/js/Base.js',
      'test-js/legacy/mockDashboards.js',
      '../cde-core/resource/js/cdf-dd-config.js',
      '../cde-core/resource/js/cdf-dd-tablemanager.js',
      '../cde-core/resource/js/cdf-dd-wizardmanager.js',
      '../cde-core/resource/js/jquery.form.js',
      '../cde-core/resource/js/cdf-dd.js',
      'resource/js/cdf-dd-base.js',
      'test-js/legacy/mock-cdf-dd-base.js',
      '../cde-core/resource/js/cdf-dd-palletemanager.js',
      '../cde-core/resource/js/cdf-dd-tableoperations.js',
      '../cde-core/resource/js/cdf-dd-layout-mobile.js',
      '../cde-core/resource/js/cdf-dd-layout.js',
      '../cde-core/resource/js/cdf-dd-components.js',
      '../cde-core/resource/js/cdf-dd-datasources.js',
      'test-js/legacy/main.js',
      {pattern: 'test-js/legacy/**/*-spec.js', included: false}
    ],


    // list of files to exclude
    exclude: [],

    preprocessors: {'resource/js/cdf-dd.js': 'coverage'},

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

    //browserNoActivityTimeout: 20000,

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
