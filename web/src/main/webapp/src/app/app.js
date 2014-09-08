'use strict';

angular.module('main', ['home', 'outfit', 'utils', 'ui.router', 'mgcrea.ngStrap'])
  .config(['$urlRouterProvider', function($urlRouterProvider){
    $urlRouterProvider.otherwise('home');
  }]);
