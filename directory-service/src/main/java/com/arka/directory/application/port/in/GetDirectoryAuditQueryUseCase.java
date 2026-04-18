package com.arka.directory.application.port.in;

import com.arka.directory.application.query.GetDirectoryAuditQuery;
import com.arka.directory.application.result.DirectoryAuditResult;
import reactor.core.publisher.Mono;

public interface GetDirectoryAuditQueryUseCase {

    Mono<DirectoryAuditResult> handle(GetDirectoryAuditQuery query);
}
