'use strict';

angular.module('settings', ['ui.router', 'LocalStorageModule'])
  .config(['$stateProvider', function($stateProvider) {
    $stateProvider
      .state('settings', {
        url: '/settings',
        controller: 'SettingsController',
        templateUrl: 'app/settings/settings.html'
      });
  }])

  .controller('SettingsController', ['$scope', 'SettingsService', function($scope, SettingsService) {
    $scope.notificationTabs = [
      {title:'Being Indexed', page: 'being-indexed.html'},
      {title:'Indexed', page: 'indexed.html'}
    ];
    $scope.notificationTabs.activeTab = 0;

    $scope.setGlobalDefaults = function() {
      SettingsService.setGlobalDefaults();
    };

    $scope.setNotificationDefaults = function() {
      SettingsService.setNotificationDefaults();
    };

    $scope.setCharacterDefaults = function() {
      SettingsService.setCharacterDefaults();
    };
  }])

  .factory('SettingsService', ['$rootScope', 'localStorageService', function($rootScope, localStorageService) {
    function category(name, defaults, migration) {
      return {
        init: function() {
          if(migration && migration(_.cloneDeep(defaults))) {
            this.setDefaults();
          } else {
            localStorageService.bind($rootScope, name, _.cloneDeep(defaults));
          }
        },
        setDefaults: function() {
          localStorageService.remove(name);
          this.init();
        }
      }
    }

    var globalSettings = category("global", {
      websocket: true,
      abbr: false
    });
    var notificationSettings = category("notification", {
      "beingIndexedEnabled":true,
      "beingIndexedExpireAfter": 5,
      "indexedEnabled": true,
      "indexedExpireAfter": 60
    });
    var characterSettings = category("character", {
      "showWeaponImages": true,
      "columnOptions": ["Kills", "KDR", "ACC", "HSR", "KPH", "SPM", "Last used", "Total time"],
      "columns": ["Kills", "KDR", "ACC", "HSR", "KPH", "SPM", "Last used", "Total time"],
      "sortOptions" : ["Used On", "Kills"],
      "sort": "Kills",
      "orderOptions": ["desc", "asc"],
      "order": "desc",
      "weaponHistoryStatsOptions": ["KDR", "ACC", "HSR", "KPH", "SPM"],
      "weaponHistoryStats": ["ACC"]
    }, function(defaults) {
      var character = localStorageService.get("character");
      if(character) {
        if(!_.isEqual(character.columnOptions, defaults.columnOptions) ||
          !_.isEqual(character.sortOptions, defaults.sortOptions) ||
          !_.isEqual(character.orderOptions, defaults.orderOptions) ||
          !_.isEqual(character.weaponHistoryStatsOptions, defaults.weaponHistoryStatsOptions)) {
          return true
        }
      }
      return false;
    });

    return {
      init: function() {
        globalSettings.init();
        notificationSettings.init();
        characterSettings.init();
      },
      setGlobalDefaults: function() {
        globalSettings.setDefaults();
      },
      setNotificationDefaults: function() {
        notificationSettings.setDefaults();
      },
      setCharacterDefaults: function() {
        characterSettings.setDefaults();
      }
    }
  }]);