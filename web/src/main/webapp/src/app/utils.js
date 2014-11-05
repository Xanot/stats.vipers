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

  .filter('profile', [function() {
    return function(str) {
      if(str != undefined) {
        var profiles = str.split(",");
        var p = [];

        if(_.contains(profiles, "2") || _.contains(profiles, "17") || _.contains(profiles, "10")) {
          p.push({name : "Infiltrator", imagePath: "files/ps2/images/static/204.png"})
        }
        if(_.contains(profiles, "4") || _.contains(profiles, "19") || _.contains(profiles, "12")) {
          p.push({name : "Light Assault", imagePath: "files/ps2/images/static/62.png"})
        }
        if(_.contains(profiles, "5") || _.contains(profiles, "20") || _.contains(profiles, "13")) {
          p.push({name : "Combat Medic", imagePath: "files/ps2/images/static/65.png"})
        }
        if(_.contains(profiles, "6") || _.contains(profiles, "21") || _.contains(profiles, "14")) {
          p.push({name : "Engineer", imagePath: "files/ps2/images/static/201.png"})
        }
        if(_.contains(profiles, "7") || _.contains(profiles, "22") || _.contains(profiles, "15")) {
          p.push({name : "Heavy Assault", imagePath: "files/ps2/images/static/59.png"})
        }
        if(_.contains(profiles, "8") || _.contains(profiles, "23") || _.contains(profiles, "16")) {
          p.push({name : "MAX", imagePath: "files/ps2/images/static/207.png"})
        }

        return p;
      }
    }
  }])

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
        scope.abbrEnabled = localStorageService.get("global").abbr;
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

