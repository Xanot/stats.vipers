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

    $scope.orderOptions = [
      "desc",
      "asc"
    ];

    $scope.sortOptions = [
      "Used On",
      "Kills"
    ];

    $scope.setRealTimeDefaults = function() {
      SettingsService.setRealTimeDefaults();
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

    var realTimeSettings = category("realTime", {enabled: true});
    var notificationSettings = category("notification",
      {"beingIndexedEnabled":true, "beingIndexedExpireAfter": 5,
        "indexedEnabled": true, "indexedExpireAfter": 60}
    );
    var characterSettings = category("character", {"sort": "Used On", "order": "desc"});

    return {
      init: function() {
        realTimeSettings.init();
        notificationSettings.init();
        characterSettings.init();
      },
      setRealTimeDefaults: function() {
        realTimeSettings.setDefaults();
      },
      setNotificationDefaults: function() {
        notificationSettings.setDefaults();
      },
      setCharacterDefaults: function() {
        characterSettings.setDefaults();
      }
    }
  }]);