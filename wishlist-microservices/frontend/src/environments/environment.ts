export const environment = {
  production: true,
  // En producción/Docker, el frontend se sirve vía Nginx y llama
  // directamente a cada microservicio expuesto en el host (docker-compose
  // publica 8081/8082/8083). Si se agrega un API Gateway más adelante,
  // basta con cambiar estas tres URLs por la del gateway.
  authApiUrl: 'http://localhost:8081/api/v1',
  productApiUrl: 'http://localhost:8082/api/v1',
  wishlistApiUrl: 'http://localhost:8083/api/v1',
};
