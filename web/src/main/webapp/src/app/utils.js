"use strict";

angular.module('utils', ['constants'])
  .factory('UrlService', ['Constants', function(Constants) {
    return {
      url: function(path, parameters) {
        return Arg.url(Constants.REST_PROTOCOL + "://" + Constants.HOST + ":" + Constants.PORT + path, parameters);
      }
    };
  }])

  .filter('outfitRank', function() {
    return function(rank) {
      switch(rank) {
        case 1:
          return "Leader";
          break;
        case 2:
          return "Officer";
          break;
        case 3:
          return "Member";
          break;
        case 4:
          return "Private";
          break;
        case 5:
          return "Recruit";
          break;
      }
    };
  })

  .factory('ProgressInterceptor', ['$q', function($q) {
    return {
      request: function (config) {
        NProgress.start();
        return config || $q.when(config);
      },
      response: function (response) {
        NProgress.done();
        return response || $q.when(response);
      },
      responseError: function (rejection) {
        NProgress.done();
        return $q.reject(rejection);
      }
    }
  }]);

