package server;

import java.io.*;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

public class ClientThread extends Thread
{
    private String fileName = "config.properties";
    private Properties prop = new Properties();
    private Socket socket;
    private Server ser;
    private DataInputStream dis = null;
    private DataOutputStream dos = null;
    private String user;
    public boolean isRunning = false;

    public ClientThread( Socket clientSocket, Server srv )
    {
        this.socket = clientSocket;
        ser = srv;
        try
        {
            dis = new DataInputStream( socket.getInputStream() );
            dos = new DataOutputStream( socket.getOutputStream() );
        }
        catch( IOException e )
        {
            e.printStackTrace();
            return;
        }

        if( !( new File( fileName ).isFile() ) )
            createConfig();
    }

    public void run()
    {
        if ( !isRunning )
        {
            isRunning = true;
            String line;
            while( isRunning )
            {
                try
                {
                    int msgtype = dis.readInt();

                    switch( msgtype )
                    {
                        case MessageType.REGISTER:
                            dos.writeBoolean( register() );
                            dos.flush();
                            isRunning = false;
                            break;

                        case MessageType.LOGIN:
                            dos.writeBoolean( login() );
                            dos.flush();
                            break;

                        case MessageType.MESSAGE:
                            line = dis.readUTF();
                            ser.sayAll( user, line );
                            break;

                        case MessageType.LOGOUT:
                            try {socket.close();}
                            catch( IOException e1 ) {e1.printStackTrace();}

                            ser.removeThread( this );

                            if( user != null )
                            {
                                try {ser.sayAll( user," left the conversation." );}
                                catch( IOException e1 ) {e1.printStackTrace();}
                            }

                            isRunning = false;
                            break;
                    }
                }
                catch( IOException e )
                {
                    System.err.println( user + " disconnected." );

                    try {socket.close();}
                    catch( IOException e1 ) {e1.printStackTrace();}

                    ser.removeThread( this );

                    try {ser.sayAll( user," left the conversation." );}
                    catch( IOException e1 ) {e1.printStackTrace();}

                    isRunning = false;
                }
            }
        }
    }

    public void say( String splt ) throws IOException
    {
        dos.writeUTF( splt );
        dos.flush();
    }

    public boolean login() throws IOException
    {
        boolean result = false;

        String username = dis.readUTF();
        String password = dis.readUTF();

        InputStream input = null;

        try
        {
            File file = new File( fileName );
            input = new FileInputStream( fileName );

            if( file.isFile() )
            {
                prop.load( input );

                if( prop.containsValue( username ) )
                {
                    String file_password = prop.getProperty( username + "password" );
                    String hashedPassword = hashMD5( password );

                    result = file_password.equals( hashedPassword );
                }
                else
                    result = false;
            }
        }
        catch( IOException ex ) {ex.printStackTrace();}
        finally
        {
            if( input != null )
            {
                try {input.close();}
                catch( IOException e ) {e.printStackTrace();}
            }
        }

        if ( result )
            user = username;

        return result;
    }

    public boolean register() throws IOException
    {
        boolean result = false;
        String usern = dis.readUTF();
        String pass = dis.readUTF();

        FileWriter output = null;
        try
        {
            output = new FileWriter( fileName, true );
            prop.load( new FileInputStream( fileName ) );

            if( !prop.contains( usern ) )
            {
                prop.clear();
                String hashedPassword = hashMD5( pass );

                prop.setProperty( usern, usern );
                prop.setProperty( usern + "password", hashedPassword );

                prop.store( output, null );
                result = true;
            }
            else
                result = false;
        }
        catch( IOException io ) {io.printStackTrace();}
        finally
        {
            if( output != null )
            {
                try {output.close();}
                catch( IOException e ) {e.printStackTrace();}
            }
        }

        ser.removeThread( this );

        return result;
    }

    private void createConfig()
    {
        OutputStream output = null;

        try
        {
            output = new FileOutputStream( fileName );

            String password = "stackoverflow";
            String hashedPassword = hashMD5( password );

            prop.setProperty( "root", "root" );
            prop.setProperty( "rootpassword", hashedPassword );
            prop.store( output, null );
        }
        catch ( IOException io ) {io.printStackTrace();}
        finally
        {
            if ( output != null )
            {
                try {output.close();}
                catch ( IOException e ) {e.printStackTrace();}
            }
        }
    }

    public String hashMD5( String passwordToHash )
    {
        String generatedPassword = null;
        try
        {
            MessageDigest md = MessageDigest.getInstance( "MD5" );
            md.update( passwordToHash.getBytes() );
            byte[] bytes = md.digest();
            StringBuilder sb = new StringBuilder();
            for ( int i = 0; i < bytes.length; i++ )
                sb.append( Integer.toString( ( bytes[i] & 0xff ) + 0x100, 16 ).substring( 1 ) );

            generatedPassword = sb.toString();
        }
        catch ( NoSuchAlgorithmException e ) {e.printStackTrace();}

        return generatedPassword;
    }

    public void close()
    {
        try
        {
            isRunning = false;
            socket.close();
        }
        catch( IOException e ) {e.printStackTrace();}
    }
}