'use strict';

angular.module('outfit', ['outfit-view', 'utils', 'ui.router'])
  .config(['$stateProvider', function($stateProvider) {
    $stateProvider
      .state('outfit', {
        url: '/outfit',
        controller: 'OutfitController',
        templateUrl: 'app/outfit/outfit.html'
      })
  }])

  .controller('OutfitController', ['$scope', '$state', 'OutfitBasic', function($scope, $state, OutfitBasic) {
    function getOutfits(page) {
      OutfitBasic.findAll({});
      OutfitBasic.bindAll($scope, 'outfits', {});
    }

    $scope.outfitHref = function(aliasLower) {
      return $state.href("outfit-view", {alias : aliasLower});
    };

    getOutfits(1)
  }])

  .factory('OutfitBasic', ['DS', 'UrlService', function(DS, UrlService) {
    return DS.defineResource({
      name: "outfitBasic",
      endpoint: "outfit",
      baseUrl: UrlService.url("/")
    });
  }]);
