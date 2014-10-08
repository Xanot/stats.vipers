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
    "./bower_components/argjs/min/arg-min.js",
    "./bower_components/marked/lib/marked.js",
    "./bower_components/jquery/dist/jquery.min.js",

    "./bower_components/angular/angular.min.js",
    "./bower_components/angular-sanitize/angular-sanitize.min.js",
    "./bower_components/angular-animate/angular-animate.min.js",
    "./bower_components/angular-ui-router/release/angular-ui-router.min.js",
    "./bower_components/angular-strap/dist/angular-strap.min.js",
    "./bower_components/angular-strap/dist/angular-strap.tpl.min.js",
    "./bower_components/angular-moment/angular-moment.min.js",
    "./bower_components/angular-marked/angular-marked.min.js",
    "./bower_components/angular-loading-bar/build/loading-bar.min.js",
    "./bower_components/angular-bindonce/bindonce.min.js",
    "./bower_components/ngInfiniteScroll/build/ng-infinite-scroll.min.js",
    "./bower_components/angular-local-storage/angular-local-storage.min.js"
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
    return gulp.src('./src/css/*.css')
      .pipe(concat("vipers.min.css"))
      .pipe(minifycss())
      .pipe(gulp.dest(buildDest + '/css'))
});

gulp.task('build', ['clean'], function() {
  // Copy libraries to build
  gulp.src(jsLibs).pipe(concat("vipers.libs.js")).pipe(gulp.dest(buildDest + '/app'));
  gulp.src(cssLibs).pipe(concat("vipers.libs.css")).pipe(gulp.dest(buildDest + '/css'));
  gulp.src(fonts).pipe(gulp.dest(buildDest + '/fonts'));

  // Minify
  gulp.run('minify-app-js');
  gulp.run('minify-app-css');

  // Copy source files to build
  gulp.src('./src/**/*.*', { base: './src' }).pipe(gulp.dest(buildDest));
});

gulp.task('test', function() {
  return gulp.src('./idontexist')
    .pipe(karma({
      configFile: 'karma.conf.js',
      action: 'run'
    }))
    .on('error', function(err) {
      // Make sure failed tests cause gulp to exit non-zero
      throw err;
    });
});