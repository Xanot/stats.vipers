'use strict';

angular.module('player', ['player.view', 'utils', 'ui.router'])
  .config(['$stateProvider', function($stateProvider) {
    $stateProvider
      .state('player', {
        url: '/player',
        controller: 'PlayerController',
        templateUrl: 'app/player/player.html'
      })
  }])

  .controller('PlayerController', ['$scope', '$stateParams', 'PlayerController', function($scope, $stateParams, PlayerController) {
    $scope.itemsPerPage = 20;
    $scope.curentPage = 1;

    function getPlayers(page) {
      PlayerController.getPlayers({limit : $scope.itemsPerPage, start: (page - 1) * $scope.itemsPerPage }).then(function(response) {
        $scope.players = response.data
      })
    }

//    getPlayers(1)
  }])

  .factory('PlayerController', ['$http', 'UrlService', function($http, UrlService) {
    return {
      getPlayers: function(alias, page) {
        return $http.get(UrlService.url("/player/" + alias, page))
      }
    }
  }]);
