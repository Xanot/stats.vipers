module.exports = function(config){
  config.set({
    basePath: '',

    files: [
      'bower_components/angular/angular.js',
      'bower_components/angular-mocks/angular-mocks.js',
      'bower_components/angular-ui-router/release/angular-ui-router.js',

      'src/app/**/*.js',
      'test/*.js'
    ],

    exclude: [],
    preprocessors: {},
    proxies: {},

    // test results reporter to use
    // possible values: 'dots', 'progress', 'junit', 'growl', 'coverage'
    reporters: ['progress'],

    port: 9876,
    colors: true,

    // level of logging
    // possible values: config.LOG_DISABLE || config.LOG_ERROR || config.LOG_WARN || config.LOG_INFO || config.LOG_DEBUG
    logLevel: config.LOG_INFO,

    autoWatch: true,
    frameworks: ['jasmine'],
    browsers: [
      'PhantomJS'
    ],

    plugins: [
      'karma-chrome-launcher',
      'karma-firefox-launcher',
      'karma-script-launcher',
      'karma-phantomjs-launcher',
      'karma-jasmine'
    ],

    captureTimeout: 60000,

    // Continuous Integration mode
    // if true, it capture browsers, run tests and exit
    singleRun: false
  });
};