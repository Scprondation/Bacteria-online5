# Деплой Bacteria Online 5 на Ubuntu

Сервер: `0.0.0.0`  
Игра/API: `http://bakteria5.ru` и TCP `bakteria5.ru:5055`  
Оплата: `http://pay.bakteria5.ru`

## 1. DNS у регистратора

Создай A-записи:

```text
bakteria5.ru      A      0.0.0.0
pay.bakteria5.ru  A      0.0.0.0
```

Пока DNS расходится, можно подключаться напрямую по IP. Обычно обновление занимает от нескольких минут до суток.

## 2. Подготовить Ubuntu

Зайти на сервер:

```bash
ssh root@0.0.0.0
```

Поставить Java, Nginx и firewall:

```bash
apt update
apt install -y openjdk-17-jdk nginx ufw
ufw allow OpenSSH
ufw allow 80/tcp
ufw allow 5055/tcp
ufw allow 5056/tcp
ufw --force enable
mkdir -p /opt/bacteria-online5
```

## 3. Загрузить проект с компьютера

Из папки проекта на Windows:

```powershell
scp -r "D:\программирование\бактерии онлайн 5\*" root@0.0.0.0:/opt/bacteria-online5/
```

Если `scp` ругается на кириллицу/пробелы, проще заархивировать:

```powershell
Compress-Archive -Path "D:\программирование\бактерии онлайн 5\*" -DestinationPath "$env:TEMP\bacteria-online5.zip" -Force
scp "$env:TEMP\bacteria-online5.zip" root@0.0.0.0:/opt/
```

На сервере:

```bash
apt install -y unzip
rm -rf /opt/bacteria-online5
mkdir -p /opt/bacteria-online5
unzip -o /opt/bacteria-online5.zip -d /opt/bacteria-online5
cd /opt/bacteria-online5
chmod +x ./gradlew
```

## 4. Собрать сервер

```bash
cd /opt/bacteria-online5
./gradlew :server:installDist
```

## 5. Настроить ЮKassa

Секреты ЮKassa храним только на сервере:

```bash
nano /etc/bacteria-online5.env
```

Содержимое:

```env
YOOKASSA_SHOP_ID=твой_shop_id
YOOKASSA_SECRET_KEY=твой_secret_key
PUBLIC_HTTP_URL=http://pay.bakteria5.ru
```

Права:

```bash
chmod 600 /etc/bacteria-online5.env
```

## 6. Systemd-сервис

Создать сервис:

```bash
nano /etc/systemd/system/bacteria-online5.service
```

Содержимое:

```ini
[Unit]
Description=Bacteria Online 5 server
After=network.target

[Service]
WorkingDirectory=/opt/bacteria-online5
EnvironmentFile=/etc/bacteria-online5.env
ExecStart=/opt/bacteria-online5/server/build/install/server/bin/server
Restart=always
RestartSec=5

[Install]
WantedBy=multi-user.target
```

Запустить:

```bash
systemctl daemon-reload
systemctl enable --now bacteria-online5
systemctl status bacteria-online5
```

Логи:

```bash
journalctl -u bacteria-online5 -f
```

## 7. Nginx для доменов

Java-сервер слушает `5056`, а Nginx отдаёт `bakteria5.ru` и `pay.bakteria5.ru` через обычный порт `80`.

```bash
nano /etc/nginx/sites-available/bacteria-online5
```

Содержимое:

```nginx
server {
    listen 80;
    server_name bakteria5.ru;

    location / {
        proxy_pass http://127.0.0.1:5056;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}

server {
    listen 80;
    server_name pay.bakteria5.ru;

    location / {
        proxy_pass http://127.0.0.1:5056;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

Включить:

```bash
ln -sf /etc/nginx/sites-available/bacteria-online5 /etc/nginx/sites-enabled/bacteria-online5
nginx -t
systemctl reload nginx
```

## 8. Проверка

```bash
curl http://bakteria5.ru/update.json
curl http://pay.bakteria5.ru/update.json
curl "http://pay.bakteria5.ru/donate?accountId=TEST&pack=0"
```

Последний запрос без реального аккаунта должен показать `Account not found`.

Проверка игрового TCP-порта:

```bash
nc -vz bakteria5.ru 5055
```

## 9. Обновление после изменений

На компьютере снова загрузить файлы, затем на сервере:

```bash
cd /opt/bacteria-online5
./gradlew :server:installDist
systemctl restart bacteria-online5
```

## 10. HTTPS позже

Когда DNS точно работает, можно подключить бесплатный SSL:

```bash
apt install -y certbot python3-certbot-nginx
certbot --nginx -d bakteria5.ru -d pay.bakteria5.ru
```

После HTTPS поменяй в коде/переменных:

```env
PUBLIC_HTTP_URL=https://pay.bakteria5.ru
```

