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
        case 'kph':
          value = (s.killCount / s.secondsPlayed) * 3600;
          break;
        case 'spm':
          value = (s.score / s.secondsPlayed) * 60;
          break;
      }

      return value;
    }
  }])

  .directive('abr', ['localStorageService', function(localStorageService) {
    return {
      restrict: 'E',
      replace: true,
      scope: {
        abbr: "@"
      },
      template: '<span ng-switch="abbrEnabled"><span ng-switch-when="true"><abbr title="{{ title }}">{{ abbr.toUpperCase() }}</abbr></span><span ng-switch-when="false">{{ abbr.toUpperCase() }}</span></span>',
      link: function(scope, elem, attrs) {
        scope.abbrEnabled = localStorageService.get("abbr").enabled;
        if(scope.abbrEnabled) {
          switch(scope.abbr) {
            case "kdr":
              scope.title = "Kills/Deaths";
              break;
            case "spm":
              scope.title = "Score per minute";
              break;
            case "kph":
              scope.title = "Kills per hour";
              break;
            case "acc":
              scope.title = "Accuracy";
              break;
            case "hsr":
              scope.title = "Headshot ratio";
              break;
            case "br":
              scope.title = "Battle rank";
              break;
          }
        }
      }
    }
  }])

  .filter('duration', function() {
    return function(duration, type, format) {
      return moment.duration(duration, type).format(format || "d[d] h[h]")
    };
  });

