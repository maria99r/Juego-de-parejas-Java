package tarea09;

import java.util.*;

/**
 * Juego de Memoria. Implementa la lógica del juego "Buscar parejas"
 * 
 * @author Fran Jiménez
 */
public class JuegoMemoria {

    private final int columnas = 4;
    private final int numParejas = (columnas * columnas)/2; // N parejas
    private int parejasDescubiertas = 0;

    // si quieres definir distintos tipos de juego puedes hacerlo aquí
    private String [] tipo = { "escudos", "personas", "plantas" };

    private final ArrayList<String> cartas = new ArrayList<>(Arrays.asList("1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16" ));
    private ArrayList<String> tablero;
    
    public void iniciarJuego(){
        Collections.shuffle(this.cartas); // se desordena la lista de todos los equipos
        
        tablero = new ArrayList<>();
        
        for (int i = 0; i < numParejas; i++) { // se introducen N parejas
            // cada carta se añade dos veces (una pareja)
            tablero.add(this.cartas.get(i));
            tablero.add(this.cartas.get(i) );
        }
        
        Collections.shuffle(this.tablero); // se barajan las N cartas elegidas
    }

    public String getCartaPosicion(int indice){
        return this.tablero.get(indice);
    }

    public boolean compruebaJugada(int indice1, int indice2){
        boolean parejaEncontrada = false;
        
        if(this.tablero.get(indice1).equals(this.tablero.get(indice2))){
            this.tablero.set(indice1,"-"); // pareja encontrada en indice1 (se coloca - en la casilla)
            this.tablero.set(indice2,"-"); // pareja encontrada en indice2 (se coloca - en la casilla)
            this.parejasDescubiertas++;
            parejaEncontrada = true;
        }
        return parejaEncontrada;
    }
    
    public boolean compruebaFin (){
        return this.parejasDescubiertas==this.numParejas;            
    }
    public int getTotalParejas(){
        return this.numParejas;
    }
    
    public int getParejasDescubiertas(){
        return this.parejasDescubiertas;
    }
    
    public ArrayList<String> getTablero(){
        return tablero;
    }
    
    public int getTamanoTablero(){
        return columnas*columnas;
    }
    
    public String getTipoPartida(){
        return this.tipo[ (int)(Math.random()*3) ] ;
    }
}
