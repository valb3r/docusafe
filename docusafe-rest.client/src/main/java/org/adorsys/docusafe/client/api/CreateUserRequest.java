package org.adorsys.docusafe.client.api;

/**
 * Created by peter on 27.02.18 at 09:46.
 */
public class CreateUserRequest {
    public String userID;
    public String readKeyPassword;

    public CreateUserRequest(String userID, String readKeyPassword) {
        this.userID = userID;
        this.readKeyPassword = readKeyPassword;
    }
}
