package ru.practicum.shareit.request;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.Exception.ValidationException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.repo.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemRequestImplService implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;

    public ItemRequestImplService(ItemRequestRepository itemRequestRepository,
                                  UserRepository userRepository) {
        this.itemRequestRepository = itemRequestRepository;
        this.userRepository = userRepository;
    }

    @Override
    public ItemRequest createRequest(ItemRequest request, Long userId) {

        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new ValidationException("Пользователь не найден"));

        request.setRequester(requester);
        request.setCreated(LocalDateTime.now());

        if (request.getItems() == null) {
            request.setItems(new ArrayList<>());
        }

        return itemRequestRepository.save(request);
    }

    @Override
    public List<ItemRequest> getUserRequests(Long userId) {
        return itemRequestRepository.findByRequesterIdOrderByCreatedDesc(userId);
    }

    @Override
    public List<ItemRequest> getAllRequests(Long userId, int from, int size) {
        return itemRequestRepository.findByRequesterIdNotOrderByCreatedDesc(userId)
                .stream()
                .skip(from)
                .limit(size)
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequest getRequestById(Long requestId, Long userId) {
        return itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new ValidationException("Запрос не найден"));
    }
}