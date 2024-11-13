package software_project;

public class Pistol extends Gun {
	public static final int totalRecoilTime = 10;
	static final int totalReloadTime = 100;
	static int shotsFired = 0;
	static final int maxRounds = 15;
	
	public Pistol() {
		id = "Pistol";
		recoilTime = 0;
		reloadTime = 0;
		maxSpread = 0.3;
		unlocked = true;
	}
	@Override
	public void resetTimes() {
		recoilTime = reloadTime = 0;
	}
	@Override
	public void shoot() {
		MainGame.existingEntities.add(new Bullet(Listeners.mx, Listeners.my, MainGame.PISTOL));
	}
	@Override
	public void setReloadRecoilTime() {
		
	}
	
}
