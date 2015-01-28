$(document).ready(function() {
  console.log('doc ready');
  // init first batch of albums
  (function($, undefined) {
    console.log('init masonry');
    window.msr = {};

    console.log($('.album'));
    $('.album').each(function(i) {
      var _this = this;
      imagesLoaded(_this, function() {
        window.msr[i] = new Masonry($(_this).find('.photos')[0], {
          photoSelector: '.photo',
          gutter: 10,
        });
      });
    });
  })(Zepto);

  // init lightbox for first batch of albums
  (function($, undefined) {
    console.log('init lightbox');
    // lightbox prototype
    var lb = $('.light-box');
    var close = function() {
      lb.css({'opacity': '0'});
      lb.css({'z-index': '-1'});
    }

    $('.light-box a').on('click', close);

    $('.photo').on('click', function(e) {
      var topY = (window.pageYOffset || document.documentElement.scrollTop);
      var photo = $(e.currentTarget);
      var image = photo.find('img');
      lb.css({'background-image': 'url(' + image.prop('src') + ')'});
      lb.css({'background-position': '50% 50%', 'background-repeat': 'no-repeat',
              'background-size': 'contain', 'top': $('body').scrollTop() + 'px'})
              
      lb.css({'z-index': '5'});
      lb.css({'opacity': '1'});
    });
  })(Zepto);
});
