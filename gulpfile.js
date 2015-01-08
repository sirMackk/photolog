var gulp = require('gulp'),
    sass = require('gulp-sass'),
    concat = require('gulp-concat'),
    minifyCSS = require('gulp-minify-css'),
    autoprefixer = require('gulp-autoprefixer'),
    uglify = require('gulp-uglify'),
    del = require('del');

// sprites?
var paths = {
  applicationScss: ['assets/sass/application.scss'],
  styles: ['assets/sass/app'],
  scripts: ['assets/js/**/*.js'],
}

gulp.task('clean', function(cb) {
  del(['./resources/public/css/app.min.css'], function(err, deletedFiles) {
    console.log('Files cleaned: ', deletedFiles.join(', '));
    cb();
  });
});

gulp.task('scss', ['clean'], function() {
  return gulp.src(paths.applicationScss)
    .pipe(autoprefixer({
      cascade: false,
    }))
    .pipe(sass({includePaths: ['./assets/sass/app/']}))
    .pipe(minifyCSS())
    .pipe(concat('app.min.css'))
    .pipe(gulp.dest('./resources/public/css/'));
});

gulp.task('watch', function() {
  gulp.watch(paths.styles.concat(paths.applicationScss), ['scss']);
});

gulp.task('default', ['watch', 'scss']);
