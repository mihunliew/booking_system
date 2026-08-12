-- V3__add_promocodes.sql
CREATE TABLE promo_codes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    discount_type VARCHAR(20) NOT NULL,
    discount_value DECIMAL(10,2) NOT NULL,
    min_spend DECIMAL(10,2) DEFAULT 0.00,
    max_discount DECIMAL(10,2) DEFAULT NULL,
    usage_limit INT DEFAULT NULL,
    used_count INT NOT NULL DEFAULT 0,
    start_date TIMESTAMP NULL,
    end_date TIMESTAMP NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE promo_code_reservations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reservation_token VARCHAR(64) NOT NULL UNIQUE,
    promo_code_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    discount_amount DECIMAL(10,2) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (promo_code_id) REFERENCES promo_codes(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

ALTER TABLE bookings
ADD COLUMN promo_code VARCHAR(50) DEFAULT NULL,
ADD COLUMN subtotal_amount DECIMAL(10, 2) DEFAULT 0.00,
ADD COLUMN discount_amount DECIMAL(10, 2) DEFAULT 0.00;

-- Seed a sample promo code for testing
INSERT INTO promo_codes (code, discount_type, discount_value, min_spend, max_discount, usage_limit, is_active)
VALUES ('SUMMER10', 'PERCENTAGE', 10.00, 50.00, 100.00, 10, TRUE),
       ('FLAT20', 'FIXED_AMOUNT', 20.00, 100.00, NULL, 5, TRUE);
