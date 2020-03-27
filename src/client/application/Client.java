package client.application;

import javafx.event.ActionEvent;
import server.MessageType;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Client
{
    private DataOutputStream dos;
    private DataInputStream dis;

    public String username, password;
    public Socket socket;

    public boolean isRunning = false;
    private Thread listen;

    private ClientEventLogin eventLogin;
    private ClientEventChat eventChat;

    public Client( String u, String p )
    {
        username = u;
        password = p;
    }

    public void setEventLogin( ClientEventLogin ev )
    {
        eventLogin = ev;
    }

    public void setEventChat( ClientEventChat ev )
    {
        eventChat = ev;
    }

    public void listen()
    {
        listen = new Thread( "Listen" )
        {
            public void run()
            {
                if( !isRunning )
                {
                    isRunning = true;
                    sendToAll( "<html><font color=\"orange\" size=\"2\"><b> has joined to conversation.</b></font><br/></html>" );

                    while( isRunning )
                    {
                        try
                        {
                            String msg = dis.readUTF();
                            eventChat.messageReceived( msg );
                        }
                        catch( IOException e )
                        {
                            eventChat.disconnectedFromTheServer();
                            disconnect();
                            isRunning = false;
                        }
                    }
                }
            }
        };
        listen.setDaemon( true );
        listen.start();
    }

    public void sendToAll( String msg )
    {
        try
        {
            dos.writeInt( MessageType.MESSAGE );
            dos.writeUTF( msg );
        }
        catch( IOException e ) {e.printStackTrace();}
    }

    public boolean connect( String ip, int port )
    {
        boolean result = false;
        try
        {
            socket = new Socket( ip, port );
            dos = new DataOutputStream( socket.getOutputStream() );
            dis = new DataInputStream( socket.getInputStream() );
            result = true;
        }
        catch( IOException e )
        {
            eventLogin.cannotConnectLogin();
            eventLogin.cannotConnectRegister();
            return false;
        }

        return result;
    }

    public void disconnect()
    {
        if( !socket.isClosed() )
        {
            try
            {
                dos.writeInt( MessageType.LOGOUT );
                socket.close();
                dos.close();
                dis.close();
            }
            catch( IOException e ) {e.printStackTrace();}
        }
    }

    public void login( ActionEvent ev )
    {
        try
        {
            dos.writeInt( MessageType.LOGIN );
            dos.writeUTF( username );
            dos.writeUTF( password );
            if( dis.readBoolean() )
                eventLogin.successfulLogin( ev );
            else
                eventLogin.invalidLogin();
        }
        catch( IOException e ) {e.printStackTrace();}
    }

    public void register()
    {
        try
        {
            dos.writeInt( MessageType.REGISTER );
            dos.writeUTF( username );
            dos.writeUTF( password );
            if( dis.readBoolean() )
                eventLogin.newAccountCreated();
            else
                eventLogin.usernameUsed();
        }
        catch( IOException e ) {e.printStackTrace();}
    }
}
