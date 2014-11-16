'use strict';

angular.module('weapon', ['utils', 'ui.router'])
  .config(['$stateProvider', function($stateProvider) {
    $stateProvider
      .state('weapon', {
        url: '/weapon/:itemId',
        controller: 'WeaponController',
        templateUrl: 'app/weapon/weapon.html',
        resolve: {
          resolveWeapon: ['$stateParams', 'WeaponService', function($stateParams, WeaponService) {
            return WeaponService.getWeapon($stateParams.itemId)
          }]
        }
      })
  }])

  .controller('WeaponController', ['$scope', 'resolveWeapon', function($scope, resolveWeapon) {
    $scope.weapon = resolveWeapon.data;
  }])

  .factory('WeaponService', ['$http', 'UrlService', function($http, UrlService) {
    return {
      getWeapon: function(id) {
        return $http.get(UrlService.url("/weapon/" + id))
      }
    }
  }]);
