'use strict';

angular.module('main', ['websocket', 'notification', 'navbar', 'home', 'outfit', 'player', 'settings', 'config.highcharts',
  'ui.router', 'ngSanitize', 'ngAnimate', 'mgcrea.ngStrap', 'angularMoment', 'angular-loading-bar',
  'pasvaz.bindonce', 'infinite-scroll', 'LocalStorageModule', 'highcharts-ng'])

  .config(['$urlRouterProvider', 'localStorageServiceProvider', function($urlRouterProvider, localStorageServiceProvider){
    $urlRouterProvider.otherwise('home');
    localStorageServiceProvider.setPrefix('stats.vipers');
  }])

  .run(['SettingsService', function(SettingsService) {
    SettingsService.init();
  }])

  .constant('angularMomentConfig', {
    preprocess: 'unix'
  });
