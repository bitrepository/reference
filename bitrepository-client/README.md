
# Bitrepository reference client libraries

The bitrepository.org client module contains java client libraries for interacting with a bitrepository through the bitrepository.org protocol as described [here](https://sbforge.org/display/BITMAG/Message+flow). 
The clients are divided into two package hierarchies, 'access' and 'modify'. The clients found under the 'access' package is for read-only operations, and clients found under 'modify' is for write operations. Descriptions of the various operations are available [here](https://sbforge.org/display/BITMAG/Operations+descriptions)

The 'access' package contains the following operations:
  * GetAuditTrails
  * GetChecksums
  * GetFile
  * GetFileIds
  * GetStatus

The 'modify' package contains the following operations:
  * DeleteFile
  * PutFile
  * ReplaceFile

The clients operate on a asynchronious event-driven basis, this means that when invoking a client an event handler needs to be provided as that is where information and results from the operation will be given. 

## Usage of clients

The various Bitrepository.org reference maven artifacts can be found on [sbforge nexus](https://sbforge.org/nexus/content/groups/public).
```xml
<repository>
  <id>sbforge-nexus</id>
  <url>https://sbforge.org/nexus/content/groups/public</url>
  <releases><enabled>true</enabled></releases>
  <snapshots><enabled>true</enabled></snapshots>
</repository>
```


The specific maven dependency information for the client module can be seen in the following snippet (*NB: the version information should be changed to an actual version*)

```xml
<dependency>
  <groupId>org.bitrepository.reference</groupId>
  <artifactId>bitrepository-client</artifactId>
  <version>VERSION</version>
</dependency>
```

Besides the dependency information, usage of the client requires the following:
  * RepositorySettings.xml - Settings file describing the repository
  * ReferenceSettings.xml - Settings file with reference client specific settings
  * Client certificate - X509 certificate and key for the client, unencrypted PEM format

The two files RepositorySettings.xml and ReferenceSettings.xml are expected to be located in the same directory

The minimal ReferenceSettings.xml needed for usage of a client looks like:
```xml
<ReferenceSettings xmlns="http://bitrepository.org/settings/ReferenceSettings.xsd">
  <ClientSettings>
    <MediatorCleanupInterval>1000</MediatorCleanupInterval>
    <ConversationTimeout>3600000</ConversationTimeout>
  </ClientSettings>
</ReferenceSettings>
```
Schemas for the settings files can be found:
  * [RepositorySettings.xsd](https://github.com/bitrepository/repository-settings/blob/master/repository-settings-xsd/src/main/resources/xsd/RepositorySettings.xsd)
  * [ReferenceSettings.xsd](https://github.com/bitrepository/reference/blob/master/bitrepository-reference-settings/src/main/resources/xsd/ReferenceSettings.xsd)


### Creating a client
To create a client a few things needs to be done:
  * Load settings
  * Create the nescesary helper classes for cryptography
  * Obtain the client itself

The following code demonstrates how this can be done

```java
    String pathToSettingDir = "path/to/settings/directory";
    String clientID = "myClientID";
    String certificateFile = "path/to/certificate.pem";
    
    SettingsProvider settingsLoader = new SettingsProvider(
                    new XMLFileSettingsLoader(pathToSettingDir), clientID);
    Settings settings = settingsLoader.getSettings();
   
    PermissionStore permissionStore = new PermissionStore();
    MessageAuthenticator authenticator = new BasicMessageAuthenticator(permissionStore);
    MessageSigner signer = new BasicMessageSigner();
    OperationAuthorizor authorizer = new BasicOperationAuthorizor(permissionStore);
    SecurityManager securityManager = new BasicSecurityManager(
                    settings.getRepositorySettings(), certificateFile, authenticator, 
                    signer, authorizer, permissionStore, settings.getComponentID());
    
    PutFileClient putClient = ModifyComponentFactory.getInstance().retrievePutClient(
                    settings, securityManager, settings.getComponentID());
```

### Using a client
The client operates on non-blocking asynchronious basis, and delivers information about what is happening to the EventHandler provided when starting an operation. This means that when using the various clients, the code should wait for the operation to finish or fail.  

```java
   
    private class MyEventHandler implements EventHandler {
        @Override
        public void handleEvent(OperationEvent event) {
            // Code to handle event
        }
    }

    String collectionID = "test-collection";
    URL fileURL = new URL("https://file-exchange01/myFile");
    String fileID = "myFileID";
    long fileSize = 1000000000L;
    ChecksumDataForFileTYPE checksumData = getChecksumDataForFile();
    ChecksumSpecTYPE checksumRequest = null;
    MyEventHandler eventHandler = new MyEventHandler();
    String auditInformation = "ingesting my file";
    
    putClient.putFile(collectionID, fileURL, fileID, fileSize, checksumData, 
                      checksumRequest, eventHandler, auditInformation);

    // Code to wait for the put operation to finish
```
For inspiration of the use of the bitrepository client libraries have a look at the following projects:
  * The client modules commandline client (in this repository: bitrepository-client/src/main/java/org/bitrepository/commandline/)
  * The Danish State and University library's [youseebitrepositoryingester](https://github.com/statsbiblioteket/youseebitrepositoryingester/) and [newspaper-bitrepository-ingester](https://github.com/statsbiblioteket/newspaper-bitrepository-ingester)

### Closing after finishing
As part of requesting a client from the client factory, a shared message bus connection is created, this should be closed when no more bitrepository.org clients are needed.
 
Note should be taken that if the message bus connection is closed while other clients in the same JVM is in use, their connection will also be closed. I.e. only close the message bus connection when all use of clients are finished. 

```java
MessageBus messageBus = MessageBusManager.getMessageBus();
if (messageBus != null) {
    messageBus.close();
}
```

### Putting it all together
The below code is a full example of the various parts above. The code will compile but not run as it needs to be adapted to actual settings files etc. 

```java
import java.net.MalformedURLException;
import java.net.URL;

import javax.jms.JMSException;

import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.client.eventhandler.OperationEvent;
import org.bitrepository.client.eventhandler.OperationEvent.OperationEventType;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.SettingsProvider;
import org.bitrepository.common.settings.XMLFileSettingsLoader;
import org.bitrepository.modify.ModifyComponentFactory;
import org.bitrepository.modify.putfile.PutFileClient;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.protocol.messagebus.MessageBusManager;
import org.bitrepository.protocol.security.BasicMessageAuthenticator;
import org.bitrepository.protocol.security.BasicMessageSigner;
import org.bitrepository.protocol.security.BasicOperationAuthorizer;
import org.bitrepository.protocol.security.SecurityManager;
import org.bitrepository.protocol.security.BasicSecurityManager;
import org.bitrepository.protocol.security.MessageAuthenticator;
import org.bitrepository.protocol.security.MessageSigner;
import org.bitrepository.protocol.security.OperationAuthorizer;
import org.bitrepository.protocol.security.PermissionStore;

public class BitrepositoryClientExample {

    private class MyEventHandler implements EventHandler {
        
        private final Object finishLock = new Object();
        private boolean finished = false;
        private OperationEventType finishEventType;
        
        @Override
        public void handleEvent(OperationEvent event) {
            switch(event.getEventType()) {
                case COMPLETE:
                    finishEventType = OperationEventType.COMPLETE;
                    finish();
                    break;
                case FAILED:
                    finishEventType = OperationEventType.FAILED;
                    finish();
                    break;
                default:
                    break;
            }   
        }
        
        private void finish() {
            synchronized(finishLock) {
                finished = true;
                finishLock.notifyAll();
            }
        }
        
        public OperationEventType waitForFinish() throws InterruptedException {
            synchronized (finishLock) {
                if(finished == false) {
                    finishLock.wait();
                }
                return finishEventType;
            }
        }
        
    }
    
    private ChecksumDataForFileTYPE getChecksumDataForFile() {
        ChecksumDataForFileTYPE checksumData = new ChecksumDataForFileTYPE();
        // Beware, checksumData is empty, should be filled in
        return checksumData;
    }
    
    public void example() throws MalformedURLException, JMSException, InterruptedException {
        
        // Creating the client    
        String pathToSettingDir = "path/to/settings/directory";
        String clientID = "myClientID";
        String certificateFile = "path/to/certificate.pem";
    
        SettingsProvider settingsLoader = new SettingsProvider(
                        new XMLFileSettingsLoader(pathToSettingDir), clientID);
        Settings settings = settingsLoader.getSettings();
    
        PermissionStore permissionStore = new PermissionStore();
        MessageAuthenticator authenticator = new BasicMessageAuthenticator(permissionStore);
        MessageSigner signer = new BasicMessageSigner();
        OperationAuthorizer authorizer = new BasicOperationAuthorizer(permissionStore);
        SecurityManager securityManager = new BasicSecurityManager(
                        settings.getRepositorySettings(), certificateFile, authenticator, 
                        signer, authorizer, permissionStore, settings.getComponentID());
    
        PutFileClient putClient = ModifyComponentFactory.getInstance().retrievePutClient(
                        settings, securityManager, settings.getComponentID());
        
        
        //Using the client
        String collectionID = "test-collection";
        URL fileURL = new URL("https://file-exchange01/myFile");
        String fileID = "myFileID";
        long fileSize = 1000000000L;
        ChecksumDataForFileTYPE checksumData = getChecksumDataForFile();
        ChecksumSpecTYPE checksumRequest = null;
        MyEventHandler eventHandler = new MyEventHandler();
        String auditInformation = "ingesting my file";
    
        putClient.putFile(collectionID, fileURL, fileID, fileSize, checksumData, 
                          checksumRequest, eventHandler, auditInformation);
    
        OperationEventType finishType = eventHandler.waitForFinish();
        // Add handling for finishType
    
        
        // Closing down after use
        MessageBus messageBus = MessageBusManager.getMessageBus();
        if (messageBus != null) {
            messageBus.close();
        }
    
    }
}
``` 
