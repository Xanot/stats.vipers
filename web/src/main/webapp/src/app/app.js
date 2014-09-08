'use strict';

angular.module('main', ['home', 'outfit', 'ui.router', 'mgcrea.ngStrap'])
  .config(['$urlRouterProvider', function($urlRouterProvider){
    $urlRouterProvider.otherwise('home');
  }]);
