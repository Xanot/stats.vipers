"use strict";

angular.module('utils', ['constants'])
  .factory('UrlService', ['Constants', function(Constants) {
    return {
      url: function(path, parameters) {
        return Arg.url(Constants.PROTOCOL + "://" + Constants.HOST + ":" + Constants.PORT + path, parameters);
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

  .filter('duration', function() {
    return function(duration, type, format) {
      return moment.duration(duration, type).format(format || "d[d] h[h]")
    };
  });

