'use strict';

angular.module('player-killboard', ['utils', 'ui.router'])
  .config(['$stateProvider', function($stateProvider) {
    $stateProvider
      .state('player-view.killboard', {
        url: '/killboard',
        controller: 'PlayerKillboardController',
        templateUrl: 'app/player/view/killboard/killboard.html',
        resolve: {
          resolveKillboard : ['$q', function($q) {
            var deferred = $q.defer();
            deferred.resolve();
            return deferred.promise;
          }]
        }
      })
  }])

  .controller('PlayerKillboardController', ['$scope', 'resolveKillboard', 'PlayerKillboardService',
    function($scope, resolveKillboard, PlayerKillboardService) {
      PlayerKillboardService.getLast100($scope.player.id).then(function(response) {
        $scope.history = response.data.characters_event_list;
      });
    }])

  .factory('PlayerKillboardService', ['$http', 'UrlService', function($http, UrlService) {
    return {
      getLast100: function(characterId) {
        return $http.jsonp("https://census.soe.com/s:soe/json/get/ps2:v2/characters_event/?character_id=" + characterId + "&type=KILL,DEATH&c:limit=100&c:resolve=character(name,faction_id),attacker(name,faction_id)&callback=JSON_CALLBACK")
      }
    }
  }]);