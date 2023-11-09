package ru.practicum.shareit.comment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> getAllByItemOrderByCreatedDesc(Item item);

    @Query(" from Comment c" +
            " where c.item.owner = :owner")
    List<Comment> getAllByOwner(User owner);
}
