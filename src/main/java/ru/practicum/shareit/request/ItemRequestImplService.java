package ru.practicum.shareit.request;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.Exception.ValidationException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ItemRequestImplService implements ItemRequestService {
    private final Map<Long, ItemRequest> requests = new HashMap<>();
    private Long currentId = 0L;

    @Override
    public ItemRequest createRequest(ItemRequest request, Long userId) {
        request.setId(currentId);
        request.setRequesterId(userId);
        request.setCreated(LocalDateTime.now());
        request.setItems(new ArrayList<>());

        requests.put(currentId, request);
        currentId++;
        return request;
    }

    @Override
    public List<ItemRequest> getUserRequests(Long userId) {
        return requests.values().stream()
                .filter(request -> request.getRequesterId().equals(userId))
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequest> getAllRequests(Long userId, int from, int size) {
        return requests.values().stream()
                .filter(request -> !request.getRequesterId().equals(userId))
                .skip(from)
                .limit(size)
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequest getRequestById(Long requestId, Long userId) {
        ItemRequest request = requests.get(requestId);
        if (request == null) {
            throw new ValidationException("Request not found");
        }
        return request;
    }
}
