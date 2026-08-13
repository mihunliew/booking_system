package com.n2n.booking.controller;

import com.n2n.booking.entity.Booking;
import com.n2n.booking.enums.BookingStatus;
import com.n2n.booking.enums.PaymentStatus;
import com.n2n.booking.repository.BookingRepository;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Optional;

@RestController
@RequestMapping("/api/stripe")
@RequiredArgsConstructor
public class StripeWebhookController {

    private final BookingRepository bookingRepository;
    private final com.n2n.booking.repository.ProductSlotHoldRepository productSlotHoldRepository;

    @Value("${stripe.webhook-secret}")
    private String endpointSecret;

    @PostMapping("/webhook")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<String> handleStripeEvent(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        Event event;

        try {
            event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
        } catch (SignatureVerificationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Signature verification failed.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Payload verification failed.");
        }

        if ("checkout.session.completed".equals(event.getType())) {
            System.out.println("Received checkout.session.completed event!");
            EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
            
            StripeObject stripeObject = dataObjectDeserializer.getObject().orElse(null);
            if (stripeObject == null) {
                System.out.println("dataObjectDeserializer.getObject() is empty! Using unsafe deserialization.");
                try {
                    stripeObject = dataObjectDeserializer.deserializeUnsafe();
                } catch (Exception e) {
                    System.out.println("Error during deserializeUnsafe: " + e.getMessage());
                }
            }

            if (stripeObject instanceof Session) {
                Session session = (Session) stripeObject;
                if (session.getMetadata() == null) {
                    System.out.println("Error: session.getMetadata() is null! Cannot get bookingId.");
                } else {
                    String bId = session.getMetadata().get("bookingId");
                    System.out.println("Processing session for bookingId: " + bId);
                    handleCheckoutSessionCompleted(session);
                }
            } else {
                System.out.println("StripeObject is not a Session! It is: " + (stripeObject != null ? stripeObject.getClass().getName() : "null"));
            }
        } else {
            System.out.println("Unhandled event type: " + event.getType());
        }

        return ResponseEntity.ok("Success");
    }

    private void handleCheckoutSessionCompleted(Session session) {
        String bookingIdStr = session.getMetadata().get("bookingId");
        if (bookingIdStr != null) {
            Long bookingId = Long.parseLong(bookingIdStr);
            Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
            
            if (bookingOpt.isPresent()) {
                Booking booking = bookingOpt.get();
                booking.setPaymentStatus(PaymentStatus.PAID);
                booking.setStatus(BookingStatus.CONFIRMED);
                booking.setStripePaymentIntentId(session.getPaymentIntent());
                
                if (session.getAmountTotal() != null) {
                    booking.setAmountPaid(new BigDecimal(session.getAmountTotal()).divide(new BigDecimal(100)));
                }

                if (booking.getUser() != null) {
                    productSlotHoldRepository.deleteByUserId(booking.getUser().getId());
                }

                bookingRepository.save(booking);
            }
        }
    }
}
