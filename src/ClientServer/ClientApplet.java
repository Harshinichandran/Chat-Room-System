/*Name: Harshini Chandrasekar
  ID: 1001586563
 */

/*-------------------References-------------------------------------------------------------------------*/
/*1. http://pirate.shu.edu/~wachsmut/Teaching/CSAS2214/Virtual/Lectures/chat-client-server.html
2. http://javarevisited.blogspot.com/2015/06/how-to-create-http-server-in-java-serversocket-example.html
3. Secure Program with static analysis by Brian Chess & Jacob West- (Page – 319 & 320)
4. http://www.baeldung.com/java-write-to-file
5. https://regex101.com/
6. https://stackoverflow.com/questions/10820033/make-a-simple-timer-in-java/14323134

/*-------------------References---------------------------------------------------------------------------*/




package ClientServer;

import java.net.*;
import java.util.Date;
import java.io.*;
import java.applet.*;
import java.awt.*;

/*Class : Contains chat interface in which messages are sent to and received from the server */
public class ClientApplet extends Applet // 
{ 
   private Socket socket = null; //Object for Socket class
   private DataOutputStream streamOut = null; //Object for DataOutputStream
   private ClientThread client    = null;  //Object for the ClientThread which Listens to and handles the message received from the server

   private boolean flag=false; 
   
   private TextArea  display = new TextArea(); //Variables initialized for the chat interface
   private TextField input   = new TextField();
   private Button    send    = new Button("Send"), connect = new Button("Connect"),
                     quit    = new Button("Log Off");
   
   private String    serverName = null;
   private int       serverPort = 0;
   
 //Variables initialized for implementing timer 
   private volatile long startTimer = 0;
   private long elapsedTime = 0;
   private long presentTime = 0;
   private long elapsedSeconds = 0;
   private long secondsDisplay = 0;
	private long elapsedMinutes = 0;
	private String timediff=null;
	
	//Variables used for storing the successfully registered Client name
	private String potentialName=null; 
	private String actualName=null;

/*Function: Called by the browser or applet viewer to display the chat interface
 * Output: A chat interface where the client operations take place  */
public void init()
   {  Panel keys = new Panel(); keys.setLayout(new GridLayout(1,2)); //Container class which provides the space in which other components are added
      keys.add(quit); keys.add(connect); //Log off and Connect Button are added
      Panel south = new Panel(); south.setLayout(new BorderLayout()); // constructs new border Layout
      south.add("West", keys); south.add("Center", input);  south.add("East", send); // positions the buttons and text field inside the Layout
      Label title = new Label("Chat Room System", Label.CENTER); //label for the application
      title.setFont(new Font("Helvetica", Font.BOLD, 14)); // Font type
      setLayout(new BorderLayout());//This method changes layout-related information
      add("North", title); add("Center", display);  add("South",  south);
      quit.	setEnabled(false); //the Log off button is disabled before connection to the server 
      send.	setEnabled(false); //the Send button is disabled before connection to the server 
      getParameters(); //Function contains Server and port number
      }

/*Function: triggers action according to the button clicked(Connect, Send, Logoff)
  *Input: Event object which encapsulates events from the Graphical user Interface 
 *Output: Calls respective functions depending on the actions triggered */

   public boolean action(Event e, Object o)
   {  
	   if (e.target == quit)//When the Log Off button is clicked 
      {  	input.setText(".logoff"); // the value for the button is set as ".logoff"
      		try {
				send();  //sends message ".logoff" to the server
			} catch (IOException e1) { //catch block is executed when error occurs
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}  quit.setEnabled(true); send.setEnabled(true); connect.setEnabled(true);
      }
      else if (e.target == connect)//When Connection button is clicked
      {  	
    	  connect(serverName, serverPort); //Connects to the server 'localhost' in port '8080'
    	  try {
			registername(); //Registers name for the client at the server immediately after they are connected
		} catch (IOException e1) { //Catch Block executed when error occurs
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	 
     }
      else if (e.target == send) // When send button is clicked
      { 
    	  try {
			send(); //sends messages enetered in the text Field to the server
		} catch (IOException e1) {  //Catch Block executed when error occurs
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
    	  
      }
   
      return true; // returns true if any of the events are triggered
   }
  
   
   
  /*Function: Establishes connection with the server and is called when the connect button is clicked
   * Input: Server name(localhost) and port number(8080)
   * Output: Connects to the server using the given server name and port */
   public void connect(String serverName, int serverPort)
   {  println("Establishing connection. Please wait ...");
      try
      {  socket = new Socket(serverName, serverPort); //ServerName and port number stored in the object of socket
         println("Connected: " + socket);
         open(); //Function which opens input and output stream
         send.setEnabled(true); //send button enabled for client to start sending messages
         connect.setEnabled(false); //connect button is disabled as connection is already established
         quit.setEnabled(true); //Log off button enabled for client to leave the chat room
      }
      catch(UnknownHostException uhe) //catch block executed when the connection to server fails and other unexpected errors
      {  println("Host unknown: " + uhe.getMessage()); }
      catch(IOException ioe)
      {  println("Unexpected exception: " + ioe.getMessage()); } 
    }
  
   
   /*Function: Provides information in the applet about acceptable name formats and sets flag to TRUE indicating that name registration is yet to be done */
   public void registername() throws IOException
   {
	   println("Enter the name you want to register");
	   println("*----Valid format : Maximum 11 characters ; Starts with alphabet ; Accepts alpha-numeric------*");//Format for registering name; validated at Server using regex.		
	   flag=true;//Flag is true when client isn't registered yet

	}
   
   /*Function: Instantiates a timer to track the interval between messages received from that client.
    * Output: calculates and returns the timeinterval between messages for a particular client   */
   public String timer()
   {
	    
	   if(startTimer==0) //when client sends first message, timer will be at 0:0
	   startTimer = System.currentTimeMillis(); //Returns the current time in milliseconds only for the first message
	   
	   presentTime=System.currentTimeMillis();//Returns the current time in milliseconds
	   	elapsedTime = presentTime - startTimer;//calculates the difference between start time and presenttime
	     elapsedSeconds = elapsedTime / 1000; //converts milliseconds to seconds
	   	 secondsDisplay = elapsedSeconds % 60; //modulo operation to find the number of seconds in the time difference
	   	 elapsedMinutes = elapsedSeconds / 60; //divide the number of seconds to display the number of minutes in the time difference 	  	
	   	
	   	 timediff=Long.toString(elapsedMinutes)+":"+Long.toString(secondsDisplay); // time difference displayed in minute:seconds format
	   	 startTimer=presentTime; //reset starttime to PresentTime for further calculations
	   
	   	 return timediff; //time difference between meesages is returned
   }
   
   
   /*Function : Messages from the client is sent to the Server in POST method Format*/
   private void send() throws IOException
   {  
	   
	   StringBuilder sb=new StringBuilder();
	   Date date = new Date(); //calculates the current time when the message is sent
	   String post="POST /Server HTTP/1.1"; 
	   String host="Host: http://localhost:8080/Server";
	   String accept="Accept: text/xml, text/html, text/plain, image/plain";
	   String acceptLanguage="Accept-Language: en-us,en";
	   String acceptEncode="Accept-Encoding: qzip";
	   String useragent="User-Agent: Mozilla/5.0";
	   String contenttype="Content-Type: application/x-www-form-urlencoded";
	   String contentLength="Content-Length:"+input.getText().length(); //calculates the length of the input
	   String currentdate= date.toString(); 
	   String connection="Connection: keep-alive";
	   
	   if (flag==true) //flag is true when client hasn't registered the name 
	  	{ 
		   try {		   
			   String inputtext="Name:"+input.getText(); //appends "Name:" along with the input(Name which client wants to register)
			   potentialName=input.getText(); //stores the client name in a potentialName string (used for disabling functions after client log off)
			    
			   /*appends the Name which the client want to register in the POST method format*/
			   sb.append(post).append("\n").append(accept).append("\n").append(acceptLanguage).append("\n").append(acceptEncode).append("\n").append(useragent).append("\n").append(contenttype).append("\n").append(contentLength).append("\n").append(currentdate).append("\n").append(connection).append("\n").append(host).append("\n").append(inputtext);
			   
			  
			
			  		streamOut.writeUTF(sb.toString()); //Writes the appended string to the output stream
			  		println("Given Name:"+input.getText()); //registered name is displayes on the interface
			  		streamOut.flush(); //Buffered output bytes are written to the outputStream
			  		input.setText(""); //Sets the value of input component to blank after writing to the output stream	
		  		}
			  		
		
		 catch (IOException e)  //catch block is executed when error occurs 
		   {
			// TODO Auto-generated catch block
			e.printStackTrace();
			} 
	  	}
	  else if(flag==false) //flag is set to false when the user sends messages to other connected clients after sucessful registration
	  {
		  try
		  {  
		   String inputtext=null;//Variables initialized to send messages to Server
		   String timer = null;
		 
		   if(!input.getText().equals(".logoff"))  //checks if the input is only messages that a client sends to another connected client
		   {
			 timer= timer(); //if it is input messages then the timer function is called and calculates the interval between messages sent
			 inputtext="("+timer+") :" + input.getText(); //appends the time interval for the message with the actual message
		   }
		   else if(input.getText().equals(".logoff")) //checks if the client is logging off from the chat room
		   {
			 inputtext=input.getText(); //only ".logoff" is sent as a message to the Server 
		   }
		   /*appends the Name which the client want to register in the POST method format*/
		   sb.append(post).append("\n").append(accept).append("\n").append(acceptLanguage).append("\n").append(acceptEncode).append("\n").append(useragent).append("\n").append(contenttype).append("\n").append(contentLength).append("\n").append(currentdate).append("\n").append(connection).append("\n").append(host).append("\n").append(inputtext);
		
		   streamOut.writeUTF(sb.toString()); //Writes the appended string to the output stream
		   streamOut.flush(); //Buffered output bytes are written to the outputStream
		   input.setText(""); //Sets the value of input component to blank after writing to the output stream	

	   
		  }
		  catch(IOException ioe) //catch block is executed when error occurs 
		  {  println("Sending error: " + ioe.getMessage()); close(); }
	   
	  }
	 }
   
   
   /*Function: Reads messsages from Server and then Handles and displays messages to the chat interface according to the given message from Server
    *Input: Messages from Server
    *Output: Display messages on the chat Interface according to messages from Server */
   public void handle(String msg)
   {  
	if (msg.contains("has logged off..")) //Checks if a client has logged off 
      {  println(msg); //prints on the chat interface to all the connected clients when a client logs off
      	 
      	if(msg.contains(actualName)) //checks if the name from the Server and in Client are the same when the client logs off
      	{	
      	println("Close the Applet Window to Exit..");
      	close();
      	quit.	setEnabled(false); //Disable all the buttons and functionalities after a client logs off 
      	send.	setEnabled(false);
      	connect.setEnabled(false);
      	}
      }
   
   	else if(msg.contains("successfully registered..")) //Checks if a client is successfully registered
		{
			flag=false; //flag is set to false, as the Name for the client is successfully registered
			actualName=potentialName; // sets the client name into actual name to disable buttons and functionalities when a client logs off
			println("Name Registration Successful");
		}
		else if(msg.contains("Name is not in valid format.!") || msg.contains("Name already exists.!") ) //checks if the validation when client registers name has failed
		{
			System.out.println("Registration Unsuccessful"); //print the messages if the validation fails
			println(msg+" Please enter the name again:");
		}
      else println(msg); // print if it contains any other message
	}
   
   
   
  /*Function: Creates a DataOutputStream for writing messages to output stream */ 
   public void open()
   {  try
      {  streamOut = new DataOutputStream(socket.getOutputStream()); //opens the output stream for the current client
         client = new ClientThread(this,socket); //call to the ClientThread which listens to and handles the message received from the server
      }
      catch(IOException ioe) //catch block is executed when error occurs 
      {  println("Error opening output stream: " + ioe); } 
   }
  
     
   /*Function:Closes the InputStream , OutputStream and Socket*/
   //Close Function is called when the Server is stopped or when unexpected error occurs.   
   public void close()
   {  try
      {  if (streamOut != null)  streamOut.close();
         if (socket    != null) {  socket.close();}
       }
      catch(IOException ioe)
      {  println("Error closing ..."); }
      client.close(); 
      //client.stopp();
   }
  
  //Function : Displays messages on the chat interface 
   private void println(String msg)
   {  display.append(msg + "\n"); 
	
   }
  //Function: gets the server and port number where the server is connected
   public void getParameters()
   {  serverName = "localhost";
   	  serverPort = 8080; }
	}