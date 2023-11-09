package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.Request;
import ru.practicum.shareit.request.dto.RequestDto;
import ru.practicum.shareit.request.dto.RequestDtoExtended;
import ru.practicum.shareit.request.dto.RequestMapper;
import ru.practicum.shareit.request.repository.RequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final RequestMapper requestMapper;
    private final ItemMapper itemMapper;

    @Override
    public RequestDto add(RequestDto requestDto, Long userId) {
        log.info("Создание запроса на вещь");
        User requestor = userRepository.findById(userId).orElse(null);
        if (requestor == null) {
            log.error("Не найден пользователь {} для создания запроса!", userId);
            throw new NotFoundException(String.format("Не найден пользователь %d для создания запроса!", userId));
        }
        requestDto.setCreated(LocalDateTime.now(ZoneId.of("Europe/Moscow")));
        return requestMapper.toInDto(requestRepository.saveAndFlush(requestMapper.fromDto(requestDto, requestor)));
    }

    @Override
    public RequestDtoExtended getById(Long requestId, Long userId) {
        log.info("Получение запроса {} пользователем {}", requestId, userId);
        User requestor = userRepository.findById(userId).orElse(null);
        if (requestor == null) {
            log.error("Не найден пользователь {} для получения запроса!", userId);
            throw new NotFoundException(String.format("Не найден пользователь %d для получения запроса!", userId));
        }
        Request request = requestRepository.findById(requestId).orElse(null);
        if (request == null) {
            log.error("Не найден запрос по идентификатору {}", requestId);
            throw new NotFoundException(String.format("Не найден запрос по идентификатору %d!", requestId));
        }
        List<ItemDto> items = itemRepository.findAllByRequest(request).stream().map(itemMapper::toDto).collect(Collectors.toList());
        return requestMapper.toOutDto(request, items);
    }

    @Override
    public List<RequestDtoExtended> getAllByUserId(Long userId) {
        User requestor = userRepository.findById(userId).orElse(null);
        if (requestor == null) {
            log.error("Не найден пользователь {} для получения запросов!", userId);
            throw new NotFoundException(String.format("Не найден пользователь %d для получения запросов!", userId));
        }
        List<Request> requests = requestRepository.findAllByRequestorOrderByCreatedDesc(requestor);
        List<Item> items = itemRepository.findAllByRequestor(requestor);
        return requests.stream().map(request -> {
            List<ItemDto> requestItems = items.stream()
                    .filter(item -> Objects.equals(item.getRequest().getId(), request.getId()))
                    .map(itemMapper::toDto)
                    .collect(Collectors.toList());
            return requestMapper.toOutDto(request, requestItems);
        }).collect(Collectors.toList());
    }

    @Override
    public List<RequestDtoExtended> getAll(Long userId, Integer from, Integer size) {
        User requestor = userRepository.findById(userId).orElse(null);
        if (requestor == null) {
            log.error("Не найден пользователь {} для получения запросов!", userId);
            throw new NotFoundException(String.format("Не найден пользователь %d для получения запросов!", userId));
        }
        if (from == null || size == null)
            return new ArrayList<>();
        if (from < 0) {
            log.error("Значение номера первого элемента должно быть неотрицательно! Текущее значение номера {}", from);
            throw new ValidationException(String.format("\"Значение номера первого элемента должно быть неотрицательно! Текущее значение номера  %d", from));
        }
        if (size <= 0) {
            log.error("Значение размера страницы должны быть положительно! Текущее значение размера {}", size);
            throw new ValidationException(String.format("Значение размера страницы должны быть положительно! Текущее значение размера %d", size));
        }
        int pageNum = from / size;
        Pageable pageable = PageRequest.of(pageNum, size, Sort.by("created").descending());
        Page<Request> requests = requestRepository.findAllByRequestorIsNot(requestor, pageable);
        List<Item> items = itemRepository.findAllByRequestIn(requests.toList());
        return requests.stream().map(request -> {
            List<ItemDto> requestItems = items.stream()
                    .filter(item -> Objects.equals(item.getRequest().getId(), request.getId()))
                    .map(itemMapper::toDto)
                    .collect(Collectors.toList());
            return requestMapper.toOutDto(request, requestItems);
        }).collect(Collectors.toList());
    }
}
