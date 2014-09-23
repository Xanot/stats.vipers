'use strict';

angular.module('main', ['websocket', 'navbar', 'home', 'outfit', 'player',
  'ui.router', 'ngSanitize', 'ngAnimate', 'mgcrea.ngStrap', 'angularMoment', 'hc.marked',
  'angular-loading-bar', 'pasvaz.bindonce',
  'infinite-scroll'])

  .config(['$urlRouterProvider', function($urlRouterProvider){
    $urlRouterProvider.otherwise('home');
  }])

  .constant('angularMomentConfig', {
    preprocess: 'unix'
  });
