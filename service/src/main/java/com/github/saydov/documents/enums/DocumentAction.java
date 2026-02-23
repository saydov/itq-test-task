package com.github.saydov.documents.enums;

import com.github.saydov.documents.dto.StatusTransition;
import com.github.saydov.documents.dto.request.BatchStatusRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.function.Function;

/**
 * Действия, которые можно выполнить над документом для смены его статуса.
 *
 * <p>Каждое действие соответствует конкретному переходу в жизненном цикле
 * {@link DocumentStatus}: выполнение действия переводит документ
 * из текущего статуса в следующий.</p>
 *
 * @see DocumentStatus
 */
@Getter
@RequiredArgsConstructor
public enum DocumentAction {

    /**
     * Отправка документа на согласование.
     * Переводит документ из статуса {@link DocumentStatus#DRAFT}
     * в {@link DocumentStatus#SUBMITTED}.
     */
    SUBMIT(StatusTransition::submit),

    /**
     * Согласование документа.
     * Переводит документ из статуса {@link DocumentStatus#SUBMITTED}
     * в {@link DocumentStatus#APPROVED}.
     */
    APPROVE(StatusTransition::approve);

    private final Function<BatchStatusRequest, StatusTransition> transitionFactory;

}
