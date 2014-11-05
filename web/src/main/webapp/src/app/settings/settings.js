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

    $scope.setRealTimeDefaults = function() {
      SettingsService.setRealTimeDefaults();
    };

    $scope.setNotificationDefaults = function() {
      SettingsService.setNotificationDefaults();
    };

    $scope.setCharacterDefaults = function() {
      SettingsService.setCharacterDefaults();
    };

    $scope.setAbbrDefaults = function() {
      SettingsService.setAbbrDefaults();
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

    var realTimeSettings = category("realTime", {enabled: true});
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
    var abbreviationSettings = category("abbr", {enabled: false});

    return {
      init: function() {
        realTimeSettings.init();
        notificationSettings.init();
        characterSettings.init();
        abbreviationSettings.init();
      },
      setRealTimeDefaults: function() {
        realTimeSettings.setDefaults();
      },
      setNotificationDefaults: function() {
        notificationSettings.setDefaults();
      },
      setCharacterDefaults: function() {
        characterSettings.setDefaults();
      },
      setAbbrDefaults: function() {
        abbreviationSettings.setDefaults();
      }
    }
  }]);