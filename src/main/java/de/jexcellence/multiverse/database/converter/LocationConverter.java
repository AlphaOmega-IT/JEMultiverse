package de.jexcellence.multiverse.database.converter;

import de.jexcellence.jeplatform.utility.serializer.LocationSerializer;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.bukkit.Location;

@Converter(autoApply = true)
public class LocationConverter implements AttributeConverter<Location, String> {

  @Override
  public String convertToDatabaseColumn(Location location) {
    if (
            location == null
    ) return null;

    return new LocationSerializer().getStringFromLocation(location);
  }

  @Override
  public Location convertToEntityAttribute(String s) {
    if (
            s == null || s.isEmpty()
    ) return null;

    return new LocationSerializer().getLocationFromString(s);
  }
}
