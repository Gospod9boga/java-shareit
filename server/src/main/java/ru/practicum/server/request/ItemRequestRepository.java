package ru.practicum.server.request;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {
    List<ItemRequest> findByRequesterIdOrderByCreatedDesc(Long requesterId);

    List<ItemRequest> findByRequesterIdNotOrderByCreatedDesc(Long requesterId);

    @Query("SELECT DISTINCT ir FROM ItemRequest ir " +
            "LEFT JOIN FETCH ir.items " +
            "WHERE ir.requester.id = :requesterId " +
            "ORDER BY ir.created DESC")
    List<ItemRequest> findByRequesterIdWithItems(@Param("requesterId") Long requesterId);

    @Query("SELECT DISTINCT ir FROM ItemRequest ir " +
            "LEFT JOIN FETCH ir.items " +
            "WHERE ir.requester.id != :requesterId " +
            "ORDER BY ir.created DESC")
    List<ItemRequest> findByRequesterIdNotWithItems(@Param("requesterId") Long requesterId);

    @Query("SELECT ir FROM ItemRequest ir " +
            "LEFT JOIN FETCH ir.items " +
            "WHERE ir.id = :requestId")
    Optional<ItemRequest> findByIdWithItems(@Param("requestId") Long requestId);
}
