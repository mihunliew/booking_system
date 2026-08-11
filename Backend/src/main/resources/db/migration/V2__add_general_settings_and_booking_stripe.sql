-- Create general_settings table
CREATE TABLE general_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    setting_type VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    provider_key VARCHAR(50),
    icon VARCHAR(50),
    description VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Seed initial payment methods
INSERT INTO general_settings (setting_type, name, provider_key, icon, description, is_active)
VALUES
('PAYMENT_METHOD', 'Credit / Debit Card', 'card', '💳', 'Instant authorization via Visa/Mastercard', TRUE),
('PAYMENT_METHOD', 'FPX Online Banking', 'fpx', '🏦', 'Direct bank transfer via FPX portal', TRUE),
('PAYMENT_METHOD', 'e-Wallet (GrabPay)', 'grabpay', '📱', 'Scan & pay with GrabPay', TRUE);

-- Update bookings table
ALTER TABLE bookings
ADD COLUMN stripe_session_id VARCHAR(255),
ADD COLUMN stripe_payment_intent_id VARCHAR(255),
ADD COLUMN amount_paid DECIMAL(10, 2);
