package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Page<Booking> findAllByBooker(User booker, Pageable pageable);

    Page<Booking> findAllByBookerAndStartIsBeforeAndEndIsAfter(User booker, LocalDateTime start, LocalDateTime end, Pageable pageable);

    Page<Booking> findAllByBookerAndEndIsBefore(User booker, LocalDateTime end, Pageable pageable);

    Page<Booking> findAllByBookerAndStartIsAfter(User booker, LocalDateTime end, Pageable pageable);

    Page<Booking> findAllByBookerAndStatus(User booker, BookingStatus status, Pageable pageable);

    Booking findFirstByItemAndStatusAndStartIsAfterOrderByStart(Item item, BookingStatus status, LocalDateTime now);

    Booking findFirstByItemAndStatusAndStartIsBeforeOrderByEndDesc(Item item, BookingStatus approved, LocalDateTime now);

    List<Booking> findAllByItemAndBookerAndStatusAndEndIsBefore(Item item, User author, BookingStatus approved, LocalDateTime now);

    List<Booking> findAllByItemInOrderByStartDesc(List<Item> items);

    @Query(" from Booking b" +
            " where b.item.owner = :owner")
    Page<Booking> findAllByItemOwner(User owner, Pageable pageable);

    @Query(" from Booking b" +
            " where b.item.owner = :owner" +
            " and b.start < :now" +
            " and b.end > :now")
    Page<Booking> findAllByItemOwnerCurrent(User owner, LocalDateTime now, Pageable pageable);

    @Query(" from Booking b" +
            " where b.item.owner = :owner" +
            " and b.end < :now")
    Page<Booking> findAllByItemOwnerPast(User owner, LocalDateTime now, Pageable pageable);

    @Query(" from Booking b" +
            " where b.item.owner = :owner" +
            " and b.start > :now")
    Page<Booking> findAllByItemOwnerFuture(User owner, LocalDateTime now, Pageable pageable);

    @Query(" from Booking b" +
            " where b.item.owner = :owner" +
            " and b.status = :status")
    Page<Booking> findAllByItemOwnerByStatus(User owner, BookingStatus status, Pageable pageable);
}
