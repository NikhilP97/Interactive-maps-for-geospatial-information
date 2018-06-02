package module6;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.GeoJSONReader;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.AbstractShapeMarker;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.MultiMarker;
import de.fhpotsdam.unfolding.providers.Google;
import de.fhpotsdam.unfolding.providers.MBTilesMapProvider;
import de.fhpotsdam.unfolding.providers.Microsoft;
import de.fhpotsdam.unfolding.utils.MapUtils;
import parsing.ParseFeed;
import processing.core.PApplet;

/** EarthquakeCityMap
 * An application with an interactive map displaying earthquake data.
 * Author: UC San Diego Intermediate Software Development MOOC team
 * @author Your name here
 * Date: July 17, 2015
 * */
public class EarthquakeCityMap extends PApplet {
	
	// We will use member variables, instead of local variables, to store the data
	// that the setUp and draw methods will need to access (as well as other methods)
	// You will use many of these variables, but the only one you should need to add
	// code to modify is countryQuakes, where you will store the number of earthquakes
	// per country.
	
	// You can ignore this.  It's to get rid of eclipse warnings
	private static final long serialVersionUID = 1L;

	// IF YOU ARE WORKING OFFILINE, change the value of this variable to true
	private static final boolean offline = false;
	
	/** This is where to find the local tiles, for working without an Internet connection */
	public static String mbTilesString = "blankLight-1-3.mbtiles";
	
	

	//feed with magnitude 2.5+ Earthquakes
	private String earthquakesURL = "https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/2.5_week.atom";
	
	// The files containing city names and info and country names and info
	private String cityFile = "city-data.json";
	private String countryFile = "countries.geo.json";
	
	// The map
	private UnfoldingMap map;
	
	// Markers for each city
	private List<Marker> cityMarkers;
	// Markers for each earthquake
	private List<Marker> quakeMarkers;
	
	private List<EarthquakeMarker> earthquakeMarkers;
	private List<LandQuakeMarker> landquakeMarkers;
	private List<OceanQuakeMarker> oceanquakeMarkers;

	// A List of country markers
	private List<Marker> countryMarkers;
	
	private List<Marker> keyMarkers;
	
	// NEW IN MODULE 5
	private CommonMarker lastSelected;
	private CommonMarker lastClicked;
	
	private boolean foundMarker = false;
	private boolean clickedCityMarker = false;
	private boolean clickedEarthquakeMarker = false;
	private boolean clickedCityKeyMarker = false;
	private boolean clickedLandquakeKeyMarker = false;
	private boolean clickedOceanquakeKeyMarker = false;
	private boolean clickedShallowKeyMarker = false;
	private boolean clickedIntermediateKeyMarker = false;
	private boolean clickedDeepKeyMarker = false;
	private boolean clickedPasthourKeyMarker = false;
	
	
	private boolean allMarkersWereShown = true;
	
	
	
	public void setup() {		
		// (1) Initializing canvas and map tiles
		size(900, 700, OPENGL);
		if (offline) {
		    map = new UnfoldingMap(this, 200, 50, 650, 600, new MBTilesMapProvider(mbTilesString));
		    earthquakesURL = "2.5_week.atom";  // The same feed, but saved August 7, 2015
		}
		else {
			map = new UnfoldingMap(this, 200, 50, 650, 600, new Microsoft.RoadProvider());
			// IF YOU WANT TO TEST WITH A LOCAL FILE, uncomment the next line
		    //earthquakesURL = "2.5_week.atom";
		}
		MapUtils.createDefaultEventDispatcher(this, map);
		
		// FOR TESTING: Set earthquakesURL to be one of the testing files by uncommenting
		// one of the lines below.  This will work whether you are online or offline
//		earthquakesURL = "test1.atom";
//		earthquakesURL = "test2.atom";
		
		// Uncomment this line to take the quiz
		earthquakesURL = "quiz2.atom";
		
		
		// (2) Reading in earthquake data and geometric properties
	    //     STEP 1: load country features and markers
		List<Feature> countries = GeoJSONReader.loadData(this, countryFile);
		countryMarkers = MapUtils.createSimpleMarkers(countries);
		
		//     STEP 2: read in city data
		List<Feature> cities = GeoJSONReader.loadData(this, cityFile);
		cityMarkers = new ArrayList<Marker>();
		for(Feature city : cities) {
		  cityMarkers.add(new CityMarker(city));
		}
	    
		//     STEP 3: read in earthquake RSS feed
	    List<PointFeature> earthquakes = ParseFeed.parseEarthquake(this, earthquakesURL);
	    quakeMarkers = new ArrayList<Marker>();
	    earthquakeMarkers = new ArrayList<EarthquakeMarker>();
	    landquakeMarkers = new ArrayList<LandQuakeMarker>();
	    oceanquakeMarkers = new ArrayList<OceanQuakeMarker>();
	    
	    for(PointFeature feature : earthquakes) {
		  //check if LandQuake
		  if(isLand(feature)) {
		    quakeMarkers.add(new LandQuakeMarker(feature));
		    earthquakeMarkers.add(new LandQuakeMarker(feature));
		    landquakeMarkers.add(new LandQuakeMarker(feature));
		    
		  }
		  // OceanQuakes
		  else {
		    quakeMarkers.add(new OceanQuakeMarker(feature));
		    earthquakeMarkers.add(new OceanQuakeMarker(feature));
		    oceanquakeMarkers.add(new OceanQuakeMarker(feature));
		  }
	    }

	    // could be used for debugging
	    printQuakes();
	 		
	    // (3) Add markers to map
	    //     NOTE: Country markers are not added to the map.  They are used
	    //           for their geometric properties
	    map.addMarkers(quakeMarkers);
	    map.addMarkers(cityMarkers);
	    
	    sortAndPrint(100);
	    
	    
	}  // End setup
	
	
	public void draw() {
		background(0);
		map.draw();
		addKey();
		
		if(clickedCityKeyMarker) {
			fill(0, 0, 0);
			ellipse(45, 100, 3, 3);
		}
		if(clickedLandquakeKeyMarker) {
			fill(0, 0, 0);
			ellipse(45, 120, 3, 3);
		}
		if(clickedOceanquakeKeyMarker) {
			fill(0, 0, 0);
			ellipse(45, 140, 3, 3);
		}
		if(clickedShallowKeyMarker) {
			fill(0, 0, 0);
			ellipse(45, 190, 3, 3);
		}
		if(clickedIntermediateKeyMarker){
			fill(0, 0, 0);
			ellipse(45, 210, 3, 3);
		}
		if(clickedDeepKeyMarker) {
			fill(0, 0, 0);
			ellipse(45, 230, 3, 3);
		}
		if(clickedPasthourKeyMarker) {
			fill(0, 0, 0);
			ellipse(45, 250, 3, 3);
		}
		
//		System.out.println("Mouse : "+mouseX+", "+mouseY);
		
	}
	
	
	// TODO: Add the method:
	//   private void sortAndPrint(int numToPrint)
	// and then call that method from setUp
	
	private void sortAndPrint(int numToPrint) {
		Collections.sort(earthquakeMarkers);
		System.out.println("Sorted list is ");
		Object[] objects = earthquakeMarkers.toArray();
		int iteratr = 0;
		while(iteratr < objects.length && iteratr < numToPrint) {
			System.out.println(( (EarthquakeMarker) objects[iteratr]).getTitle());
			iteratr++;
		}
	}
	
	/** Event handler that gets called automatically when the 
	 * mouse moves.
	 */
	@Override
	public void mouseMoved()
	{
		// clear the last selection
		if (lastSelected != null) {
			lastSelected.setSelected(false);
			lastSelected = null;
		
		}
		selectMarkerIfHover(quakeMarkers);
		selectMarkerIfHover(cityMarkers);
		//loop();
	}
	
	// If there is a marker selected 
	private void selectMarkerIfHover(List<Marker> markers)
	{
		// Abort if there's already a marker selected
		if (lastSelected != null) {
			return;
		}
		
		for (Marker m : markers) 
		{
			CommonMarker marker = (CommonMarker)m;
			if (marker.isInside(map,  mouseX, mouseY)) {
				lastSelected = marker;
				marker.setSelected(true);
				return;
			}
		}
	}
	
	/** The event handler for mouse clicks
	 * It will display an earthquake and its threat circle of cities
	 * Or if a city is clicked, it will display all the earthquakes 
	 * where the city is in the threat circle
	 */
	@Override
	public void mouseClicked()
	{
		
		if(!foundMarker) {
			checkEarthquakesForClick();
		}
		if(!foundMarker) {
			checkCitiesForClick();
		}
		if(!foundMarker) {
			cityKeyClicked();
		}
		if(!foundMarker) {
			landquakeKeyClicked();		
		}
		if(!foundMarker) {
			oceanquakeKeyClicked();
		}
		if(!foundMarker) {
			shallowKeyClicked();
		}
		if(!foundMarker) {
			intermediateKeyClicked();
		}
		if(!foundMarker) {
			deepKeyClicked();
		}
		if(!foundMarker) {
			pasthourKeyClicked();
		}
		if(!clickedCityKeyMarker && !clickedLandquakeKeyMarker && !clickedOceanquakeKeyMarker && !clickedShallowKeyMarker && !clickedIntermediateKeyMarker
				&& !clickedDeepKeyMarker && !clickedPasthourKeyMarker) {
			if(foundMarker && !clickedCityMarker && !clickedEarthquakeMarker) {
				unhideMarkers();
			}
		}
		
		if(lastClicked != null && !foundMarker) {
			unhideMarkers();
			lastClicked = null;
			allMarkersWereShown = true;
			clickedCityMarker = false;
			clickedEarthquakeMarker = false;
			clickedCityKeyMarker = false;
			clickedLandquakeKeyMarker = false;
			clickedOceanquakeKeyMarker = false;
			clickedShallowKeyMarker = false;
			clickedIntermediateKeyMarker = false;
			clickedDeepKeyMarker = false;
			clickedPasthourKeyMarker = false;
		}
		foundMarker = false;
		
		
	}
	
	private void pasthourKeyClicked() {
		// TODO Auto-generated method stub
		boolean hidden = false;
		
		boolean checkX;
		boolean checkY;
		if(mouseX >= 54 && mouseX <= 66) {
			checkX = true;
		}
		else {
			checkX =false;
		}
		
		if(mouseY >= 244 && mouseY <= 256) {
			checkY = true;
		}
		else {
			checkY = false;
		}
		
		if(checkX && checkY) {
			System.out.println("Mouse click in PastHour Key is : "+mouseX+", "+mouseY);
			if(clickedPasthourKeyMarker) {
				clickedPasthourKeyMarker = false;
				if(allMarkersWereShown) {
					unhideMarkers();
					foundMarker = true;
					return;
				}
				else {
					for(Marker m : quakeMarkers) {
						EarthquakeMarker lnd = (EarthquakeMarker)m;
						if(lnd.isMarkedX)
						m.setHidden(true);
					}
					foundMarker = true;
					return;
				}
				
			}
			
			for(Marker m : quakeMarkers) {
				EarthquakeMarker lnd = (EarthquakeMarker)m;
				if(m.isHidden() && lnd.isMarkedX) {
					if(clickedLandquakeKeyMarker && lnd.isOnLand) {
						if(clickedShallowKeyMarker && lnd.isShallow) {
							hidden = true;
							m.setHidden(false);
							lastClicked = (CommonMarker)m;
						}
						if(clickedIntermediateKeyMarker && lnd.isIntermediate) {
							hidden = true;
							m.setHidden(false);
							lastClicked = (CommonMarker)m;
						}
						if(clickedDeepKeyMarker && lnd.isDeep) {
							hidden = true;
							m.setHidden(false);
							lastClicked = (CommonMarker)m;
						}
						if(!clickedShallowKeyMarker && !clickedIntermediateKeyMarker && !clickedDeepKeyMarker) {
							hidden = true;
							m.setHidden(false);
							lastClicked = (CommonMarker)m;
						}
					}
					if(clickedOceanquakeKeyMarker && !lnd.isOnLand) {
						if(clickedShallowKeyMarker && lnd.isShallow) {
							hidden = true;
							m.setHidden(false);
							lastClicked = (CommonMarker)m;
						}
						if(clickedIntermediateKeyMarker && lnd.isIntermediate) {
							hidden = true;
							m.setHidden(false);
							lastClicked = (CommonMarker)m;
						}
						if(clickedDeepKeyMarker && lnd.isDeep) {
							hidden = true;
							m.setHidden(false);
							lastClicked = (CommonMarker)m;
						}
						if(!clickedShallowKeyMarker && !clickedIntermediateKeyMarker && !clickedDeepKeyMarker) {
							hidden = true;
							m.setHidden(false);
							lastClicked = (CommonMarker)m;
						}
					}
					if(!clickedOceanquakeKeyMarker && !clickedLandquakeKeyMarker) {
						if(clickedShallowKeyMarker && lnd.isShallow) {
							hidden = true;
							m.setHidden(false);
							lastClicked = (CommonMarker)m;
						}
						if(clickedIntermediateKeyMarker && lnd.isIntermediate) {
							hidden = true;
							m.setHidden(false);
							lastClicked = (CommonMarker)m;
						}
						if(clickedDeepKeyMarker && lnd.isDeep) {
							hidden = true;
							m.setHidden(false);
							lastClicked = (CommonMarker)m;
						}
						if(!clickedShallowKeyMarker && !clickedIntermediateKeyMarker && !clickedDeepKeyMarker) {
							hidden = true;
							m.setHidden(false);
							lastClicked = (CommonMarker)m;
						}
					}	
				}	
			}
			if(hidden) {
				allMarkersWereShown = false;
				clickedPasthourKeyMarker = true;
				foundMarker = true;
				return;
			}
			
			if(!hidden) {
				for(Marker m : cityMarkers) {
					if(!clickedCityKeyMarker) {
						m.setHidden(true);
						lastClicked = (CommonMarker)m;
					}	
				}
				for(Marker om : quakeMarkers) {
					EarthquakeMarker em = (EarthquakeMarker)om;
					if(!em.isMarkedX) {
						om.setHidden(true);
					}
					if(em.isMarkedX) {
						if(clickedLandquakeKeyMarker && !em.isOnLand && !clickedOceanquakeKeyMarker) {
							if(clickedShallowKeyMarker && !em.isShallow) {
								om.setHidden(true);
							}
							if(clickedIntermediateKeyMarker && !em.isIntermediate) {
								om.setHidden(true);
							}
							if(clickedDeepKeyMarker && !em.isDeep) {
								om.setHidden(true);
							}
							if(!clickedShallowKeyMarker && !clickedIntermediateKeyMarker && !clickedDeepKeyMarker) {
								om.setHidden(true);
							}
							
						}
						else if(clickedOceanquakeKeyMarker && em.isOnLand && !clickedLandquakeKeyMarker) {
							if(clickedShallowKeyMarker && !em.isShallow) {
								om.setHidden(true);
							}
							if(clickedIntermediateKeyMarker && !em.isIntermediate) {
								om.setHidden(true);
							}
							if(clickedDeepKeyMarker && !em.isDeep) {
								om.setHidden(true);
							}
							if(!clickedShallowKeyMarker && !clickedIntermediateKeyMarker && !clickedDeepKeyMarker) {
								om.setHidden(true);
							}
						}
					}
				}
				clickedPasthourKeyMarker = true;
				foundMarker = true;
				allMarkersWereShown = true;
				return;
			}
		}
		
	}


	private void deepKeyClicked() {
		// TODO Auto-generated method stub
		boolean hidden = false;
		
		boolean checkX;
		boolean checkY;
		if(mouseX >= 54 && mouseX <= 66) {
			checkX = true;
		}
		else {
			checkX =false;
		}
		
		if(mouseY >= 224 && mouseY <= 236) {
			checkY = true;
		}
		else {
			checkY = false;
		}
		
		if(checkX && checkY) {
			System.out.println("Mouse click in Deep Key is : "+mouseX+", "+mouseY);
			if(clickedDeepKeyMarker) {
				clickedDeepKeyMarker = false;
				if(allMarkersWereShown) {
					unhideMarkers();
					foundMarker = true;
					return;
				}
				else {
					for(Marker m : quakeMarkers) {
						EarthquakeMarker lnd = (EarthquakeMarker)m;
						if(lnd.isDeep)
						m.setHidden(true);
					}
					foundMarker = true;
					return;
				}
				
			}
			
			for(Marker m : quakeMarkers) {
				EarthquakeMarker lnd = (EarthquakeMarker)m;
				if(m.isHidden() && lnd.isDeep) {
					if(!clickedLandquakeKeyMarker && !clickedOceanquakeKeyMarker) {
						System.out.println("Shallow key pressed but no other one");
						lastClicked = (CommonMarker)m;
						m.setHidden(false);
						hidden = true;
					}
					if(clickedLandquakeKeyMarker && lnd.isOnLand) {
						lastClicked = (CommonMarker)m;
						m.setHidden(false);
						hidden = true;
					}
					if(clickedOceanquakeKeyMarker && !lnd.isOnLand) {
						lastClicked = (CommonMarker)m;
						m.setHidden(false);
						hidden = true;
					}
				}	
			}
			if(hidden) {
				allMarkersWereShown = false;
				clickedDeepKeyMarker = true;
				foundMarker = true;
				return;
			}
			
			if(!hidden) {
				for(Marker m : cityMarkers) {
					if(!clickedCityKeyMarker) {
						m.setHidden(true);
						lastClicked = (CommonMarker)m;
					}
					
				}
				for(Marker om : quakeMarkers) {
					EarthquakeMarker em = (EarthquakeMarker)om;
					if(!em.isDeep) {
						om.setHidden(true);
					}
					if(em.isDeep) {
						if(clickedLandquakeKeyMarker && !em.isOnLand && !clickedOceanquakeKeyMarker) {
							om.setHidden(true);
						}
						else if(clickedOceanquakeKeyMarker && em.isOnLand && !clickedLandquakeKeyMarker) {
							om.setHidden(true);
						}
					}
				}
				clickedDeepKeyMarker = true;
				foundMarker = true;
				allMarkersWereShown = true;
				return;
			}
		}
		
	}


	private void intermediateKeyClicked() {
		// TODO Auto-generated method stub
		boolean hidden = false;
		
		boolean checkX;
		boolean checkY;
		if(mouseX >= 54 && mouseX <= 66) {
			checkX = true;
		}
		else {
			checkX =false;
		}
		
		if(mouseY >= 204 && mouseY <= 216) {
			checkY = true;
		}
		else {
			checkY = false;
		}
		
		if(checkX && checkY) {
			System.out.println("Mouse click in Intermediate Key is : "+mouseX+", "+mouseY);
			if(clickedIntermediateKeyMarker) {
				clickedIntermediateKeyMarker = false;
				if(allMarkersWereShown) {
					unhideMarkers();
					foundMarker = true;
					return;
				}
				else {
					for(Marker m : quakeMarkers) {
						EarthquakeMarker lnd = (EarthquakeMarker)m;
						if(lnd.isIntermediate)
						m.setHidden(true);
					}
					foundMarker = true;
					return;
				}
				
			}
			
			for(Marker m : quakeMarkers) {
				EarthquakeMarker lnd = (EarthquakeMarker)m;
				if(m.isHidden() && lnd.isIntermediate) {
					if(!clickedLandquakeKeyMarker && !clickedOceanquakeKeyMarker) {
						System.out.println("Intermediate key pressed but no other one");
						lastClicked = (CommonMarker)m;
						m.setHidden(false);
						hidden = true;
					}
					if(clickedLandquakeKeyMarker && lnd.isOnLand) {
						lastClicked = (CommonMarker)m;
						m.setHidden(false);
						hidden = true;
					}
					if(clickedOceanquakeKeyMarker && !lnd.isOnLand) {
						lastClicked = (CommonMarker)m;
						m.setHidden(false);
						hidden = true;
					}
				}	
			}
			if(hidden) {
				allMarkersWereShown = false;
				clickedIntermediateKeyMarker = true;
				foundMarker = true;
				return;
			}
			
			if(!hidden) {
				for(Marker m : cityMarkers) {
					if(!clickedCityKeyMarker) {
						m.setHidden(true);
						lastClicked = (CommonMarker)m;
					}
					
				}
				for(Marker om : quakeMarkers) {
					EarthquakeMarker em = (EarthquakeMarker)om;
					if(!em.isIntermediate) {
						om.setHidden(true);
					}
					if(em.isIntermediate) {
						if(clickedLandquakeKeyMarker && !em.isOnLand && !clickedOceanquakeKeyMarker) {
							om.setHidden(true);
						}
						else if(clickedOceanquakeKeyMarker && em.isOnLand && !clickedLandquakeKeyMarker) {
							om.setHidden(true);
						}
					}
				}
				clickedIntermediateKeyMarker = true;
				foundMarker = true;
				allMarkersWereShown = true;
				
				return;
			}
		}
		
	}


	private void shallowKeyClicked() {
		// TODO Auto-generated method stub
		
		boolean hidden = false;
		
		boolean checkX;
		boolean checkY;
		if(mouseX >= 54 && mouseX <= 66) {
			checkX = true;
		}
		else {
			checkX =false;
		}
		
		if(mouseY >= 184 && mouseY <= 196) {
			checkY = true;
		}
		else {
			checkY = false;
		}
		
		if(checkX && checkY) {
			System.out.println("Mouse click in Shallowquake Key is : "+mouseX+", "+mouseY);
			if(clickedShallowKeyMarker) {
				clickedShallowKeyMarker = false;
				if(allMarkersWereShown) {
					unhideMarkers();
					foundMarker = true;
					return;
				}
				else {
					for(Marker m : quakeMarkers) {
						EarthquakeMarker lnd = (EarthquakeMarker)m;
						if(lnd.isShallow)
						m.setHidden(true);
					}
					foundMarker = true;
					return;
				}
				
			}
			
			for(Marker m : quakeMarkers) {
				EarthquakeMarker lnd = (EarthquakeMarker)m;
				
				if(m.isHidden() && lnd.isShallow) {
					if(!clickedLandquakeKeyMarker && !clickedOceanquakeKeyMarker) {
						System.out.println("Shallow key pressed but no other one");
						lastClicked = (CommonMarker)m;
						m.setHidden(false);
						hidden = true;
					}
					if(clickedLandquakeKeyMarker && lnd.isOnLand) {
						lastClicked = (CommonMarker)m;
						m.setHidden(false);
						hidden = true;
					}
					if(clickedOceanquakeKeyMarker && !lnd.isOnLand) {
						lastClicked = (CommonMarker)m;
						m.setHidden(false);
						hidden = true;
					}
				}
			}
				
			if(hidden) {
				allMarkersWereShown = false;
				clickedShallowKeyMarker = true;
				foundMarker = true;
				return;
			}
			
			if(!hidden) {
				for(Marker m : cityMarkers) {
					if(!clickedCityKeyMarker) {
						m.setHidden(true);
						lastClicked = (CommonMarker)m;
					}
					
				}
				for(Marker om : quakeMarkers) {
					EarthquakeMarker em = (EarthquakeMarker)om;
					if(!em.isShallow) {
						om.setHidden(true);
					}
					if(em.isShallow) {
						if(clickedLandquakeKeyMarker && !em.isOnLand && !clickedOceanquakeKeyMarker) {
							om.setHidden(true);
						}
						else if(clickedOceanquakeKeyMarker && em.isOnLand && !clickedLandquakeKeyMarker) {
							om.setHidden(true);
						}
					}
				}
				clickedShallowKeyMarker = true;
				foundMarker = true;
				allMarkersWereShown = true;
				
				return;
			}
		}
		
		
	}


	private void oceanquakeKeyClicked() {
		// TODO Auto-generated method stub
		boolean hidden = false;
		
		boolean checkX;
		boolean checkY;
		if(mouseX >= 55 && mouseX <= 65) {
			checkX = true;
		}
		else {
			checkX =false;
		}
		
		if(mouseY >= 135 && mouseY <= 145) {
			checkY = true;
		}
		else {
			checkY = false;
		}
		
		if(checkX && checkY) {
			System.out.println("Mouse click in Oceanquake Key is : "+mouseX+", "+mouseY);
			if(clickedOceanquakeKeyMarker) {
				clickedOceanquakeKeyMarker = false;
				if(allMarkersWereShown) {
					unhideMarkers();
					foundMarker = true;
					return;
				}
				else {
					for(Marker m : quakeMarkers) {
						EarthquakeMarker lnd = (EarthquakeMarker)m;
						if(!lnd.isOnLand)
						m.setHidden(true);
					}
					foundMarker = true;
					return;
				}
				
			}
			
			for(Marker m : quakeMarkers) {
				EarthquakeMarker lnd = (EarthquakeMarker)m;
				if(m.isHidden() && !lnd.isOnLand) {
					hidden = true;
					m.setHidden(false);
					lastClicked = (CommonMarker)m;
					
				}	
			}
			if(hidden) {
				allMarkersWereShown = false;
				clickedOceanquakeKeyMarker = true;
				foundMarker = true;
				return;
			}
			
			if(!hidden) {
				for(Marker m : cityMarkers) {
					m.setHidden(true);
					lastClicked = (CommonMarker)m;
				}
				for(Marker om : quakeMarkers) {
					EarthquakeMarker em = (EarthquakeMarker)om;
					if(em.isOnLand) {
						om.setHidden(true);
					}
				}
				clickedOceanquakeKeyMarker = true;
				foundMarker = true;
				allMarkersWereShown = true;
				return;
			}
		}
		
	}


	private void landquakeKeyClicked() {
		// TODO Auto-generated method stub
		boolean hidden = false;
		
		boolean checkX;
		boolean checkY;
		if(mouseX >= 55 && mouseX <= 65) {
			checkX = true;
		}
		else {
			checkX =false;
		}
		
		if(mouseY >= 110 && mouseY <= 130) {
			checkY = true;
		}
		else {
			checkY = false;
		}
		
		if(checkX && checkY) {
			System.out.println("Mouse click in Landquake Key is : "+mouseX+", "+mouseY);
			if(clickedLandquakeKeyMarker) {
				clickedLandquakeKeyMarker = false;
				if(allMarkersWereShown) {
					unhideMarkers();
					foundMarker = true;
					return;
				}
				else {
					for(Marker m : quakeMarkers) {
						EarthquakeMarker lnd = (EarthquakeMarker)m;
						if(lnd.isOnLand)
						m.setHidden(true);
					}
					foundMarker = true;
					return;
				}
				
			}
			
			for(Marker m : quakeMarkers) {
				EarthquakeMarker lnd = (EarthquakeMarker)m;
				if(m.isHidden() && lnd.isOnLand) {
					hidden = true;
					m.setHidden(false);
					lastClicked = (CommonMarker)m;
					
				}	
			}
			if(hidden) {
				allMarkersWereShown = false;
				clickedLandquakeKeyMarker = true;
				foundMarker = true;
				return;
			}
			
			if(!hidden) {
				for(Marker m : cityMarkers) {
					m.setHidden(true);
					lastClicked = (CommonMarker)m;
				}
				for(Marker om : quakeMarkers) {
					EarthquakeMarker em = (EarthquakeMarker)om;
					if(!em.isOnLand) {
						om.setHidden(true);
					}
				}
				clickedLandquakeKeyMarker = true;
				foundMarker = true;
				allMarkersWereShown = true;
				return;
			}
		}
		
	}


	private void cityKeyClicked() {
		// TODO Auto-generated method stub
		boolean hidden = false;
		
		boolean checkX;
		boolean checkY;
		if(mouseX >= 55 && mouseX <= 65) {
			checkX = true;
		}
		else {
			checkX =false;
		}
		
		if(mouseY >= 95 && mouseY <= 105) {
			checkY = true;
		}
		else {
			checkY = false;
		}
		
		if(checkX && checkY) {
			System.out.println("Mouse click in City Key is : "+mouseX+", "+mouseY);
			if(clickedCityKeyMarker) {
				clickedCityKeyMarker = false;
				if(allMarkersWereShown) {
					unhideMarkers();
					foundMarker = true;
					return;
				}
				else {
					for(Marker m : cityMarkers) {
						m.setHidden(true);
					}
					foundMarker = true;
					return;
				}
				
			}
			
			for(Marker m : cityMarkers) {
				if(m.isHidden()) {
					hidden = true;
					m.setHidden(false);
					lastClicked = (CommonMarker)m;
					
				}	
			}
			if(hidden) {
				allMarkersWereShown = false;
				clickedCityKeyMarker = true;
				foundMarker = true;
				return;
			}
			
			if(!hidden) {
				for(Marker m : quakeMarkers) {
					m.setHidden(true);
					lastClicked = (CommonMarker)m;
				}
				clickedCityKeyMarker = true;
				foundMarker = true;
				allMarkersWereShown = true;
				return;
			}
		}
		
	}


	// Helper method that will check if a city marker was clicked on
	// and respond appropriately
	private void checkCitiesForClick()
	{
		
		// Loop over the earthquake markers to see if one of them is selected
		for (Marker marker : cityMarkers) {
			if (!marker.isHidden() && marker.isInside(map, mouseX, mouseY)) {
				lastClicked = (CommonMarker)marker;
				if(clickedCityMarker) {
					foundMarker = true;
					clickedCityMarker = false;
					unhideMarkers();
					return;
				}
				// Hide all the other earthquakes and hide
				for (Marker mhide : cityMarkers) {
					if (mhide != lastClicked) {
						mhide.setHidden(true);
					}
				}
				for (Marker mhide : quakeMarkers) {
					EarthquakeMarker quakeMarker = (EarthquakeMarker)mhide;
					if (quakeMarker.getDistanceTo(marker.getLocation()) 
							> quakeMarker.threatCircle()) {
						quakeMarker.setHidden(true);
					}
				}
				foundMarker = true;
				clickedCityMarker = true;
				return;
			}
		}		
	}
	
	// Helper method that will check if an earthquake marker was clicked on
	// and respond appropriately
	private void checkEarthquakesForClick()
	{
		
		// Loop over the earthquake markers to see if one of them is selected
		for (Marker m : quakeMarkers) {
			EarthquakeMarker marker = (EarthquakeMarker)m;
			if (!marker.isHidden() && marker.isInside(map, mouseX, mouseY)) {
				lastClicked = marker;
				if(clickedEarthquakeMarker) {
					foundMarker = true;
					clickedEarthquakeMarker = false;
					unhideMarkers();
					return;
				}
				// Hide all the other earthquakes and hide
				for (Marker mhide : quakeMarkers) {
					if (mhide != lastClicked) {
						mhide.setHidden(true);
					}
				}
				for (Marker mhide : cityMarkers) {
					if (mhide.getDistanceTo(marker.getLocation()) 
							> marker.threatCircle()) {
						mhide.setHidden(true);
					}
				}
				clickedEarthquakeMarker = true;
				foundMarker = true;
				return;
			}
		}
	}
	
	// loop over and unhide all markers
	private void unhideMarkers() {
		for(Marker marker : quakeMarkers) {
			marker.setHidden(false);
		}
			
		for(Marker marker : cityMarkers) {
			marker.setHidden(false);
		}
	}
	
	// helper method to draw key in GUI
	private void addKey() {	
		// Remember you can use Processing's graphics methods here
		fill(255, 250, 240);
		
		int xbase = 25;
		int ybase = 50;
		
		rect(xbase, ybase, 150, 250);
		
		fill(0);
		textAlign(LEFT, CENTER);
		textSize(12);
		text("Earthquake Key", xbase+25, ybase+25);
		
		fill(150, 30, 30);
		int tri_xbase = xbase + 35;
		int tri_ybase = ybase + 50;
		triangle(tri_xbase, tri_ybase-CityMarker.TRI_SIZE, tri_xbase-CityMarker.TRI_SIZE, 
				tri_ybase+CityMarker.TRI_SIZE, tri_xbase+CityMarker.TRI_SIZE, 
				tri_ybase+CityMarker.TRI_SIZE);

		fill(0, 0, 0);
		textAlign(LEFT, CENTER);
		text("City Marker", tri_xbase + 15, tri_ybase);
		
		text("Land Quake", xbase+50, ybase+70);
		text("Ocean Quake", xbase+50, ybase+90);
		text("Size ~ Magnitude", xbase+25, ybase+110);
		
		fill(255, 255, 255);
		ellipse(xbase+35, 
				ybase+70, 
				10, 
				10);
		rect(xbase+35-5, ybase+90-5, 10, 10);
		
		fill(color(255, 255, 0));
		ellipse(xbase+35, ybase+140, 12, 12);
		fill(color(0, 0, 255));
		ellipse(xbase+35, ybase+160, 12, 12);
		fill(color(255, 0, 0));
		ellipse(xbase+35, ybase+180, 12, 12);
		
		textAlign(LEFT, CENTER);
		fill(0, 0, 0);
		text("Shallow", xbase+50, ybase+140);
		text("Intermediate", xbase+50, ybase+160);
		text("Deep", xbase+50, ybase+180);

		text("Past hour", xbase+50, ybase+200);
		
		fill(255, 255, 255);
		int centerx = xbase+35;
		int centery = ybase+200;
		ellipse(centerx, centery, 12, 12);

		strokeWeight(2);
		line(centerx-8, centery-8, centerx+8, centery+8);
		line(centerx-8, centery+8, centerx+8, centery-8);
		
		
	}

	
	
	// Checks whether this quake occurred on land.  If it did, it sets the 
	// "country" property of its PointFeature to the country where it occurred
	// and returns true.  Notice that the helper method isInCountry will
	// set this "country" property already.  Otherwise it returns false.
	private boolean isLand(PointFeature earthquake) {
		
		// IMPLEMENT THIS: loop over all countries to check if location is in any of them
		// If it is, add 1 to the entry in countryQuakes corresponding to this country.
		for (Marker country : countryMarkers) {
			if (isInCountry(earthquake, country)) {
				return true;
			}
		}
		
		// not inside any country
		return false;
	}
	
	// prints countries with number of earthquakes
	// You will want to loop through the country markers or country features
	// (either will work) and then for each country, loop through
	// the quakes to count how many occurred in that country.
	// Recall that the country markers have a "name" property, 
	// And LandQuakeMarkers have a "country" property set.
	private void printQuakes() {
		int totalWaterQuakes = quakeMarkers.size();
		for (Marker country : countryMarkers) {
			String countryName = country.getStringProperty("name");
			int numQuakes = 0;
			for (Marker marker : quakeMarkers)
			{
				EarthquakeMarker eqMarker = (EarthquakeMarker)marker;
				if (eqMarker.isOnLand()) {
					if (countryName.equals(eqMarker.getStringProperty("country"))) {
						numQuakes++;
					}
				}
			}
			if (numQuakes > 0) {
				totalWaterQuakes -= numQuakes;
				System.out.println(countryName + ": " + numQuakes);
			}
		}
		System.out.println("OCEAN QUAKES: " + totalWaterQuakes);
	}
	
	
	
	// helper method to test whether a given earthquake is in a given country
	// This will also add the country property to the properties of the earthquake feature if 
	// it's in one of the countries.
	// You should not have to modify this code
	private boolean isInCountry(PointFeature earthquake, Marker country) {
		// getting location of feature
		Location checkLoc = earthquake.getLocation();

		// some countries represented it as MultiMarker
		// looping over SimplePolygonMarkers which make them up to use isInsideByLoc
		if(country.getClass() == MultiMarker.class) {
				
			// looping over markers making up MultiMarker
			for(Marker marker : ((MultiMarker)country).getMarkers()) {
					
				// checking if inside
				if(((AbstractShapeMarker)marker).isInsideByLocation(checkLoc)) {
					earthquake.addProperty("country", country.getProperty("name"));
						
					// return if is inside one
					return true;
				}
			}
		}
			
		// check if inside country represented by SimplePolygonMarker
		else if(((AbstractShapeMarker)country).isInsideByLocation(checkLoc)) {
			earthquake.addProperty("country", country.getProperty("name"));
			
			return true;
		}
		return false;
	}

}
