package edu.dosw.parcial.core.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.dosw.parcial.core.models.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByQrCode(String qrCode);
}
