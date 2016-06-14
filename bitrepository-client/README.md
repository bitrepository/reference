
# Bitrepository reference client libraries

The bitrepository.org client module contains clients for the various operations available through the protocol. 
The clients are divided into two package heirakies, 'access' and 'modify'. The clients found under the 'access' package is for read-only operations, and clients found under 'modify' is for operations where writing is needed. 

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

The clients operate on a asynchronious event driven basis, this means that when invoking a client an event handler needs to be provided as that is where information and results from the operation will be given. 

## Usage of clients

The various Bitrepository.org reference maven artifacts can be found on [sbforge nexus](https://sbforge.org/nexus/content/groups/public).

The specific maven dependency information for the client module can be seen in the following snippit (*NB: the version information should be changed to an actual version*)

```xml
<dependency>
  <groupId>org.bitrepository.reference</groupId>
  <artifactId>bitrepository-client</artifactId>
  <version>VERSION</version>
</dependency>
```

Besides the dependency information, usage of the client requires the following:
  * RepositorySettings.xml - Settings file describing the repository
  * ReferenceSettings.xml - Settings file with client specific settings
  * Client certificate - X509 certificate and key for the client, unencrypted PEM formatted

The minimal ReferenceSettings.xml needed for usage of a client looks like:
```xml
<ReferenceSettings xmlns="http://bitrepository.org/settings/ReferenceSettings.xsd">
  <ClientSettings>
    <MediatorCleanupInterval>1000</MediatorCleanupInterval>
    <ConversationTimeout>3600000</ConversationTimeout>
  </ClientSettings>
</ReferenceSettings>
```

### Creating a client

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
SecurityManager securityManager = new BasicSecurityManager(settings.getRepositorySettings(), certificateFile,
                authenticator, signer, authorizer, permissionStore, settings.getComponentID());

PutFileClient putClient = ModifyComponentFactory.getInstance().retrievePutClient(settings, securityManager, 
                settings.getComponentID());
```



### Closing after finishing
When bitrepository.org clients are no longer needed the messagebus connection should be closed.
 
Note should be taken that if the messagebus connection is closed while other clients in the same JVM is in use, their connection will also be closed. I.e. only close the messagebus connection when all use of clients are finished. 

```java
MessageBus messageBus = MessageBusManager.getMessageBus();
if (messageBus != null) {
    messageBus.close();
}
```

