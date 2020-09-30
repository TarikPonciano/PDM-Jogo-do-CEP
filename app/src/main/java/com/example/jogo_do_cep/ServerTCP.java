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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

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
                if (gameState==0){
                    if (result!=null) {
                        cepRecebido = result;
                        gameState = 2;

                        atualizarStatus();
                    }

                }else{
                    if (gameState==1){
                        if (result!=null) {
                            cepRecebido = result;
                            gameState = 3;

                            atualizarStatus();
                        }


                    }
                    else{if (gameState==4){
                        if (result.compareTo("Acertei")==0) {

                            gameState = 6;

                            atualizarStatus();
                        }
                        if (result.compareTo("Errei")==0){
                            gameState = 3;
                            atualizarStatus();
                        }

                    }
                    }
                }

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


        if (gameState==0 || gameState==2) {
            cepJogador.post(new Runnable() {
                @Override
                public void run() {
                    cepJogador.setEnabled(true);
                }
            });
        }else{cepJogador.post(new Runnable() {
            @Override
            public void run() {
                cepJogador.setEnabled(false);
            }
        });}

        if (gameState==3){
            buscarCep.post(new Runnable() {
                @Override
                public void run() {
                    buscarCep.setEnabled(true);
                }
            });
        }else{
            buscarCep.post(new Runnable() {
                @Override
                public void run() {
                    buscarCep.setEnabled(false);
                }
            });

        }

        if (gameState==3){
            cepOponente.post(new Runnable() {
                @Override
                public void run() {
                    cepOponente.setEnabled(true);
                }
            });
        }else{
            cepOponente.post(new Runnable() {
                @Override
                public void run() {
                    cepOponente.setEnabled(false);
                }
            });

        }


        Log.d("Teste", "GameState antes do post: "+gameState);
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


                        if(gameState==3){
                            estadoJogo.setText("É a sua vez");

                        }else{ if(gameState==4){
                            estadoJogo.setText("Aguardando o turno do Oponente");


                        }else{ if (gameState==5){
                            estadoJogo.setText("Você Venceu!!!");

                        }else{if (gameState==6){
                            estadoJogo.setText("Você Perdeu :(");
                        }
                        }

                        }
                        }}}

            }
        });

        Log.i("Teste", "GameState final atualizar: "+gameState);

        if (cepRecebido!=""&&(gameState==3||gameState==4)){
            cepCorreto.post(new Runnable() {
                                @Override
                                public void run() {
                                    String subCepRecebido = cepRecebido.substring(3);
                                    cepCorreto.setText(subCepRecebido);}
                            }
            );}

        if (!(gameState==0||gameState==2)){
            definirCep.post(new Runnable() {
                @Override
                public void run() {
                    definirCep.setEnabled(false);
                }
            });
        }

        if (gameState==4||gameState==5||gameState==6){
            if (cepRecente != ""){
                ultimoCep.post(new Runnable(){
                    @Override
                    public void run(){
                        String cepComplemento = cepCorreto.getText().toString();
                        ultimoCep.setText("Último Cep Escolhido: " + cepRecente+cepComplemento);
                    }

                });
            }
            buscarCep.post(new Runnable(){
                @Override
                public void run(){
                    buscarCep.setEnabled(false);
                }

            });
        }else{if(gameState==3){
            buscarCep.post(new Runnable(){
                @Override
                public void run(){
                    buscarCep.setEnabled(true);
                }

            });} else{if(gameState==5||gameState==6){
            buscarCep.post(new Runnable(){
                @Override
                public void run(){
                    buscarCep.setEnabled(false);
                }

            });
        }
        }
        }
        if (gameState==4&&cepOponente.getText().toString()!=""){
            String cepFinal = cepOponente.getText().toString() + cepCorreto.getText().toString();
            final int cepAtual = Integer.parseInt(cepFinal);
            final int cepComparar = Integer.parseInt(cepRecebido);

            estadoCep.post(new Runnable() {
                @Override
                public void run() {
                    if (cepAtual>cepComparar){
                        estadoCep.setText("O Cep correto é menor que o inserido!");
                    }
                    else{estadoCep.setText("O Cep correto é maior que o inserido!");}
                }


            });} else{ if (gameState==5||gameState==6){

                estadoCep.post(new Runnable() {
                    @Override
                    public void run() {
                        estadoCep.setText("Jogo finalizado!");
                    }
                });

            }
        }


    }

    public void mandarCepInicial(View v) {

        cepSalvo = cepJogador.getText().toString();



        Log.v("Teste", "Entrou no botão");

        if (gameState == 0 || gameState == 2) {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {

                        Log.v("Teste", "Entrou no try");

                        URL url = new URL("https://viacep.com.br/ws/" + cepSalvo + "/json/");
                        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();//abertura da conexão TCP
                        conn.setReadTimeout(10000);//timeout da conexão
                        conn.setConnectTimeout(15000);//para ficar esperando
                        conn.setRequestMethod("GET");//serviço esperando uma conexão do tipo "GET"


                        String resposta[] = new String[1];
                        int responseCode = conn.getResponseCode();
                        Log.v("Teste", "Chegou até aqui 3");
                        if(responseCode == HttpsURLConnection.HTTP_OK){
                            Log.v("Teste", "Entrou no check de resposta");

                            BufferedReader br = new BufferedReader(
                                    new InputStreamReader(conn.getInputStream(),"utf-8")
                            );
                            StringBuilder response = new StringBuilder();
                            String responseLine = null;
                            while((responseLine = br.readLine()) != null){
                                response.append((responseLine.trim()));
                            }
                            resposta[0] = response.toString();

                            JSONObject respostaJSON = new JSONObject(resposta[0]);

                            if (resposta[0].compareTo("{\"erro\": true}") == 0) {
                                estadoJogo.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        estadoJogo.setText("Insira um CEP Válido");
                                        Log.v("Teste", "Entrou no erro de CEP");

                                    }
                                });
                            }else{

                        if (socketOutput != null) {

                            socketOutput.writeUTF(cepSalvo);
                            socketOutput.flush();

                            if (gameState == 0) {
                                gameState = 1;
                            } else {
                                if (gameState == 2) {
                                    gameState = 3;
                                }
                            }

                            Log.v("Teste", "GameState envio cep: " + gameState);

                            atualizarStatus();
                        } else {
                            //tvStatus.setText("Cliente Desconectado");
                            // btConectar.setEnabled(true);
                        }}}
                        else{
                            estadoJogo.post(new Runnable() {
                                @Override
                                public void run() {
                                    estadoJogo.setText("Insira um CEP Válido");
                                    Log.v("Teste", "Entrou no CEP Invalido");

                                }
                            });
                        }
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
            t.start();
        }
    }

    public void buscarCep(View v) {
        cepRecente = cepOponente.getText().toString();

       final String cepFinal = cepRecente + cepCorreto.getText().toString();


                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (socketOutput != null) {

                                if (cepRecebido.compareTo(cepFinal)==0) {
                                socketOutput.writeUTF("Acertei");
                                socketOutput.flush();

                                gameState = 5;}

                                else{socketOutput.writeUTF("Errei");
                                    socketOutput.flush();

                                    gameState = 4;}

                                Log.i("Teste", "GameState envio cep: " + gameState);

                                atualizarStatus();
                            } else {
                                //tvStatus.setText("Cliente Desconectado");
                                // btConectar.setEnabled(true);
                            }


                            Log.v("Teste", "Entrou no try");

                            URL url = new URL("https://viacep.com.br/ws/" + cepFinal + "/json/");
                            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();//abertura da conexão TCP
                            conn.setReadTimeout(10000);//timeout da conexão
                            conn.setConnectTimeout(15000);//para ficar esperando
                            conn.setRequestMethod("GET");//serviço esperando uma conexão do tipo "GET"


                            String resposta[] = new String[1];
                            int responseCode = conn.getResponseCode();
                            Log.v("Teste", "Chegou até aqui 3");
                            if(responseCode == HttpsURLConnection.HTTP_OK){
                                Log.v("Teste", "Entrou no check de resposta");

                                BufferedReader br = new BufferedReader(
                                        new InputStreamReader(conn.getInputStream(),"utf-8")
                                );
                                StringBuilder response = new StringBuilder();
                                String responseLine = null;
                                while((responseLine = br.readLine()) != null){
                                    response.append((responseLine.trim()));
                                }
                                resposta[0] = response.toString();

                                JSONObject respostaJSON = new JSONObject(resposta[0]);

                                final String cidade = respostaJSON.getString("cidade");
                                final String logradouro = respostaJSON.getString("logradouro");

                                if (resposta[0].compareTo("{\"erro\": true}") == 0) {
                                    cidadeCep.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            cidadeCep.setText("Cidade: XXXXX");
                                            Log.v("Teste", "Entrou no erro de CEP");

                                        }
                                    });
                                    logradouroCep.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            logradouroCep.setText("Logradouro: XXXXX");
                                            Log.v("Teste", "Entrou no erro de CEP");

                                        }
                                    });
                                }else{
                                    cidadeCep.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            cidadeCep.setText("Cidade: " + cidade);
                                            Log.v("Teste", "Entrou no erro de CEP");

                                        }
                                    });
                                    logradouroCep.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            logradouroCep.setText("Logradouro: " + logradouro);
                                            Log.v("Teste", "Entrou no erro de CEP");

                                        }
                                    });

                                }}else{
                                cidadeCep.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        cidadeCep.setText("Cidade: YYYY");
                                        Log.v("Teste", "Entrou no erro de CEP");

                                    }
                                });
                                logradouroCep.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        logradouroCep.setText("Logradouro: YYYY");
                                        Log.v("Teste", "Entrou no erro de CEP");

                                    }
                                });

                            }



                        } catch (IOException | JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
                t.start();

            }





}
