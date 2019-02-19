# Document Safe

The document safe is a framework used to securely manage (store, share and retrieve) user documents on the top of a blob storage.

## Layer
The docusafe is a layer based software. Each layer depends on its underlying layer. The underlying layer does not have any dependencies to the layers above. 
The layers will be explained from bottom to top. 
* layer 0: **docusafe-service**

    This layer has dependencies to the project cryptutils project (https://github.com/adorsys/cryptoutils). This framework contains the keystore handling, the en- and decryption and the StorecConnection. In the service layer these funktionalties are joined, so that a document can be stored encrypted with a document guard.
This layer must not be used directly. Its services should be regarded as internal/private.
 
* layer 1: **docusafe-business**

    This layer provides the main functionality of the document safe framework. Users can be creaated. The user can create and store documents. The documents can be saved as blobs or streams. 
The interface of this layer is***DocumentSafeService.*** Just to give you an idea how easy it is to store and read a document here a simple tiny main.

```java
    package org.adorsys.docusafe.business;
    
    import org.adorsys.cryptoutils.exceptions.BaseException;
    import org.adorsys.cryptoutils.storeconnectionfactory.ExtendedStoreConnectionFactory;
    import org.adorsys.docusafe.business.impl.DocumentSafeServiceImpl;
    import org.adorsys.docusafe.business.impl.WithCache;
    import org.adorsys.docusafe.business.types.UserID;
    import org.adorsys.docusafe.business.types.complex.DSDocument;
    import org.adorsys.docusafe.business.types.complex.DocumentFQN;
    import org.adorsys.docusafe.business.types.complex.UserIDAuth;
    import org.adorsys.docusafe.service.types.DocumentContent;
    import org.adorsys.encobject.domain.ReadKeyPassword;
    
    import java.util.Arrays;
    
    /**
     * Created by peter on 19.02.19 09:45.
     */
    public class ReadMeMDFileTestCode {
        public static void main(String[] args) {
            // create service
            DocumentSafeService documentSafeService = new DocumentSafeServiceImpl(WithCache.TRUE, ExtendedStoreConnectionFactory.get());
    
            // create user
            UserIDAuth userIDAuth = new UserIDAuth(new UserID("peter"), new ReadKeyPassword("passwordOfPeter"));
            documentSafeService.createUser(userIDAuth);
    
            // create document
            DocumentFQN documentFQN = new DocumentFQN("first/document.txt");
            DocumentContent documentContent = new DocumentContent(("programming is the mirror of your mind").getBytes());
            DSDocument dsDocument = new DSDocument(documentFQN, documentContent, null);
            documentSafeService.storeDocument(userIDAuth, dsDocument);
    
            // read the document again
            DSDocument dsDocumentRead = documentSafeService.readDocument(userIDAuth, documentFQN);
            if (Arrays.equals(dsDocument.getDocumentContent().getValue(), dsDocumentRead.getDocumentContent().getValue()) == true) {
                System.out.println("read the following content from " + documentFQN + ":" + new String(dsDocumentRead.getDocumentContent().getValue()));
            } else {
                throw new BaseException("This will never happen :-)");
            }
        }
    }

```
* layer 2: **docusafe-transactional**

    This layer provides the functionality to group actions that will be commited all together, or none of them. For example if you store two documents and you get an exception for what every reason after storing the first but before storing the second document. The exception will raise and prevent the mandatory endTransaction(). So none of the documents will be stored at all. This layer gives you a handle to store your documents all the time consistently matching.
This layer must not be used with layer1 at the same timel Documents stored with the services of layer1 can not be seen in layer2. If you use this layer, do not use services of layer1.
The interface of this layer is***TransactionalDocumentSafeService.***
 
* layer 3: **docusafe-cached-transactional**

    This is a thin layer based upon the previous which avoid unneccesary payload when the same document is writting or read more than one time. 
The interface of this layer is***CachedTransactionalDocumentSafeService.***
    
* layer 4: **docusafe-spring**
    
    the layers 0-3 have to used as direct services. this means, the classes have to be instantiated with new x-serviceimpl(). specially the layer 2 and 3 need a memorycontext object to store temporary information in memory rather than the filesystem. though a simple implemetation of the memorycontext is provied, most server based architectures run with spring or ee context. as the name of the layer immplies, this layer can be used, to get the needed services injected for free.
this all can be achieved by simply using the @UseDocusafeSpringConfiguration annotation. Then the ExtendedStoreConnection or the CachedTransactionalDocumentSafeService can be injected as autowired beans. Or, for more detailed access to the servicec, the
SpringExtendedStoreConnectionFactory or SpringCachedTransactionalDocusafeServiceFactory can be autowired.
 
## REST
If you are missing a REST layer, this is not provided by this framework. But in https://github.com/adorsys/docusafe.tests you can find a REST layer for the docusafe framework.
