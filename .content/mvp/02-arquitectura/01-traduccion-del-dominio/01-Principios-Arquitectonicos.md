---
title: "Principios arquitectonicos"
linkTitle: "1. Principios arquitectonicos"
weight: 1
url: "/mvp/arquitectura/traduccion-del-dominio/principios-arquitectonicos/"
aliases:
  - "/mvp/arquitectura/arc42/introduccion-objetivos/"
  - "/mvp/arquitectura/arc42/restricciones/"
  - "/mvp/arquitectura/arc42/estrategia-solucion/"
---

## Proposito de la seccion
Definir los principios y restricciones estructurales que gobiernan la
arquitectura de ArkaB2B para materializar el dominio vigente sin romper su
semantica.

## Principios que gobiernan la arquitectura
| ID | Principio | Decision arquitectonica obligatoria |
|---|---|---|
| PA-01 | Fidelidad al dominio | ningun componente tecnico puede reinterpretar ownership o reglas cerradas en Dominio |
| PA-02 | Limites semanticos explicitos | separacion por `bounded context` con ownership de datos y comportamiento por contexto |
| PA-03 | Consistencia donde corresponde | consistencia fuerte dentro de cada frontera local y coordinacion eventual entre contextos |
| PA-04 | Integracion semantica primero | contratos se disenan por significado de negocio antes que por detalle tecnico de transporte |
| PA-05 | Desacople temporal selectivo | validaciones criticas en sync; propagacion derivada y side effects en async |
| PA-06 | Idempotencia por defecto en mutaciones | comandos mutantes y consumo de eventos deben soportar reintentos sin duplicar efectos |
| PA-07 | Seguridad transversal + restricciones de negocio | autenticacion/autorizacion como capacidad transversal; aislamiento por organizacion como regla de negocio |
| PA-08 | Evolucion gobernada por ADR | cambios estructurales solo con decision registrada, trade-off explicito y trazabilidad |

## Restricciones estructurales no negociables
| Restriccion | Alcance | Implicacion tecnica |
|---|---|---|
| `Core` con mayor peso arquitectonico (`directory`, `catalog`, `inventory`, `order`) | particion, consistencia, datos y contratos | concentra mayor densidad en componentes, validaciones y ownership de verdad |
| `Generic` con densidad minima suficiente (`notification`, `reporting`) | modelado tecnico y runtime | se mantienen como capacidades del sistema sin competir con la verdad transaccional del `Core` |
| `identity-access` fuera del dominio interno | seguridad y capacidades transversales | no se modela como servicio de dominio de negocio; se integra como capacidad tecnica/plataforma |
| `database-per-context` | persistencia | sin acceso directo a base de otro contexto; intercambio solo por API/evento |
| outbox + dedupe | integracion asincrona | productores publican hechos confiables y consumidores toleran duplicados |
| metadatos de trazabilidad | observabilidad y auditoria | `traceId`, `correlationId`, `organizationId` y actor efectivo en mutaciones criticas |

## Criterios de modularidad, desacoplamiento y autonomia
| Criterio | Regla de aplicacion |
|---|---|
| Modularidad | cada servicio expone capacidades de su contexto y evita incluir logica de otro contexto |
| Desacoplamiento | toda dependencia cross-context debe tener contrato explicito y versionable |
| Ownership | cada dato transaccional tiene un solo owner semantico |
| Autonomia operativa | fallos en capacidades derivadas (`notification`, `reporting`) no revierten estados `Core` ya confirmados |
| Agrupacion pragmatica | solo se agrupa despliegue/modulo cuando no rompe semantica ni ownership del dominio |

## Criterio de fidelidad al dominio
Arquitectura consume como entrada formal la seccion
[Transicion a Arquitectura](/mvp/dominio/transicion-a-arquitectura/).
Cualquier decision que contradiga limites, reglas, contratos o clasificacion de
subdominios definida en Dominio se considera drift y debe corregirse.

## Escenarios de calidad que guian decisiones
| Escenario de calidad | Decision arquitectonica asociada |
|---|---|
| pico de lecturas/mutaciones en servicios `Core` | presupuestos de rendimiento por servicio y degradacion controlada por prioridad funcional |
| checkout concurrente sobre SKU caliente | validaciones sync `order`-`inventory` en camino critico + no confirmacion sin disponibilidad |
| falla parcial de dependencia derivada | mantener confirmacion `Core` y desacoplar `notification`/`reporting` por eventos |
| intento de acceso cross-tenant | rechazo inmediato en borde/servicio owner + auditoria obligatoria |
| backlog de broker y reproceso | outbox en productores, dedupe en consumidores y `DLQ` con runbook |
| carga 3x baseline | escalado progresivo y degradacion aceptable sin romper consistencia semantica |
