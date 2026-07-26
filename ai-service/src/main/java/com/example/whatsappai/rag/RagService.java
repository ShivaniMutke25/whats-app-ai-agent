package com.example.whatsappai.rag;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RagService {

    private final List<RetrievedDocument> documents = List.of(
            new RetrievedDocument("Refunds are allowed within 30 days for unused items.", 0.96),
            new RetrievedDocument("Orders can be canceled before dispatch. After dispatch, cancellation is not possible.", 0.9),
            new RetrievedDocument("Delivery usually takes 3-5 business days.", 0.88)
    );

    public List<RetrievedDocument> retrieve(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        List<RetrievedDocument> matches = new ArrayList<>();
        String normalized = query.toLowerCase();
        for (RetrievedDocument document : documents) {
            if (normalized.contains("refund") && document.content().toLowerCase().contains("refund")) {
                matches.add(document);
            } else if (normalized.contains("cancel") && document.content().toLowerCase().contains("cancel")) {
                matches.add(document);
            } else if (normalized.contains("delivery") && document.content().toLowerCase().contains("delivery")) {
                matches.add(document);
            }
        }
        return matches.isEmpty() ? List.of(documents.getFirst()) : matches;
    }
}
