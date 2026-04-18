package com.arka.directory.application.port.in;

import com.arka.directory.application.query.GetDirectoryAdminSummaryQuery;
import com.arka.directory.application.result.DirectoryAdminSummaryResult;
import reactor.core.publisher.Mono;

public interface GetDirectoryAdminSummaryQueryUseCase {

    Mono<DirectoryAdminSummaryResult> handle(GetDirectoryAdminSummaryQuery query);
}
