FROM python:3.11-slim
WORKDIR /app

COPY ./requiremements.txt /app

# إنشاء virtualenv داخل Docker
RUN python -m venv venv
RUN ./venv/bin/pip install --no-cache-dir -r ./requirements.txt

COPY . .

EXPOSE 5000

CMD ["./venv/bin/python", "app.py"]
