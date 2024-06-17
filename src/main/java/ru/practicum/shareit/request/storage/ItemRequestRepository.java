package ru.practicum.shareit.request.storage;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

@Repository
public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {
    List<ItemRequest> findAllByRequesterId(Long requesterId);

    @Query("select i " +
            "from ItemRequest i " +
            "where i.requester.id != ?1 " +
            "order by i.created DESC")
    List<ItemRequest> findAllByNotRequesterId(Long userId, Pageable pageable);
}

