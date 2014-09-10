'use strict';

angular.module('outfit', ['outfit.view', 'utils', 'ui.router'])
  .config(['$stateProvider', function($stateProvider) {
    $stateProvider
      .state('outfit', {
        url: '/outfit',
        controller: 'OutfitController',
        templateUrl: 'app/outfit/outfit.html'
      })
  }])

  .controller('OutfitController', ['$scope', '$state', '$stateParams', 'OutfitController', function($scope, $state, OutfitController) {
    $scope.itemsPerPage = 20;
    $scope.curentPage = 1;

    function getOutfits(page) {
      OutfitController.getOutfits({limit : $scope.itemsPerPage, start: (page - 1) * $scope.itemsPerPage }).then(function(response) {
        $scope.outfits = response.data
      })
    }

    $scope.toOutfit = function(alias) {
      $state.go("outfit.view", {alias : alias});
    };

//    getOutfits(1)
  }])

  .factory('OutfitController', ['$http', 'UrlService', function($http, UrlService) {
    return {
      getOutfits: function(page) {
        return $http.get(UrlService.url("/outfit", page))
      }
    }
  }]);
