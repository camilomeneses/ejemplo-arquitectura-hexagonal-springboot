#!/bin/bash

echo "🚗 Probando el Sistema de Parqueadero"
echo "======================================"

# Verificar que la aplicación esté funcionando
echo "1. Verificando que la aplicación esté funcionando..."
curl -s http://localhost:8080/api/parqueadero/activos > /dev/null
if [ $? -eq 0 ]; then
    echo "✅ Aplicación funcionando correctamente"
else
    echo "❌ La aplicación no está respondiendo. Asegúrate de que esté ejecutándose."
    exit 1
fi

echo ""
echo "2. 🚗 Ingresando un carro con placa ABC123..."
curl -X POST http://localhost:8080/api/parqueadero/ingresar \
  -H "Content-Type: application/json" \
  -d '{"placa": "ABC123", "tipo": "CARRO"}' \
  | jq '.'

echo ""
echo "3. 🏍️ Ingresando una moto con placa XYZ789..."
curl -X POST http://localhost:8080/api/parqueadero/ingresar \
  -H "Content-Type: application/json" \
  -d '{"placa": "XYZ789", "tipo": "MOTO"}' \
  | jq '.'

echo ""
echo "4. 📋 Consultando vehículos activos en el parqueadero..."
curl -s http://localhost:8080/api/parqueadero/activos | jq '.'

echo ""
echo "5. 🚪 Sacando el carro ABC123 del parqueadero..."
curl -X PUT http://localhost:8080/api/parqueadero/sacar/ABC123 | jq '.'

echo ""
echo "6. 💰 Consultando el costo del carro ABC123..."
curl -s http://localhost:8080/api/parqueadero/costo/ABC123

echo ""
echo ""
echo "7. 📊 Consultando historial completo..."
curl -s http://localhost:8080/api/parqueadero/historial | jq '.'

echo ""
echo ""
echo "🎉 ¡Prueba completada! El sistema está funcionando correctamente."
echo "💡 Puedes acceder a la consola H2 en: http://localhost:8080/h2-console"
