package com.github.saydov.documents;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.saydov.documents.dto.request.BatchStatusRequest;
import com.github.saydov.documents.dto.request.CreateDocumentRequest;
import com.github.saydov.documents.entity.ApprovalRegistry;
import com.github.saydov.documents.enums.DocumentAction;
import com.github.saydov.documents.enums.DocumentStatus;
import com.github.saydov.documents.enums.OperationStatus;
import com.github.saydov.documents.repository.ApprovalRegistryRepository;
import com.github.saydov.documents.repository.DocumentRepository;
import com.github.saydov.documents.repository.StatusHistoryRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfig.class)
class DocumentApiTest {

    private static final String BASE_URL = "/api/documents";
    private static final long NON_EXISTENT_ID = 99999L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private StatusHistoryRepository statusHistoryRepository;

    @Autowired
    private ApprovalRegistryRepository approvalRegistryRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void cleanup() {
        approvalRegistryRepository.deleteAll();
        statusHistoryRepository.deleteAll();
        documentRepository.deleteAll();
    }

    @Test
    @DisplayName("Полный цикл: создание -> отправка -> согласование")
    void happyPath_createSubmitApprove() throws Exception {
        var id = createDocument("Mikhail", "Report");

        mockMvc.perform(get(BASE_URL + "/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(DocumentStatus.DRAFT.name()))
                .andExpect(jsonPath("$.author").value("Mikhail"))
                .andExpect(jsonPath("$.history", hasSize(0)));

        submitDocuments(List.of(id), "Mikhail")
                .andExpect(jsonPath("$[0].status").value(OperationStatus.SUCCESS.name()));

        mockMvc.perform(get(BASE_URL + "/{id}", id))
                .andExpect(jsonPath("$.status").value(DocumentStatus.SUBMITTED.name()))
                .andExpect(jsonPath("$.history", hasSize(1)))
                .andExpect(jsonPath("$.history[0].action").value(DocumentAction.SUBMIT.name()));

        approveDocuments(List.of(id), "Bogdan")
                .andExpect(jsonPath("$[0].status").value(OperationStatus.SUCCESS.name()));

        mockMvc.perform(get(BASE_URL + "/{id}", id))
                .andExpect(jsonPath("$.status").value(DocumentStatus.APPROVED.name()))
                .andExpect(jsonPath("$.history", hasSize(2)))
                .andExpect(jsonPath("$.history[1].action").value(DocumentAction.APPROVE.name()));

        assertThat(approvalRegistryRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Массовая отправка: частичный результат (SUCCESS, CONFLICT, NOT_FOUND)")
    void bulkSubmit_partialResults() throws Exception {
        var draftId = createDocument("Mikhail", "Draft doc");
        var submittedId = createDocument("Bogdan", "Already submitted");
        submitDocuments(List.of(submittedId), "Bogdan");

        submitDocuments(List.of(draftId, submittedId, NON_EXISTENT_ID), "Mikhail")
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].status").value(OperationStatus.SUCCESS.name()))
                .andExpect(jsonPath("$[1].status").value(OperationStatus.CONFLICT.name()))
                .andExpect(jsonPath("$[2].status").value(OperationStatus.NOT_FOUND.name()));
    }

    @Test
    @DisplayName("Массовое согласование: частичный результат (SUCCESS, CONFLICT, NOT_FOUND)")
    void bulkApprove_partialResults() throws Exception {
        var submittedId = createDocument("Mikhail", "Doc 1");
        submitDocuments(List.of(submittedId), "Mikhail");

        var draftId = createDocument("Bogdan", "Doc 2");

        approveDocuments(List.of(submittedId, draftId, NON_EXISTENT_ID), "Alexey")
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].status").value(OperationStatus.SUCCESS.name()))
                .andExpect(jsonPath("$[1].status").value(OperationStatus.CONFLICT.name()))
                .andExpect(jsonPath("$[2].status").value(OperationStatus.NOT_FOUND.name()));
    }

    @Test
    @DisplayName("Согласование: откат при ошибке записи в реестр")
    void approve_rollbackOnRegistryFailure() throws Exception {
        var id = createDocument("Mikhail", "Doc");
        submitDocuments(List.of(id), "Mikhail");

        var document = documentRepository.findById(id).orElseThrow();
        approvalRegistryRepository.saveAndFlush(ApprovalRegistry.of(document, "Bogdan"));

        approveDocuments(List.of(id), "Alexey")
                .andExpect(jsonPath("$[0].status").value(OperationStatus.REGISTRY_ERROR.name()));

        entityManager.clear();

        var reloaded = documentRepository.findById(id).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(DocumentStatus.SUBMITTED);

        var history = statusHistoryRepository.findByDocumentIdOrderByCreatedAtAsc(id);
        assertThat(history).hasSize(1);
    }

    @Test
    @DisplayName("Получение документа: 404 для несуществующего ID")
    void getDocument_notFound() throws Exception {
        mockMvc.perform(get(BASE_URL + "/{id}", NON_EXISTENT_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    @DisplayName("Создание документа: ошибка валидации при пустых полях")
    void createDocument_validationError() throws Exception {
        var json = objectMapper.writeValueAsString(new CreateDocumentRequest("", ""));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("Поиск документов с фильтрами по автору и статусу")
    void searchDocuments_withFilters() throws Exception {
        createDocument("Mikhail", "Report 1");
        createDocument("Mikhail", "Report 2");
        createDocument("Bogdan", "Report 3");

        mockMvc.perform(get(BASE_URL + "/search")
                        .param("author", "Mikhail")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));

        mockMvc.perform(get(BASE_URL + "/search")
                        .param("status", DocumentStatus.DRAFT.name())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)));
    }

    @Test
    @DisplayName("Пакетное получение документов по списку ID")
    void batchGetDocuments() throws Exception {
        var id1 = createDocument("Mikhail", "Doc 1");
        var id2 = createDocument("Bogdan", "Doc 2");

        mockMvc.perform(get(BASE_URL + "/batch")
                        .param("ids", id1.toString(), id2.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    // -- helpers --

    private Long createDocument(String author, String title) throws Exception {
        var json = objectMapper.writeValueAsString(new CreateDocumentRequest(author, title));

        var result = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private ResultActions submitDocuments(List<Long> ids, String initiator) throws Exception {
        var json = objectMapper.writeValueAsString(new BatchStatusRequest(ids, initiator, null));
        return mockMvc.perform(post(BASE_URL + "/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }

    private ResultActions approveDocuments(List<Long> ids, String initiator) throws Exception {
        var json = objectMapper.writeValueAsString(new BatchStatusRequest(ids, initiator, null));
        return mockMvc.perform(post(BASE_URL + "/approve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }
}
