---
title: "MVP"
weight: 10
params:
  ciclo: "mvp"
  baselineVersion: "0.1.1"
  sidebarmenus:
    - type: page
      identifier: ciclo
      pageRef: /mvp/
      main: true
      disableTitle: true
cascade:
  - params:
      sidebarmenus:
        - type: page
          identifier: ciclo
          pageRef: /mvp/
          main: true
          disableTitle: true
url: "/mvp/"
---

## Objetivo del ciclo de vida

`MVP` define el baseline funcional y no funcional del sistema ArkaB2B para salir
a operacion inicial con alcance controlado.

Este ciclo concentra el trabajo de definicion, validacion y trazabilidad de
requerimientos que habilitan la primera entrega util del producto.

## Convencion y versionado

- Ciclo vigente: `mvp`.
- Version documental del baseline: `0.1.1`.

## Alcance actual

- Definicion de producto organizada por estrategia, contexto de uso, capacidades,
  restricciones, semantica inicial y prioridades.
- Baseline de dominio por bounded context, reglas, eventos y contratos.
- Baseline de arquitectura organizado por traduccion del dominio, estructura
  del sistema, integracion/consistencia, realizacion tecnica y
  despliegue/trazabilidad.

## Estado rapido del ciclo

| Pilar | Estado | Comentario |
|---|---|---|
| 00-producto | Cerrado | Estructura de producto normalizada y baseline `MVP` congelado. |
| 01-dominio | Cerrado | Semantica, reglas, eventos y contratos de integracion alineados al baseline vigente. |
| 02-arquitectura | Cerrado | Estructura oficial por bloques (traduccion, estructura, integracion, realizacion y despliegue) coherente con Dominio. |
| 03-calidad | Cerrado | Sistema de verificacion y certificacion minima definido de forma auditable. |
| 04-operacion | Cerrado | Baseline operativo listo para despliegue, observabilidad e incidentes en `v0.1.1`. |

## Artefactos disponibles en MVP

- [Producto](/mvp/producto/)
- [Resumen del producto](/mvp/producto/resumen-del-producto/)
- [Problema de negocio](/mvp/producto/problema-de-negocio/)
- [Objetivos del sistema](/mvp/producto/objetivos-del-sistema/)
- [Requisitos funcionales](/mvp/producto/requisitos-funcionales/)
- [Requisitos no funcionales](/mvp/producto/definicion-funcional/requisitos-no-funcionales/)
- [Glosario preliminar](/mvp/producto/glosario-preliminar/)
- [Prioridades del MVP / roadmap inmediato](/mvp/producto/prioridades-mvp-roadmap-inmediato/)
- [Dominio](/mvp/dominio/)
- [Bounded contexts](/mvp/dominio/bounded-contexts/)
- [Lenguaje Ubicuo](/mvp/dominio/lenguaje-ubicuo/)
- [Eventos de Dominio](/mvp/dominio/eventos-de-dominio/)
- [Actores, reglas y politicas](/mvp/dominio/actores-reglas-y-politicas/)
- [Agregados](/mvp/dominio/agregados/)
- [Contratos de Integracion](/mvp/dominio/contratos-de-integracion/)
- [Trazabilidad del Dominio](/mvp/dominio/trazabilidad/)
- [Arquitectura](/mvp/arquitectura/)
- [Traduccion del dominio](/mvp/arquitectura/traduccion-del-dominio/)
- [Estructura del sistema](/mvp/arquitectura/estructura-del-sistema/)
- [Integracion y consistencia](/mvp/arquitectura/integracion-y-consistencia/)
- [Realizacion tecnica](/mvp/arquitectura/realizacion-tecnica/)
- [Despliegue y trazabilidad](/mvp/arquitectura/despliegue-y-trazabilidad/)
- [Calidad](/mvp/calidad/)
- [Estrategia de Pruebas](/mvp/calidad/estrategia-pruebas/)
- [Operacion](/mvp/operacion/)
- [Runbooks Operativos](/mvp/operacion/runbooks/)

## Flujo recomendado

1. Revisar estrategia y contexto de uso del producto.
2. Validar FR y NFR del ciclo `MVP`.
3. Confirmar glosario preliminar y prioridades inmediatas.
4. Consumir dominio como baseline semantico ejecutable.
5. Revisar arquitectura para definir servicios, contratos, seguridad, runtime,
   datos y despliegue del ciclo.
6. Usar calidad y operacion como guia de verificacion y ejecucion sobre el
   baseline cerrado.

## Regla de evolucion

En este repositorio, cada ciclo de vida agrega nuevos requerimientos o deltas
sobre IDs existentes. No se reescribe informacion sin cambio: se referencia por
ID y se documenta la diferencia.
