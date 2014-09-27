'use strict';

angular.module('player.view', ['utils', 'ui.router'])
  .config(['$stateProvider', function($stateProvider) {
    $stateProvider
      .state('player-view', {
        url: '/player/:name',
        controller: 'PlayerViewController',
        templateUrl: 'app/player/view/view.html',
        resolve: {
          resolvePlayer : ['$q', '$stateParams', 'PlayerService', 'NotificationService', 'WebSocketService', function($q, $stateParams, PlayerService, NotificationService, WebSocketService) {
            var deferred = $q.defer();
            var nameLower = $stateParams.name.toLowerCase();

            WebSocketService.subscribe("c:" + nameLower, function(data) {
              NotificationService.characterIndexed(data)
            });

            PlayerService.getByName(nameLower).then(function(response) {
              deferred.resolve(response.data);
            }).catch(function(err) {
              deferred.reject();
              NotificationService.characterBeingIndexed($stateParams.name)
            });

            return deferred.promise;
          }]
        }
      })
  }])

  .controller('PlayerViewController', ['$scope', 'PlayerService', 'resolvePlayer', 'WebSocketService', 'NotificationService',
    function($scope, PlayerService, resolvePlayer, WebSocketService, NotificationService) {
      $scope.player = resolvePlayer;

      WebSocketService.subscribe("c:" + resolvePlayer.nameLower, function(data) {
        PlayerService.getByName(data).then(function(response) {
          $scope.player = response.data;
        });
        NotificationService.characterIndexed(data)
      });
  }]);
