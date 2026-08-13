package com.n2n.booking.util;

import com.n2n.booking.entity.Product;
import com.n2n.booking.exception.BadRequestException;

public class ProductValidationUtil {

    public static void validateProductAvailability(Product product) {
        if (product == null || product.getStatus() == null) {
            return;
        }
        String status = product.getStatus().trim().toUpperCase();
        if ("UNAVAILABLE".equals(status)) {
            throw new BadRequestException("Sorry, '" + product.getName() + "' is currently unavailable. Please remove it from your cart.");
        } else if ("MAINTENANCE".equals(status)) {
            throw new BadRequestException("Sorry, '" + product.getName() + "' is currently undergoing maintenance. Please remove it from your cart.");
        } else if (!"AVAILABLE".equals(status)) {
            throw new BadRequestException("Sorry, '" + product.getName() + "' is currently not available for booking (status: " + product.getStatus() + "). Please remove it from your cart.");
        }
    }
}
