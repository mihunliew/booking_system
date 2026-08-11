# N2N Booking System

This project contains both the frontend and backend for the N2N Booking System.

## Prerequisites

- **Node.js** (v18+) for Frontend
- **Java** (v21) for Backend
- **MySQL** Database
- **Stripe CLI** for local webhook testing
- **VS Code** with the following extensions (recommended for Backend):
  - **Extension Pack for Java** (by Microsoft)
  - **Spring Boot Extension Pack** (by VMware)

## 1. Running the Backend (Spring Boot)

The backend is a Java Spring Boot application. It handles the API and database communication.

1. Open your terminal and navigate to the backend folder:
   ```bash
   cd c:\N2N\Backend
   ```
2. Make sure your MySQL database is running and configured correctly in `application.properties` or `.env`.
3. Start the application:
   - If using VS Code, you can run it directly from the **Spring Boot Dashboard** or by clicking **Run** on `BookingApplication.java`.
   - If using Maven from the terminal, run:
     ```bash
     mvn spring-boot:run
     ```
4. The backend will start on `http://localhost:8080`.

## 2. Running the Frontend (Vue.js + Vite)

The frontend is built with Vue 3 and Vite.

1. Open a **new** terminal window and navigate to the frontend folder:
   ```bash
   cd c:\N2N\Frontend
   ```
2. Install the dependencies (only needed the first time):
   ```bash
   npm install
   ```
3. Start the development server:
   ```bash
   npm run dev
   ```
4. The frontend will start on `http://localhost:5173`.

## 3. Running Stripe (Local Webhooks)

To test payments locally, you must forward Stripe webhooks to your local backend so your database can update the payment status.

1. Open a **new** terminal window.
2. Login to your Stripe CLI (if you haven't already):
   ```bash
   stripe login
   ```
3. Run the following command to listen and forward events to your Backend (Port 8080):
   ```bash
   stripe listen --forward-to localhost:8080/api/stripe/webhook
   ```
4. The terminal will output a `whsec_...` key. Make sure this key matches the `STRIPE_WEBHOOK_SECRET` in your Backend's `.env` file! If it doesn't, update the `.env` file and restart your Backend.
