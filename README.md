Anamorphosis Made Easy (AME) 

AME is a (soon to be) collection of utilities for making specular and perspective anamorphic art. The specular utilities will be for cylindrical and conical mirrors, but more types may be added in the future.

-------------------------------------------------------------------------------------------------

What's All This Anamorphosis Stuff About?

Anamorphosis is a type of visual illusion concerned with the creation of distorted images according to the physics of perspective and mirror reflection. What makes these illusions so interesting is that the observer is only able to resolve an undistorted image if looking from a specific vantage point or with aid of an unusual shape of mirror.

Anamorphic illusions generally consist of two major types: The first is called specular or mirror anamorphosis. This type of anamorphosis involves reflecting a distorted image from a plane or other surface off of a curved mirror (useually a cylinder or cone) to reconstitute the original image. 

The second type is called plane anamorphosis. In this case, a distorted or “stretched” image on a plane (or other surface) is resolved only when looking from the correct vantage point. A major attraction of this type of anamorphosis is that the image, when viewed from the correct vantage point, appears to have more depth, giving it a three-dimensional appearance.

-------------------------------------------------------------------------------------------------

AME's Goals

The primary goal of the AME project is to provide an easy way for people to make their own anamorphic art and hopefully get them interested in applied mathematics, physics, and programming.

The second goal is to provide more advanced options for rendering and setting of physical parameters than utilities currently avaliable: such as preserving transparency, specifying the mirror dimensions, vantage point, and DPI.

-------------------------------------------------------------------------------------------------

Cylindrical Mirror Anamorphosis (CylMA)

CylMA is the first of the AME utilities to be made publicly available. CylMA's uses an exact mathematical transformation (rather than an approximation) for finite viewing distance. This requires that you input the coordinates of your vantage point and the physical dimensions of the cylinder from which you will view the distorted image. A derivation of the transformation used by CylMA is provided for the adventurous reader and requires working knowledge of vector calculus and some linear algebra to follow along. The derivation extends the application of plane mirror virtual images to a curved mirror for finding the inverse transformation. A future draft will include diagrams and an appendix for a derivation of the Law of Reflection from Fermat's Principle of Least Time as well as a corollary to the Law of Reflection for plane mirrors: which shows that an object and it's virtual image are equidistant from the mirror along a line normal to the mirror.

CylMA was written for JavaFX 8 and as a Mathematica notebook file. The Mathematica notebook will perform the transformation with significantly less system memory and additionally applies a nice anti-aliasing filter to the output. However, it currently doesn't support DPI metadata or retain the image's aspect ratio. It also requires that you have access to Wolfram Mathematica or download the Wolfram CDF Player. For those without access to Wolfram Mathematica, the Java utility may be used instead.

Note that while you may use CylMA on its own, both the Java utility and the Mathematica Notebook file were intended to produce image files that will be post processed with Photoshop or GIMP to crop and edit before being printed. If printing at home, using either of these programs will help ensure that the DPI metadata is enforced and no resizing of the image is performed.

-------------------------------------------------------------------------------------------------

CylMA Java Utility:

The Java utility currently supports PNG, JPEG, BMP, and GIF input file types. As of v1.01, only PNG output images are supported, though JPEG support may be implemented in later versions. By default, transparent pixels are "flattened" to WHITE when saving. However, you may preserve transparency by simply selecting the appropriate output option. A circle for locating where to place your mirror is added to the output by default, but may be omitted by deselecting it. An interpolating points selection is made available to correct for how the image is transformed (pixel corner-points). This option should usually be set to 0; however, it is useful for "rounding" the extreme top left/right edges of the output image that wrap around the back side of the cylinder.

HOW TO RUN:

1)  Select an input image (either PNG, JPEG, BMP, or GIF) by clicking the "File" button.

2)  Select desired printer DPI. Default DPI is 600, but you may select 150, 300, or 360 DPI as needed. Most professional images for photography are printed at 300 or 360 DPI, documents and certain types of graphics are printed at 600 DPI. If you have the system memory available, outputting images at 600 DPI and later scaling them down to 300 DPI (after post processing) will give better results for printing.
	
3)  Enter the radius and height of the cylinder used to view the output image in inches.
	
4)  Enter the viewpoint coordinates as x and z values in inches.

5) If working with transparent images, select "Preserve Transparency" under "Output Options." If you don't want to see the locator circle in your output image, deselect "Show Cylinder Base." If you have an input image with a WHITE background, you may select "Ignore WHITE" to lower system RAM requirements when rendering (can be useful to speed up the transformation as well). The HQ rendering method applies anti-aliasing hints on the polypixels (unfortunately this doesn't perform anti-aliasing on the overall image). If HQ RAM usage is too high, try selecting Low RAM option and Ignore WHITE.
	
5)  Next click the "Transform" button and wait for the output image to render. This can take a while or be very fast, depending on the input image size and if there are a significant number TRANSPARENT pixels (which are ignored).
	
6)  Finally, using the save prompt, find a location and specify a name for the output image to be saved. The initial file name contains information to help the user identify the viewing position and cylinder dimensions the image was rendered for. This text may be erased or kept if deemed useful.

-------------------------------------------------------------------------------------------------

KNOWN ISSUES (ClyMA Java Utility v1.01):

1)  Large amounts of RAM or pagefile are needed for "HQ" and "Low RAM" rendering. Generally 32 GiB or more RAM/pagefile is required to render large sized input images (>= 2000 pixels). It's not odd to see 16 GiB used to render smaller images. However, most small images (that are up scaled) use between 1 to 4 GiB of RAM. Due to a bug in the way Java draws polygons, output polypixels are sporadically rotated (this affects both HQ and Low RAM rendering methods). To correct for this, a stroke is applied to the inside of each polygon (filling it twice) to ensure rotated polypixels are not seen in the output. Additionally, when anti-aliasing is used (HQ rendering method), the boundary between polypixels is not filled. A polyline is drawn around each polypixel to fill these gaps and causes the HQ rendering method to use significantly more system RAM than the Low RAM method. If running the source code in Eclipse IDE, you may need to increase the maximum	heap memory the Java VM can use when running the program. To	do so add -Xmx20g (or more) to the VM argument section in Eclipse: Project -> Properties -> Run/Debug Settings -> Main() -> Arguments -> VM arguments. 

2)  If the JVM garbage collection kicks in, due to low amounts of system RAM, the program may never finish rendering. You should watch the memory usage and simply close the program if all CPU threads hit 100% for an extended period of time (a symptom that garbage collection is stalling further progress). Workarounds: A) lower the printer DPI, B) make the input image smaller, C) use a computer with more RAM or D) set your pagefile to a fixed large value (30 GiB or more, use a HDD to minimize write wear on your SSD). If you have enough system RAM CPU usage will be low (20-30%) with some occasional spikes (as it is single threaded and can't fully load the CPU).

3)  Due to limitations of the snapshot or writable image classes, the program will fail to save if the output is larger than approximately 12k x 12k pixels. This can happen quite easily if view distance (vx) is large with respect to view height (vz) and large printer DPI (600).

-------------------------------------------------------------------------------------------------

KNOWN ISSUES (CylMA Mathematica Notebook, 2D SECTION):

1)  Setting MaxRecusion beyond 5 may cause Mathematica to crash.

2)  Many features in the Java utility are not yet available.
