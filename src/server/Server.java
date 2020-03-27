package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Server
{
    private static final int PORT = 1996;
    private List<ClientThread> etList = new ArrayList<>(  );
    private static Server ser;
    private boolean isRunnig = false;
    private ServerSocket finalServerSocket;

    public static void main( String args[] )
    {
        ser = new Server();
        ser.start();
    }

    public void start()
    {
        ServerSocket serverSocket = null;
        final Socket[] socket = {null};

        try {serverSocket = new ServerSocket( PORT );}
        catch( IOException e ) {e.printStackTrace();}

        finalServerSocket = serverSocket;
        new Thread( "New_clients" )
        {
            public void run()
            {
                if( !isRunnig )
                {
                    isRunnig = true;
                    while( isRunnig )
                    {
                        try {socket[0] = finalServerSocket.accept();}
                        catch( IOException e ) {break;}

                        ClientThread et = new ClientThread( socket[0], ser );
                        et.setDaemon( true );
                        et.start();

                        etList.add( et );
                    }
                }
            }
        }.start();
    }

    public void stop()
    {
        new Thread( "Close" )
        {
            public void run()
            {
                isRunnig = false;

                for( ClientThread el: etList )
                    el.close();

                try {finalServerSocket.close();}
                catch( IOException e ) {e.printStackTrace();}
            }
        }.start();
    }

    public void sayAll( String user, String msg ) throws IOException
    {
        SimpleDateFormat dateformat = new SimpleDateFormat( "dd:MM:yyyy HH:mm:ss" );
        for( ClientThread el: etList )
            el.say( "[" + dateformat.format( new Date() ) + "] " + "<" + user + ">" + " " + msg );
    }

    public void removeThread( ClientThread et )
    {
        etList.remove( et );
    }

}