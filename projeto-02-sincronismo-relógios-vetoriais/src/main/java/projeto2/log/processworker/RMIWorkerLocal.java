/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package projeto2.log.processworker;

import java.rmi.RemoteException;
import projeto2.log.comum.RMIWorker;


public class RMIWorkerLocal implements RMIWorker {
    @Override
    public void mensagemWorker(String processo, int mensagem, int[] relogioRecebido) throws RemoteException {
        ThreadProcessaMensagemRecebida thread = new ThreadProcessaMensagemRecebida(processo, mensagem, relogioRecebido);
        new Thread(thread).start();
    }
    
    @Override
    public void encerraWorker(){
        System.out.println("Encerrando worker...");
        System.exit(0);
    }
}
