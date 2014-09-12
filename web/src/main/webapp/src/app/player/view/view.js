'use strict';

angular.module('player.view', ['utils', 'ui.router'])
  .config(['$stateProvider', function($stateProvider) {
    $stateProvider
      .state('player.view', {
        url: '/:id',
        controller: 'PlayerViewController',
        templateUrl: 'app/player/view/view.html'
      })
  }])

  .controller('PlayerViewController', ['$scope','$stateParams', 'PlayerViewService', function($scope, $stateParams, PlayerViewService) {
    function getPlayer(id) {
      PlayerViewService.getPlayer(id).then(function(response) {
        $scope.player = response.data
      })
    }

    getPlayer($stateParams.id)
  }])

  .factory('PlayerViewService', ['$http', 'UrlService', function($http, UrlService) {
    return {
      getPlayer: function(id) {
        return $http.get(UrlService.url("/player/" + id))
      }
    }
  }]);
