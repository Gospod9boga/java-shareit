package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("SELECT b FROM Booking b WHERE b.item.id IN :itemIds ORDER BY b.startDate DESC")
    List<Booking> findByItemIdInOrderByStartDateDesc(@Param("itemIds") List<Long> itemIds);

    @Query("SELECT b FROM Booking b WHERE b.booker.id = :bookerId ORDER BY b.startDate DESC")
    List<Booking> findByBookerIdOrderByStartDateDesc(@Param("bookerId") Long bookerId);


    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.owner.id = :ownerId " +
            "AND b.startDate > :now " +
            "AND b.status = :status " +
            "ORDER BY b.startDate ASC")
    List<Booking> findFutureBookingsByOwner(
            @Param("ownerId") Long ownerId,
            @Param("now") LocalDateTime now,
            @Param("status") BookingStatus status);


    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.owner.id = :ownerId " +
            "AND b.endDate < :now " +
            "AND b.status = :status " +
            "ORDER BY b.endDate DESC")
    List<Booking> findPastBookingsByOwner(
            @Param("ownerId") Long ownerId,
            @Param("now") LocalDateTime now,
            @Param("status") BookingStatus status);

    @Query("SELECT b FROM Booking b WHERE b.booker.id = :bookerId AND b.item.id = :itemId")
    List<Booking> findByBookerIdAndItemId(@Param("bookerId") Long bookerId,
                                          @Param("itemId") Long itemId);

}

