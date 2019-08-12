/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/

package org.mmbase.datatypes.handlers.html;

import org.junit.Test;
import org.mmbase.util.SerializableInputStream;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @version $Id$
 */

public  class MultiPartTest {

    private static final String BOUNDARY = "---------------------------1234";

    protected byte[] getContent() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write('\n');
        out.write(BOUNDARY.getBytes());
        out.write("\nContent-Disposition: form-data; name=\"my_form_handle\"; filename=\"ch.gif'\n".getBytes());
        out.write("Content-Length: 10\n\n".getBytes());
        out.write(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10});
        out.write(BOUNDARY.getBytes());
        out.write("--\n".getBytes());
        return out.toByteArray();
    }

    @Test
    public void basic() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getContentType()).thenReturn("multipart/form-data;  boundary=" + BOUNDARY);

        when(request.getInputStream()).thenReturn(new ServletInputStream() {
            InputStream inputStream = new ByteArrayInputStream(getContent());
            @Override
            public int read() throws IOException {
                return inputStream.read();

            }
        });

        System.out.println("" + request.getInputStream());

        MultiPart.MMultipartRequest r = MultiPart.getMultipartRequest(request, "UTF-8");

        //assertEquals(1, r.parametersMap.size());
        SerializableInputStream is = r.getInputStream("my_form_handle");
        //assertNotNull(is);
        //assertEquals("ch.gif", is.getName());



    }


}
