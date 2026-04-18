---
title: "Evolucion"
linkTitle: "5. Evolucion"
weight: 5
url: "/mvp/dominio/evolucion/"
---

## Proposito de la seccion
Definir como puede crecer el dominio sin romper coherencia con Producto,
manteniendo claro que partes estan cerradas y que tensiones siguen abiertas.

## Madurez del modelo por subdominio
| Subdominio / capacidad | Nivel de madurez | Estado de madurez | Motivo |
|---|---|---|---|
| `Nucleo` (`Core`) - `directorio` (`directory`) | alta | estable | gobierna aislamiento organizacional y regionalizacion del MVP |
| `Nucleo` (`Core`) - `catalogo` (`catalog`) | alta | estable | publica oferta vendible para flujo comercial principal |
| `Nucleo` (`Core`) - `inventario` (`inventory`) | alta | estable | protege promesa comercial con stock y disponibilidad |
| `Nucleo` (`Core`) - `pedidos` (`order`) | alta | estable | concentra carrito, pedido, estado y pago manual en una sola frontera de consistencia |
| `Generico` (`Generic`) - `notificaciones` (`notification`) | minima suficiente | acotado | cubre comunicacion derivada sin sobre-modelado |
| `Generico` (`Generic`) - `reporteria` (`reporting`) | minima suficiente | acotado | cubre `snapshots` semanales sin mutar nucleo |
| `identidad-acceso` (`identity-access`) (tecnico transversal) | fuera del dominio interno | externo al modelo de negocio | aporta legitimidad de actor, no verdad semantica de negocio |

## Tensiones abiertas y direccion de evolucion
| Tension | Riesgo si no se atiende | Direccion de evolucion |
|---|---|---|
| crecimiento regional mas profundo | divergencia de reglas entre paises | endurecer versionado y consumo de `PoliticaPorPais` (`CountryPolicy`) por contrato |
| incremento de volumen operativo | degradacion de tiempos y consistencia en flujos de nucleo | reforzar diseno de contratos asincronos e idempotencia |
| mayor demanda de seguimiento comercial | sobrecarga manual en operacion | ampliar lectura de trazabilidad sin mover verdad fuera de `pedidos` (`order`) |
| evolucion de pagos mas alla de MVP manual | ambiguedad financiera futura | particionar capacidad de pago cuando Producto lo formalice |
| expansion analitica | presion de mover logica de nucleo a reporteria | mantener `reporteria` (`reporting`) como lectura derivada y separar analitica avanzada |
| crecimiento de artefactos de evidencia | confundir lecturas derivadas con hechos de negocio | mantener pureza de eventos de dominio y modelar evidencia como salida derivada |

## Evolucion por prioridades del MVP
| Prioridad de Producto | Impacto en evolucion de dominio |
|---|---|
| legitimidad operativa y aislamiento | consolidar reglas de actor legitimado y aislamiento organizacional |
| catalogo y stock | estabilizar contratos entre `catalogo` (`catalog`) e `inventario` (`inventory`) |
| pedido y pago manual | reforzar invariantes de `pedidos` (`order`) en su propio agregado |
| preparacion regional | robustecer `directorio` (`directory`) como propietario semantico de politica regional |
| capacidades derivadas | mantener `notificaciones` (`notification`) y `reporteria` (`reporting`) en densidad minima suficiente |
