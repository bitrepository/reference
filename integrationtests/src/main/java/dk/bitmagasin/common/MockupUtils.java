package dk.bitmagasin.common;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class MockupUtils {
	
	private static final int IO_BUFFER_SIZE = 1024;
	
	public static void copyInputStreamToOutputStream(InputStream in,
			OutputStream out) throws Exception {
		if(in == null || out == null) {
			throw new NullPointerException("InputStream: " + in 
					+ ", OutputStream: " + out);
		}

		try {
			byte[] buf = new byte[IO_BUFFER_SIZE];
			int bytesRead;
			while ((bytesRead = in.read(buf)) != -1) {
				out.write(buf, 0, bytesRead);
			}
			out.flush();
		} finally {
			in.close();
		}
	}

}
