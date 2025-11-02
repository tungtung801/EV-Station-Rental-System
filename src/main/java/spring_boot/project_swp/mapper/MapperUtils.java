package spring_boot.project_swp.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import spring_boot.project_swp.entity.Booking;
import spring_boot.project_swp.entity.Discount;
import spring_boot.project_swp.entity.Location;
import spring_boot.project_swp.entity.Rental;
import spring_boot.project_swp.entity.Station;
import spring_boot.project_swp.entity.User;
import spring_boot.project_swp.entity.Vehicle;
import spring_boot.project_swp.entity.VehicleChecks;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.IGNORE)
public class MapperUtils {

  public Rental mapToRental(Long rentalId) {
    if (rentalId == null) {
      return null;
    }
    return Rental.builder().rentalId(rentalId).build();
  }

  public Vehicle mapToVehicle(Long vehicleId) {
    if (vehicleId == null) {
      return null;
    }
    return Vehicle.builder().vehicleId(vehicleId).build();
  }

  public User mapToUser(Long userId) {
    if (userId == null) {
      return null;
    }
    return User.builder().userId(userId).build();
  }

  public VehicleChecks mapToVehicleChecks(Long checkId) {
    if (checkId == null) {
      return null;
    }
    return VehicleChecks.builder().checkId(checkId).build();
  }

  public Location mapToLocation(Long locationId) {
    if (locationId == null) {
      return null;
    }
    return Location.builder().locationId(locationId).build();
  }

  public Discount mapToDiscount(Long discountId) {
    if (discountId == null) {
      return null;
    }
    return Discount.builder().discountId(discountId).build();
  }

  public Booking mapToBooking(Long bookingId) {
    if (bookingId == null) {
      return null;
    }
    return Booking.builder().bookingId(bookingId).build();
  }

  public Station mapToStation(Long stationId) {
    if (stationId == null) {
      return null;
    }
    return Station.builder().stationId(stationId).build();
  }
}
