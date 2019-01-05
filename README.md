Anamorphosis Made Easy (AME) is a (soon to be) collection of simple utilities for making specular anamorphic art.

The primary goal of the AME project is to provide an easy way for people to make their own anamorphic art. The second goal is to provide more advanced options for rendering and setting of physical parameters, such as preserving transparency, setting the mirror dimensions, vantage point, DPI, as well as use of interpolating points to smooth extreme edges of cylindrical transformations.

-------------------------------------------------------------------------------------------------

Cylindrical Mirror Anamorphosis (CylMA) is the first of the AME utilities to be made publicly available.

CylMA's rendering method uses an exact mathematical transformation (rather than an approximation) for finite viewing distance. This requires that you input the coordinates of your vantage point and the physical dimensions of the cylinder from which you will view the distorted image. A derivation of the transformation used by CylMA is provided for the adventurous reader and requires working knowledge of vector calculus and some linear algebra to follow along. The derivation extends the application of plane mirror virtual images to a curved mirror for finding the inverse transformation. A future draft will include diagrams and an appendix for a derivation of the Law of Reflection from Fermat's Principle of Least Time as well as a corollary to the Law of Reflection for plane mirrors: which shows that an object and it's virtual image are equidistant from the mirror along a line normal to the mirror.

CylMA was written for JavaFX 8 and as a Mathematica Notebook file. The Mathematica notebook code will perform the transformation with significantly less system memory and additionally applies a nice anti-aliasing filter to the output. However, it requires that you have access to Wolfram Mathematica. For those without access to Wolfram Mathematica, the Java utility may be used instead--and in some ways is more powerful.

Note that while you may use CylMA on its own, both the Java utility and the Mathematica Notebook file were intended to produce image files that will be post processed with Photoshop or GIMP to crop and edit before being printed. Furthermore, due to MS Windows' poor photo handeling, the afore mentioned programs should be used to print the output images. This is to ensure the DPI metadata is enforced during printing and no scaling or resizing of the image is performed.

-------------------------------------------------------------------------------------------------

CylMA Java Utility:

The Java utility currently supports PNG, JPEG, BMP, and GIF input file types. As of v1.01, only PNG output images are supported, though JPEG support may be implemented in later versions. By default, transparent pixels are "flattened" to WHITE when saving. However, you may preserve transparency by simply selecting the appropriate output option. A circle for locating where to place your mirror is added to the output by default, but may be omitted by deselecting it. An interpolating points selection is made available to correct for how the image is transformed (by using pixel corner points). This option should usually be set to 0; however, it is useful for "rounding" the top extreme left/right edges of the output image which may appear flat.

-------------------------------------------------------------------------------------------------

KNOWN ISSUES (ClyMA Java Utility v1.01):

1)  Large amounts of RAM or pagefile are needed for "HQ" and "Low RAM" rendering. Generally 32GiB or more RAM/pagefile is required to render large 2000 pixel sized input images. It's not odd to see 16GiB used to render smaller images. Due to a bug in the way Java draws polygons, they are sometimes rotated (this affects both HQ and Low RAM rendering methods), and forces me to apply a stroke to the inside of each polygon (literally filling it twice) to correct for this. Additionally, when anti-aliasing is used (HQ rendering method), the boundary between polygons is not filled. To fix this, a polyline is also drawn. This causes the HQ rendering method to use significantly more system RAM when rendering vs. Low RAM, but either method still uses large amounts of system memory.

2)  If garbage collection kicks in, due to low amounts of system RAM, the program may never finish rendering. You should watch the memory usage and simply close the program if all CPU/Threads hit 100% for an extended period of time (a symptom that garbage collection has kicked in). Either: A) lower the printer DPI, B) make the input image smaller, C) use a computer with more RAM or D) set your pagefile to a fixed large value (30GiB or more, use a HDD to minimize write wear on your SSD).

3)  Due to limitations of Java, the program will fail to save if the output is larger than approximately 12k x 12k pixels. This can happen quite easily if Vx (view distance) is large with respect to Vz (view height) and large printer DPI (600).

-------------------------------------------------------------------------------------------------

KNOWN ISSUES (CylMA Mathematica Script v1.00):

1)  Setting MaxRecusion beyond 5 will cause Mathematica to crash.

2)  Many features in the Java utility are not yet available.
