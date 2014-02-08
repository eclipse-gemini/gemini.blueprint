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

package org.eclipse.gemini.blueprint.test.internal.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Utility class for IO operations.
 *
 * @author Costin Leau
 */
public abstract class IOUtils {

    public static interface IOCallback {
        void doWithIO() throws IOException;
    }

    public static void doWithIO(IOCallback callback) {
        try {
            callback.doWithIO();
        } catch (IOException ioException) {
            // ???
        }
    }

    public static void closeStream(InputStream stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException ex) {
                // ignore
            }
        }
    }

    public static void closeStream(OutputStream stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException ex) {
                // ignore
            }
        }
    }

    /**
     * Delete the given file (can be a simple file or a folder).
     *
     * @param file the file to be deleted
     * @return if the deletion succeeded or not
     */
    public static boolean delete(File file) {

        // bail out quickly
        if (file == null) {
            return false;
        }

        // recursively delete children file
        boolean success = true;

        if (file.isDirectory()) {
            String[] children = file.list();
            for (String aChildren : children) {
                success &= delete(new File(file, aChildren));
            }
        }

        // The directory is now empty so delete it
        return (success &= file.delete());
    }
}
