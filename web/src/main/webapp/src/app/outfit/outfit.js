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

  .controller('OutfitController', ['$scope', '$state', '$stateParams', 'OutfitService', function($scope, $state, OutfitService) {
    $scope.itemsPerPage = 20;
    $scope.curentPage = 1;

    function getOutfits(page) {
      OutfitService.getOutfits({limit : $scope.itemsPerPage, start: (page - 1) * $scope.itemsPerPage }).then(function(response) {
        $scope.outfits = response.data
      })
    }

    $scope.toOutfit = function(alias) {
      $state.go("outfit.view", {alias : alias});
    };

//    getOutfits(1)
  }])

  .factory('OutfitService', ['$http', 'UrlService', function($http, UrlService) {
    return {
      getOutfits: function(page) {
        return $http.get(UrlService.url("/outfit", page))
      }
    }
  }]);
