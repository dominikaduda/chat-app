package client.controller;

import client.application.Client;
import client.application.ClientEventLogin;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import java.io.*;
import javafx.scene.Scene;

public class LoginController
{
    @FXML private TextField fieldUsername, fieldNewUsername, fieldServerIP, fieldServerPort;
    @FXML private PasswordField fieldPassword, fieldNewPassword;
    @FXML private Label labelError, labelNewError;
    private Client client;
    private ClientEventLogin cel = new ClientEventLogin()
    {
        @Override
        public void invalidLogin()
        {
            labelError.setText( "Invalid username or password!" );
            client.disconnect();
        }

        @Override
        public void successfulLogin( ActionEvent event )
        {
            ( ( Node ) event.getSource() ).getScene().getWindow().hide();
            FXMLLoader fxmlLoader = new FXMLLoader( getClass().getResource( "/client/view/ChatView2.fxml" ) );

            Parent parent = null;
            try {parent = fxmlLoader.load();}
            catch( IOException e ) {e.printStackTrace();}
            ChatController2 controller = fxmlLoader.getController();
            controller.init( client );

            Stage stage = new Stage();
            Scene scene = new Scene( parent );

            stage.setScene( scene );
            stage.setTitle( "Chat" );
            stage.setMinWidth( 600 );
            stage.setMinHeight( 400 );
            stage.show();
        }

        @Override
        public void newAccountCreated()
        {
            labelNewError.setTextFill( Paint.valueOf( "#00d815" ) );
            labelNewError.setText( "New account has been created!" );
        }

        @Override
        public void usernameUsed()
        {
            labelNewError.setTextFill( Paint.valueOf( "#da0000" ) );
            labelNewError.setText( "This username is already used!" );
        }

        @Override
        public void cannotConnectLogin()
        {
            labelError.setText( "Cannot connect to server!" );
        }

        @Override
        public void cannotConnectRegister()
        {
            labelNewError.setTextFill( Paint.valueOf( "#da0000" ) );
            labelNewError.setText( "Cannot connect to server!" );
        }
    };

    @FXML
    public void btnLogInAction( ActionEvent event )
    {
        String username = fieldUsername.getText();
        String password = fieldPassword.getText();
        String ip = fieldServerIP.getText();
        int port = Integer.parseInt( fieldServerPort.getText() );

        client = new Client( username, password );
        client.setEventLogin( cel );
        if( client.connect( ip, port ) )
            client.login( event );
    }

    @FXML
    public void btnSaveAction()
    {
        String username = fieldNewUsername.getText();
        String password = fieldNewPassword.getText();
        String ip = fieldServerIP.getText();
        int port = Integer.parseInt( fieldServerPort.getText() );

        client = new Client( username, password );
        client.setEventLogin( cel );
        if( client.connect( ip, port ) )
        {
            client.register();
            client.disconnect();
        }

        fieldNewUsername.clear();
        fieldNewPassword.clear();
    }

    @FXML
    public void btnClearAction()
    {
        fieldNewUsername.clear();
        fieldNewPassword.clear();
    }
}
