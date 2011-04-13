package br.com.caelum.vraptor.util.extract;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * 
 * @author <a href="http://www.github.com/jrdalpra">Jos� V. Dal Pr� Junior</a>
 */
public interface Extractable {

	URL getURL() throws IOException;

	InputStream getInputStream() throws IOException;

	String getFilename();

}
