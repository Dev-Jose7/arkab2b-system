---
title: "Escenarios de negocio"
linkTitle: "2. Escenarios de negocio"
weight: 2
url: "/mvp/producto/escenarios-de-negocio/"
---

## Propósito de la sección

Describir las situaciones de negocio de alto nivel mediante las cuales los actores obtienen valor del producto en el ciclo MVP.

Estos escenarios nacen del problema de negocio y expresan:

- una situación real,
- un actor principal,
- una intención,
- y un resultado de negocio esperado.

No describen todavía la solución funcional detallada del sistema.  
Su propósito es servir como puente entre el problema de negocio y los casos de uso funcionales.

## Escenarios de negocio identificados

| ID   | Escenario                                                    | Actor principal                     | Intención                                                                 | Resultado de negocio esperado                                                                             |
|------|--------------------------------------------------------------|-------------------------------------|---------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------|
| EN01 | `Compra B2B confiable con validación de disponibilidad`      | Comprador B2B                       | Confirmar una compra con información confiable de oferta y disponibilidad | La compra se realiza sin depender de validaciones manuales informales y sin comprometer stock inexistente |
| EN02 | `Confirmación de pedido con promesa comercial protegida`     | Comprador B2B / Operación comercial | Formalizar un pedido cuya disponibilidad ya fue validada                  | El sistema protege la promesa comercial y reduce sobreventa y reprocesos                                  |
| EN03 | `Seguimiento visible del estado del pedido`                  | Comprador B2B                       | Consultar el estado vigente y entendible del pedido sin depender de seguimiento manual | El comprador obtiene visibilidad clara del estado actual del pedido                                       |
| EN04 | `Seguimiento operativo interno del ciclo del pedido`         | Operación Arka                      | Registrar y revisar cambios operativos del pedido durante su ciclo        | La operación conserva trazabilidad verificable de cambios y reduce tiempos muertos                        |
| EN05 | `Registro y control trazable del pago manual`                | Operación comercial / financiera    | Registrar pagos manuales asociados a pedidos de forma controlada          | El ciclo comercial queda trazable y auditable sin depender de registros dispersos                         |
| EN06 | `Coordinación de cambios relevantes mediante notificaciones` | Comprador B2B / Operación Arka      | Recibir comunicación oportuna ante cambios relevantes del ciclo comercial | Los actores se mantienen informados sin reemplazar la consulta de estado ni el historial verificable      |
| EN07 | `Seguimiento semanal de ventas y abastecimiento`             | Ventas / Inventario / Operación     | Revisar el comportamiento comercial y operativo del periodo               | El negocio obtiene lectura accionable para reposición, control y decisión semanal                         |
| EN08 | `Operación preparada para crecimiento regional`              | Negocio / Operación                 | Operar bajo reglas regionales por pais sin rehacer la solucion base       | El sistema puede crecer por pais o region, cambiando condiciones operativas sin alterar el flujo core     |

## Relación con el problema de negocio

| Escenario | Fricciones a las que responde                                                           |
|-----------|-----------------------------------------------------------------------------------------|
| EN01      | validaciones manuales de disponibilidad; stock no sincronizado con la promesa comercial |
| EN02      | stock no sincronizado con la promesa comercial; validaciones manuales de disponibilidad |
| EN03      | baja visibilidad del estado del pedido                                                  |
| EN04      | baja visibilidad del estado del pedido; soporte manual para seguimiento                 |
| EN05      | soporte manual para seguimiento y pagos                                                 |
| EN06      | baja visibilidad del estado del pedido; soporte manual para seguimiento                 |
| EN07      | reprocesos operativos; necesidad de lectura confiable para abastecimiento y control     |
| EN08      | baja preparación para crecimiento regional                                              |

## Separacion semantica obligatoria
| Capacidad | Que resuelve | Que no reemplaza |
|---|---|---|
| `Visibilidad del pedido` | consulta del estado vigente y entendible del pedido | no reemplaza historial de cambios ni notificaciones |
| `Historial / trazabilidad` | evidencia verificable de cambios ya ocurridos | no reemplaza estado vigente ni notificacion oportuna |
| `Notificacion` | comunicacion derivada por cambios relevantes | no reemplaza el hecho de negocio ni la trazabilidad completa |

## Lectura metodologica

| Aspecto | Lectura correcta |
|---|---|
| Qué son | Son situaciones reales de negocio donde un actor quiere lograr algo y obtener un resultado de valor. |
| Qué explican | Quién participa, en qué contexto ocurre, con qué intención se activa y qué resultado de negocio busca. |
| Qué no son | No son requisitos funcionales, no son endpoints, no son pantallas, no son comandos ni agregados. |
| Qué viene despues | A partir de estos escenarios se derivan los casos de uso funcionales. |

Los escenarios de negocio descubren la necesidad.
Los casos de uso funcionales estructuran la respuesta del sistema.
Los requisitos funcionales formalizan esa respuesta como capacidad obligatoria.
