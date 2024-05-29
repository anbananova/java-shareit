package ru.practicum.shareit.item.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    Optional<Item> findByIdAndOwner(Long itemId, Long userId);

    List<Item> findAllByOwner(Long userId);

    @Query(" select i from Item i " +
            "where (upper(i.name) like upper(concat('%', ?1, '%')) " +
            "   or upper(i.description) like upper(concat('%', ?1, '%')))" +
            "   and is_available = true")
    List<Item> searchItems(String text);

    @Modifying(clearAutomatically = true)
    @Query("update Item i " +
            "set i.available = :available " +
            "where i.id = :id")
    void updateItemAvailableById(@Param("id") Long id, @Param("available") Boolean available);
}
