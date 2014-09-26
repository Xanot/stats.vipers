'use strict';

angular.module('player.view', ['utils', 'ui.router'])
  .config(['$stateProvider', function($stateProvider) {
    $stateProvider
      .state('player-view', {
        url: '/player/:name',
        controller: 'PlayerViewController',
        templateUrl: 'app/player/view/view.html',
        resolve: {
          resolvePlayer : ['$q', '$stateParams', 'PlayerService', 'AlertService', 'WebSocketService', function($q, $stateParams, PlayerService, AlertService, WebSocketService) {
            var deferred = $q.defer();
            var nameLower = $stateParams.name.toLowerCase();

            WebSocketService.subscribe("c:" + nameLower, function(data) {
              AlertService.alertWithData({"type": "info", name: data}, undefined, 'app/player/alert.player.tpl.html')
            });

            PlayerService.getByName(nameLower).then(function(response) {
              deferred.resolve(response.data);
            }).catch(function(err) {
              deferred.reject();
              AlertService.alert($stateParams.name, "is being indexed, you will be notified if it exists", "warning", 5)
            });

            return deferred.promise;
          }]
        }
      })
  }])

  .controller('PlayerViewController', ['$scope', 'PlayerService', 'resolvePlayer', 'WebSocketService', 'AlertService',
    function($scope, PlayerService, resolvePlayer, WebSocketService, AlertService) {
      $scope.player = resolvePlayer;

      WebSocketService.subscribe("c:" + resolvePlayer.nameLower, function(data) {
        PlayerService.getByName(data).then(function(response) {
          $scope.player = response.data;
        });
        AlertService.alertWithData({"type": "info", name: data}, undefined, 'app/player/alert.player.tpl.html')
      });
  }]);
