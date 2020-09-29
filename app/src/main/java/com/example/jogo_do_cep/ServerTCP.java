package com.example.jogo_do_cep;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerTCP extends AppCompatActivity {

    TextView tvStatus, tvNumPìngsPongs;
    ServerSocket welcomeSocket;
    DataOutputStream socketOutput;
    BufferedReader socketEntrada;
    DataInputStream fromClient;
    boolean continuarRodando = false;
    Button btLigarServer;
    TextView estadoJogo;
    EditText cepJogador, cepOponente;
    TextView cepCorreto, estadoCep, cidadeCep, ultimoCep, logradouroCep;
    String cepRecebido, cepRecente, cepSalvo;
    Button definirCep, buscarCep;

    long pings,pongs, gameState;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);
        tvStatus=findViewById(R.id.textView);
        btLigarServer=findViewById(R.id.btLigarServer);
        //tvNumPìngsPongs=findViewById(R.id.tvNumPingsPongs);

        estadoJogo = findViewById(R.id.tvEstadoJogo);
        cepJogador= findViewById(R.id.editCepJogador);
        cepOponente = findViewById(R.id.editBuscarCep);
        cepCorreto = findViewById(R.id.tvCepCorreto);
        definirCep = findViewById(R.id.btnDefinirCep);
        buscarCep = findViewById(R.id.btnBuscarCep);
        estadoCep = findViewById(R.id.tvEstadoCep);

        cidadeCep = findViewById(R.id.tvCidadeCep);
        ultimoCep = findViewById(R.id.tvUltimoCep);
        logradouroCep = findViewById(R.id.tvLogradouroCep);


        gameState = 0;
        cepRecebido = "";
        cepRecente = "";
        cepSalvo = "";
        atualizarStatus();

    }

    public void ligarServidor(View v){
        ConnectivityManager connManager;
        connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        Network[] networks = connManager.getAllNetworks();


        for (Network minhaRede:networks){
            NetworkInfo netInfo= connManager.getNetworkInfo(minhaRede);
            if(netInfo.getState().equals(NetworkInfo.State.CONNECTED)){
                NetworkCapabilities propDaRede = connManager.getNetworkCapabilities(minhaRede);

                if (propDaRede.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)){

                    WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);

                    String macAddress = wifiManager.getConnectionInfo().getMacAddress();
                    Log.v ("PDM","Wifi - MAC:"+macAddress);

                    int ip= wifiManager.getConnectionInfo().getIpAddress();
                    String ipAddress = String.format("%d.%d.%d.%d", (ip & 0xff), (ip >> 8 & 0xff), (ip >> 16 & 0xff), (ip >>24 & 0xff));

                    Log.v ("PDM","Wifi - IP:"+ipAddress);
                    tvStatus.setText("Ativo em:"+ipAddress);

                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                         ligarServerCodigo();
                        }
                    });
                    t.start();
                }

            }
        }


    }

    public void mandarPing(View v){
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (socketOutput!=null) {
                        socketOutput.writeUTF("PING");
                        socketOutput.flush();
                        pings++;
                        atualizarStatus();
                    }else{
                        tvStatus.setText("Cliente Desconectado");
                        btLigarServer.setEnabled(true);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();



    }
    public void desconectar(){
        try {
            if(socketOutput!=null) {
                socketOutput.close();
            }
            //Habilitar o Botão de Ligar
            btLigarServer.post(new Runnable() {
                @Override
                public void run() {
                    btLigarServer.setEnabled(true);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void ligarServerCodigo() {
        //Desabilitar o Botão de Ligar
        btLigarServer.post(new Runnable() {
            @Override
            public void run() {
                btLigarServer.setEnabled(false);
            }
        });

        String result = "";
        try {
            Log.v("SMD", "Ligando o Server");
            welcomeSocket = new ServerSocket(9090);
            Socket connectionSocket = welcomeSocket.accept();
            Log.v("SMD", "Nova conexão");



            //Instanciando os canais de stream
            fromClient = new DataInputStream(connectionSocket.getInputStream());
            socketOutput = new DataOutputStream(connectionSocket.getOutputStream());
            continuarRodando = true;
            while (continuarRodando) {
                result = fromClient.readUTF();
                if (gameState==0 || gameState==1){
                    if (result!=null) {
                        cepRecebido = result;
                        gameState = 3;
                      //  atualizarCep();
                        atualizarStatus();
                    }

                }else{
                    if (result.compareTo("PING") == 0) {
                        //enviar Pong
                        pongs++;
                        socketOutput.writeUTF("PONG");
                        socketOutput.flush();
                        atualizarStatus();
                    }}
            }

            Log.v("SMD", result);
            //Enviando dados para o servidor
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

   public void somarNumPongs(){
       pongs++;
       atualizarStatus();

   }

    public void atualizarStatus() {
        //Método que vai atualizar os pings e pongs, usando post para evitar problemas com as threads

        if(gameState==0||gameState==2){


            definirCep.setEnabled(true);
            cepJogador.setEnabled(true);
        }
        else{
            definirCep.setEnabled(false); cepJogador.setEnabled(false);
            if(gameState==1){

            }else if(gameState==3){

                cepOponente.setEnabled(true);
                buscarCep.setEnabled(true);
            }else if(gameState==4){

                cepOponente.setEnabled(false);
                buscarCep.setEnabled(false);

            }

        }

        estadoJogo.post(new Runnable() {
            @Override
            public void run() {

                if(gameState==0||gameState==2){

                    estadoJogo.setText("Insira a posição da máquina do tempo");

                }
                else{

                    if(gameState==1){
                        estadoJogo.setText("Aguardando oponente inserir posição");
                    }else{
                        if (cepRecebido!=""){
                            String subCepRecebido = cepRecebido.substring(2);
                            cepOponente.setText(subCepRecebido);}

                        if(gameState==3){
                            estadoJogo.setText("É a sua vez");

                        }else{ if(gameState==4){
                            estadoJogo.setText("Aguardando o turno do Oponente");


                        }

                        }}}

            }
        });


    }

    public void mandarCepInicial(View v){

        cepSalvo = cepJogador.getText().toString();
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (socketOutput!=null) {

                        socketOutput.writeUTF(cepSalvo);
                        socketOutput.flush();
                        atualizarStatus();
                    }else{
                        tvStatus.setText("Cliente Desconectado");
                        //btConectar.setEnabled(true);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();



    }

}
