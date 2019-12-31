package gui;


import java.util.List;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import logic.MainSimulation;


public class ShowSimulation_GUI {

	static StringBuilder sb;	
	static Text text;
	
	
	/**
	 * Load complete File, can be an old file of the earlier commit or an empty file in case of a new file
	 * 
	 * @param primaryStage
	 * @param changedLinesOfEachDiffEntry
	 */
	public static void start(Stage primaryStage) {					
		text = new Text();					
		sb = new StringBuilder();	
		
		MainSimulation.init();
		
		Button button1 = new Button("STEP");
        button1.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {		
				
				//call method to get complete file: In case of changed file get old file and make changes and show new one
				//if its a new file then show new file
				setNewText(MainSimulation.getNext());
							
			}
        });
                          
		
        //Create layout of GUI
		GridPane layout = new GridPane();
		text.setText(sb.toString());
		text.setX(0); 
	    text.setY(25); 
		ScrollPane scrollPane = new ScrollPane(text);	
		layout.add(scrollPane, 0, 0);
		layout.add(button1, 0, 2);	
		scrollPane.setPrefSize(2000, 2000);
		layout.setHgap(20);  
		layout.setVgap(20); 
        Scene scene= new Scene(layout, 1100, 700);     
		primaryStage.setScene(scene);
				
	}
	
	/**
	 * Set new output to show user
	 * 
	 * @param stringList
	 */
	public static void setNewText(List<String> stringList) {
		sb = new StringBuilder();
		for(String s: stringList) {
			sb.append(s + "\n");
		}
		text.setText(sb.toString());
		
	}
	
	
	
}
