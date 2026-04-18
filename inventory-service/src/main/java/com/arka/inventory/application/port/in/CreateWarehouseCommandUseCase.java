package com.arka.inventory.application.port.in;

import com.arka.inventory.application.command.CreateWarehouseCommand;
import com.arka.inventory.application.result.WarehouseResult;
import reactor.core.publisher.Mono;

public interface CreateWarehouseCommandUseCase {

    Mono<WarehouseResult> handle(CreateWarehouseCommand command);
}
