'use strict';

angular.module('outfit', ['utils', 'ui.router'])
  .config(['$stateProvider', function($stateProvider) {
    $stateProvider.state('outfit', {
      url: '/outfit',
      controller: 'OutfitController',
      templateUrl: 'app/outfit/outfit.html'
    });
  }])

  .controller('OutfitController', ['$scope', 'OutfitService', function($scope, OutfitService) {
    $scope.getOutfit = function(alias) {
      OutfitService.getOutfitByAlias(alias.toLowerCase()).then(function(response) {
        $scope.outfit = response.data
      })
    };
  }])

  .factory('OutfitService', ['$http', 'UrlService', function($http, UrlService) {
    return {
      getOutfitByAlias: function(alias) {
        return $http.get(UrlService.url("/outfit/" + alias))
      }
    }
  }]);
