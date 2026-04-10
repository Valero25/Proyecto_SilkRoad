package edu.dosw.parcial.core.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.dosw.parcial.core.models.Order;
import edu.dosw.parcial.core.models.OrderStatus;

public interface OrderRepository extends JpaRepository<Order, Long> {

    boolean existsByUserIdAndStatusIn(Long userId, List<OrderStatus> statuses);
}
