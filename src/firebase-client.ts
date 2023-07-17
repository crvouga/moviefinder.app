// Import the functions you need from the SDKs you need
import { initializeApp } from "firebase/app";
import { connectFirestoreEmulator, getFirestore } from "firebase/firestore";
import { connectFunctionsEmulator, getFunctions } from "firebase/functions";
import { connectStorageEmulator, getStorage } from "firebase/storage";
// TODO: Add SDKs for Firebase products that you want to use
// https://firebase.google.com/docs/web/setup#available-libraries

// Your web app's Firebase configuration
// For Firebase JS SDK v7.20.0 and later, measurementId is optional
const firebaseConfig = {
  apiKey: "AIzaSyAEOowZFajG8loJ-Xc0syHEnzWxVNeRM3k",
  authDomain: "moviefinder-app.firebaseapp.com",
  databaseURL: "https://moviefinder-app-default-rtdb.firebaseio.com",
  projectId: "moviefinder-app",
  storageBucket: "moviefinder-app.appspot.com",
  messagingSenderId: "154729685426",
  appId: "1:154729685426:web:8d629b1fd63fff6c0084ab",
  measurementId: "G-584FQ1X5GT",
};

// Initialize Firebase
const app = initializeApp(firebaseConfig);
const firestore = getFirestore(app);
const functions = getFunctions(app);
const storage = getStorage(app);

// Connect to Firestore emulator
const firestoreEmulatorHost = "localhost";
const firestoreEmulatorPort = 8080;
connectFirestoreEmulator(
  firestore,
  firestoreEmulatorHost,
  firestoreEmulatorPort
);

// Connect to Cloud Functions emulator
const functionsEmulatorHost = "localhost";
const functionsEmulatorPort = 5001;
connectFunctionsEmulator(
  functions,
  functionsEmulatorHost,
  functionsEmulatorPort
);

const storageEmulatorHost = "localhost";
const storageEmulatorPort = 9199;
connectStorageEmulator(storage, storageEmulatorHost, storageEmulatorPort);

export { firestore, functions, storage };
