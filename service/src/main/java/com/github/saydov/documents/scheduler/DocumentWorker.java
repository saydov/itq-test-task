package com.github.saydov.documents.scheduler;

import com.github.saydov.documents.dto.StatusChangeResult;
import com.github.saydov.documents.enums.DocumentStatus;

import java.util.List;

/**
 * Контракт для фоновых обработчиков документов.
 *
 * <p>Каждая реализация отвечает за обработку документов в определённом статусе,
 * выполняя переход в следующий статус по бизнес-правилам (например, DRAFT -> SUBMITTED).
 *
 * <p>Типовой жизненный цикл:
 * <ol>
 *   <li>Планировщик вызывает обработчик по расписанию</li>
 *   <li>Обработчик находит документы в {@link #getTargetStatus() целевом статусе}</li>
 *   <li>Документы передаются в {@link #applyStatusChange(List)} для выполнения перехода</li>
 * </ol>
 *
 * @see AbstractDocumentWorker
 */
public interface DocumentWorker {

    /**
     * Статус документов, которые должен обрабатывать данный воркер.
     *
     * @return статус для выборки документов из БД
     */
    DocumentStatus getTargetStatus();

    /**
     * Обрабатывает пакет документов по их идентификаторам.
     *
     * @param ids список идентификаторов документов для обработки
     * @return результат обработки каждого документа
     */
    List<StatusChangeResult> applyStatusChange(List<Long> ids);
}
