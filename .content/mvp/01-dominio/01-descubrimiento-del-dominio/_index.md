---
title: "Descubrimiento del dominio"
linkTitle: "1. Descubrimiento del dominio"
weight: 1
url: "/mvp/dominio/descubrimiento-del-dominio/"
aliases:
  - "/mvp/dominio/estrategica/"
  - "/mvp/dominio/verdad-dominio/"
  - "/mvp/dominio/tactico/"
---

## Proposito del bloque
Descubrir el dominio desde el comportamiento real del negocio de ArkaB2B,
partiendo de escenarios, casos de uso funcionales, `FR`, `NFR` y glosario del
pilar de Producto.

## Pregunta que responde
"Que ocurre en el negocio, que lo dispara, bajo que reglas se valida y donde
emergen fronteras semanticas de consistencia?"

## Secuencia interna
1. hechos del negocio (`eventos`);
2. intenciones que los provocan (`comandos`);
3. actores, reglas y politicas que los gobiernan;
4. unidades de consistencia (`agregados`);
5. fronteras semanticas en contextos delimitados (`bounded contexts`).

## Secciones del bloque
| Seccion | Aporte principal |
|---|---|
| Eventos de dominio | identifica hechos significativos y su cambio de negocio |
| Comandos | identifica intenciones de negocio que disparan los hechos |
| Actores, reglas y politicas | identifica gobierno semantico de los flujos |
| Agregados | identifica donde debe cerrarse consistencia e invariantes |
| Contextos delimitados (`bounded contexts`) | identifica propiedad semantica y limites entre contextos |

## Criterio de salida
Al cerrar este bloque, el dominio queda descubierto en terminos de
comportamiento, gobierno semantico, consistencia y fronteras, sin caer en un
modelo de datos.
