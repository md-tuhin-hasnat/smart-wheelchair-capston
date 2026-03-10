#include <Adafruit_MPU6050.h>
#include <BluetoothSerial.h>
#include <HX711_ADC.h>
#include <Wire.h>

// --- Pins ---
const int BUTTON_PIN = 4;
const int HX711_dout = 18;
const int HX711_sck = 19;

// --- Settings ---
const int SAMPLE_RATE_HZ = 40;
const int RECORD_SECONDS = 3;
const int TOTAL_SAMPLES = SAMPLE_RATE_HZ * RECORD_SECONDS;
const int INTERVAL_MS = 1000 / SAMPLE_RATE_HZ; // 25ms

// --- Data Structure ---
struct DataPoint {
  float ax, ay, az, gx, gy, gz, weight;
};

// --- Global Variables ---
DataPoint dataBuffer[TOTAL_SAMPLES];
volatile bool isRecording = false;
volatile bool dataReadyToSend = false;
int sampleCount = 0;

BluetoothSerial SerialBT;
Adafruit_MPU6050 mpu;
HX711_ADC LoadCell(HX711_dout, HX711_sck);

TaskHandle_t SensorTask;

// --- CORE 0: SENSOR POLLING (WDT Friendly) ---
void sensorLoop(void *pvParameters) {
  TickType_t xLastWakeTime = xTaskGetTickCount();
  const TickType_t xFrequency = pdMS_TO_TICKS(INTERVAL_MS);

  for (;;) {
    // This function tells the OS to sleep this task until exactly 25ms has
    // passed This prevents the Watchdog reset!
    vTaskDelayUntil(&xLastWakeTime, xFrequency);

    LoadCell.update();

    if (isRecording && sampleCount < TOTAL_SAMPLES) {
      sensors_event_t a, g, temp;
      mpu.getEvent(&a, &g, &temp);

      dataBuffer[sampleCount] = {
          a.acceleration.x, a.acceleration.y, a.acceleration.z,  g.gyro.x,
          g.gyro.y,         g.gyro.z,         LoadCell.getData()};

      sampleCount++;

      if (sampleCount >= TOTAL_SAMPLES) {
        isRecording = false;
        dataReadyToSend = true;
      }
    }
  }
}

void setup() {
  Serial.begin(115200);
  pinMode(BUTTON_PIN, INPUT_PULLUP);

  Wire.begin(22, 21);
  if (!mpu.begin()) {
    Serial.println("MPU Fail");
    while (1)
      ;
  }

  LoadCell.begin();
  LoadCell.start(2000, true);
  LoadCell.setCalFactor(13.13);

  SerialBT.begin("SmartWheelchair_ESP32");
  Serial.println("System Ready. Press D4.");

  // Increased stack size to 8192 to prevent overflows
  xTaskCreatePinnedToCore(sensorLoop, "SensorTask", 8192, NULL, 1, &SensorTask,
                          0);
}

void loop() {
  if (digitalRead(BUTTON_PIN) == LOW && !isRecording && !dataReadyToSend) {
    Serial.println("Starting in 1s...");
    delay(1000);
    sampleCount = 0;
    isRecording = true;
  }

  if (dataReadyToSend) {
    Serial.println("Streaming to Bluetooth...");

    // Start JSON
    SerialBT.print("[");
    for (int i = 0; i < TOTAL_SAMPLES; i++) {
      // Direct printing to SerialBT without any libraries
      SerialBT.printf("{\"ax\":%.2f,\"ay\":%.2f,\"az\":%.2f,\"gx\":%.2f,\"gy\":"
                      "%.2f,\"gz\":%.2f,\"w\":%.2f}",
                      dataBuffer[i].ax, dataBuffer[i].ay, dataBuffer[i].az,
                      dataBuffer[i].gx, dataBuffer[i].gy, dataBuffer[i].gz,
                      dataBuffer[i].weight);

      if (i < TOTAL_SAMPLES - 1)
        SerialBT.print(",");

      // Crucial: Feed the Bluetooth stack and Watchdog during long transmission
      if (i % 20 == 0)
        delay(10);
    }
    SerialBT.println("]");

    Serial.println("Finished.");
    dataReadyToSend = false;
  }
}