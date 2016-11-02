package evacuation_Simulation;

import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.continuous.RandomCartesianAdder;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.SimpleGridAdder;
import repast.simphony.space.grid.WrapAroundBorders;

public class Environment {
	ContinuousSpace<Object> space;
	Grid<Object> grid;

	public Environment(Context<Object> context){
		// TODO change space

		ContinuousSpaceFactory spaceFactory = ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null);

		space = spaceFactory.createContinuousSpace("space", context, new RandomCartesianAdder<Object>(), 
				new repast.simphony.space.continuous.WrapAroundBorders(), 50, 50);

		GridFactory gridFactory = GridFactoryFinder.createGridFactory(null);

		grid = gridFactory.createGrid("grid", context,
				new GridBuilderParameters<Object>(new WrapAroundBorders(),
						new SimpleGridAdder<Object>(),
						true, 50, 50));
	}
	
	public void place(Object obj){
		NdPoint pt = space.getLocation(obj);
		grid.moveTo(obj,  (int)pt.getX(), (int)pt.getY());
	}

}