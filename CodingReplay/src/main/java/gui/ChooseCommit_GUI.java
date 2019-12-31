package gui;

import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * Second GUI of the application to replay coding processes
 * The user can choose two commits. The differences of these two commits are
 * later simulated
 * 
 * 
 * @author Tim Hartmann
 *
 */
public class ChooseCommit_GUI {
	private static final ObservableList<RevCommit> data = FXCollections.observableArrayList();
	static Stage window;
	static Git git;

	/**
	 * Starts the GUI
	 * 
	 * @param primaryStage
	 * @param given_git
	 */
	public static void start(Stage primaryStage, Git given_git) {
		git = given_git;
        window = primaryStage;
		
        //First list of commits
        final ListView<RevCommit> listView = new ListView<RevCommit>(data);
        listView.setMinSize(200, 250);
        listView.setEditable(false);
        //Second list of commits
        final ListView<RevCommit> listView2 = new ListView<RevCommit>(data);
        listView.setMinSize(200, 250);
        listView.setEditable(false);
        
        Iterable<RevCommit> commits;
		
        try {
        	//get commits of the repository so they can be displayed
			commits = git.log().all().call();			
	        for (RevCommit commit : commits) {
	            data.add(commit);
	        }
		} catch (GitAPIException | IOException e) {
			e.printStackTrace();
		}		
        //Add commits the lists
        listView.setItems(data);  
        listView2.setItems(data);
        final Text errorMessage = new Text();
        
        //Create submit button
        Button button = new Button("Submit");
        button.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {	
				//Read out the two chosen commits
				RevCommit choosedCommit1 = listView.getSelectionModel().getSelectedItem();
				RevCommit choosedCommit2 = listView2.getSelectionModel().getSelectedItem();	
				
				//Error handling if no commit or only one is selected
				if(choosedCommit2 == null || choosedCommit1 == null) {
					errorMessage.setFill(Color.FIREBRICK);
					errorMessage.setText("Choose two commits");	
				}					
				//Error handling if the second commit is from a later time then the first
				else if(choosedCommit1.getCommitTime() < choosedCommit2.getCommitTime()) {
					errorMessage.setFill(Color.FIREBRICK);
					errorMessage.setText("Choose a later commit for the second");		
				}
				else {
					//Start the third GUI
					ChooseAssumptions_GUI.start(window, git, choosedCommit1,choosedCommit2);
				}
											
			}
        });
        
        //Setup the GUI
        Text t = new Text("Choose commits to compare!");
		t.setFont(Font.font("Verdana", 15));
		
        GridPane layout = new GridPane();
        layout.setAlignment(Pos.TOP_LEFT);
		layout.setHgap(20);
		layout.setVgap(20);
		layout.setPadding(new Insets(10, 10, 10, 10));
        
        layout.add(t, 0, 0);
        layout.add(listView, 0, 1);
        layout.add(listView2, 1, 1);
        layout.add(button, 0, 3);
        layout.add(errorMessage, 1, 3);
                                  
        Scene scene= new Scene(layout, 600, 500);     
		primaryStage.setScene(scene);
		
		
	}
	
	private RevCommit getFirstCommit() {
		int commitTime = Integer.MAX_VALUE;
		RevCommit firstCommit = null;
		 try {
			Iterable<RevCommit> commits = git.log().all().call();
			
			for (RevCommit commit : commits) {
	           if(commit.getCommitTime() < commitTime) {
	        	   commitTime = commit.getCommitTime();
	        	   firstCommit = commit;
	           }
			}			
		} catch (GitAPIException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
		return firstCommit;
	}

}
