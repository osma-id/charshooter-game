package software_project;

public class Shotgun extends Gun {
	public static final int totalRecoilTime = 20;
	static final int totalReloadTime = 150;
	static int shotsFired = 0;
	final static int maxRounds = 8;
	
	public Shotgun() {
		id = "Shotgun";
		recoilTime = 0;
		reloadTime = 0; //make later
		maxSpread = 0.6;
	}
	
	@Override
	public void resetTimes() {
		recoilTime = reloadTime = 0;
	}
	@Override
	public void shoot() {
		MainGame.existingEntities.add(new Bullet(Listeners.mx, Listeners.my, MainGame.SHOTGUN));
	}
	@Override
	public void setReloadRecoilTime() {
		if (recoilTime == totalRecoilTime);
	}
}