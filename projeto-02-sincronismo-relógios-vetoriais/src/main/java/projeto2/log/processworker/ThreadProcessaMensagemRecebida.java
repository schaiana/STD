/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package projeto2.log.processworker;

import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ThreadProcessaMensagemRecebida implements Runnable {
    private int mensagem;
    private String processo;
    private int[] relogioRecebido;
  //  private Sr
    
    public ThreadProcessaMensagemRecebida(String processo, int mensagem, int[] relogioRecebido){
        this.processo = processo;
        this.mensagem = mensagem;
        this.relogioRecebido = relogioRecebido;
    }
    
    @Override
    public void run() {
        try {
            //atualiza relogioLocal
            ProcessWorker.atualizarRelogio(relogioRecebido);
            
            // espera tempo aleatório de jitter
            final Random random = new Random(ProcessWorker.semente);  
            int randomNumJitter = random.nextInt(ProcessWorker.tempoJitter);
            Thread.sleep(randomNumJitter);
                
            ProcessWorker.incrementaRelogioLocal();
            int[] relogio = ProcessWorker.relogioLocal;
            
            String timeStamp = new SimpleDateFormat("yyyy-dd-MM HH:mm:ss").format(new java.util.Date());

            System.err.printf("%s Enviando mensagem %d para RMILog com relógio %s\n", timeStamp, mensagem, Arrays.toString(relogio));
            // envia mensagem para o Log
            ProcessWorker.stubLog.mensagemLog(ProcessWorker.nomeProcesso, mensagem, relogio);
            
        } catch (InterruptedException | RemoteException ex) {
            Logger.getLogger(ThreadProcessaMensagemRecebida.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
