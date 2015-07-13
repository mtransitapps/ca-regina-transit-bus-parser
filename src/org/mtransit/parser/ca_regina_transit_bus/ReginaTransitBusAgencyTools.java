package org.mtransit.parser.ca_regina_transit_bus;

import java.util.HashSet;
import java.util.Locale;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.Utils;
import org.mtransit.parser.gtfs.data.GCalendar;
import org.mtransit.parser.gtfs.data.GCalendarDate;
import org.mtransit.parser.gtfs.data.GRoute;
import org.mtransit.parser.gtfs.data.GSpec;
import org.mtransit.parser.gtfs.data.GTrip;
import org.mtransit.parser.mt.data.MAgency;
import org.mtransit.parser.mt.data.MDirectionType;
import org.mtransit.parser.mt.data.MRoute;
import org.mtransit.parser.CleanUtils;
import org.mtransit.parser.mt.data.MTrip;

// http://openregina.cloudapp.net/
// https://opengis.regina.ca/reginagtfs/google_transit.zip
public class ReginaTransitBusAgencyTools extends DefaultAgencyTools {

	public static void main(String[] args) {
		if (args == null || args.length == 0) {
			args = new String[3];
			args[0] = "input/gtfs.zip";
			args[1] = "../../mtransitapps/ca-regina-transit-bus-android/res/raw/";
			args[2] = ""; // files-prefix
		}
		new ReginaTransitBusAgencyTools().start(args);
	}

	private HashSet<String> serviceIds;

	@Override
	public void start(String[] args) {
		System.out.printf("\nGenerating Regina Transit bus data...");
		long start = System.currentTimeMillis();
		this.serviceIds = extractUsefulServiceIds(args, this);
		super.start(args);
		System.out.printf("\nGenerating Regina Transit bus data... DONE in %s.\n", Utils.getPrettyDuration(System.currentTimeMillis() - start));
	}

	@Override
	public boolean excludeCalendar(GCalendar gCalendar) {
		if (this.serviceIds != null) {
			return excludeUselessCalendar(gCalendar, this.serviceIds);
		}
		return super.excludeCalendar(gCalendar);
	}

	@Override
	public boolean excludeCalendarDate(GCalendarDate gCalendarDates) {
		if (this.serviceIds != null) {
			return excludeUselessCalendarDate(gCalendarDates, this.serviceIds);
		}
		return super.excludeCalendarDate(gCalendarDates);
	}

	@Override
	public boolean excludeTrip(GTrip gTrip) {
		if (this.serviceIds != null) {
			return excludeUselessTrip(gTrip, this.serviceIds);
		}
		return super.excludeTrip(gTrip);
	}

	@Override
	public Integer getAgencyRouteType() {
		return MAgency.ROUTE_TYPE_BUS;
	}

	@Override
	public long getRouteId(GRoute gRoute) {
		return Long.parseLong(gRoute.route_short_name); // using route short name as route ID
	}

	@Override
	public String getRouteLongName(GRoute gRoute) {
		return CleanUtils.cleanLabel(gRoute.route_long_name.toLowerCase(Locale.ENGLISH));
	}

	private static final String AGENCY_COLOR_BLUE = "0AB0DE"; // LIGHT BLUE (from PDF schedule)

	private static final String AGENCY_COLOR = AGENCY_COLOR_BLUE;

	@Override
	public String getAgencyColor() {
		return AGENCY_COLOR;
	}

	private static final String WEST = "west";
	private static final String EAST = "east";

	private static final String DIEPPE = "Dieppe";
	private static final String ARGYLE_PARK = "Argyle Park";
	private static final String GLENCAIRN = "Glencairn";
	private static final String EAST_THS = "East";
	private static final String DOWNTOWN = "Downtown";

	@Override
	public void setTripHeadsign(MRoute mRoute, MTrip mTrip, GTrip gTrip, GSpec gtfs) {
		if (mRoute.id == 1l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(DIEPPE, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 2l) {
			if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(ARGYLE_PARK, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 21l) {
			if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(GLENCAIRN, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 40l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignDirection(MDirectionType.SOUTH);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignDirection(MDirectionType.NORTH);
				return;
			}
		} else if (mRoute.id == 50l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(EAST_THS, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(DOWNTOWN, gTrip.direction_id);
				return;
			}
		}
		String gTripHeadsignLC = gTrip.trip_headsign.toLowerCase(Locale.ENGLISH);
		if (gTripHeadsignLC.endsWith(EAST)) {
			mTrip.setHeadsignDirection(MDirectionType.EAST);
			return;
		} else if (gTripHeadsignLC.endsWith(WEST)) {
			mTrip.setHeadsignDirection(MDirectionType.WEST);
			return;
		}
		mTrip.setHeadsignString(cleanTripHeadsign(gTripHeadsignLC), gTrip.direction_id);
	}

	private static final Pattern EXPRESS = Pattern.compile("(express)", Pattern.CASE_INSENSITIVE);

	@Override
	public String cleanTripHeadsign(String tripHeadsign) {
		tripHeadsign = EXPRESS.matcher(tripHeadsign).replaceAll(StringUtils.EMPTY);
		return CleanUtils.cleanLabel(tripHeadsign);
	}

	private static final Pattern ENDS_WITH_BOUND = Pattern.compile("([\\s]*\\([\\s]*[s|e|w|n]b\\)$)", Pattern.CASE_INSENSITIVE);

	private static final Pattern AT_SIGN = Pattern.compile("([\\s]*@[\\s]*)", Pattern.CASE_INSENSITIVE);
	private static final String AT_SIGN_REPLACEMENT = " / ";

	@Override
	public String cleanStopName(String gStopName) {
		gStopName = gStopName.toLowerCase(Locale.ENGLISH);
		gStopName = ENDS_WITH_BOUND.matcher(gStopName).replaceAll(StringUtils.EMPTY);
		gStopName = AT_SIGN.matcher(gStopName).replaceAll(AT_SIGN_REPLACEMENT);
		gStopName = CleanUtils.cleanStreetTypes(gStopName);
		gStopName = CleanUtils.cleanNumbers(gStopName);
		return CleanUtils.cleanLabel(gStopName);
	}
}
