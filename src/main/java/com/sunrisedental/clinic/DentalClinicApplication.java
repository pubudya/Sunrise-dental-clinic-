package com.sunrisedental.clinic;

import com.sunrisedental.clinic.filter.AuthFilter;
import com.sunrisedental.clinic.servlet.ApiAppointmentServlet;
import com.sunrisedental.clinic.servlet.ApiBillServlet;
import com.sunrisedental.clinic.servlet.ApiDashboardServlet;
import com.sunrisedental.clinic.servlet.ApiDentistServlet;
import com.sunrisedental.clinic.servlet.ApiLoginServlet;
import com.sunrisedental.clinic.servlet.ApiLogoutServlet;
import com.sunrisedental.clinic.servlet.ApiMetaServlet;
import com.sunrisedental.clinic.servlet.ApiRegisterServlet;
import com.sunrisedental.clinic.servlet.ApiSessionServlet;
import com.sunrisedental.clinic.servlet.ApiStaffServlet;
import com.sunrisedental.clinic.util.DatabaseInitializer;
import jakarta.servlet.DispatcherType;
import org.eclipse.jetty.ee10.servlet.DefaultServlet;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.server.Server;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;

/**
 * Starts the embedded web server so you can run the app from IntelliJ IDEA.
 * Open http://localhost:8080/login.html after starting.
 */
public class DentalClinicApplication {

    private static final int PORT = Integer.getInteger("server.port", 8080);

    public static void main(String[] args) throws Exception {
        Path projectRoot = Path.of(System.getProperty("user.dir"));
        Path webappDir = projectRoot.resolve("src/main/webapp");

        if (!Files.isDirectory(webappDir)) {
            throw new IllegalStateException("Webapp folder not found: " + webappDir);
        }

        DatabaseInitializer.initialize();

        Server server = new Server(PORT);
        ServletContextHandler context =
                new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        context.setBaseResourceAsString(webappDir.toUri().toString());

        context.addServlet(ApiLoginServlet.class, "/api/login");
        context.addServlet(ApiRegisterServlet.class, "/api/register");
        context.addServlet(ApiSessionServlet.class, "/api/session");
        context.addServlet(ApiLogoutServlet.class, "/api/logout");
        context.addServlet(ApiDashboardServlet.class, "/api/dashboard");
        context.addServlet(ApiAppointmentServlet.class, "/api/appointments");
        context.addServlet(ApiMetaServlet.class, "/api/meta");
        context.addServlet(ApiBillServlet.class, "/api/bill");
        context.addServlet(ApiStaffServlet.class, "/api/staff");
        context.addServlet(ApiDentistServlet.class, "/api/dentists");
        context.addFilter(
                AuthFilter.class,
                "/api/*",
                EnumSet.of(DispatcherType.REQUEST)
        );

        ServletHolder staticFiles = new ServletHolder("static", DefaultServlet.class);
        staticFiles.setInitParameter("dirAllowed", "false");
        context.addServlet(staticFiles, "/");

        server.setHandler(context);
        server.start();

        System.out.println("============================================");
        System.out.println("  Sunrise Dental Clinic - Server Started");
        System.out.println("  Open: http://localhost:" + PORT + "/login.html");
        System.out.println("  Default admin: admin / admin123");
        System.out.println("  MySQL database connection: OK");
        System.out.println("============================================");

        server.join();
    }
}
