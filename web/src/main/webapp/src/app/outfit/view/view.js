'use strict';

angular.module('outfit.view', ['utils', 'ui.router'])
  .config(['$stateProvider', function($stateProvider) {
    $stateProvider
      .state('outfit.view', {
        parent: 'outfit',
        url: '/:alias',
        controller: 'OutfitViewController',
        templateUrl: 'app/outfit/view/view.html'
      })
  }])

  .controller('OutfitViewController', ['$scope', '$state', '$stateParams', 'OutfitViewController', function($scope, $state, $stateParams, OutfitViewController) {
    function getOutfit(alias) {
      OutfitViewController.getOutfitByAlias(alias.toLowerCase()).then(function(response) {
        $scope.outfit = response.data
      })
    }

    $scope.leaderHref = function(id) {
      return $state.href('player.view', {id: id})
    };

    if($stateParams.alias) {
      getOutfit($stateParams.alias)
    }
  }])

  .factory('OutfitViewController', ['$http', 'UrlService', function($http, UrlService) {
    return {
      getOutfitByAlias: function(alias) {
        return $http.get(UrlService.url("/outfit/" + alias))
      }
    }
  }]);
