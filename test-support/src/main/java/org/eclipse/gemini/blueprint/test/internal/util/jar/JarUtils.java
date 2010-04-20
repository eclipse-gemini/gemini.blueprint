/******************************************************************************
 * Copyright (c) 2006, 2010 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution. 
 * The Eclipse Public License is available at 
 * http://www.eclipse.org/legal/epl-v10.html and the Apache License v2.0
 * is available at http://www.opensource.org/licenses/apache2.0.php.
 * You may elect to redistribute this code under either of these licenses. 
 * 
 * Contributors:
 *   VMware Inc.
 *****************************************************************************/

package org.eclipse.gemini.blueprint.test.internal.util.jar;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;

import org.springframework.core.io.Resource;

/**
 * Utility class for Jar files. As opposed to {@link JarCreator}, this class is
 * stateless and contains only static methods (hence the abstract qualifier).
 * 
 * @author Costin Leau
 * 
 */
public abstract class JarUtils {

	private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

	private static final String MANIFEST_JAR_LOCATION = "/META-INF/MANIFEST.MF";

	static final String SLASH = "/";


	/**
	 * Dumps the entries of a jar and return them as a String. This method can
	 * be memory expensive depending on the jar size.
	 * 
	 * @param jis
	 * @return
	 * @throws Exception
	 */
	public static String dumpJarContent(JarInputStream jis) {
		StringBuilder buffer = new StringBuilder();

		try {
			JarEntry entry;
			while ((entry = jis.getNextJarEntry()) != null) {
				buffer.append(entry.getName());
				buffer.append("\n");
			}
		}
		catch (IOException ioException) {
			buffer.append("reading from stream failed");
		}
		finally {
			closeStream(jis);
		}

		return buffer.toString();
	}

	/**
	 * Dump the entries of a jar and return them as a String. This method can be
	 * memory expensive depending on the jar size.
	 * 
	 * @param resource
	 * @return
	 */
	public static String dumpJarContent(Resource resource) {
		try {
			return dumpJarContent(new JarInputStream(resource.getInputStream()));
		}
		catch (IOException ex) {
			return "reading from stream failed" + ex;
		}
	}

	/**
	 * Writes a resource content to a jar.
	 * 
	 * @param res
	 * @param entryName
	 * @param jarStream
	 * @return the number of bytes written to the jar file
	 * @throws Exception
	 */
	public static int writeToJar(Resource res, String entryName, JarOutputStream jarStream) throws IOException {
		return writeToJar(res, entryName, jarStream, DEFAULT_BUFFER_SIZE);
	}

	/**
	 * 
	 * Writes a resource content to a jar.
	 * 
	 * @param res
	 * @param entryName
	 * @param jarStream
	 * @param bufferSize
	 * @return the number of bytes written to the jar file
	 * @throws Exception
	 */
	public static int writeToJar(Resource res, String entryName, JarOutputStream jarStream, int bufferSize)
			throws IOException {
		byte[] readWriteJarBuffer = new byte[bufferSize];

		// remove leading / if present.
		if (entryName.charAt(0) == '/')
			entryName = entryName.substring(1);

		jarStream.putNextEntry(new ZipEntry(entryName));
		InputStream entryStream = res.getInputStream();

		int numberOfBytes;

		// read data into the buffer which is later on written to the jar.
		while ((numberOfBytes = entryStream.read(readWriteJarBuffer)) != -1) {
			jarStream.write(readWriteJarBuffer, 0, numberOfBytes);
		}
		return numberOfBytes;
	}

	/**
	 * Read the manifest for a given stream. The stream will be wrapped in a
	 * JarInputStream and closed after the manifest was read.
	 * 
	 * @param stream
	 * @return
	 */
	public static Manifest getManifest(InputStream stream) {
		JarInputStream myStream = null;
		try {
			myStream = new JarInputStream(stream);
			return myStream.getManifest();
		}
		catch (IOException ioex) {
			// just ignore it
		}
		finally {
			closeStream(myStream);
		}

		// return (man != null ? man : new Manifest());
		return null;
	}

	/**
	 * Convenience method for reading a manifest from a given resource. Will
	 * assume the resource points to a jar.
	 * 
	 * @param resource
	 * @return
	 */
	public static Manifest getManifest(Resource resource) {
		try {
			return getManifest(resource.getInputStream());
		}
		catch (IOException ex) {
			// ignore
		}
		return null;
	}

	/**
	 * Creates a jar based on the given entries and manifest. This method will
	 * always close the given output stream.
	 * 
	 * @param manifest jar manifest
	 * @param entries map of resources keyed by the jar entry named
	 * @param outputStream output stream for writing the jar
	 * @return number of byte written to the jar
	 */
	public static int createJar(Manifest manifest, Map entries, OutputStream outputStream) throws IOException {
		int writtenBytes = 0;

		// load manifest
		// add it to the jar
		JarOutputStream jarStream = null;

		try {
			// add a jar stream on top
			jarStream = (manifest != null ? new JarOutputStream(outputStream, manifest) : new JarOutputStream(
				outputStream));

			// select fastest level (no compression)
			jarStream.setLevel(Deflater.NO_COMPRESSION);

			// add deps
			for (Iterator iter = entries.entrySet().iterator(); iter.hasNext();) {
				Map.Entry element = (Map.Entry) iter.next();

				String entryName = (String) element.getKey();

				// safety check - all entries must start with /
				if (!entryName.startsWith(SLASH))
					entryName = SLASH + entryName;

				Resource entryValue = (Resource) element.getValue();

				// skip special/duplicate entries (like MANIFEST.MF)
				if (MANIFEST_JAR_LOCATION.equals(entryName)) {
					iter.remove();
				}
				else {
					// write jar entry
					writtenBytes += JarUtils.writeToJar(entryValue, entryName, jarStream);
				}
			}
		}
		finally {
			try {
				jarStream.flush();
			}
			catch (IOException ex) {
				// ignore
			}
			try {
				jarStream.finish();
			}
			catch (IOException ex) {
				// ignore
			}

		}

		return writtenBytes;
	}

	private static void closeStream(InputStream stream) {
		if (stream != null)
			try {
				stream.close();
			}
			catch (IOException ex) {
				// ignore
			}
	}
}
