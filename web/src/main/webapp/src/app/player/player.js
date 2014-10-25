'use strict';

angular.module('player', ['player-view', 'utils', 'ui.router'])
  .config(['$stateProvider', function($stateProvider) {
    $stateProvider
      .state('player', {
        url: '/player',
        controller: 'PlayerController',
        templateUrl: 'app/player/player.html'
      })
  }])

  .controller('PlayerController', ['$scope', '$state', function($scope, $state) {
    $scope.go = function(name) {
      $state.transitionTo('player-view', {name: name})
    };
  }])

  .factory('PlayerService', ['$http', 'UrlService', function($http, UrlService) {
    return {
      getByName: function(name) {
        return $http.get(UrlService.url("/player/" + name))
      },
      getCharactersWeaponStatHistory: function(charId, itemId) {
        return $http.get(UrlService.url("/player/" + charId + "/stats/" + itemId))
      }
    }
  }]);
