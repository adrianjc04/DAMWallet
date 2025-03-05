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

/**
 * La clase MovimientoDAO es responsable de gestionar las operaciones 
 * relacionadas con la base de datos de movimientos financieros. Esto incluye
 * la creación de la base de datos, la creación de la tabla de movimientos, 
 * la inserción, la lectura, la eliminación y la obtención de movimientos 
 * desde la base de datos SQLite.
 */
public abstract class MovimientoDAO {

    // Ruta de la base de datos y nombre de la tabla
    public static String rutaBBDD = "BaseDeDatos" + File.separator + "Movimientos.db";
    public static final String NOMBRETABLA = "MOVIMIENTO";

    // Sentencia SQL para crear la tabla de movimientos
    public static final String CREATETABLE
            = "CREATE TABLE " + NOMBRETABLA + "("
            + "ID INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "CONCEPTO TEXT CHECK(LENGTH(CONCEPTO) <= 25 AND LENGTH(CONCEPTO) > 0),"
            + "CANTIDAD REAL CHECK(CANTIDAD > -10000000 AND CANTIDAD < 10000000 AND CANTIDAD <> 0 AND (CANTIDAD * 100) = CAST(CANTIDAD * 100 AS INTEGER)),"
            + "FECHA INTEGER CHECK(FECHA >= 0)"
            + ")";

    /**
     * Crea la base de datos SQLite en la ruta especificada si no existe.
     * Si la base de datos ya existe, simplemente crea la tabla de movimientos.
     * 
     * @return true si la base de datos y la tabla fueron creadas correctamente, 
     *         false si ocurrió un error.
     */
    public static boolean crearBaseDeDatos() {
        boolean creado;
        String url;
        try {
            new File(rutaBBDD).getParentFile().mkdirs();
        } catch (NullPointerException e) {
            // Si ocurre un error al crear el directorio, no se interrumpe el proceso
        }
        url = "jdbc:sqlite:" + rutaBBDD;
        try (Connection conexion = DriverManager.getConnection(url)) {
            creado = true;
        } catch (SQLException e) {
            e.printStackTrace();
            creado = false;
            System.out.println("MovimientoDAO: Error al crear la base de datos");
        }
        crearTablaMovimiento();
        return creado;
    }

    /**
     * Crea la tabla MOVIMIENTO en la base de datos si no existe.
     * Si la tabla ya existe, simplemente no realiza ninguna acción.
     */
    private static void crearTablaMovimiento() {
        String url = "jdbc:sqlite:" + rutaBBDD;
        try (Connection conexion = DriverManager.getConnection(url); Statement crearTabla = conexion.createStatement()) {
            crearTabla.execute(CREATETABLE);
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("MovimientoDAO: Error al crear la tabla, o ya existe.");
        }
    }

    /**
     * Lee los movimientos desde la base de datos utilizando una sentencia SELECT personalizada.
     * Los resultados se devuelven como un arreglo de objetos Movimiento.
     * 
     * @param select la sentencia SQL SELECT que define qué movimientos leer.
     * @return un arreglo de objetos Movimiento que representa los registros encontrados en la base de datos.
     */
    public static Movimiento[] leerMovimientos(String select) {
        ArrayList<Movimiento> movimientos = new ArrayList<>();
        String url = "jdbc:sqlite:" + rutaBBDD;
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

    /**
     * Inserta un nuevo movimiento en la base de datos.
     * 
     * @param movimiento el objeto Movimiento a insertar.
     * @return true si el movimiento fue insertado correctamente, false si ocurrió un error.
     */
    public static boolean escribirMovimiento(Movimiento movimiento) {
        boolean insertado;
        String url = "jdbc:sqlite:" + rutaBBDD;
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

    /**
     * Elimina un movimiento de la base de datos mediante su identificador.
     * 
     * @param id el ID del movimiento a eliminar.
     * @return true si el movimiento fue eliminado correctamente, false si ocurrió un error.
     */
    public static boolean borrarMovimiento(long id) {
        boolean borrado;
        String url = "jdbc:sqlite:" + rutaBBDD;
        try (Connection conexion = DriverManager.getConnection(url); Statement sentenciaDelete = conexion.createStatement()) {
            String delete = "DELETE FROM " + NOMBRETABLA + " WHERE ID = " + id + ";";
            sentenciaDelete.execute(delete);
            borrado = true;
        } catch (SQLException e) {
            borrado = false;
        }
        return borrado;
    }

    /**
     * Obtiene un movimiento específico de la base de datos utilizando su ID.
     * 
     * @param id el ID del movimiento a obtener.
     * @return el objeto Movimiento correspondiente al ID, o null si no se encuentra.
     */
    public static Movimiento obtenerMovimientoPorId(long id) {
        Movimiento movimiento = null;
        String url = "jdbc:sqlite:" + rutaBBDD;
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