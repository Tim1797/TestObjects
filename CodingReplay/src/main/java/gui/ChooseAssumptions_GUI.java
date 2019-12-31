package gui;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.revwalk.RevCommit;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;

import java.util.ArrayList;
import java.util.List;

import org.controlsfx.control.CheckListView;
import org.controlsfx.control.IndexedCheckModel;

import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import logic.MainSimulation;

/**
 * Second GUI of the application to replay coding processes
 * The user can modify the preselection of assumptions
 * 
 * 
 * @author Tim Hartmann
 *
 */
public class ChooseAssumptions_GUI {
	private static final ObservableList<String> data = FXCollections.observableArrayList();
	static Stage window;
	static Git git;
	static RevCommit commit1;
	static RevCommit commit2;
	static List<String> choosedAssumptions;
	
	/**
	 * Starts the GUI
	 * 
	 * @param primaryStage
	 * @param given_git
	 * @param choosedCommit1
	 * @param choosedCommit2
	 */
	public static void start(Stage primaryStage, Git given_git, RevCommit choosedCommit1, RevCommit choosedCommit2) {
		commit1 = choosedCommit1;
		commit2 = choosedCommit2;
		git = given_git;
        window = primaryStage;
		
        //Create list of assumptions, so the user can choose
        final CheckListView<String> checkListView = new CheckListView<String>(data);
        checkListView.getItems().addAll("Annahme1", "Annahme2", "Annahme3", "Annahme4");
        
        //Get checked boxes
        IndexedCheckModel<String> icm = checkListView.getCheckModel();
        //icm.check("Annahme2");
        checkListView.setCheckModel(icm);
        
        //Create submit button
        Button button = new Button("Submit");
        button.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {		
				//Get chosen assumptions
				choosedAssumptions = new ArrayList<>();
				IndexedCheckModel<String> icm_choosed = checkListView.getCheckModel();
				choosedAssumptions = icm_choosed.getCheckedItems();				
					
				//start the simulation
				ShowSimulation_GUI.start(window);
			}
        });
        
        //Setup the GUI
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20, 20, 20, 20));
        layout.getChildren().addAll(checkListView, button);
        Scene scene= new Scene(layout, 600, 300);     
		primaryStage.setScene(scene);
		
				
	}
	
	public static List<String> getAssumptions() {
		return choosedAssumptions;
	}
	
	public static Git getGit() {
		return git;
	}
	
	public static RevCommit getCommit1() {
		return commit1;
	}
	
	public static RevCommit getCommit2() {
		return commit2;
	}
	
}
