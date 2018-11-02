package erb;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author schaiana
 */
public class ErbThread implements Runnable {
    
    private Socket sBalao;
    
    public ErbThread(Socket s){
        sBalao = s;
    }
    
    @Override
    public void run() {
        try {
            long tamanho_arquivo = 0;
            
            
            DataOutputStream out_balaoErb = new DataOutputStream(sBalao.getOutputStream());
            DataInputStream in_balaoErb = new DataInputStream(sBalao.getInputStream());

            //lê o tamanho do arquivo do cliente
            tamanho_arquivo = in_balaoErb.readLong();
            //lê o nome do arquivo
            String nome_arquivo = in_balaoErb.readUTF();
            
            OutputStream out_arq = new FileOutputStream(nome_arquivo);

            byte[] bytes = new byte[8192];
            int count;
            long total = 0;
            
            while (total!=tamanho_arquivo) {
                count = in_balaoErb.read(bytes);
                out_arq.write(bytes, 0, count);
                total += count;
            }
            
            out_balaoErb.writeChar((char)1);

            System.out.printf("Recebeu arquivo %s!\n", nome_arquivo);
            
            out_arq.close();
            in_balaoErb.close();
            out_balaoErb.close();
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
    }
    
}
