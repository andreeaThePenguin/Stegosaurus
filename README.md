Stegosaurus
==========================
Command line image steganography tool, using LSB technique.
Uses only .bmp images.

The project was edited in Eclipse (Java), which can be used for easier opening.

Compilation
--------------------------
From /src

$ javac Stegosaurus

Usage
--------------------------
From /src

For encoding a message:
$ java Stegosaurus -e -<no of bits> -<color channel> <entry image> <exit image name> "message"
 
For decoding a message:
$ java Stegosaurus -d -<no of bits> -<color channel> <image name> <message dimension>
  
Usage examples
--------------------------
(With the already existing sample files)
From /src:

java Stegosaurus -e -auto -r tomatoes.bmp finalimage.bmp "-fmessage.txt"
java Stegosaurus -d -1 -r finalimage.bmp 10078

