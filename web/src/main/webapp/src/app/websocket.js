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
    for(var i = 0; i < chain.length; i++) {
      chain[i](eventData);
    }
  }

  function bind(eventName, callback) {
    console.group("Binding callback to " + eventName + ": ");
    console.log(callback);
    console.groupEnd();
    subscribers[eventName] = subscribers[eventName] || [];
    subscribers[eventName].push(callback);
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
          this.bind(eventName, callback);
          if(isOpen) {
            send("subscribe", eventName);
          } else {
            pending.push(eventName)
          }
        },
        unsubscribe: function(eventName, callback) {
          this.unbind(eventName, callback);
          send("unsubscribe", eventName);
        },
        bind : bind,
        unbind : function(eventName, callback) {
          var index = subscribers[eventName].indexOf(callback);
          subscribers[eventName].splice(index, 1);
        }
      };
    }]);
})();