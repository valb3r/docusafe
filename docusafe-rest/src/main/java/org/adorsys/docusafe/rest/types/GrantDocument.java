package org.adorsys.docusafe.rest.types;

import org.adorsys.docusafe.service.types.AccessType;
import org.adorsys.docusafe.business.types.UserID;
import org.adorsys.docusafe.business.types.complex.DocumentDirectoryFQN;

/**
 * Created by peter on 29.01.18 at 20:17.
 */
public class GrantDocument {
    private DocumentDirectoryFQN documentDirectoryFQN;
    private UserID receivingUser;
    private AccessType accessType;

    public GrantDocument() {
    }

    public GrantDocument(DocumentDirectoryFQN documentDirectoryFQN, UserID receivingUser, AccessType accessType) {
        this.documentDirectoryFQN = documentDirectoryFQN;
        this.receivingUser = receivingUser;
        this.accessType = accessType;
    }

    public DocumentDirectoryFQN getDocumentDirectoryFQN() {
        return documentDirectoryFQN;
    }

    public UserID getReceivingUser() {
        return receivingUser;
    }

    public AccessType getAccessType() {
        return accessType;
    }
}
