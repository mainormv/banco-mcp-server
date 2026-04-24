package com.banco.mcp.service;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class BankingToolsService {

    // ─────────────────────────────────────────────
    // DATOS MOCK
    // ─────────────────────────────────────────────

    private static final Map<String, Map<String, Object>> CLIENTES = new HashMap<>();
    private static final Map<String, List<Map<String, Object>>> CUENTAS = new HashMap<>();
    private static final Map<String, List<Map<String, Object>>> SERVICIOS = new HashMap<>();

    static {
        // Clientes
        CLIENTES.put("12345678", Map.of(
                "cedula", "12345678",
                "nombre", "Juan Carlos Pérez",
                "tipo", "Premium",
                "activo", true,
                "fecha_afiliacion", "2015-03-10"
        ));
        CLIENTES.put("87654321", Map.of(
                "cedula", "87654321",
                "nombre", "María Fernanda García",
                "tipo", "Estándar",
                "activo", true,
                "fecha_afiliacion", "2019-07-22"
        ));
        CLIENTES.put("11111111", Map.of(
                "cedula", "11111111",
                "nombre", "Carlos López",
                "tipo", "Básico",
                "activo", false,
                "fecha_afiliacion", "2021-01-15"
        ));

        // Cuentas
        CUENTAS.put("12345678", List.of(
                new HashMap<>(Map.of(
                        "numero_cuenta", "001-123456-01",
                        "tipo", "Cuenta Corriente",
                        "saldo", 5000.00,
                        "moneda", "USD",
                        "estado", "Activa"
                )),
                new HashMap<>(Map.of(
                        "numero_cuenta", "001-123456-02",
                        "tipo", "Cuenta de Ahorros",
                        "saldo", 12500.50,
                        "moneda", "USD",
                        "estado", "Activa"
                ))
        ));
        CUENTAS.put("87654321", List.of(
                new HashMap<>(Map.of(
                        "numero_cuenta", "001-876543-01",
                        "tipo", "Cuenta de Ahorros",
                        "saldo", 3200.75,
                        "moneda", "USD",
                        "estado", "Activa"
                ))
        ));

        // Servicios públicos
        SERVICIOS.put("12345678", new ArrayList<>(List.of(
                new HashMap<>(Map.of(
                        "codigo", "LUZ-001",
                        "nombre", "Electricidad - CNFL",
                        "categoria", "Electricidad",
                        "deuda", 45000.00,
                        "moneda", "CRC",
                        "vencimiento", "2026-05-10"
                )),
                new HashMap<>(Map.of(
                        "codigo", "AGUA-001",
                        "nombre", "Agua Potable - AyA",
                        "categoria", "Agua",
                        "deuda", 12500.00,
                        "moneda", "CRC",
                        "vencimiento", "2026-05-15"
                )),
                new HashMap<>(Map.of(
                        "codigo", "TEL-001",
                        "nombre", "Teléfono - ICE",
                        "categoria", "Telecomunicaciones",
                        "deuda", 8900.00,
                        "moneda", "CRC",
                        "vencimiento", "2026-05-20"
                ))
        )));
        SERVICIOS.put("87654321", new ArrayList<>(List.of(
                new HashMap<>(Map.of(
                        "codigo", "LUZ-002",
                        "nombre", "Electricidad - ESPH",
                        "categoria", "Electricidad",
                        "deuda", 32000.00,
                        "moneda", "CRC",
                        "vencimiento", "2026-05-12"
                ))
        )));
    }

    // ─────────────────────────────────────────────
    // HERRAMIENTA 1: Verificar cliente
    // ─────────────────────────────────────────────

    @Tool(description = """
            Verifica si una persona es cliente existente del banco.
            Retorna los datos del cliente si existe, o un mensaje de error si no se encuentra.
            """)
    public Map<String, Object> verificarCliente(
            @ToolParam(description = "Número de cédula o identificación del cliente") String cedula) {

        Map<String, Object> cliente = CLIENTES.get(cedula);

        if (cliente == null) {
            return Map.of(
                    "encontrado", false,
                    "mensaje", "No se encontró ningún cliente con la cédula: " + cedula
            );
        }

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("encontrado", true);
        respuesta.put("cliente", cliente);

        boolean activo = (boolean) cliente.get("activo");
        respuesta.put("mensaje", activo
                ? "Cliente activo encontrado exitosamente."
                : "Cliente encontrado pero su cuenta se encuentra inactiva.");

        return respuesta;
    }

    // ─────────────────────────────────────────────
    // HERRAMIENTA 2: Obtener cuentas disponibles
    // ─────────────────────────────────────────────

    @Tool(description = """
            Obtiene todas las cuentas bancarias disponibles del cliente.
            Incluye número de cuenta, tipo, saldo actual y moneda.
            """)
    public Map<String, Object> obtenerCuentas(
            @ToolParam(description = "Número de cédula o identificación del cliente") String cedula) {

        if (!CLIENTES.containsKey(cedula)) {
            return Map.of(
                    "exito", false,
                    "mensaje", "Cliente no encontrado con la cédula: " + cedula
            );
        }

        List<Map<String, Object>> cuentas = CUENTAS.getOrDefault(cedula, Collections.emptyList());

        if (cuentas.isEmpty()) {
            return Map.of(
                    "exito", true,
                    "cedula", cedula,
                    "total_cuentas", 0,
                    "cuentas", Collections.emptyList(),
                    "mensaje", "El cliente no tiene cuentas registradas."
            );
        }

        double saldoTotal = cuentas.stream()
                .mapToDouble(c -> (double) c.get("saldo"))
                .sum();

        return Map.of(
                "exito", true,
                "cedula", cedula,
                "total_cuentas", cuentas.size(),
                "saldo_total_usd", saldoTotal,
                "cuentas", cuentas,
                "mensaje", "Cuentas obtenidas exitosamente."
        );
    }

    // ─────────────────────────────────────────────
    // HERRAMIENTA 3: Obtener servicios públicos
    // ─────────────────────────────────────────────

    @Tool(description = """
            Obtiene todos los servicios públicos disponibles (electricidad, agua, teléfono, etc.)
            asociados al cliente, con su código, nombre, categoría y deuda actual.
            """)
    public Map<String, Object> obtenerServiciosPublicos(
            @ToolParam(description = "Número de cédula o identificación del cliente") String cedula) {

        if (!CLIENTES.containsKey(cedula)) {
            return Map.of(
                    "exito", false,
                    "mensaje", "Cliente no encontrado con la cédula: " + cedula
            );
        }

        List<Map<String, Object>> servicios = SERVICIOS.getOrDefault(cedula, Collections.emptyList());

        if (servicios.isEmpty()) {
            return Map.of(
                    "exito", true,
                    "cedula", cedula,
                    "total_servicios", 0,
                    "servicios", Collections.emptyList(),
                    "mensaje", "El cliente no tiene servicios públicos registrados."
            );
        }

        double deudaTotal = servicios.stream()
                .mapToDouble(s -> (double) s.get("deuda"))
                .sum();

        return Map.of(
                "exito", true,
                "cedula", cedula,
                "total_servicios", servicios.size(),
                "deuda_total_crc", deudaTotal,
                "servicios", servicios,
                "mensaje", "Servicios públicos obtenidos exitosamente."
        );
    }

    // ─────────────────────────────────────────────
    // HERRAMIENTA 4: Consultar deuda de un servicio
    // ─────────────────────────────────────────────

    @Tool(description = """
            Consulta la deuda actual de un servicio público específico del cliente.
            Retorna el monto de la deuda, fecha de vencimiento y detalles del servicio.
            """)
    public Map<String, Object> consultarDeudaServicio(
            @ToolParam(description = "Número de cédula o identificación del cliente") String cedula,
            @ToolParam(description = "Código del servicio público (ej: LUZ-001, AGUA-001, TEL-001)") String codigoServicio) {

        if (!CLIENTES.containsKey(cedula)) {
            return Map.of(
                    "exito", false,
                    "mensaje", "Cliente no encontrado con la cédula: " + cedula
            );
        }

        List<Map<String, Object>> servicios = SERVICIOS.getOrDefault(cedula, Collections.emptyList());

        Optional<Map<String, Object>> servicio = servicios.stream()
                .filter(s -> s.get("codigo").equals(codigoServicio))
                .findFirst();

        if (servicio.isEmpty()) {
            return Map.of(
                    "exito", false,
                    "mensaje", "No se encontró el servicio con código: " + codigoServicio + " para este cliente."
            );
        }

        Map<String, Object> s = servicio.get();
        double deuda = (double) s.get("deuda");

        return Map.of(
                "exito", true,
                "cedula", cedula,
                "servicio", Map.of(
                        "codigo", s.get("codigo"),
                        "nombre", s.get("nombre"),
                        "categoria", s.get("categoria")
                ),
                "deuda", deuda,
                "moneda", s.get("moneda"),
                "vencimiento", s.get("vencimiento"),
                "tiene_deuda", deuda > 0,
                "mensaje", deuda > 0
                        ? String.format("Deuda pendiente de %.2f %s con vencimiento el %s.", deuda, s.get("moneda"), s.get("vencimiento"))
                        : "No tiene deuda pendiente para este servicio."
        );
    }

    // ─────────────────────────────────────────────
    // HERRAMIENTA 5: Pagar servicio público
    // ─────────────────────────────────────────────

    @Tool(description = """
            Realiza el pago de un servicio público utilizando una cuenta bancaria seleccionada.
            Valida que el cliente exista, que la cuenta tenga saldo suficiente y que el servicio
            tenga deuda pendiente antes de procesar el pago.
            """)
    public Map<String, Object> pagarServicioPublico(
            @ToolParam(description = "Número de cédula o identificación del cliente") String cedula,
            @ToolParam(description = "Código del servicio público a pagar (ej: LUZ-001, AGUA-001, TEL-001)") String codigoServicio,
            @ToolParam(description = "Número de cuenta bancaria con la que se realizará el pago (ej: 001-123456-01)") String numeroCuenta,
            @ToolParam(description = "Monto a pagar en la moneda del servicio") double monto) {

        // Validar cliente
        if (!CLIENTES.containsKey(cedula)) {
            return Map.of("exito", false, "mensaje", "Cliente no encontrado con la cédula: " + cedula);
        }

        // Validar cuenta
        List<Map<String, Object>> cuentas = CUENTAS.getOrDefault(cedula, Collections.emptyList());
        Optional<Map<String, Object>> cuentaOpt = cuentas.stream()
                .filter(c -> c.get("numero_cuenta").equals(numeroCuenta))
                .findFirst();

        if (cuentaOpt.isEmpty()) {
            return Map.of("exito", false, "mensaje", "La cuenta " + numeroCuenta + " no pertenece al cliente.");
        }

        Map<String, Object> cuenta = cuentaOpt.get();
        double saldoActual = (double) cuenta.get("saldo");

        if (saldoActual < monto) {
            return Map.of(
                    "exito", false,
                    "mensaje", String.format("Saldo insuficiente. Saldo disponible: %.2f USD, monto requerido: %.2f.", saldoActual, monto)
            );
        }

        // Validar servicio
        List<Map<String, Object>> servicios = SERVICIOS.getOrDefault(cedula, Collections.emptyList());
        Optional<Map<String, Object>> servicioOpt = servicios.stream()
                .filter(s -> s.get("codigo").equals(codigoServicio))
                .findFirst();

        if (servicioOpt.isEmpty()) {
            return Map.of("exito", false, "mensaje", "No se encontró el servicio con código: " + codigoServicio);
        }

        Map<String, Object> servicio = servicioOpt.get();
        double deudaActual = (double) servicio.get("deuda");

        if (deudaActual <= 0) {
            return Map.of("exito", false, "mensaje", "El servicio " + codigoServicio + " no tiene deuda pendiente.");
        }

        if (monto > deudaActual) {
            return Map.of(
                    "exito", false,
                    "mensaje", String.format("El monto a pagar (%.2f) supera la deuda actual (%.2f).", monto, deudaActual)
            );
        }

        // Procesar pago (actualizamos los mocks en memoria)
        double nuevoSaldo = saldoActual - monto;
        double nuevaDeuda = deudaActual - monto;

        cuenta.put("saldo", nuevoSaldo);
        servicio.put("deuda", nuevaDeuda);

        String referencia = "TXN-" + System.currentTimeMillis();
        String fechaPago = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        return Map.of(
                "exito", true,
                "referencia_pago", referencia,
                "fecha_pago", fechaPago,
                "detalle", Map.of(
                        "cedula", cedula,
                        "servicio", servicio.get("nombre"),
                        "codigo_servicio", codigoServicio,
                        "cuenta_debitada", numeroCuenta,
                        "monto_pagado", monto,
                        "moneda_servicio", servicio.get("moneda"),
                        "saldo_cuenta_anterior", saldoActual,
                        "saldo_cuenta_nuevo", nuevoSaldo,
                        "deuda_anterior", deudaActual,
                        "deuda_restante", nuevaDeuda
                ),
                "mensaje", String.format("Pago de %.2f procesado exitosamente para el servicio %s. Referencia: %s",
                        monto, servicio.get("nombre"), referencia)
        );
    }
}
