package gui;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * First GUI of the application to replay coding processes
 * The User can input the URL of a Github repository and can submit it so it can be used in the second GUI
 * 
 * 
 * @author Tim Hartmann
 *
 */
public class ReceiveURL_GUI extends Application{

	private static TextField url;
	private static File localPath;
	private static Git git;
	Stage window;
	
	public static void main(String[] args) {
		launch();
	}
	
	
	@Override
	public void start(Stage primaryStage) {
		window = primaryStage;
		//Title of window
		primaryStage.setTitle("CodingReplayApplication");
		
		//Set GridPane layout
		GridPane layout = new GridPane();
		layout.setAlignment(Pos.TOP_LEFT);
		layout.setHgap(20);
		layout.setVgap(20);
		layout.setPadding(new Insets(5, 10, 10, 10));
		//Create and set scene
		Scene scene = new Scene(layout, 400, 150);
		primaryStage.setScene(scene);		
		//Create label
		Label url_label = new Label("URL:");
		layout.add(url_label, 0, 1);
		
		//Create text field to receive URL
		url = new TextField();
		url.setPrefWidth(300);
		layout.add(url, 1, 1);
		
		//Create submit button 
		Button submit_button = new Button("Submit");	
		layout.add(submit_button, 1, 2);
		
		final Text errorMessage = new Text();
        layout.add(errorMessage, 1, 3);
		
        //Set event of submit button
		submit_button.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {	
				//Do nothing if the URL field is empty
				if (url.getText() != null && !url.getText().isEmpty()) {
					localPath = null;
					//Load Github repository into local directory
					try {
						localPath = File.createTempFile(url.getText(), "");
						if(!localPath.delete()) {
							throw new IOException("Could not delete temporary file " + localPath);
						}
						git = Git.cloneRepository()
								.setURI(url.getText())
							    .setDirectory(localPath)      
							    .call();	
						//Start the second GUI
						ChooseCommit_GUI.start(window, git);						
					//Error handling
					} catch (IOException | GitAPIException | java.nio.file.InvalidPathException e2) {						
						//Display of error message in GUI
						errorMessage.setFill(Color.FIREBRICK);
						errorMessage.setText("Could not load repository");	
						remove(localPath);
					}						
					
				}												
				
			}
		});
							
		primaryStage.show();
	}
	
	/**
	 * Method which is executed when the window of the GUI is closed
	 * 
	 */
	@Override
	public void stop(){
		//delete local directory
		if(git != null && localPath != null) {
			git.close();
			remove(localPath);
		}
			    
	}
	
	/**
	 * Method to delete a directory with all its files
	 * 
	 * @param file
	 */
	private static void remove(File file) {
		if (file.isDirectory())
			for (File f : file.listFiles())
				remove(f);
		if (!file.delete())
			System.err.println("Could not delete file: " + file);
	}
	
	

}
