package br.com.caelum.vraptor.util.extract.java;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import br.com.caelum.vraptor.util.extract.Extractable;

public class FileExtratable implements Extractable {
	private final File	file;

	public FileExtratable(File file) {
		super();
		this.file = file;
	}

	@SuppressWarnings("deprecation")
	@Override
	public URL getURL() throws IOException {
		return file.toURL();
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return new FileInputStream(file);
	}

	@Override
	public String getFilename() {
		return file.getName();
	}

}
