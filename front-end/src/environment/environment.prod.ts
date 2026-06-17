export const environment = {
  production: true,
  // Notice there is no domain name here! 
  // Because we are using Nginx on the server, we just use a relative path.
  // Nginx will automatically catch anything going to '/api' and route it to your Spring Boot container.
  apiUrl: '/api', 
  frontEndUrl: 'http://localhost:4200',

  firebase: {
      apiKey: "AIzaSyDKlO-hdiLoq2l3Sfu-n4oKArP1Erv5TXY",
      authDomain: "crewup-a6aeb.firebaseapp.com",
      projectId: "crewup-a6aeb",
      storageBucket: "crewup-a6aeb.firebasestorage.app",
      messagingSenderId: "274788768842",
      appId: "1:274788768842:web:be062aa58aa3eb0cfbdab3",
      measurementId: "G-RFJBVP5H6H"
  }
};