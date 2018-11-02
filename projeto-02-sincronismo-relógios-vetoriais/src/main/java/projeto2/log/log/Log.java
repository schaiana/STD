package projeto2.log.log;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Level;
import java.util.logging.Logger;
import projeto2.log.comum.RMILog;

public class Log {

    // Constantes que indicam onde está sendo executado o serviço de registro,
    // qual porta e qual o nome do objeto distribuído
    private static final String IPSERVIDOR = "127.0.0.1";
    private static final int PORTA = 1234;
    public static final String NOMEOBJDISTLog = "RMILog";
    private static RMILogLocal logLocal;
    public static Registry registro;

    public static void main(String args[]){
        try {
            // Criando
            logLocal = new RMILogLocal();

            // Definindo o hostname do servidor
            System.setProperty("java.rmi.server.hostname", IPSERVIDOR);

            RMILog stubLog = (RMILog) UnicastRemoteObject.exportObject(logLocal, 0);

            // Criando serviço de registro
            registro = LocateRegistry.createRegistry(PORTA);

            // Registrando objeto distribuído
            registro.bind(NOMEOBJDISTLog, stubLog);

            System.out.println("Processo Log iniciado!\n");
            System.out.println("Pressione CTRL + C para encerrar...\n");

            System.out.println("#Timestamp\t\tFROM\tMessage\tLogicalClock");
            /*final String[] boundNames = registro.list();
            System.out.println("Names bound to RMI registry:");
            for (final String name : boundNames)
            {
               System.out.println("\t" + name);
            }*/

        } catch (RemoteException | AlreadyBoundException ex) {
            Logger.getLogger(Log.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
