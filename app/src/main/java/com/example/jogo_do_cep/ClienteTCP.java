package com.example.jogo_do_cep;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClienteTCP extends AppCompatActivity {
    TextView tvStatus, tvNumPìngsPongs;
    Socket clientSocket;
    DataOutputStream socketOutput;
    BufferedReader socketEntrada;
    DataInputStream socketInput;
    Button btConectar;
    EditText edtIp;
    long pings,pongs,gameState;
    TextView estadoJogo;
    EditText cepJogador, cepOponente;
    TextView cepCorreto, estadoCep, cidadeCep, ultimoCep, logradouroCep;
    String cepRecebido, cepRecente, cepSalvo;
    Button definirCep, buscarCep;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cliente_t_c_p);
        tvStatus=findViewById(R.id.tvStatusClient);
        btConectar=findViewById(R.id.btConectar);
     //   tvNumPìngsPongs=findViewById(R.id.tvNumPP_C);
        edtIp=findViewById(R.id.edtIP);

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

        cepOponente.setEnabled(false);
        buscarCep.setEnabled(false);


        gameState = 0;
        cepSalvo = "";
        cepRecebido = "";
        cepRecente = "";
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

    public void conectar(View v) {
        final String ip=edtIp.getText().toString();
        tvStatus.setText("Conectando em "+ip+":9090");




        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {



                    clientSocket = new Socket (ip,9090);

                    tvStatus.post(new Runnable() {
                        @Override
                        public void run() {
                            tvStatus.setText("Conectado com "+ip+":9090");
                        }
                    });




                    socketOutput =
                            new DataOutputStream(clientSocket.getOutputStream());
                    socketInput=
                            new DataInputStream (clientSocket.getInputStream());
                    while (socketInput!=null) {
                        String result = socketInput.readUTF();
                        if (gameState==0 || gameState==1){
                            if (result!=null) {
                                cepRecebido = result;
                                gameState = 4;
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


                } catch (Exception e) {

                    tvStatus.post(new Runnable() {
                        @Override
                        public void run() {
                            tvStatus.setText("Erro na conexão com "+ip+":9090");
                        }
                    });

                    e.printStackTrace();
                }
            }
        });
        t.start();
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
                        btConectar.setEnabled(true);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();



    }

    public void mandarCepInicial(View v){

        cepSalvo = cepJogador.getText().toString();
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (socketOutput!=null) {

                        socketOutput.writeUTF("CEP: "+cepSalvo);
                        socketOutput.flush();
                        atualizarStatus();
                    }else{
                        tvStatus.setText("Cliente Desconectado");
                        btConectar.setEnabled(true);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();



    }
}