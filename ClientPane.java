package csc2b.client;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Base64;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * the class handling the pane/gui and handling the different types image processes
 * @author Thwala TM, 218091161
 * @version PX
 */
public class ClientPane extends GridPane{

	private Button btnConnect;
	private Button btnSelection;
	private Button btnClassify;
	private Button btnClose;
	private Button btnProcessGrayScale;
	private Button btnProcessDilation;
	private Button btnProcessErosion;
	private Button btnFast;
	private ImageView imageView;
	private TextArea responseArea;
	
	private String grayScaleURL = "/api/GrayScale";
	private String erosionURL = "api/Erosion";
	private String dilationURL = "api/Dilation";
	private String fastFeaturesURL = "api/FastFeatures";
	private String fastURL = "api/Fast";
	
	//Streams for writing and reading data
	private BufferedReader BR;
	private DataOutputStream dataOut;
	
	//Keeping track of the image
	private File imageFileToClassify;
	
	//The socket
	private Socket socket;
	
	private int feature_Normal;
	private int feature_Defect;
	
	/**
	 * The constructor
	 * @param stage The stage
	 */
	public ClientPane(Stage stage)
	{
		//set up GUI
		setUpComponents();
		
		//handling the connect Button
		btnConnect.setOnAction(event->{
			//connect to port 5000
			connection(5000);
			
			//get response
			responseArea.appendText("Connected to the server\r\n");
			
			feature_Normal = getFeatures(new File("data/Examples/egNormal.jpg"));
			feature_Defect = getFeatures(new File("data/Examples/egDefect.jpg"));
		});
		
		//handling the selection button
		btnSelection.setOnAction(event->{
			FileChooser fileChoose = new FileChooser();
			fileChoose.setInitialDirectory(new File("data"));
			imageFileToClassify = fileChoose.showOpenDialog(stage);
			
			//display the selected images before processing
			Image image = new Image("File: " + imageFileToClassify.getAbsolutePath());
			imageView.setImage(image);
		});
		
		btnClassify.setOnAction(event->{
			//Get the fast features for the image to be tested
			int featureValue = getFeatures(imageFileToClassify);
			
			//clean the response area
			responseArea.clear();
			
			responseArea.appendText("Fast Features value: " + String.valueOf(featureValue) + "\r\n");
			
			String status = "";
			
			if(featureValue < feature_Normal + 50)
			{
				status = " ";
			}
			if(featureValue >= feature_Normal + 50 && featureValue <= feature_Defect)
			{
				status = " ";
			}
			if(featureValue > feature_Defect)
			{
				status = " ";
			}
			
			responseArea.appendText("Status: " + status + "\r\n");
		});
		
		//Handling the closing button
		btnClose.setOnAction(event->{
			System.out.println("Closing the connetion");
			//The the connection and the streams
			try {
				BR.close();
				dataOut.close();
				socket.close();
				
				//Closing the application
				stage.close();
				
				System.out.println("The application has been closed");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		
		btnProcessDilation.setOnAction(event->{
			
			//send dilation request
			process(dilationURL, imageFileToClassify);
			
			//Display image after it has been processed
			Image image = new Image("file:" + imageFileToClassify.getAbsolutePath());
			imageView.setImage(image);
		});
		
		btnProcessErosion.setOnAction(event->{
			
			//send erosion request
			process(erosionURL, imageFileToClassify);
			
			//Display image after it has been processed
			Image image = new Image("file:" + imageFileToClassify.getAbsolutePath());
			imageView.setImage(image);
		});
		
		
		btnProcessGrayScale.setOnAction(event->{
			//send gray scale request
			process(grayScaleURL, imageFileToClassify);
			
			//send erosion request
			//process(erosionURL, imageFileToClassify);
			
			//send dilation request
			//process(dilationURL, imageFileToClassify);
			
			//Display image after it has been processed
			Image image = new Image("file:" + imageFileToClassify.getAbsolutePath());
			imageView.setImage(image);
		});
		
		btnFast.setOnAction(event->{
			
			//get the fast feature
			fast(fastURL, imageFileToClassify);
			
			//Display image after it has been processed
			Image image = new Image("file:" + imageFileToClassify.getAbsolutePath());
			imageView.setImage(image);
		});
	}

	/**
	 * handling the processes of the image functions
	 * @param requestURL The URL
	 * @param imageFileToClassify2 classifying the image
	 */
	private void process(String requestURL, File imageFileToClassify2) {
		//reconnect to server
		connection(5000);
		
		try {
			//Input stream for reading from an image file
			FileInputStream fIn = new FileInputStream(imageFileToClassify2);
			byte[] bytes = new byte[(int) imageFileToClassify2.length()];
			fIn.read(bytes);
			fIn.close();
			
			//Encode the file into base64
			String encodedFile = new String(Base64.getEncoder().encodeToString(bytes));
			byte[] bytesToSend = encodedFile.getBytes();
			
			//Construct and send a request
			dataOut.write(("POST " + requestURL + " HTTP/1.1\r\n").getBytes());
			dataOut.write(("Content-Type: application/text\r\n").getBytes());
			dataOut.write(("Content-Length: " + encodedFile.length() + "\r\n").getBytes());
			dataOut.write(("\r\n").getBytes());
			dataOut.write(bytesToSend);
			dataOut.flush();
			dataOut.write(("\r\n").getBytes());
			
			responseArea.appendText("The Post command has been sent\r\n");
			
			//get the response
			String response = "";
			String line = "";
			
			while(!(line = BR.readLine()).equals("")){
				response += line + "\n";
			}
			System.out.println(response);
			
			//The data image
			String imageData = "";
			while((line = BR.readLine()) != null)
			{
				imageData += line;
			}
			System.out.println(imageData);
			
			//Get the received data
			String imageBase64 = imageData.substring(imageData.indexOf('\'') + 1, imageData.lastIndexOf('}')-1); 
			System.out.println(imageBase64);
			
			//Decode the base64 string received
			byte[] imageBytes = Base64.getDecoder().decode(imageBase64);
			
			//create an image file keep track of the processed image
			File egFile = new File("data/Examples/eg.jpg");
			FileOutputStream fos = new FileOutputStream(egFile);
			fos.write(imageBytes);
			fos.flush();
			fos.close();
			
			//updated image
			imageFileToClassify = egFile;
			} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Handling the fast feature
	 * @param requestURL the URL
	 * @param imageFile The classification of the image
	 */
	private void fast(String requestURL, File imageFile) {
		//reconnect to server
		connection(5000);
		
		try {
			//Input stream for reading from an image file
			FileInputStream fIn = new FileInputStream(imageFile);
			byte[] bytes = new byte[(int) imageFile.length()];
			fIn.read(bytes);
			fIn.close();
			
			//Encode the file into base64
			String encodedFile = new String(Base64.getEncoder().encodeToString(bytes));
			byte[] bytesToSend = encodedFile.getBytes();
			
			//Construct and send a request
			dataOut.write(("POST " + requestURL + " HTTP/1.1\r\n").getBytes());
			dataOut.write(("Content-Type: application/text\r\n").getBytes());
			dataOut.write(("Content-Length: " + encodedFile.length() + "\r\n").getBytes());
			dataOut.write(("\r\n").getBytes());
			dataOut.write(bytesToSend);
			dataOut.flush();
			dataOut.write(("\r\n").getBytes());
			
			responseArea.appendText("The Post command has been sent\r\n");
			
			//get the response
			String response = "";
			String line = "";
			
			while(!(line = BR.readLine()).equals("")){
				response += line + "\n";
			}
			System.out.println(response);
			
			//The data image
			String imageData = "";
			while((line = BR.readLine()) != null)
			{
				imageData += line;
			}
			System.out.println(imageData);
			
			//Get the received data
			String imageBase64 = imageData.substring(imageData.indexOf('\'') + 1, imageData.lastIndexOf('}')-1); 
			System.out.println(imageBase64);
			
			//Decode the base64 string received
			byte[] imageBytes = Base64.getDecoder().decode(imageBase64);
			
			//create an image file keep track of the processed image
			File egFile = new File("data/Examples/eg.jpg");
			FileOutputStream fos = new FileOutputStream(egFile);
			fos.write(imageBytes);
			fos.flush();
			fos.close();
			
			//updated image
			imageFileToClassify = egFile;
			} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Getting the feaetures
	 * @param file The file
	 * @return returning the value
	 */
	private int getFeatures(File file) {
		
		int Value = 0;
		
		//because our server aborts the connect make such it reconnects for each request
		connection(5000);
		try {
			//Input streams for reading from the file
			FileInputStream fIn = new FileInputStream(file);
			byte[] buffer = new byte[(int) file.length()];
			fIn.read(buffer);
			fIn.close();
			
			//encode the file into base64
			String encodedFile = new String(Base64.getEncoder().encodeToString(buffer));
			byte[] bytesToSend = encodedFile.getBytes();
			
			//Construct and send a request
			dataOut.write(("POST " + fastFeaturesURL + " HTTP/1.1\r\n").getBytes());
			dataOut.write(("Content-Type: application/text\r\n").getBytes());
			dataOut.write(("Content-lenght: " + encodedFile.length() + "\r\n").getBytes());
			dataOut.write(("\r\n").getBytes());
			dataOut.write(bytesToSend);
			dataOut.flush();
			dataOut.write(("\r\n").getBytes());
			
			responseArea.appendText("The Post command has been sent\r\n");
			
			//get the response
			String response = "";
			String line = "";
			
			while(!(line = BR.readLine()).equals(""))
			{
				response += line + "\n";
			}
			System.out.println(response);
			
			//get the object
			String fastFeature = "";
			while((line = BR.readLine()) != null)
			{
				fastFeature += line;
			}
			
			System.out.println(fastFeature);
			
			//Get the fast feature value
			String featureValue = fastFeature.substring(fastFeature.indexOf('[') + 1, fastFeature.lastIndexOf(']'));
			Value = Integer.parseInt(featureValue);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Value;
	}

	/**
	 * Connection establishment
	 * @param port the connection port
	 */
	private void connection(int port) {
	
			try {
				socket = new Socket("localhost", port);
				BR = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				dataOut = new DataOutputStream(socket.getOutputStream());
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
	}

	/**
	 * Setting up the components
	 */
	private void setUpComponents() {
		
		setVgap(10);
		setHgap(10);
		setAlignment(Pos.CENTER);
		
		btnConnect = new Button("Connect");
		btnSelection = new Button("Select an Image");
		btnProcessGrayScale = new Button("Process Gray Scale");
		btnClassify = new Button("Classify");
		btnClose = new Button("				Close				");
		btnProcessDilation = new Button("Process Dilation");
		btnProcessErosion = new Button("Process Erosion");
		btnFast = new Button("Classify-Fast");

		responseArea = new TextArea("Response Area:\r\n");
		
		imageView = new ImageView();
		imageView.setFitHeight(300);
		imageView.setFitWidth(300);
		
		add(btnConnect, 0, 0);
		add(btnSelection, 0, 1);
		add(btnProcessGrayScale, 1, 1);
		add(btnProcessDilation, 2, 1);
		add(btnProcessErosion, 3, 1);
		add(btnClassify, 4, 1);
		add(btnFast, 5, 1);
		add(btnClose, 1, 3, 5, 1);
		
		add(responseArea, 0, 4, 5, 1);
		add(imageView, 1, 5, 5, 1);
	}
}
