package ru.practicum.server.request;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.server.Exception.ValidationException;
import ru.practicum.server.user.User;
import ru.practicum.server.user.repo.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ItemRequestImplService implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;

    public ItemRequestImplService(ItemRequestRepository itemRequestRepository,
                                  UserRepository userRepository) {
        this.itemRequestRepository = itemRequestRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
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
        return itemRequestRepository.findByRequesterIdWithItems(userId);
    }

    @Override
    public List<ItemRequest> getAllRequests(Long userId, int from, int size) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ValidationException("Пользователь не найден"));
        return itemRequestRepository.findByRequesterIdNotWithItems(userId)
                .stream()
                .skip(from)
                .limit(size)
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequest getRequestById(Long requestId, Long userId) {
        return itemRequestRepository.findByIdWithItems(requestId)
                .orElseThrow(() -> new ValidationException("Запрос не найден"));
    }
}