-- V5__add_inventory_and_slot_holds.sql
ALTER TABLE products
ADD COLUMN stock_quantity INT NOT NULL DEFAULT 10;

CREATE TABLE product_slot_holds (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    booking_date DATE NOT NULL,
    quantity INT NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT uk_user_product_date UNIQUE (user_id, product_id, booking_date)
);
