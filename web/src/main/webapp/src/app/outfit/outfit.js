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

  .controller('OutfitController', ['$scope', '$state', function($scope, $state) {
    $scope.go = function(alias) {
      $state.transitionTo('outfit-view', {alias: alias})
    };
  }])

  .factory('OutfitService', ['$http', 'UrlService', function($http, UrlService) {
    return {
      get: function(alias) {
        return $http.get(UrlService.url("/outfit/" + alias))
      }
    }
  }]);
