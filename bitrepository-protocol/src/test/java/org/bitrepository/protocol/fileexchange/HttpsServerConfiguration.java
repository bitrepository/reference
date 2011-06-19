package org.bitrepository.protocol.fileexchange;

public class HttpsServerConfiguration extends HttpServerConfiguration {
	protected String fileExchangeClass;
    protected String httpsKeystorePath;
    protected String httpsKeyStorePassword;
    protected String httpsCertificatePath;
    protected String httpsCertificateAlias;
    public String getFileExchangeClass() {
		return fileExchangeClass;
	}
	public void setFileExchangeClass(String fileExchangeClass) {
		this.fileExchangeClass = fileExchangeClass;
	}
	public String getHttpsKeystorePath() {
		return httpsKeystorePath;
	}
	public void setHttpsKeystorePath(String httpsKeystorePath) {
		this.httpsKeystorePath = httpsKeystorePath;
	}
	public String getHttpsKeyStorePassword() {
		return httpsKeyStorePassword;
	}
	public void setHttpsKeyStorePassword(String httpsKeyStorePassword) {
		this.httpsKeyStorePassword = httpsKeyStorePassword;
	}
	public String getHttpsCertificatePath() {
		return httpsCertificatePath;
	}
	public void setHttpsCertificatePath(String httpsCertificatePath) {
		this.httpsCertificatePath = httpsCertificatePath;
	}
	public String getHttpsCertificateAlias() {
		return httpsCertificateAlias;
	}
	public void setHttpsCertificateAlias(String httpsCertificateAlias) {
		this.httpsCertificateAlias = httpsCertificateAlias;
	}
}
