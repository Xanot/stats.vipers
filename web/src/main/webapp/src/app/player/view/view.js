'use strict';

angular.module('player-view', ['utils', 'ui.router'])
  .config(['$stateProvider', function($stateProvider) {
    $stateProvider
      .state('player-view', {
        url: '/player/:name',
        controller: 'PlayerViewController',
        templateUrl: 'app/player/view/view.html',
        resolve: {
          resolvePlayer : ['$q', '$stateParams', 'PlayerService', 'NotificationService', 'WebSocketService', function($q, $stateParams, PlayerService, NotificationService, WebSocketService) {
            var deferred = $q.defer();
            var nameLower = $stateParams.name.toLowerCase();

            WebSocketService.subscribe("c:" + nameLower, function(data) {
              NotificationService.characterIndexed(data)
            });

            PlayerService.getByName(nameLower).then(function(response) {
              if((response.data.updateTime < new Date().getTime()) || response.data.weaponStats.length == 0) {
                NotificationService.characterBeingIndexed(response.data.name)
              }

              deferred.resolve(response.data);
            }).catch(function(err) {
              deferred.reject();
              NotificationService.characterBeingIndexed($stateParams.name)
            });

            return deferred.promise;
          }]
        }
      })
  }])

  .controller('PlayerViewController', ['$scope', '$filter', 'PlayerService', 'resolvePlayer', 'WebSocketService', 'NotificationService', 'localStorageService',
    function($scope, $filter, PlayerService, resolvePlayer, WebSocketService, NotificationService, localStorageService) {
      $scope.player = resolvePlayer;

      var defaultSort = function() {
        switch(localStorageService.get("character").sort) {
          case "Used On":
            $scope.predicate = '_1.lastSaveDate';
            break;
          case "Kills":
            $scope.predicate = '_1.killCount';
            break;
        }
      };

      var defaultOrder = function() {
        if(localStorageService.get("character").order == "desc") {
          $scope.reverse = !$scope.reverse
        }
      };

      defaultSort();
      defaultOrder();

      $scope.showHistory = function(stat) {
        if(stat.history) {
          delete stat.history
        } else {
          PlayerService.getCharactersWeaponStatHistory(resolvePlayer.id, stat._2.id).then(function(response) {
            stat.history = {
              options: {
                title: {
                  text: stat._2.name
                },
                tooltip: {
                  valueDecimals: 2,
                  style: {
                    padding: 10,
                    fontWeight: 'bold'
                  }
                },
                legend: {
                  layout: 'vertical',
                  align: 'right',
                  verticalAlign: 'middle',
                  borderWidth: 0
                }
              },

              size: {
                width: 512,
                height: 300
              },

              xAxis: {
                type: 'datetime',
                dateTimeLabelFormats: {
                  month: '%e. %b',
                  year: '%b'
                },
                title: {
                  text: 'Date'
                }
              },

              yAxis: {
                title: {text: 'Value'}
              },

              series: [{
                name: 'ACC',
                data: $.map(response.data, function(s) {
                  return [[s.lastSaveDate * 1000, $filter('statCalc')(s, 'acc')]]
                }),
                tooltip: {
                  valueSuffix: '%'
                }
              }, {
                name: 'HSR',
                data: $.map(response.data, function(s) {
                  return [[s.lastSaveDate * 1000, $filter('statCalc')(s, 'hsr')]];
                }),
                tooltip: {
                  valueSuffix: '%'
                }
              }, {
                name: 'KDR',
                visible: false,
                data: $.map(response.data, function(s) {
                  return [[s.lastSaveDate * 1000, $filter('statCalc')(s, 'kdr')]];
                })
              }, {
                name: 'KPH',
                visible: false,
                data: $.map(response.data, function(s) {
                  return [[s.lastSaveDate * 1000, $filter('statCalc')(s, 'kph')]];
                })
              }, {
                name: 'SPM',
                visible: false,
                data: $.map(response.data, function(s) {
                  return [[s.lastSaveDate * 1000, $filter('statCalc')(s, 'spm')]];
                })
              }]
            };
          });
        }
      };

      // Retrieve the character if it is updated
      WebSocketService.subscribe("c:" + resolvePlayer.nameLower, function(data) {
        PlayerService.getByName(data).then(function(response) {
          $scope.player = response.data;
        });
        NotificationService.characterIndexed(data)
      });

      // Don't retrieve the character if we are out of this state
      $scope.$on('$destroy', function() {
        WebSocketService.subscribe("c:" + resolvePlayer.nameLower, function(data) {
          NotificationService.characterIndexed(data)
        });
      });
  }]);
