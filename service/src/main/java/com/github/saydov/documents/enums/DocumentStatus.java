package com.github.saydov.documents.enums;

import com.github.saydov.documents.service.DocumentStateService;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Жизненный цикл документа, представленный в виде линейной цепочки статусов.
 *
 * <p>Переходы строго последовательны:
 * {@code DRAFT -> SUBMITTED -> APPROVED}.
 * Каждый статус знает свой следующий шаг и связанное с ним действие
 * ({@link DocumentAction}), что позволяет валидировать допустимость перехода
 * через {@link #canTransitionTo(DocumentStatus)}.</p>
 *
 * @see DocumentAction
 * @see DocumentStateService
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum DocumentStatus {

    /**
     * Черновик. Начальное состояние документа сразу после создания.
     * Следующий допустимый переход: {@link #SUBMITTED}.
     */
    DRAFT(null) {
        @Override
        public DocumentStatus next() {
            return SUBMITTED;
        }
    },

    /**
     * Отправлен на согласование. Документ перешел из черновика
     * после выполнения действия {@link DocumentAction#SUBMIT}.
     * Следующий допустимый переход: {@link #APPROVED}.
     */
    SUBMITTED(DocumentAction.SUBMIT) {
        @Override
        public DocumentStatus next() {
            return APPROVED;
        }
    },

    /**
     * Согласован. Финальное состояние документа, достигаемое
     * после выполнения действия {@link DocumentAction#APPROVE}.
     * Дальнейшие переходы не предусмотрены ({@link #next()} возвращает {@code null}).
     */
    APPROVED(DocumentAction.APPROVE) {
        @Override
        public DocumentStatus next() {
            return null;
        }
    };

    private final DocumentAction action;

    public abstract DocumentStatus next();

    public boolean canTransitionTo(DocumentStatus target) {
        return next() == target;
    }
}
