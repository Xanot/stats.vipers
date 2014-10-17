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

  .controller('OutfitController', ['$scope', '$state', 'OutfitService', function($scope, $state, OutfitService) {
    function getOutfits(page) {
      OutfitService.getAll().then(function(response) {
        $scope.outfits = response.data
      });
    }

    getOutfits(1)
  }])

  .factory('OutfitService', ['$http', 'UrlService', function($http, UrlService) {
    return {
      get: function(alias) {
        return $http.get(UrlService.url("/outfit/" + alias))
      },
      getAll: function() {
        return $http.get(UrlService.url("/outfit"))
      }
    }
  }]);
