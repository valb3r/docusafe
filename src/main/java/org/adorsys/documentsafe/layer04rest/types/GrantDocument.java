package org.adorsys.documentsafe.layer04rest.types;

import org.adorsys.documentsafe.layer02service.types.complextypes.DocumentDirectory;
import org.adorsys.documentsafe.layer03business.types.AccessType;
import org.adorsys.documentsafe.layer03business.types.UserID;
import org.adorsys.documentsafe.layer03business.types.complex.DocumentDirectoryFQN;
import org.adorsys.documentsafe.layer03business.types.complex.DocumentFQN;

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
