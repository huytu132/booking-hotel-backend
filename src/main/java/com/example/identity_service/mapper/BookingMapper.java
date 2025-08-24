package com.example.identity_service.mapper;

import com.example.identity_service.dto.response.BookingResponse;
import com.example.identity_service.dto.response.BookingRoomResponse;
import com.example.identity_service.dto.response.BookingRoomAddonResponse;
import com.example.identity_service.entity.Booking;
import com.example.identity_service.entity.BookingRoomClass;
import com.example.identity_service.entity.BookingRoomClassAddon;
import com.example.identity_service.entity.Room;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.email", target = "userEmail")
    @Mapping(target = "userFullName", expression = "java(booking.getUser().getFirstName() + \" \" + booking.getUser().getLastName())")
    @Mapping(source = "bookingRoomClasses", target = "bookingRoomResponses")
//    @Mapping(source = "createdAt", target = "createAt", ignore = true)
//    @Mapping(source = "updatedAt", target = "updateAt", ignore = true)
    BookingResponse toResponse(Booking booking);

    @Mapping(source = "roomClass.id", target = "roomClassId")
    @Mapping(source = "roomClass.roomClassName", target = "roomClassName")
    @Mapping(source = "roomClass.hotel.hotelName", target = "hotelName")
//    @Mapping(target = "roomIds", expression = "java(bookingRoomClass.getRooms().stream().map(Room::getId).collect(java.util.stream.Collectors.toList()))")
    @Mapping(source = "bookingRoomClassAddons", target = "addons")
    BookingRoomResponse toBookingRoomResponse(BookingRoomClass bookingRoomClass);

    @Mapping(source = "addon.id", target = "addonId")
    @Mapping(source = "addon.addonName", target = "addonName")
    BookingRoomAddonResponse toBookingRoomAddonResponse(BookingRoomClassAddon bookingRoomClassAddon);

    List<BookingResponse> toResponseList(List<Booking> bookings);
    List<BookingRoomResponse> toBookingRoomResponseList(List<BookingRoomClass> bookingRoomClasses);
    List<BookingRoomAddonResponse> toBookingRoomAddonResponseList(List<BookingRoomClassAddon> addons);
}