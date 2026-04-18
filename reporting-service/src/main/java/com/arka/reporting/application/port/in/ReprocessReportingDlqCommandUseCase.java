package com.arka.reporting.application.port.in;

import com.arka.reporting.application.command.ReprocessReportingDlqCommand;
import reactor.core.publisher.Mono;

public interface ReprocessReportingDlqCommandUseCase {

    Mono<Void> handle(ReprocessReportingDlqCommand command);
}
