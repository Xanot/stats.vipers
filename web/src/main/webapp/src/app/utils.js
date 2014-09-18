"use strict";

angular.module('utils', ['constants'])
  .factory('UrlService', ['Constants', function(Constants) {
    return {
      url: function(path, parameters) {
        return Arg.url(Constants.PROTOCOL + "://" + Constants.HOST + ":" + Constants.PORT + path, parameters);
      }
    };
  }])

  .factory('AlertService', ['$alert', function($alert) {
    return {
      alert : function(title, content, type, duration, template, placement, container) {
        $alert({
          title: "" + title,
          content: content,
          type: type,
          duration: duration,
          template: template || "app/alert.tpl.html",
          placement: placement || 'stacked',
          container: container || "#alerts"
        });
      },
      alertWithData : function(data, duration, template, placement, container) {
        this.alert(undefined, undefined, data, duration, template, placement, container);
      }
    }
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
  });

