---
title: "Casos de uso funcionales"
linkTitle: "1. Casos de uso funcionales"
weight: 1
url: "/mvp/producto/casos-de-uso-funcionales/"
---

## Proposito del archivo
Mapear los casos de uso funcionales derivados de los escenarios de negocio del
ciclo `MVP`, dejando explicita la respuesta operativa que el sistema debe dar
antes de formalizar los `FR`.

## Casos de uso funcionales identificados

| ID | Caso de uso funcional | Escenario(s) de origen | Actor principal | Intención funcional | Resultado esperado |
|---|---|---|---|---|---|
| CUF01 | `Consultar catálogo vendible` | EN01, EN02 | Comprador B2B | Explorar la oferta disponible para compra | El actor identifica productos y variantes que pueden ser considerados para compra |
| CUF02 | `Consultar disponibilidad antes de confirmar compra` | EN01, EN02 | Comprador B2B | Validar si la oferta seleccionada puede ser comprometida comercialmente | El actor decide con información confiable antes de confirmar |
| CUF03 | `Gestionar carrito de compra` | EN01 | Comprador B2B | Consolidar una intención de compra antes del pedido | El actor reúne y ajusta los productos que desea comprar |
| CUF04 | `Crear pedido a partir del carrito` | EN01, EN02 | Comprador B2B | Formalizar una compra | El sistema registra un pedido trazable a partir de una intención validada |
| CUF05 | `Ajustar pedido antes de cierre` | EN02 | Comprador B2B / Operación comercial | Corregir o modificar el pedido dentro de la ventana permitida | El pedido puede corregirse sin romper consistencia operacional |
| CUF06 | `Consultar estado del pedido` | EN03 | Comprador B2B | Conocer el estado vigente del pedido | El actor obtiene visibilidad clara del estado actual del pedido |
| CUF07 | `Actualizar estado operativo del pedido` | EN04 | Operación Arka | Reflejar el avance operativo real del pedido | El sistema registra transiciones operativas y conserva trazabilidad verificable |
| CUF08 | `Registrar pago manual` | EN05 | Operación comercial / financiera | Registrar evidencia de pago asociada a un pedido | El pedido queda trazado con información de pago manual |
| CUF09 | `Consultar estado financiero del pedido` | EN05 | Operación comercial / financiera | Verificar si el pedido tiene pago pendiente, registrado o validado | El seguimiento comercial y financiero se realiza con evidencia consistente |
| CUF10 | `Emitir notificación por cambio relevante` | EN06 | Sistema / Operación Arka | Informar cambios importantes del ciclo comercial | Los actores reciben comunicacion oportuna sin reemplazar consulta de estado ni historial |
| CUF11 | `Consultar historial de cambios relevantes` | EN03, EN04, EN06 | Comprador B2B / Operación Arka | Revisar trazabilidad cronologica de cambios ocurridos sobre el pedido | El actor accede a evidencia verificable del cambio ya ocurrido |
| CUF12 | `Actualizar stock operativo` | EN01, EN02, EN07 | Operación / Inventario | Mantener el stock alineado con la realidad operativa | La disponibilidad publicada se sostiene sobre stock confiable |
| CUF13 | `Recalcular disponibilidad comprometible` | EN01, EN02, EN07 | Sistema / Inventario | Reflejar el impacto de cambios de stock o reservas en la disponibilidad | El sistema mantiene una promesa comercial coherente |
| CUF14 | `Generar reporte semanal de ventas` | EN07 | Ventas / Operación | Obtener lectura consolidada del desempeño comercial del periodo | El negocio dispone de visibilidad semanal accionable |
| CUF15 | `Generar reporte de abastecimiento o reposición` | EN07 | Inventario / Operación | Detectar faltantes o presión de abastecimiento | El negocio identifica necesidades de reposición |
| CUF16 | `Configurar política regional aplicable` | EN08 | Negocio / Operación | Definir reglas operativas por país o región | El sistema puede operar bajo reglas regionales sin rehacer su base |
| CUF17 | `Aplicar reglas regionales en la operación` | EN08 | Sistema | Ejecutar comportamiento condicionado por país o región | La operación se adapta al contexto regional vigente |

## Relación entre escenarios y casos de uso funcionales

| Escenario | Casos de uso funcionales derivados |
|---|---|
| EN01 `Compra B2B confiable con validación de disponibilidad` | CUF01, CUF02, CUF03, CUF04 |
| EN02 `Confirmación de pedido con promesa comercial protegida` | CUF01, CUF02, CUF04, CUF05, CUF13 |
| EN03 `Seguimiento visible del estado del pedido` | CUF06, CUF11 |
| EN04 `Seguimiento operativo interno del ciclo del pedido` | CUF07, CUF11 |
| EN05 `Registro y control trazable del pago manual` | CUF08, CUF09 |
| EN06 `Coordinación de cambios relevantes mediante notificaciones` | CUF10, CUF11 |
| EN07 `Seguimiento semanal de ventas y abastecimiento` | CUF12, CUF13, CUF14, CUF15 |
| EN08 `Operación preparada para crecimiento regional` | CUF16, CUF17 |

## Separacion semantica entre capacidades relacionadas
| Capacidad funcional | Caso(s) de uso | Foco principal |
|---|---|---|
| `Visibilidad de estado` | CUF06 | estado vigente del pedido para consulta inmediata |
| `Historial / trazabilidad` | CUF07, CUF11 | evidencia verificable y cronologica de cambios operativos |
| `Notificacion derivada` | CUF10 | comunicacion oportuna ante cambios relevantes ya confirmados |

## Lectura metodologica

| Aspecto | Lectura correcta |
|---|---|
| Qué son | Son la traduccion funcional de los escenarios de negocio en acciones concretas que el sistema debe soportar. |
| Qué explican | Qué hace el sistema para responder al escenario y qué secuencia funcional ejecuta. |
| Qué no son | No son requisitos funcionales, no son endpoints, no son pantallas ni comandos de dominio. |
| Qué viene despues | A partir de estos casos de uso se formalizan los requisitos funcionales. |

Los escenarios de negocio descubren la necesidad.
Los casos de uso funcionales estructuran la respuesta del sistema.
Los requisitos funcionales formalizan esa respuesta como capacidad obligatoria.
