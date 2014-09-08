'use strict';

angular.module('constants', [])
  .constant('Constants', {
    'REST_PROTOCOL' : 'http',
    'HOST'          : 'localhost',
    'PORT'          : '8080',
    'WebSocketService' : {
      'PROTOCOL' : 'wss',
      'HOST' : 'localhost',
      'PORT' : '8081'
    }
  }
);
