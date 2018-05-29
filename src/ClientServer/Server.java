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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;


/*Server class implements Runnable interface which creates a thread for server to continuously listen for client connections*/

public class Server implements Runnable
{ 
	/*----------------For server log----------*/
	 BufferedWriter bw = null; 
	 FileWriter fw = null;
	 File file = new File("Chat_Log.txt");
	/*----------------------------------------*/
	 
	 
	 private ServerThread clients[] = new ServerThread[4]; //Class for managing each client thread
	 private ServerSocket server = null; //Server Socket
	 private Thread       thread = null; 
	 private int clientCount = 0; //Counter for number of clients
	 
	 private static List<String> ClientNames = new ArrayList<String>();//To store client names
	 private HashMap<Integer,String> hm=new HashMap<Integer,String>();  //To map client ID to name
	 
	
	 
	 /*Function: Constructor for Server to manage server operations
	  * Input: Port Number from main function
	  */
   public Server(int port)
   {  
	   try
      {  
	   	 //Server Socket created which is bound to the specified port.
	   	 System.out.println("Binding to port " + port + ", please wait  ...");
         server = new ServerSocket(port);  
         System.out.println("Server started: " + server);
         
         //Retrieval of Log file upon restart of server. 
         //Log file stores all the clients's chat data in a text file. 
         System.out.println("Do you want to retrieve the log file? yes/no");
         BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
         String userinput = br.readLine();
         
         // If the choice is yes or YES, the log file is retrieved
         if(userinput.toLowerCase().equals("yes"))
         {
        	 
        	 FileReader fr = new FileReader(file.getAbsolutePath());	//ChatLog text file is retrieved from its absolute path
        	 BufferedReader br1 = new BufferedReader(fr);
        	
        	 
        	 String line = br1.readLine(); //Read First line from file
        	 
        	
     		  if(line==null)  //When the server runs for the first time, the log file is empty.
     		  	{
     				System.out.println("Log is Empty. Server runs for the first time");
     			}
     		
     		  else 		
     		  {
     			 while (line!=null)// Read until EOF 
     			 {
     				 System.out.println(line); //The log details from the ChatLog.text file is displayed on the server
     				 line=br1.readLine();
     			 }
     			 br1.close();
     		  } 
     		 
     		 start(); // function to create a thread to listen for connections from client
     	 }
         // The choice to retrieve Log file is "no" or "NO", the file is not retrieved and the function to create thread is called.
         else if(userinput.toLowerCase().equals("no"))
         {
        	 start(); //function to create a thread to listen for connections from client
         }
         
      }
	  //catch block is executed when the server socket is unable to bind to the port
      catch(IOException ioe)
      { 
    	  System.out.println("Can not bind to port " + port + ": " + ioe.getMessage()); 
      }
   }
   
   
   /*start function creates a new thread to listen continuously for client connections.*/
     public void start()  
     { 
	   if (thread == null) //If thread doesn't exist, instantiate and start a new thread to listen for client connections.
	   {  
		thread = new Thread(this); 
		thread.start();//thread.start(); calls the run function.
	   } 
	 }
   
   /*run function is invoked from thread.start() and listens for a connection to be made from a client 
    * and calls addThread() which in turn creates a separate thread for each client. Thereby multi- threading is achieved*/
   public void run()
   {  while (thread != null)
      {  try
         {  System.out.println("Waiting for a client ..."); 
            addThread(server.accept()); 
         }
         catch(IOException ioe)   //catch block is executed when an error occurs and the thread created will be stopped and assigned to null
         {  System.out.println("Server accept error: " + ioe); stop(); }
      }
   }
   
   
   /*stop function is called when the thread needs to stopped or when an unexpected error occurs. */
   public void stop()   
   { 
	   if (thread != null)
	   {  
		   thread.stop(); 
		   thread = null;
	   }
   }
   
   /*Function: To return the nth client
    Input: Unique Client ID (socket.getPort()) during client instantiation
    Output: position of the client  */
   private int findClient(int ID)
   {  for (int i = 0; i < clientCount; i++)
         if (clients[i].getID() == ID)
            return i;
      return -1;
   }
   
   
   /*Function: To add a new thread for each client connection
    * Input: Socket */
   
   private void addThread(Socket socket)
   {  if (clientCount < clients.length) // TO check if number of clients exceeded the maximum limit
      {  System.out.println("Client accepted: " + socket);
         clients[clientCount] = new ServerThread(this, socket);// ClientServerThread Object instantiation
         try
         {  clients[clientCount].open(); //Function call to open input/output stream for each connecting client
            clients[clientCount].start();  //Function call to continuously listen for message from connected client
            clientCount++; //Increase the client count after each successful connection
          }
         catch(IOException ioe)  //catch block is executed when an error occurs and new thread could not be created
         {  System.out.println("Error opening thread: " + ioe); } }
      else
         System.out.println("Client refused: maximum " + clients.length + " reached."); // If No of clients > Maximum limit
   }
   
  /*Function: Reads messsages from ClientApplet and handles the function according to the messages *
   * Input: Unique ID for a client and the message sent by that client
   *Output: Handles messages according to the message received from Client*/ 
   public synchronized void handle(int ID,String input) throws IOException
   {  
	   try {
		   	fw = new FileWriter(file.getAbsolutePath(), true); //gets the Chat Log Text file from its absolute path
	   		} 
	   catch (IOException e1) 
	   		{
		// TODO Auto-generated catch block
		   	e1.printStackTrace();
	   		}
	   bw = new BufferedWriter(fw); //Creates a buffered character-output stream for the Filewriter
	   
	   String inputName=null;
	   System.out.println("\n");
	   System.out.println(input); // Prints the HTTP Post message sent by the client
	   StringTokenizer st=new StringTokenizer(input,"\n"); //extracts message which is sent in POST method format from the ClientApplet 
	   while(st.hasMoreTokens()) 
	   {
		   inputName=st.nextToken();// Extracts the last token which is the actual message sent by the client
	   }
	   
	   if (inputName.equals(".logoff")) //Checks if client calling this function is logging off
      { 
      	for (int i = 0; i < clientCount; i++) //broadcasts messages to all the connected clients when the current client logs off
     	clients[i].send(hm.get(ID) +" has logged off..");
		bw.append(hm.get(ID)+" "+" has logged off"); // write the log-off message to the Chat_Log File
		bw.write("\r\n");     		
    	bw.close();
      	ClientNames.remove(ClientNames.indexOf(hm.get(ID))); //remove the current client's registered name 
      	remove(ID); //remove the client object from the client object array
      	
       }
   	else if (inputName.contains("Name:")) // Checks if the client is registering Name 
   	{  
   		boolean NotExist = true; //boolean to check if the name is already registered 
   		StringTokenizer str=new StringTokenizer(inputName,":"); //extracts message which is sent in POST method format from the ClientApplet
   		str.nextElement();
		String Name=str.nextElement().toString();
		
		for (String item : ClientNames) //checks if the Name given by the current client is already registered by other connected clients
		{ 
			    if (Name.equalsIgnoreCase(item))
			    {
			    	System.out.println(item);
			    	NotExist = false; //if Name already exists, then boolean is set to false
			    	
			    }
		}
		
		    if(NotExist) //is the name does not already exist then validate by regular expression
		    {
			
		    Pattern pattern = Pattern.compile("(^[a-zA-Z]([a-zA-Z]?[0-9]?){1,9}$)"); //Maximum 11 characters ; Starts with alphabet ; Accepts alpha-numeric
	  		Matcher matcher = pattern.matcher(Name); 
		 
			    if (matcher.matches()) //checks if the name given matches the regex
			    {
			    	 hm.put(ID, Name); // use hash map to map the unique ID of the client to the name provided by the client
					 System.out.println("Client "+findClient(ID)+" registered with name:"+hm.get(ID)); //Display on the server that client is registered
					 clients[findClient(ID)].send(hm.get(ID)+"successfully registered..");//find the position of the current client and send it to ClientApplet that the Client is successfully registered with the name provided
					 ClientNames.add(Name); //Add the current client's name to the clientNames List
			    }
			    else 
			    {	
			       	
			    	clients[findClient(ID)].send("Name is not in valid format.!");//display message if the name does not match the regular expression pattern.
			 
			    }
		    } 
			    
		   
		   else {
			   
			   clients[findClient(ID)].send("Name already exists .!"); //displays that name already exists when boolean notExist is false
			   
		   		}
		   
   	}
     else //Broadcasts current client's message to all the connected clients
     {
    	 
			
			try
			{
				/*Appends all the chat messages to the CHat_Log File*/
					bw.append(hm.get(ID)+inputName);
					bw.write("\r\n");     		
		        	bw.close();
		           	
		      } 
			
		      catch (IOException e) //Catch block executes when error occurs
			{
				System.err.println(e);
			
			}
			
         for (int i = 0; i < clientCount; i++) //gets the count of the connected clients
        	 
        	
            clients[i].send(hm.get(ID)+":"+inputName); // broadcasts current client's message to all the connected clients
     }
   }
   
   /*Function: To remove a client after Log-Off  
    *Input: Unique Client ID (socket.getPort()) during client instantiation
    */
   public synchronized void remove(int ID)
   {  
	  int pos = findClient(ID); // Find position of a particular client by calling findClient(ID)
      
	  if (pos >= 0)
      {  ServerThread toTerminate = clients[pos];
         System.out.println("Removing client thread " + ID + " at " + pos);
       
         if (pos < clientCount-1) // Checks if position of the client to be removed is not the last connected client 
            for (int i = pos+1; i < clientCount; i++)
              clients[i-1] = clients[i]; // Arrange subsequent client positions after a client is removed
         clientCount--; //Decrease client count after removing the client
         
         try
         { 
        	 toTerminate.close(); //Close the client object which is removed
         
         }
         
         catch(IOException ioe)
         {  
        	 System.out.println("Error closing thread: " + ioe);
         }
         
         toTerminate.stop(); 
       }
   }
   
   /*Port Number for the server is specified as a command line argument*/
   public static void main(String args[]) 
   {  
	   Server server = null;
	   server = new Server(8080);
   }

  }//End Server Class
