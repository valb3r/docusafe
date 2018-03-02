package org.adorsys.docusafe.rest.types;

import org.adorsys.docusafe.business.types.complex.DocumentFQN;

/**
 * Created by peter on 23.01.18 at 20:08.
 */
public class CreateLinkTupel {
    private DocumentFQN source;
    private DocumentFQN destination;

    public CreateLinkTupel() {
    }

    public CreateLinkTupel(DocumentFQN source, DocumentFQN destination) {
        this.source = source;
        this.destination = destination;
    }

    public DocumentFQN getSource() {
        return source;
    }

    public DocumentFQN getDestination() {
        return destination;
    }
}
