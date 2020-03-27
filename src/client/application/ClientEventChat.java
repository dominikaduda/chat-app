package client.application;

public interface ClientEventChat
{
    void messageReceived( String msg );
    void disconnectedFromTheServer();
}
