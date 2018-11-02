/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cliente;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author schaiana
 */
public class Cliente {
    
    private char resultado = 0;
    
    //Função que mostra como usar o programa
    public static void ajuda() {
        System.out.println("Exemplo de uso: cliente -ip 66.125.44.77 -p 4100 nomearquivo");
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //Trata parâmentros de entrada digitados pelo usuário
        String ip = "";
        String arq = "";
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
            } else if (args[i].equals("-ip")) {
                if (i < args.length) {
                    ip = args[i+1];
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
            }
            else {
                arq = args[i];            
            }
        }
        System.out.println(ip +" "+ porta +" "+ arq);
        if (ip.equals("")) {
            System.out.println("É necessário o parâmetro '-ip'.");
            System.exit(1);
        }
        if (arq.equals("")) {
            System.out.println("É necessário o parâmetro 'arquivo'.");
            System.exit(1);
        }
        if (porta == 0) {
            System.out.println("É necessário o parâmetro '-p'.");
            System.exit(1);
        }
        
        Cliente cliente = new Cliente(ip, porta, arq);
    }
    
   
    
    public Cliente(String ip, int porta, String arq) {
        
        try {            
            File file = new File(arq);

            if(!file.exists() || file.isDirectory()) { //verifica se existe o arquivo ou se é diretório
                System.err.println("Arquivo " + arq + " não encontrado");
                System.exit(1);
            }
            
            //Salvar nome do arquivo
            String nome_arquivo = file.getName();
            //Pega o tamanho do arquivo
            long length = file.length();
            //Cria um buffer temporário
            byte[] bytes = new byte[8192];
            //Cria um socket e estabelece conexão com o balão
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(ip, porta), 30000);
            
            InputStream in_arq = new FileInputStream(file);
            DataOutputStream out = new DataOutputStream (socket.getOutputStream());
            DataInputStream in = new DataInputStream(socket.getInputStream());
            
            //envia o tamanho do arquivo para o balão
            out.writeLong(length);
            //envia o nome do arquivo para o balão
            out.writeUTF(nome_arquivo);
            out.flush();
            
            int count;
            while ((count = in_arq.read(bytes)) > 0) {
                out.write(bytes, 0, count);
                if(lerEntrada(in)) break;
            
            }
            
            while(resultado == 0){
                if(lerEntrada(in)) break;
            }
            
            switch (resultado){
                case 1: 
                    System.out.println("Mensagem enviada com sucesso!");
                    break;
                default:
                    System.out.println("Não foi possível enviar a mensagem!");
                    break;
            }
            
            out.close();
            in_arq.close();
            socket.close();
        } catch (UnknownHostException e) {
            System.err.println("Host " + ip + " não encontrado");
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Erro de I/O para a conexão com " + ip);
            System.exit(1);
        }
        
    }
    
    public boolean lerEntrada(DataInputStream in){
        try {
            if(in.available()>0){
                resultado = in.readChar(); 
                return true;
            }        
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
            System.exit(1);
        }
        return false;
    }
    
}
