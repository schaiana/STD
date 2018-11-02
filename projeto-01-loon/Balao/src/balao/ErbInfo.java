/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package balao;


/**
 *
 * @author schaiana
 */
public class ErbInfo {
    
    String ip;
    Double latitude;
    Double longitude;
    int porta;
    Double distancia;
    
    public ErbInfo(String ip, int porta, Double latitude, Double longitude) {
        this.ip = ip;
        this.porta = porta;   
        this.latitude = latitude;
        this.longitude = longitude;  
        this.distancia = 1000.0;
    }
    
    public void calculaDistancia(Double erbLat, Double erbLong){
        this.distancia = Balao.distance(latitude, erbLat, longitude, erbLong);
    }
    
    public Double getDistancia(){
        return this.distancia;
    }
}


