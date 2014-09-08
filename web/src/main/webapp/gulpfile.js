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

gulp.task('build', ['clean'], function() {
  // Copy libraries to build
  bower().pipe(gulp.dest(buildDest + '/lib/'));
  gulp.src('./lib/**/*.*', { base: './lib' }).pipe(gulp.dest(buildDest + '/lib/manual'));

  // Minify
  gulp.run('minify-app-js');

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