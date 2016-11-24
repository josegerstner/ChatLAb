/*****************************************************************
 * Chat con java para hacer pruebas 
 * Basado en el chat del canal de Youtube: pildorasinformaticas
*****************************************************************/
package programa;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class ChatLab_Cliente {
	public static void main(String[] args) {
		MarcoCliente miMarco = new MarcoCliente();
		miMarco.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}

@SuppressWarnings("serial")
class MarcoCliente extends JFrame{
	public MarcoCliente(){
		setTitle("- CHATLAB -");
		setBounds(200, 200, 300, 320);
		String nickUser = JOptionPane.showInputDialog("Ingresa tu nick");
		LaminaMarcoCliente miLamina = new LaminaMarcoCliente(nickUser);
		add(miLamina);
		setVisible(true);
		
		addWindowListener(new EnvioOnline(nickUser));
	}
}

//--------------------ENVIO SEÑAL ONLINE---------------------
class EnvioOnline extends WindowAdapter{
	EnvioOnline(String nickUser){
		this.nickUser = nickUser;
	}
	public void windowOpened(WindowEvent e){
		try{
			//armamos el socket del servidor
			Socket miSocket = new Socket("192.168.1.16", 9999);
			PaqueteEnvio datos = new PaqueteEnvio();
			datos.setMensaje(" online");
			datos.setNick(nickUser);
			System.out.println(datos.getMensaje());
			ObjectOutputStream paqueteDatos = new ObjectOutputStream(miSocket.getOutputStream());
			paqueteDatos.writeObject(datos);
			paqueteDatos.close();
			miSocket.close();
		}catch(Exception ex){
			System.out.println(ex.getMessage());
		}
	}
	String nickUser;
}
@SuppressWarnings("serial")
class LaminaMarcoCliente extends JPanel implements Runnable{
	@SuppressWarnings("rawtypes")
	public LaminaMarcoCliente(String nickUser){
		
		JLabel nNick = new JLabel("Nick: ");
		nick = new JLabel();
		nick.setText(nickUser);
		JLabel texto = new JLabel("Online: ");
		ip = new JComboBox();
		nicks = new JComboBox();
		campoChat = new JTextArea(12, 20);
		campoChat.setWrapStyleWord(true);
		campoTexto = new JTextField(20);
		miBoton = new JButton("Enviar");
		miBoton.addActionListener(new EnviaTexto());
		
		Box cajaSuperior = Box.createHorizontalBox();
		cajaSuperior.add(nNick);
		cajaSuperior.add(nick);
		cajaSuperior.add(Box.createHorizontalStrut(10));
		cajaSuperior.add(texto);
		//cajaSuperior.add(ip);
		cajaSuperior.add(nicks);
		
		Box cajaChat = Box.createHorizontalBox();
		cajaChat.add(campoChat);
		
		Box cajaTexto = Box.createHorizontalBox();
		cajaTexto.add(campoTexto);
		
		Box cajaEnviar = Box.createHorizontalBox();
		cajaEnviar.add(miBoton);
		
		Box contenedor = Box.createVerticalBox();
		contenedor.add(cajaSuperior);
		contenedor.add(cajaChat);
		contenedor.add(cajaTexto);
		contenedor.add(cajaEnviar);
		add(contenedor);
		
		cajaTexto.addKeyListener(new EventoTeclado());
		
		Thread hiloChat = new Thread(this);
		hiloChat.start();
	}
	private class EventoTeclado extends KeyAdapter{
		public void keyPressed(KeyEvent e){
			System.out.print(e.getKeyCode());
			if(KeyEvent.VK_ENTER == e.getKeyCode()){
				//enviar.actionPerformed(e.getSource());
				//imprimo lo que escribí
				
				campoChat.append("\n" + campoTexto.getText());
				try{
					Socket miSocket = new Socket("192.168.1.16", 9999);
					
					//instanciamos el paquete a enviar
					PaqueteEnvio datos = new PaqueteEnvio();
					datos.setNick(nick.getText());
					datos.setIp(ip.getSelectedItem().toString());
					datos.setMensaje(campoTexto.getText());
					
					ObjectOutputStream paqueteDatos = new ObjectOutputStream(miSocket.getOutputStream());
					paqueteDatos.writeObject(datos);
					//paqueteDatos.close();
					miSocket.close();
				}catch(Exception exc1){
					System.out.println(exc1.getMessage());
				}
				campoTexto.setText("");
			}
		}
	}
	private class EnviaTexto implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			//imprimo lo que escribí
			campoChat.append("\nYo: " + campoTexto.getText());
			try{
				Socket miSocket = new Socket("192.168.1.16", 9999);
				
				//instanciamos el paquete a enviar
				PaqueteEnvio datos = new PaqueteEnvio();
				datos.setNick(nick.getText());
				datos.setIp(ip.getSelectedItem().toString());
				datos.setMensaje(campoTexto.getText());
				
				ObjectOutputStream paqueteDatos = new ObjectOutputStream(miSocket.getOutputStream());
				paqueteDatos.writeObject(datos);
				//paqueteDatos.close();
				miSocket.close();
			}catch(Exception exc1){
				System.out.println(exc1.getMessage());
			}
			campoTexto.setText("");
		}
	}
	
	//Recibo la info
	@SuppressWarnings("unchecked")
	public void run(){
		try{
			System.out.println("El cliente se pone a la escucha");
			@SuppressWarnings("resource")
			ServerSocket servidorCliente = new ServerSocket(9090);
			Socket cliente;
			PaqueteEnvio paqueteRecibido;
			while(true){
				cliente = servidorCliente.accept();
				ObjectInputStream flujoEntrada = new ObjectInputStream(cliente.getInputStream());
				paqueteRecibido = (PaqueteEnvio) flujoEntrada.readObject();
				
				if(!paqueteRecibido.getMensaje().equals(" online")){
					//estamos chateando
					campoChat.append("\n" + paqueteRecibido.getNick() + ": " + paqueteRecibido.getMensaje());
				}else{
					//me devuelve la ip
					//campoChat.append("\n" + paqueteRecibido.getIPs());
					
					ArrayList<String> ipsMenu = new ArrayList<String>();
					ipsMenu = paqueteRecibido.getIPs();
					ip.removeAllItems();
					for (String z : ipsMenu) {
						ip.addItem(z);
					}
					ArrayList<String> nicksMenu = new ArrayList<String>();
					nicksMenu = paqueteRecibido.getNicks();
					nicks.removeAllItems();
					for (String z : nicksMenu) {
						nicks.addItem(z);
					}
				}
			}
		}catch(Exception e2){
			System.out.println(e2.getMessage());
		}
	}
	public String getNick(){
		return nick.getText();
	}
	
	private JTextField campoTexto;
	@SuppressWarnings("rawtypes")
	private JComboBox ip, nicks;
	private JLabel nick;
	private JButton miBoton;
	private JTextArea campoChat;
}
@SuppressWarnings("serial")
class PaqueteEnvio implements Serializable{
	public String getNick() {
		return nick;
	}
	public void setNick(String nick) {
		this.nick = nick;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public String getMensaje() {
		return mensaje;
	}
	public void setMensaje(String mensaje) {
		this.mensaje = mensaje;
	}
	public ArrayList<String> getIPs() {
		return IPs;
	}
	public void setIPs(ArrayList<String> iPs) {
		IPs = iPs;
	}
	public ArrayList<String> getNicks() {
		return nicks;
	}
	public void setNicks(ArrayList<String> nicks) {
		this.nicks = nicks;
	}
	private String nick, ip, mensaje;
	private ArrayList<String> IPs, nicks;
}