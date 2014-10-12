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

  .filter('statCalc', [function() {
    return function(s, item) {
      var value;

      switch(item) {
        case 'acc':
          value = (s.hitCount / s.fireCount) * 100;
          break;
        case 'hsr':
          value = (s.headshotCount / s.killCount) * 100;
          break;
        case 'kdr':
          value = s.killCount / s.deathCount;
          break;
        case 'kpm':
          value = (s.killCount / s.secondsPlayed) * 3600;
          break;
        case 'spm':
          value = (s.score / s.secondsPlayed) * 60;
          break;
      }

      return value;
    }
  }])

  .filter('duration', function() {
    return function(duration, type, format) {
      return moment.duration(duration, type).format(format || "d[d] h[h]")
    };
  });

