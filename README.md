citysynth
=========

Citizen Science

This application is responsible for capturing the images at regular intervals and uploading them securely to a server. To protect the images, they are encrypted with AES and uploaded through a secure channel.

The application reads the config file from the server on every run (if it exists with same name) and modifies it's working parameters accordingly.

Current Issues:  <br />
1. [MEDIUM] Does not use fragment.  <br />
2. [HIGH] Does not work with Samsung brand.  <br />
3. [LOW] Poor UI 
