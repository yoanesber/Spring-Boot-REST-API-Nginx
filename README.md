# NGINX Setup and Configuration on WSL (Ubuntu)

## 📖 Overview  

This project is a simple Spring Boot REST API that simulates an **Order Payment** system. While the backend returns dummy data, the main purpose of this project is to demonstrate the use of **NGINX** as a **reverse proxy**, a **rate limiter**, and an **SSL terminator**.

The backend API is not exposed directly to the internet — all requests must go through **NGINX**, which acts as the gateway that handles HTTPS, origin validation, browser checks, CORS headers, and provides meaningful JSON error responses when access is denied or misconfigured.  

### 🚀 Features  

Below are the core features that make this solution robust and ready for real-world scenarios:  

- **Spring Boot REST API** – Returns dummy order payment data.  
- **HTTPS with Self-Signed Certificate** – All traffic is secured using SSL/TLS (HTTPS) via NGINX.  
- **NGINX Reverse Proxy** – Forwards incoming requests to the Spring Boot backend.  
- **Rate Limiting** – Limits requests per client IP to prevent abuse or denial of service.  
- **Custom Access Control**  
    - Rejects requests without `Origin` or from disallowed origins.  
    - Optionally restricts access by `Referer`.  
    - Rejects requests not coming from browsers (e.g., CLI tools or bots).  
- **Custom JSON Error Responses** – Includes custom handlers for HTTP errors such as `401 Unauthorized`, `403 Forbidden`, `429 Too Many Requests`, etc.  
- **CORS and Security Headers** – Centralized configuration using included snippets for better maintainability and browser compatibility.  
- **Proxy Header Forwarding** – Preserves original client IP and Host headers to the backend.  

---

## 🤖 Tech Stack  

The technology used in this project are:  

- `Spring Boot Starter Web` — Building RESTful APIs or web applications.  
- `Lombok` — Reducing boilerplate code.  
- `NGINX` — used as reverse proxy, SSL terminator, request filter, and rate limiter.  
- `WSL (Windows Subsystem for Linux)` — runtime environment for NGINX on Windows.  
---

## 🏗️ Project Structure  

The project is organized into the following package structure:  

```bash
rest-api-nginx-backend/
│── src/main/java/com/yoanesber/rest_api_nginx_backend/
│   ├── 📂controller/            # Defines REST API endpoints for handling order payment requests, acting as the entry point for client interactions.
│   ├── 📂dto/                   # Contains Data Transfer Objects used for API request and response models, such as creating an order payment.
│   ├── 📂entity/                # Includes CustomHttpResponse entity only as which represents the response message structures.
```
---


## 🛠️ Installation & Setup  

This section guides you through installing and configuring **NGINX** on **WSL (Ubuntu)** for proxying your Spring Boot REST API with added layers of security, rate limiting, and CORS support.  

### A. Install NGINX on WSL (Ubuntu)  

Learn how to install NGINX on Windows Subsystem for Linux (WSL) using Ubuntu, ensuring you're set up for local development and testing. This includes updating package list, installing the software, and verifying the installation.  

#### 1. Install NGINX on Windows Subsystem for Linux (WSL) using Ubuntu  

```bash
# 1. Update system packages
sudo apt update

# 2. Install NGINX
sudo apt install nginx -y

# 3. Start NGINX service
sudo service nginx start
```  

#### 2. Verify NGINX is running  

```bash
sudo service nginx status
```  

You should see:  
```bash
 nginx.service - A high performance web server and a reverse proxy server
     Loaded: loaded (/usr/lib/systemd/system/nginx.service; enabled; preset: enabled)
     Active: active (running) since Mon 2025-04-28 22:37:50 WIB; 21h ago
       Docs: man:nginx(8)
   Main PID: 313 (nginx)
      Tasks: 33 (limit: 14999)
     Memory: 24.2M ()
     CGroup: /system.slice/nginx.service
             ├─313 "nginx: master process /usr/sbin/nginx -g daemon on; master_process on;"
             ├─314 "nginx: worker process"
```

or, you can access NGINX from Windows browser  

```bash
# Find your WSL ip address from WSL bash/terminal:
ip addr show eth0 | grep 'inet ' | awk '{print $2}' | cut -d'/' -f1

# Access NGINX from Windows browser
curl -i http://<WSL_IP_ADDRESS>
```  

You should see:  
```bash
HTTP/1.1 200 OK
Server: nginx/1.24.0 (Ubuntu)
...
Welcome to nginx!
```  

### B. NGINX Basics: Common Commands  

Familiarize yourself with the most common NGINX commands used to manage the server — including how to start, stop, reload, and check NGINX status. These commands are crucial for day-to-day configuration and troubleshooting.  


| Command                       | Description                          |
|-------------------------------|--------------------------------------|
| `sudo nginx`                  | Start NGINX                          |
| `sudo nginx -s stop`          | Immediate shutdown                   |
| `sudo nginx -s quit`          | Graceful shutdown                    |
| `sudo nginx -s reload`        | Reload config without downtime       |
| `sudo nginx -s reopen`        | Reopen log files                     |
| `nginx -t`                    | Test configuration syntax            |
| `nginx -v`                    | Show NGINX version                   |
| `nginx -V`                    | Show version with compile options    |
| `sudo systemctl restart nginx`| Restart the NGINX service            |


### C. Define Custom Map Variables  

This section explains how to create reusable, dynamic variables using the `map` directive in NGINX. These are helpful for conditional logic such as CORS handling, referer checks, or user-agent filtering.  

#### 1. Understanding Configuration Directories  

NGINX typically uses `/etc/nginx/` as its main configuration directory. Inside:  

| Directory/File         | Description                                                                 |
|------------------------|-----------------------------------------------------------------------------|
| `nginx.conf`           | The main NGINX configuration file                                           |
| `conf.d/`              | Common directory for virtual host or additional include files               |
| `snippets/`            | Ideal place for reusable configuration fragments (can be created manually)  |
| `sites-available/`     | Stores all available virtual host configuration files (active or inactive)  |
| `sites-enabled/`       | Contains symbolic links to active configs loaded by NGINX                   |


#### 2. Backup Default Configuration  

Before modifying any configs:  

```bash
cd /etc/nginx
sudo cp nginx.conf nginx.conf_$(date +%Y%m%d).bak
```  

This ensures you can restore the original if needed.  

#### 3. Create Custom Map Configs  

NGINX `map` blocks allow you to create reusable variables based on conditions. These are often defined in separate files (e.g., in `/etc/nginx/custom-maps/`) for clarity.  

```bash
sudo mkdir -p /etc/nginx/custom-maps
```

Inside, define maps like:  

##### 1) Allowed Origins  
Create a map to define which origins are allowed for CORS handling:  

```bash
sudo nano /etc/nginx/custom-maps/allowed-origins.conf
```
Example inside `allowed-origins.conf`:  

```bash
map $http_origin $is_origin_allowed {
    default 0;
    "https://yourdomain.com" 1;
    "https://admin.yourdomain.com" 1;
}
```
##### 2) Allowed Referers  
Define trusted referers to restrict access to certain resources like images or APIs:  

```bash
sudo nano /etc/nginx/custom-maps/allowed-referrer.conf
```
Example inside `allowed-referrer.conf`:  

```bash
map $http_referer $is_referer_allowed {
    default 0;
    ~^https://trusted\.com(/.*)?$ 1;
}
```

##### 3) User-Agent Detection  
Create a map to detect bots or specific clients based on `User-Agent`:  

```bash
sudo nano /etc/nginx/custom-maps/user-agent-detect.conf
```

Example inside `user-agent-detect.conf`:  

```bash
map $http_user_agent $is_browser {
    default 0;
    ~*mozilla 1;
    ~*chrome 1;
    ~*safari 1;
    ~*firefox 1;
    ~*edge 1;
}
```

##### 4) Include Custom Maps in `nginx.conf`  
Once you’ve created your custom maps (e.g., in `/etc/nginx/custom-maps/*.conf`), include them before the http {} block ends:  

```bash
sudo nano /etc/nginx/nginx.conf
```  

include the map file:  

```bash
http {
    include /etc/nginx/custom-maps/*.conf;

    ...
}
```

This makes your maps available to all server blocks for reuse.  


### D. Create Reusable Header Snippets  

This step is about modularizing and reusing HTTP header configurations in NGINX using snippet files. It promotes cleaner, more maintainable, and DRY (Don't Repeat Yourself) configurations.  

#### 1. Create the `snippets/` Directory (if not exists)  
Create a directory (usually inside `/etc/nginx/`) called `snippets/`. This folder will hold modular NGINX configuration files that can be included in other config files.  

```bash
sudo mkdir -p /etc/nginx/snippets
```

#### 2. Create a Security Headers Snippet  
Define common security-related HTTP headers (e.g., `X-Content-Type-Options`, `X-Frame-Options`, etc.) in one file. You can include this snippet in any server block without repeating the same headers.  

```bash
sudo nano /etc/nginx/snippets/security-headers.conf
```

Example inside `security-headers.conf`:  

```bash
# Prevent clickjacking attacks by disallowing the site to be embedded in a frame or iframe
add_header X-Frame-Options "SAMEORIGIN" always;

# Enforce secure connections by instructing the browser to use HTTPS for future requests (useful in HTTPS environments)
add_header Strict-Transport-Security "max-age=63072000; includeSubDomains; preload" always;

# Ensure that cookies are only accessible via HTTP (not JavaScript) and are sent securely (over HTTPS)
add_header Set-Cookie "HttpOnly; Secure" always;

# Prevent browsers from interpreting files as a different MIME type (useful to avoid XSS attacks)
add_header X-Content-Type-Options "nosniff" always;

# Enable basic protection against cross-site scripting (XSS) attacks
add_header X-XSS-Protection "1; mode=block" always;

# Restrictive Content Security Policy (CSP): only allow resources from the same origin ('self')
add_header Content-Security-Policy "default-src 'self'; script-src 'self'; style-src 'self'; object-src 'none'; base-uri 'self'; connect-src 'self'; font-src 'self'; frame-src 'self'; img-src 'self' data:; manifest-src 'self'; media-src 'self'; worker-src 'none'; frame-ancestors 'self';";
```

#### 3. Create a CORS Headers Snippet  
Define headers needed for Cross-Origin Resource Sharing (CORS). These headers are often reused across multiple APIs or frontend integrations.  

```bash
sudo nano /etc/nginx/snippets/cors-headers.conf
```

Example inside `cors-headers.conf`:  

```bash
# Dynamically allow the origin based on $cors_origin (mapped earlier)
add_header Access-Control-Allow-Origin $http_origin always;

# Allow cookies and credentials (like Authorization header or session cookies)
add_header Access-Control-Allow-Credentials "true" always;

# Inform browser which HTTP methods are allowed from this origin
add_header Access-Control-Allow-Methods "GET, POST, PUT, DELETE, OPTIONS" always;

# Inform browser which headers can be used in actual request
add_header Access-Control-Allow-Headers "Authorization, Content-Type" always;

# Control when the Referer header is included in requests
add_header Referrer-Policy "strict-origin-when-cross-origin" always;

# Cache the CORS response for 1 day (86400 seconds)
add_header Access-Control-Max-Age "86400" always;
```  

#### 4. Include Snippets in Server Blocks  
Use the `include` directive to reuse the snippet file in a server block or location. This makes the server block cleaner and consistent.  

Inside `server` block:  

```bash
include snippets/security-headers.conf;
include snippets/cors-headers.conf;
```

### E. Configure Rate Limiting  

Used to control incoming request rate to prevent abuse or overload (e.g. DDoS mitigation) using directives like `limit_req_zone` and `limit_req`.  

#### 1. Define the Rate Limiting Zone  

Add this inside your `http` block in `nginx.conf`:  

    ```bash
    limit_req_zone $binary_remote_addr zone=api_limit:10m rate=1r/s;
    ```  

    Explanation:  
    - zone=api_limit:10m: Creates a shared memory zone of 10MB  
    - rate=1r/s: Allows 1 request per second per IP  

#### 2. Apply Limits in Specific Server Block or Location  
Inside a specific server or location block:  

    ```bash
    limit_req zone=api_limit;
    limit_req_status 429;
    ```

### F. Create a Secure API Server Block  

This section guides you through setting up an `NGINX server block` (virtual host) specifically optimized for `securing an API endpoint`. This includes `SSL (HTTPS)`, `rate limiting`, `reusable headers`, and `access control`.  

#### 1. Create a New Server Block File  
Create a new file inside `/etc/nginx/sites-available/`, such as `api-example`. This is where the API-specific configurations live.  

Example:  
```bash
sudo nano /etc/nginx/sites-available/api-example
```  

#### 2. Configure the Server Block  
Define the basic server block structure, using port 443 for HTTPS.  

Example:  
```bash
server {
    listen 443 ssl http2;
    server_name api.example.com;

    ssl_certificate     /etc/ssl/certs/api-example.crt;
    ssl_certificate_key /etc/ssl/private/api-example.key;

    include snippets/security-headers.conf;
    include snippets/cors-headers.conf;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;

        limit_req zone=api_limit burst=5 nodelay;
    }
}
```  

#### 3. Configure SSL  
Enable SSL (HTTPS) by specifying the certificate and private key. **SSL/TLS** encrypts communication between the client and server, ensuring data privacy and integrity—essential for securing API traffic. Store your certificate files in the custom directory `/etc/nginx/ssl/` for better organization and permission control.  

Example:  
```bash
ssl_certificate     /etc/nginx/ssl/api-example.crt;
ssl_certificate_key /etc/nginx/ssl/api-example.key;
```  

#### 4. Apply Security & CORS Snippets  
Reuse your predefined `security-headers.conf` and `cors-headers.conf`. This simplifies adding HTTP security headers and CORS rules.  

Example:  
```bash
include snippets/security-headers.conf;
include snippets/cors-headers.conf;
```

#### 5. Add Rate Limiting (Optional but Recommended)  
Control the number of requests from a client using the `limit_req` directive. Protects your API from abuse and brute-force attacks.  

Example (in `nginx.conf` http block):  
```bash
limit_req_zone $binary_remote_addr zone=api_limit:10m rate=1r/s;
```

In server block:  
```bash
limit_req zone=api_limit;
limit_req_status 429;
```  

#### 6. Customize Error Response Globally or Per Server  
Custom error pages can be configured in NGINX to provide more informative or user-friendly error messages. This can be done either **globally** for the entire server or **per server block** to handle specific endpoints. Here, you will define custom error pages for specific HTTP status codes, and the response will be formatted as JSON, which is often used in API responses.  

Steps to **Customize Error Responses**:  
- **Define Custom Error Pages**: The `error_page` directive is used to define a custom page for specific HTTP error codes. This can either redirect to a custom URL or invoke a named location block to handle the error.  
- **JSON Response**: In the location block, you can specify the error response in **JSON format**. This is useful for APIs, as clients can handle the response easily in a structured format.

Example of Custom Error Pages:  
Here are examples of how to handle various HTTP error responses:  
```bash
# --- Custom Error Pages ---

# Customize 301 error response globally or per server
error_page 301 = @json_301;
location @json_301 {
    default_type application/json;
    return 301 '{
        "timestamp": "$time_iso8601",
        "status": 301,
        "error": "Moved Permanently",
        "path": "$request_uri"
    }';
}

# Customize 401 error response globally or per server
error_page 401 = @json_401;
location @json_401 {
    default_type application/json;
    return 401 '{
        "timestamp": "$time_iso8601",
        "status": 401,
        "error": "Missing Authorization token",
        "path": "$request_uri"
    }';
}

# Customize 403 error response globally or per server
error_page 403 = @json_403;
location @json_403 {
    default_type application/json;
    return 403 '{
        "timestamp": "$time_iso8601",
        "status": 403,
        "error": "Access Denied",
        "path": "$request_uri"
    }';
}

# Customize 405 error response globally or per server
error_page 405 = @json_405;
location @json_405 {
    default_type application/json;
    return 405 '{
        "timestamp": "$time_iso8601",
        "status": 405,
        "error": "Method Not Allowed",
        "path": "$request_uri"
    }';
}

# Customize 413 error response globally or per server
error_page 413 = @json_413;
location @json_413 {
    default_type application/json;
    return 413 '{
        "timestamp": "$time_iso8601",
        "status": 413,
        "error": "Request Entity Too Large",
        "path": "$request_uri"
    }';
}

# Customize 429 error response globally or per server
error_page 429 = @json_429;
location @json_429 {
    default_type application/json;
    return 429 '{
        "timestamp": "$time_iso8601",
        "status": 429,
        "error": "Too many requests, please slow down",
        "path": "$request_uri"
    }';
}
```

#### 7. Security Validation  
We can perform basic security validation for incoming requests by inspecting certain headers, like `Origin`, `Referer`, and `User-Agent`. This ensures that only requests from valid sources and browsers are allowed. If any of the checks fail, you can deny access by returning an HTTP 403 status.  

Example Origin & Browser Validation:  
```bash
# --- Origin & Browser Validation ---

# If the 'Origin' header is missing from the request, deny access
if ($http_origin = "") {
    return 403;
}

# If the 'Origin' is not in the allowed list (determined via $is_origin_allowed map logic), deny access
if ($is_origin_allowed = 0) {
    return 403;
}

# If the 'Referer' header is missing, deny access
# if ($http_referer = "") {
#     return 403;
# }

# If the 'Referer' is not in the allowed list (determined via $is_referer_allowed map logic), deny access
# Validating Referer is especially useful when the request involves URLs or links between web pages. It's typically used in the context of web browsers to ensure that a request originated from a legitimate and trusted source (such as a specific page on your website)
# if ($is_referer_allowed = 0) {
#     return 403;
# }

# If the request is not from a browser (determined via $is_browser map logic), deny access
if ($is_browser = 0) {
    return 403;
}

# Handle CORS Preflight Request (sent automatically by browsers before actual request)
if ($request_method = 'OPTIONS') {
    # Respond with 204 No Content to tell the browser everything's OK (no body content needed)
    return 204;
}

# Check if Authorization token is missing
# If the Authorization header is empty, return a 401 Unauthorized error with a JSON response
if ($http_authorization = "") {
    return 401;
}
```

#### 8. Enable the Site  
Create a symbolic link from `sites-available` to `sites-enabled`.  

Command:  
```bash
sudo ln -s /etc/nginx/sites-available/api-example /etc/nginx/sites-enabled/
```  

#### 9. Test Configuration and Reload  
Before applying any changes to NGINX, always **test your configuration** to avoid syntax errors or invalid directives. After a successful test, reload NGINX to apply changes **without downtime**.  

Command:  
```bash
sudo nginx -t && sudo nginx -s reload
```

You should see:  
```bash
nginx: the configuration file /etc/nginx/nginx.conf syntax is ok
nginx: configuration file /etc/nginx/nginx.conf test is successful
2025/04/29 22:33:29 [notice] 88808#88808: signal process started
```  

Best Practice: Always run `nginx -t` before applying `reload`, especially when editing files like:  
- `/etc/nginx/nginx.conf`  
- `/etc/nginx/conf.d/*.conf`  
- `/etc/nginx/sites-available/*`  



### G. Spring Boot Configuration  

1. Clone the Project  

Ensure `Git` is installed, then clone the project repository:  

```bash
git clone https://github.com/yoanesber/Spring-Boot-REST-API-Nginx.git
cd Spring-Boot-REST-API-Nginx
```


2. Run the Spring Boot Application  

Use Maven to start the application:  

```bash
mvn spring-boot:run
```

Once started, the server should be accessible at:  

```bash
http://localhost:8081/ 
```

You can test the API using: Postman (Desktop/Web version) or cURL

---


## 🧪 Testing Scenarios  

This section outlines test scenarios for verifying that your NGINX configuration works as expected. These include reverse proxy setup, validation mechanisms, rate limiting, and custom error responses. All test cases below are designed to be executed using **Postman**, a powerful API testing tool.  

**📝 Note**: 
- Ensure Postman is installed on your system. You can download it from [Download Postman](https://www.postman.com/downloads/).  
- I have mapped my WSL IP to `host.wsl.internal` in my Windows `hosts` file (`C:\Windows\System32\drivers\etc\hosts`), like:  
```bash
111.22.333.444 host.wsl.internal
```  
**This allows testing from Windows-based tools (like Postman) to reach NGINX running inside WSL using `https://host.wsl.internal`.**

### 1. Reverse Proxy Test  
**Goal**: Confirm NGINX is forwarding requests to the backend server properly.  

**Request**:
Send a `GET` request to `https://host.wsl.internal:443/api/v1/get-order-payment`.  

**Result**:  
Postman request tab with URL, headers, and successful JSON response preview.  

![Image](https://github.com/user-attachments/assets/f2f2ca31-1c4c-4ae2-b803-1ef05ec7af5c)  


### 2. Rate Limiting Test  
**Goal**: Ensure excessive requests are throttled as defined in `limit_req_zone`.  

**Request**:  
Send more than 1 request per second repeatedly to the endpoint (e.g., use Postman's "**Runner**" to send 5 requests with 200ms delay).  

**Result**:  
A `429` response in Postman showing the returned JSON, and Runner settings.  

![Image](https://github.com/user-attachments/assets/a76f5345-d793-438b-a4da-587324c73cb3)  

### 3. Origin Header Validation  
**Goal**: Deny access when the `Origin` header is missing or unapproved.  

**Request A**:  
Send a request without an `Origin` header.  

**Request B**:  
Send a request with an unlisted origin (e.g., Origin: `http://unlisted.origin.com`).  

**Result**:  
Postman request with and without Origin, and the `403` response.  
**Request A**:  
![Image](https://github.com/user-attachments/assets/17a33b66-2d62-4875-a181-cfef0a0178bf)  

**Request B**:  
![Image](https://github.com/user-attachments/assets/d5c079fe-463f-4e1d-9dfc-340bec2ff7b2)  

### 4. User-Agent Detection  
**Goal**: Block requests not coming from valid browsers (defined via `is_browser` map).  

**Request**:  
Use Postman to send a request with a non-browser `User-Agent` like `curl/7.81.0`.  

**Result**:  
Postman headers with fake `User-Agent` and the `403` result.  
![Image](https://github.com/user-attachments/assets/5a630984-6e7b-447f-813a-ed45a0d2fee2)  

### 5. CORS Preflight Request (OPTIONS Method)  
**Goal**: Verify CORS preflight (OPTIONS) requests return HTTP 204 without error.  

**Request**:  
Send a request with **Method**: `OPTIONS`

**Result**:  
Postman request and response headers showing CORS validation success with response code `204 No Content`.  
![Image](https://github.com/user-attachments/assets/a8a490f2-fbe8-4e76-8025-7e0e84fe932b)  

### 6. Custom Error Pages (JSON Format)  
**Goal**: Ensure JSON error responses are returned for specific HTTP errors.  

**Trigger Scenarios**:

Access without Authorization header → HTTP `401`  

Not found resource → HTTP `404`  

Request non-existent method → HTTP `405`  

**Result**:  
**`401` - Missing Authorization token**:  
![Image](https://github.com/user-attachments/assets/96f07652-854f-4bde-a405-8611b5a70c35)  

**`404` - Resource Not Found**:  
![Image](https://github.com/user-attachments/assets/3572aa67-e425-484e-adfb-17aeed7f25b0)  

**`405` - Method Not Allowed**:  
![Image](https://github.com/user-attachments/assets/fd5a8080-13e8-4d67-9b57-8b97122e1f35)  

---


## 🔗 Related Repositories  
- JWT Authentication Integration of Kong API Gateway, check out [Spring Boot Department API with Kong JWT Authentication (DB-Backed Mode)](https://github.com/yoanesber/Spring-Boot-JWT-Auth-Kong).  
- Rate Limiting Integration of Kong API Gateway, check out [Spring Boot Department API with Kong API Gateway & Rate Limiting (DB-Backed Mode)](https://github.com/yoanesber/Spring-Boot-Rate-Limit-Kong).  