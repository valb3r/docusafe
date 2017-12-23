package org.adorsys.resource.server.service;

import org.adorsys.resource.server.basetypes.DocumentID;
import org.adorsys.resource.server.exceptions.ServiceException;

/**
 * Created by peter on 23.12.17 at 18:16.
 */
public class S3ContainerInterface {
    byte[] getDocument(DocumentID documentID) {
        throw new ServiceException("NYI");
    }
}
