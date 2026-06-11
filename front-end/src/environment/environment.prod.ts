export const environment = {
  production: true,
  // Notice there is no domain name here! 
  // Because we are using Nginx on the server, we just use a relative path.
  // Nginx will automatically catch anything going to '/api' and route it to your Spring Boot container.
  apiUrl: '/api'
};