package ru.practicum.shareit.booking.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("select b " +
            "from Booking as b " +
            "join b.item as i " +
            "join b.booker as u " +
            "where i.id = ?1 and u.id = ?2 " +
            "and b.status = ?3 " +
            "and b.end < current_timestamp")
    List<Booking> findAllByItemIdAndBookerId(Long itemId, Long userId, BookingStatus status);

    List<Booking> findAllByBookerIdOrderByStartDesc(Long userId);

    List<Booking> findAllByBookerIdAndStatusOrderByStartDesc(Long userId, BookingStatus status);

    @Query("select b " +
            "from Booking as b " +
            "join b.item as i " +
            "where i.owner = ?1 " +
            "order by b.start desc")
    List<Booking> findAllByOwnerIdOrderByStartDesc(Long userId);


    @Query("select b " +
            "from Booking as b " +
            "join b.item as i " +
            "where i.id = ?1 and i.owner = ?2")
    List<Booking> findAllByItemIdAndOwnerId(Long itemId, Long userId);

    @Query("select b " +
            "from Booking as b " +
            "join b.item as i " +
            "where i.owner = ?1 " +
            "and b.status = ?2 " +
            "order by b.start desc")
    List<Booking> findAllByOwnerIdAndStatusOrderByStartDesc(Long userId, BookingStatus status);

    @Modifying
    @Query("update Booking b " +
            "set b.status = ?2 " +
            "where b.id = ?1")
    void updateBookingStatusById(Long id, BookingStatus status);
}
