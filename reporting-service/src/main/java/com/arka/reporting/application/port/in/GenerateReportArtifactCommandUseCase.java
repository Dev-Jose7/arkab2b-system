package com.arka.reporting.application.port.in;

import com.arka.reporting.application.command.GenerateReportArtifactCommand;
import com.arka.reporting.application.result.ReportArtifactResult;
import reactor.core.publisher.Mono;

public interface GenerateReportArtifactCommandUseCase {

    Mono<ReportArtifactResult> handle(GenerateReportArtifactCommand command);
}
