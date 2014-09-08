'use strict';

angular.module('outfit', ['ui.router'])
  .config(['$stateProvider', function($stateProvider) {
    $stateProvider.state('outfit', {
      url: '/outfit',
      controller: 'OutfitController',
      templateUrl: 'app/outfit/outfit.html'
    });
  }])

  .controller('OutfitController', ['$scope', 'OutfitService', function($scope, OutfitService) {
    $scope.getOutfit = function(alias) {
      OutfitService.getOutfitByAlias("VIPR").then(function(response) {
        $scope.test = response.data
      })
    };

    $scope.getOutfit();
  }])

  .factory('OutfitService', ['$http', 'UrlService', function($http, UrlService) {
    return {
      getOutfitByAlias: function(alias) {
        return $http.get(UrlService.url("/outfit/" + alias))
      }
    }
  }]);
