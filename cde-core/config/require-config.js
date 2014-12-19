// Find and inject tests using require
(function() {
  var karma = window.__karma__;

  var tests = [];
  for (var file in karma.files) {
    if ((/test\-js.*\-spec\.js$/).test(file)) {
      tests.push(file.replace(/^\/base\//, 'http://localhost:9876/base/'))
    }
  }

  requireCfg['baseUrl'] = 'http://localhost:9876/base/';
  requirejs.config(requireCfg);

  // Ask Require.js to load all test files and start test run
  require(tests, karma.start);
})();
