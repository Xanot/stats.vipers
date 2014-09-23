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

    $scope.outfitHref = function(outfit) {
      if(outfit) {
        if(outfit.aliasLower) {
          return $state.href("outfit-view", {aliasOrId : outfit.aliasLower});
        } else {
          return $state.href("outfit-view", {aliasOrId : outfit.id});
        }
      }
    };

    $scope.outfitHrefSearch = function(s) {
      if(s && (s.alias || s.outfit_id)) {
        $state.go("outfit-view", {aliasOrId : s.alias || s.outfit_id})
      } else {
        $state.go("outfit");
      }
    };

    $scope.search = function(name) {
      if(name && name.length >= 3) {
        return OutfitService.search(name.toLowerCase()).then(function(response) {
          for(var i = 0; i < response.data.outfit_list.length; i++) { // https://github.com/mgcrea/angular-strap/issues/874
            response.data.outfit_list[i].label =  response.data.outfit_list[i].name;
            response.data.outfit_list[i].value = "N/A"
          }
          return response.data.outfit_list;
        })
      }
    };

    getOutfits(1)
  }])

  .factory('OutfitService', ['$http', 'UrlService', function($http, UrlService) {
    return {
      search: function(name, page) {
        return $http.jsonp("http://census.soe.com/s:soe/get/ps2:v2/outfit/?name_lower=^" + name +"&c:limit=6&c:sort=name_lower&c:show=name,alias,outfit_id&callback=JSON_CALLBACK")
      },
      get: function(aliasOrId) {
        return $http.get(UrlService.url("/outfit/" + aliasOrId))
      },
      getAll: function() {
        return $http.get(UrlService.url("/outfit"))
      }
    }
  }]);
