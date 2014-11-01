'use strict';

var gulp = require('gulp'),
    uglify = require('gulp-uglify'),
    concat = require('gulp-concat'),
    minifycss = require('gulp-minify-css'),
    size = require('gulp-size'),
    bower = require('gulp-bower'),
    clean = require('gulp-clean'),
    karma = require('gulp-karma');

var buildDest = "../resources/client";

var jsLibs = [
    "./bower_components/moment/min/moment.min.js",
    "./bower_components/moment-duration-format/lib/moment-duration-format.js",
    "./lib/arg.js/arg-1.2.min.js",
    "./lib/marked/marked-0.3.2.min.js",
    "./bower_components/lodash/dist/lodash.min.js",
    "./bower_components/highcharts/adapters/standalone-framework.js",
    "./bower_components/highcharts/highcharts.js",

    "./bower_components/angular/angular.min.js",
    "./bower_components/angular-sanitize/angular-sanitize.min.js",
    "./bower_components/angular-animate/angular-animate.min.js",
    "./bower_components/angular-ui-router/release/angular-ui-router.min.js",
    "./bower_components/angular-strap/dist/angular-strap.min.js",
    "./bower_components/angular-strap/dist/angular-strap.tpl.min.js",
    "./bower_components/angular-moment/angular-moment.min.js",
    "./bower_components/angular-marked/angular-marked.min.js",
    "./bower_components/angular-loading-bar/build/loading-bar.min.js",
    "./lib/ngInfiniteScroll/infinite-scroll-1.1.2master.js",
    "./bower_components/angular-local-storage/dist/angular-local-storage.min.js",
    "./bower_components/highcharts-ng/dist/highcharts-ng.min.js"
];

var cssLibs = [
    "./bower_components/fontawesome/css/font-awesome.min.css",
    "./lib/bootstrap-themes/css/darkly.css",
    "./lib/angular-strap-additions/bootstrap-additions.min.css",
    "./bower_components/angular-motion/dist/angular-motion.min.css",
    "./bower_components/angular-loading-bar/build/loading-bar.min.css"
];

var fonts = [
    "./bower_components/fontawesome/fonts/*"
];

gulp.task('clean', function() {
  return gulp.src(buildDest, {read: false}).pipe(clean({force: true}));
});

gulp.task('minify-app-js', function () {
  return gulp.src('./src/app/**/*.js')
    .pipe(uglify())
    .pipe(concat('vipers.min.js'))
    .pipe(size())
    .pipe(gulp.dest(buildDest + '/app'));
});

gulp.task('minify-app-css', function() {
    return gulp.src('./src/css/**/*.css')
      .pipe(concat("vipers.min.css"))
      .pipe(minifycss())
      .pipe(gulp.dest(buildDest + '/css'))
});

gulp.task('build', ['clean'], function() {
  gulp.src(jsLibs).pipe(concat("vipers.libs.js")).pipe(gulp.dest(buildDest + '/app'));
  gulp.src(cssLibs).pipe(concat("vipers.libs.css")).pipe(gulp.dest(buildDest + '/css'));
  gulp.src(fonts).pipe(gulp.dest(buildDest + '/fonts'));

  gulp.run('minify-app-js');
  gulp.run('minify-app-css');

  gulp.src('./src/**/*.*', { base: './src' }).pipe(gulp.dest(buildDest));
});

gulp.task('test', function() {
  return gulp.src('./idontexist')
    .pipe(karma({
      configFile: 'karma.conf.js',
      action: 'run'
    }))
    .on('error', function(err) {
      throw err;
    });
});