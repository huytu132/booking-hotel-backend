package com.example.identity_service.mapper;

import com.example.identity_service.dto.response.*;
import com.example.identity_service.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.email", target = "userEmail")
    @Mapping(target = "userFullName", expression = "java(booking.getUser().getFirstName() + \" \" + booking.getUser().getLastName())")
    @Mapping(source = "bookingRooms", target = "bookingRooms")
    BookingResponse toResponse(Booking booking);

    @Mapping(source = "room.id", target = "roomId")
    @Mapping(source = "room.roomNumber", target = "roomNumber")
    @Mapping(source = "room.roomClass.roomClassName", target = "roomClassName")
    @Mapping(source = "room.roomClass.hotel.hotelName", target = "hotelName")
    @Mapping(source = "bookingRoomAddons", target = "addons")
    BookingRoomResponse toBookingRoomResponse(BookingRoom bookingRoom);

    @Mapping(source = "addon.id", target = "addonId")
    @Mapping(source = "addon.addonName", target = "addonName")
    BookingRoomAddonResponse toBookingRoomAddonResponse(BookingRoomAddon bookingRoomAddon);



    List<BookingResponse> toResponseList(List<Booking> bookings);
    List<BookingRoomResponse> toBookingRoomResponseList(List<BookingRoom> bookingRooms);
    List<BookingRoomAddonResponse> toBookingRoomAddonResponseList(List<BookingRoomAddon> addons);
}