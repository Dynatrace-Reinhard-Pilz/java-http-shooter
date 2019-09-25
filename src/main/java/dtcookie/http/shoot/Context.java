package dtcookie.http.shoot;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public final class Context {
	
	private static long LAST_UPDATE_TS = 0L;
	private static String ctx = null;
	
	private static final Object LOCK = new Object();

	private Context() {
		// prevent instantiation
	}
	
	public static String get(String host, int port) {
		synchronized (LOCK) {
			if ((ctx == null) || (System.currentTimeMillis() - LAST_UPDATE_TS > 30000)) {
				try {
					String newCtx = fetch(host, port);
					if (newCtx != null) {
						ctx = newCtx;
					}
					LAST_UPDATE_TS = System.currentTimeMillis();
				} catch (IOException e) {
					e.printStackTrace(System.err);
				}
			}
			return ctx;
		}
	}
	
	public static void invalidate() {
		synchronized (LOCK) {
			ctx = null;
		}
	}
	

	
	private static String fetch(String host, int port) throws IOException {
		URL url = new URL("http://" + host + ":" + port + "/fixed/context");
		URLConnection con = url.openConnection();
		con.setReadTimeout(1000);
		con.setConnectTimeout(1000);
		try (InputStream in = con.getInputStream()) {
			return readLine(in);
		} catch (IOException e) {
			return null;
		}
	}
	
	private static String readLine(InputStream in) throws IOException {
		try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			copy(in, out);
			return new String(out.toByteArray());
		}
	}
	
	private static void copy(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[4096];
		int len = in.read(buffer);
		while (len > 0) {
			out.write(buffer, 0, len);
			len = in.read(buffer);
		}
		out.flush();
	}
}
