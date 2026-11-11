package com.hospital.web;

import com.hospital.database.DatabaseConnection;
import com.hospital.exception.ValidationException;
import com.hospital.model.Appointment;
import com.hospital.model.Bill;
import com.hospital.model.Doctor;
import com.hospital.model.MedicalRecord;
import com.hospital.model.Patient;
import com.hospital.model.Person;
import com.hospital.model.User;
import com.hospital.service.AppointmentService;
import com.hospital.service.AuthService;
import com.hospital.service.BillingService;
import com.hospital.service.DoctorService;
import com.hospital.service.MedicalRecordService;
import com.hospital.service.PatientService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Built-in Lightweight HTTP Web Server.
 * Serves a responsive web interface and REST API on http://localhost:8080.
 * Reuses the exact same OOPS Models, Services, and JDBC DAOs.
 */
public class WebServer {

    private static final Logger LOGGER = Logger.getLogger(WebServer.class.getName());
    private static final int DEFAULT_PORT = 8080;

    private final int port;
    private HttpServer server;

    private final AuthService authService;
    private final PatientService patientService;
    private final DoctorService doctorService;
    private final AppointmentService appointmentService;
    private final BillingService billingService;
    private final MedicalRecordService medicalRecordService;

    public WebServer() {
        this(DEFAULT_PORT);
    }

    public WebServer(int port) {
        this.port = port;
        this.authService = new AuthService();
        this.patientService = new PatientService();
        this.doctorService = new DoctorService();
        this.appointmentService = new AppointmentService();
        this.billingService = new BillingService();
        this.medicalRecordService = new MedicalRecordService();
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);

        // Static Web UI Handlers
        server.createContext("/", new StaticFileHandler());

        // REST API Handlers
        server.createContext("/api/login", new LoginHandler());
        server.createContext("/api/overview", new OverviewHandler());
        server.createContext("/api/patients", new PatientApiHandler());
        server.createContext("/api/doctors", new DoctorApiHandler());
        server.createContext("/api/appointments", new AppointmentApiHandler());
        server.createContext("/api/bills", new BillingApiHandler());
        server.createContext("/api/records", new MedicalRecordApiHandler());
        server.createContext("/api/directory", new DirectoryApiHandler());
        server.createContext("/api/users", new UserApiHandler());

        server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
        server.start();

        System.out.println("================================================================================");
        System.out.println("   HOSPITAL MANAGEMENT SYSTEM - WEB APPLICATION SERVER RUNNING                  ");
        System.out.println("================================================================================");
        System.out.println("   Localhost Web URL: http://localhost:" + port + "/");
        System.out.println("   Backend Storage   : MySQL 8.x via JDBC (DatabaseConnection)");
        System.out.println("   Demo Accounts     : admin / admin123  |  receptionist / recep123");
        System.out.println("================================================================================");
        System.out.println("   Press Ctrl+C to stop the web server.");
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            LOGGER.info("Web server stopped.");
        }
    }

    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {
            }
        }

        try {
            DatabaseConnection.loadConfiguration();
            WebServer webServer = new WebServer(port);
            webServer.start();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to start web server on port " + port, e);
        }
    }

    // -------------------------------------------------------------------------
    // Helper Methods
    // -------------------------------------------------------------------------

    private static void sendJsonResponse(HttpExchange exchange, int statusCode, String jsonResponse) throws IOException {
        byte[] bytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buf = new byte[1024];
            int read;
            while ((read = is.read(buf)) != -1) {
                baos.write(buf, 0, read);
            }
            return new String(baos.toByteArray(), StandardCharsets.UTF_8);
        }
    }

    private static Map<String, String> parseQueryParams(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.isEmpty()) return map;
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            if (idx > 0) {
                String key = java.net.URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
                String val = java.net.URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
                map.put(key, val);
            }
        }
        return map;
    }

    // -------------------------------------------------------------------------
    // Static HTML / CSS / JS Handler
    // -------------------------------------------------------------------------

    private static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path == null || path.equals("/") || path.isEmpty()) {
                path = "/index.html";
            }

            // Load from resource directory or file system
            byte[] data = null;
            String resourcePath = "web" + path;
            try (InputStream is = WebServer.class.getClassLoader().getResourceAsStream(resourcePath)) {
                if (is != null) {
                    data = readAllBytes(is);
                }
            }

            if (data == null) {
                File file = new File("src/main/resources/web" + path);
                if (file.exists() && !file.isDirectory()) {
                    try (FileInputStream fis = new FileInputStream(file)) {
                        data = readAllBytes(fis);
                    }
                }
            }

            if (data != null) {
                String mime = "text/html; charset=UTF-8";
                if (path.endsWith(".css")) mime = "text/css; charset=UTF-8";
                else if (path.endsWith(".js")) mime = "application/javascript; charset=UTF-8";
                else if (path.endsWith(".json")) mime = "application/json; charset=UTF-8";

                exchange.getResponseHeaders().set("Content-Type", mime);
                exchange.sendResponseHeaders(200, data.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(data);
                }
            } else {
                String notFound = "<h1>404 Not Found</h1><p>Path: " + path + "</p>";
                exchange.sendResponseHeaders(404, notFound.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(notFound.getBytes(StandardCharsets.UTF_8));
                }
            }
        }

        private byte[] readAllBytes(InputStream is) throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int n;
            while ((n = is.read(buffer)) != -1) {
                baos.write(buffer, 0, n);
            }
            return baos.toByteArray();
        }
    }

    // -------------------------------------------------------------------------
    // API: Login Handler
    // -------------------------------------------------------------------------

    private class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 204, "");
                return;
            }
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}");
                return;
            }

            try {
                String body = readRequestBody(exchange);
                Map<String, String> data = SimpleJson.parseFlatObject(body);
                String user = data.get("username");
                String pass = data.get("password");

                User authUser = authService.login(user, pass);
                Map<String, Object> resp = new LinkedHashMap<>();
                resp.put("success", true);
                resp.put("username", authUser.getUsername());
                resp.put("role", authUser.getRole());
                resp.put("fullName", authUser.getFullName());
                sendJsonResponse(exchange, 200, SimpleJson.toJson(resp));
            } catch (Exception e) {
                Map<String, Object> err = new LinkedHashMap<>();
                err.put("success", false);
                err.put("error", e.getMessage());
                sendJsonResponse(exchange, 401, SimpleJson.toJson(err));
            }
        }
    }

    // -------------------------------------------------------------------------
    // API: Overview KPIs Handler
    // -------------------------------------------------------------------------

    private class OverviewHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("patientsCount", patientService.getTotalPatientCount());
            data.put("doctorsCount", doctorService.getTotalDoctorCount());
            data.put("todayAppointments", appointmentService.getTodayAppointmentCount());
            data.put("pendingBills", billingService.getPendingBillsCount());
            data.put("totalRevenue", billingService.getTotalRevenue().doubleValue());

            List<Appointment> recent = appointmentService.getAllAppointments();
            List<Map<String, Object>> recentList = new ArrayList<>();
            int limit = 0;
            for (Appointment a : recent) {
                if (limit++ >= 10) break;
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("id", a.getId());
                item.put("patientName", a.getPatientName());
                item.put("doctorName", a.getDoctorName());
                item.put("specialization", a.getDoctorSpecialization());
                item.put("date", a.getAppointmentDate().toString());
                item.put("time", a.getAppointmentTime().toString());
                item.put("status", a.getStatus());
                recentList.add(item);
            }
            data.put("recentAppointments", recentList);

            sendJsonResponse(exchange, 200, SimpleJson.toJson(data));
        }
    }

    // -------------------------------------------------------------------------
    // API: Patients Handler (GET, POST, PUT, DELETE)
    // -------------------------------------------------------------------------

    private class PatientApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod().toUpperCase();
            if ("OPTIONS".equals(method)) {
                sendJsonResponse(exchange, 204, "");
                return;
            }

            try {
                if ("GET".equals(method)) {
                    Map<String, String> query = parseQueryParams(exchange.getRequestURI().getQuery());
                    String q = query.get("query");
                    List<Patient> list;
                    if (q != null && !q.trim().isEmpty()) {
                        try {
                            list = patientService.search(Integer.parseInt(q.trim()));
                        } catch (NumberFormatException e) {
                            list = patientService.search(q.trim());
                        }
                    } else {
                        list = patientService.getAllPatients();
                    }

                    List<Map<String, Object>> jsonList = new ArrayList<>();
                    for (Patient p : list) {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("id", p.getId());
                        m.put("name", p.getName());
                        m.put("age", p.getAge());
                        m.put("gender", p.getGender());
                        m.put("phone", p.getPhone());
                        m.put("address", p.getAddress());
                        m.put("medicalHistory", p.getMedicalHistory());
                        m.put("registeredOn", p.getRegisteredOn() != null ? p.getRegisteredOn().toString() : "");
                        jsonList.add(m);
                    }
                    sendJsonResponse(exchange, 200, SimpleJson.toJson(jsonList));
                } else if ("POST".equals(method)) {
                    Map<String, String> body = SimpleJson.parseFlatObject(readRequestBody(exchange));
                    Patient p = new Patient(
                            body.get("name"),
                            Integer.parseInt(body.get("age")),
                            body.get("gender"),
                            body.get("phone"),
                            body.get("address"),
                            body.get("medicalHistory")
                    );
                    Patient saved = patientService.registerPatient(p);
                    sendJsonResponse(exchange, 200, "{\"success\":true,\"id\":" + saved.getId() + "}");
                } else if ("PUT".equals(method)) {
                    Map<String, String> body = SimpleJson.parseFlatObject(readRequestBody(exchange));
                    Patient p = new Patient(
                            Integer.parseInt(body.get("id")),
                            body.get("name"),
                            Integer.parseInt(body.get("age")),
                            body.get("gender"),
                            body.get("phone"),
                            body.get("address"),
                            body.get("medicalHistory"),
                            null
                    );
                    patientService.updatePatient(p);
                    sendJsonResponse(exchange, 200, "{\"success\":true}");
                } else if ("DELETE".equals(method)) {
                    Map<String, String> query = parseQueryParams(exchange.getRequestURI().getQuery());
                    int id = Integer.parseInt(query.get("id"));
                    patientService.deletePatient(id);
                    sendJsonResponse(exchange, 200, "{\"success\":true}");
                }
            } catch (Exception e) {
                sendJsonResponse(exchange, 400, "{\"error\":\"" + SimpleJson.escape(e.getMessage()) + "\"}");
            }
        }
    }

    // -------------------------------------------------------------------------
    // API: Doctors Handler (GET, POST, PUT, DELETE)
    // -------------------------------------------------------------------------

    private class DoctorApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod().toUpperCase();
            if ("OPTIONS".equals(method)) {
                sendJsonResponse(exchange, 204, "");
                return;
            }

            try {
                if ("GET".equals(method)) {
                    Map<String, String> query = parseQueryParams(exchange.getRequestURI().getQuery());
                    String q = query.get("query");
                    String availOnly = query.get("availableOnly");

                    List<Doctor> list;
                    if ("true".equalsIgnoreCase(availOnly)) {
                        list = doctorService.getAvailableDoctors();
                    } else if (q != null && !q.trim().isEmpty()) {
                        try {
                            list = doctorService.search(Integer.parseInt(q.trim()));
                        } catch (NumberFormatException e) {
                            list = doctorService.search(q.trim());
                        }
                    } else {
                        list = doctorService.getAllDoctors();
                    }

                    List<Map<String, Object>> jsonList = new ArrayList<>();
                    for (Doctor d : list) {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("id", d.getId());
                        m.put("name", d.getName());
                        m.put("age", d.getAge());
                        m.put("gender", d.getGender());
                        m.put("specialization", d.getSpecialization());
                        m.put("consultationFee", d.getConsultationFee().doubleValue());
                        m.put("phone", d.getPhone());
                        m.put("address", d.getAddress());
                        m.put("available", d.isAvailable());
                        jsonList.add(m);
                    }
                    sendJsonResponse(exchange, 200, SimpleJson.toJson(jsonList));
                } else if ("POST".equals(method)) {
                    Map<String, String> body = SimpleJson.parseFlatObject(readRequestBody(exchange));
                    Doctor d = new Doctor(
                            0,
                            body.get("name"),
                            Integer.parseInt(body.get("age")),
                            body.get("gender"),
                            body.get("phone"),
                            body.get("address"),
                            body.get("specialization"),
                            new BigDecimal(body.get("consultationFee")),
                            Boolean.parseBoolean(body.get("available"))
                    );
                    Doctor saved = doctorService.registerDoctor(d);
                    sendJsonResponse(exchange, 200, "{\"success\":true,\"id\":" + saved.getId() + "}");
                } else if ("PUT".equals(method)) {
                    Map<String, String> body = SimpleJson.parseFlatObject(readRequestBody(exchange));
                    Doctor d = new Doctor(
                            Integer.parseInt(body.get("id")),
                            body.get("name"),
                            Integer.parseInt(body.get("age")),
                            body.get("gender"),
                            body.get("phone"),
                            body.get("address"),
                            body.get("specialization"),
                            new BigDecimal(body.get("consultationFee")),
                            Boolean.parseBoolean(body.get("available"))
                    );
                    doctorService.updateDoctor(d);
                    sendJsonResponse(exchange, 200, "{\"success\":true}");
                } else if ("DELETE".equals(method)) {
                    Map<String, String> query = parseQueryParams(exchange.getRequestURI().getQuery());
                    int id = Integer.parseInt(query.get("id"));
                    doctorService.deleteDoctor(id);
                    sendJsonResponse(exchange, 200, "{\"success\":true}");
                }
            } catch (Exception e) {
                sendJsonResponse(exchange, 400, "{\"error\":\"" + SimpleJson.escape(e.getMessage()) + "\"}");
            }
        }
    }

    // -------------------------------------------------------------------------
    // API: Appointments Handler
    // -------------------------------------------------------------------------

    private class AppointmentApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod().toUpperCase();
            String path = exchange.getRequestURI().getPath();

            if ("OPTIONS".equals(method)) {
                sendJsonResponse(exchange, 204, "");
                return;
            }

            try {
                if (path.endsWith("/slots") && "GET".equals(method)) {
                    Map<String, String> query = parseQueryParams(exchange.getRequestURI().getQuery());
                    int docId = Integer.parseInt(query.get("doctorId"));
                    LocalDate date = LocalDate.parse(query.get("date"));
                    List<LocalTime> slots = appointmentService.getAvailableTimeSlots(docId, date);
                    List<String> slotStrings = new ArrayList<>();
                    for (LocalTime t : slots) {
                        slotStrings.add(t.toString());
                    }
                    sendJsonResponse(exchange, 200, SimpleJson.toJson(slotStrings));
                } else if (path.endsWith("/status") && "PUT".equals(method)) {
                    Map<String, String> body = SimpleJson.parseFlatObject(readRequestBody(exchange));
                    int id = Integer.parseInt(body.get("id"));
                    String status = body.get("status");
                    appointmentService.updateStatus(id, status);
                    sendJsonResponse(exchange, 200, "{\"success\":true}");
                } else if ("GET".equals(method)) {
                    Map<String, String> query = parseQueryParams(exchange.getRequestURI().getQuery());
                    String status = query.get("status");
                    String dateStr = query.get("date");
                    LocalDate date = (dateStr != null && !dateStr.isEmpty()) ? LocalDate.parse(dateStr) : null;

                    List<Appointment> list = appointmentService.filterAppointments(null, null, date, status);
                    List<Map<String, Object>> jsonList = new ArrayList<>();
                    for (Appointment a : list) {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("id", a.getId());
                        m.put("patientId", a.getPatientId());
                        m.put("patientName", a.getPatientName());
                        m.put("doctorId", a.getDoctorId());
                        m.put("doctorName", a.getDoctorName());
                        m.put("doctorSpecialization", a.getDoctorSpecialization());
                        m.put("date", a.getAppointmentDate().toString());
                        m.put("time", a.getAppointmentTime().toString());
                        m.put("status", a.getStatus());
                        jsonList.add(m);
                    }
                    sendJsonResponse(exchange, 200, SimpleJson.toJson(jsonList));
                } else if ("POST".equals(method)) {
                    Map<String, String> body = SimpleJson.parseFlatObject(readRequestBody(exchange));
                    int patientId = Integer.parseInt(body.get("patientId"));
                    int doctorId = Integer.parseInt(body.get("doctorId"));
                    LocalDate date = LocalDate.parse(body.get("date"));
                    LocalTime time = LocalTime.parse(body.get("time"));

                    Appointment appt = appointmentService.bookAppointment(patientId, doctorId, date, time);
                    sendJsonResponse(exchange, 200, "{\"success\":true,\"id\":" + appt.getId() + "}");
                }
            } catch (Exception e) {
                sendJsonResponse(exchange, 400, "{\"error\":\"" + SimpleJson.escape(e.getMessage()) + "\"}");
            }
        }
    }

    // -------------------------------------------------------------------------
    // API: Billing Handler
    // -------------------------------------------------------------------------

    private class BillingApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod().toUpperCase();
            String path = exchange.getRequestURI().getPath();

            if ("OPTIONS".equals(method)) {
                sendJsonResponse(exchange, 204, "");
                return;
            }

            try {
                if (path.endsWith("/invoice") && "GET".equals(method)) {
                    Map<String, String> query = parseQueryParams(exchange.getRequestURI().getQuery());
                    int id = Integer.parseInt(query.get("id"));
                    Bill bill = billingService.getById(id).orElseThrow(() -> new ValidationException("Bill not found"));
                    String invoice = billingService.generatePrintableInvoice(bill);
                    sendJsonResponse(exchange, 200, "{\"invoice\":\"" + SimpleJson.escape(invoice) + "\"}");
                } else if (path.endsWith("/pay") && "PUT".equals(method)) {
                    Map<String, String> body = SimpleJson.parseFlatObject(readRequestBody(exchange));
                    int id = Integer.parseInt(body.get("id"));
                    billingService.markAsPaid(id);
                    sendJsonResponse(exchange, 200, "{\"success\":true}");
                } else if ("GET".equals(method)) {
                    List<Bill> list = billingService.getAllBills();
                    List<Map<String, Object>> jsonList = new ArrayList<>();
                    for (Bill b : list) {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("id", b.getId());
                        m.put("patientId", b.getPatientId());
                        m.put("patientName", b.getPatientName());
                        m.put("appointmentId", b.getAppointmentId());
                        m.put("consultationFee", b.getConsultationFee().doubleValue());
                        m.put("otherCharges", b.getOtherCharges().doubleValue());
                        m.put("totalAmount", b.getTotalAmount().doubleValue());
                        m.put("paymentStatus", b.getPaymentStatus());
                        m.put("billDate", b.getFormattedDate());
                        jsonList.add(m);
                    }
                    sendJsonResponse(exchange, 200, SimpleJson.toJson(jsonList));
                } else if ("POST".equals(method)) {
                    Map<String, String> body = SimpleJson.parseFlatObject(readRequestBody(exchange));
                    int patientId = Integer.parseInt(body.get("patientId"));
                    String apptStr = body.get("appointmentId");
                    Integer apptId = (apptStr != null && !apptStr.isEmpty() && !apptStr.equals("null")) ? Integer.parseInt(apptStr) : null;
                    BigDecimal fee = new BigDecimal(body.get("consultationFee"));
                    BigDecimal other = new BigDecimal(body.get("otherCharges"));
                    String status = body.get("paymentStatus");

                    Bill bill = billingService.generateBill(patientId, apptId, fee, other, status);
                    sendJsonResponse(exchange, 200, "{\"success\":true,\"id\":" + bill.getId() + ",\"total\":" + bill.getTotalAmount() + "}");
                }
            } catch (Exception e) {
                sendJsonResponse(exchange, 400, "{\"error\":\"" + SimpleJson.escape(e.getMessage()) + "\"}");
            }
        }
    }

    // -------------------------------------------------------------------------
    // API: Medical Records Handler
    // -------------------------------------------------------------------------

    private class MedicalRecordApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod().toUpperCase();
            String path = exchange.getRequestURI().getPath();

            if ("OPTIONS".equals(method)) {
                sendJsonResponse(exchange, 204, "");
                return;
            }

            try {
                if (path.endsWith("/history") && "GET".equals(method)) {
                    Map<String, String> query = parseQueryParams(exchange.getRequestURI().getQuery());
                    int patientId = Integer.parseInt(query.get("patientId"));
                    String history = medicalRecordService.generatePatientHistorySummary(patientId);
                    sendJsonResponse(exchange, 200, "{\"history\":\"" + SimpleJson.escape(history) + "\"}");
                } else if ("GET".equals(method)) {
                    Map<String, String> query = parseQueryParams(exchange.getRequestURI().getQuery());
                    String pidStr = query.get("patientId");
                    List<MedicalRecord> list;
                    if (pidStr != null && !pidStr.isEmpty()) {
                        list = medicalRecordService.getRecordsByPatient(Integer.parseInt(pidStr));
                    } else {
                        list = medicalRecordService.getAllRecords();
                    }

                    List<Map<String, Object>> jsonList = new ArrayList<>();
                    for (MedicalRecord r : list) {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("id", r.getId());
                        m.put("patientId", r.getPatientId());
                        m.put("patientName", r.getPatientName());
                        m.put("doctorId", r.getDoctorId());
                        m.put("doctorName", r.getDoctorName());
                        m.put("diagnosis", r.getDiagnosis());
                        m.put("prescribedMedicines", r.getPrescribedMedicines());
                        m.put("treatmentDetails", r.getTreatmentDetails());
                        m.put("doctorNotes", r.getDoctorNotes());
                        m.put("recordDate", r.getFormattedDate());
                        jsonList.add(m);
                    }
                    sendJsonResponse(exchange, 200, SimpleJson.toJson(jsonList));
                } else if ("POST".equals(method)) {
                    Map<String, String> body = SimpleJson.parseFlatObject(readRequestBody(exchange));
                    int patientId = Integer.parseInt(body.get("patientId"));
                    int doctorId = Integer.parseInt(body.get("doctorId"));
                    String apptStr = body.get("appointmentId");
                    Integer apptId = (apptStr != null && !apptStr.isEmpty() && !apptStr.equals("null")) ? Integer.parseInt(apptStr) : null;

                    MedicalRecord mr = new MedicalRecord(
                            patientId, doctorId, apptId,
                            body.get("diagnosis"),
                            body.get("prescribedMedicines"),
                            body.get("treatmentDetails"),
                            body.get("doctorNotes")
                    );
                    MedicalRecord saved = medicalRecordService.addRecord(mr);
                    sendJsonResponse(exchange, 200, "{\"success\":true,\"id\":" + saved.getId() + "}");
                }
            } catch (Exception e) {
                sendJsonResponse(exchange, 400, "{\"error\":\"" + SimpleJson.escape(e.getMessage()) + "\"}");
            }
        }
    }

    // -------------------------------------------------------------------------
    // API: Unified Directory Handler (Polymorphism Showcase)
    // -------------------------------------------------------------------------

    private class DirectoryApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 204, "");
                return;
            }

            List<Person> list = patientService.getHospitalDirectory();
            List<Map<String, Object>> jsonList = new ArrayList<>();

            for (Person p : list) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", p.getId());
                m.put("type", (p instanceof Doctor) ? "Doctor" : "Patient");
                m.put("name", p.getName());
                m.put("phone", p.getPhone());
                // Polymorphic calls:
                m.put("summary", p.getSummary());
                m.put("displayInfo", p.displayInfo());
                jsonList.add(m);
            }
            sendJsonResponse(exchange, 200, SimpleJson.toJson(jsonList));
        }
    }

    // -------------------------------------------------------------------------
    // API: Users Handler
    // -------------------------------------------------------------------------

    private class UserApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod().toUpperCase();
            if ("OPTIONS".equals(method)) {
                sendJsonResponse(exchange, 204, "");
                return;
            }

            try {
                if ("GET".equals(method)) {
                    List<User> list = authService.getAllUsers();
                    List<Map<String, Object>> jsonList = new ArrayList<>();
                    for (User u : list) {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("id", u.getId());
                        m.put("username", u.getUsername());
                        m.put("role", u.getRole());
                        m.put("fullName", u.getFullName());
                        jsonList.add(m);
                    }
                    sendJsonResponse(exchange, 200, SimpleJson.toJson(jsonList));
                } else if ("POST".equals(method)) {
                    Map<String, String> body = SimpleJson.parseFlatObject(readRequestBody(exchange));
                    User u = authService.registerUser(
                            body.get("username"),
                            body.get("password"),
                            body.get("role"),
                            body.get("fullName")
                    );
                    sendJsonResponse(exchange, 200, "{\"success\":true,\"id\":" + u.getId() + "}");
                } else if ("DELETE".equals(method)) {
                    Map<String, String> query = parseQueryParams(exchange.getRequestURI().getQuery());
                    int id = Integer.parseInt(query.get("id"));
                    authService.deleteUser(id);
                    sendJsonResponse(exchange, 200, "{\"success\":true}");
                }
            } catch (Exception e) {
                sendJsonResponse(exchange, 400, "{\"error\":\"" + SimpleJson.escape(e.getMessage()) + "\"}");
            }
        }
    }
}
