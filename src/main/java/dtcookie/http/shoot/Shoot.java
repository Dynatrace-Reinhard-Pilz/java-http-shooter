package dtcookie.http.shoot;

import java.util.Timer;

public class Shoot {
	
	private static final Timer timer = new Timer(true);

	public static void main(String[] args) throws Exception {
		timer.schedule(new ShootTimerTask(), 0L, 3000L);
		synchronized(Shoot.class) {
			Shoot.class.wait();
		}
	}
	


}
