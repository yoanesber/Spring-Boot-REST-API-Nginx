FROM nginx:latest

# Copy configuration files
COPY nginx.conf /etc/nginx/nginx.conf

# Copy site configuration files
COPY sites-available/spring-api /etc/nginx/conf.d/default.conf

# Copy additional directories
COPY ssl/ /etc/nginx/ssl/
COPY snippets/ /etc/nginx/snippets/
COPY custom-maps/ /etc/nginx/custom-maps/