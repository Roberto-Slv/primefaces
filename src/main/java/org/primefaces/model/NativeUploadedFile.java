/**
 * Copyright 2009-2017 PrimeTek.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.primefaces.model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;

import javax.faces.FacesException;
import javax.servlet.http.Part;

import org.apache.commons.fileupload.ParameterParser;

public class NativeUploadedFile implements UploadedFile, Serializable {

    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
    private static final String FILENAME = "filename";

    private Part part;
    private String filename;
    private byte[] cachedContent;

    public NativeUploadedFile() {
    }

    public NativeUploadedFile(Part part) {
        this.part = part;
        this.filename = resolveFilename(part);
    }

    public String getFileName() {
        return filename;
    }

    public InputStream getInputstream() throws IOException {
        return part.getInputStream();
    }

    public long getSize() {
        return part.getSize();
    }

    public byte[] getContents() {
        if (cachedContent != null) {
            return cachedContent;
        }

        InputStream input = null;
        try {
            input = getInputstream();
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];

            int n = 0;
            while (-1 != (n = input.read(buffer))) {
                output.write(buffer, 0, n);
            }
            cachedContent = output.toByteArray();
        }
        catch (IOException ex) {
            cachedContent = null;
            throw new FacesException(ex);
        }
        finally {
            if (input != null) {
                try {
                    input.close();
                }
                catch (IOException ex) {
                }
            }
        }

        return cachedContent;
    }

    public String getContentType() {
        return part.getContentType();
    }

    public void write(String filePath) throws Exception {
        part.write(filePath);
    }

    public Part getPart() {
        return part;
    }

    private String resolveFilename(Part part) {
        ParameterParser parser = new ParameterParser();
        Map<String, String> params = parser.parse(part.getHeader("content-disposition"), new char[] { ',', ';' });
        return params.get(FILENAME);
    }

    protected String getContentDispositionFileName(final String line) {
        ParameterParser parser = new ParameterParser();
        Map<String, String> params = parser.parse(line, new char[] { ',', ';' });
        return decode(params.get(FILENAME));

    }

    private String decode(String encoded) {
        if (encoded == null) {
            return null;
        }
        try {
            return URLDecoder.decode(encoded, "UTF-8");
        }
        catch (UnsupportedEncodingException ex) {
            throw new FacesException(ex);
        }
    }
}
