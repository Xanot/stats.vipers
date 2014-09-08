'use strict';

angular.module('main', ['home', 'ui.router', 'mgcrea.ngStrap'])
  .config(['$urlRouterProvider', function($urlRouterProvider){
    $urlRouterProvider.otherwise('home');
  }]);
