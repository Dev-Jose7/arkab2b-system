---
title: "Definicion funcional"
linkTitle: "3. Definicion funcional"
weight: 3
url: "/mvp/producto/definicion-funcional/"
---

## Proposito del bloque
La seccion Definicion funcional formaliza lo que el producto debe hacer y las
condiciones bajo las cuales debe operar en el ciclo `MVP`. Aqui se consolidan
los casos de uso funcionales como descomposicion operativa de los escenarios de
negocio, los requisitos funcionales y los requisitos no funcionales (`NFR`)
con impacto semantico y operativo.

## Preguntas que responde
- Que casos de uso funcionales derivan de los escenarios de negocio.
- Que requisitos funcionales concretos debe ofrecer el producto.
- Que comportamientos esperados deben cumplirse en los recorridos clave.
- Bajo que restricciones operativas, de seguridad, trazabilidad o regionalizacion debe operar.
- Que condiciones no funcionales alteran el diseno y la validacion del sistema.

## Secciones del bloque
| Seccion | Aporte principal |
|---|---|
| Casos de uso funcionales | descompone escenarios de negocio en acciones funcionales del sistema y prepara la derivacion de FR |
| Requisitos funcionales | formaliza capacidades obligatorias, origen funcional, trazabilidad y criterios de aceptacion por requisito |
| Requisitos no funcionales | define umbrales, validacion, alcance operativo e impacto de restricciones que condicionan diseno y operacion |

## Salida hacia el resto del stack documental
Este bloque deja una base ejecutable para:
- Dominio: descubrir conceptos, reglas e invariantes a partir de capacidades y comportamiento esperado.
- Dominio: rastrear como los casos de uso funcionales estructuran la respuesta del sistema frente a los escenarios de negocio.
- Arquitectura: materializar restricciones que condicionan estructura, seguridad, rendimiento y disponibilidad.
- Calidad y Operacion: verificar cumplimiento de requisitos y sostener evidencia operativa.

## Criterio editorial
Definicion funcional no describe implementacion tecnica detallada ni modelado
tactico cerrado; su funcion es declarar requisitos trazables, verificables y
sin ambiguedad semantica critica para el `MVP`.
