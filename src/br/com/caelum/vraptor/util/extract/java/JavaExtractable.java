package br.com.caelum.vraptor.util.extract.java;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import br.com.caelum.vraptor.util.extract.Extractable;
import br.com.caelum.vraptor.util.extract.Extractor;

public class JavaExtractable implements Extractable {

	private final URL url;

	public JavaExtractable(URL url) {
		this.url = url;
	}

	@Override
	public URL getURL() throws IOException {
		return this.url;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return this.url.openStream();
	}

	@Override
	public String getFilename() {
		return isJarEntry() ? getJarEntryName() : this.url.getFile();
	}

	protected String getJarEntryName() {
		String name = url.getFile();
		return name.substring(name.lastIndexOf(Extractor.SEPARATOR) + 1);
	}

	protected boolean isJarEntry() {
		String name = url.getFile();
		return !name.isEmpty() && name.contains(".jar") && name.contains("!/");
	}

}
