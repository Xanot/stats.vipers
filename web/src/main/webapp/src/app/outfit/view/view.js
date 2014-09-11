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

  .controller('OutfitViewController', ['$scope', '$state', '$stateParams', 'Outfit', function($scope, $state, $stateParams, Outfit) {
    function getOutfit(aliasLower) {
      Outfit.find(aliasLower);
      Outfit.bindOne($scope, 'outfit', aliasLower);
    }

    $scope.leaderHref = function(id) {
      return $state.href('player.view', {id: id})
    };

    if($stateParams.alias) {
      getOutfit($stateParams.alias.toLowerCase())
    }
  }])

  .factory('Outfit', ['DS', 'UrlService', function(DS, UrlService) {
    return DS.defineResource({
      name: "outfit",
      baseUrl: UrlService.url("/"),
      idAttribute: 'aliasLower'
    });
  }]);
