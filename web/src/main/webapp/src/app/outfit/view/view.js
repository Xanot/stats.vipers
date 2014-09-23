'use strict';

angular.module('outfit-view', ['utils', 'ui.router', 'websocket'])
  .config(['$stateProvider', function($stateProvider) {
    $stateProvider
      .state('outfit-view', {
        url: '/outfit/:aliasOrId',
        controller: 'OutfitViewController',
        templateUrl: 'app/outfit/view/view.html',
        resolve: {
          resolveOutfit : ['$q', '$stateParams', 'OutfitService', 'AlertService', 'WebSocketService',
            function($q, $stateParams, OutfitService, AlertService, WebSocketService) {
            var deferred = $q.defer();

            if($stateParams.aliasOrId.length <= 4) {
              var aliasLower = $stateParams.aliasOrId.toLowerCase();

              WebSocketService.subscribe("o:" + aliasLower, function(data) {
                AlertService.alertWithData({"type": "info", alias: data}, undefined, 'app/outfit/alert.outfit.tpl.html')
              });

              OutfitService.get(aliasLower).then(function(response) {
                if(response.data.updateTime < new Date().getTime()) {
                  AlertService.alert(response.data.alias, "is being updated, you will be notified when it is ready", "warning", 5);
                }
                deferred.resolve(response.data);
              }).catch(function(err) {
                deferred.reject();
                AlertService.alert(aliasLower.toUpperCase(), "is being indexed, you will be notified if it exists", "warning", 5)
              });
            } else {
              OutfitService.get($stateParams.aliasOrId).then(function(response) {
                deferred.resolve(response.data);
              }).catch(function(err) {
                deferred.reject();
                AlertService.alert($stateParams.aliasOrId, "is being indexed, you will be notified if it exists", "warning", 5)
              });
            }

            return deferred.promise;
          }]
        }
      })
  }])

  .controller('OutfitViewController', ['$scope', '$state', 'OutfitService', 'resolveOutfit', 'WebSocketService', 'AlertService',
    function($scope, $state, OutfitService, resolveOutfit, WebSocketService, AlertService) {
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

      $scope.outfit = resolveOutfit;

      WebSocketService.subscribe("o:" + resolveOutfit.aliasLower, function(data) {
        OutfitService.get(resolveOutfit.aliasLower).then(function(response) {
          $scope.outfit = response.data;
        });
        AlertService.alertWithData({"type": "info", alias: data}, undefined, 'app/outfit/alert.outfit.tpl.html')
      });
    }]);
