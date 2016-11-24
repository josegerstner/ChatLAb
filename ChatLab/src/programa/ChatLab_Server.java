/*****************************************************************
 * Chat con java para hacer pruebas 
 * Basado en el chat del canal de Youtube: pildorasinformaticas
*****************************************************************/
package programa;

import java.awt.BorderLayout;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public class ChatLab_Server {
	public static void main(String[] args) {
		try{
		MarcoServidor miMarco = new MarcoServidor();
		miMarco.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		}catch(Exception e){
			System.out.println("Se ha producido una excepción: " + e.getMessage());
		}
	}
}
@SuppressWarnings("serial")
class MarcoServidor extends JFrame implements Runnable{
	public MarcoServidor(){
		setBounds(600, 200, 400, 200);
		setTitle("ChatLab - Server");
		JPanel miLamina = new JPanel();
		miLamina.setLayout(new BorderLayout());
		areaTexto = new JTextArea();
		areaTexto.setWrapStyleWord(true);
		//no hago visible el areaTexto para que sea anónimo, el server no ve nada
		//miLamina.add(areaTexto, BorderLayout.CENTER);
		
		JLabel leyenda = new JLabel("Este servidor no lee textos de usuarios");
		miLamina.add(leyenda, BorderLayout.CENTER);
		
		add(miLamina);
		setVisible(true);
		
		Thread miHilo = new Thread(this);
		miHilo.start();
	}
	public void run(){
		try {
			@SuppressWarnings("resource")
			ServerSocket servidor = new ServerSocket(9999);
			String nick, ip, mensaje;
			ArrayList<String> listaIP = new ArrayList<String>();
			ArrayList<String> listaNicks = new ArrayList<String>();
			@SuppressWarnings("unused")
			HashMap<String, String>user = new HashMap<String, String>();
			PaqueteEnvio paqueteRecibido;
			while(true){
				//recibimos la info
				Socket miSocket = servidor.accept();
				ObjectInputStream paqueteDatos = new ObjectInputStream(miSocket.getInputStream());
				paqueteRecibido = (PaqueteEnvio) paqueteDatos.readObject();
				//paqueteDatos.close();
				
				//desmenuzamos los mensajes
				nick = paqueteRecibido.getNick();
				ip = paqueteRecibido.getIp();
				mensaje = paqueteRecibido.getMensaje();
				
				if(!mensaje.equals(" online")){
					//-----------------------------------------------
					//imprimimos en nuestro JTextField al pedo
					//se podría hacer en un archivo o por consola si hace falta ;)
					//o no hacerlo para que sea anónimo
					//areaTexto.append(nick + ": " + mensaje + "\n\tPara " + ip + "\n");
					
					//enviamos la info al destinatario
					Socket enviaDestinatario = new Socket(ip, 9090);
					ObjectOutputStream paqueteReenvio = new ObjectOutputStream(enviaDestinatario.getOutputStream());
					paqueteReenvio.writeObject(paqueteRecibido);
					
					//cerramos las conexiones
					paqueteReenvio.close();
					enviaDestinatario.close();
					miSocket.close();
				}else{
					//----------DETECTA ONLINE----------------------
					InetAddress localizacion = miSocket.getInetAddress();
					String ipRemota = localizacion.getHostAddress();
					System.out.println("Online: " + ipRemota);
					System.out.println("Online: " + nick);
					
					//envío las ips
					listaIP.add(ipRemota);
					paqueteRecibido.setIPs(listaIP);
					
					//prueba para enviar nicks
					listaNicks.add(nick);
					paqueteRecibido.setNicks(listaNicks);
					
					//ENVÍO ARRAY DE IPs
					for (String z : listaIP) {
						System.out.println("Array: " + z);
						Socket enviaDestinatario = new Socket(z, 9090);
						ObjectOutputStream paqueteReenvio = new ObjectOutputStream(enviaDestinatario.getOutputStream());
						paqueteReenvio.writeObject(paqueteRecibido);
						paqueteReenvio.close();
						enviaDestinatario.close();
						miSocket.close();
					}
				}
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	private JTextArea areaTexto;
}