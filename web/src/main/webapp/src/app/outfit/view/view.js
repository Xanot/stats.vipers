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

  .controller('OutfitViewController', ['$scope', '$stateParams', 'OutfitViewController', function($scope, $stateParams, OutfitViewController) {
    function getOutfit(alias) {
      OutfitViewController.getOutfitByAlias(alias.toLowerCase()).then(function(response) {
        $scope.outfit = response.data
      })
    }

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
