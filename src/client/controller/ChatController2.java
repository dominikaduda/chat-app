package client.controller;

import client.application.Client;
import client.application.ClientEventChat;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class ChatController2 {

    @FXML
    private TextArea ShowMessageTextArea;
    @FXML
    private TextField InputMessageTextField;
    @FXML
    private Button btSend;
    @FXML
    private Button btDisconnect;

    private Client client;

    public void init(Client c){
        client=c;

        client.setEventChat(new ClientEventChat() {
            @Override
            public void messageReceived(String msg) {

                Platform.runLater( () ->
                {
                    String tmp=ShowMessageTextArea.getText();
                    ShowMessageTextArea.setText(tmp+"\n"+msg);
                } );
            }

            @Override
            public void disconnectedFromTheServer() {
                String tmp=ShowMessageTextArea.getText();
                Platform.runLater( () -> ShowMessageTextArea.setText(tmp+"\n"+"DISCONNECTED FROM THE SERVER!") );
            }
        });
        client.listen();
    }

    public void sendMessage()
    {
        if( !client.socket.isClosed() )
        {
            String message = InputMessageTextField.getText();
            if( !message.isEmpty() )
                client.sendToAll( "<" + client.username + "> " + message );
        }
        else{
            String tmp=ShowMessageTextArea.getText();
            ShowMessageTextArea.setText(tmp+"\n"+"YOU ARE NOT CONNECTED TO THE SERVER!");
        }

        InputMessageTextField.setText( "" );
    }

    public void disconnectFromServer()
    {
        client.disconnect();
    }

}
