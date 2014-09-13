'use strict';

angular.module('outfit-view', ['utils', 'ui.router'])
  .config(['$stateProvider', function($stateProvider) {
    $stateProvider
      .state('outfit-view', {
        url: '/outfit/:aliasOrId',
        controller: 'OutfitViewController',
        templateUrl: 'app/outfit/view/view.html',
        resolve: {
          resolveOutfit : ['$q', '$stateParams', 'Outfit', 'AlertService', function($q, $stateParams, Outfit, AlertService) {
            function getOutfitByAlias(aliasLower) {
              var query = { aliasLower : aliasLower };
              Outfit.findAll(query).then(function(response) {
                deferred.resolve(response[0]);
              }).catch(function(err) {
                deferred.reject();
                AlertService.alert(err.status, err.statusText, "danger")
              });
            }

            function getOutfitById(id) {
              Outfit.find(id).then(function(response) {
                deferred.resolve(response);
              }).catch(function(err) {
                deferred.reject();
                AlertService.alert(err.status, err.statusText, "danger")
              });
            }

            var deferred = $q.defer();

            if($stateParams.aliasOrId.length <= 4) {
              getOutfitByAlias($stateParams.aliasOrId.toLowerCase())
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

    Outfit.bindOne($scope, 'outfit', resolveOutfit.id);

    $scope.leaderHref = function(id) {
      return $state.href('player.view', {id: id})
    };
  }])

  .factory('Outfit', ['DS', 'UrlService', function(DS, UrlService) {
    return DS.defineResource({
      name: "outfit",
      baseUrl: UrlService.url("/")
    });
  }]);
