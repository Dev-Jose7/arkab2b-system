---
title: "Decisiones arquitectonicas (ADR)"
linkTitle: "14. Decisiones arquitectonicas (ADR)"
weight: 14
url: "/mvp/arquitectura/despliegue-y-trazabilidad/decisiones-arquitectonicas-adr/"
aliases:
  - "/mvp/arquitectura/arc42/decisiones-arquitectonicas/"
  - "/mvp/arquitectura/arc42/adr/"
---

## Proposito de la seccion
Registrar decisiones estructurales de arquitectura, su problema, alternativas,
opcion elegida, trade-offs y consecuencias vigentes.

## Catalogo ADR activo (migrado y depurado desde `legacy`)
| ADR | Problema que resolvia | Opcion elegida | Alternativas descartadas | Trade-off principal | Consecuencias vigentes | Evidencia en pilar actual |
|---|---|---|---|---|---|---|
| ADR-001 | evitar mezcla semantica y acoplamiento fuerte entre contextos | arquitectura por contexto con estilo hexagonal interno (`adapter-in/application/domain/adapter-out`) | monolito modular sin ownership fuerte, separacion por capas tecnicas sin limites de dominio | mayor complejidad operativa en despliegue/observabilidad | limites de contexto mas estables, escalado selectivo por servicio, contratos explicitos entre contextos | secciones 02, 05, 06, 15 |
| ADR-002 | proteger invariantes criticas y desacoplar side effects derivados | integracion combinada `sync`/`async` con `outbox` y dedupe | solo `sync`, solo `async`, publicacion directa sin `outbox` | mayor complejidad de consistencia eventual y operacion de broker | camino critico `Core` consistente y salida derivada desacoplada (`notification`/`reporting`) | secciones 07, 08, 09, 15 |
| ADR-003 | evitar ambiguedad entre oferta comercial y disponibilidad fisica | separacion `catalog` vs `inventory` con contratos explicitos | fusion `catalog+inventory` en un unico contexto/servicio | mas integraciones y governance contractual | ownership claro de precio/vendibilidad vs stock/reserva/disponibilidad | secciones 02, 03, 05, 07, 10 |
| ADR-004 | reducir ruptura de integraciones por evolucion de contratos | versionado de APIs por major y eventos por `eventVersion` | evolucion sin versionado formal, cambios breaking in-place | mayor costo de convivencia temporal de versiones | integraciones mas estables y despliegues menos riesgosos | secciones 07, 08, 09 |
| ADR-005 | reducir riesgo de acceso cruzado y falta de trazabilidad | aislamiento por organizacion operante + metadata de auditoria obligatoria | controles solo en borde sin enforcement en dominio, auditoria parcial | mas validaciones y mayor volumen de telemetria/auditoria | mejor capacidad de investigacion de incidentes y control de operaciones sensibles | secciones 10, 12, 15 |

## Decisiones heredadas integradas como lineamientos tecnicos
| Lineamiento heredado de `legacy` | Estado en arquitectura actual |
|---|---|
| empaquetado reproducible por servicio (`Docker`) y stack local/integracion con `compose` | integrado como lineamiento operativo, sin redefinir fronteras de dominio |
| uso de plataforma administrada con fallback controlado | tratado como decision operativa ajustable, no como cambio semantico de arquitectura |
| bloqueo semantico cuando falta politica regional vigente | integrado en contratos de `directory` consumidos por `order` y `reporting` |

## Alternativas descartadas relevantes
| Decision | Alternativa descartada | Motivo de descarte |
|---|---|---|
| estilo por contexto | agrupar logica `Core` y `Generic` en un solo servicio | mayor riesgo de contaminacion semantica y menor trazabilidad |
| estrategia de integracion | confirmar pedido dependiendo sync de `notification`/`reporting` | acoplamiento temporal innecesario para capacidades derivadas |
| separacion oferta/disponibilidad | usar disponibilidad de `inventory` como atributo editable de `catalog` | elimina ownership claro y aumenta riesgo de sobreventa |
| versionado | romper contratos in-place | impacto alto sobre consumidores y despliegues |
| seguridad de frontera | tratar `identity-access` como BC interno del negocio | contradice clasificacion de dominio y mezcla semantica tecnica con negocio |

## Regla para crear nuevo ADR
Se requiere ADR cuando cambie alguno de estos ejes:
- frontera de `bounded contexts` o ownership;
- estrategia de consistencia/integracion;
- seguridad transversal;
- topologia de despliegue base.

## Consecuencia metodologica
Las decisiones registradas en esta seccion no redefinen Dominio. Solo
materializan tecnicamente el dominio vigente y deben mantener trazabilidad
explicita hacia los comandos, contratos y reglas ya cerrados.
