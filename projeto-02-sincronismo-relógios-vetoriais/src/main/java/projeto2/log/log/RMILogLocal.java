package projeto2.log.log;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import projeto2.log.comum.RMILog;
import projeto2.log.comum.RMIWorker;


public class RMILogLocal implements RMILog{
    private int nTotalMensagensTodosWorkers = 0;
    private int nTotalMensagensRecebidas = 0;
    private ArrayList<String> processosWorkerConectados = new ArrayList<>();
    private ArrayList<Mensagem> mensagens = new ArrayList<>();
    
    @Override
    public void mensagemLog(String processo, int mensagem, int[] relogioRecebido){
        //String timeStamp = new SimpleDateFormat("yyyy-dd-MM HH:mm:ss").format(new java.util.Date());
        //String relogio = Arrays.toString(relogioRecebido);
        
        //System.out.printf("%s\t%s\t%d\t%s\n", timeStamp, processo, mensagem, relogio);]
        Mensagem msg = new Mensagem(processo, mensagem, relogioRecebido);
        mensagens.add(msg);
        
        nTotalMensagensRecebidas++;
        
        // manda workers encerrar se recebeu todas as mensagens
        if (nTotalMensagensRecebidas == nTotalMensagensTodosWorkers){
            for(int i=0; i < processosWorkerConectados.size(); i++){
                try {
                    // Procurando pelo objeto distribuído registrado por outro processo
                    RMIWorker stub = (RMIWorker) Log.registro.lookup(processosWorkerConectados.get(i));
                    stub.encerraWorker();
                } catch (RemoteException | NotBoundException ex) {
                    // exception se o processo externo ainda não registrou o objeto distribuido
                }
            }
            
            ordenaEmostraMensagens();
        }
    }
    
    @Override
    public void workerTotalMensagens(String nomeProcesso, int nTotalMensagens){
        nTotalMensagensTodosWorkers += nTotalMensagens;
        processosWorkerConectados.add(nomeProcesso);
    }
    
    public void ordenaEmostraMensagens(){        
        //ordena a fila de mensagens com base nos relogios
        Collections.sort(mensagens);
        
        //mostra as mensagens 
        for(int i = 0; i < mensagens.size(); i++){
            Mensagem msg = mensagens.get(i);
            String timeStamp = new SimpleDateFormat("yyyy-dd-MM HH:mm:ss").format(new java.util.Date());
            String relogio = Arrays.toString(msg.relogioRecebido);

            System.out.printf("%s\t%s\t%d\t%s\n", timeStamp, msg.processo, msg.mensagem, relogio);
        }
        
        System.out.println("\nFim do Log");
        System.exit(0);//
    }
}
