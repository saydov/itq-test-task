package com.github.saydov.documents.service.transition;

import com.github.saydov.documents.dto.StatusChangeResult;
import com.github.saydov.documents.dto.StatusTransition;
import com.github.saydov.documents.entity.ApprovalRegistry;
import com.github.saydov.documents.entity.Document;
import com.github.saydov.documents.entity.StatusHistory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class TransitionCollector {

    private final List<Outcome> outcomes;

    private TransitionCollector(int capacity) {
        this.outcomes = new ArrayList<>(capacity);
    }

    public static TransitionCollector withCapacity(int capacity) {
        return new TransitionCollector(capacity);
    }

    public void success(Document doc, StatusTransition transition) {
        doc.setStatus(transition.targetStatus());

        var history = StatusHistory.of(doc, transition.initiator(),
                transition.targetStatus().getAction(), transition.comment());

        var approval = transition.isApproval()
                ? ApprovalRegistry.of(doc, transition.initiator()) : null;

        outcomes.add(new Outcome.Success(doc.getId(), history, approval));
    }

    public void failure(StatusChangeResult result) {
        outcomes.add(new Outcome.Failure(result));
    }

    public List<StatusChangeResult> results() {
        return outcomes.stream().map(Outcome::toResult).toList();
    }

    public void drainTo(Consumer<List<StatusHistory>> historySink, Consumer<List<ApprovalRegistry>> approvalSink) {
        var histories = new ArrayList<StatusHistory>();
        var approvals = new ArrayList<ApprovalRegistry>();

        for (var outcome : outcomes) {
            if (outcome instanceof Outcome.Success s) {
                histories.add(s.history());

                if (s.approval() != null) {
                    approvals.add(s.approval());
                }
            }
        }

        historySink.accept(histories);
        if (!approvals.isEmpty()) {
            approvalSink.accept(approvals);
        }
    }

    private interface Outcome {

        StatusChangeResult toResult();

        record Success(Long id, StatusHistory history, ApprovalRegistry approval) implements Outcome {
            @Override
            public StatusChangeResult toResult() {
                return StatusChangeResult.success(id);
            }
        }

        record Failure(StatusChangeResult result) implements Outcome {
            @Override
            public StatusChangeResult toResult() {
                return result;
            }
        }
    }
}