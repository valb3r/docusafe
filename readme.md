# Document Safe

The document safe is an abstraction layer used to securely manage (store, share and retrieve) user document on the top of a blob storage.

## Layer
Docusafe ist eine dienstbasierte Bibliothek. Dabei sind die Dienste so in Module gepackt, dass der Benutzer immer nur auf die oberste Schicht zugreiffen sollte. Die Schichten werden nun in der Reihenfolge gelistet, wie sie aufeinander aufbauen:
* layer 0: **docusafe-service**
    
    Basiert direkt auf dem projekt cryptoutils, und da vorrangig auf dem Modul encjobject für die Verschlüsselungslogik und ExtendedStoreConnection für das Lesen und Speichern von Objekten.
Stellt Dienste für Buckets, DocumentGuards, Document und Keystores bereit.
* layer 1: **docusafe-business**

    Basiert auf docusafe-service und stellt die Hauptfunktionalität dar. Das Anlegen von Benutzern, das Speichern und Lesen von Objekten unterhalb dieser Benutzer.
* layer 2: **docusafe-transactional**

    Basiert auf docusafe-business. Ermöglicht, eine Menge von Documenten transaktional zu speichern. D.h. es werden wirklich alle Documente gespeichert, oder keines. 
* layer 3: **docusafe-cached-transactional**

    Eine Cache-Schicht, die Mehrfachaufrufe beim Lesen oder Schreiben einfach unterbindet bzw. die Daten direkt aus dem Speicher liest, statt von der ExtendedStoreConnection.
* layer 4a: **docusafe-rest**

    Dieses Layer hat nur Testfunktionalität. Es stellt die Bibliotheksdienste der layer 0-3 in einem Server zur Verfügung. Dabei können die Dienste über REST angesprochen werden.
* layer 4b: **docusafe-rest.client** 
    
    Mit Curl lassen sich nur Requests absetzen, die ganze Datenblöcke auf einmal verschicken. Der RestClient bietet die Möglichkeit, auch Streambasiert zu verschicken bzw. zu empfangen.
 
## release build

Um ein release zu erstellen, sind folgende Schritte notwendig:

    git checkout develop
    git pull
    git submodule init
    git submodule update
    ./release-scripts/release.sh 0.18.8 0.18.9
    git push --atomic origin master develop --follow-tags

Wenn das Script beim release mit folgendem Fehler terminiert

    ! [rejected]        master -> master (non-fast-forward)
    error: failed to push some refs to 'https://github.com/adorsys/cryptoutils'
    hint: Updates were rejected because the tip of your current branch is behind

, dann liegt das daran, das master erst ausgecheckt werden muss:

    git checkout master
    .release-scripts/release.sh .....
    
    