package se.urvantsev.digpro.location;

import java.util.Optional;

public record Location(long x, long y, String name) {
	public static Optional<Location> parse(String line) {
		var parameters = line.split("[\\s,]+");
		if (parameters.length != 3) {
			return Optional.empty();
		}

		try {
			var x = Long.parseLong(parameters[0]);
			var y = Long.parseLong(parameters[1]);
			return Optional.of(new Location(x, y, parameters[2]));
		}
		catch (NumberFormatException ignored) {
			return Optional.empty();
		}
	}
}
