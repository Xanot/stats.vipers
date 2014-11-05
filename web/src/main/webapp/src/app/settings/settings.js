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
    function category(name, defaults) {
      return {
        defaults: JSON.parse(JSON.stringify(defaults)),
        init: function() {
          localStorageService.bind($rootScope, name, JSON.parse(JSON.stringify(this.defaults)))
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
      "columnOptions": ["Kills", "ACC", "HSR", "KPH", "SPM", "Last used", "Total time"],
      "columns": ["Kills", "ACC", "HSR", "KPH", "SPM", "Last used", "Total time"],
      "sortOptions" : ["Used On", "Kills"],
      "sort": "Kills",
      "orderOptions": ["desc", "asc"],
      "order": "desc"
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