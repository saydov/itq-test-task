package com.github.saydov.documents.enums;

/**
 * Действия, которые можно выполнить над документом для смены его статуса.
 *
 * <p>Каждое действие соответствует конкретному переходу в жизненном цикле
 * {@link DocumentStatus}: выполнение действия переводит документ
 * из текущего статуса в следующий.</p>
 *
 * @see DocumentStatus
 */
public enum DocumentAction {

    /**
     * Отправка документа на согласование.
     * Переводит документ из статуса {@link DocumentStatus#DRAFT}
     * в {@link DocumentStatus#SUBMITTED}.
     */
    SUBMIT,

    /**
     * Согласование документа.
     * Переводит документ из статуса {@link DocumentStatus#SUBMITTED}
     * в {@link DocumentStatus#APPROVED}.
     */
    APPROVE
}
