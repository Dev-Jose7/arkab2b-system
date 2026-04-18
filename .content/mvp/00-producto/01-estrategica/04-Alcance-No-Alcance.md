---
title: "Alcance / No Alcance"
linkTitle: "4. Alcance / No alcance"
weight: 4
url: "/mvp/producto/alcance-no-alcance/"
---

## Proposito del archivo
Delimitar con precision que entra en el `MVP`, que queda fuera y que decisiones
se dejan explicitamente para una etapa posterior.

## Alcance del `MVP`
- gestion de catalogo de productos y variantes (SKU), stock y disponibilidad;
- gestion de carrito, confirmacion de pedido y seguimiento de estado;
- notificaciones de cambio de estado y recordatorios de carrito abandonado;
- reportes semanales de ventas y de productos por abastecer;
- condiciones operativas de legitimidad de actor y aislamiento por organizacion
  consumidas desde capacidad transversal;
- registro de pago manual para reflejar estado de pago del pedido.

## No alcance del `MVP`
- desarrollo de frontend;
- pasarela de pago online completa;
- analitica predictiva avanzada de demanda;
- fulfillment extendido operativo (`READY_TO_DISPATCH`, `DISPATCHED`, `DELIVERED`);
- operacion multi-region endurecida;
- compliance exhaustivo por pais;
- permisos hipergranulares por mercado o segmento;
- federacion enterprise con IdP externo.

## Frontera operativa del ciclo
| Entra ahora | Se difiere |
|---|---|
| flujo core de catalogo, disponibilidad, pedido y legitimidad operativa de actor | automatizaciones complejas y hardening enterprise |
| readiness regional controlado por configuracion | cumplimiento juridico exhaustivo por pais |
| soporte operativo por notificacion y reportes semanales | fulfillment y analitica avanzada |
| pago manual `MVP` | pagos online y conciliacion automatica bancaria |

## Decisiones que se dejan para despues
| Decision diferida | Estado en `MVP` | Motivo de diferimiento |
|---|---|---|
| automatizaciones complejas y hardening enterprise | fuera de alcance actual | priorizar estabilidad del flujo core antes de ampliar complejidad |
| cumplimiento juridico exhaustivo por pais | fuera de alcance actual | en `MVP` se asegura readiness funcional regional, no cierre legal completo |
| fulfillment extendido y analitica avanzada | fuera de alcance actual | la fase actual se concentra en confirmacion comercial inicial y soporte operativo base |
| pagos online y conciliacion automatica bancaria | fuera de alcance actual | el `MVP` opera con pago manual controlado para salir con menor riesgo |

## Limites asumidos
| Tipo de limite | Limite asumido en `MVP` | Implicacion operativa |
|---|---|---|
| Temporal | las capacidades diferidas se atienden en etapas posteriores al ciclo `MVP` | el baseline actual se cierra sin bloquear por funcionalidades futuras |
| Funcional | el foco es catalogo, disponibilidad, pedido, notificacion base, reportes semanales y pago manual | no se modelan ni implementan capacidades avanzadas fuera del flujo core |
| Canal | `MVP` centrado en backend y frontend a cargo de proveedor externo | el pilar Producto define verdad funcional del backend, no experiencia UI final |
