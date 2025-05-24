<?php
// We'll be outputting a JNLP file.
header('Content-type: application/x-java-jnlp-file');

// It will be called "toucan.jnlp".
header('Content-Disposition: attachment; filename="toucan.jnlp"');

// The JNLP source is in "toucan.jnlp"
readfile('toucan.jnlp');
?>
