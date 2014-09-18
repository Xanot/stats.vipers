'use strict';

angular.module('navbar', ['mgcrea.ngStrap.navbar', 'websocket'])
  .config(['$navbarProvider', function($navbarProvider) {
    angular.extend($navbarProvider.defaults, {
      activeClass: 'active'
    });
  }])

  .controller("NavbarController", ['$scope', 'WebSocketService', function($scope, WebSocketService) {

} ]);
