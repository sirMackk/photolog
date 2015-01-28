var gulp = require('gulp'),
    sass = require('gulp-sass'),
    concat = require('gulp-concat'),
    minifyCSS = require('gulp-minify-css'),
    autoprefixer = require('gulp-autoprefixer'),
    uglify = require('gulp-uglify'),
    del = require('del');

// sprites?
// add js minification for blog + admin
var paths = {
  applicationScss: ['assets/sass/application.scss'],
  styles: ['assets/sass/app/*.scss'],
  scriptsLibs: ['assets/js/libs/*.js'],
  scriptsApp: ['assets/js/app/*.js'],
}

gulp.task('clean-scss', function(cb) {
  del(['./resources/public/css/app.min.css'], function(err, deletedFiles) {
    console.log('Files cleaned: ', deletedFiles.join(', '));
    cb();
  });
});

gulp.task('clean-blog-js', function(cb) {
  del(['./resources/public/js/blog.min.js'], function(err, deletedFiles) {
    console.log('Files cleaned: ', deletedFiles.join(', '));
    cb();
  });
});

gulp.task('scss', ['clean-scss'], function() {
  return gulp.src(paths.applicationScss)
    .pipe(autoprefixer({
      cascade: false,
    }))
    .pipe(sass({includePaths: ['./assets/sass/app/']}))
    .pipe(minifyCSS())
    .pipe(concat('app.min.css'))
    .pipe(gulp.dest('./resources/public/css/'));
});

gulp.task('js', ['clean-blog-js'], function() {
  return gulp.src(paths.scriptsLibs.concat(paths.scriptsApp))
    .pipe(uglify())
    .pipe(concat('blog.min.js'))
    .pipe(gulp.dest('./resources/public/js/'));
});

gulp.task('watch', function() {
  gulp.watch(paths.styles.concat(paths.applicationScss), ['scss']);
  gulp.watch(paths.scriptsLibs.concat(paths.scriptsApp), ['js']);
});

gulp.task('default', ['watch', 'scss', 'js']);
