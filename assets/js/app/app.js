$(document).ready(function() {
  // init first batch of albums
  (function($, undefined) {
    window.msr = {};

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
    // lightbox prototype
    var lb = $('.light-box');
    var close = function() {
      lb.css({'opacity': '0'});
      lb.css({'z-index': '-1'});
    }

    $('.light-box a').on('click', close);

    $('.photo').on('click', function(e) {
      var topY = ($('body').scrollTop() || $('html').scrollTop());
      var photo = $(e.currentTarget);
      var image = photo.find('img');
      lb.css({'background-image': 'url(' + image.prop('src').replace('thumb_', '') + ')'});
      lb.css({'background-position': '50% 50%', 'background-repeat': 'no-repeat',
              'background-size': 'contain', 'top': topY + 'px'})
              
      lb.css({'z-index': '5'});
      lb.css({'opacity': '1'});
    });
  })(Zepto);
});
