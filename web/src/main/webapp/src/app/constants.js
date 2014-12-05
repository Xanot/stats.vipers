'use strict';

angular.module('constants', [])
  .constant('Constants', {
    'PROTOCOL'      : 'http',
    'HOST'          : 'localhost',
    'PORT'          : '8080/api',
    'WebSocket' : {
      'PROTOCOL' : 'ws',
      'HOST' : 'localhost',
      'PORT' : '8081'
    }
  }
);
