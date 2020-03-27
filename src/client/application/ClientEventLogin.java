package client.application;

import javafx.event.ActionEvent;

public interface ClientEventLogin
{
    void invalidLogin();
    void successfulLogin( ActionEvent event );
    void newAccountCreated();
    void usernameUsed();
    void cannotConnectLogin();
    void cannotConnectRegister();
}
