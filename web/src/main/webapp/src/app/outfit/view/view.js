'use strict';

angular.module('outfit-view', ['utils', 'ui.router', 'websocket'])
  .config(['$stateProvider', function($stateProvider) {
    $stateProvider
      .state('outfit-view', {
        url: '/outfit/:aliasOrId',
        controller: 'OutfitViewController',
        templateUrl: 'app/outfit/view/view.html',
        resolve: {
          resolveOutfit : ['$q', '$stateParams', 'Outfit', 'AlertService', 'WebSocketService', function($q, $stateParams, Outfit, AlertService, WebSocketService) {
            function getOutfitByAlias(aliasLower) {
              var query = { aliasLower : aliasLower };
              Outfit.findAll(query).then(function(response) {
                deferred.resolve(response[0]);
              }).catch(function(err) {
                deferred.reject();
                AlertService.alert(aliasLower.toUpperCase(), "is being indexed, you will be notified if it exists", "warning", 5)
              });
            }

            function getOutfitById(id) {
              Outfit.find(id).then(function(response) {
                deferred.resolve(response);
              }).catch(function(err) {
                deferred.reject();
                AlertService.alert(id, "is being indexed, you will be notified if it exists", "warning", 5)
              });
            }

            var deferred = $q.defer();

            if($stateParams.aliasOrId.length <= 4) {
              var aliasLower = $stateParams.aliasOrId.toLowerCase();

              WebSocketService.subscribe("o:" + aliasLower, function(data) {
                AlertService.alertWithData({"type": "info", alias: data}, undefined, 'app/outfit/alert.outfit.tpl.html')
              });

              getOutfitByAlias(aliasLower)
            } else {
              getOutfitById($stateParams.aliasOrId)
            }

            return deferred.promise;
          }]
        }
      })
  }])

  .controller('OutfitViewController', ['$scope', '$state', 'Outfit', 'resolveOutfit',
    function($scope, $state, Outfit, resolveOutfit) {
      $scope.limitRows = 30;

      $scope.increaseLimit = function() {
        $scope.limitRows += 30;
      };

      $scope.$watch("filterName", function() {
        $scope.limitRows = 30;
      });

      $scope.leaderHref = function(id) {
        return $state.href('player.view', {id: id})
      };

      Outfit.bindOne($scope, 'outfit', resolveOutfit.id);
    }])

  .factory('Outfit', ['DS', 'UrlService', function(DS, UrlService) {
    return DS.defineResource({
      name: "outfit",
      baseUrl: UrlService.url("/")
    });
  }]);
