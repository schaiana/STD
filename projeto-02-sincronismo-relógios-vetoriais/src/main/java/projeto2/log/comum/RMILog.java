package projeto2.log.comum;

import java.rmi.Remote;
import java.rmi.RemoteException;


public interface RMILog extends Remote{
    public void mensagemLog(String processo, int mensagem, int[] relogioRecebido) throws RemoteException;
    public void workerTotalMensagens(String nomeProcesso, int nTotalMensagens) throws RemoteException;
}
