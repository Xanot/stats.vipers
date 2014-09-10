'use strict';

angular.module('home', ['ui.router'])
  .config(['$stateProvider', function($stateProvider) {
    $stateProvider.state('home', {
      url: '/home',
      controller: 'HomeController',
      templateUrl: 'app/home/home.html'
    });
  }])

  .controller('HomeController', ['$scope', 'PostService', function($scope, PostService) {
    PostService.getPosts().then(function(response) {
      $scope.posts = response.data;
    });
  }])

  .factory('PostService', ['$http', function($http) {
    return{
      getPosts: function() {
        return $http.get('app/home/posts.json')
      }
    }
  }]);
