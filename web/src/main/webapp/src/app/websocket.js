'use strict';

(function() {
  var conn;
  var subscribers = [];

  function connect(address) {
    conn = new WebSocket(address);
    conn.onmessage = function(evt) {
      var json = JSON.parse(evt.data);
      publish(json.event, json.data);
    };
    conn.onclose = function() { publish('onClose', null); };
    conn.onopen = function()  { publish('onOpen',  null); };
    conn.onerror = function() { publish('onError', null); };
  }

  function publish(eventName, eventData) {
    var chain = subscribers[eventName];
    if(typeof chain == 'undefined') return;
    chain(eventData);
  }

  function bind(eventName, callback) {
    if(!subscribers[eventName]) {
      console.group("Binding callback to " + eventName + ": ");
      console.log(callback);
      console.groupEnd();
      subscribers[eventName] = callback;
      return true
    } else {
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
    .run(['Constants', function(Constants) {
      connect(Constants.WebSocket.PROTOCOL + "://" + Constants.WebSocket.HOST + ":" + Constants.WebSocket.PORT);
    }])
    .factory('WebSocketService', [function() {
      var pending = [];
      var isOpen = false;

      bind("onOpen", function() {
        isOpen = true;
        for(var i = 0; i < pending.length; i++) {
          send("subscribe", pending[i])
        }
      });

      bind("onClose", function() {
        isOpen = false;
      });

      return {
        subscribe: function(eventName, callback) {
          if(bind(eventName, callback)) {
            if(isOpen) {
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