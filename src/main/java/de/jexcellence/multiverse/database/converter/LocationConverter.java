package de.jexcellence.multiverse.database.converter;

import de.jexcellence.jeplatform.utility.serializer.LocationSerializer;
import jakarta.persistence.AttributeConverter;
import org.bukkit.Location;

public class LocationConverter implements AttributeConverter<Location, String> {

  @Override
  public String convertToDatabaseColumn(Location location) {
    return new LocationSerializer().getStringFromLocation(location);
  }

  @Override
  public Location convertToEntityAttribute(String s) {
    return new LocationSerializer().getLocationFromString(s);
  }
}
