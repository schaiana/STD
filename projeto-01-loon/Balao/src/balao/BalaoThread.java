/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package balao;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 *
 * @author schaiana
 */
public class BalaoThread implements Runnable {

    private Balao balao;
    private Socket socket;
    private Socket socketBalaoERB = null;
    private char resultado = 0;
    
    public BalaoThread(Socket s, Balao b){
        this.socket = s;
        this.balao = b;
    }
    
    @Override
    public void run() {
        try {
            if(conectaERBouBalao()){
                repassaArquivo();
            } else {
                //envia mensagem 2 para o cliente, erro ao enviar mensagem
                enviaMensagemCliente((char)2);
                System.out.println("Erro ao enviar mensagem!");
            }
            
            this.socket.close();
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
        
         
    }
    
    private boolean conectaERBouBalao(){
        ArrayList<ErbInfo> erbs = balao.getErbs();
        int i;
        
        //tenta conectar em uma das ERBs, lembrando que elas já estão ordenadas da mais perto para a mais longe
        for(i = 0; i < erbs.size(); i++){
            ErbInfo erb = erbs.get(i);
            if (erb.distancia >= 40) continue; 
            
            try {
                //tenta conectar na ERB
                Socket s = new Socket();
                s.connect(new InetSocketAddress(erb.ip, erb.porta), 30000);
                socketBalaoERB = s;
                //se chegou aqui é porque conseguiu conectar na ERB, retorna verdadeiro
                return true;
            } catch (UnknownHostException e) {
                System.err.printf("ERB %s:%d não encontrada\n", erb.ip, erb.porta);
            } catch (IOException e) {
                System.err.printf("Erro de conexão com a ERB %s:%d\n", erb.ip, erb.porta);
            }
        }
        System.err.println("Erro: não foi possível conectar em nenhuma ERB em um raio de 40 km.");
        //não conseguiu conectar em nenhuma ERB, tenta conectar no vizinho
        try {
            Socket s = new Socket();
            s.connect(new InetSocketAddress(balao.getVizinhoIP(), balao.getVizinhoPorta()), 30000);
            socketBalaoERB = s;
            
            return true;
        } catch (UnknownHostException e) {
            System.err.printf("Balão vizinho %s:%d não encontrado\n", balao.getVizinhoIP(), balao.getVizinhoPorta());
        } catch (IOException e) {
            System.err.printf("Erro de conexão com o balão vizinho %s:%d\n", balao.getVizinhoIP(), balao.getVizinhoPorta());
        }
        
        return false;    
    }
    
    private void enviaMensagemCliente(char codigo){
        try {
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeChar(codigo);
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
        
    }
    
    private void repassaArquivo(){
        try {
            long tamanho_arquivo = 0;
            
            DataInputStream in_cliente = new DataInputStream(socket.getInputStream());
            
            DataOutputStream out_balaoErb = new DataOutputStream(socketBalaoERB.getOutputStream());
            DataInputStream in_balaoErb = new DataInputStream(socketBalaoERB.getInputStream());
            
            //lê o tamanho do arquivo do cliente
            tamanho_arquivo = in_cliente.readLong();
            //envia o tamanho do arquivo para ERB/Balão
            out_balaoErb.writeLong(tamanho_arquivo);
            //lê o nome do arquivo
            String nome_arquivo = in_cliente.readUTF();
            //envia o nome do arquivo
            out_balaoErb.writeUTF(nome_arquivo);
            
            byte[] bytes = new byte[8192];
            int count;
            long total = 0;
            
            while (total != tamanho_arquivo) {
                count = in_cliente.read(bytes);
                out_balaoErb.write(bytes, 0, count);
                total += count;
                if(lerEntrada(in_balaoErb)) break;
            }
            
            while(resultado == 0){
                if(lerEntrada(in_balaoErb)) break;
            }
            
            switch (resultado){
                case 1: 
                    System.out.println("Mensagem repassada com sucesso!");
                    break;
                default:
                    System.out.println("Não foi possível repassar a mensagem!");
                    break;
            }
            
            enviaMensagemCliente(resultado);
            
            out_balaoErb.close();
            in_balaoErb.close();
            in_cliente.close();  
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
    }
    
    private boolean lerEntrada(DataInputStream in){
        try {
            if(in.available()>0){
                resultado = in.readChar(); 
                
                return true;
            }        
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
        return false;
    }
}
