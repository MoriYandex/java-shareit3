package ru.practicum.shareit.item.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.Request;
import ru.practicum.shareit.user.User;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    Page<Item> findAllByOwner(User owner, Pageable pageable);

    List<Item> findAllByRequest(Request request);

    List<Item> findAllByRequestIn(List<Request> requests);

    @Query(" from Item i" +
            " where i.available = :available" +
            " and (upper(i.name) like upper(concat('%', :text, '%'))" +
            " or upper(i.description) like upper(concat('%', :text, '%')))")
    Page<Item> searchByText(boolean available, String text, Pageable pageable);

    @Query(" from Item i" +
            " where i.request.requestor = :requestor")
    List<Item> findAllByRequestor(User requestor);
}
