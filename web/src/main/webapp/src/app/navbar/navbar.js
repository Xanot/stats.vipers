'use strict';

angular.module('navbar', ['mgcrea.ngStrap.navbar'])
  .config(['$navbarProvider', function($navbarProvider) {
    angular.extend($navbarProvider.defaults, {
      activeClass: 'active'
    });
  }]);
