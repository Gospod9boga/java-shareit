package ru.practicum.server.item.model;

import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.server.Exception.AccessDeniedException;
import ru.practicum.server.Exception.EntityNotFoundException;
import ru.practicum.server.Exception.ValidationException;
import ru.practicum.server.booking.Booking;
import ru.practicum.server.booking.BookingRepository;
import ru.practicum.server.booking.BookingStatus;
import ru.practicum.server.booking.dto.BookingShortDto;
import ru.practicum.server.comments.Comment;
import ru.practicum.server.comments.CommentDto;
import ru.practicum.server.comments.CommentMapper;
import ru.practicum.server.comments.CommentResponseDto;
import ru.practicum.server.comments.repo.CommentsRepository;
import ru.practicum.server.item.ItemRepository;
import ru.practicum.server.item.dto.ItemDto;
import ru.practicum.server.item.dto.mapper.ItemMapper;
import ru.practicum.server.request.ItemRequestRepository;
import ru.practicum.server.user.User;
import ru.practicum.server.user.repo.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
public class ItemImplService implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentsRepository commentsRepository;
    private final ItemRequestRepository itemRequestRepository;

    public ItemImplService(ItemRepository itemRepository, UserRepository userRepository, BookingRepository bookingRepository, CommentsRepository commentsRepository, ItemRequestRepository itemRequestRepository) {

        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.commentsRepository = commentsRepository;
        this.itemRequestRepository = itemRequestRepository;
    }

    @Override
    @Transactional
    public ItemDto createItem(ItemDto itemDto, Long ownerId) {
        if (itemDto.getAvailable() == null) {
            throw new ValidationException("Поле available обязательно");
        }

        log.info("Creating item, available={}", itemDto.getAvailable());

        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Item item = ItemMapper.toEntity(itemDto,itemRequestRepository);
        item.setOwner(owner);
        Item savedItem = itemRepository.save(item);
        return ItemMapper.toDto(savedItem);
    }

    @Override
    @Transactional
    public ItemDto updateItem(Long itemId, ItemDto itemDto, Long ownerId) {
        Item existingItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new ValidationException("Item not found"));

        if (!existingItem.getOwner().getId().equals(ownerId)) {
            throw new AccessDeniedException("Only owner can update item");
        }

        if (itemDto.getName() != null) {
            existingItem.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            existingItem.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            existingItem.setIsAvailable(itemDto.getAvailable());
        }

        Item updatedItem = itemRepository.save(existingItem);
        return ItemMapper.toDto(updatedItem);
    }

    @Override
    public ItemDto getItemById(Long itemId, Long userId) {
        if (itemId == null) {
            throw new ValidationException("itemId not found");
        }
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ValidationException("Item not found"));

        ItemDto itemDto = ItemMapper.toDto(item);

        List<Comment> comments = commentsRepository.findByItemId(itemId);
        List<CommentResponseDto> commentDtos = comments.stream()
                .map(CommentMapper::toDto)
                .collect(Collectors.toList());
        itemDto.setComments(commentDtos);

        if (item.getOwner().getId().equals(userId)) {
            LocalDateTime now = LocalDateTime.now();

            List<Booking> nextBookings = bookingRepository
                    .findFutureBookingsByOwner(userId, now, BookingStatus.APPROVED);
            Booking nextBooking = nextBookings.stream()
                    .filter(b -> b.getItem().getId().equals(itemId))
                    .findFirst()
                    .orElse(null);
            if (nextBooking != null) {
                itemDto.setNextBooking(bookingToShortDto(nextBooking));
            }

            List<Booking> lastBookings = bookingRepository
                    .findPastBookingsByOwner(userId, now, BookingStatus.APPROVED);
            Booking lastBooking = lastBookings.stream()
                    .filter(b -> b.getItem().getId().equals(itemId))
                    .findFirst()
                    .orElse(null);
            if (lastBooking != null) {
                itemDto.setLastBooking(bookingToShortDto(lastBooking));
            }
        }

        return itemDto;
    }

    @Override
    public List<ItemDto> getUserItems(Long ownerId) {
        List<Item> userItems = itemRepository.findByOwnerId(ownerId);


        List<Long> itemIds = userItems.stream()
                .map(Item::getId)
                .collect(Collectors.toList());


        List<Comment> allComments = commentsRepository.findByItemIdIn(itemIds);
        Map<Long, List<Comment>> commentsByItemId = allComments.stream()
                .collect(Collectors.groupingBy(comment -> comment.getItem().getId()));


        LocalDateTime now = LocalDateTime.now();


        List<Booking> nextBookings = bookingRepository
                .findFutureBookingsByOwner(
                        ownerId, now, BookingStatus.APPROVED);


        List<Booking> lastBookings = bookingRepository
                .findPastBookingsByOwner(
                        ownerId, now, BookingStatus.APPROVED);


        Map<Long, Booking> nextBookingByItemId = nextBookings.stream()
                .collect(Collectors.toMap(
                        booking -> booking.getItem().getId(),
                        booking -> booking,
                        (existing, replacement) -> existing
                ));

        Map<Long, Booking> lastBookingByItemId = lastBookings.stream()
                .collect(Collectors.toMap(
                        booking -> booking.getItem().getId(),
                        booking -> booking,
                        (existing, replacement) -> existing
                ));

        return userItems.stream()
                .map(item -> {
                    ItemDto itemDto = ItemMapper.toDto(item);


                    List<Comment> itemComments = commentsByItemId.getOrDefault(item.getId(), List.of());
                    List<CommentResponseDto> commentDtos = itemComments.stream()
                            .map(CommentMapper::toDto)
                            .collect(Collectors.toList());
                    itemDto.setComments(commentDtos);


                    Booking nextBooking = nextBookingByItemId.get(item.getId());
                    if (nextBooking != null) {
                        itemDto.setNextBooking(bookingToShortDto(nextBooking));
                    }


                    Booking lastBooking = lastBookingByItemId.get(item.getId());
                    if (lastBooking != null) {
                        itemDto.setLastBooking(bookingToShortDto(lastBooking));
                    }

                    return itemDto;
                })
                .collect(Collectors.toList());
    }


    private BookingShortDto bookingToShortDto(Booking booking) {
        if (booking == null) return null;

        BookingShortDto dto = new BookingShortDto();
        dto.setId(booking.getId());
        dto.setBookerId(booking.getBooker().getId());
        dto.setStart(booking.getStartDate());
        dto.setEnd(booking.getEndDate());
        return dto;
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        List<Item> allItems = itemRepository.findAll();

        return allItems.stream()
                .filter(item -> Boolean.TRUE.equals(item.getIsAvailable()))
                .filter(item ->
                        item.getName().toLowerCase().contains(text.toLowerCase()) ||
                                item.getDescription().toLowerCase().contains(text.toLowerCase()))
                .map(ItemMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteItem(Long itemId, Long ownerId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ValidationException("Item not found"));

        if (!item.getOwner().getId().equals(ownerId)) {
            throw new AccessDeniedException("Only owner can delete item");
        }
        itemRepository.deleteById(itemId);
    }

    @Override
    @Transactional
    public CommentResponseDto addComment(Long itemId, CommentDto commentDto, Long userId) {
        log.info("=== ADD COMMENT DEBUG ===");
        log.info("userId: {}, itemId: {}, text: {}", userId, itemId, commentDto.getText());
        if (commentDto.getText() == null || commentDto.getText().isBlank()) {
            throw new ValidationException("Текст комментария не может быть пустым");
        }


        if (commentDto.getText().length() < 1 || commentDto.getText().length() > 1000) {
            throw new ValidationException("Длина комментария должна быть от 1 до 1000 символов");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Вещь не найдена"));

        if (commentDto.getText() == null || commentDto.getText().isBlank()) {
            throw new ValidationException("Текст комментария не может быть пустым");
        }

        List<Booking> bookings = bookingRepository.findByBookerIdAndItemId(userId, itemId);
        log.info("Found {} bookings for user {}, item {}", bookings.size(), userId, itemId);

        boolean hasAnyCompletedBooking = bookings.stream()
                .anyMatch(b -> b.getEndDate().isBefore(LocalDateTime.now()));

        boolean hasCompletedApprovedBooking = bookings.stream()
                .anyMatch(b -> b.getStatus() == BookingStatus.APPROVED
                        && b.getEndDate().isBefore(LocalDateTime.now()));

        log.info("hasAnyCompletedBooking: {}, hasCompletedApprovedBooking: {}",
                hasAnyCompletedBooking, hasCompletedApprovedBooking);

        if (!hasAnyCompletedBooking) {
            log.error("No completed booking found for comment");
            throw new ValidationException("Нельзя оставить комментарий - у вас нет завершенного бронирования этой вещи");
        }

        Comment comment = new Comment();
        comment.setText(commentDto.getText());
        comment.setItem(item);
        comment.setAuthor(user);

        Comment saved = commentsRepository.save(comment);
        log.info("Comment created with id: {}", saved.getId());

        return CommentMapper.toDto(saved);
    }
}
