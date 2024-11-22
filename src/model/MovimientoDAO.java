package model;

import java.io.File;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class MovimientoDAO {

    public static final String NAMESPACEBBDD = "BaseDeDatos";
    public static final String NOMBREBBDD = "Movimientos.db";
    public static final String NOMBRETABLA = "MOVIMIENTO";
    public static final String CREATETABLE
            = "CREATE TABLE " + NOMBRETABLA + "("
            + "ID INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "CONCEPTO TEXT CHECK(LENGTH(CONCEPTO) <= 25 AND LENGTH(CONCEPTO) > 0),"
            + "CANTIDAD REAL CHECK(CANTIDAD > -10000000 AND CANTIDAD < 10000000 AND CANTIDAD <> 0 AND (CANTIDAD * 100) = CAST(CANTIDAD * 100 AS INTEGER)),"
            + "FECHA INTEGER CHECK(FECHA >= 0)"
            + ")";

    public static boolean crearBaseDeDatos() {
        boolean creado;
        String url;
        new File(NAMESPACEBBDD).mkdirs();
        url = "jdbc:sqlite:" + NAMESPACEBBDD + File.separator + NOMBREBBDD;
        try (Connection conexion = DriverManager.getConnection(url)) {
            creado = true;
        } catch (SQLException e) {
            creado = false;
            System.out.println("MovimientoDAO: Error al crear la base de datos");
        }
        crearTablaMovimiento();
        return creado;
    }

    private static void crearTablaMovimiento() {
        String url = "jdbc:sqlite:" + NAMESPACEBBDD + File.separator + NOMBREBBDD;
        try (Connection conexion = DriverManager.getConnection(url); Statement crearTabla = conexion.createStatement()) {
            crearTabla.execute(CREATETABLE);
        } catch (SQLException e) {
            System.out.println("MovimientoDAO: Error al crear la tabla, o ya existe.");
        }
    }

    public static Movimiento[] leerMovimientos(String select) {
        ArrayList<Movimiento> movimientos = new ArrayList<>();
        String url = "jdbc:sqlite:" + NAMESPACEBBDD + File.separator + NOMBREBBDD;
        try (Connection conexion = DriverManager.getConnection(url); Statement consulta = conexion.createStatement()) {
            ResultSet query = consulta.executeQuery(select);
            while (query.next()) {
                long id = query.getLong("ID");
                String concepto = query.getString("CONCEPTO");
                double cantidad = query.getDouble("CANTIDAD");
                Calendar fechaLong = Calendar.getInstance();
                fechaLong.setTime(new Date(query.getLong("FECHA")));
                LocalDate fecha = LocalDate.of(fechaLong.get(Calendar.YEAR), fechaLong.get(Calendar.MONTH) + 1, fechaLong.get(Calendar.DAY_OF_MONTH));
                movimientos.add(new Movimiento(id, concepto, cantidad, fecha));
            }
        } catch (SQLException e) {
            System.out.println("MovimientoDAO: Error al leer movimientos.");
        }
        return movimientos.toArray(Movimiento[]::new);
    }

    public static boolean escribirMovimiento(Movimiento movimiento) {
        boolean insertado;
        String url = "jdbc:sqlite:" + NAMESPACEBBDD + File.separator + NOMBREBBDD;
        try (Connection conexion = DriverManager.getConnection(url); Statement sentenciaInsert = conexion.createStatement()) {
            String insert = "INSERT INTO " + NOMBRETABLA + "(CONCEPTO, CANTIDAD, FECHA) VALUES("
                    + "'" + movimiento.getConcepto() + "',"
                    + movimiento.getCantidad() + ","
                    + movimiento.fechaToDate().getTime() + ");";
            sentenciaInsert.execute(insert);
            insertado = true;
        } catch (SQLException e) {
            insertado = false;
        }
        return insertado;
    }

    public static boolean borrarMovimiento(long id) {
        boolean borrado;
        String url = "jdbc:sqlite:" + NAMESPACEBBDD + File.separator + NOMBREBBDD;
        try (Connection conexion = DriverManager.getConnection(url); Statement sentenciaDelete = conexion.createStatement()) {
            String delete = "DELETE FROM " + NOMBRETABLA + " WHERE ID = " + id + ";";
            sentenciaDelete.execute(delete);
            borrado = true;
        } catch (SQLException e) {
            borrado = false;
        }
        return borrado;
    }

    public static Movimiento obtenerMovimientoPorId(long id) {
        Movimiento movimiento = null;
        String url = "jdbc:sqlite:" + NAMESPACEBBDD + File.separator + NOMBREBBDD;
        try (Connection conexion = DriverManager.getConnection(url); Statement consulta = conexion.createStatement()) {
            ResultSet query = consulta.executeQuery("SELECT * FROM MOVIMIENTO WHERE ID = " + id + ";");
            String concepto = query.getString("CONCEPTO");
            double cantidad = query.getDouble("CANTIDAD");
            Calendar fechaLong = Calendar.getInstance();
            fechaLong.setTime(new Date(query.getLong("FECHA")));
            LocalDate fecha = LocalDate.of(fechaLong.get(Calendar.YEAR), fechaLong.get(Calendar.MONTH) + 1, fechaLong.get(Calendar.DAY_OF_MONTH));
            movimiento = new Movimiento(id, concepto, cantidad, fecha);
        } catch (SQLException e) {
            System.out.println("MovimientoDAO: Error al leer movimientos.");
        }
        return movimiento;
    }
}
