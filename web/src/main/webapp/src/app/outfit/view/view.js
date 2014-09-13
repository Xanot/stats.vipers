'use strict';

angular.module('outfit-view', ['utils', 'ui.router'])
  .config(['$stateProvider', function($stateProvider) {
    $stateProvider
      .state('outfit-view', {
        url: '/outfit/:aliasOrId',
        controller: 'OutfitViewController',
        templateUrl: 'app/outfit/view/view.html'
      })
  }])

  .controller('OutfitViewController', ['$scope', '$state', '$stateParams', 'Outfit', function($scope, $state, $stateParams, Outfit) {
    function getOutfitByAlias(aliasLower) {
      var query = { aliasLower : aliasLower };
      Outfit.findAll(query);

      $scope.$watch(function () {
        return Outfit.lastModified();
      }, function () {
        $scope.outfit = Outfit.filter(query)[0];
      });
    }

    function getOutfitById(id) {
      Outfit.find(id);
      Outfit.bindOne($scope, 'outfit', id);
    }

    $scope.leaderHref = function(id) {
      return $state.href('player.view', {id: id})
    };

    if($stateParams.aliasOrId.length <= 4) {
      getOutfitByAlias($stateParams.aliasOrId.toLowerCase())
    } else {
      getOutfitById($stateParams.aliasOrId)
    }
  }])

  .factory('Outfit', ['DS', 'UrlService', function(DS, UrlService) {
    return DS.defineResource({
      name: "outfit",
      baseUrl: UrlService.url("/")
    });
  }]);
