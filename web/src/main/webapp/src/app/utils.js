angular.module('utils', ['constants'])
  .factory('UrlService', ['Constants', function(Constants) {
    return {
      url: function(path, parameters) {
        return Arg.url(Constants.REST_PROTOCOL + "://" + Constants.HOST + ":" + Constants.PORT + path, parameters);
      }
    };
  }]);

