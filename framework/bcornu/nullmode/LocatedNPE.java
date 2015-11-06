package bcornu.nullmode;

public class LocatedNPE extends NullPointerException {

	public LocatedNPE(String location) {
		super(location);
	}
	
	@Override
	public String toString() {
		return super.toString()+"\n"+NullInstanceManager.printNb();
	}

}
