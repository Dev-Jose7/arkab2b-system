---
title: "Vista de despliegue"
linkTitle: "13. Vista de despliegue"
weight: 13
url: "/mvp/arquitectura/despliegue-y-trazabilidad/vista-de-despliegue/"
aliases:
  - "/mvp/arquitectura/arc42/vista-despliegue/"
---

## Proposito de la seccion
Definir topologia minima de despliegue, entornos y conexiones runtime para
operar la arquitectura del `MVP`.

## Entornos de despliegue
| Entorno | Objetivo |
|---|---|
| `local` | desarrollo individual y pruebas de integracion temprana |
| `dev` | integracion continua del baseline |
| `qa` | validacion funcional/no-regresion |
| `staging` | preproduccion con configuracion cercana a produccion |
| `prod` | operacion productiva con SLO y alertas activas |

## Topologia minima en runtime
```mermaid
flowchart TB
  USERS["Clientes y Operacion"] --> EDGE["API Gateway"]

  subgraph APP["Plano de aplicacion"]
    DIR["directory-service"]
    CAT["catalog-service"]
    INV["inventory-service"]
    ORD["order-service"]
    NOTI["notification-service"]
    REP["reporting-service"]
  end

  subgraph DATA["Plano de datos"]
    KAFKA[("event broker")]
    REDIS[("cache")]
    DBDIR[("db-directory")]
    DBCAT[("db-catalog")]
    DBINV[("db-inventory")]
    DBORD[("db-order")]
    DBNOTI[("db-notification")]
    DBREP[("db-reporting")]
  end

  subgraph EXT["Externos/Transversales"]
    IAM["identity-access"]
    NPROV["notification provider"]
    OBS["observability"]
  end

  EDGE --> DIR
  EDGE --> CAT
  EDGE --> INV
  EDGE --> ORD
  EDGE --> REP
  EDGE --> IAM

  DIR --> DBDIR
  CAT --> DBCAT
  INV --> DBINV
  ORD --> DBORD
  NOTI --> DBNOTI
  REP --> DBREP

  DIR --> KAFKA
  CAT --> KAFKA
  INV --> KAFKA
  ORD --> KAFKA
  KAFKA --> NOTI
  KAFKA --> REP
  NOTI --> KAFKA

  NOTI --> NPROV
  DIR --> OBS
  CAT --> OBS
  INV --> OBS
  ORD --> OBS
  NOTI --> OBS
  REP --> OBS
```

## Artefactos y unidades de despliegue
| Unidad | Tipo |
|---|---|
| `api-gateway` | servicio de borde |
| `directory/catalog/inventory/order/notification/reporting` | servicios de aplicacion |
| `broker`, `cache`, `db por servicio` | componentes stateful |
| `identity-access`, proveedor de notificaciones, observabilidad | dependencias externas/transversales |

## Plataforma objetivo y fallback
| Escenario | Decision |
|---|---|
| plataforma objetivo | `AWS` administrado |
| fallback tactico | `Railway` (si aplica) manteniendo topologia y controles |
| emulacion local | herramientas de entorno local sin considerarse despliegue productivo |

## Reglas de despliegue
- solo el borde expone superficie publica;
- componentes stateful operan en plano privado;
- secretos fuera del repositorio;
- promocion por entornos con gates y evidencia operativa.
