package csc2b.client;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * The main method
 * @author Thwala TM 218091161
 * @version PX
 */
public class Client extends Application{

	/**
	 * launching the arguments
	 * @param args the arguments
	 */
	public static void main(String args[])
	{
		launch(args);
	}

	/**
	 * Setting up the stage
	 */
	@Override
	public void start(Stage primaryStage) throws Exception {
		ClientPane root = new ClientPane(primaryStage);
		primaryStage.setScene(new Scene(root, 700, 600));
		primaryStage.setTitle("Scalling");
		primaryStage.show();
		
	}
}
