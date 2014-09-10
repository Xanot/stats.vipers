'use strict';

angular.module('main', ['navbar', 'home', 'outfit', 'player',
  'ui.router', 'ngSanitize', 'ngAnimate', 'mgcrea.ngStrap', 'angularMoment'])
  .config(['$urlRouterProvider', '$httpProvider', function($urlRouterProvider, $httpProvider){
    $httpProvider.interceptors.push('ProgressInterceptor');
    $urlRouterProvider.otherwise('home');
  }])

  .constant('angularMomentConfig', {
    preprocess: 'unix'
  });
