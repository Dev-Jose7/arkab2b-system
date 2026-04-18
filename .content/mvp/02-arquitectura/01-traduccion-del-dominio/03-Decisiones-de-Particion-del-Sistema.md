---
title: "Decisiones de particion del sistema"
linkTitle: "3. Decisiones de particion del sistema"
weight: 3
url: "/mvp/arquitectura/traduccion-del-dominio/decisiones-de-particion-del-sistema/"
---

## Proposito de la seccion
Cerrar la particion estructural del sistema indicando que se separa, que puede
convivir y por que, con criterio semantico y operativo.

## Particiones obligatorias por semantica
| Particion | Decision | Criterio de dominio |
|---|---|---|
| `directory` separado | se mantiene como servicio independiente | politica regional y contexto organizacional tienen ownership propio |
| `catalog` separado de `inventory` | no se fusionan en una sola unidad | oferta comercial y verdad de disponibilidad son distintas |
| `inventory` separado de `order` | no se unifica transaccionalmente | reserva/disponibilidad y compromiso comercial tienen fronteras de consistencia distintas |
| `order` separado | mantiene su frontera para estado de pedido y pago manual | ciclo de pedido requiere invariantes propios y alta trazabilidad |
| `notification` separado | se mantiene desacoplado y derivado | no debe mutar transacciones `Core` |
| `reporting` separado | se mantiene como lectura derivada | no redefine verdad transaccional del nucleo |

## Agrupaciones practicas permitidas en `MVP`
| Agrupacion practica | Decision |
|---|---|
| despliegue en mismo cluster/entorno operativo | permitido, siempre que no rompa ownership de datos ni contratos |
| compartir librerias tecnicas (`logging`, `errors`, `security-client`) | permitido solo en capa tecnica, sin compartir modelo semantico |
| consolidar pipelines operativos de observabilidad | permitido; no altera fronteras de dominio |

## Criterios para separar o agrupar
| Criterio | Cuándo separar | Cuándo agrupar |
|---|---|---|
| ownership semantico | cuando hay verdad primaria distinta | cuando es solo concern tecnico transversal |
| consistencia | cuando cambia la frontera de invariantes | cuando no introduce transaccion distribuida artificial |
| ritmo de cambio | cuando evolucionan a velocidades diferentes | cuando el cambio es siempre conjunto y no genera acoplamiento semantico |
| riesgo operacional | cuando el fallo aislado debe ser contenido | cuando el overhead de separacion supera el beneficio semantico |

## Limites obligatorios vs decisiones practicas
| Tipo de limite | Estado |
|---|---|
| fronteras `Core`/`Generic`/tecnico transversal | obligatorias por semantica |
| `database-per-service` y contratos explicitos | obligatorios por ownership |
| topologia de despliegue y escalado por servicio | decision practica ajustable sin romper semantica |
| mecanismo especifico de plataforma (`AWS` objetivo / fallback) | decision practica gobernada por ADR |

## Regla de no sobrefragmentacion
No se introduce particion adicional que no este justificada por:
- frontera semantica clara,
- invariantes propias,
- o necesidad de aislamiento operativo con impacto real.
