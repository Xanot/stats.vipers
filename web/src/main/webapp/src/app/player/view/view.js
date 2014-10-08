'use strict';

angular.module('player-view', ['utils', 'ui.router', 'player-killboard'])
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
              if(response.data.updateTime < new Date().getTime()) {
                NotificationService.characterBeingIndexed(response.data.name)
              }
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

      // Retrieve the character if it is updated
      WebSocketService.subscribe("c:" + resolvePlayer.nameLower, function(data) {
        PlayerService.getByName(data).then(function(response) {
          $scope.player = response.data;
        });
        NotificationService.characterIndexed(data)
      });

      // Don't retrieve the character if we are out of this state
      $scope.$on('$destroy', function() {
        WebSocketService.subscribe("c:" + resolvePlayer.nameLower, function(data) {
          NotificationService.characterIndexed(data)
        });
      });
  }]);
