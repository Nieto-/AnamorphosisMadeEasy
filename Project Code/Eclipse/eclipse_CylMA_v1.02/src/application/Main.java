package application;
	
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 *Cylindrical Mirror Anamorphosis
 *GUI code
 *
 *NEW FEATURES/CHANGES
 * 
 *1) Disabled user resizing of GUI main window.
 *
 *2) Split code into separate classes.
 *
 *3) Fixed custom font not loading for jar file
 *
 * @author Garrett R. Mackelprang
 * @version 1.02
 * @edited  2019.02.16
 */

public class Main extends Application {
	
	private static String inputImg = ""; //image file location
	
	//TEXT FIELDS, CHOICE BOXES, & RADIO BUTTONS
	
    private TextField cylinderRF = new TextField();
    private TextField cylinderHF = new TextField();
    private TextField viewDF     = new TextField();
    private TextField viewHF     = new TextField();
    
    private ChoiceBox<String> dpiTF = new ChoiceBox<>(); //target DPI selector
    private ChoiceBox<String> intPF = new ChoiceBox<>(); //interpolating pts selector
    
    private RadioButton renderOpt2RB = new RadioButton("Low RAM");
    private RadioButton renderOpt3RB = new RadioButton("Ignore WHITE");
    private RadioButton outputOpt1RB = new RadioButton("Show Cylinder Base");
    private RadioButton outputOpt3RB = new RadioButton("Preserve Transparency");
    
	//GUI CREATION
    
    //display GUI
	@Override
	public void start(Stage primaryStage) throws Exception {
		
		BorderPane mainWindow = new BorderPane();
		mainWindow.setStyle("-fx-background-color: #f0f0f0");
		mainWindow.setPadding(new Insets(7,7,7,7));
		GridPane inputs = addInputs();
		mainWindow.setCenter(inputs);
		
		VBox options = addOptions();
		mainWindow.setRight(options);
		
		HBox bottomBar = addBottomBar();
		mainWindow.setBottom(bottomBar);

		primaryStage.setScene(new Scene(mainWindow,400,225));
		primaryStage.setResizable(false);
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
	    
		Button transformB = new Button("Transform");
		Button aboutB     = new Button("About");
	    
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
			}});

	    return bottomBar;
	}
	
	//create error message
	private static VBox addError(String errTxt) {
		
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
	private static VBox addAbout() {
		
		VBox about = new VBox();
		about.setStyle("-fx-background-color: WHITE");
		about.setPadding(new Insets(10,20,20,20));
		about.setAlignment(Pos.CENTER);
		
		try {//get custom font
		    InputStream fontStream = Main.class.getResourceAsStream("bradley.ttf");
	        Font bradley = Font.loadFont(fontStream, 42);
	        fontStream.close();

	    	Text line1 = new Text("Cylindrical Mirror");
	    	line1.setFont(bradley);
	    	line1.setFill(Color.BLACK);
	    	about.getChildren().add(line1);
	    
	    	Text line2 = new Text("Anamorphosis");
	    	line2.setFont(bradley);
	    	line2.setFill(Color.BLACK);
	    	about.getChildren().add(line2);
		} catch (IOException e) {
			e.printStackTrace();
		}
	    
	    Label line3 = new Label("A simple Java program that distorts an image such that "
	    		+ "viewing its reflection off a cylindrical mirror (from a particular "
	    		+ "vantage point) will restore the image.");
	    line3.setFont(Font.font("Calibri Light",13));
	    line3.setPrefSize(320, 90);
	    line3.setWrapText(true);
	    line3.setStyle("-fx-text-alignment: center;");
	    about.getChildren().add(line3);
	    
	    Text line4 = new Text("Garrett Mackelprang, Feb. 2019");
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
	    
	    intPF.getItems().addAll("0", "3", "5", "7", "9", "11", "13");
	    intPF.setValue("0");
	    
	    Button imageB = new Button("File");
	    imageB.setPrefSize(60, 20);
	    dpiTF.setPrefSize(60, 20);
	    intPF.setPrefSize(60, 20);
	    
	    inputs.add(imageL1,0,0); 		inputs.add(imageB,1,0);			
	    inputs.add(dpiL1,0,1); 			inputs.add(dpiTF,1,1); 			inputs.add(dpiL2,2,1);
	    inputs.add(cylinderRL1,0,2);	inputs.add(cylinderRF,1,2);		inputs.add(cylinderRL2,2,2);
	    inputs.add(cylinderHL1,0,3);	inputs.add(cylinderHF,1,3); 	inputs.add(cylinderHL2,2,3);
	    inputs.add(viewDL1,0,4); 		inputs.add(viewDF,1,4); 		inputs.add(viewDL2,2,4);
		inputs.add(viewHL1,0,5); 		inputs.add(viewHF,1,5); 		inputs.add(viewHL2,2,5);
	    inputs.add(intPL1,0,7);			inputs.add(intPF,1,7);
		
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
					inputImg = selectedImage.toURI().toString();
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
	    Label renderOptL         = new Label("--- Rendering Options ---");
	    
	    RadioButton renderOpt1RB = new RadioButton("HQ");
	    RadioButton outputOpt2RB = new RadioButton("Flatten to WHITE");
	    
	    final ToggleGroup setRenderOpt = new ToggleGroup();
	    renderOpt1RB.setToggleGroup(setRenderOpt);
	    renderOpt2RB.setToggleGroup(setRenderOpt);
	    renderOpt1RB.setSelected(true);
	    
	    HBox renderOptLabel = new HBox();
	    renderOptLabel.getChildren().add(renderOptL );
	    renderOptLabel.setAlignment(Pos.CENTER);
	    
	    //output image options label and buttons
	    Label outputOptL         = new Label("---- Output Options ----");
	    
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
			inputImg.trim().isEmpty()             ||
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
			Transform.startTransformation(inputImg);
		}
	}
	
	//SET RENDERING AND OUTPUT OPTIONS
	
	private void setOptions() {
		
		//set parameter values
		Transform.dpiT = getDpiT(); //target output DPI
		Transform.r    = getR();    //cylinder radius
		Transform.h    = getH();    //cylinder height
		Transform.vx   = getVx();   //viewing distance
		Transform.vz   = getVz();   //viewing height
		Transform.n    = getN();    //interpolating points
		
		//output image parameters text
		Transform.params = dpiTF.getValue() + ","
				+ cylinderRF.getText()      + ","
				+ cylinderHF.getText()      + ","
				+ viewDF.getText()          + ","
				+ viewHF.getText();
		
		if(Transform.n!=0) {
			Transform.params += " " + Transform.n + "n";
		}
		
		//set Low RAM rendering option
        if(renderOpt2RB.isSelected()) {
        	Transform.hqMethod = false;
        	Transform.params += " LoRAM";
        } else {
        	Transform.params += " HQ";
        }
        
        //set ignore WHITE rendering option
        if(renderOpt3RB.isSelected()) {
        	Transform.ignoreColor = Color.WHITE;
        	Transform.params += " IgnWht";
        }
        
        //don't show cylinder base in output image
        if(!outputOpt1RB.isSelected()) {
        	Transform.drawCylinder = false;
        } else {
        	Transform.params += " DrwCyl";
        }
        
        //set output image background color to TRANSPARENT
        if(outputOpt3RB.isSelected()) {
        	Transform.backgndColor = Color.TRANSPARENT;
        	Transform.params += " PresTrans";
        }
	}
    
    //GET INPUTS
	
    private double getDpiT() {
    	return Double.parseDouble(dpiTF.getValue());
    }
    
    private double getR() {
    	return Math.abs(Double.parseDouble(cylinderRF.getText()));
    }
    
    private double getH() {
    	return Math.abs(Double.parseDouble(cylinderHF.getText()));
    }
    
    private double getVx() {
    	return Math.abs(Double.parseDouble(viewDF.getText()));
    }
    
    private double getVz() {
    	return Math.abs(Double.parseDouble(viewHF.getText()));
    }
    
    private int getN() {
    	return Integer.parseInt(intPF.getValue());
    }
    
	public static void main(String[] args) {
		launch(args);
	}
}