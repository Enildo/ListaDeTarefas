package com.example.listadetarefas

import android.content.Intent
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Bundle
import android.provider.Settings
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var audioHelper: AudioHelper
    private lateinit var audioManager: AudioManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Vinculando o código à UI (Interface)
        val listView: ListView = findViewById(R.id.listView)
        val btnConfigBluetooth: Button = findViewById(R.id.btnConfigBluetooth)

        // Criando uma lista de exemplo para a empresa Doma
        val tarefas = arrayOf("Tarefa 1: Avisos", "Tarefa 2: Alerta Segurança", "Tarefa 3: Lembrete")
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, tarefas)
        listView.adapter = adapter

        // Inicializando as ferramentas de Áudio
        audioHelper = AudioHelper(this)
        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager

        // Verificação Inicial: Tem som saindo por algum lugar?
        val isSpeakerAvailable = audioHelper.audioOutputAvailable(AudioDeviceInfo.TYPE_BUILTIN_SPEAKER)
        val isBluetoothConnected = audioHelper.audioOutputAvailable(AudioDeviceInfo.TYPE_BLUETOOTH_A2DP)

        if (isSpeakerAvailable || isBluetoothConnected) {
            Toast.makeText(this, "Dispositivo de áudio pronto!", Toast.LENGTH_SHORT).show()
        }

        // Detecção Dinâmica: Avisa quando conecta ou desconecta fone
        audioManager.registerAudioDeviceCallback(object : AudioDeviceCallback() {
            override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>?) {
                super.onAudioDevicesAdded(addedDevices)
                if (audioHelper.audioOutputAvailable(AudioDeviceInfo.TYPE_BLUETOOTH_A2DP)) {
                    runOnUiThread {
                        Toast.makeText(applicationContext, "Fone Bluetooth Conectado!", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>?) {
                super.onAudioDevicesRemoved(removedDevices)
                if (!audioHelper.audioOutputAvailable(AudioDeviceInfo.TYPE_BLUETOOTH_A2DP)) {
                    runOnUiThread {
                        Toast.makeText(applicationContext, "Fone Desconectado.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }, null)

        // Ação do botão: Facilitando a conexão Bluetooth
        btnConfigBluetooth.setOnClickListener {
            abrirConfiguracoesBluetooth()
        }
    }

    private fun abrirConfiguracoesBluetooth() {
        val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            putExtra("EXTRA_CONNECTION_ONLY", true)
            putExtra("EXTRA_CLOSE_ON_CONNECT", true)
            putExtra("android.bluetooth.devicepicker.extra.FILTER_TYPE", 1)
        }
        startActivity(intent)
    }
}