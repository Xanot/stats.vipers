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
                AlertService.alert(err.status, err.statusText, "danger", 2)
              });
            }

            function getOutfitById(id) {
              Outfit.find(id).then(function(response) {
                deferred.resolve(response);
              }).catch(function(err) {
                deferred.reject();
                AlertService.alert(err.status, err.statusText, "danger", 2)
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
    $scope.limitRows = 50;

    Outfit.bindOne($scope, 'outfit', resolveOutfit.id);

    $scope.leaderHref = function(id) {
      return $state.href('player.view', {id: id})
    };

    $scope.increaseLimit = function() {
      $scope.limitRows += 30;
    };

    $scope.$watch("filterName", function() {
      $scope.limitRows = 50;
    });
  }])

  .factory('Outfit', ['DS', 'UrlService', function(DS, UrlService) {
    return DS.defineResource({
      name: "outfit",
      baseUrl: UrlService.url("/")
    });
  }]);
