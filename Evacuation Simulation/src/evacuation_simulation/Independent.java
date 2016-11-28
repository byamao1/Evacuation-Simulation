package evacuation_simulation;

import jade.core.AID;
import repast.simphony.random.RandomHelper;

public class Independent extends Person{

	public Independent(AID resultsCollector, Environment environment){
		super(resultsCollector, environment);
		
		independence = RandomHelper.nextIntFromTo(MAX_SCALE / 2, MAX_SCALE);
		areaKnowledge = RandomHelper.nextIntFromTo(MIN_SCALE, MAX_SCALE/2);
	}
	
	
}