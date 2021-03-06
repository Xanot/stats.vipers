'use strict';

angular.module('outfit-view', ['utils', 'ui.router', 'websocket'])
  .config(['$stateProvider', function($stateProvider) {
    $stateProvider
      .state('outfit-view', {
        url: '/outfit/:alias',
        controller: 'OutfitViewController',
        templateUrl: 'app/outfit/view/view.html',
        resolve: {
          resolveOutfit : ['$q', '$stateParams', 'OutfitService', 'NotificationService', 'WebSocketService',
            function($q, $stateParams, OutfitService, NotificationService, WebSocketService) {
            var deferred = $q.defer();

            if($stateParams.alias.length >= 1 && $stateParams.alias.length <= 4) {
              var aliasLower = $stateParams.alias.toLowerCase();

              WebSocketService.subscribe("o:" + aliasLower, function(data) {
                NotificationService.outfitIndexed(data)
              });

              OutfitService.get(aliasLower).then(function(response) {
                if(response.data.updateTime < new Date().getTime()) {
                  NotificationService.outfitBeingIndexed(response.data.alias)
                }
                deferred.resolve(response.data);
              }).catch(function(err) {
                deferred.reject();
                NotificationService.outfitBeingIndexed(aliasLower);
              });
            } else {
              deferred.reject();
            }

            return deferred.promise;
          }]
        }
      })
  }])

  .controller('OutfitViewController', ['$scope', '$state', 'OutfitService', 'resolveOutfit', 'WebSocketService', 'NotificationService',
    function($scope, $state, OutfitService, resolveOutfit, WebSocketService, NotificationService) {
      $scope.limitRows = 50;

      $scope.increaseLimit = function() {
        $scope.limitRows += 30;
      };

      $scope.$watch("filterName", function() {
        $scope.limitRows = 50;
      });

      $scope.leaderHref = function(name) {
        return $state.href('player-view', {name: name})
      };

      $scope.go = function(alias) {
        $state.transitionTo('outfit-view', {alias: alias})
      };

      $scope.outfit = resolveOutfit;

      // Retrieve the outfit if it is updated
      WebSocketService.subscribe("o:" + resolveOutfit.aliasLower, function(data) {
        OutfitService.get(resolveOutfit.aliasLower).then(function(response) {
          $scope.outfit = response.data;
          NotificationService.outfitIndexed(data)
        });
      });

      // Don't retrieve the outfit if we are out of this state
      $scope.$on('$destroy', function() {
        WebSocketService.subscribe("o:" + resolveOutfit.aliasLower, function(data) {
          NotificationService.outfitIndexed(data)
        })
      });
    }]);
