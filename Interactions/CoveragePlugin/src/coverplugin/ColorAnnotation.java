/* FeatureIDE - A Framework for Feature-Oriented Software Development
 * Copyright (C) 2005-2013  FeatureIDE team, University of Magdeburg, Germany
 *
 * This file is part of FeatureIDE.
 * 
 * FeatureIDE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * FeatureIDE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with FeatureIDE.  If not, see <http://www.gnu.org/licenses/>.
 *
 * See http://www.fosd.de/featureide/ for further information.
 */
package coverplugin;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;

/**
 * One class for all color annotations
 * 
 * @author Jens Meinicke
 */
public class ColorAnnotation extends Annotation {

	enum ANNOTATON_COLOR {
		lightgreen, darkgreen;
	}

	/**
	 * The number of defined colors for interactions
	 */
	private static final int MAX_INTERACTION = 10; 
	private static final int MIN_INTERACTION = -10; 
	
	private static final String PREFIX = "coverageplugin.";
	public static int base = 0;
	private final Position position;
	private final int id;
	private String text;
	private String type;

	public ColorAnnotation(int colorID, Position posistion, String type, String text) {
		super(getTypeString(colorID), false, "Color Annotation");
		this.position = posistion;
		this.id = colorID;
		this.text = text;
		this.type = type;
	}

	private static String getTypeString(int colorID) {
		ANNOTATON_COLOR[] values = ANNOTATON_COLOR.values();
		if (colorID < 0) {
			int referenceNumber = colorID < MIN_INTERACTION ? MIN_INTERACTION : colorID;
			return (PREFIX  + "interaction_" + referenceNumber).intern();
		}

		if (values.length > (colorID - base)) {
			return (PREFIX + values[colorID - base].name()).intern();
		}
		
		int referenceNumber = (colorID - values.length - base);
		if (referenceNumber > MAX_INTERACTION) {
			referenceNumber = MAX_INTERACTION;
		}
		return (PREFIX  + "interaction_" + referenceNumber).intern();
		
	}

	public Position getPosition() {
		return position;
	}

	public void updateOffset(int deltaOffset) {
		position.offset += deltaOffset;
	}

	public void updateLength(int deltaLength) {
		position.length += deltaLength;
	}

	public String getText() {
		return type + " " + id + "\n" + text;
	}

	public String getId() {
		return Integer.toString(id - base).intern();
	}
}
