package projeto2.log.processworker;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import projeto2.log.comum.RMILog;
import projeto2.log.comum.RMIWorker;
import projeto2.log.log.Log;


public class ProcessWorker {

    // Constantes que indicam onde está sendo executado o serviço de registro,
    // qual porta e qual o nome do objeto distribuído
    private static final String IPSERVIDOR = "127.0.0.1";
    private static final int PORTA = 1234;
    private static final String NOMEOBJDIST = "MeuContador";
    public static String nomeProcesso;
    public static int semente;
    public static int tempoEspera;
    public static int tempoJitter;
    private static int nTotalMensagens;
    private static String arqProcessos;
    private static String arqEventos;
    
    public static ArrayList<String> processos;
    public static ArrayList<String> eventos;
    
    public static HashMap<String, RMIWorker> processosWorkerConectados;
    private static RMIWorkerLocal processoLocal;
    private static RMIWorker stubProcessoLocal;
    public static RMILog stubLog;
    
   public static int[] relogioLocal;
   public static int posRelogioProcLocal;
    
    private static Registry registro;
    
    //Função que mostra como usar o programa
    public static void ajuda() {    
        System.out.println("Exemplo de uso: ");
        System.out.println("java nomeDaClasse Id Semente tempoEspera tempoJitter arquivo-com-processos arquivo-comeventos");
        System.out.println("java processoWorker p1 123456 3000 2000 processos.txt eventos.txt");
    }
    
    public static int converteArgIntero(String valor){
        int valorInt = 0;
        try {
            valorInt = Integer.parseInt(valor);
        } catch (Exception e) {
            System.err.println(valor + " não é um inteiro");
            System.exit(1);
        }
        return valorInt;
    }
    
    private static void lerArquivoProcessos(String nomeArquivo) {
        try {
            processos = new ArrayList<>();
            
            FileReader arq = new FileReader(nomeArquivo);
            BufferedReader lerArq = new BufferedReader(arq);
 
            String linha = lerArq.readLine();
            
            if (linha == null){
                System.err.printf("O arquivo de processos está vazio\n");
                System.exit(1);
            }
            
            int i = 0;
            
            while (linha != null) {
                if(linha.equals(nomeProcesso)){
                   posRelogioProcLocal = i; 
                }
                processos.add(linha);
                linha = lerArq.readLine();
                i++;
            }

            arq.close();            
        } catch (IOException e) {
            System.err.printf("Erro na abertura do arquivo: %s.\n", e.getMessage());
            System.exit(1);
        }
    
    }
    
    private static void lerArquivoEventos(String nomeArquivo) {
        try {
            eventos = new ArrayList<>();
            
            FileReader arq = new FileReader(nomeArquivo);
            BufferedReader lerArq = new BufferedReader(arq);
 
            String linha = lerArq.readLine();
            
            if (linha == null){
                System.err.printf("O arquivo de eventos está vazio\n");
                System.exit(1);
            }
            
            nTotalMensagens = 0;
            
            while (linha != null) {
                String[] partes = linha.split(",");
                
                if (partes.length != 2) {
                    System.out.println("A linha de evento " + linha + " não está no formato correto.");
                    System.exit(1);
                }
                
                if (partes[0].equals("e")){
                    nTotalMensagens += 2;
                } else {
                    nTotalMensagens += 1;
                }
                
                try {
                    Integer.parseInt(partes[1]);
                } catch (Exception e) {
                    System.err.println(partes[1] + " não é um inteiro");
                    System.exit(1);                
                }
                
                eventos.add(linha);
                linha = lerArq.readLine();
            }

            arq.close();            
        } catch (IOException e) {
            System.err.printf("Erro na abertura do arquivo: %s.\n", e.getMessage());
            System.exit(1);
        }
    
    }
    
    public static void conectaWorkers(){
        System.out.println("Esperando conexão com todos os processWorker");
        
        // passa pela lista de workers e tenta conectar
        for(int i=0; i < processos.size(); i++){
            
            // não conecta se o processo da lista for o próprio processo
            if (processos.get(i).equals(nomeProcesso))
                continue;
            
            while(true){
                try {
                    // Procurando pelo objeto distribuído registrado por outro processo
                    RMIWorker stub = (RMIWorker) registro.lookup(processos.get(i));
                    
                    //adiciona no hashMap
                    processosWorkerConectados.put(processos.get(i), stub);
                    
                    System.out.printf("Conectado ao processo %s\n", processos.get(i));
                    
                    //sai do while e continua o for
                    break;
                } catch (RemoteException | NotBoundException ex) {
                    // exception se o processo externo ainda não registrou o objeto distribuido
                }
            }
        }
    }
    
    public static void inicializaParametros(String args[]){
        if (args.length != 6) {
            System.out.println("Erro: número de parâmetros incorreto");
            ajuda();
            System.exit(1);
        }

        nomeProcesso = args[0];
        semente = converteArgIntero(args[1]);
        tempoEspera = converteArgIntero(args[2]);
        tempoJitter = converteArgIntero(args[3]);
        arqProcessos = args[4];
        arqEventos = args[5];
    }
    
    public static void incrementaRelogioLocal(){
        relogioLocal[posRelogioProcLocal] = relogioLocal[posRelogioProcLocal] +1;
    }
    
    public static void atualizarRelogio(int[] relogioRecebido){
        for(int i = 0; i < processos.size(); i++){
            if(relogioRecebido[i] > relogioLocal[i]){
                relogioLocal[i] = relogioRecebido[i];
            }
        }
    }
    
    public static void main(String args[]) {
        try {
            inicializaParametros(args);
            
            lerArquivoProcessos(arqProcessos);
            lerArquivoEventos(arqEventos);
            
            relogioLocal = new int[processos.size()];

            Arrays.fill(relogioLocal, 0);
            
            processosWorkerConectados = new HashMap<>();
            
            // Obtendo referência do serviço de registro
            registro = LocateRegistry.getRegistry(IPSERVIDOR, PORTA);

            // Procurando pelo objeto distribuído registrado pelo Log
            stubLog = (RMILog) registro.lookup(Log.NOMEOBJDISTLog);
            
            // cria objeto distibuido para o processo local
            processoLocal = new RMIWorkerLocal();
            stubProcessoLocal = (RMIWorker) UnicastRemoteObject.exportObject(processoLocal, 0);
            
            // Registrando objeto distribuído
            registro.bind(nomeProcesso, stubProcessoLocal);
                      
            //diz para o Log o total de mensagens que deve esperar
            stubLog.workerTotalMensagens(nomeProcesso, nTotalMensagens);
            
            //conecta nos outros processos
            conectaWorkers();

            // cria e executa a thread que irá enviar as mensagens da fila
            ThreadProcessaFilaMensagens thread = new ThreadProcessaFilaMensagens();
            new Thread(thread).start();

        } catch (RemoteException | AlreadyBoundException | NotBoundException ex) {
            Logger.getLogger(ProcessWorker.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
