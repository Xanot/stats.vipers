'use strict';

(function() {
  var conn;
  var subscribers = [];

  function connect(address) {
    if(conn) conn.close();
    conn = new WebSocket(address);
    conn.onmessage = function(evt) {
      var json = JSON.parse(evt.data);
      publish(json.event, json.data);
    };
    conn.onclose = function() { publish('onClose', null); };
    conn.onopen = function()  { publish('onOpen',  null); };
    conn.onerror = function() { publish('onError', null); };
  }

  function close() {
    if(conn) conn.close();
  }

  function publish(eventName, eventData) {
    var chain = subscribers[eventName];
    if(typeof chain == 'undefined') return;
    chain(eventData);
  }

  function bind(eventName, callback) {
    console.group("Binding callback to " + eventName + ": ");
    console.log(callback);
    console.groupEnd();

    if(!subscribers[eventName]) {
      subscribers[eventName] = callback;
      return true
    } else {
      subscribers[eventName] = callback;
      return false;
    }
  }

  function unbind(eventName) {
    var index = subscribers.indexOf(eventName);
    subscribers.splice(index, 1);
  }

  function send(eventName, eventData){
    var payload = JSON.stringify({event: eventName, data: eventData});
    conn.send(payload);
  }

  angular.module('websocket', ['constants'])
    .run(['Constants', '$rootScope', function(Constants, $rootScope) {
      $rootScope.$watch("global.websocket", function(newVal) {
        if(newVal) {
          connect(Constants.WebSocket.PROTOCOL + "://" + Constants.WebSocket.HOST + ":" + Constants.WebSocket.PORT);
        } else {
          close();
        }
      });
    }])
    .factory('WebSocketService', ['$rootScope', function($rootScope) {
      var pending = [];
      $rootScope.isConnected = false;

      bind("onOpen", function() {
        $rootScope.isConnected = true;
        $rootScope.$digest();
        for(var i = 0; i < pending.length; i++) {
          send("subscribe", pending[i])
        }
      });

      bind("onClose", function() {
        $rootScope.isConnected = false;
        $rootScope.$digest();
      });

      return {
        subscribe: function(eventName, callback) {
          if(bind(eventName, callback)) {
            if($rootScope.isConnected) {
              send("subscribe", eventName);
            } else {
              pending.push(eventName)
            }
          }
        },
        unsubscribe: function(eventName, callback) {
          unbind(eventName, callback);
          send("unsubscribe", eventName);
        }
      };
    }]);
})();