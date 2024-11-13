package software_project;

public class Rifle extends Gun {
	public static final int totalRecoilTime = 1;
	static final int totalReloadTime = 200;
	int shotsFired = 0;
	final static int maxRounds = 25;
	
	public Rifle() {
		id = "Rifle";
		recoilTime = 0;
		reloadTime = 0;
		maxSpread = 0.3;
	}
	
	@Override
	public void resetTimes() {
		recoilTime = reloadTime = 0;			
	}
	@Override
	public void shoot() {
		MainGame.existingEntities.add(new Bullet(Listeners.mx, Listeners.my, MainGame.RIFLE));	
	}
	@Override
	public void setReloadRecoilTime() {
		// TODO Auto-generated method stub
		
	}
}