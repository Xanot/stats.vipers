'use strict';

angular.module('home', ['ui.router'])
  .config(['$stateProvider', function($stateProvider) {
    $stateProvider.state('home', {
      url: '/home',
      controller: 'HomeController',
      templateUrl: 'app/home/home.html'
    });
  }])

  .controller('HomeController', ['$scope', function($scope) {
    $scope.test = "Home";
  }]);
