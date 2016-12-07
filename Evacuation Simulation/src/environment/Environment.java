package environment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import entity.Exit;
import entity.Wall;
import evacuation_simulation.Person;
import jade.core.AID;
import repast.simphony.context.Context;
import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.SimpleGridAdder;
import repast.simphony.space.grid.WrapAroundBorders;
import sajas.core.Agent;

public class Environment {
	
	public static int X_DIMENSION;
	public static int Y_DIMENSION;
	
	private Grid<Object> grid;
	private SurfaceMap map;
	private Context<Object> context;
	
	private ArrayList<Pair<Integer,Integer>> busyEntityCells;

	public Environment(Context<Object> context, String fileName){
		this.context = context;
		this.busyEntityCells = new ArrayList<Pair<Integer,Integer>>();
		
		map = new SurfaceMap(fileName);
		X_DIMENSION = map.getWidth();
		Y_DIMENSION = map.getHeight();
		
		GridFactory gridFactory = GridFactoryFinder.createGridFactory(null);

		grid = gridFactory.createGrid("mapgrid", context,
				new GridBuilderParameters<Object>(new WrapAroundBorders(),
						new SimpleGridAdder<Object>(),
						true, X_DIMENSION, Y_DIMENSION));
		placeEntities();
	}
	
	public void placeEntities(){
		for(int y = 0; y < map.getHeight(); y++){
			for(int x = 0; x < map.getWidth(); x++){
				switch(map.getObjectAt(x, y)){
				case 'E':
					Exit exit = new Exit(x,y);
					context.add(exit);
					place(exit, x, y);
					busyEntityCells.add(new Pair<Integer,Integer>(x,y));
					break;
				case 'W':
					Wall wall = new Wall(x,y);
					context.add(wall);
					place(wall, x, y);
					busyEntityCells.add(new Pair<Integer,Integer>(x,y));
					break;					
				}
			}
		}
	}
	
	public boolean cellEmpty( GridCell<Object> cell ) {
		return ( cell.size() == 0 ) ? true : false;
	}
	
	public void place(Object object, int x, int y){
		grid.getAdder().add(grid, object);
		grid.moveTo(object, x, map.getHeight()-y-1);
	}
	
	public void move(Object object, int x, int y){
		grid.moveTo(object, x, map.getHeight()-y-1);
	}
	
	public ArrayList<AID> findNear(Agent myAgent){
		return findNear(myAgent, 1);
	}
	
	public ArrayList<AID> findNear(Agent myAgent, int distance){
		ArrayList<AID> neighboursList = new ArrayList<AID>();
		
		GridCellNgh<Person> neighbourhood = new GridCellNgh<Person>(grid, grid.getLocation(myAgent), Person.class, distance, distance);
		List<GridCell<Person>> nghPoints = neighbourhood.getNeighborhood(false);
		
		for(GridCell<Person> person : nghPoints){
			if(person.size() > 0){
				Iterable<Person> iterable = person.items();
				for(Person agent : iterable){
					neighboursList.add(agent.getAID());
				}
			}
		}
		
		return neighboursList;
	}
	
	public SurfaceMap getMap(){
		return map;
	}

	public Grid<Object> getGrid(){
		return grid;
	}
	
	public ArrayList<Pair<Integer, Integer>> getBestPathFromCell(int x, int y){
		ArrayList<Pair<Integer,Integer>> neighbourCells = new ArrayList<Pair<Integer,Integer>>();
		
		if(x > 0 && map.getDistanceAt(x-1, y) != -1 && userFreeCell(x-1, y))
			neighbourCells.add(new Pair<Integer, Integer>(x-1, y));
		
		if(y > 0 && map.getDistanceAt(x, y-1) != -1 && userFreeCell(x, y-1))
			neighbourCells.add(new Pair<Integer, Integer>(x, y-1));
		
		if(x < map.getWidth() - 1 && map.getDistanceAt(x+1, y) != -1 && userFreeCell(x+1, y))
			neighbourCells.add(new Pair<Integer, Integer>(x+1, y));
		
		if(y < map.getHeight() - 1 && map.getDistanceAt(x, y+1) != -1 && userFreeCell(x, y+1))
			neighbourCells.add(new Pair<Integer, Integer>(x, y+1));
		
		Collections.sort(neighbourCells, map.getDistanceComparator());
		
		return neighbourCells;
	}
	
	public boolean userFreeCell(int x, int y){
		y = map.getHeight() - y - 1;
		boolean isExit = false;
		boolean hasPerson = false;
		Iterable<Object> cellContents = grid.getObjectsAt(x, y);
		for(Object cell : cellContents){
			if(cell instanceof Person)
				hasPerson = true;
			if(cell instanceof Exit)
				isExit = true;
		}
		return (isExit || !hasPerson);
	}
	
	public ArrayList<Pair<Integer,Integer>> getBusyEntityCells(){
		return busyEntityCells;
	}

	public static int getX_DIMENSION() {
		return X_DIMENSION;
	}

	public static int getY_DIMENSION() {
		return Y_DIMENSION;
	}
	
	
}
