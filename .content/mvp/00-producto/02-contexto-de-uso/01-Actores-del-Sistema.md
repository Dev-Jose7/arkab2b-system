---
title: "Actores del Sistema"
linkTitle: "1. Actores del sistema"
weight: 1
url: "/mvp/producto/actores-del-sistema/"
---

## Proposito del archivo
Identificar quienes participan en el producto, desde que interes operan y que
relacion guardan con el sistema.

## Actores de negocio y operacion
| Actor | Rol de negocio | Interes dentro del producto | Relacion con el producto |
|---|---|---|---|
| Comprador B2B recurrente | realiza compra y seguimiento | comprar por volumen con disponibilidad confiable y estado visible | usuario principal del flujo comercial digital (carrito, pedido y seguimiento) |
| Administrador de cuenta B2B | administra la operacion de su organizacion | asegurar segregacion de datos, uso permitido y operacion correcta para su organizacion | responsable de gobernar el uso del producto dentro de su organizacion |
| Operador Arka (comercial/operaciones) | gestiona pedidos, stock y pagos manuales | mantener continuidad operativa y resolver incidencias | actor interno que mantiene el control operativo del ciclo comercial |
| Analista de inventario | monitorea disponibilidad y faltantes | anticipar reposicion y controlar stock | consumidor de salidas del producto para decisiones de abastecimiento |
| Coordinador de despacho | consulta estado y preparacion operativa del pedido | alinear el avance comercial con la atencion posterior | actor interno que conecta el estado comercial con la ejecucion operativa posterior |

## Sistemas y dependencias externas relevantes
| Sistema o canal | Relacion con el producto |
|---|---|
| Frontend externo | consume el backend y expone la experiencia de uso al cliente final |
| Servicio de correo/notificaciones | soporta la entrega de mensajes al cliente |
| Sistemas de proveedores para abastecimiento | participan de forma progresiva en la operacion de reposicion |
| Sistema de reporteria o consumo interno | consulta salidas semanales para soporte comercial y operativo |

## Lectura de actores para el modelado posterior
Los actores anteriores no describen pantallas ni perfiles UI aislados. Describen
perspectivas reales de negocio y operacion que luego condicionan capacidades,
trazabilidad, restricciones operativas de legitimidad y prioridades del dominio.
