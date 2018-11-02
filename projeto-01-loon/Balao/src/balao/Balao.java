/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package balao;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Comparator;

/**
 *
 * @author schaiana
 */
public class Balao {
    private double latitude, longitude;
    private int porta, porta_vizinho;
    private String ip_vizinho;
            
    private ArrayList<ErbInfo> erbs = new ArrayList<>();
    
    //Função que mostra como usar o programa
    public static void ajuda() {
        System.out.println("Exemplo de uso: balao -p 4100 -lat -27.12243 -long 48.2929 [-ip_vizinho 66.125.44.15 -p_vizinho 4120]");
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //Trata parâmentros de entrada digitados pelo usuário
        double latitude = 0.0;
        double longitude = 0.0;
        int porta = 0;
        String ip_vizinho = "";
        int porta_vizinho = 0;
        
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
                        System.err.println(args[i+1] + " não é uma latitude válida");
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
                        System.err.println(args[i+1] + " não é uma longitude válida");
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
                        System.err.println(args[i+1] + " não é uma porta válida");
                        System.exit(1);
                    }
                    i++;
                    continue;
                }
                           
            } else if (args[i].equals("-ip_vizinho")) {
                if (i < args.length) {
                    ip_vizinho = args[i+1];
                    i++;
                    continue;
                }
            } else if (args[i].equals("-p_vizinho")) {
                if (i < args.length) {
                    try {
                        porta_vizinho = Integer.parseInt(args[i+1]);
                    } catch (Exception e) {
                        System.err.println(args[i+1] + " não é uma porta_vizinho válida");
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
        
        Balao balao = new Balao(latitude, longitude, porta, ip_vizinho, porta_vizinho);
    }
    
    public Balao(double latitude, double longitude, int porta, String ip_vizinho, int porta_vizinho) {
        this.latitude       = latitude;
        this.longitude      = longitude;
        this.porta          = porta;
        this.ip_vizinho     = ip_vizinho;
        this.porta_vizinho  = porta_vizinho;
        
        System.out.printf("Balão - Latitude: %f Longitude: %f\n", this.latitude, this.longitude);
        
        lerArquivoERBs();
        organizaListaERBs();
        iniciarServidor();
    }
    
    private void lerArquivoERBs() {
        try {
            FileReader arq = new FileReader("erbs.conf");
            BufferedReader lerArq = new BufferedReader(arq);
 
            String linha = lerArq.readLine();
            while (linha != null) {
                String ip = "";
                int porta = 0;
                double _lat = 0.0;
                double _long = 0.0;
                //System.out.printf("%s\n", linha);
 
                //if (linha == null) break;               
                
                String[] partes = linha.split(",");
                if (partes.length != 3) {
                    System.out.println("A linha " + linha + " não está no formato correto.");
                    System.exit(1);
                }
                String[] ip_porta = partes[0].split(":");
                if (ip_porta.length != 2) {
                    System.out.println("A linha " + partes[0] + " não está no formato correto.");
                    System.exit(1);
                }
                ip = ip_porta[0];
                try {
                    porta = Integer.parseInt(ip_porta[1]);
                } catch (Exception e) {
                    System.err.println(porta + " não é uma porta válida");
                    System.exit(1);                
                }
                try {
                    _lat = Double.parseDouble(partes[1]);
                } catch (Exception e) {
                    System.err.println(_lat + " não é uma latitude válida");
                    System.exit(1);                
                }
                try {
                    _long = Double.parseDouble(partes[2]);
                } catch (Exception e) {
                    System.err.println(_lat + " não é uma latitude válida");
                    System.exit(1);                
                }
                if(ip.equals("")) {
                    System.out.println(ip + " não é válido");
                    System.exit(1);
                }
                if(porta == 0) {
                    System.out.println(porta + " não é válido");
                    System.exit(1);
                }
                ErbInfo erbInfo = new ErbInfo(ip, porta, _lat, _long);
                erbInfo.calculaDistancia(latitude, longitude);
                erbs.add(erbInfo);
                
                linha = lerArq.readLine();
            }

            arq.close();
        } catch (IOException e) {
            System.err.printf("Erro na abertura do arquivo: %s.\n", e.getMessage());
            System.exit(1);
        }
    
    }
    
    private void organizaListaERBs(){
        erbs.sort(Comparator.comparing(ErbInfo::getDistancia));
    }
    
    /**
     * Usado somente para debug
    **/
    private void mostraListaERBs(){
        int i;
        System.out.println();
        for(i = 0; i < erbs.size(); i++){
            System.out.println(erbs.get(i).distancia);
        }
    }
    
    private void iniciarServidor(){
        ServerSocket ss = null;
        Socket cliente = null;
        try {
            // cria servidor de sockets na porta escolhida
            ss = new ServerSocket(porta);
            // aguarda solicitacao
            System.out.println("Balão: Aguardando na porta " + ss.getLocalPort());
            while (true) {
                // inicializacao da conexao
                cliente = ss.accept();
                // retornou de accept(), solicitacao recebida
                System.out.println("Server: Processando solicitacao de " +
                                   cliente.getInetAddress().getHostName());
                //Cria nova thread e passa o socket e essa instância do Balao
                BalaoThread bt = new BalaoThread(cliente, this);
                new Thread(bt).start();
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
    
    public ArrayList<ErbInfo> getErbs(){
        return this.erbs;
    }
    
    public String getVizinhoIP(){
        return this.ip_vizinho;
    }
    
    public int getVizinhoPorta(){
        return this.porta_vizinho;
    }
    
    
    /**
     * Fonte: https://stackoverflow.com/questions/3694380/calculating-distance-between-two-points-using-latitude-longitude-what-am-i-doi
    * Calculate distance between two points in latitude and longitude taking
    * into account height difference. If you are not interested in height
    * difference pass 0.0. Uses Haversine method as its base.
    * 
    * lat1, lon1 Start point lat2, lon2 End point el1 Start altitude in meters
    * el2 End altitude in meters
    * @returns Distance in Meters
    */
    public static double distance(double lat1, double lat2, double lon1, double lon2) {
        double el1 = 0.0;
        double el2 = 0.0;
        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        double height = el1 - el2;

        distance = Math.pow(distance, 2) + Math.pow(height, 2);

        return Math.sqrt(distance);
    }
}
