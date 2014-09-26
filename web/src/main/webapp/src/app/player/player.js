'use strict';

angular.module('player', ['player.view', 'utils', 'ui.router'])
  .config(['$stateProvider', function($stateProvider) {
    $stateProvider
      .state('player', {
        url: '/player',
        controller: 'PlayerController',
        templateUrl: 'app/player/player.html'
      })
  }])

  .controller('PlayerController', ['$scope', 'PlayerService', function($scope, PlayerService) {
    PlayerService.getAllCharacters().then(function(response) {
      $scope.players = response.data
    });
  }])

  .factory('PlayerService', ['$http', 'UrlService', function($http, UrlService) {
    return {
      getByName: function(name) {
        return $http.get(UrlService.url("/player/" + name))
      },
      getAllCharacters: function() {
        return $http.get(UrlService.url("/player"))
      }
    }
  }]);
