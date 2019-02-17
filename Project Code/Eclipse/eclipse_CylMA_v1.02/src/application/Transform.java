package application;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.StrokeType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 *Cylindrical Mirror Anamorphosis
 *Transformation code
 *
 *NEW FEATURES/CHANGES
 *
 *1) Exposed interpolator functionality.
 *   Useful for very small input images and smoothing top right/left edges of output 
 *   image. Consumes large amounts of RAM, so leave at n=0 unless needed.
 *
 *2) Minor bug fixes with variable declaration and scope.
 *
 *3) Optimized minY offset search and conversion from Cartesian coordinates to pixel coordinates.
 *
 *4) Fixed out of bounds error with input images of odd pixel width due to 
 *   incorrect horizontal offset when converting from pixel coordinates to
 *   Cartesian coordinates.
 *
 *KNOWN ISSUES
 *
 *1) Too large of output image breaks writable image class.
 *   Max output dimensions look to be around 12k x 12k pixels
 *
 * @author  Garrett R. Mackelprang
 * @version 1.02
 * @edited  2019.02.16
 */

public class Transform {
	
	//RENDERING OPTIONS
	
	//class variables default values
	public static double  dpiT         = 600;  //target output DPI
	public static double  r            = 0;    //cylinder radius
	public static double  h            = 0;    //cylinder height
	public static double  vx           = 0;    //viewing distance
	public static double  vz           = 0;    //viewing height
	public static int     n            = 0;    //interpolating points
	public static boolean hqMethod     = true; //uses more system RAM
	public static boolean drawCylinder = true; //show cylinder base in output
	public static Color   ignoreColor  = null; //set to WHITE to ignore rendering WHITE pixels
	public static Color   backgndColor = null; //set to TRANSPARENT to preserve input image transparency
	public static String  params       = "";  //parameter text to append to output file name
	
	//IMAGE TRANSFORMATION
	
	public static void startTransformation(String inputImg) {
		
		//GET IMAGE AND RESIZE/SCALE
		
		Image img = new Image(inputImg);
		
		//get "native" DPI and new image dimensions
		double[] imgSize = {img.getWidth(), img.getHeight()};
        double newImgSize[] = newImgSize(imgSize); //{newImgW,newImgH,dpiN}
        
		//resize image if too large
		if(newImgSize[0] != imgSize[0]) {
	        img = new Image(inputImg, newImgSize[0], newImgSize[1], false, true);
		}
		
		//SET PARAMETER VALUES
		
		//image scaling parameters
		final double dpiN = newImgSize[2]; //native image DPI
		final double s    = dpiN/dpiT;     //scale to target DPI
		
		//physical parameters
		final double rPhy  = dpiN*r;
		final double vxPhy = dpiN*vx;
		final double vzPhy = dpiN*vz;
        
        //GENERATE GRID POINTS
        
        //NOTE: x, y, and z refer to orientation of 3D Cartesian coordinates
        //      Cartesian y maps to pixel x, Cartesian x maps to pixel y 
        
        //generate input grid points
		//picture plane / Cartesian yz-plane
        //2D array row0: y and row1: z
        double[][] yzGrid = yzGrid(newImgSize);
        
        //find minimum x element of output image
        //minimum x in output image is mapped to top left point of the picture plane
        double[] temp = anamorphicT(rPhy, vxPhy, vzPhy, yzGrid[0][0], yzGrid[1][0]);
        double minX = Math.floor(temp[0]);
        
        //keep cylinder visible in output
        if(minX > -Math.ceil(rPhy) && drawCylinder) {
            minX = -Math.ceil(rPhy);
        }
        
        //find minimum y element of output image
        //minimum y in output image is mapped to a point that is between the top left
        //and first quarter of the picture plane. Index i: 0<=i<=0.25*(length-1)
        double minY = Double.MAX_VALUE;
        for(int i=0; i<(int)Math.ceil(0.25*newImgSize[0]*(n+1)); i++) {
        	temp = anamorphicT(rPhy, vxPhy, vzPhy, yzGrid[0][i], yzGrid[1][0]);
            if(temp[1] < minY) {
                minY = temp[1];
            }
        }
        minY = Math.floor(minY);
        
        //generate output grid points
        //anamorphic image / Cartesian xy-plane
        //2D array row0: x and row1: y
        double[][] xyGrid = new double[2][yzGrid[0].length*yzGrid[1].length];
        for(int i=0; i<yzGrid[1].length; i++) {
            int k = i*yzGrid[0].length;
            for(int j=0; j<yzGrid[0].length; j++) {
                temp = anamorphicT(rPhy, vxPhy, vzPhy, yzGrid[0][j], yzGrid[1][i]);
                xyGrid[0][k] = (temp[0] - minX)/s; //remove x offset
                xyGrid[1][k] = (temp[1] - minY)/s; //remove y offset
                k++;
            }
        }
        temp = null; //ready variable for garbage collection
        
        //CONSTRUCT POLYGONS
        
        BorderPane output = new BorderPane();
        
        //read pixels from resized input image
        PixelReader imgPixel = img.getPixelReader();
        
        //declare loop variables
        Color pixelColor;
        Polygon polyPixel;
        Polyline polyLine;
        double[] polyPoints = new double[4*(n+2)];
        int l;
        int startR;
        int stopR;
        int stopL;
        int startL;
        
        //loop generates a polygon "pixel" for each pixel of the input image and
        //fills the polygon with the associated color of the input image pixel
        
        if(hqMethod) { //render method 1 (better quality, uses more RAM)
        	for(int i=0; i<newImgSize[1]; i++) {     //pixel row selector (y)
        		for(int j=0; j<newImgSize[0]; j++) { //pixel column selector (x)
        			pixelColor = imgPixel.getColor(j, i);
        			if(!pixelColor.equals(Color.TRANSPARENT) && !pixelColor.equals(ignoreColor)) {
            			l = 0; //polyPoints index
            			startR = j*(n + 1) + i*yzGrid[0].length;
           				stopR  = startR + n + 1;
           				for(int k=startR; k<=stopR; k++) {  //sweep right (top points)
           					polyPoints[l]   = xyGrid[1][k]; //x coordinate
           					polyPoints[l+1] = xyGrid[0][k]; //y coordinate
           					l += 2;
               			}
           				stopL  = j*(n + 1) + (i+1)*yzGrid[0].length;
           				startL = stopL + n + 1;
            			for(int k=startL; k>=stopL; k--) {  //sweep left (bottom points)
            				polyPoints[l]   = xyGrid[1][k]; //x coordinate
            				polyPoints[l+1] = xyGrid[0][k]; //y coordinate
            				l += 2;
                		}
            			polyPixel = new Polygon(polyPoints);
           				polyPixel.setFill(pixelColor);
           				polyPixel.setStroke(pixelColor);
           				polyPixel.setStrokeType(StrokeType.INSIDE);
           				polyPixel.setStrokeWidth(10);
           				polyLine = new Polyline(polyPoints);
           				polyLine.setStroke(pixelColor);
           				output.getChildren().addAll(polyPixel, polyLine);
           			}
           		}
           	}
        } else { //render method 2 (uses less RAM)
            for(int i=0; i<newImgSize[1]; i++) {     //pixel row selector (y)
            	for(int j=0; j<newImgSize[0]; j++) { //pixel column selector (x)
            		pixelColor = imgPixel.getColor(j, i);
            		if(!pixelColor.equals(Color.TRANSPARENT) && !pixelColor.equals(ignoreColor)) {
            			l = 0; //polyPoints index
            			startR = j*(n + 1) + i*yzGrid[0].length;
           				stopR  = startR + n + 1;
           				for(int k=startR; k<=stopR; k++) {  //sweep right (top points)
           					polyPoints[l]   = xyGrid[1][k]; //x coordinate
           					polyPoints[l+1] = xyGrid[0][k]; //y coordinate
           					l += 2;
               			}
           				stopL  = j*(n + 1) + (i+1)*yzGrid[0].length;
           				startL = stopL + n + 1;
            			for(int k=startL; k>=stopL; k--) {  //sweep left (bottom points)
            				polyPoints[l]   = xyGrid[1][k]; //x coordinate
            				polyPoints[l+1] = xyGrid[0][k]; //y coordinate
            				l += 2;
                		}
            			polyPixel = new Polygon(polyPoints);
           				polyPixel.setSmooth(false);
           				polyPixel.setFill(pixelColor);
           				//polyPixel.setStrokeType(StrokeType.INSIDE);
           				//polyPixel.setStroke(pixelColor);
           				//polyPixel.setStrokeWidth(10);
           				output.getChildren().add(polyPixel);
           			}
           		}
           	}
        	
        }

        //show base of cylinder in output image
        if(drawCylinder) {
        	//                               centerX, centerY, radius, fill
        	Circle cylinderBase = new Circle(-minY/s, -minX/s, r*dpiT, null);
        	cylinderBase.setStroke(Color.BLACK);
        	output.getChildren().add(cylinderBase);
        }
        
        //SAVE OUTPUT TO FILE
        
        //set snapshot fill color 
        SnapshotParameters snapshotParam = new SnapshotParameters();
        snapshotParam.setFill(backgndColor);
        
        //convert snapshot to buffered image
        BufferedImage outputImg = SwingFXUtils.fromFXImage(output.snapshot(snapshotParam, null), null);
        
    	//set DPI metadata
    	ImageWriter writer = ImageIO.getImageWritersByFormatName("png").next();
		
    	ImageWriteParam writeParam = writer.getDefaultWriteParam();
    	ImageTypeSpecifier typeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB);
		
    	IIOMetadata metadata = writer.getDefaultImageMetadata(typeSpecifier, writeParam);

    	IIOMetadataNode horiz = new IIOMetadataNode("HorizontalPixelSize");
    	horiz.setAttribute("value", Double.toString(dpiT/25.4));

    	IIOMetadataNode vert = new IIOMetadataNode("VerticalPixelSize");
    	vert.setAttribute("value", Double.toString(dpiT/25.4));

    	IIOMetadataNode dim = new IIOMetadataNode("Dimension");
    	dim.appendChild(horiz);
    	dim.appendChild(vert);

    	IIOMetadataNode root = new IIOMetadataNode("javax_imageio_1.0");
    	root.appendChild(dim);
    	
    	//choose output file name and location (appends parameter values)
        FileChooser saveFile = new FileChooser();
        saveFile.setTitle("Save Output");
        saveFile.setInitialFileName(params);
        saveFile.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("png","*.png"));
		File newImage = saveFile.showSaveDialog(new Stage());
		
		//merge metadata and save output file
		if(newImage != null) {
            try {
            	metadata.mergeTree("javax_imageio_1.0", root);
            	ImageOutputStream stream = ImageIO.createImageOutputStream(newImage);
            	writer.setOutput(stream);
            	writer.write(metadata, new IIOImage(outputImg, null, metadata), writeParam);
            	stream.close();
            } catch (IOException e) {
				e.printStackTrace();
			}
		}
	
		//CLOSE PROGRAM
		
		//temporary solution to high memory usage
		Platform.exit();
	}
	
	private static double[] newImgSize(double[] imgSize) {
        
        //max width and height of picture plane
        double maxW = 2*r;
        double maxH = (vx*h - vz*r)/(vx - r);
        
        //calculate aspect ratios
        double ppAspect  = maxW/maxH;
        double imgAspect = imgSize[0]/imgSize[1];
        
        //find native DPI of image
        double dpiN;
        double maxDim[] = new double[2]; //image constraints
        if(imgAspect > ppAspect) {       //scaling constrained by width
        	dpiN      = imgSize[0]/maxW;
        	maxDim[0] = maxW;
        	maxDim[1] = imgSize[0];
        } else {                         //scaling constrained by height
        	dpiN      = imgSize[1]/maxH;
        	maxDim[0] = maxH;
        	maxDim[1] = imgSize[1];
        }
        
        double[] newImgSize = {imgSize[0],imgSize[1],dpiN};
        
        //RESIZE IMAGE
        
        //if dpiN/dpiT > 3 or constraining dimension > 2400 pixels,
        //resize image such that dpiN/dpiT <= 3 and maxDim <= 2000
        //second condition helps reduce ram for systems with <= 32GiB RAM
        //first allows larger images to be used by scaling them down during
        //polygon creation, thus improving output resolution
        
        if(dpiN/dpiT > 3 || 2400/maxDim[1] < 1) {
        	//down-scale factor
        	double s;
        	if(3*maxDim[0]*dpiT <= 2000) {
        		s = 3;
        	} else if(2.5*maxDim[0]*dpiT <= 2000) {
        		s = 2.5;
        	} else if(2*maxDim[0]*dpiT <= 2000) {
        		s = 2;
        	} else if(1.5*maxDim[0]*dpiT <= 2000) {
        		s = 1.5;
        	} else {
        		s = 1;
        	}
        	
        	if(imgAspect > ppAspect) { //resize constrained by width
                newImgSize[0] = Math.floor(s*maxW*dpiT);             //resizeW
                newImgSize[1] = Math.floor((s*maxW*dpiT)/imgAspect); //resizeH
                newImgSize[2] = newImgSize[0]/maxW;                  //new native DPI
            } else {                   //resize constrained by height
                newImgSize[1] = Math.floor(s*maxH*dpiT);             //resizeH
                newImgSize[0] = Math.floor(s*maxH*dpiT*imgAspect);   //resizeW
                newImgSize[2] = newImgSize[1]/maxH;                  //new native DPI
            }
        }
        
        return newImgSize;
    }

	private static double[][] yzGrid(double[] newImgSize) {
        //Generates pixel grid points and maps (x,y,0) pixel 
        //coordinates to (0,y,z) Cartesian coordinates.
        
        int imgW = (int)newImgSize[0];
        int imgH = (int)newImgSize[1];
        
        double[][] yzGrid = new double[2][];  //2D array row0: y points, row1: z points
        yzGrid[0] = new double[imgW*(n+1)+1]; //y coordinates (from input x pixel coordinates)
        yzGrid[1] = new double[imgH+1];       //z coordinates (from input y pixel coordinates)
        
        double hOffset = 0.5*imgW; //horizontal offset
        
        //generate Cartesian y coordinates
        for(int i=0; i<yzGrid[0].length; i++) {
            yzGrid[0][i] = (double)i/(n+1) - hOffset; 
        }
        
        //generate Cartesian z coordinates
        for(int i=imgH; i>=0; i--) {
            yzGrid[1][imgH-i] = i;
        }
        
        return yzGrid;
    }
    
    private static double[] anamorphicT(double r, double vx, double vz, double py, double pz) {
    	//Method uses extensive simplifications for case: vy=0 and px=0.
        //For a general solution, where vy and px may be arbitrary, see derivation.
        
        //ANAMORPHIC TRANSFORMATION
        //maps (0,y,z) to (x,y,0) in Cartesian coordinates
        
        //t parameter for intersection with cylinder
        double ts = (Math.pow(vx,2)-Math.sqrt(Math.pow(r,2)*(Math.pow(py,2)+Math.pow(vx,2))-Math.pow(vx*py,2)))/(Math.pow(py,2)+Math.pow(vx,2));
        
        //u parameter for midpoint of line A'A
        double um = (vx*py*(ts*(pz-vz)+vz))/((vz-pz)*(Math.pow(ts*py,2)+Math.pow(vx*(1-ts),2)));
        
        //anamorphic image point
        double[] anamorphImgPt = new double[2];
        anamorphImgPt[0] = 2*(vx - ts*(um*py + vx)) + (vx*pz)/(vz - pz);   //Cartesian x-coordinate
        anamorphImgPt[1] = 2*(ts*py + vx*um*(1 - ts)) - (vz*py)/(vz - pz); //Cartesian y-coordinate
        
    	return anamorphImgPt;
    }
}