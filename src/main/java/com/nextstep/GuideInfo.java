package com.nextstep;

/** What the overlay needs to show directions. Immutable. */
public class GuideInfo
{
	final String name;
	final String locationName;
	final String travelTip;
	final String directions; // may be null (no coordinates, or unknown)

	GuideInfo(String name, String locationName, String travelTip, String directions)
	{
		this.name = name;
		this.locationName = locationName;
		this.travelTip = travelTip;
		this.directions = directions;
	}
}
