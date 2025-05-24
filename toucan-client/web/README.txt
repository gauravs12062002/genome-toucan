Making Toucan available via Java Web Start:
===========================================

This directory contains:

  - htaccess        : htaccess file (rename to .htaccess) for serving jnlp
                      files with the right headers.
  - README.txt      : This readme.
  - toucan.jnlp     : Java Network Launching Protocol (JNLP) file for
                      launching Toucan via Java Web Start.
  - toucan-jnlp.php : php script for serving toucan.jnlp file with the right
                      headers.

If you are allowed to use "AddType" in .htaccess files:
  - rename htaccess to .htaccess
  - Fix toucan.jnlp so it fetches all jar files from your website.
  - Upload .htaccess, toucan.jnlp and all jar files (library jar files and
    toucan.jar) to the webserver.
  - Loading toucan.jnlp in a browser will start Toucan via Java Web Start.

If you are not allowed to use "AddType" in .htaccess files, but able to run
php scripts: 
  - Fix toucan.jnlp so it fetches all jar files from your website.
  - Upload toucan.jnlp, toucan-jnlp.php and all jar files (library jar files
    and toucan.jar) to the webserver.
  - Loading toucan-jnlp.php in a browser will start Toucan via Java Web Start.


Old Toucan homepage: http://www.esat.kuleuven.ac.be/~saerts/software/toucan.html
New Toucan homepage: https://gbiomed.kuleuven.be/apps/lcb/toucan/index.php
