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
import projeto2.log.comum.RMIWorker;
import projeto2.log.log.Log;


public class ThreadProcessaFilaMensagens implements Runnable {
    final Random random;  
    
    public ThreadProcessaFilaMensagens(){
        this.random = new Random(ProcessWorker.semente);
    }
    
    @Override
    public void run(){
        for(int i = 0; i < ProcessWorker.eventos.size(); i++){
            try {
                Thread.sleep(ProcessWorker.tempoEspera);
                
                String evento = ProcessWorker.eventos.get(i);
                String[] partes = evento.split(",");

                //System.out.println(evento);
                //System.out.printf("%s %s\n", partes[0], partes[1]);
                
                int mensagem;
                
                ProcessWorker.incrementaRelogioLocal();        
                int[] relogio = ProcessWorker.relogioLocal;
                
                
                mensagem = Integer.parseInt(partes[1]);    
                
                // se for "e" é um evento remoto
                if (partes[0].equals("e")){
                    String processo = pegaWorkerAleatorio();
                   
                    enviaMensagem(processo, mensagem, relogio);
                    
                    // espera jitter
                    int randomNumJitter = random.nextInt(ProcessWorker.tempoJitter);
                    Thread.sleep(randomNumJitter);
                    
                    ProcessWorker.incrementaRelogioLocal();        
                    relogio = ProcessWorker.relogioLocal;
                    
                    enviaMensagem(Log.NOMEOBJDISTLog, mensagem, relogio);
                } else {
                    enviaMensagem(Log.NOMEOBJDISTLog, mensagem, relogio);
                }
            } catch (InterruptedException ex){
                Logger.getLogger(ProcessWorker.class.getName()).log(Level.SEVERE, null, ex);
            }           
        }
    }
    
    private String pegaWorkerAleatorio(){
        while(true){
            int randomNum = random.nextInt(ProcessWorker.processos.size());
            String processoEscolhido = ProcessWorker.processos.get(randomNum);
            if(!processoEscolhido.equals(ProcessWorker.nomeProcesso)){
                return processoEscolhido;
            }
        }
        
    }

    private void enviaMensagem(String processoDestino, int mensagem, int[] relogio){
        try {
            String timeStamp = new SimpleDateFormat("yyyy-dd-MM HH:mm:ss").format(new java.util.Date());
            System.err.printf("%s Enviando mensagem %d para %s com relógio %s\n", timeStamp, mensagem, processoDestino, Arrays.toString(relogio));
            // se for para o Log
            if(processoDestino.equals(Log.NOMEOBJDISTLog)){             
                ProcessWorker.stubLog.mensagemLog(ProcessWorker.nomeProcesso, mensagem, relogio);
            } else { // se for para outro Worker
                RMIWorker processoRemoto = ProcessWorker.processosWorkerConectados.get(processoDestino);
                processoRemoto.mensagemWorker(ProcessWorker.nomeProcesso, mensagem, relogio);
            }
        } catch (RemoteException ex) {
            Logger.getLogger(ProcessWorker.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
