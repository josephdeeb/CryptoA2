import java.io.*;
import java.net.*;

/**
 * Client program.  Connects to the server and sends text accross.
 */

public class Client 
{
    private Socket sock;  //Socket to communicate with.
    private boolean debug; //Debug flag for client
	
    /**
     * Main method, starts the client.
     * @param args args[0] needs to be a hostname, args[1] a port number, args[2] is optional and must either be "debug" or nothing.
     */
    public static void main (String [] args)
    {
	if (args.length < 2 || args.length > 3) {
	    System.out.println ("Usage: java Client hostname port# [debug]");
	    System.out.println ("hostname is a string identifying your server");
	    System.out.println ("port is a positive integer identifying the port to connect to the server");
	    System.out.println ("[debug] is an optional argument which enables the debugging mode on the client");
	    return;
	}
	boolean debugArg = false; // set debugArg to false by default
	if (args.length == 3) {
		if (args[2] == "debug") {
			debugArg = true; // If there are 3 arguments and the third argument is "debug" then set debugArg to true
		}
		else {
			System.out.println("Usage: java Client hostname port# [debug]");
			System.out.println("Third argument must either be \"debug\" or nothing");
			return;
		}
	}

	try {
	    Client c = new Client (args[0], Integer.parseInt(args[1]), debugArg);
	}
	catch (NumberFormatException e) {
	    System.out.println ("Usage: java Client hostname port# [debug]");
	    System.out.println ("Second argument was not a port number");
	    return;
	}
    }
	
    /**
     * Constructor, in this case does everything.
     * @param ipaddress The hostname to connect to.
     * @param port The port to connect to.
     */
    public Client (String ipaddress, int port, boolean debug)
    {
    this.debug = debug;
	/* Allows us to get input from the keyboard. */
	BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
	String userinput;
	PrintWriter out;
		
	/* Try to connect to the specified host on the specified port. */
	try {
	    sock = new Socket (InetAddress.getByName(ipaddress), port);
	}
	catch (UnknownHostException e) {
	    System.out.println ("Usage: java Client hostname port# [debug]");
	    System.out.println ("First argument is not a valid hostname");
	    return;
	}
	catch (IOException e) {
	    System.out.println ("Could not connect to " + ipaddress + ".");
	    return;
	}
		
	/* Status info */
	System.out.println ("Connected to " + sock.getInetAddress().getHostAddress() + " on port " + port);
		
	try {
	    out = new PrintWriter(sock.getOutputStream());
	}
	catch (IOException e) {
	    System.out.println ("Could not create output stream.");
	    return;
	}
		
	/* Wait for the user to type stuff. */
	try {
	    while ((userinput = stdIn.readLine()) != null) {
		/* Echo it to the screen. */
		out.println(userinput);
			    
		/* Tricky bit.  Since Java does short circuiting of logical 
		 * expressions, we need to checkerror to be first so it is always 
		 * executes.  Check error flushes the outputstream, which we need
		 * to do every time after the user types something, otherwise, 
		 * Java will wait for the send buffer to fill up before actually 
		 * sending anything.  See PrintWriter.flush().  If checkerror
		 * has reported an error, that means the last packet was not 
		 * delivered and the server has disconnected, probably because 
		 * another client has told it to shutdown.  Then we check to see
		 * if the user has exitted or asked the server to shutdown.  In 
		 * any of these cases we close our streams and exit.
		 */
		if ((out.checkError()) || (userinput.compareTo("exit") == 0) || (userinput.compareTo("die") == 0)) {
		    System.out.println ("Client exiting.");
		    stdIn.close ();
		    out.close ();
		    sock.close();
		    return;
		}
	    }
	} catch (IOException e) {
	    System.out.println ("Could not read from input.");
	    return;
	}		
    }
}
