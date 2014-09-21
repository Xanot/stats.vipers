'use strict';

angular.module('player.view', ['utils', 'ui.router'])
  .config(['$stateProvider', function($stateProvider) {
    $stateProvider
      .state('player-view', {
        url: '/player/:name',
        controller: 'PlayerViewController',
        templateUrl: 'app/player/view/view.html',
        resolve: {
          resolvePlayer : ['$q', '$stateParams', 'Player', 'AlertService', 'WebSocketService', function($q, $stateParams, Player, AlertService, WebSocketService) {
            var deferred = $q.defer();
            var nameLower = $stateParams.name.toLowerCase();

            WebSocketService.subscribe("c:" + nameLower, function(data) {
              AlertService.alertWithData({"type": "info", name: data}, undefined, 'app/player/alert.player.tpl.html')
            });

            Player.find(nameLower).then(function(response) {
              deferred.resolve(response);
            }).catch(function(err) {
              deferred.reject();
              AlertService.alert($stateParams.name, "is being indexed, you will be notified if it exists", "warning", 5)
            });

            return deferred.promise;
          }]
        }
      })
  }])

  .controller('PlayerViewController', ['$scope', 'Player', 'resolvePlayer',
    function($scope, Player, resolvePlayer) {
      Player.bindOne($scope, 'player', resolvePlayer.nameLower);
    }
  ])

  .factory('Player', ['DS', 'UrlService', function(DS, UrlService) {
    return DS.defineResource({
      name: "player",
      baseUrl: UrlService.url("/"),
      idAttribute: 'nameLower'
    });
  }]);
