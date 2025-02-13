package observer;

/**
 * La interfaz BalanceObserver es utilizada para recibir notificaciones sobre cambios 
 * en el balance. Las clases que implementan esta interfaz pueden suscribirse a los 
 * cambios de balance y reaccionar a ellos, por ejemplo, actualizando la vista o 
 * realizando algún otro tipo de acción.
 * 
 * Esta interfaz define un único método `onBalanceChange`, que se invoca cuando 
 * el balance cambia.
 */
public interface BalanceObserver {

    /**
     * Método que es llamado cuando el balance cambia.
     * 
     * @param balance el nuevo balance.
     */
    void onBalanceChange(double balance);
}