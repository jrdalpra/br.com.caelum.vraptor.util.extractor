package br.com.caelum.vraptor.util.extract;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.StringTokenizer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;

import org.apache.commons.io.IOUtils;

import br.com.caelum.vraptor.ioc.ApplicationScoped;
import br.com.caelum.vraptor.ioc.Component;
import br.com.caelum.vraptor.util.extract.java.FileExtratable;
import br.com.caelum.vraptor.util.extract.java.JavaExtractable;

/**
 * 
 * @author <a href="http://www.github.com/jrdalpra">José V. Dal Prá Junior</a>
 * 
 */
@Component
@ApplicationScoped
public class Extractor {

	public static String			META_INF	= "META-INF";

	public static String			DESTINATION	= "/WEB-INF/jsp";

	public static String			SEPARATOR	= "/";

	private final ServletContext	context;

	private List<Extractable>		extractables;

	public Extractor(ServletContext context) {
		this.context = context;
		extractables = new ArrayList<Extractable>();
	}

	@PostConstruct
	public void extract() throws Exception {
		prepare();
		List<Extractable> found = find();
		for (Extractable extractable : found) {
			prepare(extractable);
			extract(extractable);
		}
	}

	private void prepare() {
		createDirectory(destinationDirectory());
	}

	protected void prepare(Extractable extractable) throws IOException {
		createDirectoryOf(extractable);
	}

	private void createDirectoryOf(Extractable extractable) throws IOException {
		StringTokenizer paths = new StringTokenizer(dirOf(extractable), SEPARATOR);
		String actual = "";
		String path = "";
		while (paths.hasMoreTokens()) {
			actual = paths.nextToken();
			path = path + (path.isEmpty() ? "" : SEPARATOR) + actual;
			createDirectory(destinationDirectory() + path);
		}
	}

	private void createDirectory(String dir) {
		File directory = new File(dir);
		if (!directory.exists()) {
			directory.mkdir();
		}
	}

	protected void extract(Extractable extractable) throws IOException {
		String destinationFile = destinationFileOf(extractable).replace(SEPARATOR, File.separator);
		FileOutputStream output = new FileOutputStream(destinationFile);
		IOUtils.copy(extractable.getInputStream(), output);
		IOUtils.closeQuietly(output);
	}

	protected String destinationFileOf(Extractable extractable) throws IOException {
		return destinationDirectory() + dirOf(extractable) + SEPARATOR + extractable.getFilename();
	}

	protected String dirOf(Extractable extractable) throws IOException {
		String url = extractable.getURL().toString();
		String dir = url.substring(url.lastIndexOf(META_INF) + META_INF.length() + 1);
		dir = dir.substring(0, dir.lastIndexOf(SEPARATOR));
		return dir;
	}

	private String destinationDirectory() {
		return getDestinationDirectory().endsWith(SEPARATOR) ? getDestinationDirectory() : getDestinationDirectory().concat(SEPARATOR);
	}

	protected String getDestinationDirectory() {
		return context.getRealPath(DESTINATION) + SEPARATOR;
	}

	protected List<Extractable> find() throws IOException, URISyntaxException {
		Enumeration<URL> resources = loader().getResources(META_INF);
		if (resources != null) {
			while (resources.hasMoreElements()) {
				verifyAndAddToExtractables(resources.nextElement());
			}
		}
		return extractables;
	}

	private void verifyAndAddToExtractables(URL resource) throws IOException, URISyntaxException {
		URLConnection connection = resource.openConnection();
		if (connection instanceof JarURLConnection) {
			verify(((JarURLConnection) connection).getJarFile());
		} else {
			verify(resource);
		}

	}

	private void verify(URL resource) throws MalformedURLException, URISyntaxException {
		File dir = new File(resource.getFile());
		verify(dir);
	}

	private void append(File file) throws MalformedURLException, URISyntaxException {
		extractables.add(new FileExtratable(file));
	}

	private void verify(File dir) throws MalformedURLException, URISyntaxException {
		if (dir != null && dir.isDirectory()) {
			List<File> files = Arrays.asList(dir.listFiles());
			for (File file : files) {
				if (file.isDirectory()) {
					verify(file);
				} else if (matches(file.getAbsolutePath())) {
					append(file);
				}
			}
		}
	}

	private void verify(JarFile jar) {
		Enumeration<JarEntry> entries = jar.entries();
		JarEntry entry = null;
		while (entries.hasMoreElements()) {
			entry = entries.nextElement();
			if (matches(entry)) {
				extractables.add(new JavaExtractable(loader().getResource(entry.getName())));
			}
		}
	}

	protected ClassLoader loader() {
		return Thread.currentThread().getContextClassLoader();
	}

	protected boolean matches(String entry) {
		return entry != null && entry.endsWith(".jsp");
	}

	protected boolean matches(JarEntry entry) {
		return matches(entry.getName());
	}

}
