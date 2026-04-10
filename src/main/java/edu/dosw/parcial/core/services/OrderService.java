package edu.dosw.parcial.core.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.dosw.parcial.dto.request.CreateOrderItemRequest;
import edu.dosw.parcial.dto.request.CreateOrderRequest;
import edu.dosw.parcial.dto.response.OrderItemResponse;
import edu.dosw.parcial.dto.response.OrderResponse;
import edu.dosw.parcial.core.models.Order;
import edu.dosw.parcial.core.models.OrderItem;
import edu.dosw.parcial.core.models.OrderStatus;
import edu.dosw.parcial.core.models.Product;
import edu.dosw.parcial.core.models.ProductStatus;
import edu.dosw.parcial.core.models.User;
import edu.dosw.parcial.core.models.UserRole;
import edu.dosw.parcial.core.repositories.OrderRepository;
import edu.dosw.parcial.core.repositories.ProductRepository;
import edu.dosw.parcial.core.repositories.UserRepository;
import edu.dosw.parcial.core.utils.BusinessException;
import edu.dosw.parcial.core.utils.ErrorCodes;

@Service
public class OrderService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public OrderService(UserRepository userRepository, ProductRepository productRepository, OrderRepository orderRepository) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        Long userId = Objects.requireNonNull(request.getUserId(), "El usuario es obligatorio");

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND,
                        ErrorCodes.RESOURCE_NOT_FOUND,
                        "El usuario no existe"));

        if (user.getRole() != UserRole.CLIENTE) {
            throw new BusinessException(
                    HttpStatus.FORBIDDEN,
                    ErrorCodes.USER_ROLE_NOT_ALLOWED,
                    "Solo los usuarios con rol CLIENTE pueden crear pedidos");
        }

        boolean hasActiveOrder = orderRepository.existsByUserIdAndStatusIn(
                user.getId(),
                List.of(OrderStatus.CREADO, OrderStatus.EN_PREPARACION));

        if (hasActiveOrder) {
            throw new BusinessException(
                    HttpStatus.CONFLICT,
                    ErrorCodes.ACTIVE_ORDER_ALREADY_EXISTS,
                    "El usuario ya tiene un pedido activo");
        }

        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.CREADO);

        BigDecimal total = BigDecimal.ZERO;
        for (CreateOrderItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findByQrCode(itemRequest.getQrCode().trim())
                    .orElseThrow(() -> new BusinessException(
                            HttpStatus.NOT_FOUND,
                            ErrorCodes.RESOURCE_NOT_FOUND,
                            "Producto no encontrado para QR: " + itemRequest.getQrCode()));

            if (product.getStatus() != ProductStatus.DISPONIBLE) {
                throw new BusinessException(
                        HttpStatus.CONFLICT,
                        ErrorCodes.PRODUCT_NOT_AVAILABLE,
                        "El producto no esta disponible: " + product.getName());
            }

            if (product.getStock() < itemRequest.getQuantity()) {
                throw new BusinessException(
                        HttpStatus.CONFLICT,
                        ErrorCodes.INSUFFICIENT_STOCK,
                        "No hay stock suficiente para: " + product.getName());
            }

            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            total = total.add(subtotal);

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setUnitPrice(product.getPrice());
            orderItem.setSubtotal(subtotal);
            order.addItem(orderItem);

            // Se descuenta stock en el momento de confirmar la creacion del pedido.
            product.setStock(product.getStock() - itemRequest.getQuantity());
            productRepository.save(product);
        }

        order.setTotal(total);
        Order savedOrder = orderRepository.save(order);

        List<OrderItemResponse> itemResponses = new ArrayList<>();
        for (OrderItem item : savedOrder.getItems()) {
            itemResponses.add(new OrderItemResponse(
                    item.getProduct().getQrCode(),
                    item.getProduct().getName(),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.getSubtotal()));
        }

        return new OrderResponse(
                savedOrder.getId(),
                savedOrder.getUser().getId(),
                savedOrder.getStatus(),
                savedOrder.getTotal(),
                savedOrder.getCreatedAt(),
                itemResponses);
    }
}
