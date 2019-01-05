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

import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

//Garrett Mackelprang
//Cylindrical Mirror Anamorphosis
//Version 1.01
//Jan. 4th 2019

//NEW FEATURES

//Exposed interpolator functionality.
//Useful for very small input images and smoothing top right/left edges of output 
//image. Consumes large amounts of RAM, so leave at n=0 unless needed.

public class Main extends Application {
	
	//RENDERING OPTIONS
	
	//default values
	double  dpiT         = 600;  //target output DPI
	int     n            = 0;    //interpolating points
	boolean hqMethod     = true; //uses more system RAM
	boolean drawCylinder = true; //show cylinder base in output
	Color   ignoreColor  = null; //set to WHITE to ignore rendering WHITE pixels
	Color   backgndColor = null; //set to TRANSPARENT to preserve input image transparency
	String  params       = " ";  //parameter text to append to output file name
	
	//CREATE FIELDS AND BUTTONS
	
	//center pane
	Button    imageB        = new Button("File");
	TextField imageF        = new TextField();   //hidden field
	ChoiceBox<String> dpiTF = new ChoiceBox<>(); //target DPI selector
	ChoiceBox<String> intPF = new ChoiceBox<>(); //interpolating pts selector
	TextField cylinderRF    = new TextField();
	TextField cylinderHF    = new TextField();
	TextField viewDF        = new TextField();
	TextField viewHF        = new TextField();
    
	//right pane
	RadioButton renderOpt1RB = new RadioButton("HQ");
	RadioButton renderOpt2RB = new RadioButton("Low RAM");
	RadioButton renderOpt3RB = new RadioButton("Ignore WHITE");
	RadioButton outputOpt1RB = new RadioButton("Show Cylinder Base");
	RadioButton outputOpt2RB = new RadioButton("Flatten to WHITE");
	RadioButton outputOpt3RB = new RadioButton("Preserve Transparency");
    
	//bottom pane
	Button transformB = new Button("Transform");
	Button aboutB     = new Button("About");
    
	//GUI CREATION
    
	//display GUI
	@Override
	public void start(Stage primaryStage) throws Exception{
		
		BorderPane mainWindow = new BorderPane();
		mainWindow.setStyle("-fx-background-color: #f0f0f0");
		mainWindow.setPadding(new Insets(7,7,7,7));
		
		GridPane inputs = addInputs();
		mainWindow.setCenter(inputs);
		
		VBox options = addOptions();
		mainWindow.setRight(options);
		
		HBox bottomBar = addBottomBar();
		mainWindow.setBottom(bottomBar);

		primaryStage.setScene(new Scene(mainWindow,400,235));
		primaryStage.setTitle("Cylindrical Mirror Anamorphosis");
		primaryStage.show();
	}
	
	//create bottom bar
	private HBox addBottomBar() {
		
		//create bottom bar
		HBox bottomBar = new HBox();
		bottomBar.setStyle("-fx-background-color: #f0f0f0");
		bottomBar.setPadding(new Insets(7,0,0,0));
		bottomBar.setSpacing(7);
	    
		//resize bottom bar buttons
		transformB.setPrefSize(85,20);
		aboutB.setPrefSize(85,20);
	
		bottomBar.getChildren().addAll(transformB,aboutB);
		
		//transform input image
		transformB.setOnAction(new EventHandler<ActionEvent>(){
			public void handle(ActionEvent e){
				//check for errors and run transformation
				preRunCheck();
			}
		});
		
		//display about window
		aboutB.setOnAction(new EventHandler<ActionEvent>(){
			public void handle(ActionEvent e){
				Stage aboutStage = new Stage();
				VBox about = addAbout();
				Scene aboutScene = new Scene(about,370,245);
				aboutStage.setScene(aboutScene);
				aboutStage.setTitle("About");
	            		aboutStage.show();
			}
		});

		return bottomBar;
	}
	
	//create error message
	private VBox addError(String errTxt) {
		
		VBox error = new VBox();
		error.setStyle("-fx-background-color: WHITE");
		error.setPadding(new Insets(7,7,7,7));
		error.setAlignment(Pos.CENTER);
	    
		Label line0 = new Label(errTxt);
		line0.setFont(Font.font("Calibri Light",15));
		line0.setPrefSize(300,25);
		line0.setWrapText(true);
		line0.setStyle("-fx-text-alignment: left;");
		error.getChildren().add(line0);
	    
		return error;
	}
	
	//create about message
	private VBox addAbout() {
		
		VBox about = new VBox();
		about.setStyle("-fx-background-color: WHITE");
		about.setPadding(new Insets(10,20,20,20));
		about.setAlignment(Pos.CENTER);
		
		Text line1 = new Text("Cylindrical Mirror");
		line1.setFont(Font.loadFont("file:resources/fonts/bradley_hand_itc.ttf",40));
		line1.setFill(Color.BLACK);
		about.getChildren().add(line1);
	    
		Text line2 = new Text("Anamorphosis");
		line2.setFont(Font.loadFont("file:resources/fonts/bradley_hand_itc.ttf",40));
		line2.setFill(Color.BLACK);
		about.getChildren().add(line2);
	    
		Label line3 = new Label("A simple Java program that distorts an image such that "
	    		+ "viewing its reflection off a cylindrical mirror (from a particular "
	    		+ "vantage point) will restore the image.");
		line3.setFont(Font.font("Calibri Light",13));
		line3.setPrefSize(320, 90);
		line3.setWrapText(true);
		line3.setStyle("-fx-text-alignment: center;");
		about.getChildren().add(line3);
	    
		Text line4 = new Text("Garrett Mackelprang, Jan. 2019");
		line4.setFont(Font.font("Calibri Light",13));
		line4.setFill(Color.BLACK);
		about.getChildren().add(line4);
	    
		return about;
	}
	
	//create input pane
	private GridPane addInputs() {
		
		//create input grid pane
		GridPane inputs = new GridPane();
		inputs.setStyle("-fx-background-color: WHITE;"
				+ "-fx-border-color: #f0f0f0;"
				+ "-fx-border-width: 0 3 0 0;");
		inputs.setPadding(new Insets(7,7,7,7));
	    
		ColumnConstraints column1 = new ColumnConstraints(114);
		ColumnConstraints column2 = new ColumnConstraints(60);
		ColumnConstraints column3 = new ColumnConstraints(41);
		inputs.getColumnConstraints().addAll(column1,column2,column3);
	    
		Label imageL1     = new Label("Input Image");
		Label dpiL1       = new Label("Printer DPI");
		Label dpiL2       = new Label(" dots/in");
		Label intPL1      = new Label("Interpolating Pts");
		Label cylinderRL1 = new Label("Cylinder Radius r");
		Label cylinderRL2 = new Label(" in");
		Label cylinderHL1 = new Label("Cylinder Height h");
		Label cylinderHL2 = new Label(" in");
		Label viewDL1     = new Label("Viewing Distance vx");
		Label viewDL2     = new Label(" in");
		Label viewHL1     = new Label("Viewing Height vz");
		Label viewHL2     = new Label(" in");
	    
		dpiTF.getItems().addAll("600", "360", "300", "150");
		dpiTF.setValue("600");
	    
		intPF.getItems().addAll("0", "3", "5", "7", "9");
		intPF.setValue("0");
	    
		imageB.setPrefSize(60, 20);
		dpiTF.setPrefSize(60, 20);
		intPF.setPrefSize(60, 20);
	    
		inputs.add(imageL1,0,0); 	inputs.add(imageB,1,0);			
		inputs.add(dpiL1,0,1); 		inputs.add(dpiTF,1,1); 		inputs.add(dpiL2,2,1);
		inputs.add(cylinderRL1,0,2);	inputs.add(cylinderRF,1,2);	inputs.add(cylinderRL2,2,2);
		inputs.add(cylinderHL1,0,3);	inputs.add(cylinderHF,1,3); 	inputs.add(cylinderHL2,2,3);
		inputs.add(viewDL1,0,4); 	inputs.add(viewDF,1,4); 	inputs.add(viewDL2,2,4);
		inputs.add(viewHL1,0,5); 	inputs.add(viewHF,1,5); 	inputs.add(viewHL2,2,5);
		inputs.add(intPL1,0,7);		inputs.add(intPF,1,7);
		
		//select input image
		imageB.setOnAction(new EventHandler<ActionEvent>(){
			public void handle(ActionEvent e){
				FileChooser selectImage = new FileChooser();
				selectImage.getExtensionFilters().addAll(
					new FileChooser.ExtensionFilter("Images","*.png","*.jpg","*.gif","*.bmp")
				);
	               selectImage.setTitle("Select Image");
				File selectedImage = selectImage.showOpenDialog(new Stage());
				if(selectedImage != null) {
					imageF.setText(selectedImage.toURI().toString());
				}
			}
		});
		
		return inputs;
	}
	
	//create options pane
	private VBox addOptions() {
		
		//create options VBox
		VBox options = new VBox();
		options.setStyle("-fx-background-color: WHITE");
		options.setPadding(new Insets(7,7,7,7));
		options.setSpacing(7);
	    
		//rendering options label and buttons
		Label renderOptL = new Label("--- Rendering Options ---");
	    
		final ToggleGroup setRenderOpt = new ToggleGroup();
		renderOpt1RB.setToggleGroup(setRenderOpt);
		renderOpt2RB.setToggleGroup(setRenderOpt);
		renderOpt1RB.setSelected(true);
	    
		HBox renderOptLabel = new HBox();
		renderOptLabel.getChildren().add(renderOptL );
		renderOptLabel.setAlignment(Pos.CENTER);
	    
		//output image options label and buttons
		Label outputOptL = new Label("----- Output Options -----");
	    
		outputOpt1RB.setSelected(true);
	    
		HBox outputOptLabel = new HBox();
		outputOptLabel.getChildren().add(outputOptL );
		outputOptLabel.setAlignment(Pos.CENTER);
	    
		final ToggleGroup setOutputOpt = new ToggleGroup();
		outputOpt2RB.setToggleGroup(setOutputOpt);
		outputOpt3RB.setToggleGroup(setOutputOpt);
		outputOpt2RB.setSelected(true);
	    
		GridPane renderOpt12 = new GridPane();
		renderOpt12.setHgap(10);
		renderOpt12.add(renderOpt1RB,0,0);
		renderOpt12.add(renderOpt2RB,1,0);
	    
		options.getChildren().addAll(
			renderOptLabel,
	    		renderOpt12,
	    		renderOpt3RB,
	    		outputOptLabel,
	    		outputOpt1RB,
	    		outputOpt2RB,
	    		outputOpt3RB
	    	);
	    
		return options;
	}
	
	//INPUT VALIDATION
	
	private void preRunCheck() {
		
		//one or more input field is empty
		if(
			imageF.getText().trim().isEmpty()     ||
			cylinderRF.getText().trim().isEmpty() ||
			cylinderHF.getText().trim().isEmpty() ||
			viewDF.getText().trim().isEmpty()     ||
			viewHF.getText().trim().isEmpty()
			){
			Stage secondaryStage = new Stage();
			String errTxt = "Please select an image and input all parameters.";
			VBox error = addError(errTxt);
			Scene secondScene = new Scene(error,314,39);
			secondaryStage.setScene(secondScene);
			secondaryStage.setTitle("One Or More Fields Empty");
			secondaryStage.show();
		}
		
		//viewing distance must be increased
		else if(getVx() <= getR()) {
			Stage secondaryStage = new Stage();
			String errTxt = "vx must be greater than r.";
			VBox error = addError(errTxt);
			Scene secondScene = new Scene(error,230,39);
			secondaryStage.setScene(secondScene);
			secondaryStage.setTitle("vx Is Too Small");
			secondaryStage.show();
		}
		
		//viewing height must be increased
		else if(getVz() <= getH()) {
			Stage secondaryStage = new Stage();
			String errTxt = "vz must be greater than h.";
			VBox error = addError(errTxt);
			Scene secondScene = new Scene(error,230,39);
			secondaryStage.setScene(secondScene);
			secondaryStage.setTitle("vz Is Too Small");
			secondaryStage.show();
		}
		
		//everything okay: perform transformation
		else {
			setOptions();
			startTransformation();
		}
	}
	
	//SET RENDERING AND OUTPUT OPTIONS
	
	private void setOptions() {
		//set target DPI to value from choice box
		dpiT = Double.parseDouble(dpiTF.getValue());
		
		//set interpolating points to value from choice box
		n = Integer.parseInt(intPF.getValue());
		
		//output image parameters text
		params = dpiTF.getValue()      + ","
			+ cylinderRF.getText() + ","
			+ cylinderHF.getText() + ","
			+ viewDF.getText()     + ","
			+ viewHF.getText();
		
		if(n!=0) {
			params += " " + n + "n";
		}
		
		//set Low RAM rendering option
		if(renderOpt2RB.isSelected()) {
			hqMethod = false;
			params += " LoRAM";
		} else {
			params += " HQ";
		}
        
		//set ignore WHITE rendering option
		if(renderOpt3RB.isSelected()) {
			ignoreColor = Color.WHITE;
			params += " IgnWht";
		}
        
		//don't show cylinder base in output image
		if(!outputOpt1RB.isSelected()) {
			drawCylinder = false;
		} else {
			params += " DrwCyl";
		}
        
		//set output image background color to TRANSPARENT
		if(outputOpt3RB.isSelected()) {
			backgndColor = Color.TRANSPARENT;
			params += " PresTrans";
		}
	}
	
	//IMAGE TRANSFORMATION
	
	private void startTransformation(){
		
		//GET IMAGE AND RESIZE/SCALE
		
		Image img = new Image(imageF.getText());
		
		//get "native" DPI and new image dimensions
		double[] imgSize = {img.getWidth(), img.getHeight()};
		double newImgSize[] = newImgSize(imgSize); //{newImgW,newImgH,dpiN}
        
		//resize image (if needed)
		if(newImgSize[0] != imgSize[0]) {
	        img = new Image(imageF.getText(), newImgSize[0], newImgSize[1], false, true);
		}
		
		double dpiN = newImgSize[2]; //native image DPI
		double s    = dpiN/dpiT;     //scale to target DPI
		
		//ASSIGN INPUTS
		
		//physical parameters
		double[] physPar = new double[4];
		physPar[0] = dpiN*getR();  //cylinder radius
		physPar[1] = dpiN*getH();  //cylinder height
		physPar[2] = dpiN*getVx(); //view distance
		physPar[3] = dpiN*getVz(); //view height
        
		//GENERATE POLYGON POINTS
        
		//NOTE: x, y, and z refer to orientation of 3D Cartesian coordinates
		//Cartesian y --> pixel x, Cartesian x --> pixel y 
        
		//generate input grid points
		//2D array row0: y and row1: z
		double yzGrid[][] = yzGrid(newImgSize,n);
        
		//find minimum x element
		double[] temp = anamorphicT(physPar,yzGrid[0][0],yzGrid[1][0]);
		double minX = Math.floor(temp[0]);
        
		//keep cylinder visible in output
		if(minX > -Math.floor(physPar[0]) && drawCylinder){
			minX = -Math.floor(physPar[0]);
		}
        
		//generate output grid points
		//2D array row0: x and row1: y
		double xyGrid[][] = new double[2][yzGrid[0].length*yzGrid[1].length];
		for(int i=0; i<yzGrid[1].length; i++){
			int k = i*yzGrid[0].length;
			for(int j=0; j<yzGrid[0].length; j++){
				temp = anamorphicT(physPar,yzGrid[0][j],yzGrid[1][i]);
				xyGrid[0][k] = (temp[0] - minX)/s; //remove x offset
				xyGrid[1][k] = temp[1]/s;
				k++;
			}
		}
        
		//find minimum y element
		double minY = Double.MAX_VALUE;
		for(int i=0; i<=(int)Math.floor(0.5*(newImgSize[0]+1))*(n+1); i++){ 
			if(xyGrid[1][i] < minY){
				minY = xyGrid[1][i];
			}
		}
		minY = Math.floor(minY);
        
		//remove y offset
		for(int i=0; i<xyGrid[0].length; i++){
			xyGrid[1][i] = xyGrid[1][i] - minY;
		}
        
		//CONSTRUCT POLYGONS
        
		BorderPane output = new BorderPane();
        
		//read pixels from resized input image
		PixelReader imgPixel = img.getPixelReader();
		Color pixelColor = null;
        
		//loop generates a polygon "pixel" for each pixel of the input image and
		//fills the polygon with the associated color of the input image pixel
        
		if(hqMethod) { //render method 1 (better quality, uses more RAM)
			for(int i=0; i<newImgSize[1]; i++){        //pixel row selector (y)
				for(int j=0;j<newImgSize[0]; j++){ //pixel column selector (x)
					pixelColor = imgPixel.getColor(j,i);
					if(!pixelColor.equals(Color.TRANSPARENT) && !pixelColor.equals(ignoreColor)) {
						double[] polyPoints = new double[4*(n+2)];
						int l = 0; //polyPoints index
						int startR = j*(n + 1) + i*yzGrid[0].length;
						int stopR  = startR + n + 1;
						for(int k=startR; k<=stopR; k++){       //sweep right (top points)
							polyPoints[l]   = xyGrid[1][k]; //x coordinate
							polyPoints[l+1] = xyGrid[0][k]; //y coordinate
							l+=2;
						}
						int stopL  = j*(n + 1) + (i+1)*yzGrid[0].length;
						int startL = stopL + n + 1;
						for(int k=startL; k>=stopL; k--){       //sweep left (bottom points)
							polyPoints[l]   = xyGrid[1][k]; //x coordinate
							polyPoints[l+1] = xyGrid[0][k]; //y coordinate
							l+=2;
						}
						Polygon polyPixel = new Polygon(polyPoints);
						polyPixel.setFill(pixelColor);
						polyPixel.setStroke(pixelColor);
						polyPixel.setStrokeType(StrokeType.INSIDE);
						polyPixel.setStrokeWidth(10);
						Polyline polyLine = new Polyline(polyPoints);
						polyLine.setStroke(pixelColor);
						output.getChildren().add(polyPixel);
						output.getChildren().add(polyLine);
					}
				}
			}
		} else { //render method 2 (uses less RAM)
			for(int i=0; i<newImgSize[1]; i++){        //pixel row selector (y)
				for(int j=0;j<newImgSize[0]; j++){ //pixel column selector (x)
					pixelColor = imgPixel.getColor(j,i);
					if(!pixelColor.equals(Color.TRANSPARENT) && !pixelColor.equals(ignoreColor)) {
						double[] polyPoints = new double[4*(n+2)];
						int l = 0; //polyPoints index
						int startR = j*(n + 1) + i*yzGrid[0].length;
						int stopR  = startR + n + 1;
						for(int k=startR; k<=stopR; k++){       //sweep right (top points)
							polyPoints[l]   = xyGrid[1][k]; //x coordinate
							polyPoints[l+1] = xyGrid[0][k]; //y coordinate
							l+=2;
						}
						int stopL  = j*(n + 1) + (i+1)*yzGrid[0].length;
						int startL = stopL + n + 1;
						for(int k=startL; k>=stopL; k--){   //sweep left (bottom points)
							polyPoints[l]   = xyGrid[1][k]; //x coordinate
							polyPoints[l+1] = xyGrid[0][k]; //y coordinate
							l+=2;
						}
						Polygon polyPixel = new Polygon(polyPoints);
						polyPixel.setSmooth(false);
						polyPixel.setFill(pixelColor);
						polyPixel.setStrokeType(StrokeType.INSIDE);
						polyPixel.setStroke(pixelColor);
						polyPixel.setStrokeWidth(10);
						output.getChildren().add(polyPixel);
					}
				}
			}
		}

		//show base of cylinder in output image
		if(drawCylinder) {
			Circle cylinderBase = new Circle();
			cylinderBase.setCenterX(-minY);
			cylinderBase.setCenterY(-minX/s);
			cylinderBase.setRadius(physPar[0]/s);
			cylinderBase.setFill(null);
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

	private double[] newImgSize(double[] imgSize){
		
		//physical parameters
		double r  = getR();
		double h  = getH();
		double vx = getVx();
		double vz = getVz();
        
		//max width and height of picture plane
		double maxW = 2*r;
		double maxH = (vx*h - vz*r)/(vx - r);
        
		//calculate aspect ratios
		double ppAspect = maxW/maxH;
		double imgAspect = imgSize[0]/imgSize[1];
        
		//find native DPI of image
		double dpiN   = dpiT;
		double maxDim[] = new double[2]; //image constraints
		if (imgAspect > ppAspect){       //scaling constrained by width
			dpiN      = imgSize[0]/maxW;
			maxDim[0] = maxW;
			maxDim[1] = imgSize[0];
		} else{                          //scaling constrained by height
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
			double s = 1; //down scale factor
			if(3*maxDim[0]*dpiT <= 2000) {
				s = 3;
			} else if(2.5*maxDim[0]*dpiT <= 2000) {
				s = 2.5;
			} else if(2*maxDim[0]*dpiT <= 2000) {
				s = 2;
			} else if(1.5*maxDim[0]*dpiT <= 2000) {
				s = 1.5;
			}
			if (imgAspect > ppAspect){ //resize constrained by width
				newImgSize[0] = Math.floor(s*maxW*dpiT);             //resizeW
				newImgSize[1] = Math.floor((s*maxW*dpiT)/imgAspect); //resizeH
				newImgSize[2] = newImgSize[0]/maxW;                  //new native DPI
			} else{                    //resize constrained by height
				newImgSize[1] = Math.floor(s*maxH*dpiT);             //resizeH
				newImgSize[0] = Math.floor(s*maxH*dpiT*imgAspect);   //resizeW
				newImgSize[2] = newImgSize[1]/maxH;                  //new native DPI
			}
		}
        
		return newImgSize;
	}

	private static double[][] yzGrid(double[] newImgSize, int n){
		//Generates pixel gridpoints and maps (x,y,0) pixel 
		//coordinates to (0,y,z) Cartesian coordinates.
        
		int imgW = (int)newImgSize[0];
		int imgH = (int)newImgSize[1];
        
		double[][] yzGrid = new double[2][];  //2D array row0: y points, row1: z points
		yzGrid[0] = new double[imgW*(n+1)+1]; //y coordinates (from input x pixel coordinates)
		yzGrid[1] = new double[imgH+1];       //z coordinates (from input y pixel coordinates)
        
		double hOffset = Math.floor(0.5*(imgW+1)); //horizontal offset
        
		//generate Cartesian y coordinates
		for(int i=0; i<yzGrid[0].length; i++){
			yzGrid[0][i] = (double)i/(n+1) - hOffset;
		}
        
		//generate Cartesian z coordinates
		for(int i=imgH; i>=0; i--) {
			yzGrid[1][imgH-i] = i;
		}
        
		return yzGrid;
	}
    
	private static double[] anamorphicT(double[] physPar, double py, double pz){
		//Method uses extensive simplifications for case: vy=0 and px=0.
		//For a general solution, where vy and px may be arbitrary, see
		//derivation.
        
		//physical parameters
		double r  = physPar[0]; //radius of cylinder
		double vx = physPar[2]; //x-coordinate of vantage point
		double vz = physPar[3]; //z-coordinate of vantage point
        
		//ANAMORPHIC TRANSFORMATION
		//maps (0,y,z) to (x,y,0) in Cartesian coordinates
        
		//t parameter for intersection with cylinder
		double ts = (Math.pow(vx,2)-Math.sqrt(Math.pow(r,2)*(Math.pow(py,2)+Math.pow(vx,2))-Math.pow(vx*py,2)))/(Math.pow(py,2)+Math.pow(vx,2));
        
		//u parameter for midpoint of line A'A
		double um = (vx*py*(ts*(pz-vz)+vz))/((vz-pz)*(Math.pow(ts*py,2)+Math.pow(vx*(1-ts),2)));
        
		//anamorphic image point
		double[] W = new double[2];
		W[0] = 2*(vx - ts*(um*py + vx)) + (vx*pz)/(vz - pz);
		W[1] = 2*(ts*py + vx*um*(1 - ts)) - (vz*py)/(vz - pz);
        
		return W;
	}
    
	//GET INPUTS
	
	private double getR() {
		double r = Math.abs(Double.parseDouble(cylinderRF.getText()));
		return r;
	}
    
	private double getH() {
		double h = Math.abs(Double.parseDouble(cylinderHF.getText()));
		return h;
	}
    
	private double getVx() {
		double vx = Math.abs(Double.parseDouble(viewDF.getText()));
		return vx;
	}
    
	private double getVz() {
		double vz = Math.abs(Double.parseDouble(viewHF.getText()));
		return vz;
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
