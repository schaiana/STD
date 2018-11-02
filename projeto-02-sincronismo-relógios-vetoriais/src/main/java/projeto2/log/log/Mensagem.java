/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package projeto2.log.log;


public class Mensagem implements Comparable {
    public String processo;
    public int mensagem;
    public int[] relogioRecebido;
    
    public Mensagem(String processo, int mensagem, int[] relogioRecebido){
        this.processo = processo;
        this.mensagem = mensagem;
        this.relogioRecebido = relogioRecebido;
    }
    
    @Override
    public int compareTo(Object o) {
        Mensagem msg2 = (Mensagem) o;
        
        boolean isGreater = false;
        boolean isLess = false;

        int tamanhoRelogio = this.relogioRecebido.length;
        
        for(int z = 0; z < tamanhoRelogio; z++){
            int diferenca = this.relogioRecebido[z] - msg2.relogioRecebido[z];
            if(diferenca > 0) isGreater = true;
            if(diferenca < 0) isLess = true;
        }

        if(isGreater && isLess) return 0;
        if(isLess) return -1;
        if(isGreater) return 1;
        return 0; // se forem iguais
    }
}
