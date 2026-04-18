---
title: "Integracion y consistencia"
linkTitle: "3. Integracion y consistencia"
weight: 3
url: "/mvp/arquitectura/integracion-y-consistencia/"
---

## Proposito del bloque
Este bloque formaliza como se integran los servicios entre si y como se protege
la verdad de cada contexto en escenarios distribuidos.

## Preguntas que responde
- Que contratos cruzan fronteras y que informacion no debe cruzar.
- Que flujos son sincronos, cuales son asincronos y por que.
- Donde termina cada transaccion local y como se gobierna la consistencia eventual.

## Secciones del bloque
| Seccion | Aporte principal |
|---|---|
| Contratos de integracion | define interfaces, eventos y ownership semantico de intercambio |
| Interacciones sincronas y asincronas | define patrones de coordinacion temporal y desacople |
| Consistencia, transacciones y manejo de eventos | formaliza limites transaccionales, outbox/dedupe y fallos de propagacion |

## Criterio metodologico
Este bloque aplica `contract-first`, diseno orientado a eventos y fronteras de
consistencia por contexto para evitar acoplamiento accidental.
