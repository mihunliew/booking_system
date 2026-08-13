package com.n2n.booking.service;

import com.n2n.booking.entity.Booking;
import com.n2n.booking.entity.BookingItem;
import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class StripeService {

    @Value("${stripe.secret-key}")
    private String stripeSecretKey;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    public Session createCheckoutSession(Booking booking, String providerKey) throws Exception {
        List<SessionCreateParams.LineItem> sessionItems = new ArrayList<>();

        for (BookingItem item : booking.getItems()) {
            SessionCreateParams.LineItem sessionItem = SessionCreateParams.LineItem.builder()
                    .setPriceData(
                            SessionCreateParams.LineItem.PriceData.builder()
                                    .setCurrency("myr")
                                    .setUnitAmount(item.getPrice().multiply(new BigDecimal(100)).longValue()) // Stripe uses cents
                                    .setProductData(
                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                    .setName(item.getProductName())
                                                    .build()
                                    )
                                    .build()
                    )
                    .setQuantity((long) item.getQuantity())
                    .build();
            sessionItems.add(sessionItem);
        }

        SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(frontendUrl + "/bookings/" + booking.getId() + "?success=true")
                .setCancelUrl(frontendUrl + "/checkout?canceled=true")
                .putMetadata("bookingId", booking.getId().toString())
                .addAllLineItem(sessionItems);

        if (providerKey != null && !providerKey.isEmpty()) {
            if ("card".equals(providerKey)) {
                paramsBuilder.addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD);
            } else if ("fpx".equals(providerKey)) {
                paramsBuilder.addPaymentMethodType(SessionCreateParams.PaymentMethodType.FPX);
            } else if ("grabpay".equals(providerKey)) {
                paramsBuilder.addPaymentMethodType(SessionCreateParams.PaymentMethodType.GRABPAY);
            }
        }

        // Apply promo code discount if present
        if (booking.getDiscountAmount() != null && booking.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
            long discountInCents = booking.getDiscountAmount().multiply(new BigDecimal(100)).longValue();
            String promoName = booking.getPromoCode() != null ? booking.getPromoCode() : "PROMO";

            com.stripe.param.CouponCreateParams couponParams = com.stripe.param.CouponCreateParams.builder()
                    .setAmountOff(discountInCents)
                    .setCurrency("myr")
                    .setName("Promo Code Discount (" + promoName + ")")
                    .setDuration(com.stripe.param.CouponCreateParams.Duration.ONCE)
                    .build();

            com.stripe.model.Coupon coupon = com.stripe.model.Coupon.create(couponParams);

            paramsBuilder.addDiscount(
                    SessionCreateParams.Discount.builder()
                            .setCoupon(coupon.getId())
                            .build()
            );
        }

        return Session.create(paramsBuilder.build());
    }

    public com.stripe.model.Refund processRefund(String stripePaymentIntentId, BigDecimal amount) throws Exception {
        if (stripePaymentIntentId == null || stripePaymentIntentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Stripe PaymentIntent ID is missing for this booking");
        }

        long amountInCents = amount.multiply(new BigDecimal(100)).longValue();
        com.stripe.param.RefundCreateParams params = com.stripe.param.RefundCreateParams.builder()
                .setPaymentIntent(stripePaymentIntentId)
                .setAmount(amountInCents)
                .build();

        return com.stripe.model.Refund.create(params);
    }
}
