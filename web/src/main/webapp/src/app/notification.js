'use strict';

angular.module('notification', ['LocalStorageModule'])
  .factory('NotificationService', ['$alert', 'localStorageService', function($alert, localStorageService) {
    var alert = function(title, content, type, duration, template) {
      $alert({
        title: "" + title,
        content: content,
        type: type,
        duration: duration,
        template: template || "app/alert.tpl.html",
        placement: 'stacked',
        container: "#alerts"
      });
    };
    var alertWithData = function(data, duration, template) {
      alert(undefined, undefined, data, duration, template);
    };

    return {
      outfitBeingIndexed: function(identity) {
        if(localStorageService.get("notification").beingIndexedEnabled) {
          alert(identity, "is being updated", "warning", localStorageService.get("notification").beingIndexedExpireAfter);
        }
      },
      outfitIndexed: function(alias) {
        if(localStorageService.get("notification").indexedEnabled) {
          alertWithData({"type": "info", alias: alias}, localStorageService.get("notification").indexedExpireAfter, 'app/outfit/alert.outfit.tpl.html')
        }
      },

      characterBeingIndexed: function(identity) {
        if(localStorageService.get("notification").beingIndexedEnabled) {
          alert(identity, "is being updated", "warning", localStorageService.get("notification").beingIndexedExpireAfter);
        }
      },
      characterIndexed: function(name) {
        if(localStorageService.get("notification").indexedEnabled) {
          alertWithData({"type": "info", name: name}, localStorageService.get("notification").indexedExpireAfter, 'app/player/alert.player.tpl.html')
        }
      }
    }
  }]);
