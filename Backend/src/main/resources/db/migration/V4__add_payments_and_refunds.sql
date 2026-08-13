-- V4__add_payments_and_refunds.sql
ALTER TABLE bookings
ADD COLUMN refunded_amount DECIMAL(10, 2) DEFAULT 0.00;
