# photolog

Simple image blogging application.

Handles file uploads, creating thumbnails, pagination, post status. Uses http-kit as a server. Front-end wise, it uses [GulpJS][2] to compile and manage scss and js, and uses [zepto][3] (lightweight jquery alternative), [imagesloaded][4] (loading images in a masonry-friendly way), and [masonry][5] (incredibly nice image tiling).

Check it out at [photolog.mattscodecave.com][6]

## Prerequisites

You will need [Leiningen][1] 1.7.0 or above installed,
Postgresql and a jre.

[1]: https://github.com/technomancy/leiningen
[2]: http://gulpjs.com/
[3]: http://zeptojs.com/
[4]: https://github.com/desandro/imagesloaded
[5]: https://github.com/desandro/masonry
[6]: http://photolog.mattscodecave.com

## Running

To start a web server for the application, run:

    lein ring server

## License

Copyright (C) 2015 sirMackk

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see 
[http://www.gnu.org/licenses/](http://www.gnu.org/licenses).

