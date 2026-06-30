# Usamos la imagen oficial de Nginx Alpine 
FROM nginx:alpine

# Eliminar la configuración por defecto
RUN rm /etc/nginx/conf.d/default.conf

# Copiar nuestra configuración personalizada
COPY nginx.conf /etc/nginx/nginx.conf

# Crear directorio para SSL si no existe
RUN mkdir -p /etc/nginx/ssl

# Copiar los certificados SSL
COPY ssl/server.crt /etc/nginx/ssl/server.crt
COPY ssl/server.key /etc/nginx/ssl/server.key

# Cambiar permisos de la clave privada (seguridad)
RUN chmod 600 /etc/nginx/ssl/server.key

# Crear directorio para logs
RUN mkdir -p /var/log/nginx

# Exponer puertos
EXPOSE 80 443

# Comando para iniciar Nginx en primer plano
CMD ["nginx", "-g", "daemon off;"]
