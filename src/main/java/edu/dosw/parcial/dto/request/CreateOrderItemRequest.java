package edu.dosw.parcial.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateOrderItemRequest {

    @NotBlank(message = "El codigo QR es obligatorio")
    @Size(max = 80, message = "El codigo QR no puede superar 80 caracteres")
    private String qrCode;

    @Min(value = 1, message = "La cantidad debe ser mayor o igual a 1")
    private int quantity;

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
