package software_project;

public abstract class Gun {
	String id;
	int recoilTime = 10;
	int reloadTime = 100;
	double maxSpread = 0;
	public boolean unlocked = false;
	
	Gun() {}
	
	abstract public void resetTimes();
	abstract public void shoot();
	abstract public void setReloadRecoilTime();
	
}
