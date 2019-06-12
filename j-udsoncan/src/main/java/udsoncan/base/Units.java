package udsoncan.base;

/**
 * Units defined in standard. Nowhere does the ISO-14229 make use of them, but
 * they are defined
 */
public class Units {

	class Prefixs {
		class Prefix {
			public String name;
			public int id;
			public String symbol;
			public String description;

			public Prefix(int id, String name, String symbol, String description) {
				this.name = name;
				this.id = id;
				this.symbol = symbol;
				this.description = description;
			}

			@Override
			public String toString() {
				StringBuilder builder = new StringBuilder();
				builder.append(name);
				return builder.toString();
			}
		}

		public final Prefix exa = new Prefix(0x40, "exa", "E", "10e18");
		public final Prefix peta = new Prefix(0x41, "peta", "P", "10e15");
		public final Prefix tera = new Prefix(0x42, "tera", "T", "10e12");
		public final Prefix giga = new Prefix(0x43, "giga", "G", "10e9");
		public final Prefix mega = new Prefix(0x44, "mega", "M", "10e6");
		public final Prefix kilo = new Prefix(0x45, "kilo", "k", "10e3");
		public final Prefix hecto = new Prefix(0x46, "hecto", "h", "10e2");
		public final Prefix deca = new Prefix(0x47, "deca", "da", "10e1");
		public final Prefix deci = new Prefix(0x48, "deci", "d", "10e-1");
		public final Prefix centi = new Prefix(0x49, "centi", "c", "10e-2");
		public final Prefix milli = new Prefix(0x4A, "milli", "m", "10e-3");
		public final Prefix micro = new Prefix(0x4B, "micro", "m", "10e-6");
		public final Prefix nano = new Prefix(0x4C, "nano", "n", "10e-9");
		public final Prefix pico = new Prefix(0x4D, "pico", "p", "10e-12");
		public final Prefix femto = new Prefix(0x4E, "femto", "f", "10e-15");
		public final Prefix atto = new Prefix(0x4F, "atto", "a", "10e-18");
	}

	class Unit {
		public int id;
		public String name;
		public String symbol;
		public String description;

		public Unit(int id, String name, String symbol, String description) {
			this.name = name;
			this.id = id;
			this.symbol = symbol;
			this.description = description;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append(name);
			return builder.toString();
		}

	}

	public final Unit no_unit = new Unit(0x00, "no unit", "-", "-");
	public final Unit meter = new Unit(0x01, "meter", "m", "length");
	public final Unit foor = new Unit(0x02, "foot", "ft", "length");
	public final Unit inch = new Unit(0x03, "inch", "in", "length");
	public final Unit yard = new Unit(0x04, "yard", "yd", "length");
	public final Unit english_mile = new Unit(0x05, "mile (English)", "mi", "length");
	public final Unit gram = new Unit(0x06, "gram", "g", "mass");
	public final Unit metric_ton = new Unit(0x07, "ton (metric)", "t", "mass");
	public final Unit second = new Unit(0x08, "second", "s", "time");
	public final Unit minute = new Unit(0x09, "minute", "min", "time");
	public final Unit hour = new Unit(0x0A, "hour", "h", "time");
	public final Unit day = new Unit(0x0B, "day", "d", "time");
	public final Unit year = new Unit(0x0C, "year", "y", "time");
	public final Unit ampere = new Unit(0x0D, "ampere", "A", "current");
	public final Unit volt = new Unit(0x0E, "volt", "V", "voltage");
	public final Unit coulomb = new Unit(0x0F, "coulomb", "C", "electric charge");
	public final Unit ohm = new Unit(0x10, "ohm", "W", "resistance");
	public final Unit farad = new Unit(0x11, "farad", "F", "capacitance");
	public final Unit henry = new Unit(0x12, "henry", "H", "inductance");
	public final Unit siemens = new Unit(0x13, "siemens", "S", "electric conductance");
	public final Unit weber = new Unit(0x14, "weber", "Wb", "magnetic flux");
	public final Unit tesla = new Unit(0x15, "tesla", "T", "magnetic flux density");
	public final Unit kelvin = new Unit(0x16, "kelvin", "K", "thermodynamic temperature");
	public final Unit Celsius = new Unit(0x17, "Celsius", "°C", "thermodynamic temperature");
	public final Unit Fahrenheit = new Unit(0x18, "Fahrenheit", "°F", "thermodynamic temperature");
	public final Unit candela = new Unit(0x19, "candela", "cd", "luminous intensity");
	public final Unit radian = new Unit(0x1A, "radian", "rad", "plane angle");
	public final Unit degree = new Unit(0x1B, "degree", "°", "plane angle");
	public final Unit hertz = new Unit(0x1C, "hertz", "Hz", "frequency");
	public final Unit joule = new Unit(0x1D, "joule", "J", "energy");
	public final Unit Newton = new Unit(0x1E, "Newton", "N", "force");
	public final Unit kilopond = new Unit(0x1F, "kilopond", "kp", "force");
	public final Unit pound = new Unit(0x20, "pound force", "lbf", "force");
	public final Unit watt = new Unit(0x21, "watt", "W", "power");
	public final Unit horsem = new Unit(0x22, "horse power (metric)", "hk", "power");
	public final Unit horse = new Unit(0x23, "horse power(UK and US)", "hp", "power");
	public final Unit Pascal = new Unit(0x24, "Pascal", "Pa", "pressure");
	public final Unit bar = new Unit(0x25, "bar", "bar", "pressure");
	public final Unit atmosphere = new Unit(0x26, "atmosphere", "atm", "pressure");
	public final Unit psi = new Unit(0x27, "pound force per square inch", "psi", "pressure");
	public final Unit becqerel = new Unit(0x28, "becqerel", "Bq", "radioactivity");
	public final Unit lumen = new Unit(0x29, "lumen", "lm", "light flux");
	public final Unit lux = new Unit(0x2A, "lux", "lx", "illuminance");
	public final Unit liter = new Unit(0x2B, "liter", "l", "volume");
	public final Unit gallonb = new Unit(0x2C, "gallon (British)", "-", "volume");
	public final Unit gallon = new Unit(0x2D, "gallon (US liq)", "-", "volume");
	public final Unit cubic = new Unit(0x2E, "cubic inch", "cu in", "volume");
	public final Unit meter_per_sec = new Unit(0x2F, "meter per seconds", "m/s", "speed");
	public final Unit kmh = new Unit(0x30, "kilometre per hour", "km/h", "speed");
	public final Unit mph = new Unit(0x31, "mile per hour", "mph", "speed");
	public final Unit rps = new Unit(0x32, "revolutions per second", "rps", "angular velocity");
	public final Unit rpm = new Unit(0x33, "revolutions per minute", "rpm", "angular velocity");
	public final Unit counts = new Unit(0x34, "counts", "-", "-");
	public final Unit percent = new Unit(0x35, "percent", "%", "-");
	public final Unit mg_per_stroke = new Unit(0x36, "milligram per stroke", "mg/stroke", "mass per engine stroke");
	public final Unit meter_per_sec2 = new Unit(0x37, "meter per square seconds", "m/s2", "acceleration");
	public final Unit Nm = new Unit(0x38, "Newton meter", "Nm", "moment");
	public final Unit liter_per_min = new Unit(0x39, "liter per minute", "l/min", "flow");
	public final Unit watt_per_meter2 = new Unit(0x3A, "watt per square meter", "W/m2", "intensity");
	public final Unit bar_per_sec = new Unit(0x3B, "bar per second", "bar/s", "pressure change");
	public final Unit radians_per_sec = new Unit(0x3C, "radians per second", "rad/s", "angular velocity");
	public final Unit radians = new Unit(0x3D, "radians square second", "rad/s2", "angular acceleration");
	public final Unit kilogram_per_meter2 = new Unit(0x3E, "kilogram per square meter", "kg/m2", "-");
	public final Unit date1 = new Unit(0x50, "Date1", "-", "Year-Month-Day");
	public final Unit date2 = new Unit(0x51, "Date2", "-", "Day/Month/Year");
	public final Unit date3 = new Unit(0x52, "Date3", "-", "Month/Day/Year");
	public final Unit week = new Unit(0x53, "week", "W", "calendar week");
	public final Unit time1 = new Unit(0x54, "Time1", "-", "UTC Hour/Minute/Second");
	public final Unit time2 = new Unit(0x55, "Time2", "-", "Hour/Minute/Second");
	public final Unit datetime1 = new Unit(0x56, "DateAndTime1", "-", "Second/Minute/Hour/Day/Month/Year");
	public final Unit datetime2 = new Unit(0x57, "DateAndTime2", "-",
			"Second/Minute/Hour/Day/Month/Year/Local minute offset/Localhour offset");
	public final Unit datetime3 = new Unit(0x58, "DateAndTime3", "-", "Second/Minute/Hour/Month/Day/Year");
	public final Unit datetime4 = new Unit(0x59, "DateAndTime4", "-",
			"Second/Minute/Hour/Month/Day/Year/Local minute offset/Localhour offset");

}
