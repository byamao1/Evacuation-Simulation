package evacuation_simulation;

import java.io.File;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import environment.Environment;
import environment.Pair;
import jade.core.AID;
import repast.simphony.context.Context;
import repast.simphony.random.RandomHelper;
import sajas.wrapper.ContainerController;
import tools.Log;

public class ScenarioBuilder {
	private Environment environment;

	private String populationFile = "scenarios/scenario1.xml";
	private String environmentFile = "maps/testMap_wall.map";
	private Context<Object> currentContext;
	private ResultsCollector resultsCollector;
	private ContainerController agentContainer;

	ScenarioBuilder(Context<Object> context){
		this.currentContext = context;
		this.environment = new Environment(currentContext, environmentFile);
	}

	/**
	 * @param resultsCollector the resultsCollector to set
	 */
	public void setResultsCollector(ResultsCollector resultsCollector) {
		this.resultsCollector = resultsCollector;
	}

	/**
	 * @param agentContainer the agentContainer to set
	 */
	public void setAgentContainer(ContainerController agentContainer) {
		this.agentContainer = agentContainer;
	}

	/**
	 * Reads from a configuration file the specification for the population. 
	 * @return true upon success, false otherwise
	 */
	public boolean createPopulation() {
		try {
			File fXmlFile = new File(populationFile);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);

			doc.getDocumentElement().normalize();

			NodeList nList = doc.getElementsByTagName("scenario");

			ArrayList<Pair<Integer,Integer>> busyCells = new ArrayList<Pair<Integer, Integer>>();
			busyCells.addAll(environment.getBusyEntityCells());

			int nPeople = 0;
			for (int i = 0; i < nList.getLength(); i++) {
				try{
					Node nNode = nList.item(i);

					if (nNode.getNodeType() == Node.ELEMENT_NODE) {

						Element eElement = (Element) nNode;

						int areaKnowledge = Integer.parseInt(eElement.getElementsByTagName("areaKnowledge").item(0).getTextContent());
						int altruism = Integer.parseInt(eElement.getElementsByTagName("altruism").item(0).getTextContent());
						int independence = Integer.parseInt(eElement.getElementsByTagName("independence").item(0).getTextContent());
						int fatigue = Integer.parseInt(eElement.getElementsByTagName("fatigue").item(0).getTextContent());
						int mobility = Integer.parseInt(eElement.getElementsByTagName("mobility").item(0).getTextContent());
						int panic = Integer.parseInt(eElement.getElementsByTagName("panic").item(0).getTextContent());
						int age = Integer.parseInt(eElement.getElementsByTagName("age").item(0).getTextContent());

						int x, y;
						if(eElement.getElementsByTagName("position").getLength() > 0){
							Element positionElement = (Element) eElement.getElementsByTagName("position").item(0);
							x = Integer.parseInt(positionElement.getAttribute("x"));
							y = Integer.parseInt(positionElement.getAttribute("y"));
						}else{
							x = RandomHelper.nextIntFromTo(0, Environment.getX_DIMENSION()-1);
							y = RandomHelper.nextIntFromTo(0, Environment.getY_DIMENSION()-1);
						}

						while(busyCells.contains(new Pair<Integer,Integer>(x, y)) 
								|| x > Environment.getX_DIMENSION()-1 || y > Environment.getY_DIMENSION()-1){
							x = RandomHelper.nextIntFromTo(0, Environment.getX_DIMENSION()-1);
							y = RandomHelper.nextIntFromTo(0, Environment.getY_DIMENSION()-1);
						}
						busyCells.add(new Pair<Integer,Integer>(x, y));

						Person newAgent = null;
						AID resultsCollectorAID = null;
						if(resultsCollector != null) {
							resultsCollectorAID = resultsCollector.getAID();
						}

						if(Knowledgeable.validAttributes(areaKnowledge, independence)){
							newAgent = new Knowledgeable(resultsCollectorAID, environment, currentContext, x, y);
						}else if(IndependentKnowledgeable.validAttributes(areaKnowledge, independence)){
							newAgent = new IndependentKnowledgeable(resultsCollectorAID, environment, currentContext, x, y);
						}else if(Independent.validAttributes(areaKnowledge, independence)){
							newAgent = new Independent(resultsCollectorAID, environment, currentContext, x, y);
						}else if(DependentUnknowledgeable.validAttributes(areaKnowledge, independence)){
							newAgent = new DependentUnknowledgeable(resultsCollectorAID, environment, currentContext, x, y);
						}else{
							Log.error("Invalid configuration for Person.");
						}

						newAgent.setAreaKnowledge(areaKnowledge);
						newAgent.setIndependence(independence);
						newAgent.setAltruism(altruism);
						newAgent.setFatigue(fatigue);
						newAgent.setAge(age);
						newAgent.setMobility(mobility);
						newAgent.setPanic(panic);

						agentContainer.acceptNewAgent(newAgent.getClass().getSimpleName() + "_" + i, newAgent).start();
						nPeople++;
					}
				}catch(Exception e){
					Log.error("Invalid configuration for Person.");
					continue;
				}
			}

			if(resultsCollector != null) {
				resultsCollector.setnEvacuees(nPeople);
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			Log.error("Unable to configure scenario.");
			return false;
		}
	}


}
