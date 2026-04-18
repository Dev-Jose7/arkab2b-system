---
title: "Capacidades transversales y dependencias externas"
linkTitle: "11. Capacidades transversales y dependencias externas"
weight: 11
url: "/mvp/arquitectura/realizacion-tecnica/capacidades-transversales-y-dependencias-externas/"
---

## Proposito de la seccion
Delimitar las capacidades tecnicas transversales y dependencias externas que el
sistema consume, asegurando que no contaminen el dominio interno.

## Capacidades transversales del sistema
| Capacidad | Rol arquitectonico | Contextos/servicios consumidores |
|---|---|---|
| `identity-access` | autenticacion/autorizacion y legitimidad de actor | borde (`api-gateway`) y servicios de negocio |
| `api-gateway` | control de borde, ruteo y enforcement tecnico inicial | todos los servicios HTTP |
| `config/discovery` | configuracion central y descubrimiento de servicios | todos los servicios |
| `broker de eventos` | integracion asincrona y desacople temporal | `Core`, `notification`, `reporting` |
| `cache/coordination` | optimizacion de lectura, idempotencia y control de contencion | servicios con necesidad operativa |
| `observabilidad` | logs, metricas, trazas, alertas | todo el sistema |
| `secret management` | gestion de secretos y rotacion | plataforma y servicios |

## Dependencias externas relevantes
| Dependencia | Uso principal |
|---|---|
| proveedor de notificaciones | entrega de email/sms/canal equivalente |
| almacenamiento de objetos | artefactos de reporte y respaldos |
| plataforma cloud objetivo (`AWS`) | runtime, red, datos y servicios administrados |
| fallback autorizado (`Railway`, si aplica) | continuidad de despliegue manteniendo topologia logica |

## Regla de integracion de dependencias externas
| Regla | Aplicacion |
|---|---|
| encapsulamiento por puertos/adaptadores | ningun servicio de dominio depende de SDK externo en capa de dominio |
| degradacion controlada | fallo externo no debe romper invariantes `Core` ya materializadas |
| observabilidad obligatoria | toda dependencia externa debe tener trazabilidad operativa |
| sustitucion gobernada | cambio de proveedor requiere ADR y verificacion de compatibilidad contractual |

## Limites para evitar contaminacion semantica
- `identity-access` no define lenguaje de negocio de ArkaB2B;
- proveedor de notificaciones no define estados transaccionales de pedido;
- plataforma de observabilidad no redefine reglas del dominio;
- decisiones de infraestructura no alteran ownership de `bounded contexts`.
