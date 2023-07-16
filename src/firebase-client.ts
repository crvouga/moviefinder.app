// Import the functions you need from the SDKs you need
import { initializeApp } from "firebase/app";
import { getAnalytics } from "firebase/analytics";
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
const analytics = getAnalytics(app);
