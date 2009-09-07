package de.d3web.we.kdom.renderer;

import java.util.HashMap;
import java.util.Map;

public class FontColorRenderer extends StyleRenderer {
	
	public static String COLOR1 = "color:rgb(40, 40, 160)";
	public static String COLOR2 = "color:rgb(255, 0, 0)";
	public static String COLOR3 = "color:rgb(0, 128, 0)";
	public static String COLOR4 = "color:rgb(200, 0, 200)";
	public static String COLOR5 = "color:rgb(128, 128, 0)";
	public static String COLOR6 = "color:rgb(0, 0, 255)";
	public static String COLOR7 = "color:rgb(255, 0, 102)";
	public static String COLOR8 = "color:rgb(0, 0, 102)";
	
	private static Map<String, FontColorRenderer> renderers = new HashMap<String, FontColorRenderer>();
	
	public static FontColorRenderer getRenderer(String color) {
		if(renderers.containsKey(color)) {
			return renderers.get(color);
		}else {
			renderers.put(color, new FontColorRenderer(color));
			return renderers.get(color);
		}
	}

	public FontColorRenderer(String s) {
		super(s);
	}
	
	public String getColor() {
		return this.style;
	}

}
