package tarea09;

import java.io.File;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

/**
 * Clase Controladora
 *
 * @author MARIA ROSALES ROMAN
 */
public class MemoriaController implements Initializable {

    // definición de variables internas para el desarrollo del juego
    private JuegoMemoria juego;         // instancia que controlará el estado del juego (tablero, parejas descubiertas, etc)
    private ArrayList<Button> cartas;   // array para almacenar referencias a las cartas @FXML definidas en la interfaz 
    private int segundos = 0;             // tiempo de juego
    private boolean primerBotonPulsado = false, segundoBotonPulsado = false; // indica si se han pulsado ya los dos botones para mostrar la pareja
    private int idBoton1, idBoton2;     // identificadores de los botones pulsados
    private boolean esPareja;           // almacenará si un par de botones pulsados descubren una pareja o no
    private int intentos = 0;           
    String tipo;                        // tipo de cartas al azar

    @FXML
    private GridPane contenedorGrid;    // grid donde se almacenan los botones de las cartas
    @FXML
    private AnchorPane main;      // panel principal (incluye la notación @FXML porque hace referencia a un elemento de la interfaz)
    @FXML
    private Label numeroIntentos, numeroTiempo; // etiqueta para mostrar el número de intentos y tiempo

    // linea de tiempo para gestionar la finalización del intento al pasar 1.5 segundos
    private final Timeline finIntento = new Timeline(new KeyFrame(Duration.seconds(1.5), e -> finalizarIntento()));

    // linea de tiempo para gestionar el contador de tiempo del juego
    private Timeline contadorTiempo;

    // MediaPlayer para la música de fondo
    private MediaPlayer musicaPlay;

    /**
     * Método interno que configura todos los aspectos necesarios para
     * inicializar el juego.
     *
     * @param url No utilizaremos este parámetro (null).
     * @param resourceBundle No utilizaremos este parámetro (null).
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        juego = new JuegoMemoria(); // instanciación del juego (esta instancia gestionará el estado de juego)
        juego.iniciarJuego();       // comienzo de una nueva partida
        cartas = new ArrayList<>(); // inicialización del ArrayList de referencias a cartas @FXML
        intentos = 0;               // inicializacion de intentos y del tiempo a 0
        segundos = 0;
        numeroIntentos.setText(String.format("%d", intentos));   //pongo el numero de intentos en pantalla

        // guarda en el ArrayList cartas todas las referencias a los botones cartas
        for (Node boton : contenedorGrid.getChildren()) {
            if (boton instanceof Button) {
                cartas.add((Button) boton);
            }
        }

        // inicialización de todos los aspectos necesarios
        // creo contador de tiempo de la partida cada 1 segundo
        contadorTiempo = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            segundos++;
            numeroTiempo.setText(String.format("%d", segundos)); //muestro el tiempo por pantalla
        }));
        contadorTiempo.setCycleCount(Timeline.INDEFINITE);  // reproducción infinita del contador
        contadorTiempo.play();                                // inicio el contador 

        // música de fondo para que se reproduce cuando se inicia el juego
        Media musicaFondo = new Media(getClass().getResource("/tarea09/assets/sonidos/musica.mp3").toExternalForm());
        musicaPlay = new MediaPlayer(musicaFondo);
        musicaPlay.setCycleCount(MediaPlayer.INDEFINITE); // Reproducción en bucle de la musica
        musicaPlay.play(); // inicio la musica

        tipo = juego.getTipoPartida(); // defino el tipo de cartas
    }

    /**
     * Acción asociada al botón <strong>Comenzar nuevo juego</strong> que
     * permite comenzar una nueva partida.
     *
     * Incluye la notación @FXML porque será accesible desde la interfaz de
     * usuario
     *
     * @param event Evento que ha provocado la llamada a este método
     */
    @FXML
    private void reiniciarJuego(ActionEvent event) {

        // detener el contador de tiempo 
        contadorTiempo.stop();

        // detener la reproducción de la música de fondo
        musicaPlay.stop();


        /* hacer visibles las 16 cartas de juego ya que es posible que no todas estén visibles 
           si se encontraron parejas en la partida anterior */
        for (Button carta : cartas) {
            carta.setVisible(true);
            carta.setGraphic(null);
        }

        // Remover la imagen de victoria si estuviera al haber ganado y querer reiniciar el juego
        for (Node node : new ArrayList<>(contenedorGrid.getChildren())) {
            if (node instanceof ImageView) {
                contenedorGrid.getChildren().remove(node); // busco que el contenedor tenga una imagen como nodo y la borro
            }
        }

        // llamar al método initialize para terminar de configurar la nueva partida
        initialize(null, null);

    }

    /**
     * Este método deberá llamarse cuando el jugador haga clic en cualquiera de
     * las cartas del tablero.
     *
     * Incluye la notación @FXML porque será accesible desde la interfaz de
     * usuario
     *
     * @param event Evento que ha provocado la llamada a este método (carta que
     * se ha pulsado)
     */
    @FXML
    private void mostrarContenidoCasilla(ActionEvent event) {

        Button boton = (Button) event.getSource(); // obtener el botón pulsado
        int num = cartas.indexOf(boton); // obtener el índice del botón en el ArrayList de cartas

        if (primerBotonPulsado && num == idBoton1) {
            return; // Ignoro si pulso el mismo boton
        }

        String valor = juego.getCartaPosicion(num);

        // descubrir la imagen asociada al boton
        mostrarImagenCarta(boton, valor);

        // efectos de sonido
        Media sonidoAcierto = new Media(getClass().getResource("/tarea09/assets/sonidos/pareja.mp3").toExternalForm());
        Media sonidoFallo = new Media(getClass().getResource("/tarea09/assets/sonidos/noPareja.mp3").toExternalForm());

        // gestionar correctamente la pulsación de las cartas (si es la primera o la segunda)
        if (!primerBotonPulsado) {
            idBoton1 = num;
            primerBotonPulsado = true;

        } else if (!segundoBotonPulsado) {
            idBoton2 = num;
            segundoBotonPulsado = true;

            // identificar si se ha encontrado una pareja o no
            esPareja = juego.compruebaJugada(idBoton1, idBoton2);

            // reproducir el efecto de sonido 
            if (esPareja) {
                MediaPlayer mediaPlayer = new MediaPlayer(sonidoAcierto);
                mediaPlayer.play();
            } else {
                MediaPlayer mediaPlayer = new MediaPlayer(sonidoFallo);
                mediaPlayer.play();
            }
            
            intentos++; // aumento los intentos y los muestro en pantalla
            numeroIntentos.setText(String.format("%d", intentos));

            // finalizar intento (usar el timeline para que haga la llamada transcurrido el tiempo definido)
            finIntento.play();
        }
    }

    // voy a crear un metodo para mostrar la imagen de las carta al pulsar el boton
    private void mostrarImagenCarta(Button boton, String valor) {

        // segun el tipo de partida, muestro las cartas
        Image imagenCarta = new Image(getClass().getResourceAsStream("/tarea09/assets/cartas/" + tipo + "/" + valor + ".png"));
        ImageView imagen = new ImageView(imagenCarta);

        // Ajustar el tamaño de la imagen al 90% del tamaño del botón para que no sobresalga
        double ancho = boton.getWidth() * 0.9;
        double alto = boton.getHeight() * 0.9;
        imagen.setFitWidth(ancho);
        imagen.setFitHeight(alto);

        // centro imagen
        imagen.setPreserveRatio(true);

        // pongo la imagen en el boton
        boton.setGraphic(imagen);
    }

    // ocultar la imagen de las carta al pulsar el boton
    private void ocultarImagenCarta(Button carta) {
        carta.setGraphic(null);
    }

    /**
     * Este método permite finalizar un intento realizado. Se pueden dar dos
     * situaciones:
     * <ul>
     * <li>Si se ha descubierto una pareja: en este caso se ocultarán las cartas
     * desapareciendo del tablero. Además, se debe comprobar si la pareja
     * descubierta es la última pareja del tablero y en ese caso terminar la
     * partida.</li>
     * <li>Si NO se ha descubierto una pareja: las imágenes de las cartas deben
     * volver a ocultarse (colocando su imagen a null).</li>
     * </ul>
     * Este método será interno y NO se podrá acceder desde la interfaz, por lo
     * que NO incorpora notación @FXML.
     */
    private void finalizarIntento() {

        if (esPareja) {
            // hacer desaparecer del tablero las cartas seleccionadas si forman una pareja
            cartas.get(idBoton1).setVisible(false);
            cartas.get(idBoton2).setVisible(false);
        } else {
            // Ocultar las imágenes de las cartas seleccionadas si NO forman una pareja
            ocultarImagenCarta(cartas.get(idBoton1));
            ocultarImagenCarta(cartas.get(idBoton2));

            // oculto todas las imagenes por si se han pulsado rapido otros botones
            for (Button carta : cartas) {
                ocultarImagenCarta(carta);
            }

        }

        // Comprobar el final de partida
        if (juego.compruebaFin()) {
            // si es final de partida mostra el mensaje de victoria y detener el temporizador y la música
            // Crear un ImageView con la imagen de victoria
            ImageView imagenVictoria = new ImageView(new Image(getClass().getResourceAsStream("/tarea09/assets/interfaz/victoria.png")));

            // Establecer el tamaño de la imagen para que cubra todo el tablero grid
            imagenVictoria.setFitWidth(contenedorGrid.getWidth());
            imagenVictoria.setFitHeight(contenedorGrid.getHeight());

            // Agregar imagenVictoria al grid principal
            contenedorGrid.getChildren().add(imagenVictoria);

            // Detener el temporizador
            contadorTiempo.stop();

            // Reproducir música de victoria
            Media sonidoVictoria = new Media(getClass().getResource("/tarea09/assets/sonidos/tada.mp3").toExternalForm());
            MediaPlayer mediaPlayer = new MediaPlayer(sonidoVictoria);
            mediaPlayer.play();
        }

        // Reiniciar variables para el próximo intento
        primerBotonPulsado = false;
        segundoBotonPulsado = false;
        esPareja = false;
        finIntento.stop(); // Detener el timeline de fin de intento
    }

    /**
     * Este método permite salir de la aplicación. Debe mostrar una alerta de
     * confirmación que permita confirmar o rechazar la acción Al confirmar la
     * acción la aplicación se cerrará (opcionalmente, se puede incluir algún
     * efecto de despedida) Incluye la notación @FXML porque será accesible
     * desde la interfaz de usuario
     */
    @FXML
    private void salir() {

        // Alerta de confirmación que permita elegir si se desea salir o no del juego
        // SOLO si se confirma la acción se cerrará la ventana y el juego finalizará. 
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle("Confirmar salida");
        alerta.setHeaderText("¿Estás seguro de que deseas salir?");

        // reproduzco sonido 
        Media sonidoSalir = new Media(getClass().getResource("/tarea09/assets/sonidos/bye_bye.mp3").toExternalForm());
        MediaPlayer mediaPlayer = new MediaPlayer(sonidoSalir);
        mediaPlayer.play();

        // Obtengo la respuesta del usuario con un boton que tenga que pulsar para que confirme
        Optional<ButtonType> resultado = alerta.showAndWait();

        // Verificar si el usuario confirmó la salida (pulso el boton)
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            // se cierra la aplicación
            Platform.exit();
        }
    }
}
