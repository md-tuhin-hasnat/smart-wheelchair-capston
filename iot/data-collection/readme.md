# Smart Wheelchair Data Collection - ESP32

This folder contains the Arduino/ESP32 firmware (`arduio-code.ino`) used to collect synchronized data from an MPU6050 (accelerometer and gyroscope) and an HX711 (load cell/weight sensor), and transmit the data via Bluetooth Classic.

## Hardware Requirements

- **Microcontroller**: ESP32 (dual-core is utilized for stable performance)
- **Sensors**:
  - MPU6050 (6-axis accelerometer & gyroscope)
  - HX711 Load Cell Amplifier (with load cell)
- **Other Components**: 
  - Push button (for triggering data collection)

## Wiring & Device Setup

| Component | ESP32 Pin | Notes |
| :--- | :--- | :--- |
| **MPU6050 SDA** | Pin 21 | I2C Data |
| **MPU6050 SCL** | Pin 22 | I2C Clock |
| **HX711 DT (DOUT)** | Pin 18 | Data Out |
| **HX711 SCK** | Pin 19 | Serial Clock |
| **Push Button** | Pin 4 | Connect between Pin 4 and GND. Uses internal pull-up (`INPUT_PULLUP`), triggers on `LOW`. |

*Note: Ensure both sensors and the ESP32 share a common ground (GND).*

## Software Dependencies

You will need to install the following libraries in your Arduino IDE:
1. **Adafruit MPU6050** (`Adafruit_MPU6050.h`)
2. **HX711_ADC** (`HX711_ADC.h`) by Olav Kallhovd
3. **BluetoothSerial** (Included by default with the ESP32 board manager package)

## How It Works

1. **Initialization**: On startup, the system initializes I2C, connects to the sensors, calibrates the load cell (using a hardcoded calibration factor of `13.13`), and starts the Bluetooth serial service under the name **`SmartWheelchair_ESP32`**.
2. **Real-Time Operating System (RTOS)**: 
   - **Core 0** runs a dedicated sensor polling task (`sensorLoop`). It accurately reads all sensors every 25ms (40Hz sample rate) and buffers the data. Using `vTaskDelayUntil`, it ensures precise timing and avoids triggering the ESP32 Watchdog Timer.
   - **Core 1** runs the main `loop()` which monitors the start button and handles the heavier Bluetooth transmission task.
3. **Data Collection**:
   - Pressing the button on Pin 4 initiates a 3-second recording session.
   - The device collects exactly 120 samples (40Hz * 3s = 120).
4. **Data Transmission**:
   - Once the buffer is full (3 seconds of data), the device stops recording and streams the entire dataset over Bluetooth.
   - The data is sent as a JSON array where each element contains `ax`, `ay`, `az`, `gx`, `gy`, `gz`, and `w` (weight).
   - Short delays are inserted during transmission to feed the Bluetooth stack and prevent Watchdog resets.

## Usage Instructions

1. Flash the `arduio-code.ino` onto your ESP32.
2. Pair your mobile device or computer to the Bluetooth device named **`SmartWheelchair_ESP32`**.
3. Open a serial monitor (115200 baud) or a Bluetooth terminal app to view the status.
4. Press the button connected to Pin 4. The serial monitor will display "Starting in 1s...".
5. Wait 3 seconds for data collection.
6. The JSON array of sensor readings will be streamed over Bluetooth.
