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

  .controller('PlayerController', ['$scope', 'PlayerService', function($scope, PlayerService) {
    $scope.search = function(name) {
      if(name && name.length >= 3) {
        return PlayerService.search(name.toLowerCase()).then(function(response) {
          return response.data.character_name_list;
        })
      }
    };
  }])

  .factory('PlayerService', ['$http', 'UrlService', function($http) {
    return {
      search: function(name, page) {
        return $http.jsonp("http://census.soe.com/s:soe/get/ps2:v2/character_name/?name.first_lower=^" + name +"&c:limit=6&c:sort=name.first_lower&c:show=name.first,character_id&callback=JSON_CALLBACK")
      }
    }
  }]);
