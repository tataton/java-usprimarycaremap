package com.tataton;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.GeoJSONReader;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.providers.OpenStreetMap;
import de.fhpotsdam.unfolding.utils.MapUtils;
import processing.core.PApplet;

public class USStateData extends PApplet {
	
	private static final Location USA_CENTER = new Location(38f, -96.5f);
	private static final long serialVersionUID = 1L;

	UnfoldingMap map;
	Map<String, Float> pcpRatioByState2010;
	Map<String, Float> pcpRatioByState2016;
	List<Feature> states;
	List<Marker> stateMarkers;
	
	int white = color(255, 255, 255);
	int black = color(0, 0, 0);
	
	public void setup() {
		size(800, 500, OPENGL);
		map = new UnfoldingMap(this, 50, 50, 700, 400, new OpenStreetMap.OpenStreetMapProvider());
		MapUtils.createDefaultEventDispatcher(this, map);

		// Load primary care physician data
		pcpRatioByState2010 = loadStateDataFromCSV("PCPData2010.csv");
		pcpRatioByState2016 = loadStateDataFromCSV("PCPData2016.csv");

		// Load state polygons and add them as markers
		states = GeoJSONReader.loadData(this, "usstates.geo.json");
		stateMarkers = MapUtils.createSimpleMarkers(states);
		map.addMarkers(stateMarkers);
		
		// State markers are shaded according to relative gain or loss of
		// primary care physicians
		shadeStates();
		map.zoomLevel(3);
		map.panTo(USA_CENTER);
	}
	
	public void draw() {
		// Draw map tiles and state markers
		map.draw();
		addKey();
	}
	
	private void shadeStates() {
		for (Marker marker : stateMarkers) {
			// Find name of state of the current marker. File has title case names.
			String stateName = marker.getProperty("NAME").toString();
			float pcpChange = pcpRatioByState2016.get(stateName) / pcpRatioByState2010.get(stateName);

			// Encode value as brightness (values range: 0.4-0.9)
			float colorFraction = map(pcpChange, 0.5f, 0.8f, 0, 1);
			println(colorFraction);
			int stateColor = lerpColor(black, white, colorFraction);
			marker.setColor(stateColor);
		}
	}
	
	private void addKey() {	
		fill(white);
		rect(600, 335, 150, 115);
		fill(black);
		textAlign(CENTER, CENTER);
		text("Percent decrease in", 675, 350);
		text("primary care physicians,", 675, 365);
		text("2010-2016", 675, 380);
		text("-20%", 632, 410);
		text("-50%", 718, 410);
		horizontalGradientLine(630, 720, 430, 10, white, black);
		stroke(black);
		rect(630, 425, 90, 10);
	}
	
	private void horizontalGradientLine(int x1, int x2, int y, int linewidth, int color1, int color2) {
		for (int i = x1; i < x2; i++) {
			float intermediate = map(i, x1, x2, 0, 1);
			int c = lerpColor(color1, color2, intermediate);
			stroke(c);
			line(i, (y - (linewidth/2)), i, (y + (linewidth/2)));
		}
	}
	
	private Map<String, Float> loadStateDataFromCSV(String fileName) {
		Map<String, Float> stateDataMap = new HashMap<String, Float>();

		String[] rows = loadStrings(fileName);
		for (String row : rows) {
			String[] columns = row.split(",");
			stateDataMap.put(columns[0], Float.parseFloat(columns[3]));
		}
		return stateDataMap;
	}

}