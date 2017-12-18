# Document Safe

The document safe is an abstraction layer used to securely manage (store, share and retrieve) user document on the top of a blob storage.

# Object Model

## DataEnvelope

Each document is stored in the storage encrypted. Each encrypted object shall have following properties:

Envelope format keeping encrypted bytes closed to encryption details. A provider implementation concept can allow the selection of different types of envelopes like 
- JWE (JSON Web Encryption, RFC 7516), 
- CMS (Cryptographic Message Syntax, RFC 5652).

The initial version will use JWE as envelop.

## DataKey 

The data key is a symmetric cryptographic key used to encrypt the data. 

The data key used for the symmetric encryption can be used for:
- a single document or 
- the whole directory, if the underlying storage support the concept of directory
- a whole bucket   

We can annotate an object (directory, bucket) with the property "key-mode=[bucket, dir, file]". This annotation will decide on whether to use the same key for a bucket, a directory or a file.  

## DataKeyGuard

The data key guard is an encrypted version of the message key. The data key shall at least display following information:
- The key-id : the id of the data key
- The file-id : the id of the data encrypted

### User Keys Store

Each user has a repository of keys. These are:
- Key pairs (Public/Private) : used for asymmetric encryption of data key
- Secret keys : used for symmetric encryption of data key

Each user has a user-keys files used to store those keys. For the java implementation, we will use the Java key store format.

When a user is logged in:
- secret keys of the user can be read and used to store user's documents. 

When a user want to share a document with another user:
- a public key of the receiving user is used to encrypt the data key.

For the java implementation version, we can use the java key store format to store those information. Following property can be used to implement the key storage and document sharing:

- the bucket of the user is named using the user-id#bucket
- the key store of the user can be stored accessible under the name user-id#keystore
- the storepass is also carries the name user-id
- the keypass is a secret stored by the identity provider and only accessible when the user is logged in

# Functions

DECRYPTION
url = authenticateAndAuthorize(user, password, messageID)

userID = getUserIDOf(url)
messageID = getMessageIDOf(url)

messageKeyGuard = findMessageKeyGuard(userID,messageID)
messageEnvelope = findMessageEnvelope(messageID)
privateKeyGuard = findPrivateKeyGuard(userdID)

usersPrivateKey = asymmetricDecrypt(privateKeyGuard, usersDataKey)
messageKey = asymmetricDecrypt(messageKeyGuard, usersPrivateKey)
message = symmetricDecrypt(messageEnvelopy, messageKey)
