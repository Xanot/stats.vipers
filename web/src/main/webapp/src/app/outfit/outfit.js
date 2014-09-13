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

  .controller('OutfitController', ['$scope', '$state', 'OutfitBasic', 'OutfitService', function($scope, $state, OutfitBasic, OutfitService) {
    function getOutfits(page) {
      OutfitBasic.findAll({});
      OutfitBasic.bindAll($scope, 'outfits', {});
    }

    $scope.outfitHref = function(aliasLower) {
      return $state.href("outfit-view", {aliasOrId : aliasLower});
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

  .factory('OutfitBasic', ['DS', 'UrlService', function(DS, UrlService) {
    return DS.defineResource({
      name: "outfitBasic",
      endpoint: "outfit",
      baseUrl: UrlService.url("/")
    });
  }])

  .factory('OutfitService', ['$http', function($http) {
    return {
      search: function(name, page) {
        return $http.jsonp("http://census.soe.com/s:soe/get/ps2:v2/outfit/?name_lower=^" + name +"&c:limit=6&c:sort=name_lower&c:show=name,alias,outfit_id&callback=JSON_CALLBACK")
      }
    }
  }]);
