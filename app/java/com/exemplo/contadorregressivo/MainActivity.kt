import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.speech.tts.TextToSpeech
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var tvTimer: TextView
    private lateinit var btnStart: Button
    private lateinit var btnReset: Button
    
    private var countDownTimer: CountDownTimer? = null
    private var timeLeftInMillis: Long = 15 * 60 * 1000 // 15 minutos em milissegundos
    private var timerRunning = false
    
    private lateinit var tts: TextToSpeech
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var alarmPlayer: MediaPlayer
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Inicializar views
        tvTimer = findViewById(R.id.tvTimer)
        btnStart = findViewById(R.id.btnStart)
        btnReset = findViewById(R.id.btnReset)
        
        // Inicializar TextToSpeech
        tts = TextToSpeech(this, this)
        
        // Inicializar MediaPlayer para os bips
        mediaPlayer = MediaPlayer.create(this, R.raw.beep)
        
        // Inicializar MediaPlayer para a sirene
        alarmPlayer = MediaPlayer.create(this, R.raw.alarm)
        
        // Configurar botões
        btnStart.setOnClickListener {
            if (timerRunning) {
                pauseTimer()
            } else {
                startTimer()
            }
        }
        
        btnReset.setOnClickListener {
            resetTimer()
        }
        
        updateCountDownText()
    }
    
    private fun startTimer() {
        countDownTimer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateCountDownText()
                
                // Anúncios em momentos específicos
                when (millisUntilFinished / 1000) {
                    (15 * 60).toLong() -> speak("O jogo começou")
                    (10 * 60).toLong() -> speak("Faltam 10 minutos")
                    (5 * 60).toLong() -> speak("Faltam 5 minutos")
                    10L -> playBeep()
                    9L -> playBeep()
                    8L -> playBeep()
                }
            }
            
            override fun onFinish() {
                timerRunning = false
                btnStart.text = "Iniciar"
                playAlarm()
                speak("Fim do jogo")
                timeLeftInMillis = 15 * 60 * 1000
                updateCountDownText()
            }
        }.start()
        
        // Anúncio inicial e bips de contagem regressiva
        speak("Iniciando contagem regressiva")
        object : CountDownTimer(4000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                if (millisUntilFinished < 4000) {
                    playBeep()
                }
            }
            
            override fun onFinish() {
                speak("O jogo começou")
            }
        }.start()
        
        timerRunning = true
        btnStart.text = "Pausar"
    }
    
    private fun pauseTimer() {
        countDownTimer?.cancel()
        timerRunning = false
        btnStart.text = "Iniciar"
    }
    
    private fun resetTimer() {
        countDownTimer?.cancel()
        timeLeftInMillis = 15 * 60 * 1000
        updateCountDownText()
        btnStart.text = "Iniciar"
        timerRunning = false
    }
    
    private fun updateCountDownText() {
        val minutes = (timeLeftInMillis / 1000) / 60
        val seconds = (timeLeftInMillis / 1000) % 60
        
        val timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        tvTimer.text = timeLeftFormatted
    }
    
    private fun speak(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }
    
    private fun playBeep() {
        mediaPlayer.seekTo(0)
        mediaPlayer.start()
    }
    
    private fun playAlarm() {
        alarmPlayer.seekTo(0)
        alarmPlayer.start()
    }
    
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts.setLanguage(Locale("pt", "BR"))
            
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Linguagem não suportada
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        tts.stop()
        tts.shutdown()
        mediaPlayer.release()
        alarmPlayer.release()
    }
}