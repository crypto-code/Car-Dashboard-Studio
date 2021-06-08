# 🚗 Car-Dashboard-Android Studio

This repo is a Android Studio Project with Kotlin as Front-End and Python as Back-End using Chaquopy 🐼


## The DAA Dashboard

![](https://github.com/crypto-code/Car-Dashboard-Studio/blob/main/assets/1.png)

## Acknowledgment 

This project was completed in collaboration with the Center of Excellence Artificial Intelligence and Robotics

<p align="center">
<img src="https://github.com/crypto-code/Car-Dashboard-Studio/blob/main/assets/9.png" height="500">
</p>
  
## Dashboard Layout

![](https://github.com/crypto-code/Car-Dashboard-Studio/blob/main/assets/2.png)
---
![](https://github.com/crypto-code/Car-Dashboard-Studio/blob/main/assets/3.png)
---
![](https://github.com/crypto-code/Car-Dashboard-Studio/blob/main/assets/4.png)
---  
![](https://github.com/crypto-code/Car-Dashboard-Studio/blob/main/assets/5.png)


## User Flow Diagram

![](https://github.com/crypto-code/Car-Dashboard-Studio/blob/main/assets/6.png)

## Facial Recognition

  The Facial Recognition used in this app runs entirely using the **face_recognition** library in Python. The model used for facial recognition has a 99.38% accuracy on the Labelled Faces in the Wild dataset. Python is linked to Android Studio using the open source **Chaquopy plugin**. All facial recognition data is stored on a Firebase database.
  
  During the facial recognition stage, the front camera of the Android device is used to capture the image. Then the image is passed as a raw byte stream to the Python backend to extract the facial features. This is then compared against the feature encoding stored in the Firebase database to validate the face.

<p align="center">
<img src="https://github.com/crypto-code/Car-Dashboard-Studio/blob/main/assets/10.png" height="400">
</p>

---

![](https://github.com/crypto-code/Car-Dashboard-Studio/blob/main/assets/7.png)

## APIs Used

- ***Open Weather Map API***:
    This is used to get weather updates on the Dashboard. It calls for update every hour and also if the distance travelled exceeds 50km.
    <p align="center">
    <img src="https://github.com/crypto-code/Car-Dashboard-Studio/blob/main/assets/11.png" height="300">
    </p>
    
- ***Google Maps API***:
    This is used to display the map on the Dashboard. It is also used to calculate the speed of the vehicle.
    
- ***YouTube Music API***:
    This is used to make the Music Player on the Dashboard. It uses the **ytmusicapi** and **pafy** library in Python.
    <p align="center">
    <img src="https://github.com/crypto-code/Car-Dashboard-Studio/blob/main/assets/12.png" height="400">
    </p>

## Future Prospects

  The app could be connected to an Arduino on the vehicle and when logged in, the app sends a message to the Arduino via Bluetooth to switch on the relay and start the engine.
  <p align="center">
  <img src="https://github.com/crypto-code/Car-Dashboard-Studio/blob/main/assets/8.png" height="700">
  </p>
  

# G00D LUCK

For doubts email me at:
atinsaki@gmail.com

