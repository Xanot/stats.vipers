'use strict';

angular.module('main', ['navbar', 'home', 'outfit', 'player',
  'ui.router', 'ngSanitize', 'ngAnimate', 'mgcrea.ngStrap', 'angularMoment', 'hc.marked',
  'angular-data.DS', 'angular-data.DSCacheFactory', 'angular-loading-bar'])

  .config(['$urlRouterProvider', function($urlRouterProvider){
    $urlRouterProvider.otherwise('home');
  }])

  .constant('angularMomentConfig', {
    preprocess: 'unix'
  });
