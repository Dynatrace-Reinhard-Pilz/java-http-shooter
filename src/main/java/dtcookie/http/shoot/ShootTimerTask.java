package dtcookie.http.shoot;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class ShootTimerTask extends TimerTask {
	
	private static final int NUM_THREADS = 8;
	
	private static final String HOST = "ec2-3-89-32-201.compute-1.amazonaws.com";
	private static final int PORT = 8080;
	
	private static final AtomicInteger COUNT = new AtomicInteger(0);
	private static final AtomicInteger CONCURRENTS = new AtomicInteger(0);
	
	private static final ExecutorService THREAD_POOL = Executors.newFixedThreadPool(NUM_THREADS, new ThreadFactory() {
		
		@Override
		public Thread newThread(Runnable r) {
			Thread thread = new Thread(r, "shooter-" + COUNT.incrementAndGet());
			thread.setDaemon(true);
			return thread;
		}
	});
	
	@Override
	public void run() {
		if (CONCURRENTS.get() > NUM_THREADS) {
			return;
		}
		THREAD_POOL.execute(new Runnable() {
			@Override
			public void run() {
				try {
					int fib = 61;
					int count = CONCURRENTS.incrementAndGet();
					String ctx = Context.get(HOST, PORT);
					if (ctx == null) {
						return;
					}
					URL url = new URL("http://" + HOST + ":" + PORT + ctx + "/status?fib=" + fib);
					long start = System.currentTimeMillis();
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					conn.setConnectTimeout(1000);
					conn.setReadTimeout(10000);
					try (InputStream in = conn.getInputStream()) {
						drain(in);
						System.out.println("[" + fib + "] " + "[" + ctx + "] " + (System.currentTimeMillis() - start) + " ms (" + count + ")");
					} catch (IOException e) {
						try {
							if (conn.getResponseCode() == 404) {
								Context.invalidate();
							}
						} catch (Throwable t) {
							// ignore
						}
						System.err.println("[" + fib + "] " + "[" + ctx + "] " + (System.currentTimeMillis() - start) + " ms (" + count + ")");
					}
				} catch (Throwable t) {
					t.printStackTrace(System.err);
				} finally {
					CONCURRENTS.decrementAndGet();
				}
			}			
		});
	}

	public static final void drain(InputStream in) throws IOException {
		byte[] buffer = new byte[4096];
		int read = in.read(buffer);
		while (read > 0) {
			read = in.read(buffer);
		}
	}	
	
}
