/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package erb;

import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author schaiana
 */
public class Erb {
    private double latitude, longitude;
    private int porta;
    
    //Função que mostra como usar o programa
    public static void ajuda() {
        System.out.println("Exemplo de uso: erb -p 4100 -lat -27.12243 -long 48.2929");
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //Trata parâmentros de entrada digitados pelo usuário
        double latitude = 0.0;
        double longitude = 0.0;
        int porta = 0;
        if (args.length == 0) {
            ajuda();
            System.exit(1);
        }
        for(int i = 0; i < args.length; i++) {
            //System.out.println(args[i]);
            if (args[i].equals("-h") ||args[i].equals("--help")) {
                ajuda();
                System.exit(1);            
            } else if (args[i].equals("-lat")) {
                if (i < args.length) {
                    try {
                    latitude = Double.parseDouble(args[i+1]);
                    } catch (Exception e) {
                        System.out.println(args[i+1] + " não é uma latitude válida");
                        System.exit(1);
                    }
                    
                    i++;
                    continue;
                }
            }
            else if (args[i].equals("-long")) {
                if (i < args.length) {
                    try {
                        longitude = Double.parseDouble(args[i+1]);
                    } catch (Exception e) {
                        System.out.println(args[i+1] + " não é uma longitude válida");
                        System.exit(1);
                    }
                    i++;
                    continue;
                }
            }
            else if (args[i].equals("-p")) {
                if (i < args.length) {
                    try {
                        porta = Integer.parseInt(args[i+1]);
                    } catch (Exception e) {
                        System.out.println(args[i+1] + " não é uma porta válida");
                        System.exit(1);
                    }
                    i++;
                    continue;
                }
                           
            }
        }
        //System.out.println(latitude +" "+ longitude +" "+ porta);
        if (latitude == 0.0) {
            System.out.println("É necessário o parâmetro '-lat'.");
            System.exit(1);
        }
        if (longitude == 0.0) {
            System.out.println("É necessário o parâmetro '-long'.");
            System.exit(1);
        }
        if (porta == 0) {
            System.out.println("É necessário o parâmetro '-p'.");
            System.exit(1);
        }
        
        Erb erb = new Erb(latitude, longitude, porta);
    }
    
    public Erb(double latitude, double longitude, int porta) {
        this.porta = porta;
        this.latitude = latitude;
        this.longitude = longitude;
        
        System.out.printf("ERB - Latitude: %f Longitude: %f\n", this.latitude, this.longitude);
        
        iniciarServidor();
    }
    
    private void iniciarServidor(){
        ServerSocket ss = null;
        Socket sBalao = null;
        try {
            // cria servidor de sockets na porta escolhida
            ss = new ServerSocket(porta);
            // aguarda solicitacao
            System.out.println("ERB: Aguardando na porta " + ss.getLocalPort());
            while (true) {
                // inicializacao da conexao
                sBalao = ss.accept();
                // retornou de accept(), solicitacao recebida
                System.out.println("Server: Processando solicitacao de " +
                                   sBalao.getInetAddress().getHostName());
                //Cria nova thread e passa o socket e essa instância do Balao
                ErbThread et = new ErbThread(sBalao);
                new Thread(et).start();
            }
        }
        catch (Exception e) {
            System.err.println(e);
        }
        finally {
            try {
              ss.close();
            }
            catch (Exception e) {
              System.err.println(e);
            }
        }
    }
}
